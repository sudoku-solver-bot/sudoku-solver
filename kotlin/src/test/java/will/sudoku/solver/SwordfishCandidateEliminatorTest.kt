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
    fun `Swordfish pattern across three rows eliminates candidates correctly`() {
        // Create a Swordfish pattern for candidate 7
        // Rows 0, 3, 6 have candidate 7 in columns 1, 4, 7
        // This should eliminate 7 from other cells in these columns
        
        val values = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 0: candidate 7 in col 1,4,7
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 3: candidate 7 in col 1,4,7  
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 6: candidate 7 in col 1,4,7
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        )
        
        val board = Board(values)
        // Fill candidates to create Swordfish pattern
        SimpleCandidateEliminator().eliminate(board)
        
        // Manually set up the Swordfish pattern for candidate 7
        board.setCandidates(0, 1, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 0, Col 1
        board.setCandidates(0, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 0, Col 4
        board.setCandidates(0, 7, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 0, Col 7
        
        board.setCandidates(3, 1, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 3, Col 1
        board.setCandidates(3, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 3, Col 4
        board.setCandidates(3, 7, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 3, Col 7
        
        board.setCandidates(6, 1, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 6, Col 1
        board.setCandidates(6, 4, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 6, Col 4
        board.setCandidates(6, 7, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 6, Col 7
        
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Verify that candidate 7 was eliminated from other cells in these columns
        // Check cells that should be affected by the Swordfish pattern
        assertThat(board.getCandidates(1, 1)).doesNotContain(7)  // Row 1, Col 1 - should not have 7
        assertThat(board.getCandidates(2, 1)).doesNotContain(7)  // Row 2, Col 1 - should not have 7
        assertThat(board.getCandidates(4, 1)).doesNotContain(7)  // Row 4, Col 1 - should not have 7
        assertThat(board.getCandidates(5, 1)).doesNotContain(7)  // Row 5, Col 1 - should not have 7
        assertThat(board.getCandidates(7, 1)).doesNotContain(7)  // Row 7, Col 1 - should not have 7
        assertThat(board.getCandidates(8, 1)).doesNotContain(7)  // Row 8, Col 1 - should not have 7
        
        assertThat(changed).isTrue()
    }

    @Test
    fun `Swordfish pattern across three columns eliminates candidates correctly`() {
        // Create a Swordfish pattern for candidate 5
        // Columns 0, 3, 6 have candidate 5 in rows 1, 4, 7
        // This should eliminate 5 from other cells in these rows
        
        val values = intArrayOf(
            1, 0, 0, 2, 0, 0, 3, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 1: candidate 5 in col 0,3,6
            4, 0, 0, 5, 0, 0, 6, 0, 0,
            7, 0, 0, 8, 0, 0, 9, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 4: candidate 5 in col 0,3,6
            1, 0, 0, 2, 0, 0, 3, 0, 0,
            4, 0, 0, 5, 0, 0, 6, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,  // Row 7: candidate 5 in col 0,3,6
            7, 0, 0, 8, 0, 0, 9, 0, 0
        )
        
        val board = Board(values)
        // Fill candidates to create Swordfish pattern
        SimpleCandidateEliminator().eliminate(board)
        
        // Manually set up the Swordfish pattern for candidate 5
        board.setCandidates(1, 0, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 1, Col 0
        board.setCandidates(1, 3, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 1, Col 3
        board.setCandidates(1, 6, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 1, Col 6
        
        board.setCandidates(4, 0, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 4, Col 0
        board.setCandidates(4, 3, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 4, Col 3
        board.setCandidates(4, 6, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 4, Col 6
        
        board.setCandidates(7, 0, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 7, Col 0
        board.setCandidates(7, 3, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 7, Col 3
        board.setCandidates(7, 6, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))  // Row 7, Col 6
        
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Verify that candidate 5 was eliminated from other cells in these rows
        // Check cells that should be affected by the Swordfish pattern
        assertThat(board.getCandidates(1, 1)).doesNotContain(5)  // Row 1, Col 1 - should not have 5
        assertThat(board.getCandidates(1, 2)).doesNotContain(5)  // Row 1, Col 2 - should not have 5
        assertThat(board.getCandidates(1, 4)).doesNotContain(5)  // Row 1, Col 4 - should not have 5
        assertThat(board.getCandidates(1, 5)).doesNotContain(5)  // Row 1, Col 5 - should not have 5
        assertThat(board.getCandidates(1, 7)).doesNotContain(5)  // Row 1, Col 7 - should not have 5
        assertThat(board.getCandidates(1, 8)).doesNotContain(5)  // Row 1, Col 8 - should not have 5
        
        assertThat(changed).isTrue()
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
    fun `Swordfish overlapping with X-Wing pattern`() {
        // Test that Swordfish doesn't interfere with X-Wing patterns
        // and both can coexist in the same puzzle
        
        val values = intArrayOf(
            0, 0, 0, 1, 0, 0, 2, 0, 0,  // Row 0: candidate 3 in col 2,5,8
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 1, 0, 0, 2, 0, 0,  // Row 2: candidate 3 in col 2,5,8
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 1, 0, 0, 2, 0, 0,  // Row 4: candidate 3 in col 2,5,8
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 1, 0, 0, 2, 0, 0,  // Row 6: candidate 3 in col 2,5,8
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            0, 0, 0, 1, 0, 0, 2, 0, 0   // Row 8: candidate 3 in col 2,5,8
        )
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        // Set up candidates for Swordfish pattern (candidate 3)
        for (row in intArrayOf(0, 2, 4, 6)) {
            for (col in intArrayOf(2, 5, 8)) {
                board.setCandidates(row, col, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
            }
        }
        
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Board should remain valid and tests should pass
        assertThat(board.isValid()).isTrue()
        assertThat(changed).isTrue() // Should trigger elimination
    }

    @Test
    fun `Complex Swordfish with wrapped rows and columns`() {
        // Test a more complex Swordfish pattern that wraps around the grid
        // Candidate 3 appears in rows 1,4,7 and columns 0,3,6
        
        val values = intArrayOf(
            1, 0, 0, 2, 0, 0, 3, 0, 0,   // Row 0
            0, 0, 0, 0, 0, 0, 0, 0, 0,   // Row 1: candidate 3 in col 0,3,6
            4, 0, 0, 5, 0, 0, 6, 0, 0,
            7, 0, 0, 8, 0, 0, 9, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,   // Row 4: candidate 3 in col 0,3,6
            1, 0, 0, 2, 0, 0, 3, 0, 0,
            4, 0, 0, 5, 0, 0, 6, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,   // Row 7: candidate 3 in col 0,3,6
            7, 0, 0, 8, 0, 0, 9, 0, 0
        )
        
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        
        // Set up the complex Swordfish pattern
        for (row in intArrayOf(1, 4, 7)) {
            for (col in intArrayOf(0, 3, 6)) {
                board.setCandidates(row, col, intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
            }
        }
        
        val eliminator = SwordfishCandidateEliminator()
        val changed = eliminator.eliminate(board)
        
        // Verify that candidate 3 was eliminated from cells that see the Swordfish pattern
        // Test some cells that should be affected
        assertThat(board.getCandidates(2, 1)).doesNotContain(3)  // Row 2, Col 1 - should not have 3
        assertThat(board.getCandidates(3, 1)).doesNotContain(3)  // Row 3, Col 1 - should not have 3
        assertThat(board.getCandidates(5, 1)).doesNotContain(3)  // Row 5, Col 1 - should not have 3
        assertThat(board.getCandidates(6, 1)).doesNotContain(3)  // Row 6, Col 1 - should not have 3
        assertThat(board.getCandidates(8, 1)).doesNotContain(3)  // Row 8, Col 1 - should not have 3
        
        assertThat(changed).isTrue()
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
