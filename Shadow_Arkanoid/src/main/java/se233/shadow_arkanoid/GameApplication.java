package se233.shadow_arkanoid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.shadow_arkanoid.controller.GameController;
import se233.shadow_arkanoid.view.GameViewport;
import se233.shadow_arkanoid.view.GameRenderer;
import se233.shadow_arkanoid.view.audio.SoundManager;

public final class GameApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameApplication.class);

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, exception) -> LOGGER.error("Uncaught JavaFX thread exception", exception));
        Canvas canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        GameViewport root = new GameViewport(canvas);
        GameRenderer renderer = new GameRenderer(canvas);
        SoundManager soundManager = new SoundManager();
        GameController controller = new GameController(canvas, renderer, root, soundManager);
        double windowScale = 0.72;
        Scene scene = new Scene(root, FrameLayout.FRAME_WIDTH * windowScale,
                FrameLayout.FRAME_HEIGHT * windowScale);
        controller.install(scene);
        stage.setTitle(GameConfig.TITLE);
        stage.setMinWidth(600);
        stage.setMinHeight(665);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> controller.stop());
        stage.show();
        controller.start();
        LOGGER.info("Shadow Arkanoid started");
    }

    @Override
    public void stop() {
        LOGGER.info("Shadow Arkanoid stopped");
    }
}
