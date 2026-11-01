package se233.shadow_arkanoid;

public final class FrameLayout {
    public static final String IMAGE_RESOURCE = "/se233/shadow_arkanoid/assets/picture/Frame.png";
    public static final double FRAME_WIDTH = 1191;
    public static final double FRAME_HEIGHT = 1321;
    public static final double GAMEPLAY_X = 132;
    public static final double GAMEPLAY_Y = 109;
    public static final double GAMEPLAY_WIDTH = 927;
    public static final double GAMEPLAY_HEIGHT = 1212;

    private FrameLayout() {
    }

    public static double scaleFor(double viewportWidth, double viewportHeight) {
        if (!Double.isFinite(viewportWidth) || !Double.isFinite(viewportHeight)
                || viewportWidth <= 0 || viewportHeight <= 0) {
            return 0;
        }
        return Math.min(viewportWidth / FRAME_WIDTH, viewportHeight / FRAME_HEIGHT);
    }

    public static double horizontalOffset(double viewportWidth, double scale) {
        return (viewportWidth - FRAME_WIDTH * scale) / 2;
    }

    public static double verticalOffset(double viewportHeight, double scale) {
        return (viewportHeight - FRAME_HEIGHT * scale) / 2;
    }
}
