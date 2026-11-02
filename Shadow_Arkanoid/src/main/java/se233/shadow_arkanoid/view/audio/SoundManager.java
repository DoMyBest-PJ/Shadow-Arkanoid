package se233.shadow_arkanoid.view.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SoundManager implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoundManager.class);
    private static final Map<String, List<String>> ASSETS = Map.of(
            "LIFE_LOST", List.of(
                    "/se233/shadow_arkanoid/assets/sound/livedrop1.mp4",
                    "/se233/shadow_arkanoid/assets/sound/livedrop2.mp4",
                    "/se233/shadow_arkanoid/assets/sound/livedrop3.mp4"),

            "STAGE_COMPLETE", List.of("/se233/shadow_arkanoid/assets/sound/Stage-Finish.mp3"),

            "PARRY_SUCCESS", List.of(
                    "/se233/shadow_arkanoid/assets/sound/Parry1.mp4",
                    "/se233/shadow_arkanoid/assets/sound/Parry2.mp4"),

            "BRICK_HIT", List.of(
                    "/se233/shadow_arkanoid/assets/sound/blockhit1.mp3",
                    "/se233/shadow_arkanoid/assets/sound/blockhit2.mp3"),

            "BRICK_DESTROYED", List.of(
                    "/se233/shadow_arkanoid/assets/sound/blockbreak1.mp3",
                    "/se233/shadow_arkanoid/assets/sound/blockbreak2.mp3"),

            "PADDLE_HIT", List.of(
                    "/se233/shadow_arkanoid/assets/sound/Block1.mp4",
                    "/se233/shadow_arkanoid/assets/sound/Block2.mp4"),

            "FRAME_HIT", List.of(
                    "/se233/shadow_arkanoid/assets/sound/Wood_hit1.ogg",
                    "/se233/shadow_arkanoid/assets/sound/Wood_hit2.mp3"),

            "WIND_SLASH", List.of("/se233/shadow_arkanoid/assets/sound/windslash_sound.mp4"),

            "UPGRADE_SELECTED", List.of("/se233/shadow_arkanoid/assets/sound/upgrade.mp4"));

    private static final Map<String, String> MUSIC_ASSETS = Map.of(
            "BGM_START", "/se233/shadow_arkanoid/assets/sound/bgm-start.mp3",
            "BGM_INGAME", "/se233/shadow_arkanoid/assets/sound/bgm-ingame.mp3");

    private static final double DEFAULT_MUSIC_VOLUME = 0.25;
    private final Map<String, List<AudioClip>> clips = new ConcurrentHashMap<>();
    private final Map<String, Media> musicTracks = new ConcurrentHashMap<>();
    private final ExecutorService loader = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "arkanoid-audio-loader");
        thread.setDaemon(true);
        return thread;
    });
    private double musicVolume = DEFAULT_MUSIC_VOLUME;
    private MediaPlayer currentMusicPlayer;
    private String currentMusicTrack;
    private String requestedMusicTrack;

    public SoundManager() {
        ASSETS.forEach((event, path) -> loader.submit(() -> load(event, path)));
        MUSIC_ASSETS.forEach((track, path) -> loader.submit(() -> loadMusic(track, path)));
    }

    public void play(String event) {
        List<AudioClip> eventClips = clips.get(event);
        if (eventClips != null && !eventClips.isEmpty()) {
            AudioClip clip = eventClips.get(ThreadLocalRandom.current().nextInt(eventClips.size()));
            if ("PARRY_SUCCESS".equals(event)) {
                clip.play(0.25);
            } else {
                clip.play();
            }
        }
    }

    public void playMusic(String track) {
        if (!MUSIC_ASSETS.containsKey(track)) {
            throw new IllegalArgumentException("Unknown music track: " + track);
        }
        requestedMusicTrack = track;
        if (track.equals(currentMusicTrack)) {
            return;
        }
        stopCurrentMusic();
        Media media = musicTracks.get(track);
        if (media != null) {
            startMusic(track, media);
        }
    }

    public void setMusicVolume(double volume) {
        if (!Double.isFinite(volume) || volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Music volume must be between zero and one");
        }
        musicVolume = volume;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(volume);
        }
    }

    public double musicVolume() {
        return musicVolume;
    }

    private void loadMusic(String track, String path) {
        var resource = SoundManager.class.getResource(path);
        if (resource == null) {
            LOGGER.warn("Required music asset not found: {}", path);
            return;
        }
        Platform.runLater(() -> {
            try {
                Media media = new Media(resource.toExternalForm());
                musicTracks.put(track, media);
                if (track.equals(requestedMusicTrack)) {
                    startMusic(track, media);
                }
            } catch (RuntimeException | LinkageError exception) {
                LOGGER.warn("Could not load music asset {}", path, exception);
            }
        });
    }

    private void startMusic(String track, Media media) {
        stopCurrentMusic();
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(musicVolume);
        player.setOnError(() -> LOGGER.warn("Could not play music track {}", track, player.getError()));
        currentMusicPlayer = player;
        currentMusicTrack = track;
        player.play();
    }

    private void stopCurrentMusic() {
        if (currentMusicPlayer != null) {
            currentMusicPlayer.stop();
            currentMusicPlayer.dispose();
            currentMusicPlayer = null;
        }
        currentMusicTrack = null;
    }

    private void load(String event, List<String> paths) {
        List<AudioClip> loadedClips = new ArrayList<>();
        for (String path : paths) {
            var resource = SoundManager.class.getResource(path);
            if (resource == null) {
                LOGGER.debug("Optional sound asset not found: {}", path);
                continue;
            }
            try {
                loadedClips.add(new AudioClip(resource.toExternalForm()));
            } catch (RuntimeException | LinkageError exception) {
                LOGGER.warn("Could not load optional sound asset {}", path, exception);
            }
        }
        if (!loadedClips.isEmpty()) {
            Platform.runLater(() -> clips.put(event, List.copyOf(loadedClips)));
        }
    }

    @Override
    public void close() {
        loader.shutdownNow();
        clips.clear();
        musicTracks.clear();
        requestedMusicTrack = null;
        stopCurrentMusic();
    }
}
