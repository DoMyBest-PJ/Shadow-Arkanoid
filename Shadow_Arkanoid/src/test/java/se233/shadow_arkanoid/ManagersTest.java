package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.GameState;
import se233.shadow_arkanoid.model.GameWorld;
import se233.shadow_arkanoid.model.Paddle;
import se233.shadow_arkanoid.system.LifeManager;
import se233.shadow_arkanoid.system.ParrySystem;

class ManagersTest {
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
        ball.setPosition(world.paddle().centerX(), 550);
        ball.setVelocity(0, 88);
        assertTrue(world.attemptParry());
        assertEquals(GameConfig.PARRY_BONUS, world.scoreManager().score());
        assertTrue(ball.speed() > 88);
        assertTrue(ball.isPiercing());
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
        Ball ball = new Ball(paddle.centerX(), 550, 0, 88, GameConfig.BALL_RADIUS);
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
        assertEquals(2, world.lifeManager().lives());
        assertEquals(GameState.LIFE_LOST, world.state());
        world.update(0.05, 0);
        assertEquals(2, world.lifeManager().lives());
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
