package bootstrap;

import game.engine.GameEngine;
import ui.gui.GUI;

public class Main {
    private static final String VERSION = "1.0.0.re";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       TankWar Game v" + VERSION);
        System.out.println("========================================");
        System.out.println("Loading game engine...");

        GameEngine engine = ServiceLoader.loadGameEngine();
        System.out.println("Engine loaded successfully");

        System.out.println("Initializing game...");
        engine.initialize();
        System.out.println("Game initialized");

        System.out.println("Starting GUI...");
        GUI.setStaticEngine(engine);

        System.out.println("Launching window...");
        try {
            GUI.launch(GUI.class, args);
        } catch (Exception e) {
            System.err.println("Failed to launch GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
