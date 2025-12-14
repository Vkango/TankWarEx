package ui.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import game.engine.GameContext;
import game.engine.SaveManager;
import game.rules.RuleProvider;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import plugin.api.PluginManager;
import java.util.Optional;

public class GUI extends Application {
    private static GUI instance;
    private static GameContext staticContext;

    private Canvas canvas;
    private GameRenderer renderer;
    private GameContext context;
    private UIState currentState = UIState.MAIN_MENU;

    private StackPane root;
    private Pane gamePane;
    private MainMenuPane mainMenuPane;
    private LevelSelectPane levelSelectPane;
    private PauseMenuPane pauseMenuPane;
    private GameOverPane gameOverPane;

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

    private void switchState(UIState newState) {
        currentState = newState;

        mainMenuPane.setVisible(false);
        levelSelectPane.setVisible(false);
        pauseMenuPane.setVisible(false);
        gameOverPane.setVisible(false);
        gamePane.setVisible(false);

        switch (newState) {
            case MAIN_MENU:
                mainMenuPane.setVisible(true);
                break;
            case LEVEL_SELECT:
                levelSelectPane.setVisible(true);
                break;
            case PLAYING:
                gamePane.setVisible(true);
                if (context.getEngine() != null) {
                    System.out.println("[GUI] Starting game engine");
                    context.getEngine().start();
                }
                break;
            case PAUSED:
                gamePane.setVisible(true);
                pauseMenuPane.setVisible(true);
                break;
            case GAME_OVER:
                gamePane.setVisible(true);
                gameOverPane.setVisible(true);
                break;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TankWarEx");
        primaryStage.setOnCloseRequest(e -> {
            shutdown();
        });

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        mainMenuPane = new MainMenuPane();
        mainMenuPane.setOnStartGame(() -> {
            switchState(UIState.LEVEL_SELECT);
        });
        mainMenuPane.setOnExitGame(() -> {
            shutdown();
        });

        levelSelectPane = new LevelSelectPane();
        levelSelectPane.setOnBack(() -> {
            switchState(UIState.MAIN_MENU);
        });
        levelSelectPane.setOnLevelSelected(map -> {
            startLevel(map);
        });

        gamePane = new Pane();
        canvas = new Canvas();
        canvas.widthProperty().bind(gamePane.widthProperty());
        canvas.heightProperty().bind(gamePane.heightProperty());

        gamePane.getChildren().add(canvas);

        pauseMenuPane = new PauseMenuPane();
        pauseMenuPane.setOnResume(() -> {
            context.getEngine().resume();
            switchState(UIState.PLAYING);
        });
        pauseMenuPane.setOnRestart(() -> {
            context.getEngine().stop();
            context.getEngine().initialize();
            context.getEngine().start();
            switchState(UIState.PLAYING);
        });
        pauseMenuPane.setOnBackToLevelSelect(() -> {
            context.getEngine().stop();
            switchState(UIState.LEVEL_SELECT);
        });
        pauseMenuPane.setOnSaveGame(() -> {
            saveGame();
        });

        gameOverPane = new GameOverPane();
        gameOverPane.setOnRestart(() -> {
            context.getEngine().stop();
            context.getEngine().initialize();
            context.getEngine().start();
            switchState(UIState.PLAYING);
        });
        gameOverPane.setOnBackToLevelSelect(() -> {
            context.getEngine().stop();
            switchState(UIState.LEVEL_SELECT);
        });

        root.getChildren().addAll(
                mainMenuPane,
                levelSelectPane,
                gamePane,
                pauseMenuPane,
                gameOverPane);

        game.engine.EventBus.getInstance().subscribe("GameOver", event -> {
            Platform.runLater(() -> {
                context.getSoundManager().stopAll();
                String message = event.getMessage();
                if (message == null) {
                    message = (String) event.getData();
                }
                gameOverPane.setMessage(message);
                switchState(UIState.GAME_OVER);
            });
        });

        game.engine.EventBus.getInstance().subscribe("ShowToast", event -> {
            Platform.runLater(() -> {
                String message = event.getMessage();
                if (message == null) {
                    message = (String) event.getData();
                }
                if (message != null && !message.isEmpty()) {
                    Toast toast = new Toast(message);
                    root.getChildren().add(toast);
                    StackPane.setAlignment(toast, javafx.geometry.Pos.BOTTOM_CENTER);
                    StackPane.setMargin(toast, new javafx.geometry.Insets(0, 0, 50, 0));
                    toast.show();
                }
            });
        });

        Scene scene = new Scene(root, 800, 600);

        setupKeyHandlers(scene);

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        switchState(UIState.MAIN_MENU);
    }

    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            handleKeyPress(event);
        });

        scene.setOnKeyReleased(event -> {
            if (currentState == UIState.PLAYING && context.getEngine() != null) {
                context.getInputManager().releaseKey(event.getCode());
                context.getEngine().handleKeyRelease(event.getCode());
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            togglePause();
            return;
        }

        // Zoom controls
        if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.ADD) {
            if (renderer != null) {
                renderer.setScale(renderer.getScale() + 0.1);
            }
        }
        if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
            if (renderer != null) {
                renderer.setScale(renderer.getScale() - 0.1);
            }
        }

        if (currentState == UIState.PLAYING &&
                context.getEngine() != null &&
                !context.getEngine().getState().isPaused()) {
            context.getInputManager().pressKey(event.getCode());
            context.getEngine().handleKeyPress(event.getCode());
        }
    }

    private void togglePause() {
        if (currentState == UIState.PLAYING) {
            context.getEngine().pause();
            switchState(UIState.PAUSED);
        } else if (currentState == UIState.PAUSED) {
            context.getEngine().resume();
            switchState(UIState.PLAYING);
        }
    }

    private void shutdown() {
        if (context.getEngine() != null) {
            context.getEngine().stop();
        }
        Platform.exit();
        System.exit(0);
    }

    private void startLevel(game.map.MapProvider map) {
        System.out.println("[GUI] Starting level: " + map.getMapName());
        context.setMapProvider(map);

        RuleProvider matchingRuleProvider = PluginManager.getInstance().getRuleProviderForMap(map);
        if (matchingRuleProvider != null) {
            context.setRuleProvider(matchingRuleProvider);
            System.out.println("[GUI] Set RuleProvider: " + matchingRuleProvider.getClass().getName());
        }

        // 检查是否有该地图的存档
        if (SaveManager.hasSaveGame(context)) {
            Platform.runLater(() -> {
                String saveInfo = SaveManager.getSaveInfo(context);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("发现存档");
                alert.setHeaderText("检测到该地图的存档");
                alert.setContentText(saveInfo + "\n\n是否加载该存档？");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    loadGame();
                } else {
                    startNewGame();
                }
            });
        } else {
            startNewGame();
        }
    }

    private void startNewGame() {
        context.getEngine().initialize();
        context.getEngine().initResources();
        if (renderer == null) {
            renderer = new GameRenderer(canvas);
            renderer.start();
        }

        switchState(UIState.PLAYING);
    }

    private void saveGame() {
        try {
            SaveManager.saveGame(context);
            context.showToast("游戏已保存");
        } catch (Exception e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("保存失败");
                alert.setHeaderText(null);
                alert.setContentText("保存游戏失败: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadGame() {
        try {
            SaveManager.loadGame(context);
            // SaveManager.loadGame 已经调用了 reloadContext，不需要再调用
            if (renderer == null) {
                renderer = new GameRenderer(canvas);
                renderer.start();
            }
            switchState(UIState.PLAYING);
        } catch (Exception e) {
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载失败");
                alert.setHeaderText(null);
                alert.setContentText("加载游戏失败: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }
}