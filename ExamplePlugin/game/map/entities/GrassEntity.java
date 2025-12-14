package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
// import javafx.scene.paint.Color;

public class GrassEntity extends BaseEntity {
    public GrassEntity(double x, double y) {
        super(x, y, 40, 40);
    }

    public GrassEntity() {
        super(0, 0, 40, 40);
    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.drawImage(context.getImageManager().getImage("grass"), x, y, width, height);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof game.map.entities.tanks.BaseTankEntity) {
            return true; // 阻挡坦克
        }
        return false;
    }

    @Override
    public int getZIndex() {
        return 1; // 草地的Z-index在坦克上方，当然也在子弹上
    }
}
