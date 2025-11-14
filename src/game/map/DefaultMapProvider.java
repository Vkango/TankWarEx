package game.map;

import game.engine.*;
import game.map.entities.*;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DefaultMapProvider implements MapProvider {

    private final Random random = new Random();
    private double aiChangeDirectionTimer = 0;
    private double aiFireTimer = 0;
    private double waveTimer = 0;
    private int currentWave = 0;

    public DefaultMapProvider() {
        // 订阅子弹生成事件
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

        // AI控制逻辑（队伍1）
        updateAI(1, deltaTime, state);

        // 增援逻辑（队伍1每10秒生成2个单位）
        Object base = state.getTeamBase(1);
        if (base instanceof Entity entity && entity.isAlive()) {
            updateReinforcement(1, deltaTime, state);
        }

    }

    private void updateAI(int teamIndex, double deltaTime, GameState state) {
        List<Object> teamEntities = state.getTeamEntityList(teamIndex);

        aiChangeDirectionTimer -= deltaTime;
        aiFireTimer -= deltaTime;

        for (Object obj : teamEntities) {
            if (obj instanceof TankEntity tank && tank.isAlive()) {
                if (aiChangeDirectionTimer <= 0) {
                    changeAIDirection(tank);
                    aiChangeDirectionTimer = 2.0 + random.nextDouble() * 2.0;
                }

                if (aiFireTimer <= 0) {
                    tank.fireShell(GameContext.getInstance());
                    aiFireTimer = 1.0 + random.nextDouble();
                }

                if (tank.getX() < 20 || tank.getX() > state.getWorldWidth() - 60 ||
                        tank.getY() < 20 || tank.getY() > state.getWorldHeight() - 60) {
                    changeAIDirection(tank);
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
        TankEntity playerTank = getPlayerTank(context);
        if (playerTank == null)
            return;

        switch (key) {
            case UP -> {
                playerTank.setVelocity(0, -200);
                playerTank.setRotation(-Math.PI / 2);
            }
            case DOWN -> {
                playerTank.setVelocity(0, 200);
                playerTank.setRotation(Math.PI / 2);
            }
            case LEFT -> {
                playerTank.setVelocity(-200, 0);
                playerTank.setRotation(Math.PI);
            }
            case RIGHT -> {
                playerTank.setVelocity(200, 0);
                playerTank.setRotation(0);
            }
            case SPACE -> playerTank.fireShell(context);
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
}
