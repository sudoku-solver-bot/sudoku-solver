package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HiddenSubsetCandidateEliminatorTest {

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `integration test - solver uses hidden subset eliminator`() {
        // Verify that the solver includes HiddenSubsetCandidateEliminator in its eliminators
        val hasHiddenSubsetEliminator = Settings.eliminators.any {
            it is HiddenSubsetCandidateEliminator
        }

        assertThat(hasHiddenSubsetEliminator)
            .`as`("Solver should include HiddenSubsetCandidateEliminator")
            .isTrue()
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

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator handles various puzzle states`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[4] = 2  // Center
        values[8] = 3

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `hidden subset eliminator handles all group types`() {
        // Simple test - just verify eliminator runs
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[10] = 2
        values[20] = 3

        val board = Board(values)
        val eliminator = HiddenSubsetCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }
}
