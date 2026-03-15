package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for DifficultyRater.
 *
 * Tests that puzzle difficulty is correctly rated based on
 * the solving techniques required.
 */
class DifficultyRaterTest {

    @Test
    fun `rates easy puzzle correctly`() {
        // Easy puzzle: only simple elimination needed
        // No eliminations from advanced techniques
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 1000000,
            backtrackingCount = 0,
            propagationPasses = 5,
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(10, 3, 500)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.EASY)
        assertThat(rating.backtracking).isFalse()
    }

    @Test
    fun `rates medium puzzle with hidden singles`() {
        // Medium puzzle: hidden singles required
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 2000000,
            backtrackingCount = 0,
            propagationPasses = 10,
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(10, 3, 500),
                "ExclusionCandidateEliminator" to EliminatorMetrics(5, 2, 300)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.MEDIUM)
        assertThat(rating.techniquesUsed).contains("hidden singles")
        assertThat(rating.backtracking).isFalse()
    }

    @Test
    fun `rates hard puzzle with subsets`() {
        // Hard puzzle: naked/hidden subsets required
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 5000000,
            backtrackingCount = 0,
            propagationPasses = 20,
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(15, 5, 800),
                "ExclusionCandidateEliminator" to EliminatorMetrics(5, 2, 400),
                "GroupCandidateEliminator" to EliminatorMetrics(3, 2, 300)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.HARD)
        assertThat(rating.techniquesUsed).contains("naked subsets")
        assertThat(rating.backtracking).isFalse()
    }

    @Test
    fun `rates expert puzzle with X-Wing`() {
        // Expert puzzle: X-Wing required
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 10000000,
            backtrackingCount = 0,
            propagationPasses = 30,
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(20, 8, 1000),
                "ExclusionCandidateEliminator" to EliminatorMetrics(8, 4, 600),
                "XWingCandidateEliminator" to EliminatorMetrics(2, 1, 400)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.EXPERT)
        assertThat(rating.techniquesUsed).contains("X-Wing")
        assertThat(rating.backtracking).isFalse()
    }

    @Test
    fun `rates master puzzle with backtracking`() {
        // Master puzzle: backtracking required
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 50000000,
            backtrackingCount = 5,
            maxRecursionDepth = 10,
            propagationPasses = 100,
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(50, 20, 5000),
                "ExclusionCandidateEliminator" to EliminatorMetrics(20, 10, 3000)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.MASTER)
        assertThat(rating.backtracking).isTrue()
    }

    @Test
    fun `rateLevel returns correct level`() {
        val easyMetrics = SolverMetrics(eliminatorMetrics = emptyMap())
        assertThat(DifficultyRater.rateLevel(easyMetrics)).isEqualTo(DifficultyRater.Level.EASY)

        val masterMetrics = SolverMetrics(backtrackingCount = 1)
        assertThat(DifficultyRater.rateLevel(masterMetrics)).isEqualTo(DifficultyRater.Level.MASTER)
    }

    @Test
    fun `isHard returns true for hard puzzles`() {
        val easyMetrics = SolverMetrics(eliminatorMetrics = emptyMap())
        assertThat(DifficultyRater.isHard(easyMetrics)).isFalse()

        val hardMetrics = SolverMetrics(
            eliminatorMetrics = mapOf(
                "GroupCandidateEliminator" to EliminatorMetrics(3, 2, 300)
            )
        )
        assertThat(DifficultyRater.isHard(hardMetrics)).isTrue()

        val masterMetrics = SolverMetrics(backtrackingCount = 1)
        assertThat(DifficultyRater.isHard(masterMetrics)).isTrue()
    }

    @Test
    fun `rating toString includes technique info`() {
        val metrics = SolverMetrics(
            eliminatorMetrics = mapOf(
                "ExclusionCandidateEliminator" to EliminatorMetrics(5, 2, 300)
            )
        )

        val rating = DifficultyRater.rate(metrics)
        val ratingString = rating.toString()

        // Should contain "Medium" and mention hidden singles
        assertThat(rating.level).isEqualTo(DifficultyRater.Level.MEDIUM)
        assertThat(rating.techniquesUsed).contains("hidden singles")
    }

    @Test
    fun `easy rating has no technique info`() {
        val metrics = SolverMetrics(
            eliminatorMetrics = mapOf(
                "SimpleCandidateEliminator" to EliminatorMetrics(10, 3, 500)
            )
        )

        val rating = DifficultyRater.rate(metrics)

        assertThat(rating.level).isEqualTo(DifficultyRater.Level.EASY)
        assertThat(rating.techniquesUsed).isEmpty()
    }

    @Test
    fun `level enum has correct values`() {
        assertThat(DifficultyRater.Level.EASY.value).isEqualTo(1)
        assertThat(DifficultyRater.Level.MEDIUM.value).isEqualTo(2)
        assertThat(DifficultyRater.Level.HARD.value).isEqualTo(3)
        assertThat(DifficultyRater.Level.EXPERT.value).isEqualTo(4)
        assertThat(DifficultyRater.Level.MASTER.value).isEqualTo(5)
    }
}
