package game.engine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private boolean running = false;
    private boolean paused = false;
    private final List<Object> entities = new CopyOnWriteArrayList<>();

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public List<Object> getEntities() {
        return entities;
    }

    public void addEntity(Object entity) {
        entities.add(entity);
        entities.sort(Comparator.comparingInt(e -> {
            if (e instanceof Entity ent) {
                return ent.getZIndex();
            }
            return 0;
        }));
    }

    public void removeEntity(Object entity) {
        entities.remove(entity);
    }

    public void reset() {
        running = false;
        paused = false;
        entities.clear();
    }
}
