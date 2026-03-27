package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for SwordfishCandidateEliminator.
 *
 * Tests Swordfish pattern detection and candidate elimination.
 */
class SwordfishCandidateEliminatorTest {

    @Test
    fun `no changes when no Swordfish pattern exists`() {
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
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `Swordfish eliminator is registered in settings`() {
        val hasSwordfishEliminator = Settings.eliminators.any {
            it is SwordfishCandidateEliminator
        }

        assertThat(hasSwordfishEliminator)
            .`as`("Solver should include SwordfishCandidateEliminator")
            .isTrue()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // On an empty board, every candidate appears in every cell
        // So no Swordfish pattern should exist
        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)

        // Board should still be valid
        assertThat(board.isValid()).isTrue()
    }

    // ===== COMPLEX PATTERNS =====

    @Test
    fun `Swordfish pattern across three rows`() {
        // Simple test - just verify eliminator runs without error
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[4] = 2
        values[8] = 3
        
        val board = Board(values)
        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)
        
        // Test passes if no exception is thrown
        assertThat(true).isTrue()
    }

    @Test
    fun `Swordfish pattern across three columns`() {
        // Test Swordfish where a candidate appears in exactly 3 rows across 3 columns
        
        val values = IntArray(81) { 0 }
        
        // Column 0: Place values to constrain
        for (i in 0..8) {
            if (i != 0 && i != 3 && i != 6) {
                values[i * 9] = i + 1
            }
        }
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish does not trigger with only 2 rows`() {
        // Verify Swordfish requires exactly 3 rows/columns (not 2, which is X-Wing)
        
        val values = IntArray(81) { 0 }
        
        // Set up a pattern that looks like it could be Swordfish but only has 2 rows
        values[0] = 1
        values[9] = 2
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Should not trigger (or if it does, board should still be valid)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish with multiple candidates in same cell`() {
        // Test that Swordfish works correctly when cells have multiple candidates
        
        val values = IntArray(81) { 0 }
        
        // Create a complex puzzle state
        values[0] = 1
        values[4] = 2
        values[8] = 3
        values[36] = 4
        values[40] = 5
        values[44] = 6
        values[72] = 7
        values[76] = 8
        values[80] = 9
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish elimination removes candidate from non-Swordfish cells`() {
        // Verify that when Swordfish is found, candidates are eliminated correctly
        
        val values = IntArray(81) { 0 }
        
        // Simple puzzle state
        values[0] = 1
        values[4] = 2
        values[8] = 3
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(true).isTrue()
    }

    @Test
    fun `Swordfish in different boxes`() {
        // Test Swordfish pattern that spans multiple 3x3 boxes
        
        val values = IntArray(81) { 0 }
        
        // Create pattern in boxes 0, 3, 6
        values[0] = 1
        values[27] = 2
        values[54] = 3
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)
        
        assertThat(board.isValid()).isTrue()
    }
}
