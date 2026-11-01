package se233.shadow_arkanoid.view.effects;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class ParryEffect {
    private static final double DURATION = 0.95;
    private static final RadialGradient IMPACT_GLOW = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(255, 248, 205, 0.98)),
            new Stop(0.22, Color.rgb(255, 153, 32, 0.86)),
            new Stop(0.62, Color.rgb(255, 91, 8, 0.24)),
            new Stop(1, Color.TRANSPARENT));
    private static final GaussianBlur GLOW_BLUR = new GaussianBlur(11);
    private static final DropShadow RING_GLOW = new DropShadow(12, Color.rgb(255, 115, 18, 0.9));
    private static final DropShadow TEXT_GLOW = new DropShadow(16, Color.web("#ff7b12"));
    private final double x;
    private final double y;
    private final ParticleSystem particles = new ParticleSystem(28);
    private double age;

    public ParryEffect(double x, double y) {
        this.x = x;
        this.y = y;
        particles.burst(x, y, 22, 75, 265, 0.28, 0.78, 1.7, 3.8);
    }

    public void update(double deltaTime) {
        age += deltaTime;
        particles.update(deltaTime);
    }

    public void render(GraphicsContext graphics) {
        double progress = Math.min(1, age / DURATION);
        double remaining = 1 - progress;
        graphics.save();

        double glowRadius = 19 + progress * 35;
        graphics.setEffect(GLOW_BLUR);
        graphics.setFill(IMPACT_GLOW);
        graphics.fillOval(x - glowRadius, y - glowRadius, glowRadius * 2, glowRadius * 2);
        graphics.setEffect(null);

        double ringRadius = 9 + progress * 76;
        graphics.setEffect(RING_GLOW);
        graphics.setStroke(Color.rgb(255, 177, 64, remaining * 0.95));
        graphics.setLineWidth(Math.max(1.2, 4.5 * remaining));
        graphics.strokeOval(x - ringRadius, y - ringRadius, ringRadius * 2, ringRadius * 2);
        graphics.setStroke(Color.rgb(255, 239, 191, remaining * 0.78));
        graphics.setLineWidth(Math.max(0.7, 1.6 * remaining));
        double innerRadius = ringRadius * 0.82;
        graphics.strokeOval(x - innerRadius, y - innerRadius, innerRadius * 2, innerRadius * 2);
        graphics.setEffect(null);

        if (progress < 0.15) {
            double flash = 1 - progress / 0.15;
            double flashRadius = 8 + progress * 180;
            graphics.setFill(Color.rgb(255, 245, 196, flash * 0.9));
            graphics.fillOval(x - flashRadius, y - flashRadius, flashRadius * 2, flashRadius * 2);
        }

        particles.render(graphics);

        double textOpacity = Math.min(1, remaining * 2.5);
        graphics.setGlobalAlpha(textOpacity);
        graphics.setEffect(TEXT_GLOW);
        graphics.setFill(Color.web("#fff0b0"));
        graphics.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 18));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.fillText("PERFECT PARRY!", x, y - 28 - progress * 42);
        graphics.setEffect(null);

        graphics.restore();
    }

    public boolean isFinished() {
        return age >= DURATION && particles.isEmpty();
    }
}
