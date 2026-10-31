package se233.shadow_arkanoid.model;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import se233.shadow_arkanoid.GameConfig;

public final class Ball {
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private final double radius;
    private boolean piercing;
    private boolean attached;
    private final Set<Brick> overlappingBricks = Collections.newSetFromMap(new IdentityHashMap<>());

    public Ball(double x, double y, double velocityX, double velocityY, double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Ball radius must be positive");
        }
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.radius = radius;
        this.attached = velocityX == 0 && velocityY == 0;
    }

    public void update(double deltaTime) {
        if (!attached) {
            x += velocityX * deltaTime;
            y += velocityY * deltaTime;
        }
    }

    public void launch() {
        launch(0.26);
    }

    public void launch(double horizontalRatio) {
        if (attached) {
            double ratio = Math.max(-0.75, Math.min(0.75, horizontalRatio));
            attached = false;
            velocityX = GameConfig.BALL_INITIAL_SPEED * ratio;
            velocityY = -GameConfig.BALL_INITIAL_SPEED * Math.sqrt(1 - ratio * ratio);
        }
    }

    public void attachAt(double centerX, double paddleY) {
        attached = true;
        x = centerX;
        y = paddleY - radius - 1;
        velocityX = 0;
        velocityY = 0;
        overlappingBricks.clear();
    }

    public void setVelocity(double x, double y) {
        velocityX = x;
        velocityY = y;
        if (Math.hypot(x, y) > 0) {
            attached = false;
        }
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void scaleSpeed(double multiplier, double maximumSpeed) {
        if (!Double.isFinite(multiplier) || multiplier <= 0 || maximumSpeed <= 0) {
            throw new IllegalArgumentException("Speed multiplier and maximum must be positive");
        }
        double speed = speed();
        if (speed == 0) {
            return;
        }
        double target = Math.min(maximumSpeed, speed * multiplier);
        velocityX = velocityX / speed * target;
        velocityY = velocityY / speed * target;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public void clearBrickOverlap(Brick brick) {
        overlappingBricks.remove(brick);
    }

    public Set<Brick> overlappingBricks() {
        return overlappingBricks;
    }

    public void markBrickOverlap(Brick brick) {
        overlappingBricks.add(brick);
    }

    public void clearBrickOverlaps() {
        overlappingBricks.clear();
    }

    public double x() { return x; }
    public double y() { return y; }
    public double velocityX() { return velocityX; }
    public double velocityY() { return velocityY; }
    public double radius() { return radius; }
    public boolean isPiercing() { return piercing; }
    public boolean isAttached() { return attached; }
    public double speed() { return Math.hypot(velocityX, velocityY); }
}
