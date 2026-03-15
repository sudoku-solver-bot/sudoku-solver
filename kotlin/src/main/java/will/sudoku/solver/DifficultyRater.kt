package will.sudoku.solver

/**
 * Rates the difficulty of a Sudoku puzzle based on solving metrics.
 *
 * Difficulty is determined by which solving techniques were required:
 * - Level 1 (Easy): Simple elimination only
 * - Level 2 (Medium): Hidden singles needed
 * - Level 3 (Hard): Naked/hidden subsets needed
 * - Level 4 (Expert): X-Wing needed
 * - Level 5 (Master): Requires backtracking (guessing)
 *
 * ## Usage
 * ```kotlin
 * val solver = SolverWithMetrics()
 * val result = solver.solveWithMetrics(board)
 * if (result.solvedBoard != null) {
 *     val difficulty = DifficultyRater.rate(result.metrics)
 *     println("Difficulty: $difficulty")
 * }
 * ```
 */
object DifficultyRater {

    /**
     * Difficulty level with description.
     */
    enum class Level(val value: Int, val displayName: String, val description: String) {
        EASY(1, "Easy", "Simple elimination only"),
        MEDIUM(2, "Medium", "Hidden singles required"),
        HARD(3, "Hard", "Naked/hidden subsets required"),
        EXPERT(4, "Expert", "X-Wing technique required"),
        MASTER(5, "Master", "Requires backtracking");

        override fun toString(): String = displayName
    }

    /**
     * Result of difficulty rating.
     *
     * @property level The difficulty level
     * @property techniquesUsed List of techniques that were required
     * @property backtracking Whether backtracking was required
     */
    data class Rating(
        val level: Level,
        val techniquesUsed: List<String>,
        val backtracking: Boolean
    ) {
        override fun toString(): String = buildString {
            append(level.name)
            if (techniquesUsed.isNotEmpty() && level != Level.EASY) {
                append(" (")
                append(techniquesUsed.joinToString(", "))
                append(")")
            }
        }
    }

    /**
     * Rate the difficulty of a puzzle based on solving metrics.
     *
     * @param metrics The metrics collected during solving
     * @return The difficulty rating
     */
    fun rate(metrics: SolverMetrics): Rating {
        val techniquesUsed = mutableListOf<String>()
        var level = Level.EASY

        // Check if backtracking was required (highest difficulty)
        val requiredBacktracking = metrics.backtrackingCount > 0
        if (requiredBacktracking) {
            level = Level.MASTER
            techniquesUsed.add("backtracking")
        }

        // Check which eliminators made progress
        val eliminatorNames = metrics.eliminatorMetrics.keys

        // Check for X-Wing usage (Expert level)
        val usedXWing = eliminatorNames.any { it.contains("XWing", ignoreCase = true) } &&
                metrics.eliminatorMetrics.values.any { it.eliminations > 0 }
        if (usedXWing && level < Level.EXPERT) {
            level = Level.EXPERT
            techniquesUsed.add("X-Wing")
        }

        // Check for hidden subset usage (Hard level)
        val usedHiddenSubset = eliminatorNames.any { it.contains("HiddenSubset", ignoreCase = true) } &&
                metrics.eliminatorMetrics.entries.find { it.key.contains("HiddenSubset", ignoreCase = true) }?.value?.eliminations ?: 0 > 0
        if (usedHiddenSubset && level < Level.HARD) {
            level = Level.HARD
            techniquesUsed.add("hidden subsets")
        }

        // Check for group candidate (naked pairs/triples) usage (Hard level)
        val usedGroupCandidate = eliminatorNames.any { it.contains("Group", ignoreCase = true) } &&
                metrics.eliminatorMetrics.entries.find { it.key.contains("Group", ignoreCase = true) }?.value?.eliminations ?: 0 > 0
        if (usedGroupCandidate && level < Level.HARD) {
            level = Level.HARD
            if (!techniquesUsed.contains("naked subsets")) {
                techniquesUsed.add("naked subsets")
            }
        }

        // Check for exclusion (hidden singles) usage (Medium level)
        val usedExclusion = eliminatorNames.any { it.contains("Exclusion", ignoreCase = true) } &&
                metrics.eliminatorMetrics.entries.find { it.key.contains("Exclusion", ignoreCase = true) }?.value?.eliminations ?: 0 > 0
        if (usedExclusion && level < Level.MEDIUM) {
            level = Level.MEDIUM
            techniquesUsed.add("hidden singles")
        }

        return Rating(
            level = level,
            techniquesUsed = techniquesUsed,
            backtracking = requiredBacktracking
        )
    }

    /**
     * Quick difficulty check - returns just the level.
     */
    fun rateLevel(metrics: SolverMetrics): Level = rate(metrics).level

    /**
     * Check if a puzzle is considered "hard" (requires advanced techniques or backtracking).
     */
    fun isHard(metrics: SolverMetrics): Boolean = rate(metrics).level.value >= Level.HARD.value
}
