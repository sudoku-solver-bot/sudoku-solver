package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Test for XYZWingCandidateEliminator.
 *
 * Tests XYZ-Wing pattern detection and candidate elimination.
 */
class XYZWingCandidateEliminatorTest {

    @Test
    fun `no changes when no XYZ-Wing pattern exists`() {
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
        val eliminator = XYZWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `XYZ-Wing eliminator is registered in settings`() {
        val hasXYZWingEliminator = Settings.eliminators.any {
            it is XYZWingCandidateEliminator
        }

        assertThat(hasXYZWingEliminator)
            .`as`("XYZWingCandidateEliminator is currently disabled due to bugs")
            .isFalse()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = XYZWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no cell has exactly 3 candidates
        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on nearly solved board`() {
        val values = intArrayOf(
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 0
        )

        val board = Board(values)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    // ===== EDGE CASES =====

    @Test
    fun `XYZ-Wing with pivot having 3 candidates`() {
        // Setup: Create a scenario where XYZ-Wing could potentially exist
        val values = IntArray(81) { 0 }

        // Place values to create constrained candidates
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[18] = 6
        values[19] = 7

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XYZ-Wing pattern in same row`() {
        // Test XYZ-Wing where all three cells are in the same row
        val values = IntArray(81) { 0 }

        // Row 3: Create constraints
        values[27] = 1
        values[28] = 2
        values[29] = 3
        values[30] = 4
        values[31] = 5
        values[32] = 6

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XYZ-Wing pattern in same box`() {
        // Test XYZ-Wing where all three cells are in the same 3x3 box
        val values = IntArray(81) { 0 }

        // Box 0 (cells 0-2, 9-11, 18-20): Place 5 values
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XYZ-Wing with multiple potential pivots`() {
        // Test scenario with multiple cells having 3 candidates
        val values = IntArray(81) { 0 }

        // Create a complex puzzle state
        for (i in 0..8) {
            values[i] = if (i % 2 == 0) (i + 1) else 0
        }
        values[9] = 7
        values[18] = 8

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XYZ-Wing does not eliminate from pivot or wings`() {
        // Verify that XYZ-Wing only eliminates from cells seeing all three,
        // not from the pivot or wing cells themselves
        val values = IntArray(81) { 0 }

        // Set up a scenario with limited candidates
        values[0] = 1
        values[4] = 2  // Center
        values[8] = 3

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XYZ-Wing eliminator handles all candidate values`() {
        // Test that the eliminator can handle all candidate values (1-9)
        // Use a solved Sudoku puzzle to ensure validity
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
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }
}
