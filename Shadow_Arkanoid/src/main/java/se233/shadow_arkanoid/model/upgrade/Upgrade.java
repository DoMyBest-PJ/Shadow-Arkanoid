package se233.shadow_arkanoid.model.upgrade;

import se233.shadow_arkanoid.model.GameWorld;

public interface Upgrade {
    String name();
    String description();
    void apply(GameWorld world);
}
