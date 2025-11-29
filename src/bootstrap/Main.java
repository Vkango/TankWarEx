package bootstrap;

import game.config.DefaultGameConfig;
import game.engine.GameContext;
import game.engine.GameEngine;
import game.rules.RuleProvider;
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
        
        // Don't load map provider eagerly
        // MapProvider mapProvider = PluginManager.getInstance().getMapProviders().stream()
        //         .findFirst()
        //         .orElseThrow(() -> new RuntimeException("No MapProvider found!"));
        
        RuleProvider ruleProvider = PluginManager.getInstance().getRuleProviders().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RuleProvider found!"));

        // System.out.println("Using MapProvider: " + mapProvider.getMapName());
        System.out.println("Using RuleProvider: " + ruleProvider.getClass().getSimpleName());

        System.out.println("Loading game engine...");

        DefaultGameConfig config = new DefaultGameConfig();
        GameContext context = GameContext.getInstance();
        context.setConfig(config);
        // context.setMapProvider(mapProvider);
        context.setRuleProvider(ruleProvider);
        GameEngine engine = new GameEngine();

        // engine.initialize(); // Don't initialize engine yet
        context.setGameEngine(engine);        System.out.println("Engine loaded successfully");

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
