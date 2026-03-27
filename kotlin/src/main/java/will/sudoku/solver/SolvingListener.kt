package will.sudoku.solver

import will.sudoku.solver.Board
import will.sudoku.solver.Coord

/**
 * Listener interface for observing and recording the solving process.
 *
 * Implement this interface to receive callbacks at key points during solving.
 * This enables features like metrics collection, step recording, debugging,
 * and progress tracking without modifying the core solver.
 *
 * ## Usage Examples
 *
 * ### Simple Solving (No Listener)
 * ```kotlin
 * val solver = Solver()
 * val solution = solver.solve(board)
 * ```
 *
 * ### With Metrics Collection
 * ```kotlin
 * val solver = Solver()
 * val metrics = MetricsCollector()
 * val solution = solver.solve(board, metrics)
 * println("Time: ${metrics.solveTimeMs}ms")
 * println("Backtracks: ${metrics.backtrackCount}")
 * ```
 *
 * ### With Step Recording
 * ```kotlin
 * val solver = Solver()
 * val recorder = StepRecorder()
 * val solution = solver.solve(board, recorder)
 * recorder.progress.steps.forEach { step ->
 *     println("${step.stepNumber}: ${step.explanation}")
 * }
 * ```
 *
 * ### Combined Listeners
 * ```kotlin
 * val solver = Solver()
 * val metrics = MetricsCollector()
 * val recorder = StepRecorder()
 * val combined = CompositeListener(metrics, recorder)
 * val solution = solver.solve(board, combined)
 * ```
 *
 * @see MetricsCollector for collecting performance metrics
 * @see StepRecorder for recording step-by-step progress
 */
interface SolvingListener {
    
    /**
     * Called when a cell is filled with a value.
     *
     * @param coord The coordinate of the filled cell
     * @param value The value that was placed (1-9)
     * @param explanation Human-readable explanation of why this value was chosen
     */
    fun onCellFilled(coord: Coord, value: Int, explanation: String) {}
    
    /**
     * Called when the solver backtracks from an unsuccessful branch.
     * 
     * This indicates that a previously tried value didn't lead to a solution
     * and the solver is undoing that choice to try a different path.
     */
    fun onBacktracking() {}
    
    /**
     * Called when an elimination technique is applied.
     *
     * @param name Name of the elimination technique (e.g., "SimpleCandidateEliminator")
     * @param eliminations Number of candidates eliminated by this technique
     */
    fun onEliminatorApplied(name: String, eliminations: Int) {}
    
    /**
     * Called when solving is complete.
     *
     * @param solved Whether a solution was found
     * @param timeNanos Total solving time in nanoseconds
     * @param backtracks Number of backtracking operations performed
     */
    fun onSolveComplete(solved: Boolean, timeNanos: Long, backtracks: Int) {}
    
    /**
     * Called when a constraint propagation pass begins.
     */
    fun onPropagationPassStarted() {}
    
    /**
     * Called when a constraint propagation pass completes.
     *
     * @param cellsFilled Number of cells filled during this pass
     * @param candidatesEliminated Total candidates eliminated during this pass
     */
    fun onPropagationPassComplete(cellsFilled: Int, candidatesEliminated: Int) {}
    
    /**
     * Called when the solver is about to make a guess (backtracking point).
     *
     * @param coord The coordinate where the guess is being made
     * @param value The value being guessed
     * @param candidateCount Number of candidates available at this cell
     */
    fun onGuessMade(coord: Coord, value: Int, candidateCount: Int) {}
}

/**
 * Composite listener that forwards events to multiple listeners.
 *
 * Useful for combining multiple listeners (e.g., metrics + steps).
 *
 * ```kotlin
 * val combined = CompositeListener(metricsCollector, stepRecorder)
 * solver.solve(board, combined)
 * ```
 */
class CompositeListener(vararg listeners: SolvingListener) : SolvingListener {
    private val listeners = listeners.toList()
    
    override fun onCellFilled(coord: Coord, value: Int, explanation: String) {
        listeners.forEach { it.onCellFilled(coord, value, explanation) }
    }
    
    override fun onBacktracking() {
        listeners.forEach { it.onBacktracking() }
    }
    
    override fun onEliminatorApplied(name: String, eliminations: Int) {
        listeners.forEach { it.onEliminatorApplied(name, eliminations) }
    }
    
    override fun onSolveComplete(solved: Boolean, timeNanos: Long, backtracks: Int) {
        listeners.forEach { it.onSolveComplete(solved, timeNanos, backtracks) }
    }
    
    override fun onPropagationPassStarted() {
        listeners.forEach { it.onPropagationPassStarted() }
    }
    
    override fun onPropagationPassComplete(cellsFilled: Int, candidatesEliminated: Int) {
        listeners.forEach { it.onPropagationPassComplete(cellsFilled, candidatesEliminated) }
    }
    
    override fun onGuessMade(coord: Coord, value: Int, candidateCount: Int) {
        listeners.forEach { it.onGuessMade(coord, value, candidateCount) }
    }
}

/**
 * No-op listener for when no listener is needed.
 * Used as default parameter to avoid null checks.
 */
object NoOpListener : SolvingListener
