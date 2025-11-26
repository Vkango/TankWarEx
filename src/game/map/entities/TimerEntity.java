package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimerEntity extends BaseEntity {
    private double time = 0;

    public TimerEntity(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
        // 计时器不移动
        time += deltaTime;
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setFill(Color.CYAN);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%.2fs", time), x + 5, y + 25);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        return false;
    }
}
