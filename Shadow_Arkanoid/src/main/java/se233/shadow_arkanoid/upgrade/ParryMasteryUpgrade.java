package se233.shadow_arkanoid.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class ParryMasteryUpgrade implements Upgrade {
    @Override public String name() { return "PARRY MASTERY"; }
    @Override public String description() { return "Add 40 ms to your Parry timing window."; }
    @Override public void apply(GameWorld world) { world.improveParryWindow(0.04); }
}
