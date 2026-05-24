package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Debug test to find which advanced eliminator causes failures.
 */
class DebugPuzzleGeneratorTest {

    @Test
    fun `test each advanced eliminator individually`() {
        val seed = 1L
        val level = DifficultyRater.Level.EASY

        println("\n=== Testing puzzle with seed=$seed, level=$level ===")
        val puzzle = PuzzleGenerator.generate(level, seed)
        
        val baseEliminators = listOf(
            SimpleCandidateEliminator(),
            GroupCandidateEliminator(),
            HiddenSubsetCandidateEliminator(),
            ExclusionCandidateEliminator(9),
            XWingCandidateEliminator(),
            SwordfishCandidateEliminator(),
            XYWingCandidateEliminator()
        )
        
        val advancedEliminators = listOf(
            "XYZ-Wing" to XYZWingCandidateEliminator(),
            "W-Wing" to WWingCandidateEliminator(),
            "Simple Coloring" to SimpleColoringCandidateEliminator()
        )
        
        // Test with base eliminators only
        println("\nTesting with base eliminators only (7):")
        val baseConfig = SolverConfig(eliminators = baseEliminators)
        val baseSolver = Solver(baseConfig)
        val baseSolution = baseSolver.solve(puzzle)
        println("  Result: ${if (baseSolution == null) "FAILED" else "SUCCESS"}")
        
        // Test each advanced eliminator individually
        for ((name, eliminator) in advancedEliminators) {
            println("\nTesting with base + $name:")
            val config = SolverConfig(eliminators = baseEliminators + eliminator)
            val solver = Solver(config)
            val solution = solver.solve(puzzle)
            println("  Result: ${if (solution == null) "FAILED" else "SUCCESS"}")
        }
        
        // Test with all eliminators
        println("\nTesting with all eliminators (10):")
        val fullConfig = SolverConfig()
        val fullSolver = Solver(fullConfig)
        val fullSolution = fullSolver.solve(puzzle)
        println("  Result: ${if (fullSolution == null) "FAILED" else "SUCCESS"}")
        
        // Assert that base eliminators work
        assertThat(baseSolution)
            .`as`("Base eliminators should solve the puzzle")
            .isNotNull()
    }
}
