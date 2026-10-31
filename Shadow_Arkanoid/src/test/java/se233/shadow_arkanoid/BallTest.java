package se233.shadow_arkanoid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import se233.shadow_arkanoid.model.Ball;

class BallTest {
    @Test
    void positionUpdatesFromVelocityAndElapsedTime() {
        Ball ball = new Ball(20, 40, 100, -50, 5);
        ball.update(0.5);
        assertEquals(70, ball.x(), 0.0001);
        assertEquals(15, ball.y(), 0.0001);
    }
}
