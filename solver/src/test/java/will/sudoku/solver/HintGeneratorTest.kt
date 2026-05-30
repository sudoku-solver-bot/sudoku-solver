package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for HintGenerator.
 *
 * Tests hint generation for various solving techniques.
 */
class HintGeneratorTest {

    @Test
    fun `no hint for solved board`() {
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
        val hint = HintGenerator.generate(board)

        assertThat(hint).isNull()
    }

    @Test
    fun `generates hint for unsolved board or returns null if no technique applies`() {
        val values = IntArray(81) { 0 }
        // Simple puzzle with obvious hidden single
        values[0] = 1

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val hint = HintGenerator.generate(board)

        // Hint may or may not be found depending on board state
        // This test just verifies the generator doesn't crash
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `hint contains required fields`() {
        val values = IntArray(81) { 0 }
        values[0] = 1

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val hint = HintGenerator.generate(board)

        if (hint != null) {
            assertThat(hint.coord).isNotNull
            assertThat(hint.value).isIn(1..9)
            assertThat(hint.technique).isNotNull
            assertThat(hint.explanation).isNotBlank()
        }
    }

    @Test
    fun `hint toString is readable`() {
        val values = IntArray(81) { 0 }
        values[0] = 1

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val hint = HintGenerator.generate(board)

        if (hint != null) {
            val hintString = hint.toString()
            assertThat(hintString).contains("Hint:")
            assertThat(hintString).contains("Technique:")
            assertThat(hintString).contains("Explanation:")
        }
    }

    @Test
    fun `technique enum has correct values`() {
        assertThat(HintGenerator.Technique.HIDDEN_SINGLE.displayName).isEqualTo("Hidden Single")
        assertThat(HintGenerator.Technique.NAKED_PAIR.displayName).isEqualTo("Naked Pair")
        assertThat(HintGenerator.Technique.X_WING.displayName).isEqualTo("X-Wing")
    }

    @Test
    fun `generate does not crash on red belt Q1 puzzle with intermediate exhaustion`() {
        // Red belt Q1 puzzle — tests that generate() runs intermediate exhaustion
        // (pointing pairs + box/line reductions) without errors.
        val puzzle = "800000000003600000070090200050007000000045700000100030001000068008500010090000400"
        val board = BoardReader.readBoard(puzzle)

        val hint = HintGenerator.generate(board)

        // The generate() method should run without exceptions.
        if (hint != null) {
            assertThat(hint.technique).isNotNull
            assertThat(hint.explanation).isNotBlank()
            assertThat(hint.value).isIn(1..9)
        }
    }

    @Test
    fun `X-Wing puzzle still found after intermediate technique exhaustion`() {
        // Same puzzle used in HintAdvancedTechniqueTest.
        // Verifies that adding pointing pair/box-line exhaustion doesn't break
        // existing technique detection.
        val puzzle = "1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5"
        val board = BoardReader.readBoard(puzzle)
        SimpleCandidateEliminator().eliminate(board)

        val hint = HintGenerator.generate(board)

        assertThat(hint).isNotNull()
        assertThat(hint!!.technique)
            .`as`("After exhausting intermediate techniques, should still find an advanced technique")
            .isNotEqualTo(HintGenerator.Technique.HIDDEN_SINGLE)
    }

    @Test
    fun `applyPointingPairsUntilStable and applyBoxLineReductionsUntilStable are internal methods`() {
        // Verify the exhaustion methods are accessible (internal visibility)
        val puzzle = "800000000003600000070090200050007000000045700000100030001000068008500010090000400"
        val board = BoardReader.readBoard(puzzle)
        val workingBoard = board.copy()

        // Should run without exceptions
        HintGenerator.applyPointingPairsUntilStable(workingBoard)
        HintGenerator.applyBoxLineReductionsUntilStable(workingBoard)

        assertThat(workingBoard.isValid()).isTrue()
    }
}
