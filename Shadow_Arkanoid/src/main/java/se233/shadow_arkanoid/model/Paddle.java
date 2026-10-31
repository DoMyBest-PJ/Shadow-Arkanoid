package se233.shadow_arkanoid.model;

import se233.shadow_arkanoid.GameConfig;

public final class Paddle {
    private double centerX = GameConfig.WIDTH / 2;
    private double width = GameConfig.PADDLE_WIDTH;
    private double movementSpeed = GameConfig.PADDLE_SPEED;

    public void move(double direction, double deltaTime) {
        centerX = clamp(centerX + direction * movementSpeed * deltaTime);
    }

    public void moveTo(double x) {
        centerX = clamp(x);
    }

    public void setWidthMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Paddle width multiplier must be positive");
        }
        width = GameConfig.PADDLE_WIDTH * multiplier;
        centerX = clamp(centerX);
    }

    public void setMovementSpeedMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            throw new IllegalArgumentException("Movement speed multiplier must be positive");
        }
        movementSpeed = GameConfig.PADDLE_SPEED * multiplier;
    }

    public void reset() {
        centerX = GameConfig.WIDTH / 2;
        width = GameConfig.PADDLE_WIDTH;
        movementSpeed = GameConfig.PADDLE_SPEED;
    }

    public double left() { return centerX - width / 2; }
    public double right() { return centerX + width / 2; }
    public double centerX() { return centerX; }
    public double y() { return GameConfig.PADDLE_Y; }
    public double width() { return width; }
    public double height() { return GameConfig.PADDLE_HEIGHT; }
    public double movementSpeed() { return movementSpeed; }

    private double clamp(double x) {
        return Math.max(GameConfig.WALL_MARGIN + width / 2,
                Math.min(GameConfig.WIDTH - GameConfig.WALL_MARGIN - width / 2, x));
    }
}
