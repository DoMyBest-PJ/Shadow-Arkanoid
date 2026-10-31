package se233.shadow_arkanoid.model;

import se233.shadow_arkanoid.GameConfig;

public enum BrickType {
    NORMAL(1, GameConfig.BRICK_DESTROY_SCORE),
    STRONG(2, GameConfig.STRONG_BRICK_SCORE),
    BONUS(1, GameConfig.BONUS_BRICK_SCORE);

    private final int durability;
    private final int points;

    BrickType(int durability, int points) {
        this.durability = durability;
        this.points = points;
    }

    public int durability() {
        return durability;
    }

    public int points() {
        return points;
    }
}
