package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Move;
import model.Pyraminx;
import view.PyraminxView;

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

        btnReset.setOnAction(e -> { cube.resetSolved(); writeState("Reset to solved."); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnScramble.setOnAction(e -> { cube.scramble(20); writeState("Scrambled 20 random moves."); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnUndo.setOnAction(e -> { cube.undo(); writeState("Undo"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRedo.setOnAction(e -> { cube.redo(); writeState("Redo"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });

        rbNormal.setOnAction(e -> tipOnlyMode = false);
        rbTipOnly.setOnAction(e -> {
            tipOnlyMode = true;
            System.out.println("Tip Only Mode: " + tipOnlyMode);
        });

        btnR.setOnAction(e -> { cube.apply(Move.R, tipOnlyMode); writeState(tipOnlyMode ? "r" : "R"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRprime.setOnAction(e -> { cube.apply(Move.R_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "r'" : "R'"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnL.setOnAction(e -> { cube.apply(Move.L, tipOnlyMode); writeState(tipOnlyMode ? "l" : "L"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnLprime.setOnAction(e -> { cube.apply(Move.L_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "l'" : "L'"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnU.setOnAction(e -> { cube.apply(Move.U, tipOnlyMode); writeState(tipOnlyMode ? "u" : "U"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnUprime.setOnAction(e -> { cube.apply(Move.U_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "u'" : "U'"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnB.setOnAction(e -> { cube.apply(Move.B, tipOnlyMode); writeState(tipOnlyMode ? "b" : "B"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnBprime.setOnAction(e -> { cube.apply(Move.B_PRIME, tipOnlyMode); writeState(tipOnlyMode ? "b'" : "B'"); PyraminxView.drawPyraminx(cube, canvas); updateUndoRedoButtons(btnUndo, btnRedo); });
        btnRunSeq.setOnAction(e -> {
            try {
                cube.apply(seqField.getText());
                writeState("Applied: " + seqField.getText());
                PyraminxView.drawPyraminx(cube, canvas);
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
                PyraminxView.drawPyraminx(cube, canvas);
                updateUndoRedoButtons(btnUndo, btnRedo);
            } catch (Exception ex) {
                log.appendText("Load error: " + ex.getMessage() + "\n");
            }
        });

        updateUndoRedoButtons(btnUndo, btnRedo);

        writeState("Ready.");
        PyraminxView.drawPyraminx(cube, canvas);
        stage.setScene(new Scene(root, 720, 700));
        stage.show();
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