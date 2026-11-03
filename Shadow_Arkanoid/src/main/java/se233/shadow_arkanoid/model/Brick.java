package se233.shadow_arkanoid.model;

import javafx.geometry.Rectangle2D;
import se233.shadow_arkanoid.GameConfig;

public final class Brick {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final BrickType type;
    private int durability;
    private boolean destroyed;
    private boolean destructionAwarded;
    private double hitFlash;

    public Brick(double x, double y, double width, double height, BrickType type) {
        if (width <= 0 || height <= 0 || type == null) {
            throw new IllegalArgumentException("Brick dimensions and type must be valid");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.durability = type.durability();
    }

    // brick hit by the ball
    public void hit() {
        if (destroyed) {
            return;
        }
        durability--;
        hitFlash = GameConfig.BRICK_HIT_ANIMATION_SECONDS;
        if (durability <= 0) {
            destroyed = true;
        }
    }

    // destroy brick immedietely when hit by slash power
    public boolean destroy() {
        if (destroyed) {
            return false;
        }
        durability = 0;
        destroyed = true;
        return true;
    }

    public boolean markDestructionAwarded() {
        if (!destroyed || destructionAwarded) {
            return false;
        }
        destructionAwarded = true;
        return true;
    }

    public void update(double deltaTime) {
        hitFlash = Math.max(0, hitFlash - deltaTime);
    }


    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }
    public BrickType type() { return type; }
    public int durability() { return durability; }
    public boolean isDestroyed() { return destroyed; }
    public double hitFlash() { return hitFlash; }
}
