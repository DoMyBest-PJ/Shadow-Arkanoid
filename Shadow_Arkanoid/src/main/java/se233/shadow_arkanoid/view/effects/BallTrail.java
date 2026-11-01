package se233.shadow_arkanoid.view.effects;

import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import se233.shadow_arkanoid.model.Ball;

public final class BallTrail {
    private static final double EMISSION_INTERVAL = 0.024;
    private static final double RESTORE_DURATION = 0.34;
    private final ParticleSystem particles = new ParticleSystem(36);
    private double energy;
    private double emissionTime;

    public void update(Ball ball, double deltaTime) {
        particles.update(deltaTime);
        if (ball != null && ball.isPiercing()) {
            energy = 1;
            emissionTime += deltaTime;
            while (emissionTime >= EMISSION_INTERVAL) {
                emissionTime -= EMISSION_INTERVAL;
                emitTrailParticle(ball);
            }
        } else {
            energy = Math.max(0, energy - deltaTime / RESTORE_DURATION);
            emissionTime = 0;
        }
    }

    public void render(GraphicsContext graphics) {
        particles.render(graphics);
    }

    public double energy() {
        return energy;
    }

    public boolean isFinished() {
        return energy <= 0 && particles.isEmpty();
    }

    private void emitTrailParticle(Ball ball) {
        double speed = ball.speed();
        if (speed == 0) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double direction = Math.atan2(-ball.velocityY(), -ball.velocityX())
                + random.nextDouble(-0.45, 0.45);
        double particleSpeed = random.nextDouble(34, 105);
        double spawnDistance = ball.radius() * random.nextDouble(0.65, 1.15);
        double particleX = ball.x() - ball.velocityX() / speed * spawnDistance;
        double particleY = ball.y() - ball.velocityY() / speed * spawnDistance;
        Color color = random.nextBoolean() ? Color.web("#ff7b12") : Color.web("#ffc04d");
        particles.emit(particleX, particleY, Math.cos(direction) * particleSpeed,
                Math.sin(direction) * particleSpeed, random.nextDouble(0.2, 0.42),
                random.nextDouble(1.7, 3.4), color);
    }
}
