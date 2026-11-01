package se233.shadow_arkanoid.view.effects;

import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public final class ParticleSystem {
    private static final Color[] ENERGY_COLORS = {
            Color.web("#ff6a00"), Color.web("#ff9d24"), Color.web("#ffd36a"), Color.web("#fff2bd")
    };
    private static final DropShadow PARTICLE_GLOW = new DropShadow(7, Color.rgb(255, 112, 16, 0.8));
    private final Particle[] particles;

    public ParticleSystem(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Particle capacity must be positive");
        }
        particles = new Particle[capacity];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
    }

    public void emit(double x, double y, double velocityX, double velocityY, double lifetime,
                     double radius, Color color) {
        for (Particle particle : particles) {
            if (!particle.isAlive()) {
                particle.reset(x, y, velocityX, velocityY, lifetime, radius, color);
                return;
            }
        }
    }

    public void burst(double x, double y, int count, double minimumSpeed, double maximumSpeed,
                      double minimumLifetime, double maximumLifetime, double minimumRadius,
                      double maximumRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble(0, Math.PI * 2);
            double speed = random.nextDouble(minimumSpeed, maximumSpeed);
            emit(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    random.nextDouble(minimumLifetime, maximumLifetime),
                    random.nextDouble(minimumRadius, maximumRadius),
                    ENERGY_COLORS[random.nextInt(ENERGY_COLORS.length)]);
        }
    }

    public void update(double deltaTime) {
        for (Particle particle : particles) {
            particle.update(deltaTime);
        }
    }

    public void render(GraphicsContext graphics) {
        graphics.save();
        graphics.setEffect(PARTICLE_GLOW);
        for (Particle particle : particles) {
            particle.draw(graphics);
        }
        graphics.restore();
    }

    public boolean isEmpty() {
        for (Particle particle : particles) {
            if (particle.isAlive()) {
                return false;
            }
        }
        return true;
    }
}
