package game.map.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import game.engine.GameContext;

/**
 * 爆炸效果实体
 */
public class ExplosionEntity extends BaseEntity {
    private double lifeTime;
    private final double maxLifeTime = 0.5;

    public ExplosionEntity(double x, double y) {
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
        gc.setFill(Color.rgb(255, 100, 0, alpha));
        gc.fillOval(x - 10, y - 10, width + 20, height + 20);
    }
}
