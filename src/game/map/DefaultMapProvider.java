package game.map;

import game.engine.*;
import game.map.entities.*;
import javafx.scene.input.KeyCode;

import java.util.*;

public class DefaultMapProvider implements MapProvider {

    private final Random random = new Random();
    private final Map<UUID, AITimer> aiTimers = new HashMap<>(); // 每个AI坦克的独立计时器
    private double waveTimer = 0;
    private int currentWave = 0;
    private double LifeCapsuleTimer = 0;
    private final int LifeCapsuleInterval = 3; // 每20秒生成一个生命胶囊

    private static class AITimer {
        double changeDirectionTimer;
        double fireTimer;

        AITimer() {
            this.changeDirectionTimer = 0;
            this.fireTimer = 0;
        }
    }

    public DefaultMapProvider() {
        EventBus.getInstance().subscribe("SpawnShell", this::handleSpawnShell);
    }

    @Override
    public String getMapName() {
        return "default map";
    }

    @Override
    public int getMapWidth() {
        return 800;
    }

    @Override
    public int getMapHeight() {
        return 600;
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

            context.getImageManager().loadImage("heart", "assets/images/heart.png");
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + e.getMessage());
        }

        context.getSoundManager().playSoundEffect("start");
    }

    @Override
    public void createEntities(GameContext context) {
        GameState state = context.getState();

        state.setWorldWidth(getMapWidth());
        state.setWorldHeight(getMapHeight());

        // 队伍0（玩家）
        TankEntity playerTank = new TankEntity(100, 500, 0);
        state.addEntity(playerTank);
        state.addTeamEntity(0, playerTank);

        BaseStructure playerBase = new BaseStructure(50, 550, 0);
        state.addEntity(playerBase);
        state.setTeamBase(0, playerBase);

        // 队伍1（AI）
        TankEntity aiTank1 = new TankEntity(600, 50, 1);
        state.addEntity(aiTank1);
        state.addTeamEntity(1, aiTank1);

        TankEntity aiTank2 = new TankEntity(650, 50, 1);
        state.addEntity(aiTank2);
        state.addTeamEntity(1, aiTank2);

        BaseStructure aiBase = new BaseStructure(700, 50, 1);
        state.addEntity(aiBase);
        state.setTeamBase(1, aiBase);

        // 地图障碍物
        for (int i = 0; i < 5; i++) {
            state.addEntity(new BrickWallTile(350 + i * 40, 250));
        }
        state.addEntity(new SteelWallTile(300, 300));
        state.addEntity(new SteelWallTile(500, 300));
        for (int i = 0; i < 3; i++) {
            state.addEntity(new WaterTile(200 + i * 40, 400));
        }

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
            LifeCapsule capsule = new LifeCapsule(x, y);
            state.addEntity(capsule);
            System.out.println("[Bonus] Life capsule spawned at (" + String.format("%.2f", x) + ", "
                    + String.format("%.2f", y) + ")");
        }
        // AI控制逻辑（队伍1）
        updateAI(1, deltaTime, state);

        // 增援逻辑（队伍1每10秒生成2个单位）
        Object base = state.getTeamBase(1);
        if (base instanceof Entity entity && entity.isAlive()) {
            updateReinforcement(1, deltaTime, state);
        }

        // 清理不存在的坦克的AI计时器
        cleanupAITimers(state);
    }

    private void updateAI(int teamIndex, double deltaTime, GameState state) {
        List<Object> teamEntities = state.getTeamEntityList(teamIndex);

        for (Object obj : teamEntities) {
            if (obj instanceof TankEntity tank && tank.isAlive()) {
                // 获取或创建该坦克的独立计时器
                AITimer timer = aiTimers.computeIfAbsent(tank.getId(), k -> new AITimer());

                timer.changeDirectionTimer -= deltaTime;
                timer.fireTimer -= deltaTime;

                // 方向控制
                if (timer.changeDirectionTimer <= 0) {
                    changeAIDirection(tank);
                    timer.changeDirectionTimer = 2.0 + random.nextDouble() * 2.0;
                }

                // 开火控制
                if (timer.fireTimer <= 0) {
                    tank.fireShell(GameContext.getInstance());
                    timer.fireTimer = 1.0 + random.nextDouble();
                }

                // 边界检查
                if (tank.getX() < 20 || tank.getX() > state.getWorldWidth() - 60 ||
                        tank.getY() < 20 || tank.getY() > state.getWorldHeight() - 60) {
                    // changeAIDirection(tank);
                    // timer.changeDirectionTimer = 2.0 + random.nextDouble() * 2.0;
                }
            }
        }
    }

    private void changeAIDirection(TankEntity tank) {
        int direction = random.nextInt(4);
        switch (direction) {
            case 0 -> {
                tank.setVelocity(0, -150);
                tank.setRotation(-Math.PI / 2);
            }
            case 1 -> {
                tank.setVelocity(0, 150);
                tank.setRotation(Math.PI / 2);
            }
            case 2 -> {
                tank.setVelocity(-150, 0);
                tank.setRotation(Math.PI);
            }
            case 3 -> {
                tank.setVelocity(150, 0);
                tank.setRotation(0);
            }
        }
    }

    private void updateReinforcement(int teamIndex, double deltaTime, GameState state) {
        waveTimer += deltaTime;

        if (waveTimer >= 10.0) {
            waveTimer = 0;
            currentWave++;

            for (int i = 0; i < 2; i++) {
                TankEntity tank = new TankEntity(400 + i * 50, 50, teamIndex);
                state.addEntity(tank);
                state.addTeamEntity(teamIndex, tank);
            }

            System.out.println("[Wave " + currentWave + "] Team 1 reinforcements: 2 units");
        }
    }

    @Override
    public void onKeyPressed(KeyCode key, GameContext context) {

        // 重生

        if (key == KeyCode.R) {
            System.out.println("[Player] Respawn requested");
            BaseStructure playerBase = (BaseStructure) context.state.getTeamBase(0);
            if (playerBase == null || !playerBase.isAlive()) {
                return;
            }

            if (context.state.getTeamEntityList(0).isEmpty()) {
                TankEntity playerTank = new TankEntity(100, 500, 0);
                context.state.addEntity(playerTank);
                context.state.addTeamEntity(0, playerTank);
            }
            return;
        }

        TankEntity playerTank = getPlayerTank(context);
        if (playerTank == null)
            return;

        // 存活

        switch (key) {
            case UP -> {
                playerTank.setVelocity(0, -200);
                playerTank.setRotation(-Math.PI / 2);
                context.getSoundManager().playBGM("tank_move");
            }
            case DOWN -> {
                playerTank.setVelocity(0, 200);
                playerTank.setRotation(Math.PI / 2);
                context.getSoundManager().playBGM("tank_move");
            }
            case LEFT -> {
                playerTank.setVelocity(-200, 0);
                playerTank.setRotation(Math.PI);
                context.getSoundManager().playBGM("tank_move");
            }
            case P -> {
                context.state.addEntity(new SteelWallTile(playerTank.getX() + 60, playerTank.getY()));
            }
            case RIGHT -> {
                playerTank.setVelocity(200, 0);
                playerTank.setRotation(0);
                context.getSoundManager().playBGM("tank_move");
            }
            case SPACE -> {
                context.getSoundManager().playSoundEffect("tank_fire");
                playerTank.fireShell(context);
            }

            default -> {
                // do nothing
            }
        }
    }

    @Override
    public void onKeyReleased(KeyCode key, GameContext context) {
        TankEntity playerTank = getPlayerTank(context);
        if (playerTank == null)
            return;

        if (key == KeyCode.UP || key == KeyCode.DOWN ||
                key == KeyCode.LEFT || key == KeyCode.RIGHT) {
            playerTank.setVelocity(0, 0);
            context.getSoundManager().stopSound("tank_move");
        }
    }

    private TankEntity getPlayerTank(GameContext context) {
        return context.getState().getTeamEntityList(0).stream()
                .filter(e -> e instanceof TankEntity)
                .map(e -> (TankEntity) e)
                .filter(Entity::isAlive)
                .findFirst()
                .orElse(null);
    }

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
                if (obj instanceof TankEntity tank && tank.isAlive()) {
                    aliveTankIds.add(tank.getId());
                }
            }
        }

        // 只保留存活坦克的计时器
        aiTimers.keySet().retainAll(aliveTankIds);
    }
}