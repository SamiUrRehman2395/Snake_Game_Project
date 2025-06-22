package com.example.snakegame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameView {
    private final GraphicsContext gc;
    private final int cellSize;

    public GameView(GraphicsContext gc, int cellSize) {
        this.gc = gc;
        this.cellSize = cellSize;
    }


    public void render(char[][] grid,
                       int foodX, int foodY,
                       String difficulty,
                       int bonusFoodX, int bonusFoodY,
                       boolean hasBonusFood) {

        final int w = grid[0].length, h = grid.length;

        /* ----------  BACKGROUND  ---------- */
        gc.setFill(Color.web("#0f2027"));
        gc.fillRect(0, 0, w * cellSize, h * cellSize);

        /* ----------  GRID & SNAKE  ---------- */
        Color grassA = Color.web("#1e272e");
        Color grassB = Color.web("#2f3640");
        Color snakeOrange = Color.GREEN;

        double segSize   = cellSize * 0.9;         // body diameter
        double segOffset = (cellSize - segSize) / 2;

        double headSize   = cellSize * 1.0;        // head a bit bigger
        double headOffset = (cellSize - headSize) / 2;

        // First pass: draw background grid
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                gc.setFill((x + y) % 2 == 0 ? grassA : grassB);
                gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // Second pass: snake body & head
        int headX = -1, headY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                char c = grid[y][x];
                if (c == 'S') {                               // body segment
                    gc.setFill(snakeOrange);
                    gc.fillOval(x * cellSize + segOffset,
                            y * cellSize + segOffset,
                            segSize, segSize);
                } else if (c == 'H') {                        // remember head
                    headX = x;
                    headY = y;
                }
            }
        }

        // Draw head last so it sits on top
        if (headX >= 0) {
            gc.setFill(snakeOrange);
            gc.fillOval(headX * cellSize + headOffset,
                    headY * cellSize + headOffset,
                    headSize, headSize);

            /* --- simple cartoon eyes & tongue --- */
            double eyeR   = cellSize * 0.10;
            double eyeOff = cellSize * 0.22;
            double cx     = headX * cellSize + cellSize / 2.0;
            double cy     = headY * cellSize + cellSize / 2.0;

            gc.setFill(Color.WHITE);
            gc.fillOval(cx - eyeOff - eyeR, cy - eyeOff - eyeR, eyeR * 2, eyeR * 2); // left eye white
            gc.fillOval(cx + eyeOff - eyeR, cy - eyeOff - eyeR, eyeR * 2, eyeR * 2); // right eye white

            gc.setFill(Color.BLACK);
            double pupilR = eyeR * 0.5;
            gc.fillOval(cx - eyeOff - pupilR, cy - eyeOff - pupilR, pupilR * 2, pupilR * 2);
            gc.fillOval(cx + eyeOff - pupilR, cy - eyeOff - pupilR, pupilR * 2, pupilR * 2);

            // tongue (small red triangle)
            gc.setFill(Color.RED);
            gc.fillPolygon(
                    new double[]{cx,               cx - cellSize * 0.05, cx + cellSize * 0.05},
                    new double[]{cy + headSize*0.35, cy + headSize*0.55,  cy + headSize*0.55},
                    3
            );
        }

        /* ----------  REGULAR FOOD (orange orb)  ---------- */
        gc.setFill(Color.RED);
        double foodSize   = cellSize * 0.8;
        double foodOffset = (cellSize - foodSize) / 2;
        gc.fillOval(foodX * cellSize + foodOffset,
                foodY * cellSize + foodOffset,
                foodSize, foodSize);

        /* ----------  BONUS FOOD (blue diamond)  ---------- */
        if (hasBonusFood) {
            gc.setFont(new Font(cellSize));
            gc.setFill(Color.DEEPSKYBLUE);
            gc.fillText("💎",
                    bonusFoodX * cellSize + cellSize * 0.10,
                    bonusFoodY * cellSize + cellSize * 0.80);
        }

        /* ----------  HARD DIFFICULTY BORDER  ---------- */
        if ("Hard".equalsIgnoreCase(difficulty)) {
            gc.setStroke(Color.rgb(173, 7, 7));
            gc.setLineWidth(12);
            gc.strokeRect(0, 0, w * cellSize, h * cellSize);
        }
    }
}
