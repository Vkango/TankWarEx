package game.engine;

/**
 * 可控制实体接口
 */
public interface Controllable extends Entity {
    /**
     * 获取队伍索引
     */
    int getTeamIndex();

    /**
     * 设置速度
     */
    void setVelocity(double vx, double vy);

    double[] getVelocity();

    /**
     * 设置旋转角度
     */
    void setRotation(double rotation);

    /**
     * 设置位置
     */
    void setPosition(double x, double y);

    void noticeOutOfBounds();
}
