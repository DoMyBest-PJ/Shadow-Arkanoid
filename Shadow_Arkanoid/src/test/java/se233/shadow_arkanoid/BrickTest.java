package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.BrickType;
import se233.shadow_arkanoid.model.system.ScoreManager;

class BrickTest {
    @Test
    void strongBrickRequiresTwoHits() {
        Brick brick = new Brick(0, 0, 20, 10, BrickType.STRONG);
        brick.hit();
        assertEquals(1, brick.durability());
        assertFalse(brick.isDestroyed());
        assertEquals(GameConfig.BRICK_HIT_ANIMATION_SECONDS, brick.hitFlash());
        brick.update(GameConfig.BRICK_HIT_ANIMATION_SECONDS / 2);
        assertEquals(GameConfig.BRICK_HIT_ANIMATION_SECONDS / 2, brick.hitFlash(), 0.0001);
        brick.hit();
        assertTrue(brick.isDestroyed());
    }

    @Test
    void destructionPointsAreAwardedOnlyOnce() {
        Brick brick = new Brick(0, 0, 20, 10, BrickType.NORMAL);
        brick.hit();
        ScoreManager scores = new ScoreManager();
        assertEquals(GameConfig.BRICK_DESTROY_SCORE, scores.awardBrick(brick));
        assertEquals(0, scores.awardBrick(brick));
        assertEquals(GameConfig.BRICK_DESTROY_SCORE, scores.score());
    }
}
