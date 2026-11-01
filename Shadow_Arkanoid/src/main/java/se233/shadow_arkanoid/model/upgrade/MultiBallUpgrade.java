package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class MultiBallUpgrade implements Upgrade {
    @Override public String name() { return "DOUBLE-BALL"; }
    @Override public String description() { return "Split into an additional ball."; }
    @Override public void apply(GameWorld world) { world.addBall(); }
}
