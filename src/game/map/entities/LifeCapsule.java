package game.map.entities;

import game.engine.GameContext;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import game.engine.Entity;

/**
 * 爆炸效果实体
 */
public class LifeCapsule extends BaseEntity {
    private double lifeTime;
    private final double maxLifeTime = 20;

    public LifeCapsule(double x, double y) {
        super(x, y, 40, 40);
        this.lifeTime = maxLifeTime;
    }

    @Override
    public void update(double deltaTime) {
        lifeTime -= deltaTime;
        if (lifeTime <= 0) {
            markDead();
        }
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        double alpha = lifeTime / maxLifeTime;

        gc.setFill(Color.rgb(0, 255, 0, alpha));
        gc.fillOval(x - 10, y - 10, width + 20, height + 20);
        gc.drawImage(context.getImageManager().getImage("heart"), x - 10, y - 10, width + 20, height + 20);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof TankEntity tank) {
            tank.takeDamage(-50); // 负伤害表示加血
            markDead();
            return true;
        }
        return false;
    }

}
