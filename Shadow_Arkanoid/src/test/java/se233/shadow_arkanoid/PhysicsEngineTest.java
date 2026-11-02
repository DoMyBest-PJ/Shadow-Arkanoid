package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.BrickType;
import se233.shadow_arkanoid.model.Paddle;
import se233.shadow_arkanoid.model.physics.PhysicsEngine;

class PhysicsEngineTest {
    private final PhysicsEngine physics = new PhysicsEngine();
    private final Paddle paddle = new Paddle();

    @Test
    void ballReflectsFromLeftAndTopWalls() {
        Ball left = new Ball(7, 200, -100, 0, 5);
        physics.update(left, paddle, List.of(), 0.1);
        assertTrue(left.velocityX() > 0);

        Ball top = new Ball(200, 7, 0, -100, 5);
        physics.update(top, paddle, List.of(), 0.1);
        assertTrue(top.velocityY() > 0);
    }

    @Test
    void ballCannotEnterHudArea() {
        Ball ball = new Ball(200, GameConfig.HUD_HEIGHT + GameConfig.BALL_RADIUS + 1,
                0, -100, GameConfig.BALL_RADIUS);

        physics.update(ball, paddle, List.of(), 0.1);

        assertTrue(ball.y() >= GameConfig.HUD_HEIGHT + ball.radius());
        assertTrue(ball.velocityY() > 0);
    }

    @Test
    void ballReflectsFromRightWall() {
        Ball ball = new Ball(GameConfig.WIDTH - 7, 200, 100, 0, 5);
        physics.update(ball, paddle, List.of(), 0.1);
        assertTrue(ball.velocityX() < 0);
    }

    @Test
    void paddleBounceAlwaysHasAnUpwardComponent() {
        Ball ball = new Ball(paddle.centerX(), paddle.y() - 9, 0, 300, GameConfig.BALL_RADIUS);
        PhysicsEngine.resolvePaddleCollision(ball, paddle);
        assertTrue(ball.velocityY() < 0);
        assertTrue(ball.speed() >= GameConfig.BALL_INITIAL_SPEED);
    }

    @Test
    void piercingDestroysBricksWithoutReversingOrEndingOnBrickContact() {
        Ball ball = new Ball(110, 200, 0, 200, 5);
        ball.setPiercing(true);
        Brick brick = new Brick(90, 220, 40, 14, BrickType.NORMAL);
        PhysicsEngine.BallResult result = physics.update(ball, paddle, List.of(brick), 0.2);
        assertTrue(brick.isDestroyed());
        assertEquals(200, ball.velocityY(), 0.0001);
        assertTrue(ball.isPiercing());
        assertEquals(List.of(brick), result.destroyedBricks());
    }

    @Test
    void normalBrickCollisionReflectsAndSeparatesTheBall() {
        Ball ball = new Ball(110, 200, 0, 200, 5);
        Brick brick = new Brick(90, 220, 40, 14, BrickType.NORMAL);
        physics.update(ball, paddle, List.of(brick), 0.2);
        assertTrue(brick.isDestroyed());
        assertTrue(ball.velocityY() < 0);
        assertTrue(ball.y() < brick.y());
    }

    @Test
    void piercingSurvivesPaddleContact() {
        Ball ball = new Ball(paddle.centerX(), paddle.y() - 15, 0, 200, GameConfig.BALL_RADIUS);
        ball.setPiercing(true);
        PhysicsEngine.BallResult result = physics.update(ball, paddle, List.of(), 0.1);
        assertTrue(result.paddleHit());
        assertTrue(ball.velocityY() < 0);
        assertTrue(ball.isPiercing());
    }

    @Test
    void ballIsCaughtWhenItFallsIntoThePaddleFromTheSide() {
        Ball ball = new Ball(paddle.left() - GameConfig.BALL_RADIUS - 1,
                paddle.y() + 2, 200, 100, GameConfig.BALL_RADIUS);

        PhysicsEngine.BallResult result = physics.update(ball, paddle, List.of(), 0.05);

        assertTrue(result.paddleHit());
        assertTrue(ball.velocityY() < 0);
        assertTrue(ball.y() <= paddle.y() - ball.radius());
    }

    @Test
    void piercingEndsAtTopLeftAndRightBoundaries() {
        assertPiercingEnds(new Ball(200, 7, 0, -100, 5));
        assertPiercingEnds(new Ball(7, 200, -100, 0, 5));
        assertPiercingEnds(new Ball(GameConfig.WIDTH - 7, 200, 100, 0, 5));
    }

    @Test
    void wallHitResultIncludesTheClampedImpactPosition() {
        Ball ball = new Ball(7, 200, -100, 0, 5);
        ball.setPiercing(true);

        PhysicsEngine.BallResult result = physics.update(ball, paddle, List.of(), 0.1);

        assertTrue(result.wallHit());
        assertEquals(GameConfig.WALL_MARGIN + ball.radius(), result.wallX(), 0.0001);
        assertEquals(200, result.wallY(), 0.0001);
    }

    private void assertPiercingEnds(Ball ball) {
        ball.setPiercing(true);
        physics.update(ball, paddle, List.of(), 0.1);
        assertFalse(ball.isPiercing());
    }
}
