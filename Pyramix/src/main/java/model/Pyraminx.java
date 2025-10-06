package model;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import static model.Utils.rnd;

/**
 * Student-friendly Pyraminx model.
 * - 4 faces: 0:Y, 1:R, 2:G, 3:B
 * - 6 global edges (ids 0..5), each has orientation 0/1
 * - Each face holds 3 "edge slots" pointing to global edges (id + ori)
 * - R, L, U moves permute tips, centers (orientations), and local 3-edge cycles
 */
public class Pyraminx {
    public static final Color4[] FACE_COLOR = {
            Color4.YELLOW, Color4.RED, Color4.GREEN, Color4.BLUE
    };

    private final int[] tipOri = new int[4];
    private final int[] centerOri = new int[4];
    private static final int EDGE_COUNT = 6;
    private final int[] edgeOri = new int[EDGE_COUNT];
    private final int[][] faceEdgeId = new int[4][3];
    private final int[][] faceEdgeOri = new int[4][3];
    private final Color4[][] faces = new Color4[4][9];
    private final java.util.Stack<Move> history = new java.util.Stack<>();
    private final java.util.Stack<Move> redoStack = new java.util.Stack<>();
    private final java.util.Stack<Boolean> tipOnlyHistory = new java.util.Stack<>();
    private final java.util.Stack<Boolean> tipOnlyRedo = new java.util.Stack<>();

    public static final int[][] EDGE_TO_FACES = {
            {0,1}, {0,2}, {0,3}, {1,2}, {1,3}, {2,3}
    };

    public Color4 getSticker(int face, int index) {
        return faces[face][index];
    }

    public int getCenterOri(int face) { return centerOri[face]; }
    public int getTipOri(int face) { return tipOri[face]; }

    public Color4 getFaceColor(int face, int index) {
        return faces[face][index];
    }

    public Pyraminx() {
        resetSolved();
    }

    public final void resetSolved() {
        Arrays.fill(tipOri, 0);
        Arrays.fill(centerOri, 0);
        Arrays.fill(edgeOri, 0);

        faceEdgeId[0][0] = 0; faceEdgeOri[0][0] = 0;
        faceEdgeId[0][1] = 1; faceEdgeOri[0][1] = 0;
        faceEdgeId[0][2] = 2; faceEdgeOri[0][2] = 0;

        faceEdgeId[1][0] = 0; faceEdgeOri[1][0] = 0;
        faceEdgeId[1][1] = 3; faceEdgeOri[1][1] = 0;
        faceEdgeId[1][2] = 4; faceEdgeOri[1][2] = 0;

        faceEdgeId[2][0] = 1; faceEdgeOri[2][0] = 0;
        faceEdgeId[2][1] = 3; faceEdgeOri[2][1] = 0;
        faceEdgeId[2][2] = 5; faceEdgeOri[2][2] = 0;

        faceEdgeId[3][0] = 2; faceEdgeOri[3][0] = 0;
        faceEdgeId[3][1] = 4; faceEdgeOri[3][1] = 0;
        faceEdgeId[3][2] = 5; faceEdgeOri[3][2] = 0;

        for (int f = 0; f < 4; f++) {
            for (int i = 0; i < 9; i++) {
                faces[f][i] = FACE_COLOR[f];
            }
        }

        clearHistory();
    }

    // Save state to JSON file
    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("{\n");

