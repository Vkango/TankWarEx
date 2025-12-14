package game.map.entities;

import game.engine.*;
import java.util.UUID;

public abstract class BaseEntity implements Entity {
    protected final UUID id;
    protected double x, y;
    protected double vx, vy;
    protected final double width, height;
    protected double rotation;
    protected boolean alive = true;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.width = 0;
        this.height = 0;
    }

    public BaseEntity(double x, double y, double width, double height) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vx = 0;
        this.vy = 0;
        this.rotation = 0;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public int getZIndex() {
        return 0;
    }

    @Override
    public void markDead() {
        this.alive = false;
    }

    @Override
    public boolean intersects(Entity other) {
        return x < other.getX() + other.getWidth() &&
                x + width > other.getX() &&
                y < other.getY() + other.getHeight() &&
                y + height > other.getY();
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        // 默认不阻挡
        return false;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
