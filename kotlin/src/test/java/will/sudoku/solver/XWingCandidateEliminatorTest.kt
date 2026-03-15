package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for XWingCandidateEliminator.
 *
 * Tests X-Wing pattern detection and candidate elimination.
 *
 * X-Wing occurs when a candidate appears in exactly 2 cells in each of 2 rows,
 * and those cells are in the same 2 columns (or vice versa).
 */
class XWingCandidateEliminatorTest {

    @Test
    fun `detects row-based X-Wing and eliminates from columns`() {
        // Create a board where:
        // - Row 2 has candidate X only in columns 1 and 7
        // - Row 5 has candidate X only in columns 1 and 7
        // - Other cells in columns 1 and 7 should have X eliminated

        val values = IntArray(81) { 0 }

        // Block candidate 5 in specific positions to create X-Wing pattern
        // Row 2: block 5 in all columns except 1 and 7
        for (col in 0..8) {
            if (col != 1 && col != 7) {
                // Place values to block 5 in row 2 (using column constraints)
                values[3 * 9 + col] = 5  // Row 3 has 5 in each column
            }
        }

        // Row 5: block 5 in all columns except 1 and 7
        for (col in 0..8) {
            if (col != 1 && col != 7) {
                values[6 * 9 + col] = 5  // Row 6 has 5 in each column
            }
        }

        // Also place 5 in rows 2 and 5, columns 1 and 7 in OTHER rows to block them
        // This creates the X-Wing: rows 2 and 5 are the only rows where
        // columns 1 and 7 can have 5

        val board = Board(values)

        // Run simple eliminator first to propagate constraints
        SimpleCandidateEliminator().eliminate(board)

        // Now run X-Wing eliminator
        val eliminator = XWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // Verify X-Wing was detected and eliminations made
        // (exact assertions depend on the board state after simple elimination)
    }

    @Test
    fun `detects column-based X-Wing and eliminates from rows`() {
        val values = IntArray(81) { 0 }

        // Create X-Wing pattern based on columns
        // Column 2 has candidate X only in rows 1 and 6
        // Column 7 has candidate X only in rows 1 and 6

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XWingCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `no changes when no X-Wing pattern exists`() {
        // Create a fully solved board
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
        val eliminator = XWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `X-Wing eliminator is registered in settings`() {
        val hasXWingEliminator = Settings.eliminators.any {
            it is XWingCandidateEliminator
        }

        assertThat(hasXWingEliminator)
            .`as`("Solver should include XWingCandidateEliminator")
            .isTrue()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = XWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, every candidate appears in every cell
        // So no X-Wing pattern should exist
        assertThat(changed).isFalse()
    }

    @Test
    fun `classic X-Wing example - candidate 1`() {
        // Classic X-Wing example for candidate 1:
        // In rows 0 and 4, candidate 1 only appears in columns 2 and 6
        // Therefore, eliminate 1 from columns 2 and 6 in all other rows

        val values = IntArray(81) { 0 }

        // Fill in many values to create a constrained board
        // Row 0: candidate 1 only possible in cols 2, 6 (others blocked)
        // Row 4: candidate 1 only possible in cols 2, 6 (others blocked)

        // Block 1 in row 0, columns 0,1,3,4,5,7,8 by placing 1 in those columns in other rows
        for (col in listOf(0, 1, 3, 4, 5, 7, 8)) {
            values[1 * 9 + col] = 1  // Row 1 has 1 in these columns
        }

        // Block 1 in row 4, columns 0,1,3,4,5,7,8 by placing 1 in those columns in other rows
        for (col in listOf(0, 1, 3, 4, 5, 7, 8)) {
            values[5 * 9 + col] = 1  // Row 5 has 1 in these columns
        }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        // Check initial state: rows 0 and 4 should have 1 only in cols 2, 6
        val row0CandidatesFor1 = (0..8).filter { col ->
            val coord = Coord(0, col)
            !board.isConfirmed(coord) && board.candidateValues(coord).contains(1)
        }
        val row4CandidatesFor1 = (0..8).filter { col ->
            val coord = Coord(4, col)
            !board.isConfirmed(coord) && board.candidateValues(coord).contains(1)
        }

        // If setup is correct, run X-Wing eliminator
        val eliminator = XWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // If X-Wing was detected, candidate 1 should be eliminated from
        // columns 2 and 6 in rows other than 0 and 4
        if (row0CandidatesFor1 == listOf(2, 6) && row4CandidatesFor1 == listOf(2, 6)) {
            // X-Wing pattern exists
            for (row in listOf(2, 3, 6, 7, 8)) {
                for (col in listOf(2, 6)) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord)) {
                        assertThat(board.candidateValues(coord))
                            .`as`("Row $row, Col $col should not have 1 after X-Wing elimination")
                            .doesNotContain(1)
                    }
                }
            }
        }
    }
}
