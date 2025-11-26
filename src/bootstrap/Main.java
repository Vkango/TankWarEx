package bootstrap;

import game.config.DefaultGameConfig;
import game.engine.GameContext;
import game.engine.GameEngine;
import game.map.DefaultMapProvider;
import game.rules.DefaultRuleProvider;
import ui.gui.GUI;

public class Main {
    private static final String VERSION = "1.0.0.re";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       TankWar Game v" + VERSION);
        System.out.println("========================================");
        System.out.println("Loading game engine...");

        DefaultMapProvider mapProvider = new DefaultMapProvider();
        DefaultRuleProvider ruleProvider = new DefaultRuleProvider();
        DefaultGameConfig config = new DefaultGameConfig();
        GameContext context = GameContext.getInstance();
        context.setConfig(config);
        context.setMapProvider(mapProvider);
        context.setRuleProvider(ruleProvider);
        GameEngine engine = new GameEngine();

        engine.initialize();
        context.setGameEngine(engine);

        System.out.println("Engine loaded successfully");

        System.out.println("Initializing game...");

        System.out.println("Game initialized");

        System.out.println("Starting GUI...");

        System.out.println("Launching window...");

        // 加载资源需要等JavaFX线程启动后进行，此处不加载。
        GUI.setStaticContext(context);
        try {
            GUI.launch(GUI.class, args);
        } catch (Exception e) {
            System.err.println("Failed to launch GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
