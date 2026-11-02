package se233.shadow_arkanoid.view;

import java.net.URL;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import se233.shadow_arkanoid.model.GameState;

public final class GameViewport extends Pane {
    private final ImageView frameImage;
    private final Canvas gameplayCanvas;
    private boolean menuView = true;

    public GameViewport(Canvas gameplayCanvas) {
        this.gameplayCanvas = gameplayCanvas;
        URL imageUrl = GameViewport.class.getResource(FrameLayout.IMAGE_RESOURCE);
        if (imageUrl == null) {
            throw new IllegalStateException("Frame image was not found: " + FrameLayout.IMAGE_RESOURCE);
        }
        Image image = new Image(imageUrl.toExternalForm());
        if (image.isError()) {
            throw new IllegalStateException("Could not load frame image: " + FrameLayout.IMAGE_RESOURCE,
                    image.getException());
        }
        if (image.getWidth() != FrameLayout.FRAME_WIDTH || image.getHeight() != FrameLayout.FRAME_HEIGHT) {
            throw new IllegalStateException("Unexpected frame image dimensions: "
                    + image.getWidth() + "x" + image.getHeight());
        }

        frameImage = new ImageView(image);
        frameImage.setPreserveRatio(true);
        frameImage.setSmooth(false);
        frameImage.setManaged(false);
        frameImage.setVisible(false);
        gameplayCanvas.setManaged(false);
        gameplayCanvas.setFocusTraversable(true);
        setStyle("-fx-background-color: #080808;");
        getChildren().addAll(frameImage, gameplayCanvas);
    }

    public void showState(GameState state) {
        boolean showMenuView = state == GameState.MAIN_MENU || state == GameState.HOW_TO_PLAY;
        if (menuView != showMenuView) {
            menuView = showMenuView;
            frameImage.setVisible(!menuView);
            requestLayout();
        }
    }

    @Override
    protected void layoutChildren() {
        if (menuView) {
            gameplayCanvas.setWidth(getWidth());
            gameplayCanvas.setHeight(getHeight());
            gameplayCanvas.relocate(0, 0);
            return;
        }
        double scale = FrameLayout.scaleFor(getWidth(), getHeight());
        if (scale <= 0) {
            return;
        }
        double frameWidth = FrameLayout.FRAME_WIDTH * scale;
        double frameHeight = FrameLayout.FRAME_HEIGHT * scale;
        double offsetX = FrameLayout.horizontalOffset(getWidth(), scale);
        double offsetY = FrameLayout.verticalOffset(getHeight(), scale);

        frameImage.setFitWidth(frameWidth);
        frameImage.setFitHeight(frameHeight);
        frameImage.relocate(offsetX, offsetY);

        double gameplayWidth = FrameLayout.GAMEPLAY_WIDTH * scale;
        double gameplayHeight = FrameLayout.GAMEPLAY_HEIGHT * scale;
        gameplayCanvas.setWidth(gameplayWidth);
        gameplayCanvas.setHeight(gameplayHeight);
        gameplayCanvas.relocate(offsetX + FrameLayout.GAMEPLAY_X * scale,
                offsetY + FrameLayout.GAMEPLAY_Y * scale);
    }
}
