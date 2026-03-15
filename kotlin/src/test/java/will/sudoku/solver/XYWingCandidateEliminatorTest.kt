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
}
