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
    private Canvas canvas;
    private boolean tipOnlyMode = false;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Pyraminx Visualizer (2D Net)");

        Button btnReset = new Button("Reset (Solved)");
        Button btnScramble = new Button("Scramble x20");
        Button btnUndo = new Button("Undo");
        Button btnRedo = new Button("Redo");
        Button btnSave = new Button("Save");
        Button btnLoad = new Button("Load");
        Button btnR = new Button("R");
        Button btnRprime = new Button("R'");
        Button btnL = new Button("L");
        Button btnLprime = new Button("L'");
        Button btnU = new Button("U");
        Button btnUprime = new Button("U'");
        Button btnB = new Button("B");
        Button btnBprime = new Button("B'");

        RadioButton rbNormal = new RadioButton("Normal Moves");
        RadioButton rbTipOnly = new RadioButton("Tip Only");
        ToggleGroup moveMode = new ToggleGroup();
        rbNormal.setToggleGroup(moveMode);
        rbTipOnly.setToggleGroup(moveMode);
        rbNormal.setSelected(true);

        TextField seqField = new TextField("U' L' U L");
        Button btnRunSeq = new Button("Run Sequence");

        HBox topRow = new HBox(10, btnReset, btnScramble, btnUndo, btnRedo, btnSave, btnLoad);
        HBox moveRow = new HBox(10, btnR, btnRprime, btnL, btnLprime, btnU, btnUprime, btnB, btnBprime);
        HBox modeRow = new HBox(15, new Label("Move Mode:"), rbNormal, rbTipOnly);
        HBox seqRow = new HBox(10, new Label("Algorithm:"), seqField, btnRunSeq);
        topRow.setPadding(new Insets(10));
        moveRow.setPadding(new Insets(10));
        modeRow.setPadding(new Insets(10));
        seqRow.setPadding(new Insets(10));

        log.setEditable(false);
        log.setPrefRowCount(8);
        log.setWrapText(true);
        canvas = new Canvas(500, 450);

        VBox root = new VBox(10, topRow, moveRow, modeRow, seqRow, new Label("State:"), log, canvas);
        root.setPadding(new Insets(10));

        btnReset.setOnAction(e -> { cube.resetSolved(); writeState("Reset to solved."); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnScramble.setOnAction(e -> { cube.scramble(20); writeState("Scrambled 20 random moves."); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnUndo.setOnAction(e -> { cube.undo(); writeState("Undo"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRedo.setOnAction(e -> { cube.redo(); writeState("Redo"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });

        rbNormal.setOnAction(e -> tipOnlyMode = false);
        rbTipOnly.setOnAction(e -> {
            tipOnlyMode = true;
            System.out.println("Tip Only Mode: " + tipOnlyMode);
        });

        btnR.setOnAction(e -> { cube.apply(Move.R, tipOnlyMode); writeState(tipOnlyMode ? "r" : "R"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRprime.setOnAction(e -> { cube.apply(Move.R_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "r'" : "R'"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnL.setOnAction(e -> { cube.apply(Move.L, tipOnlyMode); writeState(tipOnlyMode ? "l" : "L"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnLprime.setOnAction(e -> { cube.apply(Move.L_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "l'" : "L'"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnU.setOnAction(e -> { cube.apply(Move.U, tipOnlyMode); writeState(tipOnlyMode ? "u" : "U"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnUprime.setOnAction(e -> { cube.apply(Move.U_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "u'" : "U'"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnB.setOnAction(e -> { cube.apply(Move.B, tipOnlyMode); writeState(tipOnlyMode ? "b" : "B"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnBprime.setOnAction(e -> { cube.apply(Move.B_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "b'" : "B'"); drawPyraminx(); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRunSeq.setOnAction(e -> {
            try {
                cube.apply(seqField.getText());
                writeState("Applied: " + seqField.getText());
                drawPyraminx();
                updateUndoRedoButtons(btnUndo, btnRedo);
            } catch (Exception ex) {
                log.appendText("Error: " + ex.getMessage() + "\n");
            }
        });

        btnSave.setOnAction(e -> {
            try {
                cube.saveToFile("pyraminx_save.json");
                log.appendText("State saved to pyraminx_save.json\n");
            } catch (Exception ex) {
                log.appendText("Save error: " + ex.getMessage() + "\n");
            }
        });

        btnLoad.setOnAction(e -> {
            try {
                cube.loadFromFile("pyraminx_save.json");
                writeState("State loaded from file.");
                drawPyraminx();
                updateUndoRedoButtons(btnUndo, btnRedo);
            } catch (Exception ex) {
                log.appendText("Load error: " + ex.getMessage() + "\n");
            }
        });

        updateUndoRedoButtons(btnUndo, btnRedo);

        writeState("Ready.");
        drawPyraminx();
        stage.setScene(new Scene(root, 720, 700));
        stage.show();
    }

    private void drawPyraminx() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double size = 120;
        double h = Math.sqrt(3) / 2 * size;
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;

        double topY = cy - h * 0.6;
        drawFace(gc, cx - size, topY, size, 2, 0);     // Green (left)
        drawFace(gc, cx, topY, size, 1, 0);            // Red (center)
        drawFace(gc, cx + size, topY, size, 0, 180);   // Yellow (right, flipped)

        double bottomY = cy + h * 0.6;
        drawFace(gc, cx, bottomY, size, 3, -1);  // Blue (bottom, flipped both ways)
    }

    private void drawFace(GraphicsContext gc, double cx, double cy, double size, int face, double rot) {
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

    private void drawTriangle(GraphicsContext gc, double cx, double cy, double s,
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

    private Color fxColor(model.Color4 c) {
        return switch (c) {
            case YELLOW -> Color.YELLOW;
            case RED -> Color.RED;
            case GREEN -> Color.LIMEGREEN;
            case BLUE -> Color.DODGERBLUE;
        };
    }

    private void writeState(String header) {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append('\n');
        sb.append("Moves: ").append(cube.getMoveCount()).append(" | ");
        sb.append("History: ").append(cube.getMoveHistory()).append('\n');
        for (int f = 0; f < 4; f++) {
            sb.append(cube.faceSummary(f)).append('\n');
        }
        sb.append("Status: ").append(cube.status()).append('\n');
        log.setText(sb.toString());
    }

    private void updateUndoRedoButtons(Button btnUndo, Button btnRedo) {
        btnUndo.setDisable(!cube.canUndo());
        btnRedo.setDisable(!cube.canRedo());
    }

    public static void jumpStart(String[] args) {
        launch(args);
    }
}