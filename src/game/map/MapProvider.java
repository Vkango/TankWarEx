package game.map;

import game.engine.*;
import javafx.scene.input.KeyCode;

public interface MapProvider {
    /**
     * 地图名称
     */
    String getMapName();

    /**
     * 地图宽度
     */
    int getMapWidth();

    /**
     * 地图高度
     */
    int getMapHeight();

    /**
     * 创建所有游戏实体
     * 包括所有队伍的单位、基地、地图元素等
     * 
     * @param context 游戏上下文
     */
    void createEntities(GameContext context);

    /**
     * 包括：控制器更新、AI行为、增援生成、规则检查等
     * 
     * @param deltaTime 时间增量
     * @param context   游戏上下文
     */
    void update(double deltaTime, GameContext context);

    /**
     * 处理按键按下事件
     * 
     * @param key     按键
     * @param context 游戏上下文
     */
    void onKeyPressed(KeyCode key, GameContext context);

    /**
     * 处理按键释放事件
     * 
     * @param key     按键
     * @param context 游戏上下文
     */
    void onKeyReleased(KeyCode key, GameContext context);

    void initResources(GameContext context);
}
