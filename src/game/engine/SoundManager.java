package game.engine;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class SoundManager {
    private final Map<String, Clip> sounds = new HashMap<>();
    private double globalVolume = 1.0;
    private static final SoundManager INSTANCE = new SoundManager();

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void loadSound(String key, String filePath) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filePath));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        sounds.put(key, clip);
    }

    public void playBGM(String key) {
        Clip clip = sounds.get(key);
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            setClipVolume(clip, globalVolume);
        }
    }

    public void playSoundEffect(String key) {
        Clip clip = sounds.get(key);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
            setClipVolume(clip, globalVolume);
        }
    }

    private void setClipVolume(Clip clip, double volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (20 * Math.log10(volume));
            gain.setValue(dB);
        }
    }

    public void stopSound(String key) {
        Clip clip = sounds.get(key);
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public void stopAll() {
        for (Clip clip : sounds.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public void cleanup() {
        sounds.values().forEach(clip -> {
            clip.stop();
            clip.close();
        });
        sounds.clear();
    }
}