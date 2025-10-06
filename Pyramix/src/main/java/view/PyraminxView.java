package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.Pyraminx;

public class PyraminxView {

    public static void drawPyraminx(Pyraminx cube, Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double size = 120;
        double h = Math.sqrt(3) / 2 * size;
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;

        double topY = cy - h * 0.6;
        drawFace(gc, cx - size, topY, size, 2, 0, cube);     // Green (left)
        drawFace(gc, cx, topY, size, 1, 0, cube);            // Red (center)
        drawFace(gc, cx + size, topY, size, 0, 180, cube);   // Yellow (right, flipped)

        double bottomY = cy + h * 0.6;
        drawFace(gc, cx, bottomY, size, 3, -1, cube);  // Blue (bottom, flipped both ways)
    }

    private static void drawFace(GraphicsContext gc, double cx, double cy, double size, int face, double rot, Pyraminx cube) {
        double h = Math.sqrt(3) / 2 * size;
        boolean flipHorizontal = (rot == 180);
        boolean flipBoth = (rot == -1);

        gc.save();
        gc.translate(cx, cy);
        if (flipHorizontal) {
            gc.scale(-1, 1); // Flip horizontally only
        } else if (flipBoth) {
            gc.scale(-1, -1); // Flip both horizontally and vertically
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokePolygon(new double[]{0, -size/2, size/2}, new double[]{-h/2, h/2, h/2}, 3);

        double s = size / 3;
        double hs = Math.sqrt(3) / 2 * s;

        // Draw tip
        drawTriangle(gc, 0, -h/2 + hs/3, s, cube.getSticker(face, 0), false);

        // Draw corners
        drawTriangle(gc, -s/2, -h/2 + hs + hs/3, s, cube.getSticker(face, 1), false);
        drawTriangle(gc, s/2, -h/2 + hs + hs/3, s, cube.getSticker(face, 2), false);

        // Draw center
        drawTriangle(gc, 0, -h/2 + hs * 1.33, s, cube.getSticker(face, 6), true);

        // Draw bottom corners
        drawTriangle(gc, -size/2 + s/2, -h/2 + 2*hs + hs/3, s, cube.getSticker(face, 3), false);
        drawTriangle(gc, size/2 - s/2, -h/2 + 2*hs + hs/3, s, cube.getSticker(face, 5), false);

        // Draw bottom centers
        drawTriangle(gc, -s/2, -h/2 + 2*hs * 1.17, s, cube.getSticker(face, 7), true);
        drawTriangle(gc, 0, -h/2 + 2*hs + hs/3, s, cube.getSticker(face, 4), false);
        drawTriangle(gc, s/2, -h/2 + 2*hs * 1.17, s, cube.getSticker(face, 8), true);

        gc.restore();
    }

    private static void drawTriangle(GraphicsContext gc, double cx, double cy, double s,
                              model.Color4 color, boolean inverted) {
        double h = Math.sqrt(3) / 2 * s;
        double[] xs, ys;

        if (!inverted) {
            xs = new double[]{cx, cx - s/2, cx + s/2};
            ys = new double[]{cy - h/2, cy + h/2, cy + h/2};
        } else {
            xs = new double[]{cx, cx - s/2, cx + s/2};
            ys = new double[]{cy + h/2, cy - h/2, cy - h/2};
        }

        gc.setFill(fxColor(color));
        gc.fillPolygon(xs, ys, 3);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokePolygon(xs, ys, 3);
    }

    private static Color fxColor(model.Color4 c) {
        return switch (c) {
            case YELLOW -> Color.YELLOW;
            case RED -> Color.RED;
            case GREEN -> Color.LIMEGREEN;
            case BLUE -> Color.DODGERBLUE;
        };
    }
}
