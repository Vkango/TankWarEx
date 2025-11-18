package ui.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import game.engine.GameEngine;
import game.engine.EventBus;
import game.engine.GameEvent;
import java.util.Map;

public class GUI extends Application {
    private static GUI instance;
    private static GameEngine staticEngine;
    private Stage primaryStage;
    private Canvas canvas;
    private GameRenderer renderer;
    private GameEngine engine;

    public GUI() {
        instance = this;
        this.engine = staticEngine;

        // 订阅基地摧毁事件
        EventBus.getInstance().subscribe("BaseDestroyed", this::handleBaseDestroyed);
    }

    public static GUI getInstance() {
        return instance;
    }

    public static void setStaticEngine(GameEngine engine) {
        staticEngine = engine;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
        if (canvas != null && engine != null) {
            renderer = new GameRenderer(canvas, engine);
            renderer.start();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("TankWarEx");
        primaryStage.setOnCloseRequest(e -> {
            if (engine != null) {
                engine.stop();
            }
            Platform.exit();
            System.exit(0);
        });

        canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 800, 600);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (engine.getState().isPaused())
                    engine.resume();
                else
                    engine.pause();
                return;
            }
            if (engine != null && !engine.getState().isPaused()) {
                engine.handleKeyPress(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            if (engine != null) {
                engine.handleKeyRelease(event.getCode());
            }
        });

        engine.initResources();

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        System.out.println("[GUI] Window shown, engine is: " + (engine != null ? "ready" : "NULL"));

        if (engine != null) {
            System.out.println("[GUI] Starting renderer and game engine...");
            renderer = new GameRenderer(canvas, engine);
            renderer.start();
            engine.start();
            System.out.println("[GUI] Engine started");
        } else {
            System.err.println("[GUI] ERROR: Engine is null!");
        }
    }

    /**
     * 处理基地摧毁事件
     */
    private void handleBaseDestroyed(GameEvent event) {
        Map<String, Object> data = (Map<String, Object>) event.getData();
        int teamIndex = (int) data.get("teamIndex");

    }
}
