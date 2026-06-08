package will.sudoku.solver

import kotlin.reflect.KClass

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
            FishCandidateEliminator(2),
            FishCandidateEliminator(3),
            XYWingCandidateEliminator(),
            XYZWingCandidateEliminator(),
            WWingCandidateEliminator(),
            SimpleColoringCandidateEliminator(),
            UniqueRectanglesCandidateEliminator(),
            ForcingChainsCandidateEliminator(),
            ALSXZCandidateEliminator(),
            FrankenFishCandidateEliminator(),
            MutantFishCandidateEliminator()
            // DeathBlossom excluded — too slow for default set (combinatorial explosion)
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
                eliminators.add(FishCandidateEliminator(2))
                eliminators.add(FishCandidateEliminator(3))
                eliminators.add(XYWingCandidateEliminator())
                eliminators.add(XYZWingCandidateEliminator())
                eliminators.add(WWingCandidateEliminator())
                eliminators.add(SimpleColoringCandidateEliminator())
            }

            return SolverConfig(eliminators = eliminators)
        }

        /**
         * Creates a config with all default eliminators EXCEPT those matching
         * the given technique type. Used to check if a technique is truly required.
         */
        fun withoutTechnique(technique: StepType): SolverConfig {
            val excludedClasses = techniqueToEliminatorClasses(technique)
            val filtered = defaultEliminators().filter { eliminator ->
                eliminator::class !in excludedClasses
            }
            return SolverConfig(eliminators = filtered)
        }

        /**
         * Map StepType to the eliminator class(es) that produce it.
         */
        private fun techniqueToEliminatorClasses(technique: StepType): Set<KClass<out CandidateEliminator>> = when (technique) {
            StepType.SIMPLE_ELIMINATION -> setOf(SimpleCandidateEliminator::class, ExclusionCandidateEliminator::class)
            StepType.NAKED_PAIR, StepType.NAKED_TRIPLE, StepType.NAKED_SUBSET -> setOf(GroupCandidateEliminator::class)
            StepType.HIDDEN_SINGLE, StepType.HIDDEN_PAIR, StepType.HIDDEN_TRIPLE, StepType.HIDDEN_SUBSET -> setOf(HiddenSubsetCandidateEliminator::class)
            StepType.X_WING -> setOf(FishCandidateEliminator::class)
            StepType.SWORDFISH -> setOf(FishCandidateEliminator::class)
            StepType.XY_WING -> setOf(XYWingCandidateEliminator::class)
            else -> emptySet() // unknown/generic: can't exclude
        }
    }

    /**
     * Returns true if any configured eliminator assumes the puzzle has a unique solution.
     * These techniques can eliminate valid candidates from puzzles with multiple solutions.
     */
    fun usesUniquenessTechniques(): Boolean {
        return eliminators.any { it is UniqueRectanglesCandidateEliminator }
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


