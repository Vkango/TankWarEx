package game.engine;

import game.map.MapProvider;
import game.rules.RuleProvider;
import game.config.GameConfig;

public final class GameContext {
    private static final GameContext INSTANCE = new GameContext();
    public final GameState state = new GameState();
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final ImageManager imageManager = ImageManager.getInstance();
    private static final EventBus eventBus = EventBus.getInstance();
    private static final InputManager inputManager = InputManager.getInstance();
    private MapProvider mapProvider;
    private RuleProvider ruleProvider;
    private GameEngine engine;
    private GameConfig config;

    public GameConfig getConfig() {
        return config;
    }

    public void setConfig(GameConfig config) {
        INSTANCE.config = config;
    }

    public void setMapProvider(MapProvider mapProvider) {
        INSTANCE.mapProvider = mapProvider;
    }

    public MapProvider getMapProvider() {
        return this.mapProvider;
    }

    public void setRuleProvider(RuleProvider ruleProvider) {
        INSTANCE.ruleProvider = ruleProvider;
    }

    public RuleProvider getRuleProvider() {
        return this.ruleProvider;
    }

    private GameContext() {
    }

    public static GameContext getInstance() {
        return INSTANCE;
    }

    public GameState getState() {
        return state;
    }

    public void reset() {
        state.reset();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public ImageManager getImageManager() {
        return imageManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void setGameEngine(GameEngine engine) {
        this.engine = engine;
    }

    public GameEngine getEngine() {
        return this.engine;
    }
}
