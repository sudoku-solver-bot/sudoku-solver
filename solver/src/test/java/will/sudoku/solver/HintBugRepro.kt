package will.sudoku.solver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class HintBugRepro {

    @Test
    fun `bug 224 - solved board after constraint propagation gets complete hint not Scanning`() {
        val puzzle = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
        val board = BoardReader.readBoard(puzzle)
        SimpleCandidateEliminator().eliminate(board)

        // Board is now fully solved by constraint propagation
        assertTrue(board.isSolved(), "Puzzle should be solvable by naked singles alone")

        val hint = TeachingHintProvider().getHint(board)
        assertNotEquals("Scanning", hint.technique,
            "Solved board should NOT fall back to 'Scanning'")
        assertEquals(HintType.COMPLETE, hint.type,
            "Solved board should get COMPLETE hint type")
    }

    @Test
    fun `bug 224 - unsolved board with hidden singles gets specific technique`() {
        val puzzle = "000000000000003085001020000000507000004000100090000000500000073002010000000040009"
        val board = BoardReader.readBoard(puzzle)
        SimpleCandidateEliminator().eliminate(board)

        // Board is NOT fully solved — there should be hidden singles or other techniques
        val hint = TeachingHintProvider().getHint(board)

        if (!board.isSolved()) {
            assertNotEquals("Scanning", hint.technique,
                "Unsolved board with techniques should get a specific hint, not 'Scanning'")
        }
    }

    @Test
    fun `bug 224 - HintGenerator still returns null for solved board`() {
        // This confirms existing behavior — HintGenerator returns null for solved boards
        val values = intArrayOf(
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 9
        )
        val board = Board(values)
        val hint = HintGenerator.generate(board)

        // HintGenerator should return null for a solved board
        // (TeachingHintProvider handles this at a higher level)
        assertNull(hint, "HintGenerator should return null for a solved board")
    }
}
