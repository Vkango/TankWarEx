package game.map.entities;

import game.engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 队伍的基地
 */
public class BaseStructure extends BaseEntity implements Controllable {
    private final int teamIndex;
    private int health = 100;

    public BaseStructure() {
        super(0, 0, 50, 50);
        this.teamIndex = -1;
    }

    public BaseStructure(double x, double y, int teamIndex) {
        super(x, y, 50, 50);
        this.teamIndex = teamIndex;
    }

    @Override
    public int getTeamIndex() {
        return teamIndex;
    }

    @Override
    public void setVelocity(double vx, double vy) {
        // 基地不移动
    }

    @Override
    public double[] getVelocity() {
        return new double[] { 0, 0 };
    }

    @Override
    public void setRotation(double rotation) {
        // 基地不旋转
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void noticeOutOfBounds() {
        // 基地不会出界
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

    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.drawImage(context.getImageManager().getImage("base"), x, y, width, height);
        gc.setFill(Color.WHITE);
        gc.fillText("BASE", x + 8, y + 30);
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 基地阻挡所有实体（坦克和子弹）
        // 注意：子弹的handleCollision会处理对基地的伤害
        return true;
    }
}
