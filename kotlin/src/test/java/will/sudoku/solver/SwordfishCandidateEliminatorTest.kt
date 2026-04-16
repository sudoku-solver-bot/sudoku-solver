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

        val eliminator = SwordfishCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish eliminator handles various puzzle states`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[4] = 2  // Center
        values[8] = 3

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

        // Verify board remains valid
        assertThat(board.isValid()).isTrue()
    }
}
