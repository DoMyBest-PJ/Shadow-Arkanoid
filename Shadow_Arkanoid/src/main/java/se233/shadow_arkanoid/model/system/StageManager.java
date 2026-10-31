package se233.shadow_arkanoid.model.system;

import java.util.ArrayList;
import java.util.List;
import se233.shadow_arkanoid.GameConfig;
import se233.shadow_arkanoid.model.Brick;
import se233.shadow_arkanoid.model.BrickType;

public final class StageManager {
    public List<Brick> createStage(int stage) {
        if (stage < 1) {
            throw new IllegalArgumentException("Stage number must be positive");
        }
        List<Brick> bricks = new ArrayList<>();
        int columns = 10;
        int rows = Math.min(4 + (stage + 1) / 2, 7);
        double brickWidth = 78;
        double brickHeight = 24;
        double gapX = 8;
        double gapY = 9;
        double totalWidth = columns * brickWidth + (columns - 1) * gapX;
        double startX = (GameConfig.WIDTH - totalWidth) / 2;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (!isPresent(stage, row, column)) {
                    continue;
                }
                BrickType type = chooseType(stage, row, column);
                bricks.add(new Brick(startX + column * (brickWidth + gapX),
                        104 + row * (brickHeight + gapY), brickWidth, brickHeight, type));
            }
        }
        return bricks;
    }

    public boolean hasNextStage(int currentStage) {
        return currentStage < GameConfig.TOTAL_STAGES;
    }

    private boolean isPresent(int stage, int row, int column) {
        return switch (stage % 5) {
            case 1 -> row < 3 || (column + row) % 3 != 0;
            case 2 -> row == 0 || row == 1 || (column + row) % 2 == 0;
            case 3 -> Math.abs(column - 4.5) <= row + 2.5;
            case 4 -> (column + row) % 4 != 0 || row < 2;
            default -> row == 0 || column % 2 == row % 2 || row > 3;
        };
    }

    private BrickType chooseType(int stage, int row, int column) {
        if (stage >= 2 && (row + column + stage) % 7 == 0) {
            return BrickType.BONUS;
        }
        if (stage >= 2 && row < stage / 2 && (row + column) % 3 == 0) {
            return BrickType.STRONG;
        }
        return BrickType.NORMAL;
    }
}
