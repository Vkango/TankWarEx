package game.map;

import game.engine.*;
import game.map.entities.*;
import javafx.scene.input.KeyCode;
import game.map.entities.tanks.*;
import game.map.MapXMLParser;

import java.util.*;

public class DefaultMapProvider implements MapProvider {

    private final Random random = new Random();
    private double waveTimer = 0;
    private int currentWave = 0;
    private double LifeCapsuleTimer = 0;
    private final int LifeCapsuleInterval = 3; // 每20秒生成一个生命胶囊
    private final MapXMLParser mapParser = new MapXMLParser();

    public DefaultMapProvider() {
        EventBus.getInstance().subscribe("SpawnShell", this::handleSpawnShell);
    }

    @Override
    public String getMapName() {
        return "default map";
    }

    @Override
    public int getMapWidth() {
        return 1366;
    }

    @Override
    public int getMapHeight() {
        return 768;
    }

    @Override
    public void initResources(GameContext context) {
        // 加载声音图形资源
        // 加载地图资源
        try {
            context.getSoundManager().loadSound("tank_fire", "assets/sounds/shoot.wav");
            context.getSoundManager().loadSound("explosion", "assets/sounds/eexplosion.wav");
            context.getSoundManager().loadSound("tank_move", "assets/sounds/moving.wav");
            context.getSoundManager().loadSound("bonus", "assets/sounds/bonus.wav");
            context.getSoundManager().loadSound("steelhit", "assets/sounds/steelhit.wav");
            context.getSoundManager().loadSound("start", "assets/sounds/levelstarting.wav");
            context.getSoundManager().loadSound("gameover", "assets/sounds/gameover.wav");
            context.getSoundManager().loadSound("reflector", "assets/sounds/shieldhit.wav");
            context.getImageManager().loadImage("heart", "assets/images/capsule.png");
            context.getImageManager().loadImage("grass", "assets/images/grass.png");
            context.getImageManager().loadImage("base", "assets/images/base.png");
            context.getImageManager().loadImage("ice", "assets/images/ice.png");

        } catch (Exception e) {
            System.err.println("Failed to load sound: " + e.getMessage());
        }

        context.getSoundManager().playSoundEffect("start");
    }

    @Override
    public void createEntities(GameContext context) {
        GameState state = context.getState();

        int[] mapSize = mapParser.loadMapFromXML("assets/maps/default.xml", 40);

        state.setWorldWidth(mapSize[0] * 40);
        state.setWorldHeight(mapSize[1] * 40);

        TimerEntity timer = new TimerEntity(10, 10);
        state.addEntity(timer);

        // 队伍0（玩家）
        PlayerTank playerTank = new PlayerTank(100, 600, 0);
        state.addEntity(playerTank);
        state.addTeamEntity(0, playerTank);

        BaseStructure playerBase = new BaseStructure(50, 550, 0);
        state.addEntity(playerBase);
        state.setTeamBase(0, playerBase);

        // 队伍1（AI）
        AITank aiTank1 = new AITank(600, 50, 1);
        state.addEntity(aiTank1);
        state.addTeamEntity(1, aiTank1);

        AITank aiTank2 = new AITank(650, 50, 1);
        state.addEntity(aiTank2);
        state.addTeamEntity(1, aiTank2);

        BaseStructure aiBase = new BaseStructure(700, 50, 1);
        state.addEntity(aiBase);
        state.setTeamBase(1, aiBase);

        mapParser.placeNodes(state);

        // // 地图障碍物
        // for (int i = 0; i < 5; i++) {
        // state.addEntity(new BrickWallTile(350 + i * 40, 250));
        // }
        // state.addEntity(new SteelWallTile(300, 300));
        // state.addEntity(new SteelWallTile(500, 300));
        // for (int i = 0; i < 3; i++) {
        // state.addEntity(new WaterTile(200 + i * 40, 400));
        // }

    }

