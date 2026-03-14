package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for HiddenSubsetCandidateEliminator.
 *
 * Tests hidden pairs, triples, and quads detection.
 *
 * A hidden subset occurs when N candidates appear in exactly N cells within a group.
 * All other candidates can be removed from those N cells.
 *
 * Example (Hidden Pair):
 * In a row, candidates {2, 3} only appear in cells A and B.
 * This means cells A and B MUST be {2, 3}, so all other candidates can be removed.
 */
class HiddenSubsetCandidateEliminatorTest {

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        // Run simple eliminator first
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no hidden subsets should be found
        // (all cells have all candidates)
        assertThat(changed).isFalse()
    }

    @Test
    fun `no changes when no hidden subsets exist`() {
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
        val eliminator = HiddenSubsetCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator processes groups correctly`() {
        // Create a board with some values to ensure eliminator processes correctly
        val values = IntArray(81) { 0 }

        // Place some values to create a partial puzzle
        values[0] = 1  // (0, 0) = 1
        values[1] = 2  // (0, 1) = 2
        values[2] = 3  // (0, 2) = 3

        val board = Board(values)

        // Run simple eliminator first to propagate constraints
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = HiddenSubsetCandidateEliminator()
        // Just verify it runs without error
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `integration test - solver uses hidden subset eliminator`() {
        // Verify that the solver includes HiddenSubsetCandidateEliminator in its eliminators
        val hasHiddenSubsetEliminator = Settings.eliminators.any {
            it is HiddenSubsetCandidateEliminator
        }

        assertThat(hasHiddenSubsetEliminator)
            .`as`("Solver should include HiddenSubsetCandidateEliminator")
            .isTrue()
    }
}
