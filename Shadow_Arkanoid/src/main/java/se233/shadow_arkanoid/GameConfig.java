package se233.shadow_arkanoid;

import se233.shadow_arkanoid.exception.GameConfigurationException;

public final class GameConfig {
    public static final String TITLE = "SHADOW ARKANOID";
    public static final double WIDTH = 960;
    public static final double HEIGHT = 640;
    public static final double WALL_MARGIN = 28;
    public static final double HUD_HEIGHT = 62;
    public static final double PADDLE_Y = 570;
    public static final double PADDLE_WIDTH = 112;
    public static final double PADDLE_HEIGHT = 16;
    public static final double PADDLE_SPEED = 560;
    public static final double BALL_RADIUS = 9;
    public static final double BALL_INITIAL_SPEED = 450;
    public static final double BALL_MAX_SPEED = 760;
    public static final double MIN_VERTICAL_SPEED_RATIO = 0.22;
    public static final int STARTING_LIVES = 3;
    public static final int PARRY_BONUS = 100;
    public static final double PARRY_SPEED_MULTIPLIER = 1.25;
    public static final double PARRY_COOLDOWN_SECONDS = 1.5;
    public static final double PARRY_WINDOW_SECONDS = 0.125;
    public static final int TOTAL_STAGES = 5;
    public static final int BRICK_DESTROY_SCORE = 100;
    public static final int STRONG_BRICK_SCORE = 180;
    public static final int BONUS_BRICK_SCORE = 250;

    static {
        validate();
    }

    private static void validate() {
        if (WIDTH <= 0 || HEIGHT <= 0 || PADDLE_WIDTH <= 0 || PADDLE_HEIGHT <= 0
                || BALL_RADIUS <= 0 || BALL_INITIAL_SPEED <= 0 || BALL_MAX_SPEED < BALL_INITIAL_SPEED
                || PARRY_WINDOW_SECONDS <= 0 || PARRY_COOLDOWN_SECONDS < 0
                || STARTING_LIVES <= 0 || TOTAL_STAGES <= 0) {
            throw new GameConfigurationException("Game configuration contains invalid dimensions or gameplay values");
        }
    }
}
