package ui.gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import game.engine.*;

public class GameRenderer extends AnimationTimer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GameEngine engine = GameContext.getInstance().getEngine();
    private final GameContext context = GameContext.getInstance();
    private String gameOverMessage = null;
    private int fps = 0;
    private long lastFpsTime = System.nanoTime();
    private long lastHandleTime = System.nanoTime();
    private int frameCount = 0;
    private long frameTimeNanos;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        updateFrameTime();
        EventBus.getInstance().subscribe("GameOver", this::handleGameOver);
    }

    private void updateFrameTime() {
        double targetFps = engine.getConfig().getTargetFps();
        this.frameTimeNanos = (long) (1_000_000_000.0 / (long) targetFps);
    }

    @Override
    public void handle(long now) {

        if (now - lastHandleTime < frameTimeNanos) {
            return;
        }
        lastHandleTime = now;

        frameCount++;
        if (now - lastFpsTime >= 1_000_000_000L) { // 更新帧数显示
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

        // // 网格
        // gc.setStroke(Color.GRAY);
        // gc.setLineWidth(1);
        // for (int i = 0; i < canvas.getWidth(); i += 50) {
        // gc.strokeLine(i, 0, i, canvas.getHeight());
        // }
        // for (int i = 0; i < canvas.getHeight(); i += 50) {
        // gc.strokeLine(0, i, canvas.getWidth(), i);
        // }

        for (Object obj : state.getEntities()) {
            if (obj instanceof Entity entity && entity.isAlive()) {
                if (obj instanceof Entity rend) {
                    rend.render(gc, context);
                }
            }
        }

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font(14));
        gc.fillText("TankWarEx - WASD/Arrow Keys to move, SPACE to fire, R to respawn"
                + (engine.getState().isPaused() ? " [PAUSED]" : ""), 10, 20);
        gc.fillText("Entities: " + state.getEntities().size(), 10, 40);

        int yOffset = 60;
        for (int teamIndex : state.getTeamEntities().keySet()) {
            long tankCount = state.getTeamEntityList(teamIndex).stream()
                    .filter(e -> e instanceof game.map.entities.tanks.BaseTankEntity)
                    .filter(e -> ((Entity) e).isAlive())
                    .count();

            Object base = state.getTeamBase(teamIndex);
            String baseStatus = "Destroyed";
            if (base instanceof Entity entity && entity.isAlive()) {
                baseStatus = "Alive";
            }

            gc.fillText(String.format("Team %d: %d tanks, Base: %s",
                    teamIndex + 1, tankCount, baseStatus), 10, yOffset);
            yOffset += 20;
        }

        yOffset += 10;
        gc.fillText("FPS: " + fps, 10, yOffset);

        if (gameOverMessage != null) {
            gc.setFill(Color.BLACK);
            gc.setGlobalAlpha(0.7);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0);

            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Lucida Console", FontWeight.BOLD, 48));
            gc.fillText("GAME OVER", canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 - 50);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Lucida Console", FontWeight.NORMAL, 24));
            gc.fillText(gameOverMessage, canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 + 20);
            this.stop();
        }
    }

    private void handleGameOver(GameEvent event) {
        gameOverMessage = event.getMessage();
    }
}
