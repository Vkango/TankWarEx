package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Map;
import java.util.UUID;

/**
 * 子弹实体
 */
public class ShellEntity extends BaseEntity implements Controllable {
    private final int teamIndex;
    private final double speed = 300;
    private final UUID ownerTankId; // 发射者ID，避免与发射者碰撞

    public ShellEntity() {
        super(0, 0, 10, 10);
        this.teamIndex = -1;
        this.ownerTankId = null;
    }

    public ShellEntity(double x, double y, double angle, int teamIndex, UUID ownerTankId) {
        super(x, y, 10, 10);
        this.teamIndex = teamIndex;
        this.rotation = angle;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.ownerTankId = ownerTankId;
    }

    public int getTeamIndex() {
        return teamIndex;
    }

    @Override
    public void noticeOutOfBounds() {
        markDead();
    }

    @Override
    public void setRotation(double rotation) {

    }

    @Override
    public double[] getVelocity() {
        return new double[] { vx, vy };
    }

    @Override
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    @Override
    public void update(double deltaTime) {
        x += vx * deltaTime;
        y += vy * deltaTime;
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setFill(Color.YELLOW);
        gc.fillOval(x, y, width, height);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 忽略与发射者的碰撞
        if (other.getId().equals(ownerTankId)) {
            return false; // 不阻挡
        }

        // 忽略与其他子弹的碰撞
        if (other instanceof ShellEntity) {
            other.markDead();
            markDead();
            spawnExplosion(context);
            return false; // 阻挡其他子弹，形成对碰，别问问就是原版这么写的，太细节了bro
        }

        // 忽略水域（子弹可穿过）
        if (other instanceof WaterTile) {
            return false; // 不阻挡，子弹继续飞行
        }

        if (other instanceof game.map.entities.tanks.BaseTankEntity tank) {
            // 自己的也杀
            context.getSoundManager().playSoundEffect("explosion");
            tank.takeDamage(50);
            if (!tank.isAlive() && tank.getTeamIndex() == 0) {
                context.showToast("你的坦克已被击毁，请按R键复活！");
            }
            markDead();
            spawnExplosion(context);
            return false;
        }

        // 击中敌方基地
        if (other instanceof BaseStructure base && base.getTeamIndex() != teamIndex) {
            base.takeDamage(100);
            markDead();
            spawnExplosion(context);
            context.getSoundManager().playSoundEffect("explosion");
            // 通知规则提供者
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("teamIndex", base.getTeamIndex());
            context.showToast("队伍" + (base.getTeamIndex() + 1) + "基地已被摧毁，此后将无法复活！");
            game.engine.EventBus.getInstance().publish(
                    new game.engine.GameEvent("BaseDestroyed", data, "Base destroyed"));
            return false; // 子弹已死亡，不需要阻挡
        }

        // 击中墙体
        if (other instanceof BrickWallTile || other instanceof SteelWallTile) {
            markDead();
            spawnExplosion(context);

            // 砖墙被摧毁
            if (other instanceof BrickWallTile) {
                other.markDead();
                context.getSoundManager().playSoundEffect("explosion");
            } else {
                context.getSoundManager().playSoundEffect("steelhit");
            }
            return false; // 子弹已死亡，不需要阻挡
        }

        // 其他情况不阻挡
        return false;
    }

    private void spawnExplosion(GameContext context) {
        ExplosionEntity explosion = new ExplosionEntity(x, y);
        context.getState().addEntity(explosion);
    }
}
