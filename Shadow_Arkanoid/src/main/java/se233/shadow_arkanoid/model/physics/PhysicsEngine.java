package se233.shadow_arkanoid.model.physics;

import java.util.ArrayList;
import java.util.List;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.Paddle;

public final class PhysicsEngine {
    public record BallResult(List<Brick> hitBricks, List<Brick> destroyedBricks,
                             boolean wallHit, boolean paddleHit, boolean lost) {
    }

    public BallResult update(Ball ball, Paddle paddle, List<Brick> bricks, double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        List<Brick> hitBricks = new ArrayList<>();
        List<Brick> destroyedBricks = new ArrayList<>();
        boolean wallHit = false;
        boolean paddleHit = false;
        int substeps = Math.max(1, (int) Math.ceil(ball.speed() * deltaTime / Math.max(2, ball.radius() * 0.45)));
        double step = substeps == 0 ? 0 : deltaTime / substeps;

        for (int i = 0; i < substeps; i++) {
            if (ball.isAttached()) {
                break;
            }
            double previousX = ball.x();
            double previousY = ball.y();
            ball.update(step);

            for (Brick brick : bricks) {
                if (brick.isDestroyed()) {
                    ball.clearBrickOverlap(brick);
                    continue;
                }
                boolean intersects = circleIntersects(ball.x(), ball.y(), ball.radius(), brick);
                if (!intersects) {
                    ball.clearBrickOverlap(brick);
                    continue;
                }
                if (ball.overlappingBricks().add(brick)) {
                    brick.hit();
                    hitBricks.add(brick);
                    if (brick.isDestroyed()) {
                        destroyedBricks.add(brick);
                        ball.clearBrickOverlap(brick);
                    }
                    if (!ball.isPiercing()) {
                        resolveBrickCollision(ball, brick, previousX, previousY);
                        break;
                    }
                }
            }

            double minX = GameConfig.WALL_MARGIN + ball.radius();
            double maxX = GameConfig.WIDTH - GameConfig.WALL_MARGIN - ball.radius();
            if (ball.x() < minX) {
                ball.setPosition(minX, ball.y());
                if (ball.velocityX() < 0) {
                    ball.setVelocity(-ball.velocityX(), ball.velocityY());
                }
                preventHorizontalTrap(ball);
                wallHit = true;
            } else if (ball.x() > maxX) {
                ball.setPosition(maxX, ball.y());
                if (ball.velocityX() > 0) {
                    ball.setVelocity(-ball.velocityX(), ball.velocityY());
                }
                preventHorizontalTrap(ball);
                wallHit = true;
            }
            if (ball.y() < GameConfig.HUD_HEIGHT + ball.radius()) {
                ball.setPosition(ball.x(), GameConfig.HUD_HEIGHT + ball.radius());
                if (ball.velocityY() < 0) {
                    ball.setVelocity(ball.velocityX(), -ball.velocityY());
                }
                preventHorizontalTrap(ball);
                wallHit = true;
            }

            // set ball speed to normal when Piercing ball hit the wall
            if (wallHit && ball.isPiercing()) {
                ball.setPiercing(false);
                ball.setSpeed(GameConfig.BALL_INITIAL_SPEED); // set speed to normal
            }

            if (ball.velocityY() > 0 && ball.y() + ball.radius() >= paddle.y()
                    && ball.x() + ball.radius() >= paddle.left()
                    && ball.x() - ball.radius() <= paddle.right()
                    && previousY + ball.radius() <= paddle.y() + 1) {
                resolvePaddleCollision(ball, paddle);
                paddleHit = true;
            }
            if (ball.y() - ball.radius() > GameConfig.HEIGHT) {
                return new BallResult(hitBricks, destroyedBricks, wallHit, paddleHit, true);
            }
        }
        return new BallResult(hitBricks, destroyedBricks, wallHit, paddleHit, false);
    }

