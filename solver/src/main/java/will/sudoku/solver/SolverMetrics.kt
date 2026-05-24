package will.sudoku.solver

/**
 * Metrics collected during Sudoku solving process.
 *
 * Tracks performance statistics for the solver and individual eliminators to enable
 * performance analysis and bottleneck identification.
 *
 * ## Metrics Categories
 *
 * - **Solver-level metrics**: Overall solve time, backtracking, recursion depth
 * - **Eliminator-level metrics**: Per-eliminator performance statistics
 * - **Propagation metrics**: Constraint propagation passes and eliminations
 *
 * ## Example Usage
 * ```kotlin
 * val solver = Solver()
 * val result = solver.solveWithMetrics(board)
 * if (result.solvedBoard != null) {
 *     println(result.solvedBoard)
 *     println(result.metrics)
 * }
 * ```
 *
 * @property totalSolveTimeNanos Total time spent solving (in nanoseconds)
 * @property backtrackingCount Number of failed branches/backtracking attempts
 * @property maxRecursionDepth Maximum recursion depth reached
 * @property propagationPasses Total number of constraint propagation passes
 * @property cellsProcessed Total number of cells examined
 * @property eliminatorMetrics Map of eliminator names to their individual metrics
 */
data class SolverMetrics(
    val totalSolveTimeNanos: Long = 0,
    val backtrackingCount: Int = 0,
    val maxRecursionDepth: Int = 0,
    val propagationPasses: Int = 0,
    val cellsProcessed: Int = 0,
    val eliminatorMetrics: Map<String, EliminatorMetrics> = emptyMap()
) {

    /**
     * Human-readable summary of metrics.
     */
    override fun toString(): String = buildString {
        appendLine("=== Solver Metrics ===")
        appendLine()
        appendLine("Overall Performance:")
        appendLine("  Total Solve Time: ${formatTime(totalSolveTimeNanos.toDouble())}")
        appendLine("  Backtracking Attempts: $backtrackingCount")
        appendLine("  Max Recursion Depth: $maxRecursionDepth")
        appendLine("  Constraint Propagation Passes: $propagationPasses")
        appendLine("  Cells Processed: $cellsProcessed")
        appendLine()

        if (eliminatorMetrics.isNotEmpty()) {
            appendLine("Eliminator Performance:")
            eliminatorMetrics.entries
                .sortedByDescending { it.value.totalTimeNanos }
                .forEach { (eliminator, metrics) ->
                    appendLine("  $eliminator:")
                    appendLine("    Eliminations: ${metrics.eliminations}")
                    appendLine("    Passes: ${metrics.passes}")
                    appendLine("    Time: ${formatTime(metrics.totalTimeNanos.toDouble())}")
                }
        }
    }

    /**
     * Detailed breakdown with additional statistics.
     */
    fun toDetailedString(): String = buildString {
        appendLine("=== Detailed Solver Metrics ===")
        appendLine()

        appendLine("Overall Performance:")
        appendLine("  Total Solve Time: ${formatTime(totalSolveTimeNanos.toDouble())}")
        appendLine("    (${totalSolveTimeNanos / 1_000_000.0} ms)")
        appendLine("  Backtracking Attempts: $backtrackingCount")
        appendLine("  Max Recursion Depth: $maxRecursionDepth")
        appendLine("  Constraint Propagation Passes: $propagationPasses")
        appendLine("  Cells Processed: $cellsProcessed")
        appendLine()

        if (eliminatorMetrics.isNotEmpty()) {
            appendLine("Eliminator Performance:")
            appendLine()

            val totalEliminations = eliminatorMetrics.values.sumOf { it.eliminations }
            val totalTimeNanos = eliminatorMetrics.values.sumOf { it.totalTimeNanos }

            eliminatorMetrics.entries
                .sortedByDescending { it.value.totalTimeNanos }
                .forEach { (eliminator, metrics) ->
                    val timePercent = if (totalTimeNanos > 0) {
                        (metrics.totalTimeNanos * 100.0 / totalTimeNanos)
                    } else 0.0
                    val eliminationPercent = if (totalEliminations > 0) {
                        (metrics.eliminations * 100.0 / totalEliminations)
                    } else 0.0

                    appendLine("  $eliminator:")
                    appendLine("    Eliminations: ${metrics.eliminations} (${"%.2f".format(eliminationPercent)}%)")
                    appendLine("    Passes: ${metrics.passes}")
                    appendLine("    Time: ${formatTime(metrics.totalTimeNanos.toDouble())} (${"%.1f".format(timePercent)}%)")
                    if (metrics.passes > 0) {
                        val avgTimePerPass = metrics.totalTimeNanos / metrics.passes.toDouble()
                        appendLine("    Avg Time/Pass: ${formatTime(avgTimePerPass)}")
                    }
                    appendLine()
                }
        }

        appendLine("=== Summary ===")
        if (propagationPasses > 0) {
            val avgTimePerPass = totalSolveTimeNanos / propagationPasses.toDouble()
            appendLine("  Avg Time per Propagation Pass: ${formatTime(avgTimePerPass)}")
        }
        if (cellsProcessed > 0) {
            val avgTimePerCell = totalSolveTimeNanos / cellsProcessed.toDouble()
            appendLine("  Avg Time per Cell: ${formatTime(avgTimePerCell)}")
        }
    }

    /**
     * CSV format for data analysis.
     */
    fun toCSV(): String = buildString {
        appendLine("metric,value")
        appendLine("total_solve_time_ms,${totalSolveTimeNanos / 1_000_000}")
        appendLine("backtracking_count,$backtrackingCount")
        appendLine("max_recursion_depth,$maxRecursionDepth")
        appendLine("propagation_passes,$propagationPasses")
        appendLine("cells_processed,$cellsProcessed")

        eliminatorMetrics.forEach { (eliminator, metrics) ->
            val sanitizedEliminator = eliminator.replace(" ", "_")
            appendLine("${sanitizedEliminator}_eliminations,${metrics.eliminations}")
            appendLine("${sanitizedEliminator}_passes,${metrics.passes}")
            appendLine("${sanitizedEliminator}_time_ms,${metrics.totalTimeNanos / 1_000_000}")
        }
    }

    private fun formatTime(nanos: Double): String {
        return when {
            nanos < 1_000 -> "${nanos.toInt()}ns"
            nanos < 1_000_000 -> "${"%.2f".format(nanos / 1_000.0)}µs"
            nanos < 1_000_000_000 -> "${"%.2f".format(nanos / 1_000_000.0)}ms"
            else -> "${"%.2f".format(nanos / 1_000_000_000.0)}s"
        }
    }
}

/**
 * Metrics collected for a single eliminator.
 *
 * @property eliminations Number of candidates removed
 * @property passes Number of do-while loop iterations (constraint propagation passes)
 * @property totalTimeNanos Total time spent in this eliminator (nanoseconds)
 */
data class EliminatorMetrics(
    val eliminations: Int = 0,
    val passes: Int = 0,
    val totalTimeNanos: Long = 0
) {
    /**
     * Creates a copy with an elimination added.
     */
    fun withElimination(): EliminatorMetrics = copy(eliminations = eliminations + 1)

    /**
     * Creates a copy with a pass added.
     */
    fun withPass(): EliminatorMetrics = copy(passes = passes + 1)

    /**
     * Creates a copy with time added.
     */
    fun withTimeAdded(nanos: Long): EliminatorMetrics =
        copy(totalTimeNanos = totalTimeNanos + nanos)
}

/**
 * Result of solving a puzzle with metrics.
 *
 * @property solvedBoard The solved board, or null if unsolvable
 * @property metrics The metrics collected during solving
 */
data class SolveResult(
    val solvedBoard: Board?,
    val metrics: SolverMetrics
)

/**
 * Exception thrown when solving metrics indicate an issue.
 */
class MetricsException(message: String) : RuntimeException(message)
