package will.sudoku.solver

/**
 * Solver with comprehensive metrics collection.
 *
 * **Note:** This class now delegates to Solver + MetricsCollector.
 * It is maintained for backward compatibility.
 *
 * ## Recommended Migration
 *
 * ```kotlin
 * // Old way (deprecated):
 * val solver = SolverWithMetrics()
 * val result = solver.solveWithMetrics(board)
 *
 * // New way (recommended):
 * val solver = Solver()
 * val metrics = MetricsCollector()
 * val solution = solver.solve(board, metrics)
 * val result = SolveResult(solution, metrics.toSolverMetrics())
 * ```
 *
 * ## Metrics Collected
 *
 * - Total solve time
 * - Number of backtracking attempts
 * - Maximum recursion depth
 * - Constraint propagation passes
 * - Cells processed
 * - Per-eliminator: eliminations, passes, time
 *
 * @property config Solver configuration including eliminators and limits
 * @see Solver for the underlying solver
 * @see MetricsCollector for the metrics collection listener
 */
class SolverWithMetrics(private val config: SolverConfig = SolverConfig()) {

    private val solver = Solver(config)

    /**
     * Solves a puzzle and returns the result with metrics.
     *
     * @param board The initial board state
     * @return SolveResult containing the solved board (or null) and metrics
     */
    fun solveWithMetrics(board: Board): SolveResult {
        val metrics = MetricsCollector()
        val solvedBoard = solver.solve(board, metrics)
        
        return SolveResult(
            solvedBoard = solvedBoard,
            metrics = metrics.toSolverMetrics()
        )
    }
}
