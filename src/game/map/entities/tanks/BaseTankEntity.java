package game.map.entities.tanks;

import game.map.entities.*;
import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Map;

/**
 * 只包含通用属性和行为
 */
public abstract class BaseTankEntity extends BaseEntity implements Controllable {
    protected final int teamIndex;
    protected int health = 100;
    protected double rotation;
    protected double vx, vy;

    private double fireCooldown = 0;
    private static final double FIRE_COOLDOWN_TIME = 0.1;

    // 加速状态（计时器>0表示正在加速）
    private double speedBoostTimer = 0;
    protected static final double SPEED_BOOST_DURATION = 10.0;
    protected static final double SPEED_MULTIPLIER = 2.0;

    // 冻结状态管理
    private boolean isFrozen = false;
    private double frozenTimer = 0;
    private double savedVx = 0; // 保存被冻结前的速度
    private double savedVy = 0;

    @Override
    public void noticeOutOfBounds() {

    }

    public BaseTankEntity(double x, double y, int teamIndex) {
        super(x, y, 40, 40);
        this.teamIndex = teamIndex;
    }

    @Override
    public void update(double deltaTime) {
        // 更新冻结状态
        if (isFrozen) {
            frozenTimer -= deltaTime;
            if (frozenTimer <= 0) {
                unfreeze();
            }
            // 冻结状态下不移动
            return;
        }

        handleInput(deltaTime, GameContext.getInstance());
        double speedMultiplier = getSpeedMultiplier();
        x += vx * speedMultiplier * deltaTime;
        y += vy * speedMultiplier * deltaTime;

        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }

        if (speedBoostTimer > 0) {
            speedBoostTimer -= deltaTime;
            if (speedBoostTimer <= 0) {
                onSpeedBoostEnd();
            }
        }
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        Color[] teamColors = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
        Color tankColor = teamColors[teamIndex % teamColors.length];
        if (isFrozen) {
            gc.setFill(Color.rgb(100, 200, 255, 0.7));
        } else {
            gc.setFill(tankColor);
        }
        gc.fillRect(x, y, width, height);

        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double barrelEndX = centerX + Math.cos(rotation) * 20;
        double barrelEndY = centerY + Math.sin(rotation) * 20;

        gc.setStroke(isFrozen ? Color.LIGHTBLUE : Color.DARKGRAY);
        gc.setLineWidth(4);
        gc.strokeLine(centerX, centerY, barrelEndX, barrelEndY);

        gc.setFill(Color.WHITE);
        gc.fillText("T" + (teamIndex + 1), x + 5, y + 15);

        if (isFrozen) {
            gc.setFill(Color.CYAN);
            gc.fillText(String.format("❄ %.1fs", frozenTimer), x, y - 10);
        }

        double hpBarWidth = width * (health / 100.0);
        gc.setFill(Color.RED);
        gc.fillRect(x, y - 8, width, 5);
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y - 8, hpBarWidth, 5);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof SpeedUp && other.isAlive()) {
            activateSpeedBoost();
            other.markDead();
            context.getSoundManager().playSoundEffect("bonus");
            return false;
        }

        return other instanceof BaseTankEntity ||
                other instanceof WaterTile ||
                other instanceof BrickWallTile ||
                other instanceof SteelWallTile ||
                other instanceof BaseStructure;
    }

    protected abstract void handleInput(double deltaTime, GameContext context);

    protected boolean canFire() {
        if (fireCooldown <= 0) {
            fireCooldown = FIRE_COOLDOWN_TIME;
            return true;
        }
        return false;
    }

    public void activateSpeedBoost() {
        this.speedBoostTimer = SPEED_BOOST_DURATION;
    }

    protected double getSpeedMultiplier() {
        return speedBoostTimer > 0 ? SPEED_MULTIPLIER : 1.0;
    }

    protected void onSpeedBoostEnd() {

    }

    public void fireShell(GameContext context) {
        if (fireCooldown > 0)
            return;

        fireCooldown = FIRE_COOLDOWN_TIME;
        context.getSoundManager().playSoundEffect("tank_fire");

        double shellX = x + width / 2 + Math.cos(rotation) * 25;
        double shellY = y + height / 2 + Math.sin(rotation) * 25;

        Map<String, Object> data = Map.of(
                "x", shellX,
                "y", shellY,
                "angle", rotation,
                "teamIndex", teamIndex,
                "ownerTankId", id);
        context.getEventBus().publish(new GameEvent("SpawnShell", data, "Tank fired"));
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0)
            markDead();
    }

    @Override
    public int getTeamIndex() {
        return teamIndex;
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
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

    public void freeze(double duration) {
        if (!isFrozen) {
            isFrozen = true;
            frozenTimer = duration;
            // 保存当前速度
            savedVx = vx;
            savedVy = vy;
            // 停止移动
            vx = 0;
            vy = 0;
        } else {
            // 如果已经冻结，重置计时器
            frozenTimer = duration;
        }
    }

    public void unfreeze() {
        if (isFrozen) {
            isFrozen = false;
            frozenTimer = 0;
            // 恢复速度
            vx = savedVx;
            vy = savedVy;
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }
}