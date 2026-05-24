package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for ExclusionCandidateEliminator (hidden single detection).
 *
 * This test specifically verifies the fix for a bug where hidden singles
 * for value 1 were never detected. The bug was:
 *
 *   BEFORE (bug): if (pattern and masks[candidateValue - 1] > 1)
 *   AFTER (fixed): if (pattern and masks[candidateValue - 1] != 0)
 *
 * When candidateValue=1, mask=1, and (pattern and 1) returns 1 when bit is set.
 * The buggy check `1 > 1` was false, so value 1 was never detected as hidden single.
 */
class ExclusionCandidateEliminatorTest {

    /**
     * Test that hidden single detection works for ALL values 1-9.
     *
     * This specifically catches the bug where value 1 (mask=1) was not detected
     * because the check used `> 1` instead of `!= 0`.
     */
    @Test
    fun `hidden single detection works for all values 1 through 9`() {
        // Test each value 1-9 as a hidden single
        for (testValue in 1..9) {
            // Create an empty board (all zeros = all candidates possible)
            val values = IntArray(81) { 0 }

            // Set up a hidden single for `testValue` in row 0 at column 0:
            // - Block `testValue` in columns 1-8 by placing it in row 3 (outside top 3x3 regions)
            // - This way, in row 0, `testValue` can only appear in column 0

            for (col in 1..8) {
                values[3 * 9 + col] = testValue  // Row 3, columns 1-8
            }

            val board = Board(values)

            // First run simple eliminator to remove confirmed values from peers
            // This is necessary because the board constructor doesn't do this automatically
            SimpleCandidateEliminator().eliminate(board)

            // Now run the exclusion eliminator
            val eliminator = ExclusionCandidateEliminator(shortCircuitThreshold = 10)
            val changed = eliminator.eliminate(board)

            // Should have detected the hidden single
            assertThat(changed)
                .withFailMessage("Hidden single detection failed for value $testValue (mask=${1 shl (testValue - 1)})")
                .isTrue()

            // Row 0 col 0 should now be confirmed as `testValue`
            assertThat(board.value(Coord(0, 0)))
                .withFailMessage("Hidden single for value $testValue was not confirmed at (0,0)")
                .isEqualTo(testValue)
        }
    }

    /**
     * Specific regression test for the value 1 bug.
     *
     * The original bug: when checking if a bit is set using `> 1`,
     * value 1 (mask=1) would fail because `1 > 1` is false.
     */
    @Test
    fun `regression test - hidden single for value 1 is detected`() {
        val values = IntArray(81) { 0 }

        // Block value 1 in row 0, columns 1-8 by placing 1 in row 3
        for (col in 1..8) {
            values[3 * 9 + col] = 1
        }

        val board = Board(values)

        // Run simple eliminator first to set up candidates correctly
        SimpleCandidateEliminator().eliminate(board)

        // Now in row 0, value 1 can only go in column 0 (hidden single)

        val eliminator = ExclusionCandidateEliminator(shortCircuitThreshold = 10)
        val changed = eliminator.eliminate(board)

        assertThat(changed).isTrue()
        assertThat(board.value(Coord(0, 0))).isEqualTo(1)
    }
}
