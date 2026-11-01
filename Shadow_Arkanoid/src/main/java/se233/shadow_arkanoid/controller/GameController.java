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
import se233.shadow_arkanoid.view.GameViewport;
import se233.shadow_arkanoid.view.audio.SoundManager;

public final class GameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);
    private final Canvas canvas;
    private final GameRenderer renderer;
    private final GameViewport viewport;
    private final SoundManager soundManager;
    private final GameWorld world = new GameWorld();
    private final EnumSet<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    private AnimationTimer animationTimer;
    private long previousFrame;
    private boolean adjustingMusicVolume;

    public GameController(Canvas canvas, GameRenderer renderer, GameViewport viewport, SoundManager soundManager) {
        this.canvas = canvas;
        this.renderer = renderer;
        this.viewport = viewport;
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
                viewport.showState(world.state());
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
                if (inside(x, y, renderer.menuButtonX(), renderer.menuStartY(),
                        renderer.menuButtonWidth(), renderer.menuButtonHeight())) {
                    world.startNewGame();
                } else if (inside(x, y, renderer.menuButtonX(), renderer.menuHowToPlayY(),
                        renderer.menuButtonWidth(), renderer.menuButtonHeight())) {
                    world.showHowToPlay();
                } else if (inside(x, y, renderer.menuButtonX(), renderer.menuExitY(),
                        renderer.menuButtonWidth(), renderer.menuButtonHeight())) {
                    Stage stage = (Stage) canvas.getScene().getWindow();
                    stage.close();
                }
            }
            case HOW_TO_PLAY -> {
                if (inside(x, y, renderer.howToPlayBackX(), renderer.howToPlayBackY(), 180, 44)) {
                    world.returnToMenu();
                }
            }
            case READY -> world.launch();
            case PAUSED -> {
                if (inside(x, y, GameRenderer.PAUSE_BUTTON_X, GameRenderer.PAUSE_RESUME_Y, 220, 48)) {
                    world.pause();
                } else if (inside(x, y, GameRenderer.PAUSE_BUTTON_X, GameRenderer.PAUSE_MENU_Y, 220, 48)) {
                    world.returnToMenu();
                }
            }
            case UPGRADE_SELECTION -> {
                double cardWidth = GameRenderer.UPGRADE_CARD_WIDTH;
                double gap = GameRenderer.UPGRADE_CARD_GAP;
                double start = (GameConfig.WIDTH - (cardWidth * 3 + gap * 2)) / 2;
                for (int i = 0; i < world.upgradeChoices().size(); i++) {
                    if (inside(x, y, start + i * (cardWidth + gap), GameRenderer.UPGRADE_CARD_Y, cardWidth, 230)) {
                        world.selectUpgrade(i);
                        return;
                    }
                }
            }
            case GAME_OVER, VICTORY -> {
                if (inside(x, y, GameRenderer.END_BUTTON_X, GameRenderer.END_PLAY_AGAIN_Y, 230, 48)) {
                    world.startNewGame();
                } else if (inside(x, y, GameRenderer.END_BUTTON_X, GameRenderer.END_MENU_Y, 230, 44)) {
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
        return Math.abs(y - renderer.musicSliderY()) <= 16
                && x >= renderer.musicSliderLeft() - 12 && x <= renderer.musicSliderRight() + 12;
    }

    private void setMusicVolumeAt(double x) {
        double volume = (x - renderer.musicSliderLeft())
                / (renderer.musicSliderRight() - renderer.musicSliderLeft());
        soundManager.setMusicVolume(Math.max(0, Math.min(1, volume)));
    }

    private boolean inside(double x, double y, double left, double top, double width, double height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
}
