package se233.shadow_arkanoid.view.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.canvas.GraphicsContext;
import se233.shadow_arkanoid.model.Ball;
import se233.shadow_arkanoid.model.GameWorld;

public final class VisualEffectManager {
    private final Map<Ball, BallTrail> ballTrails = new IdentityHashMap<>();
    private final List<ParryEffect> parryEffects = new ArrayList<>();
    private final List<EnergyDissipation> dissipations = new ArrayList<>();
    private final List<BrickImpactEffect> brickImpacts = new ArrayList<>();
    private final List<SpecialSkillSlashEffect> slashEffects = new ArrayList<>();
    private double elapsedTime;

    public void update(double deltaTime, List<GameWorld.VisualCue> cues, List<Ball> balls) {
        elapsedTime += deltaTime;
        for (GameWorld.VisualCue cue : cues) {
            switch (cue.type()) {
                case PERFECT_PARRY -> parryEffects.add(new ParryEffect(cue.x(), cue.y()));
                case PIERCING_DISSIPATED -> dissipations.add(new EnergyDissipation(cue.x(), cue.y()));
                case BRICK_HIT, BRICK_DESTROYED -> brickImpacts.add(new BrickImpactEffect(cue));
                case SPECIAL_SKILL_SLASH -> slashEffects.add(new SpecialSkillSlashEffect(cue.x()));
            }
        }

        Set<Ball> currentBalls = Collections.newSetFromMap(new IdentityHashMap<>());
        currentBalls.addAll(balls);
        for (Ball ball : balls) {
            if (ball.isPiercing() || ballTrails.containsKey(ball)) {
                ballTrails.computeIfAbsent(ball, ignored -> new BallTrail()).update(ball, deltaTime);
            }
        }

        Iterator<Map.Entry<Ball, BallTrail>> trailIterator = ballTrails.entrySet().iterator();
        while (trailIterator.hasNext()) {
            Map.Entry<Ball, BallTrail> entry = trailIterator.next();
            if (!currentBalls.contains(entry.getKey())) {
                entry.getValue().update(null, deltaTime);
            }
            if (entry.getValue().isFinished()) {
                trailIterator.remove();
            }
        }

        updateEffects(parryEffects, deltaTime);
        updateEffects(dissipations, deltaTime);
        Iterator<BrickImpactEffect> impactIterator = brickImpacts.iterator();
        while (impactIterator.hasNext()) {
            BrickImpactEffect impact = impactIterator.next();
            impact.update(deltaTime);
            if (impact.isFinished()) {
                impactIterator.remove();
            }
        }
        Iterator<SpecialSkillSlashEffect> slashIterator = slashEffects.iterator();
        while (slashIterator.hasNext()) {
            SpecialSkillSlashEffect slash = slashIterator.next();
            slash.update(deltaTime);
            if (slash.isFinished()) {
                slashIterator.remove();
            }
        }
    }

    public void renderTrails(GraphicsContext graphics) {
        for (BallTrail trail : ballTrails.values()) {
            trail.render(graphics);
        }
    }

    public void renderEffects(GraphicsContext graphics) {
        for (EnergyDissipation dissipation : dissipations) {
            dissipation.render(graphics);
        }
        for (ParryEffect effect : parryEffects) {
            effect.render(graphics);
        }
        for (BrickImpactEffect impact : brickImpacts) {
            impact.render(graphics);
        }
        for (SpecialSkillSlashEffect slash : slashEffects) {
            slash.render(graphics);
        }
    }

    public double energyFor(Ball ball) {
        BallTrail trail = ballTrails.get(ball);
        return trail == null ? 0 : trail.energy();
    }

    public double elapsedTime() {
        return elapsedTime;
    }

    private <T> void updateEffects(List<T> effects, double deltaTime) {
        Iterator<T> iterator = effects.iterator();
        while (iterator.hasNext()) {
            T effect = iterator.next();
            if (effect instanceof ParryEffect parryEffect) {
                parryEffect.update(deltaTime);
                if (parryEffect.isFinished()) {
                    iterator.remove();
                }
            } else if (effect instanceof EnergyDissipation dissipation) {
                dissipation.update(deltaTime);
                if (dissipation.isFinished()) {
                    iterator.remove();
                }
            }
        }
    }
}
