package ui.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import game.engine.GameContext;

public class GUI extends Application {
    private static GUI instance;
    private static GameContext staticContext;
    private Canvas canvas;
    private GameRenderer renderer;
    private GameContext context;

    public GUI() {
        instance = this;
        this.context = staticContext;
    }

    public static GUI getInstance() {
        return instance;
    }

    public static void setStaticContext(GameContext context) {
        staticContext = context;
    }

    public void setContext(GameContext context) {
        this.context = context;
        if (canvas != null && context != null) {
            renderer = new GameRenderer(canvas);
            renderer.start();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TankWarEx");
        primaryStage.setOnCloseRequest(e -> {
            if (context.getEngine() != null) {
                context.getEngine().stop();
            }
            Platform.exit();
            System.exit(0);
        });

        canvas = new Canvas(context.getEngine().getState().getWorldWidth(),
                context.getEngine().getState().getWorldHeight());
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, context.getEngine().getState().getWorldWidth(),
                context.getEngine().getState().getWorldHeight());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (context.getEngine().getState().isPaused())
                    context.getEngine().resume();
                else
                    context.getEngine().pause();
                return;
            }
            if (context.getEngine() != null && !context.getEngine().getState().isPaused()) {
                context.getInputManager().pressKey(event.getCode());
                context.getEngine().handleKeyPress(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> {
            if (context.getEngine() != null) {
                context.getInputManager().releaseKey(event.getCode());
                context.getEngine().handleKeyRelease(event.getCode());
            }
        });

        context.getEngine().initResources();

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        if (context.getEngine() != null) {
            System.out.println("[GUI] Starting renderer and game engine...");
            renderer = new GameRenderer(canvas);
            renderer.start();
            context.getEngine().start();
            System.out.println("[GUI] Engine started");
        } else {
            System.err.println("[GUI] ERROR: Engine is null!");
        }
    }
}
