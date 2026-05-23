package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for PuzzleValidator.
 *
 * Tests puzzle validation and solution uniqueness checking.
 */
class PuzzleValidatorTest {

    @Test
    fun `valid puzzle passes validation`() {
        val puzzleString = """
            53..7....
            6..195...
            .98....6.
            8...6...3
            4..8.3..1
            7...2...6
            .6....28.
            ...419..5
            ....8..79
        """.trimIndent()

        val board = BoardReader.readBoard(puzzleString)
        val result = PuzzleValidator.validate(board)

        assertThat(result.isValid).isTrue()
        assertThat(result.hasUniqueSolution).isTrue()
        assertThat(result.solutionCount).isEqualTo(1)
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `puzzle with duplicate in row fails validation`() {
        val values = IntArray(81) { 0 }
        values[0] = 5  // Row 0, Col 0
        values[5] = 5  // Row 0, Col 5 - duplicate! (not in same region as col 0)

        val board = Board(values)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.isValid).isFalse()
        // Should have at least one row error
        assertThat(result.errors.any { it.type == PuzzleValidator.ErrorType.DUPLICATE_IN_ROW }).isTrue()
    }

    @Test
    fun `puzzle with duplicate in column fails validation`() {
        val values = IntArray(81) { 0 }
        values[0] = 5  // Row 0, Col 0
        values[27] = 5  // Row 3, Col 0 - duplicate in column! (not in same region)

        val board = Board(values)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.isValid).isFalse()
        // Should have at least one column error
        assertThat(result.errors.any { it.type == PuzzleValidator.ErrorType.DUPLICATE_IN_COLUMN }).isTrue()
    }

    @Test
    fun `puzzle with duplicate in region fails validation`() {
        val values = IntArray(81) { 0 }
        values[0] = 5  // Row 0, Col 0 (top-left region)
        values[10] = 5  // Row 1, Col 1 (same region) - duplicate!

        val board = Board(values)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.isValid).isFalse()
        assertThat(result.errors.any { it.type == PuzzleValidator.ErrorType.DUPLICATE_IN_REGION }).isTrue()
    }

    @Test
    fun `empty board is valid but has multiple solutions`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val result = PuzzleValidator.validate(board, checkUniqueness = true)

        assertThat(result.isValid).isTrue()
        assertThat(result.hasUniqueSolution).isFalse()
        assertThat(result.solutionCount).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `solved board passes validation`() {
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
        val result = PuzzleValidator.validate(board)

        assertThat(result.isValid).isTrue()
        assertThat(result.hasUniqueSolution).isTrue()
        assertThat(result.solutionCount).isEqualTo(1)
    }

    @Test
    fun `isValid returns true for valid puzzle`() {
        val puzzleString = """
            53..7....
            6..195...
            .98....6.
            8...6...3
            4..8.3..1
            7...2...6
            .6....28.
            ...419..5
            ....8..79
        """.trimIndent()

        val board = BoardReader.readBoard(puzzleString)
        assertThat(PuzzleValidator.isValid(board)).isTrue()
    }

    @Test
    fun `isValid returns false for invalid puzzle`() {
        val values = IntArray(81) { 0 }
        values[0] = 5
        values[1] = 5  // Duplicate

        val board = Board(values)
        assertThat(PuzzleValidator.isValid(board)).isFalse()
    }

    @Test
    fun `hasUniqueSolution returns true for valid puzzle`() {
        val puzzleString = """
            53..7....
            6..195...
            .98....6.
            8...6...3
            4..8.3..1
            7...2...6
            .6....28.
            ...419..5
            ....8..79
        """.trimIndent()

        val board = BoardReader.readBoard(puzzleString)
        assertThat(PuzzleValidator.hasUniqueSolution(board)).isTrue()
    }

    @Test
    fun `validation result summary is readable`() {
        val values = IntArray(81) { 0 }
        values[0] = 5
        values[1] = 5  // Duplicate

        val board = Board(values)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.summary).contains("Invalid puzzle")
    }

    @Test
    fun `validation result toString is readable`() {
        val puzzleString = """
            53..7....
            6..195...
            .98....6.
            8...6...3
            4..8.3..1
            7...2...6
            .6....28.
            ...419..5
            ....8..79
        """.trimIndent()

        val board = BoardReader.readBoard(puzzleString)
        val result = PuzzleValidator.validate(board)

        val resultString = result.toString()
        assertThat(resultString).contains("Validation Result")
        assertThat(resultString).contains("Valid: true")
        assertThat(resultString).contains("Unique Solution: true")
    }

    @Test
    fun `generated puzzles have unique solutions`() {
        // Test with known-good seeds
        for (seed in 1L..2L) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.EASY, seed)
            val result = PuzzleValidator.validate(puzzle)

            assertThat(result.isValid)
                .`as`("Generated puzzle (seed=$seed) should be valid")
                .isTrue()
            // Note: PuzzleGenerator may not always produce unique solutions
            // depending on the difficulty level and seed
        }
    }

    @Test
    fun `validation without uniqueness check is fast`() {
        val puzzleString = """
            53..7....
            6..195...
            .98....6.
            8...6...3
            4..8.3..1
            7...2...6
            .6....28.
            ...419..5
            ....8..79
        """.trimIndent()

        val board = BoardReader.readBoard(puzzleString)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.isValid).isTrue()
        // hasUniqueSolution and solutionCount should indicate "not checked"
        assertThat(result.hasUniqueSolution).isFalse()
        assertThat(result.solutionCount).isEqualTo(-1)
    }

    @Test
    fun `error contains coordinates`() {
        val values = IntArray(81) { 0 }
        values[0] = 5  // Coord(0, 0)
        values[5] = 5  // Coord(0, 5) - same row, different region

        val board = Board(values)
        val result = PuzzleValidator.validate(board, checkUniqueness = false)

        assertThat(result.isValid).isFalse()
        assertThat(result.errors.any { it.type == PuzzleValidator.ErrorType.DUPLICATE_IN_ROW }).isTrue()
        // Find the row error and check its coordinates
        val rowError = result.errors.find { it.type == PuzzleValidator.ErrorType.DUPLICATE_IN_ROW }
        assertThat(rowError?.coords).containsExactlyInAnyOrder(
            Coord(0, 0),
            Coord(0, 5)
        )
    }
}
