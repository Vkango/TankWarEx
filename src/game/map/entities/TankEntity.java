package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现Controllable接口，可被队伍控制器操作
 */
public class TankEntity extends BaseEntity implements Controllable {
    private final int teamIndex;
    private double fireCooldown = 0;
    private static final double FIRE_COOLDOWN_TIME = 0.1;
    private int health = 100;
    private double speedUpTimer = 0;
    private static final double SPEED_UP_TIME = 10;
    private boolean isInSpeedUp = false;
    private static final double SPEED_UP_RATE = 5;

    public void setVelocityX(double v) {
        setVelocity(v, getVelocity()[1] / (isInSpeedUp ? SPEED_UP_RATE : 1));
    }

    public void setVelocityY(double v) {
        setVelocity(getVelocity()[0] / (isInSpeedUp ? SPEED_UP_RATE : 1), v);
    }

    @Override
    public double[] getVelocity() {
        return new double[] { vx, vy };
    }

    public TankEntity(double x, double y, int teamIndex) {
        super(x, y, 40, 40);
        this.teamIndex = teamIndex;
    }

    @Override
    public int getTeamIndex() {
        return teamIndex;
    }

    @Override
    public void setVelocity(double vx, double vy) {
        if (speedUpTimer > 0 && isInSpeedUp) {
            // 处理加速
            vx = vx * SPEED_UP_RATE;
            vy = vy * SPEED_UP_RATE;
        }

        this.vx = vx;
        this.vy = vy;

    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    public boolean tryFire() {
        if (fireCooldown <= 0) {
            fireCooldown = FIRE_COOLDOWN_TIME;
            return true;
        }
        return false;
    }

    public void setSpeedUp() {
        // 只要接触到就加速 (重置时间)
        speedUpTimer = SPEED_UP_TIME;

        if (!isInSpeedUp) {
            isInSpeedUp = true;
            vx = vx * SPEED_UP_RATE;
            vy = vy * SPEED_UP_RATE;
        }

    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            markDead();
        }
    }

    @Override
    public void update(double deltaTime) {
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }

        if (speedUpTimer > 0 && isInSpeedUp) {
            speedUpTimer -= deltaTime;
            // System.out.println(speedUpTimer + " | " + vx + " | " + vy);
        } else if (speedUpTimer <= 0 && isInSpeedUp) {
            vx = vx / SPEED_UP_RATE;
            vy = vy / SPEED_UP_RATE;
            isInSpeedUp = false;
        }

        // 移动
        x += vx * deltaTime;
        y += vy * deltaTime;
    }

    @Override
    public int getZIndex() {
        return 1; // 坦克的Z-index高于基地等静态实体
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        // 定义队伍颜色
        Color[] TEAM_COLORS = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
        Color tankColor = TEAM_COLORS[teamIndex % TEAM_COLORS.length];

        // 绘制坦克主体
        gc.setFill(tankColor);
        gc.fillRect(x, y, width, height);

        // 绘制炮管
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double barrelEndX = centerX + Math.cos(rotation) * 20;
        double barrelEndY = centerY + Math.sin(rotation) * 20;
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(4);
        gc.strokeLine(centerX, centerY, barrelEndX, barrelEndY);

        // 绘制队伍标识
        gc.setFill(Color.WHITE);
        gc.fillText("T" + (teamIndex + 1), x + 5, y + 15);

        // 绘制生命值条
        double hpBarWidth = width * (health / 100.0);
        gc.setFill(Color.RED);
        gc.fillRect(x, y - 8, width, 5);
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y - 8, hpBarWidth, 5);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 坦克与其他坦克碰撞就阻挡
        if (other instanceof TankEntity) {
            return true;
        }

        // 坦克与水域碰撞就阻挡
        if (other instanceof WaterTile) {
            return true;
        }

        // 坦克与墙体碰撞就阻挡
        if (other instanceof BrickWallTile || other instanceof SteelWallTile) {
            return true;
        }

        // 坦克与基地碰撞就阻挡
        if (other instanceof BaseStructure) {
            return true;
        }

        // 其他情况不阻挡
        return false;
    }

    @Override
    public void noticeOutOfBounds() {
        // 当坦克超出边界时，简单地将其标记为死亡
        // markDead();
    }

    public void fireShell(GameContext context) {
        if (tryFire()) {
            context.getSoundManager().playSoundEffect("tank_fire");
            double shellX = x + width / 2 + Math.cos(rotation) * 25;
            double shellY = y + height / 2 + Math.sin(rotation) * 25;

            // 通过事件总线发布生成子弹的事件
            Map<String, Object> data = new HashMap<>();
            data.put("x", shellX);
            data.put("y", shellY);
            data.put("angle", rotation);
            data.put("teamIndex", teamIndex);
            data.put("ownerTankId", id); // 传递发射者ID
            game.engine.EventBus.getInstance().publish(
                    new game.engine.GameEvent("SpawnShell", data, "Tank fired"));
        }
    }
}
