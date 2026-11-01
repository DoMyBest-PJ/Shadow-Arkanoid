package se233.shadow_arkanoid;

import se233.shadow_arkanoid.exception.GameConfigurationException;

public final class GameConfig {
    public static final String TITLE = "SHADOW ARKANOID";
    public static final double WIDTH = FrameLayout.GAMEPLAY_WIDTH;
    public static final double HEIGHT = FrameLayout.GAMEPLAY_HEIGHT;
    public static final double WALL_MARGIN = 0;
    public static final double HUD_HEIGHT = 170;
    public static final double PADDLE_Y = HEIGHT - 190;
    public static final double PADDLE_WIDTH = 200;
    public static final double PADDLE_HEIGHT = 22;
    public static final double PADDLE_SPEED = 560;
    public static final double BALL_RADIUS = 14;
    public static final double BALL_INITIAL_SPEED = 800;
    public static final double BALL_MAX_SPEED = 2000;
    public static final double MIN_VERTICAL_SPEED_RATIO = 0.22;
    public static final int STARTING_LIVES = 99;
    public static final int PARRY_BONUS = 150;
    public static final double PARRY_SPEED_MULTIPLIER = 1.5;
    public static final double PARRY_COOLDOWN_SECONDS = 1.5;
    public static final double PARRY_WINDOW_SECONDS = 0.125;
    public static final int TOTAL_STAGES = 5;
    public static final int BRICK_DESTROY_SCORE = 100;
    public static final int STRONG_BRICK_SCORE = 200;
    public static final int BONUS_BRICK_SCORE = 300;
    public static final double BRICK_HIT_ANIMATION_SECONDS = 0.15;
    public static final double BRICK_BREAK_ANIMATION_SECONDS = 0.2;
    public static final double SPECIAL_SKILL_DROP_CHANCE = 0.10;
    public static final double SPECIAL_SKILL_DROP_SPEED = 280;
    public static final double SPECIAL_SKILL_DROP_SIZE = 42;
    public static final double SPECIAL_SKILL_SLASH_DURATION_SECONDS = 0.72;
    public static final int WIND_SLASH_SPRITE_COLUMNS = 7;
    public static final int WIND_SLASH_SPRITE_ROWS = 4;
    public static final int WIND_SLASH_SPRITE_FRAME_COUNT = 27;
    public static final int KATANA_PADDLE_SPRITE_COLUMNS = 4;
    public static final int KATANA_PADDLE_SPRITE_ROWS = 3;
    public static final int KATANA_PADDLE_SPRITE_FRAME_COUNT = 12;
    public static final double KATANA_PADDLE_SPRITE_FRAME_WIDTH = 288;
    public static final double KATANA_PADDLE_SPRITE_FRAME_HEIGHT = 48;
    public static final double KATANA_PADDLE_ANIMATION_CYCLE_SECONDS = 0.96;

    static {
        validate();
    }

    private static void validate() {
        if (WIDTH <= 0 || HEIGHT <= 0 || PADDLE_WIDTH <= 0 || PADDLE_HEIGHT <= 0
                || BALL_RADIUS <= 0 || BALL_INITIAL_SPEED <= 0 || BALL_MAX_SPEED < BALL_INITIAL_SPEED
                || PARRY_WINDOW_SECONDS <= 0 || PARRY_COOLDOWN_SECONDS < 0
                || STARTING_LIVES <= 0 || TOTAL_STAGES <= 0
                || SPECIAL_SKILL_DROP_CHANCE < 0 || SPECIAL_SKILL_DROP_CHANCE > 1
                || SPECIAL_SKILL_DROP_SPEED <= 0 || SPECIAL_SKILL_DROP_SIZE <= 0
                || SPECIAL_SKILL_SLASH_DURATION_SECONDS <= 0
                || KATANA_PADDLE_ANIMATION_CYCLE_SECONDS <= 0) {
            throw new GameConfigurationException("Game configuration contains invalid dimensions or gameplay values");
        }
    }
}
