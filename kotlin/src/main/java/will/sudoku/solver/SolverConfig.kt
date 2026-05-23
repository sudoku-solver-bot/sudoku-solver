package will.sudoku.solver

import kotlin.math.sqrt

/**
 * Configuration for Sudoku solver behavior.
 *
 * Allows customization of solving strategy, including which eliminators to use
 * and performance tuning parameters.
 *
 * ## Usage
 *
 * ```kotlin
 * // Use default configuration
 * val solver = Solver()
 *
 * // Use custom configuration
 * val config = SolverConfig(
 *     eliminators = listOf(SimpleCandidateEliminator())
 * )
 * val solver = Solver(config)
 *
 * // Create config with only basic eliminators
 * val basicConfig = SolverConfig.basic()
 * val solver = Solver(basicConfig)
 * ```
 *
 * @property eliminators List of candidate eliminators to apply during solving
 * @property maxRecursionDepth Maximum recursion depth for backtracking (safety limit)
 */
data class SolverConfig(
    val eliminators: List<CandidateEliminator> = defaultEliminators(),
    val maxRecursionDepth: Int = 1000
) {
    companion object {
        /**
         * Default eliminators used when no configuration is provided.
         * Includes all available techniques for maximum solving power.
         */
        fun defaultEliminators(): List<CandidateEliminator> = listOf(
            SimpleCandidateEliminator(),
            GroupCandidateEliminator(),
            HiddenSubsetCandidateEliminator(),
            ExclusionCandidateEliminator(9),
            XWingCandidateEliminator(),
            SwordfishCandidateEliminator(),
            XYWingCandidateEliminator(),
            XYZWingCandidateEliminator(),
            WWingCandidateEliminator(),
            SimpleColoringCandidateEliminator(),
            UniqueRectanglesCandidateEliminator(),
            ForcingChainsCandidateEliminator(),
            ALSXZCandidateEliminator(),
            FrankenFishCandidateEliminator(),
            MutantFishCandidateEliminator(),
            DeathBlossomCandidateEliminator()
        )

        /**
         * Creates a config with only basic eliminators (no advanced techniques).
         * Useful for testing or when advanced techniques are not needed.
         */
        fun basic(): SolverConfig = SolverConfig(
            eliminators = listOf(
                SimpleCandidateEliminator(),
                ExclusionCandidateEliminator(9)
            )
        )


    }

    /**
     * Returns true if any configured eliminator assumes the puzzle has a unique solution.
     * These techniques can eliminate valid candidates from puzzles with multiple solutions.
     */
    fun usesUniquenessTechniques(): Boolean {
        return eliminators.any { it is UniqueRectanglesCandidateEliminator }
    }


}

/**
 * Board dimensions and display settings.
 *
 * These are fixed for standard 9x9 Sudoku but are kept configurable
 * for potential future variants.
 */
object BoardSettings {
    const val size: Int = 9
    val regionSize: Int = sqrt(size.toDouble()).toInt()
    val symbols: CharArray = charArrayOf('.', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    init {
        require(regionSize * regionSize == size) { "given size [$size] cannot be properly sqrt into another integer" }
    }
}


