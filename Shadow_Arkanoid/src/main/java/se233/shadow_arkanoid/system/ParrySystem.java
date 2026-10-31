package se233.shadow_arkanoid.system;

import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.Paddle;

public final class ParrySystem {
    private double cooldownRemaining;
    private double timingWindow = GameConfig.PARRY_WINDOW_SECONDS;

    public boolean attempt(Ball ball, Paddle paddle) {
        if (cooldownRemaining > 0 || ball.isAttached() || ball.velocityY() <= 0) {
            return false;
        }
        double collisionY = paddle.y() - ball.radius();
        double timeToPaddle = (collisionY - ball.y()) / ball.velocityY();
        if (timeToPaddle < 0 || timeToPaddle > timingWindow) {
            return false;
        }
        double predictedX = reflectedX(ball.x() + ball.velocityX() * timeToPaddle, ball.radius());
        double paddlePrediction = paddle.centerX();
        if (Math.abs(predictedX - paddlePrediction) > paddle.width() / 2 + ball.radius()) {
            return false;
        }
        ball.scaleSpeed(GameConfig.PARRY_SPEED_MULTIPLIER, GameConfig.BALL_MAX_SPEED);
        ball.setPiercing(true);
        cooldownRemaining = GameConfig.PARRY_COOLDOWN_SECONDS;
        return true;
    }

    public void update(double deltaTime) {
        cooldownRemaining = Math.max(0, cooldownRemaining - deltaTime);
    }

    public void improveTimingWindow(double seconds) {
        if (!Double.isFinite(seconds) || seconds <= 0) {
            throw new IllegalArgumentException("Timing window increase must be positive");
        }
        timingWindow += seconds;
    }

    public void reset() {
        cooldownRemaining = 0;
        timingWindow = GameConfig.PARRY_WINDOW_SECONDS;
    }

    public boolean isAvailable() { return cooldownRemaining <= 0; }
    public double cooldownRemaining() { return cooldownRemaining; }
    public double cooldownFraction() {
        return 1 - cooldownRemaining / GameConfig.PARRY_COOLDOWN_SECONDS;
    }
    public double timingWindow() { return timingWindow; }

    private double reflectedX(double x, double radius) {
        double left = GameConfig.WALL_MARGIN + radius;
        double right = GameConfig.WIDTH - GameConfig.WALL_MARGIN - radius;
        double range = right - left;
        double folded = (x - left) % (2 * range);
        if (folded < 0) {
            folded += 2 * range;
        }
        return folded <= range ? left + folded : right - (folded - range);
    }
}
