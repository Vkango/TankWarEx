package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 钢墙是不可摧毁的障碍物
 */
public class SteelWallTile extends BaseEntity {
    public SteelWallTile(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
        // 墙不移动
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        return true;
    }
}
