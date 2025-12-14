package game.map.entities;

import game.engine.GameContext;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import game.engine.Entity;

public class CReflector extends BaseEntity {

    public CReflector(double x, double y) {
        super(x, y, 40, 40);
    }

    public CReflector() {
        super(0, 0, 40, 40);
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc, GameContext context) {
        gc.setStroke(Color.rgb(255, 255, 255));
        gc.setLineWidth(2);
        gc.strokeLine(x + width, y, x, y + height);
    }

    @Override
    public boolean intersects(Entity other) {
        double x2 = this.x, y2 = this.y + height;
        double x1 = this.x + this.width, y1 = this.y;
        double minX = other.getX();
        double maxX = other.getX() + other.getWidth();
        double minY = other.getY();
        double maxY = other.getY() + other.getHeight();
        return lineIntersectsRect(x1, y1, x2, y2, minX, minY, maxX, maxY);
    }

    private boolean lineIntersectsRect(double x1, double y1, double x2, double y2,
            double minX, double minY, double maxX, double maxY) {
        // 快速拒绝,线段的包围盒与矩形不相交
        if (Math.max(x1, x2) < minX || Math.min(x1, x2) > maxX ||
                Math.max(y1, y2) < minY || Math.min(y1, y2) > maxY) {
            return false;
        }

        double dx = x2 - x1;
        double dy = y2 - y1;
        double tMin = 0.0, tMax = 1.0;

        // X轴方向的交点参数
        if (dx != 0) {
            double tNear = (minX - x1) / dx;
            double tFar = (maxX - x1) / dx;
            if (tNear > tFar) {
                double temp = tNear;
                tNear = tFar;
                tFar = temp;
            }
            tMin = Math.max(tMin, tNear);
            tMax = Math.min(tMax, tFar);
        } else if (x1 < minX || x1 > maxX) {
            return false; // 垂直线且X不在矩形内
        }

        // Y轴方向的交点参数
        if (dy != 0) {
            double tNear = (minY - y1) / dy;
            double tFar = (maxY - y1) / dy;
            if (tNear > tFar) {
                double temp = tNear;
                tNear = tFar;
                tFar = temp;
            }
            tMin = Math.max(tMin, tNear);
            tMax = Math.min(tMax, tFar);
        } else if (y1 < minY || y1 > maxY) {
            return false; // 水平线且Y不在矩形内
        }
        return tMin <= tMax;
    }

    @Override
    public boolean handleCollision(Entity other, GameContext context) {
        if (other instanceof game.map.entities.ShellEntity shell) {
            shell.setVelocity(-shell.getVelocity()[1], -shell.getVelocity()[0]); // 反射子弹速度
            context.getSoundManager().playSoundEffect("reflector");
            return true;
        }
        return false;
    }

}
