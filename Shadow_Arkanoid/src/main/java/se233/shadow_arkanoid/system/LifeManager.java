package se233.shadow_arkanoid.system;

public final class LifeManager {
    private int lives;

    public LifeManager(int startingLives) {
        if (startingLives <= 0) {
            throw new IllegalArgumentException("Starting lives must be positive");
        }
        lives = startingLives;
    }

    public boolean loseLife() {
        if (lives == 0) {
            return false;
        }
        lives--;
        return true;
    }

    public void addLife() {
        lives++;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public int lives() { return lives; }

    public void reset(int startingLives) {
        if (startingLives <= 0) {
            throw new IllegalArgumentException("Starting lives must be positive");
        }
        lives = startingLives;
    }
}
