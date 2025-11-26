package game.engine;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

/**
 * 每帧记录按键状态，供实体查询
 */

public final class InputManager {
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private static final InputManager INSTANCE = new InputManager();

    private InputManager() {
    }

    public static InputManager getInstance() {
        return INSTANCE;
    }

    public void pressKey(KeyCode key) {
        pressedKeys.add(key);
    }

    public void releaseKey(KeyCode key) {
        pressedKeys.remove(key);
    }

    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    public void clear() {
        pressedKeys.clear();
    }
}