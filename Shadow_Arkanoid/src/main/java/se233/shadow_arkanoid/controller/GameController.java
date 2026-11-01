package se233.shadow_arkanoid.controller;

import java.util.EnumSet;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.GameState;
import se233.shadow_arkanoid.model.GameWorld;
import se233.shadow_arkanoid.view.GameRenderer;
import se233.shadow_arkanoid.view.audio.SoundManager;

public final class GameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);
    private final Canvas canvas;
    private final GameRenderer renderer;
    private final SoundManager soundManager;
    private final GameWorld world = new GameWorld();
    private final EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private AnimationTimer animationTimer;
    private long previousFrame;
    private boolean adjustingMusicVolume;

    public GameController(Canvas canvas, GameRenderer renderer, SoundManager soundManager) {
        this.canvas = canvas;
        this.renderer = renderer;
        this.soundManager = soundManager;
    }

    public void install(Scene scene) {
        canvas.setFocusTraversable(true);
        scene.setOnKeyPressed(event -> {
            if (pressedKeys.add(event.getCode())) {
                handleKeyPressed(event.getCode());
            }
        });
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        canvas.setOnMouseDragged(event -> {
            if (adjustingMusicVolume) {
                setMusicVolumeAt(renderer.logicalX(event.getX()));
            }
        });
        canvas.setOnMouseReleased(event -> adjustingMusicVolume = false);
        canvas.setOnMousePressed(event -> {
            canvas.requestFocus();
            if (event.getButton() == MouseButton.PRIMARY) {
                handleClick(renderer.logicalX(event.getX()), renderer.logicalY(event.getY()));
            }
        });
    }

    public void start() {
        if (animationTimer != null) {
            return;
        }
        syncMusicWithGameState();
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = previousFrame == 0 ? 0 : (now - previousFrame) / 1_000_000_000.0;
                previousFrame = now;
                double direction = (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT) ? 1 : 0)
                        - (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT) ? 1 : 0);
                double frameTime = Math.min(deltaTime, 0.05);
                world.update(frameTime, direction);
                renderer.updateEffects(frameTime, world.drainVisualCues(), world.balls());
                syncMusicWithGameState();
                for (String event : world.drainEvents()) {
                    soundManager.play(event);
                }
                renderer.render(world, soundManager.musicVolume());
            }
        };
        animationTimer.start();
    }

    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        soundManager.close();
    }

    private void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.ESCAPE || code == KeyCode.P) {
            world.pause();
        } else if (code == KeyCode.SPACE) {
            if (world.state() == GameState.READY) {
                world.launch();
            } else if (world.state() == GameState.PLAYING) {
                world.attemptParry();
            } else if (world.state() == GameState.MAIN_MENU) {
                world.startNewGame();
            }
        } else if (code == KeyCode.ENTER) {
            if (world.state() == GameState.READY) {
                world.launch();
            } else if (world.state() == GameState.MAIN_MENU) {
                world.startNewGame();
            }
        } else if (code == KeyCode.BACK_SPACE && world.state() == GameState.HOW_TO_PLAY) {
            world.returnToMenu();
        }
    }

    private void handleClick(double x, double y) {
        if (isOnMusicSlider(x, y)) {
            adjustingMusicVolume = true;
            setMusicVolumeAt(x);
            return;
        }
        switch (world.state()) {
            case MAIN_MENU -> {
                if (inside(x, y, 380, 310, 200, 52)) {
                    world.startNewGame();
                } else if (inside(x, y, 380, 382, 200, 52)) {
                    world.showHowToPlay();
                } else if (inside(x, y, 380, 454, 200, 48)) {
                    Stage stage = (Stage) canvas.getScene().getWindow();
                    stage.close();
                }
            }
            case HOW_TO_PLAY -> {
                if (inside(x, y, 390, 555, 180, 44)) {
                    world.returnToMenu();
                }
            }
            case READY -> world.launch();
            case PAUSED -> {
                if (inside(x, y, 370, 310, 220, 48)) {
                    world.pause();
                } else if (inside(x, y, 370, 370, 220, 48)) {
                    world.returnToMenu();
                }
            }
            case UPGRADE_SELECTION -> {
                double cardWidth = 250;
                double gap = 22;
                double start = (GameConfig.WIDTH - (cardWidth * 3 + gap * 2)) / 2;
                for (int i = 0; i < world.upgradeChoices().size(); i++) {
                    if (inside(x, y, start + i * (cardWidth + gap), 235, cardWidth, 230)) {
                        world.selectUpgrade(i);
                        return;
                    }
                }
            }
            case GAME_OVER, VICTORY -> {
                if (inside(x, y, 365, 375, 230, 48)) {
                    world.startNewGame();
                } else if (inside(x, y, 365, 435, 230, 44)) {
                    world.returnToMenu();
                }
            }
            default -> { }
        }
    }

    private void syncMusicWithGameState() {
        if (world.state() == GameState.MAIN_MENU || world.state() == GameState.HOW_TO_PLAY) {
            soundManager.playMusic("BGM_START");
        } else {
            soundManager.playMusic("BGM_INGAME");
        }
    }

    private boolean isOnMusicSlider(double x, double y) {
        return Math.abs(y - GameRenderer.BGM_SLIDER_Y) <= 16
                && x >= GameRenderer.BGM_SLIDER_LEFT - 12 && x <= GameRenderer.BGM_SLIDER_RIGHT + 12;
    }

    private void setMusicVolumeAt(double x) {
        double volume = (x - GameRenderer.BGM_SLIDER_LEFT)
                / (GameRenderer.BGM_SLIDER_RIGHT - GameRenderer.BGM_SLIDER_LEFT);
        soundManager.setMusicVolume(Math.max(0, Math.min(1, volume)));
    }

    private boolean inside(double x, double y, double left, double top, double width, double height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
}
