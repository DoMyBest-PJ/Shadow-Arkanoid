package se233.shadow_arkanoid.model;

import se233.shadow_arkanoid.GameConfig;

public final class SpecialSkillDrop {
    private final double x;
    private double y;
    public double x() { return x; }
    public double y() { return y; }

    //drop ถูกที่อะปล่าว
    public SpecialSkillDrop(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Special skill drop position must be finite");
        }
        this.x = x;
        this.y = y;
    }

    //dt = เวลาที่ใช้ render
    public void update(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        y += GameConfig.SPECIAL_SKILL_DROP_SPEED * deltaTime;
    }

    //เช็คว่าชนผู้เล่นไหม
    public boolean isCollectedBy(Paddle paddle) {
        return x + GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 >= paddle.left()
                && x - GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 <= paddle.right()
                && y + GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 >= paddle.y()
                && y - GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 <= paddle.y() + paddle.height();
    }

    //เช็คว่าลงโลกไปรึยัง
    public boolean isBelowPlayfield() {
        return y - GameConfig.SPECIAL_SKILL_DROP_SIZE / 2 > GameConfig.HEIGHT;
    }
}
