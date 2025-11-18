package game.engine;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public final class ImageManager {
    private final Map<String, Image> images = new HashMap<>();
    private static final ImageManager INSTANCE = new ImageManager();

    private ImageManager() {
    }

    public static ImageManager getInstance() {
        return INSTANCE;
    }

    public void loadImage(String key, String filePath) throws Exception {
        Image image = new Image("file:" + filePath);
        if (image.isError()) {
            throw new Exception("Failed to load image: " + filePath, image.getException());
        }
        images.put(key, image);
    }

    public Image getImage(String key) {
        return images.get(key);
    }

    public void unloadImage(String key) {
        images.remove(key);
    }

    public void cleanup() {
        images.clear();
    }
}