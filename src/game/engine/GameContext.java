package game.engine;

public final class GameContext {
    private static final GameContext INSTANCE = new GameContext();
    public final GameState state = new GameState();
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final ImageManager imageManager = ImageManager.getInstance();

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
}
