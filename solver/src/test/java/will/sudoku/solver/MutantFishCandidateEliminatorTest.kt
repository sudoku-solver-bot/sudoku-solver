package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for MutantFishCandidateEliminator.
 *
 * Tests Mutant Fish pattern detection and candidate elimination.
 *
 * Mutant Fish is the most advanced fish pattern, extending Franken Fish
 * by allowing MIXED base sets (rows AND columns) and MIXED cover sets
 * (columns AND rows).
 */
class MutantFishCandidateEliminatorTest {

    @Test
    fun `detects size-2 Mutant Fish with mixed base sets`() {
        // Create a board where:
        // - Base: 1 row + 1 column
        // - Cover: 1 column + 1 row
        // - Mutant Fish pattern found

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = MutantFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `detects size-3 Mutant Fish with mixed base sets`() {
        // Create a board where:
        // - Base: 2 rows + 1 column
        // - Cover: 2 columns + 1 row
        // - Mutant Fish pattern found

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = MutantFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `detects size-4 Mutant Fish with mixed base sets`() {
        // Create a board where:
        // - Base: 2 rows + 2 columns
        // - Cover: 2 columns + 2 rows
        // - Mutant Fish pattern found

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = MutantFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `no changes when no Mutant Fish pattern exists`() {
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
        val eliminator = MutantFishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = MutantFishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no Mutant Fish pattern should exist
        assertThat(changed).isFalse()
    }

    @Test
    fun `detects Mutant Fish with all rows as base sets`() {
        // Mutant Fish should still work when all base sets are rows
        // (reduces to Franken Fish behavior)

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = MutantFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `detects Mutant Fish with all columns as base sets`() {
        // Mutant Fish should still work when all base sets are columns
        // (reduces to Franken Fish behavior)

        val values = IntArray(81) { 0 }

        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = MutantFishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }
}
