package game.map.entities;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
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

    public ExplosionEntity() {
        super(0, 0, 40, 40);
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
        if (lifeTime <= 0) {
            return;
        }
        String[] imgNameS = { "explosion1", "explosion2", "explosion3", "explosion4", "explosion5" };
        double bate = lifeTime / maxLifeTime;
        String imgName;
        if (bate >= 0.8) {
            imgName = imgNameS[0];
        } else if (bate >= 0.6) {
            imgName = imgNameS[1];

        } else if (bate >= 0.4) {
            imgName = imgNameS[2];

        } else if (bate >= 0.2) {
            imgName = imgNameS[3];
        } else {
            imgName = imgNameS[4];
        }
        Image BaseImg = context.getImageManager().getImage(imgName);
        gc.drawImage(BaseImg, x - width / 2, y - height / 2, width, height);

    }
}
