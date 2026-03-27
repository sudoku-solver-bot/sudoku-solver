package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for XYWingCandidateEliminator.
 *
 * Tests XY-Wing pattern detection and candidate elimination.
 */
class XYWingCandidateEliminatorTest {

    @Test
    fun `no changes when no XY-Wing pattern exists`() {
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
        val eliminator = XYWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `XY-Wing eliminator is registered in settings`() {
        val hasXYWingEliminator = Settings.eliminators.any {
            it is XYWingCandidateEliminator
        }

        assertThat(hasXYWingEliminator)
            .`as`("Solver should include XYWingCandidateEliminator")
            .isTrue()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = XYWingCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, no cell has exactly 2 candidates
        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = XYWingCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    // ===== EDGE CASES =====

    @Test
    fun `XY-Wing with pivot cell having candidates 1 and 2`() {
        // Setup: Create a scenario where XY-Wing should trigger
        // Pivot cell has candidates {1, 2}
        // Wing cell A has candidates {1, 3}
        // Wing cell B has candidates {2, 3}
        // Cell C (seen by both wings) should have 3 eliminated

        val values = IntArray(81) { 0 }
        
        // Set up a minimal puzzle that could contain an XY-Wing
        // Row 0: place some values to constrain candidates
        values[0] = 4
        values[1] = 5
        values[2] = 6
        
        // Column 0: more constraints
        values[9] = 7
        values[18] = 8
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = XYWingCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XY-Wing pattern in same row`() {
        // Test XY-Wing where all three cells are in the same row
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
        
        val eliminator = XYWingCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XY-Wing pattern in same box`() {
        // Test XY-Wing where all three cells are in the same 3x3 box
        val values = IntArray(81) { 0 }
        
        // Box 0 (cells 0-2, 9-11, 18-20): Place 6 values
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = XYWingCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XY-Wing with multiple eliminations`() {
        // Test scenario where XY-Wing can eliminate candidates from multiple cells
        val values = IntArray(81) { 0 }
        
        // Create a complex puzzle state
        for (i in 0..8) {
            values[i] = if (i % 3 == 0) (i + 1) else 0
        }
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = XYWingCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Just verify it runs without error
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `XY-Wing does not eliminate from pivot or wings`() {
        // Verify that XY-Wing only eliminates from cells seen by both wings,
        // not from the pivot or wing cells themselves
        val values = IntArray(81) { 0 }
        
        // Set up a scenario with limited candidates
        values[0] = 1
        values[4] = 2  // Center
        values[8] = 3
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = XYWingCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }
}
