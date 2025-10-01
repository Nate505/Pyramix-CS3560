package model;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;

import static model.Move.*;
import static model.Utils.rnd;

/**
 * Student-friendly Pyraminx model (updated with undo/redo).
 * - 4 faces: 0:Y, 1:R, 2:G, 3:B
 * - 6 global edges (ids 0..5), each has orientation 0/1
 * - Each face holds 3 "edge slots" pointing to global edges (id + ori)
 * - R, L, U moves permute tips, centers (orientations), and local 3-edge cycles
 */
public class Pyraminx {
    /**Face -> Color Mapping*/
    public static final Color4[] FACE_COLOR = {
            Color4.YELLOW, Color4.RED, Color4.GREEN, Color4.BLUE
    };

    /**Tip orientation per face (0,2); solved => all 0*/
    private final int[] tipOri = new int[4];

    /**Center orientation per face (0, 2); solved => all 0 aligned*/
    private final int [] centerOri = new int[4];

    /**Global Edge Count*/
    private static final int EDGE_COUNT = 6;

    /**Edge orientation (0 or 1) by id*/
    private final int[] edgeOri = new int[EDGE_COUNT];

    /**
     * For each face, we store 3 local edge slots (indices 0..2).
     * Each slot stores a global edge id.
     */
    private final int[][] faceEdgeId = new int[4][3];

    /**Each local view also needs the local orientation (0/1) of that global edge */
    private final int[][] faceEdgeOri = new int[4][3];

    // Undo/Redo stacks (store moves in the order they were applied)
    private final Deque<Move> history = new ArrayDeque<>();
    private final Deque<Move> redoStack = new ArrayDeque<>();

    public Pyraminx(){
        resetSolved();
    }

    /**set the cube to the solved state */
    public final void resetSolved(){
        Arrays.fill(tipOri, 0);
        Arrays.fill(edgeOri, 0);
        Arrays.fill(centerOri, 0);

        // Global edges: 0..5
        // We assign a consistent solved mapping (face→slots) to make rendering & checks easy
        // Face 0 (Y): edges 0,1,2
        faceEdgeId[0][0] = 0; faceEdgeOri[0][0] = 0;
        faceEdgeId[0][1] = 1; faceEdgeOri[0][1] = 0;
        faceEdgeId[0][2] = 2; faceEdgeOri[0][2] = 0;

        // Face 1 (R): edges 0,3,4
        faceEdgeId[1][0] = 0; faceEdgeOri[1][0] = 0;
        faceEdgeId[1][1] = 3; faceEdgeOri[1][1] = 0;
        faceEdgeId[1][2] = 4; faceEdgeOri[1][2] = 0;

        // Face 2 (G): edges 1,3,5
        faceEdgeId[2][0] = 1; faceEdgeOri[2][0] = 0;
        faceEdgeId[2][1] = 3; faceEdgeOri[2][1] = 0;
        faceEdgeId[2][2] = 5; faceEdgeOri[2][2] = 0;

        // Face 3 (B): edges 2,4,5
        faceEdgeId[3][0] = 2; faceEdgeOri[3][0] = 0;
        faceEdgeId[3][1] = 4; faceEdgeOri[3][1] = 0;
        faceEdgeId[3][2] = 5; faceEdgeOri[3][2] = 0;

        // clear history
        history.clear();
        redoStack.clear();
    }

    /** Scramble with n random face turns. */
    public void scramble(int n) {
        java.util.Random rand = new java.util.Random();
        Move[] moves = Move.values();
        for (int i = 0; i < n; i++) {
            apply(moves[rand.nextInt(moves.length)]);
        }
    }

    /** Apply a single move and record it to history. */
    public void apply(Move m) {
        doApply(m, true);
    }

    /** Internal apply with option to record (used by undo/redo). */
    private void doApply(Move m, boolean record) {
        if (m == null) return;

        // perform actual rotation(s)
        switch (m) {
            case R -> rCW();
            case R_PRIME -> { rCW(); rCW(); rCW(); }
            case L -> lCW();
            case L_PRIME -> { lCW(); lCW(); lCW(); }
            case U -> uCW();
            case U_PRIME -> { uCW(); uCW(); uCW(); }
        }

        if (record) {
            history.push(m);
            // applying a new move invalidates the redo stack
            redoStack.clear();
        }
    }

    /** Apply a sequence (e.g., "U' L' U L"). */
    public void apply(String sequence) {
        List<Move> seq = Move.parseSequence(sequence);
        for (Move m : seq) apply(m);
    }

    /** Undo last move (if any). */
    public void undo() {
        if (history.isEmpty()) return;
        Move last = history.pop();
        Move inv = inverseOf(last);
        // apply inverse without recording (so we don't store the inverse in history)
        doApply(inv, false);
        // store the original move on redo stack so redo re-applies it
        redoStack.push(last);
    }

    /** Redo last undone move (if any). */
    public void redo() {
        if (redoStack.isEmpty()) return;
        Move m = redoStack.pop();
        // reapply and record it to history
        doApply(m, true);
    }

    /** Tips solved if all orientations 0. */
    public boolean tipsSolved() {
        for (int t : tipOri) if (t != 0) return false;
        return true;
    }
    /** Centers solved if all 0. */
    public boolean centersSolved() {
        for (int c : centerOri) if (c != 0) return false;
        return true;
    }
    /** Simplified edge check: in this model, "solved" = local orientations 0 and
     * each face shows the same triplet it had in resetSolved().
     */
    public boolean firstLayerEdgesSolved() {
        return faceEdgeId[0][0] == 0 && faceEdgeOri[0][0] == 0
                && faceEdgeId[0][1] == 1 && faceEdgeOri[0][1] == 0
                && faceEdgeId[0][2] == 2 && faceEdgeOri[0][2] == 0;
    }
    /** Whole cube solved if tips, centers, and face triplets are back to solved template. */
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

