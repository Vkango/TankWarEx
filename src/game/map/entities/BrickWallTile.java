package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 砖墙是可被摧毁的障碍物
 */
public class BrickWallTile extends BaseEntity {
    public BrickWallTile(double x, double y) {
        super(x, y, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
        // 墙不移动
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(139, 69, 19));
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.rgb(101, 50, 15));
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 砖墙阻挡所有实体（坦克和子弹）
        // 子弹的handleCollision会处理摧毁砖墙的逻辑
        return true;
    }
}