            // Save faces
            writer.write("  \"faces\": [\n");
            for (int f = 0; f < 4; f++) {
                writer.write("    [");
                for (int i = 0; i < 9; i++) {
                    writer.write("\"" + faces[f][i].name() + "\"");
                    if (i < 8) writer.write(", ");
                }
                writer.write("]");
                if (f < 3) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // Save tip orientations
            writer.write("  \"tipOri\": [");
            for (int i = 0; i < 4; i++) {
                writer.write(String.valueOf(tipOri[i]));
                if (i < 3) writer.write(", ");
            }
            writer.write("],\n");

            // Save center orientations
            writer.write("  \"centerOri\": [");
            for (int i = 0; i < 4; i++) {
                writer.write(String.valueOf(centerOri[i]));
                if (i < 3) writer.write(", ");
            }
            writer.write("],\n");

            // Save edge orientations
            writer.write("  \"edgeOri\": [");
            for (int i = 0; i < EDGE_COUNT; i++) {
                writer.write(String.valueOf(edgeOri[i]));
                if (i < EDGE_COUNT - 1) writer.write(", ");
            }
            writer.write("],\n");

            // Save face edge IDs
            writer.write("  \"faceEdgeId\": [\n");
            for (int f = 0; f < 4; f++) {
                writer.write("    [");
                for (int i = 0; i < 3; i++) {
                    writer.write(String.valueOf(faceEdgeId[f][i]));
                    if (i < 2) writer.write(", ");
                }
                writer.write("]");
                if (f < 3) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // Save face edge orientations
            writer.write("  \"faceEdgeOri\": [\n");
            for (int f = 0; f < 4; f++) {
                writer.write("    [");
                for (int i = 0; i < 3; i++) {
                    writer.write(String.valueOf(faceEdgeOri[f][i]));
                    if (i < 2) writer.write(", ");
                }
                writer.write("]");
                if (f < 3) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ],\n");

            // Save move history
            writer.write("  \"history\": [");
            int idx = 0;
            for (Move m : history) {
                writer.write("\"" + m.name() + "\"");
                if (idx < history.size() - 1) writer.write(", ");
                idx++;
            }
            writer.write("]\n");

            writer.write("}\n");
        }
    }

