package will.sudoku.solver

/**
 * A solver wrapper that records every step of the solving process.
 *
 * **Note:** This class now delegates to Solver + StepRecorder.
 * It is maintained for backward compatibility.
 *
 * ## Recommended Migration
 *
 * ```kotlin
 * // Old way (deprecated):
 * val solver = SolverWithSteps()
 * val (solution, progress) = solver.solveWithSteps(board)
 *
 * // New way (recommended):
 * val solver = Solver()
 * val recorder = StepRecorder()
 * val solution = solver.solve(board, recorder)
 * val progress = recorder.progress
 * ```
 *
 * This solver wraps the standard Solver and captures:
 * - Each elimination technique applied
 * - Each cell filled
 * - Backtracking steps (if needed)
 *
 * @property config Solver configuration including eliminators and limits
 * @see Solver for the underlying solver
 * @see StepRecorder for the step recording listener
 */
class SolverWithSteps(private val config: SolverConfig = SolverConfig()) {

    private val solver = Solver(config)

    /**
     * Solve a puzzle and return the solution with step-by-step progress.
     */
    fun solveWithSteps(initialBoard: Board): Pair<Board?, SolvingProgress> {
        val recorder = StepRecorder()
        recorder.setBoardState(writeBoardToString(initialBoard))
        
        val solution = solver.solve(initialBoard, recorder)
        
        if (solution != null) {
            recorder.setBoardState(writeBoardToString(solution))
        }
        
        return Pair(solution, recorder.progress)
    }

    /**
     * Convert board to string representation.
     */
    private fun writeBoardToString(board: Board): String {
        return buildString {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val coord = Coord(row, col)
                    if (board.isConfirmed(coord)) {
                        append(board.value(coord))
                    } else {
                        append('.')
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Convenience method to solve a puzzle string and get step-by-step results.
         */
        fun solveWithSteps(puzzleString: String): Pair<Board?, SolvingProgress> {
            val board = BoardReader.readBoard(puzzleString)
            return SolverWithSteps().solveWithSteps(board)
        }

        /**
         * Convenience method to solve with custom config.
         */
        fun solveWithSteps(puzzleString: String, config: SolverConfig): Pair<Board?, SolvingProgress> {
            val board = BoardReader.readBoard(puzzleString)
            return SolverWithSteps(config).solveWithSteps(board)
        }
    }
}
