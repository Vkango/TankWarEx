package game.engine;

import java.util.UUID;
import javafx.scene.canvas.GraphicsContext;

public interface Entity {
    /**
     * 获取实体唯一ID
     */
    UUID getId();

    void render(GraphicsContext gc, GameContext context);

    /**
     * 获取X坐标
     */
    double getX();

    /**
     * 获取Y坐标
     */
    double getY();

    /**
     * 获取宽度
     */
    double getWidth();

    /**
     * 获取高度
     */
    double getHeight();

    /**
     * 获取旋转角度
     */
    double getRotation();

    /**
     * 是否存活
     */
    boolean isAlive();

    /**
     * 标记为死亡
     */
    void markDead();

    /**
     * 更新实体状态
     * 
     * @param deltaTime 时间增量
     */
    void update(double deltaTime);

    /**
     * 检查与另一个实体是否碰撞
     */
    boolean intersects(Entity other);

    /**
     * 处理与另一个实体的碰撞
     * 
     * @param other   碰撞的另一个实体
     * @param context 游戏上下文，用于访问游戏状态和事件总线
     * @return 是否阻挡移动
     */
    boolean handleCollision(Entity other, GameContext context);
}
