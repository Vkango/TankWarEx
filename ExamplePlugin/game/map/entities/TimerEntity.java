package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimerEntity extends BaseEntity {
    private double remainTime = 0.0;

    public TimerEntity() {
        super(0, 0, 40, 40);
    }

    public TimerEntity(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
        remainTime += deltaTime;
    }

    @Override
    public String getMessage() {
        return String.format("游戏时间: %.2f seconds", remainTime);
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setFill(Color.CYAN);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%.2fs", remainTime), x + 5, y + 25);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        return false;
    }
}
