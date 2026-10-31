package se233.shadow_arkanoid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.shadow_arkanoid.controller.GameController;
import se233.shadow_arkanoid.view.GameRenderer;
import se233.shadow_arkanoid.view.audio.SoundManager;

public final class GameApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameApplication.class);

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, exception) -> LOGGER.error("Uncaught JavaFX thread exception", exception));
        Canvas canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        StackPane root = new StackPane(canvas);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        GameRenderer renderer = new GameRenderer(canvas);
        SoundManager soundManager = new SoundManager();
        GameController controller = new GameController(canvas, renderer, soundManager);
        Scene scene = new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT);
        controller.install(scene);
        stage.setTitle(GameConfig.TITLE);
        stage.setMinWidth(720);
        stage.setMinHeight(480);
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
