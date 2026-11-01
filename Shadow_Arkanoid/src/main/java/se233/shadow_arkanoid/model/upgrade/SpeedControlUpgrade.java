package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class SpeedControlUpgrade implements Upgrade {
    @Override public String name() { return "QUICK MOVE"; }
    @Override public String description() { return "Move the paddle 20% faster."; }
    @Override public void apply(GameWorld world) { world.increasePaddleSpeed(1.2); }
}
