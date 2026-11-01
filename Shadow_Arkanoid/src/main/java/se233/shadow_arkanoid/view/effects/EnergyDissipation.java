package se233.shadow_arkanoid.view.effects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public final class EnergyDissipation {
    private static final double DURATION = 0.48;
    private static final RadialGradient GLOW = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(255, 244, 188, 0.95)),
            new Stop(0.35, Color.rgb(255, 133, 18, 0.68)),
            new Stop(1, Color.TRANSPARENT));
    private static final GaussianBlur BLUR = new GaussianBlur(7);
    private static final DropShadow RING_GLOW = new DropShadow(9, Color.web("#ff7814"));
    private final double x;
    private final double y;
    private final ParticleSystem particles = new ParticleSystem(16);
    private double age;

    public EnergyDissipation(double x, double y) {
        this.x = x;
        this.y = y;
        particles.burst(x, y, 12, 45, 145, 0.18, 0.4, 1.2, 2.7);
    }

    public void update(double deltaTime) {
        age += deltaTime;
        particles.update(deltaTime);
    }

    public void render(GraphicsContext graphics) {
        double progress = Math.min(1, age / DURATION);
        double remaining = 1 - progress;
        double radius = 7 + progress * 31;
        graphics.save();
        graphics.setEffect(BLUR);
        graphics.setFill(GLOW);
        graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        graphics.setEffect(RING_GLOW);
        graphics.setStroke(Color.rgb(255, 164, 52, remaining * 0.85));
        graphics.setLineWidth(Math.max(0.8, 3 * remaining));
        double ringRadius = 5 + progress * 34;
        graphics.strokeOval(x - ringRadius, y - ringRadius, ringRadius * 2, ringRadius * 2);
        graphics.setEffect(null);
        particles.render(graphics);
        graphics.restore();
    }

    public boolean isFinished() {
        return age >= DURATION && particles.isEmpty();
    }
}
