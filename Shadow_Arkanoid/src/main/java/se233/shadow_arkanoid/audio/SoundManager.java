package se233.shadow_arkanoid.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SoundManager implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoundManager.class);
    private static final Map<String, String> ASSETS = Map.of(
            "LIFE_LOST", "/se233/shadow_arkanoid/assets/sound/LiveDrop.mp3",
            "STAGE_COMPLETE", "/se233/shadow_arkanoid/assets/sound/Stage-Finish.mp3",
            "PARRY_SUCCESS", "/se233/shadow_arkanoid/assets/sound/Parry1.mp4",
            "BRICK_HIT", "/se233/shadow_arkanoid/assets/audio/Block1.mp3",
            "BRICK_DESTROYED", "/se233/shadow_arkanoid/assets/audio/Block2.mp3",
            "PADDLE_HIT", "/se233/shadow_arkanoid/assets/audio/paddle-hit.mp3",
            "PIERCING_ENDED", "/se233/shadow_arkanoid/assets/audio/piercing-end.mp3",
            "UPGRADE_SELECTED", "/se233/shadow_arkanoid/assets/audio/upgrade.mp3");
    private final Map<String, AudioClip> clips = new ConcurrentHashMap<>();
    private final ExecutorService loader = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "arkanoid-audio-loader");
        thread.setDaemon(true);
        return thread;
    });

    public SoundManager() {
        ASSETS.forEach((event, path) -> loader.submit(() -> load(event, path)));
    }

    public void play(String event) {
        AudioClip clip = clips.get(event);
        if (clip != null) {
            clip.play();
        }
    }

    private void load(String event, String path) {
        var resource = SoundManager.class.getResource(path);
        if (resource == null) {
            LOGGER.debug("Optional sound asset not found: {}", path);
            return;
        }
        try {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            Platform.runLater(() -> clips.put(event, clip));
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.warn("Could not load optional sound asset {}", path, exception);
        }
    }

    @Override
    public void close() {
        loader.shutdownNow();
        clips.clear();
    }
}
