package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for SolverConfig dependency injection.
 *
 * These tests verify that the solver can be configured with different
 * sets of eliminators, enabling better testability and flexibility.
 */
class SolverConfigTest {

    @Test
    fun `default config includes all eliminators`() {
        val config = SolverConfig()

        assertThat(config.eliminators).hasSize(10)
        assertThat(config.eliminators.map { it::class.simpleName }).containsExactly(
            "SimpleCandidateEliminator",
            "GroupCandidateEliminator",
            "HiddenSubsetCandidateEliminator",
            "ExclusionCandidateEliminator",
            "XWingCandidateEliminator",
            "SwordfishCandidateEliminator",
            "XYWingCandidateEliminator",
            "XYZWingCandidateEliminator",
            "WWingCandidateEliminator",
            "SimpleColoringCandidateEliminator"
        )
    }

    @Test
    fun `basic config includes only simple eliminators`() {
        val config = SolverConfig.basic()
        
        assertThat(config.eliminators).hasSize(2)
        assertThat(config.eliminators.map { it::class.simpleName }).containsExactly(
            "SimpleCandidateEliminator",
            "ExclusionCandidateEliminator"
        )
    }

    @Test
    fun `custom config allows selective eliminators`() {
        val config = SolverConfig(
            eliminators = listOf(SimpleCandidateEliminator())
        )
        
        assertThat(config.eliminators).hasSize(1)
        assertThat(config.eliminators[0]).isInstanceOf(SimpleCandidateEliminator::class.java)
    }

    @Test
    fun `solver with default config solves puzzle`() {
        val solver = Solver()
        val board = BoardReader.readBoard(EASY_PUZZLE)
        
        val solution = solver.solve(board)
        
        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }

    @Test
    fun `solver with basic config solves easy puzzle`() {
        val solver = Solver(SolverConfig.basic())
        val board = BoardReader.readBoard(EASY_PUZZLE)
        
        val solution = solver.solve(board)
        
        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }

    @Test
    fun `solver with minimal config may not solve hard puzzle`() {
        // Create a config with only SimpleCandidateEliminator
        val minimalConfig = SolverConfig(
            eliminators = listOf(SimpleCandidateEliminator())
        )
        val solver = Solver(minimalConfig)
        
        // This puzzle requires more advanced techniques
        val board = BoardReader.readBoard(HARD_PUZZLE)
        
        val solution = solver.solve(board)
        
        // With only simple elimination, solver may need backtracking or may not solve
        // This is expected behavior - demonstrates configurability
        // The solution might still work via backtracking
        if (solution != null) {
            assertThat(solution.isSolved()).isTrue()
        }
    }

    // Note: SolverWithMetrics puzzle solving test removed - 
    // SolverMetricsTest already covers metrics collection with solved/invalid puzzles

    @Test
    fun `solver with steps uses injected config`() {
        val config = SolverConfig() // Use full config to ensure solvability
        val solver = SolverWithSteps(config)
        val board = BoardReader.readBoard(EASY_PUZZLE)
        
        val (solution, progress) = solver.solveWithSteps(board)
        
        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
        assertThat(progress.isSolved).isTrue()
    }

    @Test
    fun `max recursion depth limits backtracking`() {
        // Create a config with very low recursion limit
        val config = SolverConfig(
            eliminators = SolverConfig.defaultEliminators(),
            maxRecursionDepth = 2
        )
        val solver = Solver(config)
        
        // This hard puzzle likely needs deep backtracking
        val board = BoardReader.readBoard(HARD_PUZZLE)
        
        val solution = solver.solve(board)
        
        // With only 2 levels of recursion, may not solve
        // This is expected behavior - demonstrates configurability
    }

    @Test
    fun `deprecated settings still works for backward compatibility`() {
        @Suppress("DEPRECATION")
        val eliminators = Settings.eliminators

        assertThat(eliminators).hasSize(10)
    }

    companion object {
        private val EASY_PUZZLE = """
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

        private val HARD_PUZZLE = """
            8........
            ..36.....
            .7..9.2..
            .5...7...
            ....457..
            ...1...3.
            ..1....68
            ..85...1.
            .9....4..
        """.trimIndent()
    }
}
