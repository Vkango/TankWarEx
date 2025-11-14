package game.engine;

public class GameEvent {
    private final String type;
    private final Object data;
    private final String message;

    public GameEvent(String type, Object data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
