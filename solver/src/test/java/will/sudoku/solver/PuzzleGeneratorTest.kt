package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for PuzzleGenerator.
 *
 * Tests puzzle generation and validity.
 */
class PuzzleGeneratorTest {

    /** Solver for verifying generated puzzles — uses basic eliminators
     *  to avoid OOM on resource-constrained CI. A puzzle solvable by
     *  basic techniques is still a valid puzzle. */
    private val verifier = Solver(SolverConfig.basic())

    @Test
    fun `generates solvable puzzle`() {
        val puzzle = PuzzleGenerator.generate()

        // Puzzle should be solvable
        val solution = verifier.solve(puzzle)

        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }

    @Test
    fun `generates puzzle with reproducible seed`() {
        val puzzle1 = PuzzleGenerator.generate(seed = 12345)
        val puzzle2 = PuzzleGenerator.generate(seed = 12345)

        // Same seed should produce same puzzle
        assertThat(puzzle1).isEqualTo(puzzle2)
    }

    @Test
    fun `generates different puzzles with different seeds`() {
        val puzzle1 = PuzzleGenerator.generate(seed = 11111)
        val puzzle2 = PuzzleGenerator.generate(seed = 22222)

        // Different seeds should produce different puzzles (usually)
        assertThat(puzzle1).isNotEqualTo(puzzle2)
    }

    @Test
    fun `generates easy puzzle with more clues`() {
        val easyPuzzle = PuzzleGenerator.generate(DifficultyRater.Level.EASY)

        // Count filled cells
        val filledCount = Coord.all.count { easyPuzzle.isConfirmed(it) }

        // Easy puzzles should have more clues (around 46)
        assertThat(filledCount).isGreaterThanOrEqualTo(40)
    }

    @Test
    fun `generates hard puzzle with fewer clues`() {
        val hardPuzzle = PuzzleGenerator.generate(DifficultyRater.Level.EXPERT)

        // Count filled cells
        val filledCount = Coord.all.count { hardPuzzle.isConfirmed(it) }

        // Expert puzzles should have fewer clues (around 25)
        assertThat(filledCount).isGreaterThanOrEqualTo(20)
    }

    @Test
    fun `generated puzzle is valid`() {
        val puzzle = PuzzleGenerator.generate()

        // Puzzle should be in a valid state
        assertThat(puzzle.isValid()).isTrue()
    }

    @Test
    fun `generated puzzle is not solved`() {
        val puzzle = PuzzleGenerator.generate()

        // Generated puzzle should not be already solved
        assertThat(puzzle.isSolved()).isFalse()
    }

    @Test
    fun `generates minimal puzzle`() {
        // Use fixed seed for deterministic test
        val puzzle = PuzzleGenerator.generateMinimal(seed = 12345)

        // Minimal puzzle should be solvable
        val solution = verifier.solve(puzzle)

        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }
}
