package game.engine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private boolean running = false;
    private boolean paused = false;
    private final List<Object> entities = new CopyOnWriteArrayList<>();
    private final Map<Integer, List<Object>> teamEntities = new HashMap<>();
    private final Map<Integer, Object> teamBases = new HashMap<>();
    private double worldWidth = 800;
    private double worldHeight = 600;

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
    }

    public void removeEntity(Object entity) {
        entities.remove(entity);
    }

    public Map<Integer, List<Object>> getTeamEntities() {
        return teamEntities;
    }

    public List<Object> getTeamEntityList(int teamIndex) {
        return teamEntities.computeIfAbsent(teamIndex, k -> new ArrayList<>());
    }

    public void addTeamEntity(int teamIndex, Object entity) {
        teamEntities.computeIfAbsent(teamIndex, k -> new ArrayList<>()).add(entity);
    }

    public void removeTeamEntity(int teamIndex, Object entity) {
        List<Object> team = teamEntities.get(teamIndex);
        if (team != null) {
            team.remove(entity);
        }
    }

    public Map<Integer, Object> getTeamBases() {
        return teamBases;
    }

    public void setTeamBase(int teamIndex, Object base) {
        teamBases.put(teamIndex, base);
    }

    public Object getTeamBase(int teamIndex) {
        return teamBases.get(teamIndex);
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(double width) {
        this.worldWidth = width;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(double height) {
        this.worldHeight = height;
    }

    public void reset() {
        running = false;
        paused = false;
        entities.clear();
        teamEntities.clear();
        teamBases.clear();
    }
}
