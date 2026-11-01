package se233.shadow_arkanoid.view.effects;

import java.net.URL;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.shadow_arkanoid.GameConfig;

final class SpecialSkillSlashEffect {
    private static final double FRAME_WIDTH = 600;
    private static final double FRAME_HEIGHT = 600;
    private static final Image SPRITE_SHEET = loadSpriteSheet();
    private static final DropShadow GLOW = new DropShadow(20, Color.web("#63eaff"));
    private final double x;
    private double age;

    SpecialSkillSlashEffect(double x) {
        this.x = x;
    }

    void update(double deltaTime) {
        age += deltaTime;
    }

    void render(GraphicsContext graphics) {
        double progress = Math.min(1, age / GameConfig.SPECIAL_SKILL_SLASH_DURATION_SECONDS);
        int frame = Math.min(GameConfig.WIND_SLASH_SPRITE_FRAME_COUNT - 1,
                (int) (progress * GameConfig.WIND_SLASH_SPRITE_FRAME_COUNT));
        int column = frame % GameConfig.WIND_SLASH_SPRITE_COLUMNS;
        int row = frame / GameConfig.WIND_SLASH_SPRITE_COLUMNS;
        double y = GameConfig.HUD_HEIGHT + 10;
        double height = GameConfig.PADDLE_Y - y - 10;

        graphics.save();
        graphics.setGlobalAlpha(Math.min(1, (1 - progress) * 2.5));
        graphics.setEffect(GLOW);
        graphics.drawImage(SPRITE_SHEET,
                column * FRAME_WIDTH, row * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT,
                x - 190, y, 380, height);
        graphics.restore();
    }

    boolean isFinished() {
        return age >= GameConfig.SPECIAL_SKILL_SLASH_DURATION_SECONDS;
    }

    private static Image loadSpriteSheet() {
        URL resource = SpecialSkillSlashEffect.class.getResource(
                "/se233/shadow_arkanoid/assets/picture/wind_slash_vfx_spritesheet.png");
        if (resource == null) {
            throw new IllegalStateException("Missing wind slash sprite sheet resource");
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            throw new IllegalStateException("Could not load wind slash sprite sheet", image.getException());
        }
        double expectedWidth = FRAME_WIDTH * GameConfig.WIND_SLASH_SPRITE_COLUMNS;
        double expectedHeight = FRAME_HEIGHT * GameConfig.WIND_SLASH_SPRITE_ROWS;
        if (image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
            throw new IllegalStateException("Wind slash sprite sheet must be 4200x2400");
        }
        return image;
    }
}
