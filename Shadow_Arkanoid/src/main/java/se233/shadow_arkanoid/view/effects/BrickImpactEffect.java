package se233.shadow_arkanoid.view.effects;

import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.GameWorld;

final class BrickImpactEffect {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final boolean destroyed;
    private final double duration;
    private final Color color;
    private final DropShadow glow;
    private final ParticleSystem particles;
    private double age;

    BrickImpactEffect(GameWorld.VisualCue cue) {
        x = cue.x();
        y = cue.y();
        width = cue.width();
        height = cue.height();
        destroyed = cue.type() == GameWorld.VisualCue.Type.BRICK_DESTROYED;
        duration = destroyed ? GameConfig.BRICK_BREAK_ANIMATION_SECONDS
                : GameConfig.BRICK_HIT_ANIMATION_SECONDS;
        color = switch (cue.brickType()) {
            case NORMAL -> Color.web("#42bfff");
            case STRONG -> Color.web("#b46bff");
            case BONUS -> Color.web("#ffc34f");
        };
        glow = new DropShadow(destroyed ? 12 : 7, color);
        particles = new ParticleSystem(destroyed ? 28 : 10);
        emitParticles();
    }

    void update(double deltaTime) {
        age += deltaTime;
        particles.update(deltaTime);
    }

    void render(GraphicsContext graphics) {
        double progress = Math.min(1, age / duration);
        double remaining = 1 - progress;
        double radius = Math.max(width, height) * (0.15 + progress * (destroyed ? 0.9 : 0.55));
        graphics.save();
        graphics.setEffect(glow);
        graphics.setStroke(color.deriveColor(0, 1, 1, remaining * 0.9));
        graphics.setLineWidth(Math.max(0.7, (destroyed ? 3.2 : 2.4) * remaining));
        graphics.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        if (progress < 0.12) {
            graphics.setFill(Color.WHITE.deriveColor(0, 1, 1, (1 - progress / 0.12) * 0.75));
            graphics.fillRoundRect(x - width / 2, y - height / 2, width, height, 7, 7);
        }
        graphics.setEffect(null);
        particles.render(graphics);
        graphics.restore();
    }

    boolean isFinished() {
        return age >= duration && particles.isEmpty();
    }

    private void emitParticles() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = destroyed ? 24 : 8;
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble(0, Math.PI * 2);
            double speed = destroyed ? random.nextDouble(65, 190) : random.nextDouble(35, 105);
            double lifetime = destroyed ? random.nextDouble(0.24, 0.48) : random.nextDouble(0.12, 0.24);
            double radius = destroyed ? random.nextDouble(1.4, 3.1) : random.nextDouble(1.0, 2.2);
            Color particleColor = i % 3 == 0 ? Color.WHITE : color;
            particles.emit(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    lifetime, radius, particleColor);
        }
    }
}
