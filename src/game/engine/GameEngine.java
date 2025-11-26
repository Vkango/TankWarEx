package game.engine;

import game.map.MapProvider;
import game.rules.RuleProvider;
import javafx.scene.input.KeyCode;
import game.config.GameConfig;
import java.util.UUID;

/**
 * 职责：
 * 1. 生命周期管理（启动/停止/暂停）
 * 2. 输入路由（键盘事件 -> MapProvider）
 * 3. 更新调度（调用实体update、MapProvider.update）
 * 4. 碰撞检测
 * 
 * 游戏逻辑需要插件自己去实现
 */
public class GameEngine {
    private final GameContext context = GameContext.getInstance();
    private final EventBus eventBus = EventBus.getInstance();
    private final GameConfig config;

    private Thread gameThread;
    private volatile boolean running = false;
    private MapProvider mapProvider;
    private RuleProvider ruleProvider;

    public GameEngine() {
        this.config = context.getConfig();
        this.mapProvider = context.getMapProvider();
        this.ruleProvider = context.getRuleProvider();
    }

    public GameConfig getConfig() {
        return config;
    }

    public void initialize() {
        if (running) {
            stop();
        }

        context.reset();

        context.getState().setWorldWidth(mapProvider.getMapWidth());
        context.getState().setWorldHeight(mapProvider.getMapHeight());

        mapProvider.createEntities(context);

        eventBus.publish(new GameEvent("GameInitialized", null, "Game initialized"));
    }

    public void initResources() {
        mapProvider.initResources(context);
    }

    public void start() {
        if (running)
            return;

        running = true;
        context.getState().setRunning(true);

        gameThread = new Thread(this::gameLoop, "GameLogicThread");
        gameThread.setDaemon(true);
        gameThread.start();

        eventBus.publish(new GameEvent("GameStarted", null, "Game started"));
    }

    private void gameLoop() {
        long lastTime = System.nanoTime();
        double accumulator = 0;

        while (running) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            if (context.getState().isPaused()) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                lastTime = System.nanoTime();
                continue;
            }

            accumulator += deltaTime;

            while (accumulator >= config.getFrameTime()) {
                update(config.getFrameTime());
                accumulator -= config.getFrameTime();
            }

            // 检查游戏是否结束
            if (ruleProvider.isGameOver(context)) {
                running = false;
                eventBus.publish(new GameEvent("GameOver", null, ruleProvider.getGameOverMessage(context)));
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void update(double deltaTime) {
        GameState state = context.getState();

        mapProvider.update(deltaTime, context);

        // 保存所有可移动实体的位置，必要时回退(比如撞到了)
        java.util.Map<UUID, double[]> oldPositions = new java.util.HashMap<>();
        for (Object obj : state.getEntities()) {
            if (obj instanceof Controllable controllable && controllable.isAlive()) {
                oldPositions.put(controllable.getId(),
                        new double[] { controllable.getX(), controllable.getY() });
            }
        }

        for (Object obj : state.getEntities()) {
            if (obj instanceof Entity entity && entity.isAlive()) {
                entity.update(deltaTime);
            }
        }

        // 防止实体离开地图
        enforceBoundaries();

        // 碰撞检测与响应
        detectCollisionsAndRespond(oldPositions);

        // 清理死亡实体
        cleanupDeadEntities();
    }

    private void enforceBoundaries() {
        GameState state = context.getState();
        double worldWidth = state.getWorldWidth();
        double worldHeight = state.getWorldHeight();

        for (Object obj : state.getEntities()) {
            if (!(obj instanceof Controllable controllable) || !controllable.isAlive())
                continue;

            double x = controllable.getX();
            double y = controllable.getY();
            double width = controllable.getWidth();
            double height = controllable.getHeight();

            // 计算修正后的位置
            double newX = Math.max(0, Math.min(x, worldWidth - width));
            double newY = Math.max(0, Math.min(y, worldHeight - height));

            // 如果超出边界，停止移动并调整位置
            if (newX != x || newY != y) {
                controllable.setVelocity(0, 0);
                controllable.setPosition(newX, newY);
                controllable.noticeOutOfBounds();
            }
        }
    }

    private void detectCollisionsAndRespond(java.util.Map<UUID, double[]> oldPositions) {
        GameState state = context.getState();
        java.util.List<Object> entities = new java.util.ArrayList<>(state.getEntities());

        for (int i = 0; i < entities.size(); i++) {
            if (!(entities.get(i) instanceof Entity e1) || !e1.isAlive())
                continue;

            for (int j = i + 1; j < entities.size(); j++) {
                if (!(entities.get(j) instanceof Entity e2) || !e2.isAlive())
                    continue;

                if (e1.intersects(e2)) {
                    // 调用双方的碰撞处理，获取是否阻挡
                    boolean e1Blocked = e1.handleCollision(e2, context);
                    boolean e2Blocked = e2.handleCollision(e1, context);

                    // 如果e1被阻挡且是可控制实体，回退到旧位置
                    if (e1Blocked && e1 instanceof Controllable c1 && oldPositions.containsKey(c1.getId())) {
                        double[] oldPos = oldPositions.get(c1.getId());
                        c1.setPosition(oldPos[0], oldPos[1]);
                        c1.setVelocity(0, 0);
                    }

                    // 如果e2被阻挡且是可控制实体，回退到旧位置
                    if (e2Blocked && e2 instanceof Controllable c2 && oldPositions.containsKey(c2.getId())) {
                        double[] oldPos = oldPositions.get(c2.getId());
                        c2.setPosition(oldPos[0], oldPos[1]);
                        c2.setVelocity(0, 0);
                    }
                }
            }
        }
    }

    private void cleanupDeadEntities() {
        GameState state = context.getState();
        state.getEntities().removeIf(obj -> obj instanceof Entity entity && !entity.isAlive());

        // 清理队伍实体列表
        for (java.util.List<Object> teamList : state.getTeamEntities().values()) {
            teamList.removeIf(obj -> obj instanceof Entity entity && !entity.isAlive());
        }
    }

    public void stop() {
        if (!running)
            return;

        running = false;
        context.getState().setRunning(false);

        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        eventBus.publish(new GameEvent("GameStopped", null, "Game stopped"));
    }

    public void pause() {
        context.getState().setPaused(true);
        eventBus.publish(new GameEvent("GamePaused", null, "Game paused"));
    }

    public void resume() {
        context.getState().setPaused(false);
        eventBus.publish(new GameEvent("GameResumed", null, "Game resumed"));
    }

    public void handleKeyPress(KeyCode key) {
        if (mapProvider != null) {
            mapProvider.onKeyPressed(key, context);
        }
    }

    public void handleKeyRelease(KeyCode key) {
        if (mapProvider != null) {
            mapProvider.onKeyReleased(key, context);
        }
    }

    public GameState getState() {
        return context.getState();
    }
}
