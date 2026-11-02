package se233.shadow_arkanoid.view;

import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.BrickType;
import se233.shadow_arkanoid.model.GameState;
import se233.shadow_arkanoid.model.GameWorld;
import se233.shadow_arkanoid.model.upgrade.Upgrade;
import se233.shadow_arkanoid.model.Paddle;
import se233.shadow_arkanoid.view.effects.VisualEffectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameRenderer.class);
    public static final double BGM_SLIDER_LEFT = 82;
    public static final double BGM_SLIDER_RIGHT = 202;
    private static final double BGM_SLIDER_Y = GameConfig.HEIGHT - 55;
    private static final double MENU_BUTTON_WIDTH_RATIO = 0.46;
    private static final double MENU_BUTTON_HEIGHT_RATIO = 0.068;
    private static final double MENU_BUTTON_START_Y_RATIO = 0.47;
    private static final double MENU_BUTTON_GAP_RATIO = 0.09;
    private static final double HOW_TO_PLAY_BACK_WIDTH = 180;
    private static final double HOW_TO_PLAY_BACK_HEIGHT = 44;
    private static final int STAGE_BACKGROUND_COLUMNS = 5;
    private static final String STAGE_BACKGROUND_RESOURCE =
            "/se233/shadow_arkanoid/assets/picture/stageBG.png";
    public static final double PAUSE_BUTTON_X = (GameConfig.WIDTH - 220) / 2;
    public static final double PAUSE_RESUME_Y = GameConfig.HEIGHT / 2 - 26;
    public static final double PAUSE_MENU_Y = GameConfig.HEIGHT / 2 + 34;
    public static final double UPGRADE_CARD_WIDTH = 250;
    public static final double UPGRADE_CARD_GAP = 22;
    public static final double UPGRADE_CARD_Y = GameConfig.HEIGHT / 2 - 176;
    public static final double END_BUTTON_X = (GameConfig.WIDTH - 230) / 2;
    public static final double END_PLAY_AGAIN_Y = GameConfig.HEIGHT / 2 + 14;
    public static final double END_MENU_Y = GameConfig.HEIGHT / 2 + 74;
    private static final Color BACKGROUND = Color.web("#080d1b");
    private static final Color CYAN = Color.web("#35e7ff");
    private static final Color MAGENTA = Color.web("#ff4fc8");
    private static final Color ENERGY_ORANGE = Color.web("#ff8518");
    private static final RadialGradient ENERGY_HALO = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(255, 242, 188, 0.76)),
            new Stop(0.35, Color.rgb(255, 143, 28, 0.52)),
            new Stop(1, Color.TRANSPARENT));
    private static final RadialGradient ENERGY_BALL = new RadialGradient(0, 0, 0.36, 0.32, 0.68, true,
            CycleMethod.NO_CYCLE, new Stop(0, Color.web("#fff7d5")),
            new Stop(0.24, Color.web("#ffd16b")),
            new Stop(0.7, Color.web("#ff7b12")),
            new Stop(1, Color.web("#d83b08")));
    private final Canvas canvas;
    private final DropShadow ballGlow = new DropShadow(18, Color.web("#65edff"));
    private final DropShadow energyBallGlow = new DropShadow(23, Color.web("#ff7614"));
    private final DropShadow specialSkillGlow = new DropShadow(12, Color.web("#ff857d"));
    private final Image stageBackgroundSpriteSheet = loadStageBackgroundSpriteSheet();
    private final Image specialSkillImage = loadImage(
            "/se233/shadow_arkanoid/assets/picture/special_skill.png");
    private final Image katanaPaddleSpriteSheet = loadKatanaPaddleSpriteSheet();
    private final VisualEffectManager visualEffects = new VisualEffectManager();
    private double scale = 1;
    private double offsetX;
    private double offsetY;
    private boolean mainMenuLayout;
    private double menuWidth;
    private double menuHeight;
    private double menuButtonX;
    private double menuButtonWidth;
    private double menuButtonHeight;
    private double menuStartY;
    private double menuHowToPlayY;
    private double menuExitY;
    private double menuSliderY;
    private double menuSliderLeft;
    private double menuSliderRight;
    private double howToPlayBackX;
    private double howToPlayBackY;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(GameWorld world, double musicVolume) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        mainMenuLayout = world.state() == GameState.MAIN_MENU || world.state() == GameState.HOW_TO_PLAY;
        if (mainMenuLayout) {
            menuWidth = canvasWidth;
            menuHeight = canvasHeight;
            scale = 1;
            offsetX = 0;
            offsetY = 0;
            menuButtonWidth = Math.min(300, menuWidth * MENU_BUTTON_WIDTH_RATIO);
            menuButtonHeight = Math.min(72, menuHeight * MENU_BUTTON_HEIGHT_RATIO);
            menuButtonX = (menuWidth - menuButtonWidth) / 2;
            menuStartY = menuHeight * MENU_BUTTON_START_Y_RATIO;
            menuHowToPlayY = menuStartY + menuHeight * MENU_BUTTON_GAP_RATIO;
            menuExitY = menuHowToPlayY + menuHeight * MENU_BUTTON_GAP_RATIO;
            menuSliderY = menuHeight - Math.max(34, menuHeight * 0.055);
            menuSliderLeft = Math.max(44, menuWidth * 0.085);
            menuSliderRight = menuSliderLeft + Math.min(160, menuWidth * 0.15);
        } else {
            scale = Math.min(canvasWidth / GameConfig.WIDTH, canvasHeight / GameConfig.HEIGHT);
            offsetX = (canvasWidth - GameConfig.WIDTH * scale) / 2;
            offsetY = (canvasHeight - GameConfig.HEIGHT * scale) / 2;
        }
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);
        drawBackdrop(gc, mainMenuLayout ? menuWidth : GameConfig.WIDTH,
                mainMenuLayout ? menuHeight : GameConfig.HEIGHT);

        if (world.state() == GameState.MAIN_MENU || world.state() == GameState.HOW_TO_PLAY) {
            drawMenu(gc);
            if (world.state() == GameState.HOW_TO_PLAY) {
                drawHowToPlay(gc);
            }
        } else {
            drawGame(gc, world);
            switch (world.state()) {
                case READY -> drawReady(gc);
                case PAUSED -> drawPause(gc);
                case STAGE_COMPLETE -> drawCenterMessage(gc, "STAGE COMPLETE", "Choose your next upgrade...");
                case UPGRADE_SELECTION -> drawUpgradeSelection(gc, world);
                case LIFE_LOST -> drawCenterMessage(gc, "LIFE LOST", "The next launch is ready.");
                case GAME_OVER -> drawEndScreen(gc, world, false);
                case VICTORY -> drawEndScreen(gc, world, true);
                default -> { }
            }
        }
        drawFooter(gc, world, musicVolume);
        gc.restore();
    }

    public double logicalX(double canvasX) {
        return (canvasX - offsetX) / scale;
    }

    public double logicalY(double canvasY) {
        return (canvasY - offsetY) / scale;
    }

    public double musicSliderY() {
        return mainMenuLayout ? menuSliderY : BGM_SLIDER_Y;
    }

    public double musicSliderLeft() {
        return mainMenuLayout ? menuSliderLeft : BGM_SLIDER_LEFT;
    }

    public double musicSliderRight() {
        return mainMenuLayout ? menuSliderRight : BGM_SLIDER_RIGHT;
    }

    public double menuButtonX() {
        return menuButtonX;
    }

    public double menuButtonWidth() {
        return menuButtonWidth;
    }

    public double menuButtonHeight() {
        return menuButtonHeight;
    }

    public double menuStartY() {
        return menuStartY;
    }

    public double menuHowToPlayY() {
        return menuHowToPlayY;
    }

    public double menuExitY() {
        return menuExitY;
    }

    public double howToPlayBackX() {
        return howToPlayBackX;
    }

    public double howToPlayBackY() {
        return howToPlayBackY;
    }

    public void updateEffects(double deltaTime, List<GameWorld.VisualCue> cues, List<Ball> balls) {
        visualEffects.update(deltaTime, cues, balls);
    }

    private void drawBackdrop(GraphicsContext gc, double width, double height) {
        gc.setFill(new LinearGradient(0, 0, 0, height, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#101a32")), new Stop(1, BACKGROUND)));
        gc.fillRect(0, 0, width, height);
        gc.setStroke(Color.web("#182443"));
        gc.setLineWidth(1);
        for (int x = 28; x < width; x += 48) {
            gc.strokeLine(x, 0, x, height);
        }
        for (int y = 16; y < height; y += 48) {
            gc.strokeLine(0, y, width, y);
        }
    }

    private void drawMenu(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setEffect(new DropShadow(30, Color.web("#29cfff")));
        gc.setFill(Color.WHITE);
        double centerX = menuWidth / 2;
        double titleY = menuHeight * 0.24;
        gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, Math.min(52, menuHeight * 0.066)));
        gc.fillText("ARKANOID", centerX, titleY);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, Math.min(30, menuHeight * 0.038)));
        gc.fillText("SHADOW-TWICE", centerX, titleY + menuHeight * 0.07);
        gc.setEffect(null);
        drawButton(gc, "START GAME", menuButtonX, menuStartY,
                menuButtonWidth, menuButtonHeight, CYAN);
        drawButton(gc, "HOW TO PLAY", menuButtonX, menuHowToPlayY,
                menuButtonWidth, menuButtonHeight, MAGENTA);
        drawButton(gc, "EXIT", menuButtonX, menuExitY,
                menuButtonWidth, menuButtonHeight, Color.web("#65708c"));
        gc.setFill(Color.web("#93a4c4"));
        gc.setFont(Font.font("Monospaced", Math.min(16, menuHeight * 0.02)));
        gc.fillText("BREAK THE ARK. MASTER THE PARRY. SURVIVE THE RUN.",
                centerX, menuHeight * 0.86);
    }

    private void drawHowToPlay(GraphicsContext gc) {
        double panelWidth = Math.min(780, menuWidth * 0.92);
        double panelHeight = Math.min(600, menuHeight * 0.84);
        double panelX = (menuWidth - panelWidth) / 2;
        double panelY = (menuHeight - panelHeight) / 2;
        gc.setFill(Color.rgb(2, 5, 13, 0.78));
        gc.fillRect(0, 0, menuWidth, menuHeight);
        drawPanel(gc, panelX, panelY, panelWidth, panelHeight);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.fillText("HOW TO PLAY", menuWidth / 2, panelY + 48);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#dce8ff"));
        String[] lines = {
                "KEYBOARD ONLY      Move the paddle",
                "A / LEFT ARROW     Move left",
                "D / RIGHT ARROW    Move right",
                "SPACE              Attempt to Parry",
                "ESC                Open menu",
                "",
                "Break every brick to clear a stage and choose an upgrade.",
                "Strong bricks take more hit. Bonus bricks award extra points.",
                "Catch SPECIAL SKILL drops to slash a vertical brick line.",
                "Parry just before a descending ball reaches your paddle.",
                "A Perfect Parry grants +150 points and get pierces.",
                "The Perfect Parry pierces every brick until it hits a wall,",
                "You have five lives. Press ESC or P to pause."
        };
        double lineGap = Math.min(35, (panelHeight - 190) / (lines.length - 1));
        gc.setFont(Font.font("Monospaced", Math.min(16, Math.max(12, lineGap * 0.55))));
        double y = panelY + 100;
        for (String line : lines) {
            gc.fillText(line, panelX + 30, y, panelWidth - 60);
            y += lineGap;
        }
        howToPlayBackX = (menuWidth - HOW_TO_PLAY_BACK_WIDTH) / 2;
        howToPlayBackY = panelY + panelHeight - 62;
        drawButton(gc, "BACK", howToPlayBackX, howToPlayBackY,
                HOW_TO_PLAY_BACK_WIDTH, HOW_TO_PLAY_BACK_HEIGHT, MAGENTA);
    }

    private void drawGame(GraphicsContext gc, GameWorld world) {
        drawStageBackground(gc, world.stage());
        drawHud(gc, world);
        for (Brick brick : world.bricks()) {
            drawBrick(gc, brick);
        }
        drawSpecialSkillDrops(gc, world);
        drawPaddle(gc, world);
        visualEffects.renderTrails(gc);
        for (Ball ball : world.balls()) {
            drawBall(gc, ball, visualEffects.energyFor(ball));
        }
        if (!world.message().isEmpty()) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 36));
            gc.setFill(world.message().contains("PARRY") ? Color.web("#fff28a") : Color.WHITE);
            gc.setEffect(new DropShadow(15, CYAN));
            gc.fillText(world.message(), GameConfig.WIDTH / 2, GameConfig.HEIGHT * 0.48);
            gc.setEffect(null);
        }
        drawStatus(gc, world);
        visualEffects.renderEffects(gc);
    }

    private void drawStageBackground(GraphicsContext gc, int stage) {
        int stageIndex = Math.clamp(stage - 1, 0, STAGE_BACKGROUND_COLUMNS - 1);
        double frameWidth = stageBackgroundSpriteSheet.getWidth() / STAGE_BACKGROUND_COLUMNS;
        gc.drawImage(stageBackgroundSpriteSheet,
                stageIndex * frameWidth, 0, frameWidth, GameConfig.HEIGHT,
                0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
    }

    private void drawHud(GraphicsContext gc, GameWorld world) {
        gc.setFill(Color.web("#10172b"));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HUD_HEIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(Color.web("#f4cd75"));
        gc.fillText("HIGH SCORE  " + world.scoreManager().highestScore(), 30, 30);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 27));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE  " + world.scoreManager().score(), 30, 70);
        gc.setFill(CYAN);
        gc.fillText("STAGE  " + world.stage(), 30, 116);
        gc.setFill(Color.web("#ff718e"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("LIVES  " + world.lifeManager().lives(), GameConfig.WIDTH / 2, 70);
        gc.setFill(Color.web("#9aaccc"));
        gc.setFont(Font.font("Monospaced", 18));
        gc.fillText("A / D  MOVE     SPACE  PARRY", GameConfig.WIDTH / 2, 116);
        drawParryMeter(gc, world);
        if (!world.activeUpgrades().isEmpty()) {
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(Color.web("#f4cd75"));
            gc.setFont(Font.font("Monospaced", 16));
            String upgrades = String.join("  ", world.activeUpgrades());
            gc.fillText("UPGRADES: " + upgrades, GameConfig.WIDTH - 27, 150);
        }
    }

    private void drawParryMeter(GraphicsContext gc, GameWorld world) {
        double x = GameConfig.WIDTH - 220;
        double y = 55;
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(world.parrySystem().isAvailable() ? CYAN : Color.web("#7d88a3"));
        gc.fillText(world.parrySystem().isAvailable() ? "PARRY READY" : "PARRY CHARGING", x, y);
        gc.setFill(Color.web("#26324c"));
        gc.fillRoundRect(x, y + 14, 180, 9, 9, 9);
        gc.setFill(world.parrySystem().isAvailable() ? CYAN : MAGENTA);
        gc.fillRoundRect(x, y + 14, 180 * Math.max(0, Math.min(1, world.parrySystem().cooldownFraction())),
                9, 9, 9);
    }

    private void drawBrick(GraphicsContext gc, Brick brick) {
        Color color = switch (brick.type()) {
            case NORMAL -> Color.web("#A8FF00");
            case STRONG -> brick.durability() == 2 ? Color.web("#FF3B30") : Color.web("#A8FF00");
            case BONUS -> Color.web("#ffc34f");
        };
        if (brick.hitFlash() > 0) color = Color.WHITE;
        gc.setFill(color.deriveColor(0, 0.78, 0.72, 1));
        gc.fillRoundRect(brick.x(), brick.y(), brick.width(), brick.height(), 9, 9);
        gc.setFill(color);
        gc.fillRoundRect(brick.x() + 2, brick.y() + 2, brick.width() - 4, brick.height() - 5, 7, 7);
        gc.setFill(Color.rgb(255, 255, 255, 0.35));
        gc.fillRoundRect(brick.x() + 5, brick.y() + 4, brick.width() - 10, 4, 4, 4);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(brick.x(), brick.y(), brick.width(), brick.height(), 9, 9);
        if (brick.type() == BrickType.BONUS) {
            gc.setFill(Color.web("#462d00"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("+", brick.x() + brick.width() / 2, brick.y() + brick.height() / 2 + 1);
        }
    }

    private void drawSpecialSkillDrops(GraphicsContext gc, GameWorld world) {
        double size = GameConfig.SPECIAL_SKILL_DROP_SIZE;
        gc.save();
        gc.setEffect(specialSkillGlow);
        for (var drop : world.specialSkillDrops()) {
            gc.drawImage(specialSkillImage, drop.x() - size / 2, drop.y() - size / 2, size, size);
        }
        gc.restore();
    }

    private static Image loadImage(String resourcePath) {
        var resource = GameRenderer.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing game image resource: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            throw new IllegalStateException("Could not load game image: " + resourcePath, image.getException());
        }
        return image;
    }

    private static Image loadStageBackgroundSpriteSheet() {
        Image image = loadImage(STAGE_BACKGROUND_RESOURCE);
        if (image.getWidth() != GameConfig.WIDTH * STAGE_BACKGROUND_COLUMNS
                || image.getHeight() != GameConfig.HEIGHT) {
            throw new IllegalStateException("Unexpected stage background dimensions: "
                    + image.getWidth() + "x" + image.getHeight());
        }
        return image;
    }

    private static Image loadKatanaPaddleSpriteSheet() {
        var resource = GameRenderer.class.getResource(
                "/se233/shadow_arkanoid/assets/picture/katana_paddle_spriteSheet.png");
        if (resource == null) {
            resource = GameRenderer.class.getResource(
                    "/se233/shadow_arkanoid/assets/picture/\u0E49katana_paddle_spriteSheet.png");
        }
        if (resource == null) {
            LOGGER.warn("Missing katana paddle sprite sheet resource");
            return null;
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            LOGGER.warn("Could not load katana paddle sprite sheet", image.getException());
            return null;
        }
        return image;
    }

    private void drawPaddle(GraphicsContext gc, GameWorld world) {
        Paddle paddle = world.paddle();
        double baseX = paddle.left() - 6;
        double baseY = paddle.y() - 4;
        double baseWidth = paddle.width() + 12;
        double baseHeight = paddle.height() + 10;
        gc.setFill(Color.rgb(3, 10, 24, 0.92));
        gc.fillRoundRect(baseX, baseY, baseWidth, baseHeight, 14, 14);
        gc.setStroke(CYAN);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(baseX, baseY, baseWidth, baseHeight, 14, 14);

        if (katanaPaddleSpriteSheet != null) {
            double cycleTime = visualEffects.elapsedTime() % GameConfig.KATANA_PADDLE_ANIMATION_CYCLE_SECONDS;
            int frameIndex = (int) ((cycleTime / GameConfig.KATANA_PADDLE_ANIMATION_CYCLE_SECONDS)
                    * GameConfig.KATANA_PADDLE_SPRITE_FRAME_COUNT);
            frameIndex = Math.min(GameConfig.KATANA_PADDLE_SPRITE_FRAME_COUNT - 1, Math.max(0, frameIndex));

            int col = frameIndex % GameConfig.KATANA_PADDLE_SPRITE_COLUMNS;
            int row = frameIndex / GameConfig.KATANA_PADDLE_SPRITE_COLUMNS;
            double sx = col * GameConfig.KATANA_PADDLE_SPRITE_FRAME_WIDTH;
            double sy = row * GameConfig.KATANA_PADDLE_SPRITE_FRAME_HEIGHT;
            double sw = GameConfig.KATANA_PADDLE_SPRITE_FRAME_WIDTH;
            double sh = GameConfig.KATANA_PADDLE_SPRITE_FRAME_HEIGHT;

            double drawWidth = paddle.width() * (288.0 / 266.0);
            double drawHeight = drawWidth * (48.0 / 288.0);
            double drawX = paddle.centerX() - drawWidth / 2.0;
            double drawY = paddle.y() - (17.0 / 48.0) * drawHeight;

            gc.save();
            DropShadow glow = world.parrySystem().isAvailable()
                    ? new DropShadow(18, Color.web("#27d8ff"))
                    : new DropShadow(12, Color.web("#ff4fc8"));
            gc.setEffect(glow);
            gc.drawImage(katanaPaddleSpriteSheet,
                    sx, sy, sw, sh,
                    drawX, drawY, drawWidth, drawHeight);
            gc.restore();
        } else {
            gc.setEffect(new DropShadow(16, Color.web("#27d8ff")));
            gc.setFill(new LinearGradient(paddle.left(), 0, paddle.right(), 0, false,
                    CycleMethod.NO_CYCLE, new Stop(0, Color.web("#497dff")), new Stop(0.5, Color.web("#6bf5ff")),
                    new Stop(1, Color.web("#497dff"))));
            gc.fillRoundRect(paddle.left(), paddle.y(), paddle.width(),
                    paddle.height(), 14, 14);
            gc.setEffect(null);
        }
    }

    private void drawBall(GraphicsContext gc, Ball ball, double energy) {
        double radius = ball.radius();
        double diameter = radius * 2;
        if (energy > 0) {
            double pulse = 0.96 + 0.04 * Math.sin(visualEffects.elapsedTime() * 15);
            double haloRadius = radius * 3.25 * pulse;
            gc.save();
            gc.setGlobalAlpha(energy);
            gc.setFill(ENERGY_HALO);
            gc.fillOval(ball.x() - haloRadius, ball.y() - haloRadius, haloRadius * 2, haloRadius * 2);
            gc.setEffect(energyBallGlow);
            gc.setFill(ENERGY_BALL);
            gc.fillOval(ball.x() - radius, ball.y() - radius, diameter, diameter);
            gc.setEffect(null);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.5);
            gc.strokeOval(ball.x() - radius, ball.y() - radius, diameter, diameter);
            gc.restore();
        }
        if (energy < 1) {
            gc.save();
            gc.setGlobalAlpha(1 - energy);
            gc.setEffect(ballGlow);
            gc.setFill(Color.WHITE);
            gc.fillOval(ball.x() - radius, ball.y() - radius, diameter, diameter);
            gc.setEffect(null);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.5);
            gc.strokeOval(ball.x() - radius, ball.y() - radius, diameter, diameter);
            gc.restore();
        }
    }

    private void drawStatus(GraphicsContext gc, GameWorld world) {
        String status = world.balls().stream().anyMatch(Ball::isPiercing) ? "PIERCING ACTIVE - ENDS AT NEXT WALL"
                : world.state() == GameState.READY ? "SPACE OR LEFT CLICK TO LAUNCH" : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(world.balls().stream().anyMatch(Ball::isPiercing) ? ENERGY_ORANGE : Color.web("#8393b2"));
        gc.fillText(status, GameConfig.WIDTH / 2, GameConfig.HEIGHT - 85);
    }

    private void drawFooter(GraphicsContext gc, GameWorld world, double musicVolume) {
        double sliderY = musicSliderY();
        double sliderLeft = musicSliderLeft();
        double sliderRight = musicSliderRight();
        double handleX = sliderLeft + (sliderRight - sliderLeft) * musicVolume;
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#9aaccc"));
        gc.fillText("BGM", 30, sliderY);
        gc.setStroke(Color.web("#34476a"));
        gc.setLineWidth(5);
        gc.strokeLine(sliderLeft, sliderY, sliderRight, sliderY);
        gc.setStroke(CYAN);
        gc.strokeLine(sliderLeft, sliderY, handleX, sliderY);
        gc.setFill(Color.WHITE);
        gc.fillOval(handleX - 6, sliderY - 6, 12, 12);
        gc.setFill(Color.web("#9aaccc"));
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText(Math.round(musicVolume * 100) + "%", sliderRight + 12, sliderY);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#f4cd75"));
        if (world.state() == GameState.MAIN_MENU || world.state() == GameState.HOW_TO_PLAY) {
            double highestScoreX = mainMenuLayout ? menuWidth - 30 : GameConfig.WIDTH - 30;
            gc.fillText("HIGHEST SCORE  " + world.scoreManager().highestScore(), highestScoreX, sliderY);
        }
    }

    private void drawReady(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 8, 18, 0.38));
        gc.fillRect(0, GameConfig.HUD_HEIGHT, GameConfig.WIDTH, GameConfig.HEIGHT - GameConfig.HUD_HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText("READY", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 - 18);
        gc.setFont(Font.font("Monospaced", 16));
        gc.setFill(CYAN);
        gc.fillText("PRESS SPACE OR CLICK TO LAUNCH", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 + 18);
    }

    private void drawPause(GraphicsContext gc) {
        drawOverlay(gc);
        drawPanel(gc, (GameConfig.WIDTH - 360) / 2, GameConfig.HEIGHT / 2 - 110, 360, 220);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        gc.fillText("PAUSED", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 - 65);
        drawButton(gc, "RESUME", PAUSE_BUTTON_X, PAUSE_RESUME_Y, 220, 48, MAGENTA);
        drawButton(gc, "MAIN MENU", PAUSE_BUTTON_X, PAUSE_MENU_Y, 220, 48, CYAN);
    }

    private void drawUpgradeSelection(GraphicsContext gc, GameWorld world) {
        drawOverlay(gc);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 34));
        gc.fillText("CHOOSE ONE UPGRADE", GameConfig.WIDTH / 2, UPGRADE_CARD_Y - 45);
        double cardWidth = UPGRADE_CARD_WIDTH;
        double gap = UPGRADE_CARD_GAP;
        double start = (GameConfig.WIDTH - (cardWidth * 3 + gap * 2)) / 2;
        for (int i = 0; i < world.upgradeChoices().size(); i++) {
            Upgrade upgrade = world.upgradeChoices().get(i);
            double x = start + i * (cardWidth + gap);
            drawPanel(gc, x, UPGRADE_CARD_Y, cardWidth, 230);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(i == 1 ? MAGENTA : CYAN);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));
            gc.fillText(upgrade.name(), x + cardWidth / 2, UPGRADE_CARD_Y + 50);
            gc.setFill(Color.web("#dae4fb"));
            gc.setFont(Font.font("Monospaced", 16));
            wrapText(gc, upgrade.description(), x + 24, UPGRADE_CARD_Y + 100, cardWidth - 48, 21);
            drawButton(gc, "SELECT  " + (i + 1), x + 35, UPGRADE_CARD_Y + 170, cardWidth - 70, 42,
                    i == 1 ? MAGENTA : CYAN);
        }
        gc.setFill(Color.web("#a7b5d1"));
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText("UPGRADES PERSIST FOR THE REST OF THIS RUN",
                GameConfig.WIDTH / 2, UPGRADE_CARD_Y + 285);
    }

    private void drawEndScreen(GraphicsContext gc, GameWorld world, boolean victory) {
        drawOverlay(gc);
        double panelY = GameConfig.HEIGHT / 2 - 170;
        drawPanel(gc, (GameConfig.WIDTH - 420) / 2, panelY, 420, 340);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(victory ? Color.web("#ffe17a") : Color.web("#ff718e"));
        gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 40));
        gc.fillText(victory ? "VICTORY!" : "GAME OVER", GameConfig.WIDTH / 2, panelY + 65);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 22));
        gc.fillText("FINAL SCORE  " + world.scoreManager().score(), GameConfig.WIDTH / 2, panelY + 130);
        gc.fillText("STAGE REACHED  " + world.stage(), GameConfig.WIDTH / 2, panelY + 165);
        drawButton(gc, "PLAY AGAIN", END_BUTTON_X, END_PLAY_AGAIN_Y, 230, 48, CYAN);
        drawButton(gc, "MAIN MENU", END_BUTTON_X, END_MENU_Y, 230, 44, MAGENTA);
    }

    private void drawCenterMessage(GraphicsContext gc, String title, String subtitle) {
        drawOverlay(gc);
        double panelY = GameConfig.HEIGHT / 2 - 72;
        drawPanel(gc, (GameConfig.WIDTH - 410) / 2, panelY, 410, 145);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 32));
        gc.fillText(title, GameConfig.WIDTH / 2, panelY + 45);
        gc.setFill(Color.web("#dce8ff"));
        gc.setFont(Font.font("Monospaced", 18));
        gc.fillText(subtitle, GameConfig.WIDTH / 2, panelY + 95);
    }

    private void drawButton(GraphicsContext gc, String label, double x, double y, double width, double height,
                            Color color) {
        gc.setStroke(color);
        gc.setLineWidth(1.7);
        gc.setFill(Color.rgb(19, 30, 52, 0.94));
        gc.fillRoundRect(x, y, width, height, 12, 12);
        gc.strokeRoundRect(x, y, width, height, 12, 12);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 17));
        gc.fillText(label, x + width / 2, y + height / 2 + 1);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.rgb(10, 17, 34, 0.97));
        gc.fillRoundRect(x, y, width, height, 18, 18);
        gc.setStroke(Color.web("#34476a"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, width, height, 18, 18);
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(Color.rgb(2, 5, 13, 0.76));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
    }

    private void wrapText(GraphicsContext gc, String text, double x, double y, double maxWidth, double lineHeight) {
        StringBuilder line = new StringBuilder();
        int lineNumber = 0;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (gc.getFont().getSize() * candidate.length() * 0.61 > maxWidth && !line.isEmpty()) {
                gc.fillText(line.toString(), x + maxWidth / 2, y + lineNumber++ * lineHeight);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            gc.fillText(line.toString(), x + maxWidth / 2, y + lineNumber * lineHeight);
        }
    }
}
