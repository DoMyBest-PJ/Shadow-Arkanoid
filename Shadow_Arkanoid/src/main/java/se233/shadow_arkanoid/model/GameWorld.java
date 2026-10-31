package se233.shadow_arkanoid.model;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.physics.PhysicsEngine;
import se233.shadow_arkanoid.model.system.LifeManager;
import se233.shadow_arkanoid.model.system.ParrySystem;
import se233.shadow_arkanoid.model.system.ScoreManager;
import se233.shadow_arkanoid.model.system.StageManager;
import se233.shadow_arkanoid.model.upgrade.Upgrade;
import se233.shadow_arkanoid.model.upgrade.UpgradeManager;

public final class GameWorld {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameWorld.class);
    private final Paddle paddle = new Paddle();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Brick> bricks = new ArrayList<>();
    private final List<String> events = new ArrayList<>();
    private final ScoreManager scoreManager = new ScoreManager();
    private final LifeManager lifeManager = new LifeManager(GameConfig.STARTING_LIVES);
    private final ParrySystem parrySystem = new ParrySystem();
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private final StageManager stageManager = new StageManager();
    private final UpgradeManager upgradeManager = new UpgradeManager();
    private final List<String> activeUpgrades = new ArrayList<>();
    private List<Upgrade> upgradeChoices = List.of();
    private GameState state = GameState.MAIN_MENU;
    private GameState pausedFrom = GameState.PLAYING;
    private int stage = 1;
    private double stateTimer;
    private double messageTimer;
    private String message = "";
    private double paddleWidthMultiplier = 1;
    private double paddleSpeedMultiplier = 1;
    private double scoreMultiplier = 1;

    public void startNewGame() {
        scoreManager.reset();
        lifeManager.reset(GameConfig.STARTING_LIVES);
        parrySystem.reset();
        paddle.reset();
        balls.clear();
        bricks.clear();
        events.clear();
        activeUpgrades.clear();
        upgradeChoices = List.of();
        stateTimer = 0;
        messageTimer = 0;
        message = "";
        pausedFrom = GameState.PLAYING;
        stage = 1;
        paddleWidthMultiplier = 1;
        paddleSpeedMultiplier = 1;
        scoreMultiplier = 1;
        addAttachedBall();
        loadStage();
        state = GameState.READY;
        LOGGER.info("New game started");
    }

    public void update(double deltaTime, double paddleDirection) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        double dt = Math.min(deltaTime, 0.05);
        messageTimer = Math.max(0, messageTimer - dt);
        if (state == GameState.LIFE_LOST) {
            stateTimer -= dt;
            if (stateTimer <= 0) {
                state = lifeManager.isGameOver() ? GameState.GAME_OVER : GameState.READY;
                if (state == GameState.GAME_OVER) {
                    LOGGER.info("Game over at score {} and stage {}", scoreManager.score(), stage);
                    events.add("GAME_OVER");
                }
            }
            return;
        }
        if (state == GameState.STAGE_COMPLETE) {
            stateTimer -= dt;
            if (stateTimer <= 0) {
                if (stageManager.hasNextStage(stage)) {
                    upgradeChoices = upgradeManager.chooseThree();
                    state = GameState.UPGRADE_SELECTION;
                } else {
                    state = GameState.VICTORY;
                    events.add("VICTORY");
                    LOGGER.info("Victory at score {}", scoreManager.score());
                }
            }
            return;
        }
        if (state != GameState.PLAYING && state != GameState.READY) {
            return;
        }

        paddle.move(paddleDirection, dt);
        if (state == GameState.READY) {
            for (Ball ball : balls) {
                if (ball.isAttached()) {
                    ball.attachAt(paddle.centerX(), paddle.y());
                }
            }
            return;
        }

        parrySystem.update(dt);
        List<Ball> lostBalls = new ArrayList<>();
        for (Ball ball : balls) {
            boolean piercingBeforeStep = ball.isPiercing();
            PhysicsEngine.BallResult result = physicsEngine.update(ball, paddle, bricks, dt);
            for (Brick hit : result.hitBricks()) {
                if (hit.type() == BrickType.BONUS) {
                    events.add("BONUS_HIT");
                } else {
                    events.add("BRICK_HIT");
                }
            }
            for (Brick destroyed : result.destroyedBricks()) {
                int points = scoreManager.awardBrick(destroyed);
                if (points > 0) {
                    LOGGER.info("{} brick destroyed; awarded {} points", destroyed.type(), points);
                    events.add("BRICK_DESTROYED");
                }
            }
            if (piercingBeforeStep && !ball.isPiercing()) {
                showMessage("PIERCING ENDED", 0.8);
                events.add("PIERCING_ENDED");
                LOGGER.info("Piercing ended at an outer wall");
            }
            if (result.paddleHit()) {
                events.add("PADDLE_HIT");
            }
            if (result.lost()) {
                lostBalls.add(ball);
            }
        }
        balls.removeAll(lostBalls);
        bricks.removeIf(Brick::isDestroyed);
        bricks.forEach(brick -> brick.update(dt));
        if (balls.isEmpty()) {
            loseLife();
            return;
        }
        if (bricks.isEmpty()) {
            state = GameState.STAGE_COMPLETE;
            stateTimer = 1.1;
            showMessage("STAGE CLEAR", 1.1);
            events.add("STAGE_COMPLETE");
            LOGGER.info("Stage {} completed", stage);
        }
    }

    // Texts when parry
    public boolean attemptParry() {

        // if parry cooldown...
        if (parrySystem.cooldownRemaining() > 0) {
            showMessage("PARRY IS NOT READY!",0.65);
            events.add("PARRY_FAILED");
            return false;
        }

        if (state != GameState.PLAYING || balls.isEmpty()) {
            showMessage("PARRY MISS", 0.65);
            events.add("PARRY_FAILED");
            return false;
        }
        for (Ball ball : balls) {
            if (parrySystem.attempt(ball, paddle)) {
                scoreManager.add(GameConfig.PARRY_BONUS);
                showMessage("PERFECT PARRY!", 1.0);
                events.add("PARRY_SUCCESS");
                LOGGER.info("Perfect Parry succeeded; piercing activated");
                return true;
            }
        }
        showMessage("PARRY MISS", 0.65);
        events.add("PARRY_FAILED");
        LOGGER.debug("Perfect Parry attempt missed");
        return false;
    }

    public void launch() {
        if (state == GameState.READY) {
            for (int i = 0; i < balls.size(); i++) {
                double ratio = balls.size() == 1 ? 0.26 : (i - (balls.size() - 1) / 2.0) * 0.38;
                balls.get(i).launch(ratio);
            }
            state = GameState.PLAYING;
            events.add("BALL_LAUNCHED");
        }
    }

    public void pause() {
        if (state == GameState.PLAYING || state == GameState.READY) {
            pausedFrom = state;
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = pausedFrom;
        }
    }

    public void showHowToPlay() {
        state = GameState.HOW_TO_PLAY;
    }

    public void returnToMenu() {
        state = GameState.MAIN_MENU;
        balls.clear();
        bricks.clear();
    }

    public void selectUpgrade(int index) {
        if (state != GameState.UPGRADE_SELECTION || index < 0 || index >= upgradeChoices.size()) {
            return;
        }
        Upgrade selected = upgradeChoices.get(index);
        selected.apply(this);
        activeUpgrades.add(selected.name());
        events.add("UPGRADE_SELECTED");
        LOGGER.info("Upgrade selected: {}", selected.name());
        stage++;
        loadStage();
        for (int i = 0; i < balls.size(); i++) {
            double offset = (i - (balls.size() - 1) / 2.0) * 28;
            balls.get(i).attachAt(paddle.centerX() + offset, paddle.y());
        }
        state = GameState.READY;
    }

    public void movePaddleTo(double x) {
        paddle.moveTo(x);
    }

    public void addLife() {
        lifeManager.addLife();
    }

    public void increasePaddleWidth(double factor) {
        paddleWidthMultiplier *= factor;
        paddle.setWidthMultiplier(paddleWidthMultiplier);
    }

    public void increasePaddleSpeed(double factor) {
        paddleSpeedMultiplier *= factor;
        paddle.setMovementSpeedMultiplier(paddleSpeedMultiplier);
    }

    public void increaseScoreMultiplier(double increment) {
        scoreMultiplier += increment;
        scoreManager.setMultiplier(scoreMultiplier);
    }

    public void improveParryWindow(double seconds) {
        parrySystem.improveTimingWindow(seconds);
    }

    public void addBall() {
        double offset = (balls.size() % 2 == 0 ? 1 : -1) * (18 + balls.size() * 5);
        Ball clone = new Ball(paddle.centerX() + offset, paddle.y() - GameConfig.BALL_RADIUS - 1,
                0, 0, GameConfig.BALL_RADIUS);
        balls.add(clone);
        if (state == GameState.PLAYING) {
            double angle = Math.toRadians(offset < 0 ? -35 : 35);
            clone.setVelocity(GameConfig.BALL_INITIAL_SPEED * Math.sin(angle),
                    -GameConfig.BALL_INITIAL_SPEED * Math.cos(angle));
        } else {
            clone.attachAt(paddle.centerX() + 16, paddle.y());
        }
    }

    public void showMessage(String text, double seconds) {
        message = text;
        messageTimer = seconds;
    }

    public List<String> drainEvents() {
        List<String> drained = List.copyOf(events);
        events.clear();
        return drained;
    }

    private void loseLife() {
        if (lifeManager.loseLife()) {
            LOGGER.info("Life lost; {} remain", lifeManager.lives());
            events.add("LIFE_LOST");
        }
        paddle.reset();
        paddle.setWidthMultiplier(paddleWidthMultiplier);
        paddle.setMovementSpeedMultiplier(paddleSpeedMultiplier);
        balls.clear();
        addAttachedBall();
        state = GameState.LIFE_LOST;
        stateTimer = 1.1;
        showMessage("LIFE LOST", 1.1);
    }

    private void loadStage() {
        bricks.clear();
        bricks.addAll(stageManager.createStage(stage));
        LOGGER.info("Stage {} started with {} bricks", stage, bricks.size());
    }

    private void addAttachedBall() {
        Ball ball = new Ball(paddle.centerX(), paddle.y() - GameConfig.BALL_RADIUS - 1, 0, 0,
                GameConfig.BALL_RADIUS);
        ball.attachAt(paddle.centerX(), paddle.y());
        balls.add(ball);
    }

    public Paddle paddle() { return paddle; }
    public List<Ball> balls() { return List.copyOf(balls); }
    public List<Brick> bricks() { return List.copyOf(bricks); }
    public ScoreManager scoreManager() { return scoreManager; }
    public LifeManager lifeManager() { return lifeManager; }
    public ParrySystem parrySystem() { return parrySystem; }
    public GameState state() { return state; }
    public int stage() { return stage; }
    public List<Upgrade> upgradeChoices() { return upgradeChoices; }
    public List<String> activeUpgrades() { return List.copyOf(activeUpgrades); }
    public String message() { return messageTimer > 0 ? message : ""; }
    public double messageTimer() { return messageTimer; }
}
