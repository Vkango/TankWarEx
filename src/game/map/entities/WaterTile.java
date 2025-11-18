package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WaterTile extends BaseEntity {
    public WaterTile(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
        // 水不移动
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setFill(Color.CYAN);
        gc.fillRect(x, y, width, height);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof TankEntity) {
            return true; // 阻挡坦克
        }
        return false;
    }
}
