package se233.shadow_arkanoid.model.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.shadow_arkanoid.model.Brick;

public final class ScoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreManager.class);
    private int score;
    private int highestScore;
    private double multiplier = 1;

    public int awardBrick(Brick brick) {
        if (!brick.markDestructionAwarded()) {
            return 0;
        }
        int points = (int) Math.round(brick.type().points() * multiplier);
        add(points);
        return points;
    }

    public void add(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Score award cannot be negative");
        }
        score += points;
        if (score > highestScore) {
            highestScore = score;
        }
        LOGGER.debug("Score increased by {} to {}", points, score);
    }

    public void setMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier < 1) {
            throw new IllegalArgumentException("Score multiplier must be at least one");
        }
        this.multiplier = multiplier;
    }

    public int score() { return score; }
    public int highestScore() { return highestScore; }
    public double multiplier() { return multiplier; }

    public void reset() {
        score = 0;
        multiplier = 1;
    }
}
