package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Verify that PuzzleGenerator produces puzzles with unique solutions
 * across all difficulty levels.
 */
class GeneratorUniquenessTest {

    @Test
    fun `easy puzzle has unique solution`() {
        repeat(2) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.EASY)
            assertThat(PuzzleValidator.hasUniqueSolution(puzzle))
                .withFailMessage("Easy puzzle should have unique solution")
                .isTrue()
        }
    }

    @Test
    fun `medium puzzle has unique solution`() {
        repeat(2) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.MEDIUM)
            assertThat(PuzzleValidator.hasUniqueSolution(puzzle))
                .withFailMessage("Medium puzzle should have unique solution")
                .isTrue()
        }
    }

    @Test
    fun `hard puzzle has unique solution`() {
        repeat(2) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.HARD)
            assertThat(PuzzleValidator.hasUniqueSolution(puzzle))
                .withFailMessage("Hard puzzle should have unique solution")
                .isTrue()
        }
    }

    @Test
    fun `expert puzzle has unique solution`() {
        repeat(2) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.EXPERT)
            assertThat(PuzzleValidator.hasUniqueSolution(puzzle))
                .withFailMessage("Expert puzzle should have unique solution")
                .isTrue()
        }
    }

    @Test
    fun `master puzzle has unique solution`() {
        repeat(2) {
            val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.MASTER)
            assertThat(PuzzleValidator.hasUniqueSolution(puzzle))
                .withFailMessage("Master puzzle should have unique solution")
                .isTrue()
        }
    }
}
