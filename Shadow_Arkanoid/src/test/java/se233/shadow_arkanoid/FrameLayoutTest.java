package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.view.FrameLayout;
import se233.shadow_arkanoid.view.GameRenderer;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.Paddle;
import se233.shadow_arkanoid.model.system.StageManager;

class FrameLayoutTest {
    @Test
    void frameImageIsPackagedAtItsExistingResourcePath() {
        assertNotNull(FrameLayout.class.getResource(FrameLayout.IMAGE_RESOURCE));
    }

    @Test
    void stageBackgroundSpriteSheetHasFiveGameplaySizeFrames() throws IOException {
        var imageUrl = GameRenderer.class.getResource(
                "/se233/shadow_arkanoid/assets/picture/stageBG.png");
        assertNotNull(imageUrl);

        BufferedImage image = ImageIO.read(imageUrl);
        assertNotNull(image);
        assertEquals(GameConfig.WIDTH * 5, image.getWidth());
        assertEquals(GameConfig.HEIGHT, image.getHeight());
    }

    @Test
    void gameplayBoundsMatchTheFrameInterior() {
        assertEquals(927, FrameLayout.FRAME_WIDTH - FrameLayout.GAMEPLAY_X * 2);
        assertEquals(927, FrameLayout.GAMEPLAY_WIDTH);
        assertEquals(1212, FrameLayout.FRAME_HEIGHT - FrameLayout.GAMEPLAY_Y);
        assertEquals(1212, FrameLayout.GAMEPLAY_HEIGHT);
        assertEquals(FrameLayout.GAMEPLAY_WIDTH, GameConfig.WIDTH);
        assertEquals(FrameLayout.GAMEPLAY_HEIGHT, GameConfig.HEIGHT);
    }

    @Test
    void viewportScalingPreservesTheFrameAspectRatioAndCentersLetterboxing() {
        double scale = FrameLayout.scaleFor(1600, 900);

        assertEquals(900.0 / FrameLayout.FRAME_HEIGHT, scale, 0.0001);
        assertEquals(0, FrameLayout.verticalOffset(900, scale), 0.0001);
        assertTrue(FrameLayout.horizontalOffset(1600, scale) > 0);
    }

    @Test
    void bricksAndPaddleStayWithinTheGameplayRegion() {
        for (Brick brick : new StageManager().createStage(GameConfig.TOTAL_STAGES)) {
            assertTrue(brick.x() >= 0);
            assertTrue(brick.x() + brick.width() <= GameConfig.WIDTH);
            assertTrue(brick.y() >= GameConfig.HUD_HEIGHT);
            assertTrue(brick.y() + brick.height() < GameConfig.PADDLE_Y);
        }

        Paddle paddle = new Paddle();
        paddle.move(1, 10);
        assertTrue(paddle.right() <= GameConfig.WIDTH);
        paddle.move(-1, 10);
        assertTrue(paddle.left() >= 0);
    }
    @Test
    void katanaPaddleSpriteSheetIsPackagedAndConfiguredCorrectly() {
        assertNotNull(GameRenderer.class.getResource("/se233/shadow_arkanoid/assets/picture/katana_paddle_spriteSheet.png"));
        assertEquals(12, GameConfig.KATANA_PADDLE_SPRITE_FRAME_COUNT);
        assertEquals(4, GameConfig.KATANA_PADDLE_SPRITE_COLUMNS);
        assertEquals(3, GameConfig.KATANA_PADDLE_SPRITE_ROWS);
        assertEquals(288, GameConfig.KATANA_PADDLE_SPRITE_FRAME_WIDTH);
        assertEquals(48, GameConfig.KATANA_PADDLE_SPRITE_FRAME_HEIGHT);
    }
}