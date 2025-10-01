package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Move;
import model.Pyraminx;

public class PyraminxApp extends Application {
    private final Pyraminx cube = new Pyraminx();
    private final TextArea log = new TextArea();
    private Canvas netCanvas;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pyraminx – Abstraction to Implementation");

        // Controls
        Button btnReset = new Button("Reset (Solved)");
        Button btnScramble = new Button("Scramble x20");

        Button btnR = new Button("R");
        Button btnRprime = new Button("R'");
        Button btnL = new Button("L");
        Button btnLprime = new Button("L'");
        Button btnU = new Button("U");
        Button btnUprime = new Button("U'");

        Button btnTips = new Button("Step 1: Solve Tips");
        Button btnCenters = new Button("Step 2: Solve Centers");
        Button btnEdges = new Button("Step 3: Solve Edges");
        Button btnSecond = new Button("Step 4: Second Layer");

        TextField seqField = new TextField("U' L' U L");
        Button btnRunSeq = new Button("Run Sequence");

        // Layout
        HBox topRow = new HBox(10, btnReset, btnScramble);
        HBox moveRow = new HBox(10, btnR, btnRprime, btnL, btnLprime, btnU, btnUprime);
        HBox stepRow = new HBox(10, btnTips, btnCenters, btnEdges, btnSecond);
        HBox seqRow = new HBox(10, new Label("Algorithm:"), seqField, btnRunSeq);

        topRow.setPadding(new Insets(10));
        moveRow.setPadding(new Insets(10));
        stepRow.setPadding(new Insets(10));
        seqRow.setPadding(new Insets(10));

        log.setEditable(false);
        log.setPrefRowCount(14);
        log.setWrapText(true);

        // Canvas for net
        netCanvas = new Canvas(500, 500);

        VBox root = new VBox(10,
                topRow, moveRow, stepRow, seqRow,
                new Label("State:"), log,
                new Label("2D Net:"), netCanvas
        );
        root.setPadding(new Insets(10));

        // Wiring
        btnReset.setOnAction(e -> {
            cube.resetSolved();
            writeState("Reset to solved.");
        });
        btnScramble.setOnAction(e -> {
            cube.scramble(20);  // now calls apply() not just tip scramble
            writeState("Scrambled 20 random moves.");
        });

        btnR.setOnAction(e -> { cube.apply(Move.R); writeState("R"); });
        btnRprime.setOnAction(e -> { cube.apply(Move.R_PRIME); writeState("R'"); });
        btnL.setOnAction(e -> { cube.apply(Move.L); writeState("L"); });
        btnLprime.setOnAction(e -> { cube.apply(Move.L_PRIME); writeState("L'"); });
        btnU.setOnAction(e -> { cube.apply(Move.U); writeState("U"); });
        btnUprime.setOnAction(e -> { cube.apply(Move.U_PRIME); writeState("U'"); });

        btnTips.setOnAction(e -> { cube.solveTips(); writeState("Step 1: solveTips()"); });
        btnCenters.setOnAction(e -> { cube.solveCenters(); writeState("Step 2: solveCenters()"); });
        btnEdges.setOnAction(e -> { cube.solveEdges(); writeState("Step 3: solveEdges()"); });
        btnSecond.setOnAction(e -> { cube.solveSecondLayer(); writeState("Step 4: solveSecondLayer()"); });

        btnRunSeq.setOnAction(e -> {
            String seq = seqField.getText();
            try {
                cube.apply(seq);
                writeState("Applied: " + seq);
            } catch (Exception ex) {
                append("Error: " + ex.getMessage());
            }
        });

        // Initial state
        writeState("Ready.");
        stage.setScene(new Scene(root, 720, 700));
        stage.show();
    }

    /** Update both log + net canvas */
    private void writeState(String header) {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append('\n');
        for (int f = 0; f < 4; f++) {
            sb.append(cube.faceSummary(f)).append('\n');
        }
        sb.append("Status: ").append(cube.status()).append('\n');
        log.setText(sb.toString());

        renderNet(netCanvas);
    }

    private void append(String line) {
        log.appendText(line + "\n");
    }

    /** Draws the 2D net from model */
    private void renderNet(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(1.2);

        double size = 150;
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;

        // Top (face 0, yellow)
        drawFace(gc, cx, cy - size, size, 0, 0);
        // Left (face 2, green)
        drawFace(gc, cx - size, cy, size, 0, 2);
        // Right (face 1, red)
        drawFace(gc, cx + size, cy, size, 0, 1);
        // Bottom (face 3, blue), rotated
        drawFace(gc, cx, cy + size, size, 180, 3);
    }

    /** Draw one triangular face with n=3 subdivisions */
    private void drawFace(GraphicsContext gc, double anchorX, double anchorY,
                          double size, double rotationDeg, int faceIndex) {

        int n = 3;
        double triSide = size / n;
        double triH = Math.sqrt(3) / 2 * triSide;

        gc.save();
        gc.translate(anchorX, anchorY);
        gc.rotate(rotationDeg);

        Color baseColor = Pyraminx.FACE_COLOR[faceIndex].toFXColor();

        for (int row = 0; row < n; row++) {
            for (int col = 0; col <= row; col++) {
                double bx = -(row * triSide) / 2.0 + col * triSide;
                double by = row * triH;

                // Up triangle
                double[] ux = { bx, bx + triSide / 2, bx - triSide / 2 };
                double[] uy = { by, by + triH, by + triH };

                Color fill = baseColor;
                if (row == 0 && col == 0) {
                    fill = cube.stickerColor(faceIndex, 0).toFXColor();
                } else if (row == n - 1 && col == 0) {
                    fill = cube.stickerColor(faceIndex, 1).toFXColor();
                } else if (row == n - 1 && col == n - 1) {
                    fill = cube.stickerColor(faceIndex, 2).toFXColor();
                }

                gc.setFill(fill);
                gc.fillPolygon(ux, uy, 3);
                gc.setStroke(Color.BLACK);
                gc.strokePolygon(ux, uy, 3);

                // Down triangle
                if (row < n - 1) {
                    double[] dx = { bx, bx + triSide, bx + triSide / 2 };
                    double[] dy = { by, by, by + triH };

                    gc.setFill(baseColor);
                    gc.fillPolygon(dx, dy, 3);
                    gc.setStroke(Color.BLACK);
                    gc.strokePolygon(dx, dy, 3);
                }
            }
        }
        gc.restore();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
