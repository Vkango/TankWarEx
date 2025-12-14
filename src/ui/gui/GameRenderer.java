package ui.gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import game.engine.*;

public class GameRenderer extends AnimationTimer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private GameEngine engine;
    private final GameContext context = GameContext.getInstance();
    private int fps = 0;
    private long lastFpsTime = System.nanoTime();
    private long lastHandleTime = System.nanoTime();
    private int frameCount = 0;
    private long frameTimeNanos;
    private double scale = 1.0;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.engine = GameContext.getInstance().getEngine();
        updateFrameTime();
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.5, Math.min(2.0, scale));
    }

    public double getScale() {
        return scale;
    }

    public void cleanup() {
        stop();
    }

    private void updateFrameTime() {
        if (engine != null) {
            double targetFps = engine.getConfig().getTargetFps();
            this.frameTimeNanos = (long) (1_000_000_000.0 / (long) targetFps);
        }
    }

    @Override
    public void handle(long now) {
        if (engine != GameContext.getInstance().getEngine()) {
            engine = GameContext.getInstance().getEngine();
            updateFrameTime();
        }

        if (engine == null)
            return;

        if (now - lastHandleTime < frameTimeNanos) {
            return;
        }
        lastHandleTime = now;

        frameCount++;
        if (now - lastFpsTime >= 1_000_000_000L) {
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = now;
        }

        GameState state = engine.getState();
        if (state == null) {
            return;
        }

        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double viewportWidth = canvas.getWidth() / scale;
        double viewportHeight = canvas.getHeight() / scale;

        double worldWidth = 800;
        double worldHeight = 600;
        if (context.getMapProvider() != null) {
            worldWidth = context.getMapProvider().getMapWidth();
            worldHeight = context.getMapProvider().getMapHeight();
        }

        double cameraX = 0;
        double cameraY = 0;

        Entity player = null;
        if (context.getMapProvider() != null) {
            player = context.getMapProvider().getPlayerEntity(context);
        }

        if (player != null) {
            cameraX = player.getX() + player.getWidth() / 2 - viewportWidth / 2;
            cameraY = player.getY() + player.getHeight() / 2 - viewportHeight / 2;
        } else {
            cameraX = worldWidth / 2.0 - viewportWidth / 2;
            cameraY = worldHeight / 2.0 - viewportHeight / 2;
        }

        cameraX = Math.max(0, Math.min(cameraX, worldWidth - viewportWidth));
        cameraY = Math.max(0, Math.min(cameraY, worldHeight - viewportHeight));

        gc.save();
        gc.scale(scale, scale);
        gc.translate(-cameraX, -cameraY);

        StringBuffer msg = new StringBuffer();

        for (Object obj : state.getEntities()) {
            if (obj instanceof Entity entity && entity.isAlive()) {
                if (obj instanceof Entity rend) {
                    rend.render(gc, context);
                    if (rend.getMessage().equals(""))
                        continue;
                    msg.append(rend.getMessage() + "\n");
                }
            }
        }

        gc.restore();

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font(14));
        gc.fillText("TankWarEx - 按下ESC键暂停, 或 -/+ 键缩放"
                + (engine.getState().isPaused() ? " [暂停]" : "") + "\n" + "实体数量: " + state.getEntities().size()
                + "\nFPS: " + fps + "\n" + msg, 10, 20);
    }

}
