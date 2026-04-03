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
            MutantFishCandidateEliminator()
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

        /**
         * Creates a config with eliminators up to a certain difficulty level.
         *
         * @param maxDifficulty Maximum difficulty level to include
         */
        fun withMaxDifficulty(maxDifficulty: DifficultyLevel): SolverConfig {
            val eliminators = mutableListOf<CandidateEliminator>(
                SimpleCandidateEliminator(),
                ExclusionCandidateEliminator(9)
            )

            if (maxDifficulty >= DifficultyLevel.HARD) {
                eliminators.add(GroupCandidateEliminator())
                eliminators.add(HiddenSubsetCandidateEliminator())
            }

            if (maxDifficulty >= DifficultyLevel.EXPERT) {
                eliminators.add(XWingCandidateEliminator())
                eliminators.add(SwordfishCandidateEliminator())
                eliminators.add(XYWingCandidateEliminator())
                eliminators.add(XYZWingCandidateEliminator())
                eliminators.add(WWingCandidateEliminator())
                eliminators.add(SimpleColoringCandidateEliminator())
            }

            return SolverConfig(eliminators = eliminators)
        }
    }

    /**
     * Difficulty levels for solver configuration.
     */
    enum class DifficultyLevel {
        EASY,   // Simple elimination only
        MEDIUM, // Hidden singles
        HARD,   // Naked/hidden subsets
        EXPERT  // X-Wing, Swordfish, XY-Wing
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

/**
 * Legacy Settings object for backward compatibility.
 *
 * @deprecated Use SolverConfig instead. This object will be removed in a future version.
 */
@Deprecated(
    message = "Use SolverConfig for dependency injection. This object will be removed in a future version.",
    replaceWith = ReplaceWith("SolverConfig", "will.sudoku.solver.SolverConfig")
)
object Settings {
    const val size: Int = BoardSettings.size
    val regionSize: Int = BoardSettings.regionSize
    val symbols: CharArray = BoardSettings.symbols

    val eliminators: List<CandidateEliminator> = SolverConfig.defaultEliminators()

    // Individual eliminators for backward compatibility
    val simpleCandidateEliminator = SimpleCandidateEliminator()
    val groupCandidateEliminator = GroupCandidateEliminator()
    val hiddenSubsetCandidateEliminator = HiddenSubsetCandidateEliminator()
    val exclusionCandidateEliminator = ExclusionCandidateEliminator(9)
    val xWingCandidateEliminator = XWingCandidateEliminator()
    val swordfishCandidateEliminator = SwordfishCandidateEliminator()
    val xyWingCandidateEliminator = XYWingCandidateEliminator()
    val xyzWingCandidateEliminator = XYZWingCandidateEliminator()
    val wWingCandidateEliminator = WWingCandidateEliminator()
    val simpleColoringCandidateEliminator = SimpleColoringCandidateEliminator()
    val uniqueRectanglesCandidateEliminator = UniqueRectanglesCandidateEliminator()
    val forcingChainsCandidateEliminator = ForcingChainsCandidateEliminator()
    val alsxzCandidateEliminator = ALSXZCandidateEliminator()
    val frankenFishCandidateEliminator = FrankenFishCandidateEliminator()
    val mutantFishCandidateEliminator = MutantFishCandidateEliminator()
}