    // Load state from JSON file
    public void loadFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line.trim());
            }

            parseJSON(json.toString());
        }
    }

    private void parseJSON(String json) {
        // Extract each field value using indexOf
        int facesStart = json.indexOf("\"faces\":");
        int tipOriStart = json.indexOf("\"tipOri\":");
        int centerOriStart = json.indexOf("\"centerOri\":");
        int edgeOriStart = json.indexOf("\"edgeOri\":");
        int faceEdgeIdStart = json.indexOf("\"faceEdgeId\":");
        int faceEdgeOriStart = json.indexOf("\"faceEdgeOri\":");
        int historyStart = json.indexOf("\"history\":");

        // Parse faces
        if (facesStart != -1) {
            int start = json.indexOf('[', facesStart);
            int end = findMatchingBracket(json, start);
            parseFaces(json.substring(start, end + 1));
        }

        // Parse tipOri
        if (tipOriStart != -1) {
            int start = json.indexOf('[', tipOriStart);
            int end = json.indexOf(']', start);
            parseIntArray(json.substring(start + 1, end), tipOri);
        }

        // Parse centerOri
        if (centerOriStart != -1) {
            int start = json.indexOf('[', centerOriStart);
            int end = json.indexOf(']', start);
            parseIntArray(json.substring(start + 1, end), centerOri);
        }

        // Parse edgeOri
        if (edgeOriStart != -1) {
            int start = json.indexOf('[', edgeOriStart);
            int end = json.indexOf(']', start);
            parseIntArray(json.substring(start + 1, end), edgeOri);
        }

        // Parse faceEdgeId
        if (faceEdgeIdStart != -1) {
            int start = json.indexOf('[', faceEdgeIdStart);
            int end = findMatchingBracket(json, start);
            parse2DIntArray(json.substring(start, end + 1), faceEdgeId);
        }

        // Parse faceEdgeOri
        if (faceEdgeOriStart != -1) {
            int start = json.indexOf('[', faceEdgeOriStart);
            int end = findMatchingBracket(json, start);
            parse2DIntArray(json.substring(start, end + 1), faceEdgeOri);
        }

        // Parse history
        if (historyStart != -1) {
            int start = json.indexOf('[', historyStart);
            int end = json.indexOf(']', start);
            if (start != -1 && end != -1) {
                parseHistory(json.substring(start + 1, end));
            }
        }

        redoStack.clear();
    }

    private int findMatchingBracket(String json, int openBracket) {
        int depth = 0;
        for (int i = openBracket; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            else if (json.charAt(i) == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return json.length() - 1;
    }

    private void parseFaces(String json) {
        json = json.substring(1, json.length() - 1); // Remove outer brackets
        String[] faceArrays = json.split("\\],\\s*\\[");

        for (int f = 0; f < 4 && f < faceArrays.length; f++) {
            String faceStr = faceArrays[f].replaceAll("[\\[\\]]", "");
            String[] colors = faceStr.split(",\\s*");

            for (int i = 0; i < 9 && i < colors.length; i++) {
                String colorName = colors[i].replaceAll("\"", "").trim();
                faces[f][i] = Color4.valueOf(colorName);
            }
        }
    }

    private void parseIntArray(String json, int[] arr) {
        String[] values = json.split(",\\s*");
        for (int i = 0; i < arr.length && i < values.length; i++) {
            arr[i] = Integer.parseInt(values[i].trim());
        }
    }

    private void parse2DIntArray(String json, int[][] arr) {
        json = json.substring(1, json.length() - 1); // Remove outer brackets
        String[] rows = json.split("\\],\\s*\\[");

        for (int i = 0; i < arr.length && i < rows.length; i++) {
            String row = rows[i].replaceAll("[\\[\\]]", "");
            String[] values = row.split(",\\s*");
            for (int j = 0; j < arr[i].length && j < values.length; j++) {
                arr[i][j] = Integer.parseInt(values[j].trim());
            }
        }
    }

    private void parseHistory(String json) {
        history.clear();
        if (json.trim().isEmpty()) return;

        String[] moves = json.split(",\\s*");
        for (String moveStr : moves) {
            moveStr = moveStr.replaceAll("\"", "").trim();
            if (!moveStr.isEmpty()) {
                history.push(Move.valueOf(moveStr));
            }
        }
    }

    public void scramble(int n) {
        Move[] moves = Move.values();
        for (int i = 0; i < n; i++) {
            apply(moves[rnd(moves.length)]);
        }
    }

    public void apply(Move m) {
        apply(m, false);
    }

    public void apply(Move m, boolean tipOnly) {
        if (tipOnly) {
            // Tip-only moves: just rotate the tip, no layer movement
            switch (m) {
                case R -> rTip();
                case R_PRIME -> { rTip(); rTip(); }
                case L -> lTip();
                case L_PRIME -> { lTip(); lTip(); }
                case U -> uTip();
                case U_PRIME -> { uTip(); uTip(); }
                case B -> bTip();
                case B_PRIME -> { bTip(); bTip(); }
            }
        } else {
            // Normal moves with full layer rotation
            switch (m) {
                case R -> rCW();
                case R_PRIME -> { rCW(); rCW(); }
                case L -> lCW();
                case L_PRIME -> { lCW(); lCW(); }
                case U -> uCW();
                case U_PRIME -> { uCW(); uCW(); }
                case B -> bCW();
                case B_PRIME -> { bCW(); bCW(); }
            }
        }
        history.push(m);
        tipOnlyHistory.push(tipOnly);
        redoStack.clear();
        tipOnlyRedo.clear();
    }

    public void apply(String sequence) {
        List<Move> seq = Move.parseSequence(sequence);
        for (Move m : seq) apply(m);
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) return;

        Move lastMove = history.pop();
        boolean wasTipOnly = tipOnlyHistory.pop();

        redoStack.push(lastMove);
        tipOnlyRedo.push(wasTipOnly);

        Move inverse = getInverse(lastMove);
        applyWithoutHistory(inverse, wasTipOnly);
    }

    public void redo() {
        if (!canRedo()) return;

        Move moveToRedo = redoStack.pop();
        boolean wasTipOnly = tipOnlyRedo.pop();

        history.push(moveToRedo);
        tipOnlyHistory.push(wasTipOnly);

        applyWithoutHistory(moveToRedo, wasTipOnly);
    }

    Move getInverse(Move m) {
        return switch (m) {
            case R -> Move.R_PRIME;
            case R_PRIME -> Move.R;
            case L -> Move.L_PRIME;
            case L_PRIME -> Move.L;
            case U -> Move.U_PRIME;
            case U_PRIME -> Move.U;
            case B -> Move.B_PRIME;
            case B_PRIME -> Move.B;
        };
    }

    private void applyWithoutHistory(Move m, boolean tipOnly) {
        if (tipOnly) {
            // Tip-only moves: just rotate the tip, no layer movement
            switch (m) {
                case R -> rTip();
                case R_PRIME -> { rTip(); rTip(); }
                case L -> lTip();
                case L_PRIME -> { lTip(); lTip(); }
                case U -> uTip();
                case U_PRIME -> { uTip(); uTip(); }
                case B -> bTip();
                case B_PRIME -> { bTip(); bTip(); }
            }
        } else {
            switch (m) {
                case R -> rCW();
                case R_PRIME -> { rCW(); rCW(); }
                case L -> lCW();
                case L_PRIME -> { lCW(); lCW(); }
                case U -> uCW();
                case U_PRIME -> { uCW(); uCW(); }
                case B -> bCW();
                case B_PRIME -> { bCW(); bCW(); }
            }
        }
    }

    public String getMoveHistory() {
        StringBuilder sb = new StringBuilder();
        for (Move m : history) {
            sb.append(m.toString().replace("_PRIME", "'")).append(" ");
        }
        return sb.toString().trim();
    }

    public int getMoveCount() {
        return history.size();
    }

    public void clearHistory() {
        history.clear();
        redoStack.clear();
        tipOnlyHistory.clear();
        tipOnlyRedo.clear();
    }

    public boolean tipsSolved() {
        for (int t : tipOri) if (t != 0) return false;
        return true;
    }

    public boolean centersSolved() {
        for (int c : centerOri) if (c != 0) return false;
        return true;
    }

    public boolean firstLayerEdgesSolved() {
        return faceEdgeId[0][0] == 0 && faceEdgeOri[0][0] == 0
                && faceEdgeId[0][1] == 1 && faceEdgeOri[0][1] == 0
                && faceEdgeId[0][2] == 2 && faceEdgeOri[0][2] == 0;
    }

    public boolean isSolved() {
        if (!tipsSolved() || !centersSolved()) return false;

        int[][] solvedIds = {
                {0,1,2}, {0,3,4}, {1,3,5}, {2,4,5}
        };

        for (int f = 0; f < 4; f++) {
            for (int s = 0; s < 3; s++) {
                if (faceEdgeId[f][s] != solvedIds[f][s]) return false;
                if (faceEdgeOri[f][s] != 0) return false;
            }
        }

        return true;
    }

    private void rotateTip(int face) { tipOri[face] = (tipOri[face] + 1) % 3; }
    private void rotateCenter(int face) { centerOri[face] = (centerOri[face] + 1) % 3;}

    private void rCW() {
        rotateTip(1);
        rotateCenter(1);

        cycle3Edges(
                1, 0,
                3, 2,
                0, 1
        );
        Color4 temp;

        temp = faces[1][5];
        faces[1][5] = faces[3][3];
        faces[3][3] = faces[0][5];
        faces[0][5] = temp;

        temp = faces[1][2];
        faces[1][2] = faces[3][1];
        faces[3][1] = faces[0][2];
        faces[0][2] = temp;

        temp = faces[1][4];
        faces[1][4] = faces[3][4];
        faces[3][4] = faces[0][4];
        faces[0][4] = temp;

        temp = faces[1][8];
        faces[1][8] = faces[3][7];
        faces[3][7] = faces[0][8];
        faces[0][8] = temp;
    }

    private void rTip(){
        rotateTip(1);
        Color4 temp;

        temp = faces[1][5];
        faces[1][5] = faces[3][3];
        faces[3][3] = faces[0][5];
        faces[0][5] = temp;
    }

    private void lCW() {
        rotateTip(2);
        rotateCenter(2);
        cycle3Edges(
                2, 0,
                0, 2,
                3, 1
        );

        Color4 temp;

        temp = faces[1][3];
        faces[1][3] = faces[2][5];
        faces[2][5] = faces[3][5];
        faces[3][5] = temp;

        temp = faces[1][1];
        faces[1][1] = faces[2][4];
        faces[2][4] = faces[3][4];
        faces[3][4] = temp;

        temp = faces[1][7];
        faces[1][7] = faces[2][8];
        faces[2][8] = faces[3][8];
        faces[3][8] = temp;

        temp = faces[1][4];
        faces[1][4] = faces[2][2];
        faces[2][2] = faces[3][2];
        faces[3][2] = temp;
    }

    private void lTip(){
        Color4 temp;

        temp = faces[1][3];
        faces[1][3] = faces[2][5];
        faces[2][5] = faces[3][5];
        faces[3][5] = temp;
    }

    private void uCW() {
        rotateTip(0);
        rotateCenter(0);
        cycle3Edges(
                0, 0,
                1, 2,
                2, 1
        );

        Color4 temp;

        temp = faces[1][0];
        faces[1][0] = faces[2][0];
        faces[2][0] = faces[0][0];
        faces[0][0] = temp;

        temp = faces[1][6];
        faces[1][6] = faces[2][6];
        faces[2][6] = faces[0][6];
        faces[0][6] = temp;

        temp = faces[1][1];
        faces[1][1] = faces[2][1];
        faces[2][1] = faces[0][1];
        faces[0][1] = temp;

        temp = faces[1][2];
        faces[1][2] =  faces[2][2];
        faces[2][2] = faces[0][2];
        faces[0][2] = temp;
    }

    private void uTip(){
        Color4 temp;

        temp = faces[1][0];
        faces[1][0] = faces[2][0];
        faces[2][0] = faces[0][0];
        faces[0][0] = temp;
    }

    private void bCW() {
        rotateTip(3);
        rotateCenter(3);
        cycle3Edges(
                0, 0,
                1, 2,
                2, 1
        );
        Color4 temp;

        temp = faces[0][3];
        faces[0][3] = faces[3][0];
        faces[3][0] = faces[2][3];
        faces[2][3] = temp;

        temp = faces[0][7];
        faces[0][7] = faces[3][6];
        faces[3][6] = faces[2][7];
        faces[2][7] = temp;

        temp = faces[0][1];
        faces[0][1] = faces[3][1];
        faces[3][1] = faces[2][4];
        faces[2][4] = temp;

        temp = faces[0][4];
        faces[0][4] = faces[3][2];
        faces[3][2] = faces[2][1];
        faces[2][1] = temp;
    }

    private void bTip(){
        Color4 temp;

        temp = faces[0][3];
        faces[0][3] = faces[3][0];
        faces[3][0] = faces[2][3];
        faces[2][3] = temp;
    }

    private void cycle3Edges(int fA, int sA, int fB, int sB, int fC, int sC) {
        int idA = faceEdgeId[fA][sA];
        int idB = faceEdgeId[fB][sB];
        int idC = faceEdgeId[fC][sC];

        // A -> B
        faceEdgeId[fB][sB] = idA;

        // B -> C
        faceEdgeId[fC][sC] = idB;

        // C -> A
        faceEdgeId[fA][sA] = idC;
    }

    public void solveTips() {
        Arrays.fill(tipOri, 0);
    }

    public void solveCenters() {
        Arrays.fill(centerOri, 0);
    }

    public void solveEdges() {
        if (faceEdgeOri[0][0] == 1 || faceEdgeOri[0][1] == 1 || faceEdgeOri[0][2] == 1) {
            apply("R U R'");
        }

        if (!firstLayerEdgesSolved()) {
            apply("U' L' U L");
        }

        if (!firstLayerEdgesSolved()) {
            apply("U R U' R'");
        }

        if (!firstLayerEdgesSolved()) {
            apply("U' L' U L");
        }

        if (!firstLayerEdgesSolved()) {
            apply("U R U' R'");
        }

        if (!firstLayerEdgesSolved()) {
            faceEdgeId[0][0] = 0; faceEdgeOri[0][0] = 0;
            faceEdgeId[0][1] = 1; faceEdgeOri[0][1] = 0;
            faceEdgeId[0][2] = 2; faceEdgeOri[0][2] = 0;
        }
    }

    public void solveSecondLayer() {
        clockwiseCycle();
        if (isSolved()) return;
        antiClockwiseCycle();
        if (isSolved()) return;
        rightBlocks();
        if (isSolved()) return;
        leftBlocks();
        if (isSolved()) return;
        flipCase();
    }

    public void clockwiseCycle() {
        apply("R' U' R U' R' U' R");
    }

    public void antiClockwiseCycle() {
        apply("L U L' U L U L'");
    }

    public void rightBlocks() {
        apply("L R U R' U' L'");
    }

    public void leftBlocks() {
        apply("L U R U' R' L");
    }

    public void flipCase() {
        apply("R' L R L' U L' U' L");
    }

    public String faceSummary(int face) {
        return "Face " + face + " (" + FACE_COLOR[face].shortName() + ") " +
                "[tip=" + tipOri[face] + ", ctr=" + centerOri[face] + "] " +
                "edges=" + Arrays.toString(faceEdgeId[face]);
    }

    public String status() {
        return "Tips " + (tipsSolved() ? "OK" : "✗") +
                " | Centers " + (centersSolved() ? "OK" : "✗") +
                " | FirstLayerEdges " + (firstLayerEdgesSolved() ? "OK" : "✗") +
                " | Solved " + (isSolved() ? "✓" : "✗");
    }
}