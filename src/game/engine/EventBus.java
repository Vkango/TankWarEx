package game.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final Map<String, List<Consumer<GameEvent>>> eventTypeListeners = new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(String eventType, Consumer<GameEvent> listener) {
        eventTypeListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void unsubscribe(String eventType, Consumer<GameEvent> listener) {
        List<Consumer<GameEvent>> listeners = eventTypeListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void publish(GameEvent event) {
        List<Consumer<GameEvent>> listeners = eventTypeListeners.get(event.getType());
        if (listeners != null) {
            for (Consumer<GameEvent> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("Event listener error: " + e.getMessage());
                }
            }
        }
    }

    public void clear() {
        eventTypeListeners.clear();
    }

    public void clearType(String eventType) {
        eventTypeListeners.remove(eventType);
    }

    public int getListenerCount(String eventType) {
        List<Consumer<GameEvent>> listeners = eventTypeListeners.get(eventType);
        return listeners != null ? listeners.size() : 0;
    }
}
