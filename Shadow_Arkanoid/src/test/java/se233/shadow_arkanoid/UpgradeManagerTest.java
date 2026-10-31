package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Random;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.upgrade.UpgradeManager;

class UpgradeManagerTest {
    @Test
    void choicesAreDistinctAndLimitedToAvailableOptions() {
        UpgradeManager manager = new UpgradeManager(new Random(5));
        var choices = manager.chooseThree();
        assertEquals(3, choices.size());
        assertEquals(3, new HashSet<>(choices.stream().map(upgrade -> upgrade.name()).toList()).size());
        assertEquals(6, manager.choose(50).size());
        assertTrue(manager.choose(0).isEmpty());
    }
}
