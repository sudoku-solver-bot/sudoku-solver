package will.sudoku.solver

import will.sudoku.solver.Board
import will.sudoku.solver.Coord

/**
 * Collects performance metrics during solving.
 *
 * Tracks:
 * - Solve time
 * - Number of backtracking attempts
 * - Maximum recursion depth
 * - Number of propagation passes
 * - Cells filled
 * - Candidates eliminated
 * - Eliminator statistics
 *
 * ## Usage
 *
 * ```kotlin
 * val solver = Solver()
 * val metrics = MetricsCollector()
 * val solution = solver.solve(board, metrics)
 *
 * if (solution != null) {
 *     println("Solved in ${metrics.solveTimeMs}ms")
 *     println("Backtracks: ${metrics.backtrackCount}")
 *     println("Max depth: ${metrics.maxRecursionDepth}")
 * }
 * ```
 *
 * @see Solver.solve
 * @see SolverMetrics for the result data structure
 */
class MetricsCollector : SolvingListener {
    
    private var startTime: Long = 0
    private var solveComplete: Boolean = false
    private var _backtrackCount: Int = 0
    private var _maxRecursionDepth: Int = 0
    private var _propagationPasses: Int = 0
    private var _cellsFilled: Int = 0
    private var _candidatesEliminated: Int = 0
    private var _guessCount: Int = 0
    private val _eliminatorStats = mutableMapOf<String, EliminatorStats>()
    
    /**
     * Total time spent solving (milliseconds).
     */
    val solveTimeMs: Long
        get() = if (solveComplete) (System.nanoTime() - startTime) / 1_000_000 else 0L
    
    /**
     * Number of backtracking attempts.
     */
    val backtrackCount: Int
        get() = _backtrackCount
    
    /**
     * Maximum recursion depth reached.
     */
    val maxRecursionDepth: Int
        get() = _maxRecursionDepth
    
    /**
     * Number of constraint propagation passes.
     */
    val propagationPasses: Int
        get() = _propagationPasses
    
    /**
     * Number of cells filled.
     */
    val cellsFilled: Int
        get() = _cellsFilled
    
    /**
     * Number of candidates eliminated.
     */
    val candidatesEliminated: Int
        get() = _candidatesEliminated
    
    /**
     * Number of guesses made (backtracking initiated).
     */
    val guessCount: Int
        get() = _guessCount
    
    /**
     * Per-eliminator statistics.
     */
    val eliminatorStats: Map<String, EliminatorStats>
        get() = _eliminatorStats.toMap()
    
    override fun onCellFilled(coord: Coord, value: Int, explanation: String) {
        _cellsFilled++
    }
    
    override fun onBacktracking() {
        _backtrackCount++
    }
    
    override fun onEliminatorApplied(name: String, eliminations: Int) {
        _candidatesEliminated += eliminations
        _eliminatorStats.getOrPut(name) { EliminatorStats(name) }
            .also { it.recordApplication(eliminations) }
    }
    
    override fun onSolveComplete(solved: Boolean, timeNanos: Long, backtracks: Int) {
        solveComplete = true
        _backtrackCount = backtracks
    }
    
    override fun onPropagationPassStarted() {
        _propagationPasses++
    }
    
    override fun onPropagationPassComplete(cellsFilled: Int, candidatesEliminated: Int) {
        // Already tracked in onCellFilled and onEliminatorApplied
    }
    
    override fun onGuessMade(coord: Coord, value: Int, candidateCount: Int) {
        _guessCount++
        // Track max depth (simplified - actual depth tracking would need more context)
        _maxRecursionDepth = maxOf(_maxRecursionDepth, _guessCount)
    }
    
    /**
     * Converts to SolverMetrics for compatibility with existing code.
     */
    fun toSolverMetrics(): SolverMetrics {
        return SolverMetrics(
            totalSolveTimeNanos = solveTimeMs * 1_000_000,
            backtrackingCount = backtrackCount,
            maxRecursionDepth = maxRecursionDepth,
            propagationPasses = propagationPasses,
            cellsProcessed = cellsFilled,
            eliminatorMetrics = eliminatorStats.mapValues { 
                EliminatorMetrics(
                    eliminations = it.value.totalEliminations,
                    passes = it.value.applications,
                    totalTimeNanos = 0 // Not tracked in listener version
                )
            }
        )
    }
    
    /**
     * Statistics for a single eliminator.
     */
    class EliminatorStats(val name: String) {
        var applications: Int = 0
            private set
        
        var totalEliminations: Int = 0
            private set
        
        fun recordApplication(eliminations: Int) {
            applications++
            totalEliminations += eliminations
        }
    }
}
