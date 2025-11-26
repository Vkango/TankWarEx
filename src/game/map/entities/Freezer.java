package game.map.entities;

import game.engine.GameContext;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import game.engine.Entity;
import game.map.entities.tanks.BaseTankEntity;

public class Freezer extends BaseEntity {
    private double lifeTime;
    private final double maxLifeTime = 20;
    private final double freezeRadius = 150.0; // 冻结范围半径
    private final double freezeDuration = 5.0; // 冻结持续时间（秒）

    private boolean hasTriggered;

    public Freezer(double x, double y) {
        super(x, y, 20, 20);
        this.lifeTime = maxLifeTime;
        this.hasTriggered = false;
    }

    @Override
    public void update(double deltaTime) {
        // 未触发时正常倒计时
        if (!hasTriggered) {
            lifeTime -= deltaTime;
            if (lifeTime <= 0) {
                markDead();
            }
        }
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        double alpha = hasTriggered ? 0.3 : lifeTime / maxLifeTime;

        // 绘制冻结光环
        gc.setFill(Color.rgb(137, 205, 248, alpha * 0.3));
        gc.fillOval(x - freezeRadius, y - freezeRadius, freezeRadius * 2, freezeRadius * 2);

        // 绘制中心图标
        gc.setFill(Color.rgb(137, 205, 248, alpha));
        gc.fillOval(x - 10, y - 10, width + 20, height + 20);
        gc.drawImage(context.getImageManager().getImage("ice"), x - 10, y - 10, width + 20, height + 20);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (!hasTriggered && other instanceof BaseTankEntity) {
            hasTriggered = true;
            BaseTankEntity pickerTank = (BaseTankEntity) other;

            context.getEventBus()
                    .publish(new game.engine.GameEvent("FreezeTriggered", other.getId(), "Freeze effect triggered"));

            // 冻结范围内的其他坦克
            for (Object obj : context.getState().getEntities()) {
                if (obj instanceof BaseTankEntity tank && tank.isAlive() && tank.getId() != pickerTank.getId()) {
                    // 检查是否在冻结范围内
                    double dx = tank.getX() - x;
                    double dy = tank.getY() - y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance <= freezeRadius) {
                        tank.freeze(freezeDuration);
                    }
                }
            }

            context.getSoundManager().playSoundEffect("bonus");
            markDead();
            return false;
        }
        return false;
    }
}