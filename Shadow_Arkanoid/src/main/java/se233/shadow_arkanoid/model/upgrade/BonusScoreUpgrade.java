package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public final class BonusScoreUpgrade implements Upgrade {
    @Override public String name() { return "GOLDEN TOUCH"; }
    @Override public String description() { return "Brick score increased by 50%."; }
    @Override public void apply(GameWorld world) { world.increaseScoreMultiplier(0.5); }
}
