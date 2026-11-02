package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.GameState;
import se233.shadow_arkanoid.model.GameWorld;
import se233.shadow_arkanoid.model.Paddle;
import se233.shadow_arkanoid.model.SpecialSkillDrop;
import se233.shadow_arkanoid.model.system.LifeManager;
import se233.shadow_arkanoid.model.system.ParrySystem;

class ManagersTest {
    @Test
    void specialSkillDropChanceIsTenPercent() {
        assertEquals(0.10, GameConfig.SPECIAL_SKILL_DROP_CHANCE);
    }

    @Test
    void specialSkillClearsOnlyItsVerticalBrickColumn() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        Brick firstBrick = world.bricks().get(0);
        double attackX = firstBrick.x() + firstBrick.width() / 2;
        var targetColumn = world.bricks().stream()
                .filter(brick -> attackX >= brick.x() && attackX < brick.x() + brick.width())
                .toList();
        int expectedScore = targetColumn.stream().mapToInt(brick -> brick.type().points()).sum();
        int expectedBrickCount = world.bricks().size() - targetColumn.size();

        world.activateSpecialSkill(attackX);

        assertFalse(targetColumn.isEmpty());
        assertTrue(targetColumn.stream().allMatch(Brick::isDestroyed));
        assertEquals(expectedBrickCount, world.bricks().size());
        assertEquals(expectedScore, world.scoreManager().score());
        assertEquals(GameWorld.VisualCue.Type.SPECIAL_SKILL_SLASH,
                world.drainVisualCues().get(0).type());
        assertTrue(world.drainEvents().contains("WIND_SLASH"));
    }

    @Test
    void specialSkillDropCanBeCaughtByPaddle() {
        Paddle paddle = new Paddle();
        paddle.moveTo(300);
        SpecialSkillDrop drop = new SpecialSkillDrop(300,
                paddle.y() - GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 - 14);

        drop.update(0.05);

        assertTrue(drop.isCollectedBy(paddle));
    }

    @Test
    void lifeLossIsBoundedAndGameOverActivatesAtZero() {
        LifeManager lives = new LifeManager(1);
        assertTrue(lives.loseLife());
        assertTrue(lives.isGameOver());
        assertFalse(lives.loseLife());
        assertEquals(0, lives.lives());
    }

    @Test
    void perfectParryAwardsPointsBoostsSpeedAndActivatesPiercing() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        world.launch();
        Ball ball = world.balls().get(0);
        ball.setPosition(world.paddle().centerX(), world.paddle().y() - ball.radius() - 8);
        ball.setVelocity(0, 88);
        assertTrue(world.attemptParry());
        assertEquals(GameConfig.PARRY_BONUS, world.scoreManager().score());
        assertTrue(ball.speed() > 88);
        assertTrue(ball.isPiercing());
    }

    @Test
    void perfectParryEmitsImpactAtPredictedPaddleContact() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        world.launch();
        Ball ball = world.balls().get(0);
        ball.setPosition(world.paddle().centerX() - 6, world.paddle().y() - ball.radius() - 8);
        ball.setVelocity(48, 88);
        double timeToPaddle = (world.paddle().y() - ball.radius() - ball.y()) / ball.velocityY();
        double expectedImpactX = ball.x() + ball.velocityX() * timeToPaddle;

        assertTrue(world.attemptParry());

        GameWorld.VisualCue cue = world.drainVisualCues().get(0);
        assertEquals(GameWorld.VisualCue.Type.PERFECT_PARRY, cue.type());
        assertEquals(expectedImpactX, cue.x(), 0.0001);
        assertEquals(world.paddle().y() - ball.radius(), cue.y(), 0.0001);
    }

    @Test
    void piercingWallHitEmitsDissipationAtBoundaryContact() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        world.launch();
        Ball ball = world.balls().get(0);
        ball.setPiercing(true);
        ball.setPosition(GameConfig.WALL_MARGIN + ball.radius() + 1, 200);
        ball.setVelocity(-100, 0);

        world.update(0.1, 0);

        GameWorld.VisualCue cue = world.drainVisualCues().get(0);
        assertEquals(GameWorld.VisualCue.Type.PIERCING_DISSIPATED, cue.type());
        assertEquals(GameConfig.WALL_MARGIN + ball.radius(), cue.x(), 0.0001);
        assertEquals(200, cue.y(), 0.0001);
        assertFalse(ball.isPiercing());
    }

    @Test
    void failedParryDoesNotAwardPointsOrActivatePiercing() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        world.launch();
        Ball ball = world.balls().get(0);
        ball.setPosition(world.paddle().centerX(), 500);
        ball.setVelocity(0, -150);
        double speed = ball.speed();
        assertFalse(world.attemptParry());
        assertEquals(0, world.scoreManager().score());
        assertEquals(speed, ball.speed(), 0.0001);
        assertFalse(ball.isPiercing());
    }

    @Test
    void cooldownPreventsRepeatedParryAttempts() {
        ParrySystem parry = new ParrySystem();
        Paddle paddle = new Paddle();
        Ball ball = new Ball(paddle.centerX(), paddle.y() - GameConfig.BALL_RADIUS - 8,
                0, 88, GameConfig.BALL_RADIUS);
        assertTrue(parry.attempt(ball, paddle));
        assertFalse(parry.attempt(ball, paddle));
    }

    @Test
    void aMissedBallCostsOnlyOneLifeWhileLifeLostStateIsActive() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        world.launch();
        Ball ball = world.balls().get(0);
        ball.setPosition(world.paddle().centerX(), GameConfig.HEIGHT + 20);
        ball.setVelocity(0, 100);
        world.update(0.01, 0);
        assertEquals(GameConfig.STARTING_LIVES - 1, world.lifeManager().lives());
        assertEquals(GameState.LIFE_LOST, world.state());
        world.update(0.05, 0);
        assertEquals(GameConfig.STARTING_LIVES - 1, world.lifeManager().lives());
    }

    @Test
    void gameOverActivatesAfterTheLastLifeIsLost() {
        GameWorld world = new GameWorld();
        world.startNewGame();
        for (int loss = 0; loss < GameConfig.STARTING_LIVES; loss++) {
            world.launch();
            Ball ball = world.balls().get(0);
            ball.setPosition(world.paddle().centerX(), GameConfig.HEIGHT + 20);
            ball.setVelocity(0, 100);
            world.update(0.01, 0);
            for (int frame = 0; frame < 24; frame++) {
                world.update(0.05, 0);
            }
        }
        assertEquals(0, world.lifeManager().lives());
        assertEquals(GameState.GAME_OVER, world.state());
    }
}
