package se233.shadow_arkanoid.view;

import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
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
import se233.shadow_arkanoid.view.effects.VisualEffectManager;

public final class GameRenderer {
    private static final double MENU_WIDTH = 960;
    private static final double MENU_HEIGHT = 640;
    public static final double BGM_SLIDER_LEFT = 82;
    public static final double BGM_SLIDER_RIGHT = 202;
    public static final double BGM_SLIDER_Y = GameConfig.HEIGHT - 34;
    public static final double MENU_BUTTON_WIDTH = 200;
    public static final double MENU_BUTTON_HEIGHT = 52;
    public static final double MENU_BUTTON_X = (MENU_WIDTH - MENU_BUTTON_WIDTH) / 2;
    public static final double MENU_START_Y = 310;
    public static final double MENU_HOW_TO_PLAY_Y = 382;
    public static final double MENU_EXIT_Y = 454;
    public static final double HOW_TO_PLAY_BACK_X = (GameConfig.WIDTH - 180) / 2;
    public static final double HOW_TO_PLAY_BACK_Y = 900;
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
    private final VisualEffectManager visualEffects = new VisualEffectManager();
    private double scale = 1;
    private double offsetX;
    private double offsetY;
    private boolean mainMenuLayout;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(GameWorld world, double musicVolume) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        mainMenuLayout = world.state() == GameState.MAIN_MENU;
        double logicalWidth = mainMenuLayout ? MENU_WIDTH : GameConfig.WIDTH;
        double logicalHeight = mainMenuLayout ? MENU_HEIGHT : GameConfig.HEIGHT;
        scale = Math.min(canvasWidth / logicalWidth, canvasHeight / logicalHeight);
        offsetX = (canvasWidth - logicalWidth * scale) / 2;
        offsetY = (canvasHeight - logicalHeight * scale) / 2;
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);
        drawBackdrop(gc);

        if (world.state() == GameState.MAIN_MENU) {
            drawMenu(gc);
        } else if (world.state() == GameState.HOW_TO_PLAY) {
            drawHowToPlay(gc);
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
        return mainMenuLayout ? 604 : BGM_SLIDER_Y;
    }

    private double logicalWidth() {
        return mainMenuLayout ? MENU_WIDTH : GameConfig.WIDTH;
    }

    public void updateEffects(double deltaTime, List<GameWorld.VisualCue> cues, List<Ball> balls) {
        visualEffects.update(deltaTime, cues, balls);
    }

    private void drawBackdrop(GraphicsContext gc) {
        gc.setFill(new LinearGradient(0, 0, 0, GameConfig.HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#101a32")), new Stop(1, BACKGROUND)));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
        gc.setStroke(Color.web("#182443"));
        gc.setLineWidth(1);
        for (int x = 28; x < GameConfig.WIDTH; x += 48) {
            gc.strokeLine(x, 0, x, GameConfig.HEIGHT);
        }
        for (int y = 16; y < GameConfig.HEIGHT; y += 48) {
            gc.strokeLine(0, y, GameConfig.WIDTH, y);
        }
    }

    private void drawMenu(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setEffect(new DropShadow(30, Color.web("#29cfff")));
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 43));
        gc.fillText("ARKANOID", logicalWidth() / 2, 175);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 25));
        gc.fillText("SHADOW-TWICE", logicalWidth() / 2, 222);
        gc.setEffect(null);
        drawButton(gc, "START GAME", MENU_BUTTON_X, MENU_START_Y,
                MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT, CYAN);
        drawButton(gc, "HOW TO PLAY", MENU_BUTTON_X, MENU_HOW_TO_PLAY_Y,
                MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT, MAGENTA);
        drawButton(gc, "EXIT", MENU_BUTTON_X, MENU_EXIT_Y,
                MENU_BUTTON_WIDTH, 48, Color.web("#65708c"));
        gc.setFill(Color.web("#93a4c4"));
        gc.setFont(Font.font("Monospaced", 13));
        gc.fillText("BREAK THE ARK. MASTER THE PARRY. SURVIVE THE RUN.", logicalWidth() / 2, 550);
    }

    private void drawHowToPlay(GraphicsContext gc) {
        drawPanel(gc, 90, 245, GameConfig.WIDTH - 180, 700);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.fillText("HOW TO PLAY", GameConfig.WIDTH / 2, 295);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web("#dce8ff"));
        gc.setFont(Font.font("Monospaced", 16));
        String[] lines = {
                "KEYBOARD ONLY      Move the paddle",
                "A / LEFT ARROW     Move left",
                "D / RIGHT ARROW    Move right",
                "SPACE              Attempt to Parry",
                "ESC                Open menu",
                "",
                "Break every brick to clear a stage and choose an upgrade.",
                "Strong bricks take more hit. Bonus bricks award extra points.",
                "Parry just before a descending ball reaches your paddle.",
                "A Perfect Parry grants +100 points and a 1.25x speed boost.",
                "The faster ball pierces every brick until it hits a top,",
                "left, or right wall. Brick hits never end piercing.",
                "You have three lives. Press ESC or P to pause."
        };
        double y = 355;
        for (String line : lines) {
            gc.fillText(line, 125, y);
            y += 35;
        }
        drawButton(gc, "BACK", HOW_TO_PLAY_BACK_X, HOW_TO_PLAY_BACK_Y, 180, 44, MAGENTA);
    }

    private void drawGame(GraphicsContext gc, GameWorld world) {
        drawHud(gc, world);
        for (Brick brick : world.bricks()) {
            drawBrick(gc, brick);
        }
        drawPaddle(gc, world);
        visualEffects.renderTrails(gc);
        for (Ball ball : world.balls()) {
            drawBall(gc, ball, visualEffects.energyFor(ball));
        }
        if (!world.message().isEmpty()) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 30));
            gc.setFill(world.message().contains("PARRY") ? Color.web("#fff28a") : Color.WHITE);
            gc.setEffect(new DropShadow(15, CYAN));
            gc.fillText(world.message(), GameConfig.WIDTH / 2, GameConfig.HEIGHT * 0.48);
            gc.setEffect(null);
        }
        drawStatus(gc, world);
        visualEffects.renderEffects(gc);
    }

    private void drawHud(GraphicsContext gc, GameWorld world) {
        gc.setFill(Color.web("#10172b"));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HUD_HEIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 17));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE  " + world.scoreManager().score(), 30, 25);
        gc.setFill(CYAN);
        gc.fillText("STAGE  " + world.stage(), 30, 48);
        gc.setFill(Color.web("#ff718e"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("LIVES  " + world.lifeManager().lives(), GameConfig.WIDTH / 2, 25);
        gc.setFill(Color.web("#9aaccc"));
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText("A / D  MOVE     SPACE  PARRY", GameConfig.WIDTH / 2, 48);
        drawParryMeter(gc, world);
        if (!world.activeUpgrades().isEmpty()) {
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(Color.web("#f4cd75"));
            gc.setFont(Font.font("Monospaced", 11));
            String upgrades = String.join("  ", world.activeUpgrades());
            gc.fillText("UPGRADES: " + upgrades, GameConfig.WIDTH - 27, 83);
        }
    }

    private void drawParryMeter(GraphicsContext gc, GameWorld world) {
        double x = GameConfig.WIDTH - 220;
        double y = 17;
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(world.parrySystem().isAvailable() ? CYAN : Color.web("#7d88a3"));
        gc.fillText(world.parrySystem().isAvailable() ? "PARRY READY" : "PARRY CHARGING", x, y);
        gc.setFill(Color.web("#26324c"));
        gc.fillRoundRect(x, y + 9, 180, 7, 7, 7);
        gc.setFill(world.parrySystem().isAvailable() ? CYAN : MAGENTA);
        gc.fillRoundRect(x, y + 9, 180 * Math.max(0, Math.min(1, world.parrySystem().cooldownFraction())),
                7, 7, 7);
    }

    private void drawBrick(GraphicsContext gc, Brick brick) {
        Color color = switch (brick.type()) {
            case NORMAL -> Color.web("#42bfff");
            case STRONG -> brick.durability() == 2 ? Color.web("#b46bff") : Color.web("#e0a1ff");
            case BONUS -> Color.web("#ffc34f");
        };
        if (brick.hitFlash() > 0) color = Color.WHITE;
        gc.setFill(color.deriveColor(0, 0.78, 0.72, 1));
        gc.fillRoundRect(brick.x(), brick.y(), brick.width(), brick.height(), 9, 9);
        gc.setFill(color);
        gc.fillRoundRect(brick.x() + 2, brick.y() + 2, brick.width() - 4, brick.height() - 5, 7, 7);
        gc.setFill(Color.rgb(255, 255, 255, 0.35));
        gc.fillRoundRect(brick.x() + 5, brick.y() + 4, brick.width() - 10, 4, 4, 4);
        if (brick.type() == BrickType.STRONG) {
            gc.setFill(Color.web("#201537"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(Integer.toString(brick.durability()), brick.x() + brick.width() / 2,
                    brick.y() + brick.height() / 2 + 2);
        } else if (brick.type() == BrickType.BONUS) {
            gc.setFill(Color.web("#462d00"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("+", brick.x() + brick.width() / 2, brick.y() + brick.height() / 2 + 1);
        }
    }

    private void drawPaddle(GraphicsContext gc, GameWorld world) {
        gc.setEffect(new DropShadow(16, Color.web("#27d8ff")));
        gc.setFill(new LinearGradient(world.paddle().left(), 0, world.paddle().right(), 0, false,
                CycleMethod.NO_CYCLE, new Stop(0, Color.web("#497dff")), new Stop(0.5, Color.web("#6bf5ff")),
                new Stop(1, Color.web("#497dff"))));
        gc.fillRoundRect(world.paddle().left(), world.paddle().y(), world.paddle().width(),
                world.paddle().height(), 14, 14);
        gc.setEffect(null);
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
            gc.restore();
        }
        if (energy < 1) {
            gc.save();
            gc.setGlobalAlpha(1 - energy);
            gc.setEffect(ballGlow);
            gc.setFill(Color.WHITE);
            gc.fillOval(ball.x() - radius, ball.y() - radius, diameter, diameter);
            gc.setEffect(null);
            gc.restore();
        }
    }

    private void drawStatus(GraphicsContext gc, GameWorld world) {
        String status = world.balls().stream().anyMatch(Ball::isPiercing) ? "PIERCING ACTIVE - ENDS AT NEXT WALL"
                : world.state() == GameState.READY ? "SPACE OR LEFT CLICK TO LAUNCH" : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(world.balls().stream().anyMatch(Ball::isPiercing) ? ENERGY_ORANGE : Color.web("#8393b2"));
        gc.fillText(status, GameConfig.WIDTH / 2, GameConfig.HEIGHT - 65);
    }

    private void drawFooter(GraphicsContext gc, GameWorld world, double musicVolume) {
        double sliderY = musicSliderY();
        double handleX = BGM_SLIDER_LEFT + (BGM_SLIDER_RIGHT - BGM_SLIDER_LEFT) * musicVolume;
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#9aaccc"));
        gc.fillText("BGM", 30, sliderY);
        gc.setStroke(Color.web("#34476a"));
        gc.setLineWidth(5);
        gc.strokeLine(BGM_SLIDER_LEFT, sliderY, BGM_SLIDER_RIGHT, sliderY);
        gc.setStroke(CYAN);
        gc.strokeLine(BGM_SLIDER_LEFT, sliderY, handleX, sliderY);
        gc.setFill(Color.WHITE);
        gc.fillOval(handleX - 6, sliderY - 6, 12, 12);
        gc.setFill(Color.web("#9aaccc"));
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText(Math.round(musicVolume * 100) + "%", BGM_SLIDER_RIGHT + 12, sliderY);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#f4cd75"));
        gc.fillText("HIGHEST SCORE  " + world.scoreManager().highestScore(), logicalWidth() - 30, sliderY);
    }

    private void drawReady(GraphicsContext gc) {
        gc.setFill(Color.rgb(5, 8, 18, 0.38));
        gc.fillRect(0, GameConfig.HUD_HEIGHT, GameConfig.WIDTH, GameConfig.HEIGHT - GameConfig.HUD_HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 21));
        gc.setFill(Color.WHITE);
        gc.fillText("READY", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 - 18);
        gc.setFont(Font.font("Monospaced", 13));
        gc.setFill(CYAN);
        gc.fillText("PRESS SPACE OR CLICK TO LAUNCH", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 + 18);
    }

    private void drawPause(GraphicsContext gc) {
        drawOverlay(gc);
        drawPanel(gc, (GameConfig.WIDTH - 360) / 2, GameConfig.HEIGHT / 2 - 110, 360, 220);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 31));
        gc.fillText("PAUSED", GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2 - 65);
        drawButton(gc, "RESUME", PAUSE_BUTTON_X, PAUSE_RESUME_Y, 220, 48, MAGENTA);
        drawButton(gc, "MAIN MENU", PAUSE_BUTTON_X, PAUSE_MENU_Y, 220, 48, CYAN);
    }

    private void drawUpgradeSelection(GraphicsContext gc, GameWorld world) {
        drawOverlay(gc);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
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
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
            gc.fillText(upgrade.name(), x + cardWidth / 2, UPGRADE_CARD_Y + 50);
            gc.setFill(Color.web("#dae4fb"));
            gc.setFont(Font.font("Monospaced", 13));
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
        gc.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 34));
        gc.fillText(victory ? "VICTORY!" : "GAME OVER", GameConfig.WIDTH / 2, panelY + 65);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 18));
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
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 27));
        gc.fillText(title, GameConfig.WIDTH / 2, panelY + 45);
        gc.setFill(Color.web("#dce8ff"));
        gc.setFont(Font.font("Monospaced", 14));
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
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
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
