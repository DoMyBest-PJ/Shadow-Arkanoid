package se233.shadow_arkanoid.view.effects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class Particle {
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double age;
    private double lifetime;
    private double radius;
    private Color color = Color.TRANSPARENT;

    void reset(double x, double y, double velocityX, double velocityY, double lifetime,
               double radius, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.age = 0;
        this.lifetime = lifetime;
        this.radius = radius;
        this.color = color;
    }

    void update(double deltaTime) {
        if (!isAlive()) {
            return;
        }
        age += deltaTime;
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        double drag = Math.exp(-2.2 * deltaTime);
        velocityX *= drag;
        velocityY = velocityY * drag + 28 * deltaTime;
    }

    void draw(GraphicsContext graphics) {
        if (!isAlive()) {
            return;
        }
        double remaining = Math.max(0, 1 - age / lifetime);
        double currentRadius = radius * (0.35 + remaining * 0.65);
        graphics.setFill(color.deriveColor(0, 1, 1, remaining));
        graphics.fillOval(x - currentRadius, y - currentRadius, currentRadius * 2, currentRadius * 2);
    }

    boolean isAlive() {
        return age < lifetime;
    }
}
