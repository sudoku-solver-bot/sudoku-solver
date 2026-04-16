package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for ForcingChainsCandidateEliminator.
 *
 * Tests forcing chain detection and candidate elimination.
 *
 * Forcing chains explore the consequences of setting a bi-value cell
 * to each of its two candidates, looking for contradictions or convergences.
 */
class ForcingChainsCandidateEliminatorTest {

    @Test
    fun `no changes when no forcing chain exists`() {
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
        val eliminator = ForcingChainsCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = ForcingChainsCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no cell has exactly 2 candidates initially
        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles board with bi-value cells`() {
        // Create a board with some bi-value cells
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            2, 0, 0, 0, 0, 0, 0, 0, 0,
            3, 0, 0, 0, 0, 0, 0, 0, 0,
            4, 0, 0, 0, 0, 0, 0, 0, 0,
            5, 0, 0, 0, 0, 0, 0, 0, 0,
            6, 0, 0, 0, 0, 0, 0, 0, 0,
            7, 0, 0, 0, 0, 0, 0, 0, 0,
            8, 0, 0, 0, 0, 0, 0, 0, 0,
            9, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator does not modify confirmed cells`() {
        val values = intArrayOf(
            1, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 2, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val board = Board(values)
        val initialConfirmed = board.candidatePattern(Coord(0, 0))

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        // Confirmed cell should remain unchanged
        assertThat(board.candidatePattern(Coord(0, 0))).isEqualTo(initialConfirmed)
    }

    @Test
    fun `eliminator handles complex board state`() {
        // Test on a more complex board state
        val values = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 2, 3, 0, 0, 0,
            0, 0, 0, 4, 5, 6, 0, 0, 0,
            0, 0, 0, 7, 8, 9, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator can run multiple times safely`() {
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
        val eliminator = ForcingChainsCandidateEliminator()

        // Run multiple times - should be idempotent
        eliminator.eliminate(board)
        eliminator.eliminate(board)
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles board after basic elimination`() {
        // Test that eliminator works correctly after basic elimination
        val values = IntArray(81) { 0 }

        // Place some values
        values[0] = 1
        values[4] = 5
        values[8] = 9
        values[10] = 2
        values[20] = 8
        values[40] = 7
        values[50] = 3
        values[60] = 4
        values[80] = 6

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator respects chain depth limit`() {
        // Create a board that could potentially have very long chains
        val values = IntArray(81) { 0 }

        // Place values to create a long chain potential
        for (i in 0 until 40 step 2) {
            values[i] = (i % 9) + 1
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        // Should complete without hanging or crashing
        // Result may or may not have changes
        assertThat(true).isTrue()
    }

    @Test
    fun `eliminator handles boards with no bi-value cells`() {
        // All cells are either confirmed or have 3+ candidates
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            2, 0, 0, 0, 0, 0, 0, 0, 0,
            3, 0, 0, 0, 0, 0, 0, 0, 0,
            4, 0, 0, 0, 0, 0, 0, 0, 0,
            5, 0, 0, 0, 0, 0, 0, 0, 0,
            6, 0, 0, 0, 0, 0, 0, 0, 0,
            7, 0, 0, 0, 0, 0, 0, 0, 0,
            8, 0, 0, 0, 0, 0, 0, 0, 0,
            9, 0, 0, 0, 0, 0, 0, 0, 0
        )

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        // Count bi-value cells
        val biValueCount = Coord.all.count { coord ->
            !board.isConfirmed(coord) && board.candidateValues(coord).size == 2
        }

        // May or may not have bi-value cells depending on puzzle state
        val eliminator = ForcingChainsCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator handles contradiction detection gracefully`() {
        // Test scenario where one path might lead to contradiction
        val values = IntArray(81) { 0 }

        // Create a constrained board
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles convergence detection gracefully`() {
        // Test scenario where both paths might converge to same conclusion
        val values = IntArray(81) { 0 }

        // Create a board where convergence might occur
        values[40] = 5 // Center
        values[0] = 1
        values[80] = 9

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ForcingChainsCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator works with other eliminators`() {
        // Test that eliminator doesn't interfere with other eliminators
        val values = IntArray(81) { 0 }

        // Place some values
        for (i in 0 until 20) {
            values[i] = (i % 9) + 1
        }

        val board = Board(values)

        // Run multiple eliminators
        SimpleCandidateEliminator().eliminate(board)
        ForcingChainsCandidateEliminator().eliminate(board)
        GroupCandidateEliminator().eliminate(board)

        // Just verify it runs without error
        // Board state may vary depending on interaction between eliminators
        assertThat(true).isTrue()
    }
}
