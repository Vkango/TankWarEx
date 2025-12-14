package bootstrap;

import game.config.DefaultGameConfig;
import game.engine.GameContext;
import game.engine.GameEngine;
import plugin.api.PluginManager;
import ui.gui.GUI;

public class Main {
    private static final String VERSION = "1.0.0.re";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       TankWar Game v" + VERSION);
        System.out.println("========================================");

        System.out.println("Loading plugins...");
        PluginManager.getInstance().loadPlugins();

        System.out.println("Loading game engine...");

        DefaultGameConfig config = new DefaultGameConfig();
        GameContext context = GameContext.getInstance();
        context.setConfig(config);
        GameEngine engine = new GameEngine();

        context.setGameEngine(engine);

        GUI.setStaticContext(context);
        try {
            GUI.launch(GUI.class, args);
        } catch (Exception e) {
            System.err.println("Failed to launch GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
