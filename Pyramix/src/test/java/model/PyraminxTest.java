package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PyraminxTest {

    private Pyraminx pyraminx;

    @BeforeEach
    void setUp() {
        pyraminx = new Pyraminx();
    }

    @Test
    void testSolvedAtStart() {
        assertTrue(pyraminx.isSolved(), "New puzzle should start solved");
    }

    @Test
    void testApplyMoveAndInverse() {
        pyraminx.apply(Move.R);   // do R
        assertFalse(pyraminx.isSolved(), "After one move, puzzle should not be solved");

        pyraminx.apply(Move.R_PRIME); // undo R
        assertTrue(pyraminx.isSolved(), "Applying inverse should return to solved state");
    }

    @Test
    void testUndoRedo() {
        pyraminx.apply(Move.U);
        pyraminx.apply(Move.L);
        assertFalse(pyraminx.isSolved());

        pyraminx.undo(); // undo L
        pyraminx.undo(); // undo U
        assertTrue(pyraminx.isSolved(), "Undo should restore to solved");

        pyraminx.redo(); // redo U
        pyraminx.redo(); // redo L
        assertFalse(pyraminx.isSolved(), "Redo should reapply moves");
    }


    @Test
    void testScrambleNotSolved() {
        pyraminx.scramble(20);
        assertFalse(pyraminx.isSolved(), "Scramble should leave cube unsolved");
    }

    @Test
    void testReset() {
        pyraminx.scramble(10);
        pyraminx.resetSolved();
        assertTrue(pyraminx.isSolved(), "Reset should return to solved state");
    }

    @Test
    void testTipOnlyMode() {
        pyraminx.apply(Move.R, true); // should only rotate tip
        assertTrue(pyraminx.canUndo(), "Tip move should still be logged");

        pyraminx.undo();
        assertTrue(pyraminx.isSolved(), "Undo should restore solved state");
    }


    @Test
    void testEachMoveHasCorrectInverse() {
        for (Move move : Move.values()) {
            Pyraminx fresh = new Pyraminx();
            fresh.apply(move);
            fresh.apply(fresh.getInverse(move));
            assertTrue(fresh.isSolved(),
                    "Move " + move + " followed by its inverse should solve the puzzle");
        }
    }

    @Test
    void testRedoStackClearedAfterNewMove() {
        pyraminx.apply(Move.R);
        pyraminx.apply(Move.U);
        pyraminx.undo();             // undo U
        assertTrue(pyraminx.canRedo());

        pyraminx.apply(Move.L);      // new move should clear redo stack
        assertFalse(pyraminx.canRedo(), "Redo stack should be cleared after a new move");
    }
}
