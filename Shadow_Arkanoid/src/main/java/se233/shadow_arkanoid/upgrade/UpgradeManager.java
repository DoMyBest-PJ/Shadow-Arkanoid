package se233.shadow_arkanoid.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class UpgradeManager {
    private final List<Upgrade> upgrades;
    private final Random random;

    public UpgradeManager() {
        this(new Random());
    }

    public UpgradeManager(Random random) {
        this.random = random;
        upgrades = List.of(new ExtendedPaddleUpgrade(), new ExtraLifeUpgrade(), new MultiBallUpgrade(),
                new BonusScoreUpgrade(), new SpeedControlUpgrade(), new ParryMasteryUpgrade());
    }

    public List<Upgrade> choose(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Choice count cannot be negative");
        }
        List<Upgrade> shuffled = new ArrayList<>(upgrades);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(count, shuffled.size())));
    }

    public List<Upgrade> chooseThree() {
        return choose(3);
    }
}
