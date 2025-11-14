package bootstrap;

import game.engine.GameEngine;
import ui.gui.GUI;

public class ServiceLoader {

    public static GameEngine loadGameEngine() {
        return new GameEngine();
    }

    public static GUI loadUI() {
        return new GUI();
    }
}