    @Override
    public void update(double deltaTime, GameContext context) {
        GameState state = context.getState();
        LifeCapsuleTimer += deltaTime;
        if (LifeCapsuleTimer >= LifeCapsuleInterval) {
            LifeCapsuleTimer = 0;
            // 在随机位置生成生命胶囊
            double x = 50 + random.nextDouble() * (state.getWorldWidth() - 100);
            double y = 50 + random.nextDouble() * (state.getWorldHeight() - 100);
            if (random.nextInt(2) == 0) {
                LifeCapsule capsule = new LifeCapsule(x, y);
                state.addEntity(capsule);
                System.out.println("[Bonus] Life capsule spawned at (" + String.format("%.2f", x) + ", "
                        + String.format("%.2f", y) + ")");
            } else {
                Freezer capsule = new Freezer(x, y);
                state.addEntity(capsule);
                System.out.println("[Bonus] Freezer capsule spawned at (" + String.format("%.2f", x) + ", "
                        + String.format("%.2f", y) + ")");
            }

        }

        // 增援逻辑（队伍1每10秒生成2个单位）
        Object base = state.getTeamBase(1);
        if (base instanceof Entity entity && entity.isAlive()) {
            updateReinforcement(1, deltaTime, state);
        }

        // 清理不存在的坦克的AI计时器
        cleanupAITimers(state);
    }

    private void updateReinforcement(int teamIndex, double deltaTime, GameState state) {
        waveTimer += deltaTime;

        if (waveTimer >= 10.0) {
            waveTimer = 0;
            currentWave++;

            for (int i = 0; i < 2; i++) {
                AITank tank = new AITank(400 + i * 50, 50, teamIndex);
                state.addEntity(tank);
                state.addTeamEntity(teamIndex, tank);
            }

            System.out.println("[Wave " + currentWave + "] Team 1 reinforcements: 2 units");
        }
    }

    @Override
    public void onKeyPressed(KeyCode key, GameContext context) {
        // 处理重生键
        if (key == KeyCode.R) {
            System.out.println("[Player] Respawn requested");
            BaseStructure playerBase = (BaseStructure) context.state.getTeamBase(0);
            if (playerBase == null || !playerBase.isAlive()) {
                return;
            }

            if (context.state.getTeamEntityList(0).isEmpty()) {
                PlayerTank playerTank = new PlayerTank(100, 600, 0);
                context.state.addEntity(playerTank);
                context.state.addTeamEntity(0, playerTank);
            }
            return;
        }

        // 处理调试键
        PlayerTank playerTank = getPlayerTank(context);
        if (playerTank == null) {
            return;
        }

        switch (key) {
            case P -> {
                context.state.addEntity(new Reflector(playerTank.getX() + 60, playerTank.getY()));
            }
            case G -> {
                context.state.addEntity(new SpeedUp(playerTank.getX() + 60, playerTank.getY()));
            }
            default -> {
                // do nothing
            }
        }
    }

    @Override
    public void onKeyReleased(KeyCode key, GameContext context) {
        // 键盘释放事件现在由PlayerTank.handleInput处理
        // 这里保留空方法以满足接口要求
    }

    private PlayerTank getPlayerTank(GameContext context) {
        return context.getState().getTeamEntityList(0).stream()
                .filter(e -> e instanceof PlayerTank)
                .map(e -> (PlayerTank) e)
                .filter(Entity::isAlive)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void handleSpawnShell(GameEvent event) {
        Map<String, Object> data = (Map<String, Object>) event.getData();
        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double angle = (double) data.get("angle");
        int teamIndex = (int) data.get("teamIndex");
        UUID ownerTankId = (UUID) data.get("ownerTankId");

        ShellEntity shell = new ShellEntity(x, y, angle, teamIndex, ownerTankId);
        GameContext.getInstance().getState().addEntity(shell);
    }

    /**
     * 清理不存在的坦克的AI计时器，防止内存泄漏
     */
    private void cleanupAITimers(GameState state) {
        // 获取所有存活的坦克ID
        Set<UUID> aliveTankIds = new HashSet<>();
        for (int team = 0; team < 2; team++) {
            List<Object> teamEntities = state.getTeamEntityList(team);
            for (Object obj : teamEntities) {
                if (obj instanceof BaseTankEntity tank && tank.isAlive()) {
                    aliveTankIds.add(tank.getId());
                }
            }
        }
    }
}