    /*==========================================================
     * Internal rotations (clockwise) for R, L, U faces
     *==========================================================*/
    private void rotateTip(int face) { tipOri[face] = (tipOri[face] + 1) % 3; }
    private void rotateCenter(int face) { centerOri[face] = (centerOri[face] + 1) % 3; }

    private void rCW() {
        // "R" acts primarily on Face 1 (RED)
        rotateTip(1);
        rotateCenter(1);
        cycle3Edges(
                1, 0,
                3, 2,
                0, 1,
                true
        );
    }

    private void lCW() {
        // "L" acts on Face 2 (GREEN)
        rotateTip(2);
        rotateCenter(2);
        cycle3Edges(
                2, 0,
                0, 2,
                3, 1,
                true
        );
    }

    private void uCW() {
        // "U" acts on Face 0 (YELLOW)
        rotateTip(0);
        rotateCenter(0);
        cycle3Edges(
                0, 0,
                1, 2,
                2, 1,
                true
        );
    }

    /** Utility: rotate 3 local slots (faceA.slotA → faceB.slotB → faceC.slotC → faceA.slotA).
     * If flipOnMove=true, we toggle local orientations (teaches "edge flip" idea).
     */
    private void cycle3Edges(int fA, int sA, int fB, int sB, int fC, int sC, boolean flipOnMove) {
        int idA = faceEdgeId[fA][sA], oriA = faceEdgeOri[fA][sA];
        int idB = faceEdgeId[fB][sB], oriB = faceEdgeOri[fB][sB];
        int idC = faceEdgeId[fC][sC], oriC = faceEdgeOri[fC][sC];

        // A -> B
        faceEdgeId[fB][sB] = idA;
        faceEdgeOri[fB][sB] = flipOnMove ? (oriA ^ 1) : oriA;
        // B -> C
        faceEdgeId[fC][sC] = idB;
        faceEdgeOri[fC][sC] = flipOnMove ? (oriB ^ 1) : oriB;
        // C -> A
        faceEdgeId[fA][sA] = idC;
        faceEdgeOri[fA][sA] = flipOnMove ? (oriC ^ 1) : oriC;
    }

    /*==========================================================
     * Step-by-step Solving Methods (Algorithms as Functions)
     *==========================================================*/
    public void solveTips() {
        Arrays.fill(tipOri, 0);
    }

    public void solveCenters() {
        Arrays.fill(centerOri, 0);
    }

    public void solveEdges() {
        if (faceEdgeOri[0][0] == 1 || faceEdgeOri[0][1] == 1 || faceEdgeOri[0][2] == 1) {
            apply("R U R'"); // flip case
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

    public void clockwiseCycle() { apply("R' U' R U' R' U' R"); }
    public void antiClockwiseCycle() { apply("L U L' U L U L'"); }
    public void rightBlocks() { apply("L R U R' U' L'"); }
    public void leftBlocks() { apply("L U R U' R' L"); }
    public void flipCase() { apply("R' L R L' U L' U' L"); }

    /*==========================================================
     * Read-only inspection helpers for UI
     *==========================================================*/
    public String faceSummary(int face) {
        return "Face " + face + " (" + FACE_COLOR[face].shortName() + ") " +
                "[tip=" + tipOri[face] + ", ctr=" + centerOri[face] + "] " +
                "edges=" + Arrays.toString(faceEdgeId[face]) +
                " ori=" + Arrays.toString(faceEdgeOri[face]);
    }
    public String status() {
        return "Tips " + (tipsSolved() ? "OK" : "✗") +
                " | Centers " + (centersSolved() ? "OK" : "✗") +
                " | FirstLayerEdges " + (firstLayerEdgesSolved() ? "OK" : "✗") +
                " | Solved " + (isSolved() ? "✓" : "✗");
    }

    /* Helper to compute inverse of a move (enum-level) */
    private Move inverseOf(Move m) {
        return switch (m) {
            case R -> R_PRIME;
            case R_PRIME -> R;
            case L -> L_PRIME;
            case L_PRIME -> L;
            case U -> U_PRIME;
            case U_PRIME -> U;
        };
    }

    // add to Pyraminx.java (public methods)
    public static final int[][] edgeToFace = {
            {0,1}, // edge 0 between face 0 and 1
            {0,2}, // edge 1 between face 0 and 2
            {0,3}, // edge 2 between face 0 and 3
            {1,2}, // edge 3 between face 1 and 2
            {1,3}, // edge 4 between face 1 and 3
            {2,3}  // edge 5 between face 2 and 3
    };

    /** return the global edge id stored at face f, slot s (0..2) */
    public int getFaceEdgeId(int f, int s) { return faceEdgeId[f][s]; }

    /** return the local orientation stored at face f, slot s (0/1) */
    public int getFaceEdgeOri(int f, int s) { return faceEdgeOri[f][s]; }

    /**
     * Return the Color4 to draw on face f for the given local slot (0..2).
     * If faceEdgeOri==0 the sticker visible on this face is that face's color,
     * otherwise it shows the other face color of that edge piece.
     */
    public Color4 stickerColor(int face, int slot) {
        int edgeId = faceEdgeId[face][slot];
        int ori = faceEdgeOri[face][slot];
        int[] faces = edgeToFace[edgeId];
        int other = (faces[0] == face) ? faces[1] : faces[0];
        return ori == 0 ? FACE_COLOR[face] : FACE_COLOR[other];
    }

}
