package game.config;

public interface GameConfig {
    default double getTargetFps() {
        return 240.0;
    }

    default double getFrameTime() {
        return 1.0 / getTargetFps();
    }

    default int getDifficulty() {
        return 1;
    }
}