    public static boolean circleIntersects(double x, double y, double radius, Brick brick) {
        double closestX = Math.max(brick.x(), Math.min(x, brick.x() + brick.width()));
        double closestY = Math.max(brick.y(), Math.min(y, brick.y() + brick.height()));
        double dx = x - closestX;
        double dy = y - closestY;
        return dx * dx + dy * dy <= radius * radius;
    }

    public static void resolvePaddleCollision(Ball ball, Paddle paddle) {
        double offset = Math.max(-1, Math.min(1, (ball.x() - paddle.centerX()) / (paddle.width() / 2)));
        double angle = offset * Math.acos(GameConfig.MIN_VERTICAL_SPEED_RATIO);
        double speed = Math.min(GameConfig.BALL_MAX_SPEED, Math.max(GameConfig.BALL_INITIAL_SPEED, ball.speed()));
        ball.setPosition(ball.x(), paddle.y() - ball.radius());
        ball.setVelocity(Math.sin(angle) * speed, -Math.cos(angle) * speed);
    }

    private void resolveBrickCollision(Ball ball, Brick brick, double previousX, double previousY) {
        double closestX = Math.max(brick.x(), Math.min(ball.x(), brick.x() + brick.width()));
        double closestY = Math.max(brick.y(), Math.min(ball.y(), brick.y() + brick.height()));
        double normalX = ball.x() - closestX;
        double normalY = ball.y() - closestY;
        double distance = Math.hypot(normalX, normalY);
        if (distance < 1e-8) {
            double leftDistance = Math.abs(previousX - (brick.x() - ball.radius()));
            double rightDistance = Math.abs(previousX - (brick.x() + brick.width() + ball.radius()));
            double topDistance = Math.abs(previousY - (brick.y() - ball.radius()));
            double bottomDistance = Math.abs(previousY - (brick.y() + brick.height() + ball.radius()));
            double nearest = Math.min(Math.min(leftDistance, rightDistance), Math.min(topDistance, bottomDistance));
            if (nearest == leftDistance) {
                normalX = -1;
                closestX = brick.x();
                closestY = Math.max(brick.y(), Math.min(ball.y(), brick.y() + brick.height()));
            } else if (nearest == rightDistance) {
                normalX = 1;
                closestX = brick.x() + brick.width();
                closestY = Math.max(brick.y(), Math.min(ball.y(), brick.y() + brick.height()));
            } else if (nearest == topDistance) {
                normalY = -1;
                closestX = Math.max(brick.x(), Math.min(ball.x(), brick.x() + brick.width()));
                closestY = brick.y();
            } else {
                normalY = 1;
                closestX = Math.max(brick.x(), Math.min(ball.x(), brick.x() + brick.width()));
                closestY = brick.y() + brick.height();
            }
        } else {
            normalX /= distance;
            normalY /= distance;
        }
        ball.setPosition(closestX + normalX * ball.radius(), closestY + normalY * ball.radius());
        double approachSpeed = ball.velocityX() * normalX + ball.velocityY() * normalY;
        if (approachSpeed < 0) {
            ball.setVelocity(ball.velocityX() - 2 * approachSpeed * normalX,
                    ball.velocityY() - 2 * approachSpeed * normalY);
        }
        preventHorizontalTrap(ball);
    }

    private void preventHorizontalTrap(Ball ball) {
        double speed = ball.speed();
        double minimumVertical = speed * GameConfig.MIN_VERTICAL_SPEED_RATIO;
        if (speed == 0 || Math.abs(ball.velocityY()) >= minimumVertical) {
            return;
        }
        double vertical = Math.copySign(minimumVertical, ball.velocityY() == 0 ? -1 : ball.velocityY());
        double horizontal = Math.sqrt(speed * speed - vertical * vertical);
        ball.setVelocity(Math.copySign(horizontal, ball.velocityX() == 0 ? 1 : ball.velocityX()), vertical);
    }
}
