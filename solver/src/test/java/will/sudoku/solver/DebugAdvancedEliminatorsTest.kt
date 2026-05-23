package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Debug test for advanced eliminators.
 */
class DebugAdvancedEliminatorsTest {

    @Test
    fun `test XYZ-Wing with empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        println("Initial board isValid: ${board.isValid()}")

        val eliminator = XYZWingCandidateEliminator()
        eliminator.eliminate(board)

        println("After XYZ-Wing isValid: ${board.isValid()}")
        println("After XYZ-Wing candidatePatterns[0]: ${board.candidatePatterns[0]}")

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `test W-Wing with empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        println("Initial board isValid: ${board.isValid()}")

        val eliminator = WWingCandidateEliminator()
        eliminator.eliminate(board)

        println("After W-Wing isValid: ${board.isValid()}")
        println("After W-Wing candidatePatterns[0]: ${board.candidatePatterns[0]}")

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `test Simple Coloring with empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        println("Initial board isValid: ${board.isValid()}")

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        println("After Simple Coloring isValid: ${board.isValid()}")
        println("After Simple Coloring candidatePatterns[0]: ${board.candidatePatterns[0]}")

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `test each eliminator with simple board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        // Test XYZ-Wing
        val board1 = Board(values)
        SimpleCandidateEliminator().eliminate(board1)
        println("Before XYZ-Wing isValid: ${board1.isValid()}")
        XYZWingCandidateEliminator().eliminate(board1)
        println("After XYZ-Wing isValid: ${board1.isValid()}")
        assertThat(board1.isValid()).isTrue()

        // Test W-Wing
        val board2 = Board(values)
        SimpleCandidateEliminator().eliminate(board2)
        println("Before W-Wing isValid: ${board2.isValid()}")
        WWingCandidateEliminator().eliminate(board2)
        println("After W-Wing isValid: ${board2.isValid()}")
        assertThat(board2.isValid()).isTrue()

        // Test Simple Coloring
        val board3 = Board(values)
        SimpleCandidateEliminator().eliminate(board3)
        println("Before Simple Coloring isValid: ${board3.isValid()}")
        SimpleColoringCandidateEliminator().eliminate(board3)
        println("After Simple Coloring isValid: ${board3.isValid()}")
        assertThat(board3.isValid()).isTrue()
    }
}
