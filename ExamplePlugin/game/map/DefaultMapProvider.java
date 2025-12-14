package game.map;

import game.engine.*;
import game.map.entities.*;
import javafx.scene.input.KeyCode;
import game.map.entities.tanks.*;

import java.util.*;

public class DefaultMapProvider implements MapProvider {

    private final Random random = new Random();
    private double waveTimer = 0;
    private int currentWave = 0;
    private double LifeCapsuleTimer = 0;
    private final int LifeCapsuleInterval = 3; // 每20秒生成一个生命胶囊
    private final MapXMLParser mapParser = new MapXMLParser();
    private int width = 800;
    private int height = 600;

    public DefaultMapProvider() {

    }

    @Override
    public String getMapName() {
        return "爱来自安慕希";
    }

    @Override
    public int getMapWidth() {
        return width;
    }

    @Override
    public int getMapHeight() {
        return height;
    }

    @Override
    public void initResources(GameContext context) {
        EventBus.getInstance().clearType("SpawnShell");
        EventBus.getInstance().subscribe("SpawnShell", this::handleSpawnShell);
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
            context.getImageManager().loadImage("tank_base", "assets/images/tank_base.png");

            context.getImageManager().loadImage("steels", "assets/images/steels.png");
            context.getImageManager().loadImage("tank_base1", "assets/images/tank_base1.png");
            context.getImageManager().loadImage("tank_base2", "assets/images/tank_base2.png");
            context.getImageManager().loadImage("tank_base3", "assets/images/tank_base3.png");
            context.getImageManager().loadImage("aitank_base1", "assets/images/aitank_base1.png");
            context.getImageManager().loadImage("aitank_base2", "assets/images/aitank_base2.png");
            context.getImageManager().loadImage("aitank_base3", "assets/images/aitank_base3.png");
            context.getImageManager().loadImage("aitank_base4", "assets/images/aitank_base4.png");
            context.getImageManager().loadImage("explosion1", "assets/images/explosion1.png");
            context.getImageManager().loadImage("explosion2", "assets/images/explosion2.png");
            context.getImageManager().loadImage("explosion3", "assets/images/explosion3.png");
            context.getImageManager().loadImage("explosion4", "assets/images/explosion4.png");
            context.getImageManager().loadImage("explosion5", "assets/images/explosion5.png");
            context.getImageManager().loadImage("shell1", "assets/images/shell1.png");
            context.getImageManager().loadImage("shell2", "assets/images/shell2.png");
            context.getImageManager().loadImage("shell3", "assets/images/shell3.png");
            context.getImageManager().loadImage("shell4", "assets/images/shell4.png");
            context.getImageManager().loadImage("sand", "assets/images/sand.png");
            context.getImageManager().loadImage("timer", "assets/images/timer.gif");
            context.getImageManager().loadImage("water", "assets/images/water.png");
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + e.getMessage());
        }

        context.getSoundManager().playSoundEffect("start");
    }

    @Override
    public void createEntities(GameContext context) {
        GameState state = context.getState();

        int[] mapSize = mapParser.loadMapFromXML("assets/maps/swordmap.xml", 40);

        width = mapSize[0] * 40;
        height = mapSize[1] * 40;

        TimerEntity timer = new TimerEntity(10, 10);
        state.addEntity(timer);

        PlayerTank playerTank = new PlayerTank(20, 590, 0);
        state.addEntity(playerTank);

        BaseStructure playerBase = new BaseStructure(50, 550, 0);
        state.addEntity(playerBase);

        AITank aiTank1 = new AITank(600, 50, 1);
        state.addEntity(aiTank1);

        AITank aiTank2 = new AITank(650, 50, 1);
        state.addEntity(aiTank2);

        BaseStructure aiBase = new BaseStructure(700, 50, 1);
        state.addEntity(aiBase);

        mapParser.placeNodes(state);
    }

    @Override
    public Entity getPlayerEntity(GameContext context) {
        GameState state = context.getState();
        if (state == null)
            return null;

        for (Object obj : state.getEntities()) {
            if (obj instanceof PlayerTank tank && tank.isAlive()) {
                return tank;
            }
        }
        return null;
    }

    @Override
    public void update(double deltaTime, GameContext context) {
        GameState state = context.getState();
        LifeCapsuleTimer += deltaTime;
        if (LifeCapsuleTimer >= LifeCapsuleInterval) {
            LifeCapsuleTimer = 0;
            double x = 50 + random.nextDouble() * (getMapWidth() - 100);
            double y = 50 + random.nextDouble() * (getMapHeight() - 100);
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
        BaseStructure enemyBase = null;
        for (Object obj : state.getEntities()) {
            if (obj instanceof BaseStructure base && base.getTeamIndex() == 1 && base.isAlive()) {
                enemyBase = base;
                break;
            }
        }

        if (enemyBase != null) {
            updateReinforcement(1, deltaTime, state);
        } else {
            // 基地不存在或已死亡，重置增援计时器
            waveTimer = 0;
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
            }

            System.out.println("[Wave " + currentWave + "] Team 1 reinforcements: 2 units");
        }
    }

    @Override
    public void onKeyPressed(KeyCode key, GameContext context) {
        // 处理重生键
        if (key == KeyCode.R) {
            System.out.println("[Player] Respawn requested");

            // 查找玩家基地
            BaseStructure playerBase = null;
            for (Object obj : context.state.getEntities()) {
                if (obj instanceof BaseStructure base && base.getTeamIndex() == 0 && base.isAlive()) {
                    playerBase = base;
                    break;
                }
            }

            if (playerBase == null) {
                context.showToast("你的基地已被摧毁，无法复活！");
                return;
            }

            // 检查是否有存活的玩家坦克
            boolean hasPlayerTank = false;
            for (Object obj : context.state.getEntities()) {
                if (obj instanceof PlayerTank tank && tank.isAlive()) {
                    hasPlayerTank = true;
                    break;
                }
            }

            if (!hasPlayerTank) {
                context.showToast("复活成功！");
                PlayerTank playerTank = new PlayerTank(20, 590, 0);
                context.state.addEntity(playerTank);
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

    }

    private PlayerTank getPlayerTank(GameContext context) {
        GameState state = context.getState();
        for (Object obj : state.getEntities()) {
            if (obj instanceof PlayerTank tank && tank.isAlive()) {
                return tank;
            }
        }
        return null;
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
        Set<UUID> aliveTankIds = new HashSet<>();
        for (Object obj : state.getEntities()) {
            if (obj instanceof BaseTankEntity tank && tank.isAlive()) {
                aliveTankIds.add(tank.getId());
            }
        }
    }
}