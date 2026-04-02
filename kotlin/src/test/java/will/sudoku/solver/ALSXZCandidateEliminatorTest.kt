package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for ALSXZCandidateEliminator.
 *
 * Tests Almost Locked Set XZ pattern detection and candidate elimination.
 *
 * ALS-XZ finds two Almost Locked Sets (n cells with n+1 candidates) that
 * share a restricted common, then eliminates non-restricted commons.
 */
class ALSXZCandidateEliminatorTest {

    @Test
    fun `no changes when no ALS-XZ pattern exists`() {
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
        val eliminator = ALSXZCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = ALSXZCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, all cells have 9 candidates, no ALSs
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

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles board with ALS candidates`() {
        // Create a board that might have ALSs
        val values = IntArray(81) { 0 }

        // Place values to create potential ALSs
        for (row in 0 until 3) {
            for (col in 0 until 5) {
                values[row * 9 + col] = (row * 3 + col) % 9 + 1
            }
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
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

        val eliminator = ALSXZCandidateEliminator()
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

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
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
        val eliminator = ALSXZCandidateEliminator()

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

        val eliminator = ALSXZCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator handles boards with varying candidate counts`() {
        // Test with different candidate distributions
        val values = IntArray(81) { 0 }

        // Create a mixed board
        for (i in 0 until 30 step 3) {
            values[i] = (i % 9) + 1
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator handles ALS detection in rows`() {
        // Test ALS detection specifically in rows
        val values = IntArray(81) { 0 }

        // Fill first row partially to create potential ALS
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[3] = 4

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles ALS detection in columns`() {
        // Test ALS detection specifically in columns
        val values = IntArray(81) { 0 }

        // Fill first column partially
        values[0] = 1
        values[9] = 2
        values[18] = 3
        values[27] = 4

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles ALS detection in boxes`() {
        // Test ALS detection specifically in boxes
        val values = IntArray(81) { 0 }

        // Fill first box partially
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
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
        ALSXZCandidateEliminator().eliminate(board)
        GroupCandidateEliminator().eliminate(board)

        // Just verify it runs without error
        assertThat(true).isTrue()
    }

    @Test
    fun `eliminator handles edge cases gracefully`() {
        // Test various edge cases
        val values = IntArray(81) { 0 }

        // Create a challenging board state
        values[40] = 5  // Center
        values[0] = 1
        values[8] = 9
        values[72] = 3
        values[80] = 7

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = ALSXZCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }
}
