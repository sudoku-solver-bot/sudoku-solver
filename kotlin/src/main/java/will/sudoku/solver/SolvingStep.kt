package will.sudoku.solver

import kotlinx.serialization.Serializable

/**
 * Represents a single step in the solving process.
 *
 * Each step captures:
 * - The type of solving action performed
 * - The cell(s) affected
 * - The value(s) involved
 * - A human-readable explanation
 * - The board state after this step (optional, for replay)
 */
@Serializable
data class SolvingStep(
    val stepNumber: Int,
    val stepType: StepType,
    val affectedCells: List<Coord>,
    val values: Set<Int>,
    val explanation: String,
    val boardState: String? = null,  // Optional snapshot of board after step
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create a step for filling a cell.
         */
        fun cellFilled(stepNumber: Int, cell: Coord, value: Int, explanation: String, boardState: String? = null): SolvingStep {
            return SolvingStep(
                stepNumber = stepNumber,
                stepType = StepType.CELL_FILLED,
                affectedCells = listOf(cell),
                values = setOf(value),
                explanation = explanation,
                boardState = boardState
            )
        }

        /**
         * Create a step for candidate elimination.
         */
        fun candidateEliminated(
            stepNumber: Int,
            cell: Coord,
            eliminatedValue: Int,
            technique: StepType,
            explanation: String
        ): SolvingStep {
            return SolvingStep(
                stepNumber = stepNumber,
                stepType = technique,
                affectedCells = listOf(cell),
                values = setOf(eliminatedValue),
                explanation = explanation
            )
        }

        /**
         * Create a step for a guess (backtracking).
         */
        fun guessMade(stepNumber: Int, cell: Coord, guessedValue: Int, explanation: String): SolvingStep {
            return SolvingStep(
                stepNumber = stepNumber,
                stepType = StepType.GUESS_MADE,
                affectedCells = listOf(cell),
                values = setOf(guessedValue),
                explanation = explanation
            )
        }

        /**
         * Create a step for backtracking.
         */
        fun backtrack(stepNumber: Int, cell: Coord, wrongValue: Int, explanation: String): SolvingStep {
            return SolvingStep(
                stepNumber = stepNumber,
                stepType = StepType.BACKTRACK,
                affectedCells = listOf(cell),
                values = setOf(wrongValue),
                explanation = explanation
            )
        }
    }

    /**
     * Format as a human-readable string.
     */
    fun format(): String {
        val cellStr = affectedCells.joinToString(", ") { "(${it.row + 1}, ${it.col + 1})" }
        val valueStr = values.joinToString(", ")
        return "Step $stepNumber: ${stepType.displayName}\n  Cells: $cellStr\n  Values: $valueStr\n  $explanation"
    }
}
