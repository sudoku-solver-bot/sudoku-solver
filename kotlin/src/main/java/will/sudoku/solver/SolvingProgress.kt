package will.sudoku.solver

import kotlinx.serialization.Serializable

/**
 * Tracks the overall progress of solving a Sudoku puzzle step by step.
 *
 * This class accumulates all solving steps and provides:
 * - Step-by-step replay capability
 * - Progress statistics
 * - Serialization for API responses
 */
@Serializable
data class SolvingProgress(
    val originalPuzzle: String,
    val steps: MutableList<SolvingStep> = mutableListOf(),
    var currentBoardState: String,
    var isSolved: Boolean = false,
    var hasNoSolution: Boolean = false,
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null
) {
    /**
     * Add a new step to the progress.
     */
    fun addStep(step: SolvingStep) {
        steps.add(step)
    }

    /**
     * Get the current step number (next step will be this + 1).
     */
    fun nextStepNumber(): Int = steps.size + 1

    /**
     * Mark the puzzle as solved.
     */
    fun markSolved(finalBoardState: String) {
        isSolved = true
        currentBoardState = finalBoardState
        endTime = System.currentTimeMillis()
        addStep(SolvingStep(
            stepNumber = nextStepNumber(),
            stepType = StepType.PUZZLE_SOLVED,
            affectedCells = emptyList(),
            values = emptySet(),
            explanation = "Puzzle solved successfully in ${steps.size} steps!"
        ))
    }

    /**
     * Mark the puzzle as having no solution.
     */
    fun markNoSolution(reason: String = "No valid solution exists") {
        hasNoSolution = true
        endTime = System.currentTimeMillis()
        addStep(SolvingStep(
            stepNumber = nextStepNumber(),
            stepType = StepType.NO_SOLUTION,
            affectedCells = emptyList(),
            values = emptySet(),
            explanation = reason
        ))
    }

    /**
     * Get total solving time in milliseconds.
     */
    fun solveTimeMs(): Long? {
        return if (endTime != null) endTime!! - startTime else null
    }

    /**
     * Get statistics about solving techniques used.
     */
    fun techniqueStats(): Map<StepType, Int> {
        return steps.groupingBy { it.stepType }.eachCount()
    }

    /**
     * Get steps filtered by technique.
     */
    fun stepsByType(type: StepType): List<SolvingStep> {
        return steps.filter { it.stepType == type }
    }

    /**
     * Get a summary of the solving process.
     */
    fun summary(): String {
        val timeStr = solveTimeMs()?.let { "${it}ms" } ?: "in progress"
        val stats = techniqueStats()
        val techniqueCounts = stats.entries
            .filter { it.key !in listOf(StepType.PUZZLE_SOLVED, StepType.NO_SOLUTION) }
            .joinToString(", ") { "${it.key.displayName}: ${it.value}" }

        return buildString {
            appendLine("Solving Progress")
            appendLine("─".repeat(40))
            appendLine("Status: ${when { isSolved -> "Solved ✓"; hasNoSolution -> "No Solution ✗"; else -> "In Progress..." }}")
            appendLine("Steps: ${steps.size}")
            appendLine("Time: $timeStr")
            if (techniqueCounts.isNotEmpty()) {
                appendLine("Techniques: $techniqueCounts")
            }
        }
    }

    companion object {
        /**
         * Create a new SolvingProgress from a puzzle string.
         */
        fun fromPuzzle(puzzle: String): SolvingProgress {
            return SolvingProgress(
                originalPuzzle = puzzle,
                currentBoardState = puzzle
            )
        }
    }
}
