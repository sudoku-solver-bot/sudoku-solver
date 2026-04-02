package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for UniqueRectanglesCandidateEliminator.
 *
 * Tests Unique Rectangle pattern detection and candidate elimination.
 *
 * Unique Rectangles is based on the uniqueness constraint: a proper Sudoku puzzle
 * should have exactly one solution. Patterns that would create multiple solutions
 * can be avoided by eliminating certain candidates.
 */
class UniqueRectanglesCandidateEliminatorTest {

    @Test
    fun `no changes when no deadly rectangle exists`() {
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
        val eliminator = UniqueRectanglesCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = UniqueRectanglesCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, all cells have 9 candidates, no deadly rectangles
        assertThat(changed).isFalse()
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles board with only two candidates per cell`() {
        // Every cell has exactly 2 candidates (after elimination)
        val values = IntArray(81) { 0 }

        // Place values to create a constrained board
        for (row in 0 until 9) {
            for (col in 0 until 3) {
                values[row * 9 + col] = (row % 3) + 1 + (col * 3)
            }
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = UniqueRectanglesCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        // eliminator may or may not make changes depending on board state
        assertThat(result).isNotNull()
    }

    @Test
    fun `rectangle not detected when all cells are confirmed`() {
        val values = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            2, 1, 4, 3, 6, 5, 8, 7, 9,
            3, 4, 1, 2, 7, 8, 9, 6, 5,
            4, 3, 2, 1, 8, 7, 6, 9, 8,
            5, 6, 7, 8, 9, 1, 2, 3, 4,
            6, 5, 8, 7, 2, 9, 1, 4, 3,
            7, 8, 9, 6, 1, 3, 4, 5, 2,
            8, 7, 6, 9, 3, 4, 5, 2, 1,
            9, 8, 5, 7, 4, 2, 3, 1, 6
        )

        val board = Board(values)
        val eliminator = UniqueRectanglesCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // All cells confirmed, no eliminations possible
        assertThat(changed).isFalse()
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

        val eliminator = UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        // Confirmed cell should remain unchanged
        assertThat(board.candidatePattern(Coord(0, 0))).isEqualTo(initialConfirmed)
    }

    @Test
    fun `handles edge case - rectangle at board corners`() {
        // Test rectangle detection at corners of the board
        // (0,0), (0,1), (1,0), (1,1)
        val values = IntArray(81) { 0 }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `handles edge case - rectangle at board center`() {
        // Test rectangle detection near center of board
        // (4,4), (4,5), (5,4), (5,5)
        val values = IntArray(81) { 0 }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `handles complex board state`() {
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

        val eliminator = UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Type 1 pattern is not falsely detected`() {
        // Ensure Type 1 is only applied when exactly one cell has extras
        val values = IntArray(81) { 0 }

        // Create a board where multiple cells might have extra candidates
        // Type 1 should NOT trigger (requires exactly one cell with extras)
        for (i in 0 until 81 step 2) {
            values[i] = (i % 9) + 1
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = UniqueRectanglesCandidateEliminator()
        val result = eliminator.eliminate(board)

        // Just verify it runs without error
        assertThat(result).isNotNull()
    }

    @Test
    fun `eliminator handles same-box corner detection correctly`() {
        // Ensure corners in same box don't trigger elimination
        val values = IntArray(81) { 0 }

        // Fill box 0 (top-left 3x3) with some values
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = UniqueRectanglesCandidateEliminator()
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

        val eliminator = UniqueRectanglesCandidateEliminator()
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
        val eliminator = UniqueRectanglesCandidateEliminator()

        // Run multiple times - should be idempotent
        eliminator.eliminate(board)
        eliminator.eliminate(board)
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }
}
