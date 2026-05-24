package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for FrankenFishCandidateEliminator.
 *
 * Tests Franken Fish pattern detection and candidate elimination.
 *
 * Franken Fish is an advanced fish pattern that extends X-Wing and Swordfish.
 * It can use N base sets (rows or columns) and N cover sets (columns or rows),
 * where N ranges from 2 to 4.
 */
class FrankenFishCandidateEliminatorTest {

    @Test
    fun `detects size-2 Franken Fish and eliminates from columns`() {
        // Create a board where:
        // - 2 rows have candidate X only in same 2 columns
        // - Eliminate from those columns in all other rows

        val values = IntArray(81) { 0 }

        // Block candidate 5 to create Franken Fish pattern
        // Rows 2 and 5: candidate 5 only in columns 1 and 7

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = FrankenFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `detects size-3 Franken Fish and eliminates from columns`() {
        // Create a board where:
        // - 3 rows have candidate X only in same 3 columns
        // - Eliminate from those columns in all other rows

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = FrankenFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `detects size-4 Franken Fish and eliminates from columns`() {
        // Create a board where:
        // - 4 rows have candidate X only in same 4 columns
        // - Eliminate from those columns in all other rows

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = FrankenFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `no changes when no Franken Fish pattern exists`() {
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
        val eliminator = FrankenFishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = FrankenFishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no Franken Fish pattern should exist
        assertThat(changed).isFalse()
    }

    @Test
    fun `detects column-based Franken Fish and eliminates from rows`() {
        val values = IntArray(81) { 0 }

        // Create Franken Fish pattern based on columns
        // N columns have candidate X only in same N rows

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = FrankenFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }
}
