package will.sudoku.solver

/**
 * Solver with comprehensive metrics collection.
 *
 * Extends the base Solver class to track performance metrics during solving,
 * including time spent in each eliminator, backtracking attempts, and
 * recursion depth.
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
 * ## Usage
 *
 * ```kotlin
 * val solver = Solver()
 * val result = solver.solveWithMetrics(board)
 * if (result.solvedBoard != null) {
 *     println(result.solvedBoard)
 *     println(result.metrics)
 * } else {
 *     println("No solution found")
 *     println(result.metrics)  // Metrics still useful even on failure
 * }
 * ```
 *
 * @property config Solver configuration including eliminators and limits
 */
class SolverWithMetrics(private val config: SolverConfig = SolverConfig()) {

    /**
     * Solves a puzzle and returns the result with metrics.
     *
     * @param board The initial board state
     * @return SolveResult containing the solved board (or null) and metrics
     */
    fun solveWithMetrics(board: Board): SolveResult {
        val startTime = System.nanoTime()
        val metricsBuilder = SolverMetricsBuilder()

        val solvedBoard = solveWithMetrics(board, 0, metricsBuilder)

        val totalTime = System.nanoTime() - startTime
        val finalMetrics = metricsBuilder.build(totalSolveTimeNanos = totalTime)

        return SolveResult(solvedBoard, finalMetrics)
    }

    /**
     * Internal recursive solving method with metrics tracking.
     */
    private fun solveWithMetrics(
        board: Board,
        depth: Int,
        metricsBuilder: SolverMetricsBuilder
    ): Board? {
        // Update max depth
        metricsBuilder.updateMaxDepth(depth)

        // Validate board
        if (!board.isValid()) {
            metricsBuilder.incrementBacktracking()
            return null
        }

        // Check if solved
        if (board.isSolved()) {
            return board
        }

        // Apply constraint propagation with metrics
        applyConstraintPropagation(board, metricsBuilder)

        // Select cell with minimum remaining candidates (MRV heuristic)
        val unresolvedCoord = board.unresolvedCoord()
            ?: run {
                metricsBuilder.incrementBacktracking()
                return null
            }

        metricsBuilder.incrementCellsProcessed()

        // Try each candidate value
        for (candidateValue in board.candidateValues(unresolvedCoord)) {
            val newBoard = board.copy()
            newBoard.markValue(unresolvedCoord, candidateValue)

            val result = solveWithMetrics(newBoard, depth + 1, metricsBuilder)
            if (result != null) {
                return result
            }
        }

        // All branches failed - backtrack
        metricsBuilder.incrementBacktracking()
        return null
    }

    /**
     * Apply constraint propagation with metrics tracking.
     */
    private fun applyConstraintPropagation(
        board: Board,
        metricsBuilder: SolverMetricsBuilder
    ) {
        var anyChanges: Boolean
        do {
            anyChanges = false

            for (eliminator in config.eliminators) {
                val startTime = System.nanoTime()
                var eliminations = 0
                var passes = 0

                var eliminatorMadeChanges: Boolean
                do {
                    eliminatorMadeChanges = false
                    passes++

                    val eliminatorResult = applyEliminatorWithTracking(
                        board,
                        eliminator,
                        metricsBuilder
                    )

                    if (eliminatorResult.changesMade) {
                        eliminations += eliminatorResult.eliminations
                        eliminatorMadeChanges = true
                        anyChanges = true
                    }
                } while (eliminatorMadeChanges)

                val eliminatorTime = System.nanoTime() - startTime

                if (eliminations > 0 || eliminatorTime > 0) {
                    metricsBuilder.recordEliminatorPass(
                        eliminatorName = eliminator::class.simpleName ?: "Unknown",
                        eliminations = eliminations,
                        passes = passes,
                        timeNanos = eliminatorTime
                    )
                }

                metricsBuilder.incrementPropagationPasses()
            }
        } while (anyChanges)
    }

    /**
     * Apply a single eliminator and track what it does.
     *
     * Since eliminators don't currently expose fine-grained metrics,
     * we estimate eliminations by counting candidate pattern changes.
     */
    private fun applyEliminatorWithTracking(
        board: Board,
        eliminator: CandidateEliminator,
        metricsBuilder: SolverMetricsBuilder
    ): EliminatorResult {
        val patternsBefore = board.candidatePatterns.copyOf()

        val changesMade = eliminator.eliminate(board)

        if (!changesMade) {
            return EliminatorResult(changesMade = false, eliminations = 0)
        }

        // Estimate eliminations by counting pattern changes
        var eliminations = 0
        for (i in board.candidatePatterns.indices) {
            if (patternsBefore[i] != board.candidatePatterns[i]) {
                // Count bits that were removed
                eliminations += countBitsRemoved(patternsBefore[i], board.candidatePatterns[i])
            }
        }

        return EliminatorResult(changesMade = true, eliminations = eliminations)
    }

    /**
     * Count how many candidate bits were removed from a cell.
     */
    private fun countBitsRemoved(before: Int, after: Int): Int {
        val removed = before and after.inv()
        return removed.countOneBits()
    }
}

/**
 * Result of applying an eliminator.
 */
private data class EliminatorResult(
    val changesMade: Boolean,
    val eliminations: Int
)

/**
 * Builder for constructing SolverMetrics incrementally during solving.
 */
private class SolverMetricsBuilder(
    private var backtrackingCount: Int = 0,
    private var maxRecursionDepth: Int = 0,
    private var propagationPasses: Int = 0,
    private var cellsProcessed: Int = 0,
    private val eliminatorMetrics: MutableMap<String, EliminatorMetricsBuilder> = mutableMapOf()
) {

    fun incrementBacktracking() {
        backtrackingCount++
    }

    fun updateMaxDepth(depth: Int) {
        if (depth > maxRecursionDepth) {
            maxRecursionDepth = depth
        }
    }

    fun incrementPropagationPasses() {
        propagationPasses++
    }

    fun incrementCellsProcessed() {
        cellsProcessed++
    }

    fun recordEliminatorPass(
        eliminatorName: String,
        eliminations: Int,
        passes: Int,
        timeNanos: Long
    ) {
        eliminatorMetrics.getOrPut(eliminatorName) { EliminatorMetricsBuilder() }
            .apply {
                this.eliminations += eliminations
                this.passes += passes
                this.totalTimeNanos += timeNanos
            }
    }

    fun build(totalSolveTimeNanos: Long): SolverMetrics {
        val finalEliminatorMetrics: Map<String, EliminatorMetrics> = eliminatorMetrics.mapValues { it.value.build() }
        return SolverMetrics(
            totalSolveTimeNanos = totalSolveTimeNanos,
            backtrackingCount = backtrackingCount,
            maxRecursionDepth = maxRecursionDepth,
            propagationPasses = propagationPasses,
            cellsProcessed = cellsProcessed,
            eliminatorMetrics = finalEliminatorMetrics
        )
    }
}

/**
 * Builder for individual eliminator metrics.
 */
private class EliminatorMetricsBuilder(
    var eliminations: Int = 0,
    var passes: Int = 0,
    var totalTimeNanos: Long = 0
) {
    fun build(): EliminatorMetrics = EliminatorMetrics(
        eliminations = eliminations,
        passes = passes,
        totalTimeNanos = totalTimeNanos
    )
}
