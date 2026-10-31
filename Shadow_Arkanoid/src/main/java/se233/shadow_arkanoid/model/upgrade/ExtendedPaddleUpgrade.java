package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class ExtendedPaddleUpgrade implements Upgrade {
    @Override public String name() { return "WIDE ARC"; }
    @Override public String description() { return "Increase paddle width by 25%."; }
    @Override public void apply(GameWorld world) { world.increasePaddleWidth(1.25); }
}
