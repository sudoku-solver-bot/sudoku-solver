package will.sudoku.solver

import will.sudoku.solver.Board
import will.sudoku.solver.Coord

/**
 * Records each step of the solving process.
 *
 * Tracks:
 * - Each cell filled
 * - Each backtracking event
 * - Each eliminator application
 * - Complete solving progress
 *
 * ## Usage
 *
 * ```kotlin
 * val solver = Solver()
 * val recorder = StepRecorder()
 * val solution = solver.solve(board, recorder)
 *
 * if (solution != null) {
 *     println("Solved in ${recorder.progress.stepCount} steps")
 *     recorder.progress.steps.forEach { step ->
 *         println("${step.stepNumber}: ${step.explanation}")
 *     }
 * }
 * ```
 *
 * @see SolvingProgress for the complete progress data
 * @see SolvingStep for individual step details
 */
class StepRecorder : SolvingListener {
    
    private val _steps = mutableListOf<SolvingStep>()
    private var stepNumber = 0
    private var currentBoardState: String? = null
    private var solved = false
    private var noSolution = false
    private var noSolutionReason: String? = null
    
    /**
     * The solving progress recorded so far.
     */
    val progress: SolvingProgress
        get() {
            val progressObj = SolvingProgress(
                originalPuzzle = currentBoardState ?: "",
                currentBoardState = currentBoardState ?: ""
            )
            _steps.forEach { progressObj.addStep(it) }
            if (solved) {
                progressObj.markSolved(currentBoardState ?: "")
            } else if (noSolution) {
                progressObj.markNoSolution(noSolutionReason ?: "No valid solution found")
            }
            return progressObj
        }
    
    override fun onCellFilled(coord: Coord, value: Int, explanation: String) {
        stepNumber++
        _steps.add(SolvingStep.cellFilled(
            stepNumber = stepNumber,
            cell = coord,
            value = value,
            explanation = explanation,
            boardState = currentBoardState ?: ""
        ))
    }
    
    override fun onBacktracking() {
        stepNumber++
        _steps.add(SolvingStep.backtrack(
            stepNumber = stepNumber,
            cell = Coord(0, 0), // Placeholder - no specific cell for backtracking
            wrongValue = 0,     // Placeholder - no specific value
            explanation = "Backtracking - no valid candidates found"
        ))
    }
    
    override fun onEliminatorApplied(name: String, eliminations: Int) {
        // Per-cell detail is handled by onCandidatesEliminated.
        // This aggregate callback is kept for backward compatibility with other listeners.
    }

    override fun onCandidatesEliminated(name: String, eliminations: List<Elimination>) {
        if (eliminations.isEmpty()) return

        stepNumber++
        val affectedCells = eliminations.map { it.cell }
        val allValues = eliminations.flatMap { it.eliminatedValues }.toSet()
        val totalEliminated = eliminations.sumOf { it.eliminatedValues.size }

        // Build human-readable per-cell explanation
        val cellDescs = eliminations.sortedBy { "${it.cell.row},${it.cell.col}" }
            .joinToString("; ") { e ->
                val cellLabel = "(${e.cell.row + 1}, ${e.cell.col + 1})"
                val vals = e.eliminatedValues.sorted().joinToString(",")
                "$cellLabel: [$vals]"
            }

        val stepType = StepType.fromTechniqueName(name)
        val explanation = "$name: $totalEliminated candidate(s) eliminated — $cellDescs"

        _steps.add(SolvingStep(
            stepNumber = stepNumber,
            stepType = stepType,
            affectedCells = affectedCells,
            values = allValues,
            explanation = explanation
        ))
    }
    
    override fun onSolveComplete(solved: Boolean, timeNanos: Long, backtracks: Int) {
        if (solved) {
            this.solved = true
        } else {
            this.noSolution = true
            this.noSolutionReason = "No valid solution found"
        }
    }
    
    override fun onPropagationPassStarted() {
        // No action needed
    }
    
    override fun onPropagationPassComplete(cellsFilled: Int, candidatesEliminated: Int) {
        // No action needed
    }
    
    override fun onGuessMade(coord: Coord, value: Int, candidateCount: Int) {
        // Already handled by onCellFilled with "Guess" explanation
    }
    
    /**
     * Sets the current board state (called by solver).
     */
    fun setBoardState(boardString: String) {
        currentBoardState = boardString
    }
}
