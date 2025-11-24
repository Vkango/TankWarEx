package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SpeedUp extends BaseEntity {
    public SpeedUp(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setFill(Color.PURPLE);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.WHITE);
        gc.fillText("UP", x + 8, y + 30);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof TankEntity tank) {

            tank.setSpeedUp();
        }
        return false;
    }
}
