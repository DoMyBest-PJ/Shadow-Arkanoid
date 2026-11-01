package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class ExtraLifeUpgrade implements Upgrade {
    @Override public String name() { return "EXTRA LIFE!"; }
    @Override public String description() { return "Gain one extra life."; }
    @Override public void apply(GameWorld world) { world.addLife(); }
}
