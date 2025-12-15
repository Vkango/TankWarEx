package game.config;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.io.ObjectOutputStream;
import java.io.File;

public class GameConfig {
    public static final Map<String, String> CUSTOM_SETTINGS = Map.of();

    public double getTargetFps() {
        return 240.0;
    }

    public double getFrameTime() {
        return 1.0 / getTargetFps();
    }

    public int getDifficulty() {
        return 1;
    }

    public void saveSettings() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(CUSTOM_SETTINGS);
            byte[] data = baos.toByteArray();
            baos.close();
            oos.close();
            File file = new File("config.dat");
            file.createNewFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {

    }

    public void setCustomSetting(String key, String value) {
        CUSTOM_SETTINGS.put(key, value);
    }

    public Object getCustomSetting(String key) {
        return CUSTOM_SETTINGS.get(key);
    }
}
