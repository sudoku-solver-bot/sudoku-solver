package will.sudoku.solver

/**
 * Main Sudoku solver using constraint propagation and backtracking.
 *
 * The solver uses a combination of:
 * 1. **Constraint propagation**: Eliminates impossible candidates using various techniques
 * 2. **Backtracking**: When propagation stalls, makes a guess and recurses
 *
 * ## Solving Strategy
 * The solver applies all configured eliminators iteratively until no more progress can be made.
 * If the puzzle is still unsolved, it picks the cell with the fewest candidates and tries each
 * value recursively (backtracking with minimum remaining values heuristic).
 *
 * ## Performance
 * Most puzzles are solved without backtracking using just constraint propagation.
 * Harder puzzles may require backtracking, which is tracked in metrics.
 *
 * ## Example
 * ```kotlin
 * val solver = Solver()
 * val board = BoardReader.readBoard(puzzleString)
 * val solution = solver.solve(board)
 *
 * if (solution != null) {
 *     println("Solved!")
 *     println(solution)
 * } else {
 *     println("No solution found")
 * }
 * ```
 *
 * @see SolverWithMetrics for solving with performance metrics
 * @see SolverConfig for configuration options for the list of configured eliminators
 */
class Solver(private val config: SolverConfig = SolverConfig()) {
    
    private val logger = SolverLogger("Solver")

    /**
     * Solves the given Sudoku puzzle.
     *
     * @param board The puzzle board to solve
     * @return The solved board, or null if no solution exists
     */
    fun solve(board: Board): Board? {
        return solve(board, NoOpListener)
    }

    /**
     * Solves the given Sudoku puzzle with a listener for observing the process.
     *
     * @param board The puzzle board to solve
     * @param listener Listener to receive callbacks during solving
     * @return The solved board, or null if no solution exists
     */
    fun solve(board: Board, listener: SolvingListener): Board? {
        val startTime = System.nanoTime()
        val puzzleString = board.toString()
        
        logger.logSolveStart(puzzleString, "Solver")
        
        listener.onPropagationPassStarted()
        
        // Try with configured eliminators first
        var result = solveInternal(board, 0, listener, config.eliminators)
        
        // If configured eliminators failed and we're not already using basic config,
        // fall back to basic eliminators which use only safe techniques (no advanced patterns).
        // This prevents bugs in individual advanced eliminators from making solvable puzzles
        // unsolvable.
        if (result == null && config.eliminators != SolverConfig.basic().eliminators) {
            result = solveInternal(board, 0, NoOpListener, SolverConfig.basic().eliminators)
        }
        
        // Notify completion
        val timeNanos = System.nanoTime() - startTime
        val backtracks = 0 // Will be updated by listener if it tracks this
        listener.onSolveComplete(result != null, timeNanos, backtracks)
        
        logger.logSolveComplete(
            success = result != null,
            solveTimeMs = timeNanos / 1_000_000.0,
            backtrackingCount = backtracks,
            stepsCount = null,
            solverType = "Solver"
        )
        
        return result
    }

    /**
     * Recursive solving with depth tracking (backward compatibility).
     *
     * @param board The puzzle board to solve
     * @param depth Current recursion depth (for backtracking tracking)
     * @return The solved board, or null if no solution exists
     */
    fun solve(board: Board, depth: Int): Board? {
        return solveInternal(board, depth, NoOpListener, config.eliminators)
    }

    /**
     * Internal recursive solving with listener support (uses configured eliminators).
     *
     * @param board The puzzle board to solve
     * @param depth Current recursion depth
     * @param listener Listener to receive callbacks
     * @return The solved board, or null if no solution exists
     */
    private fun solveInternal(board: Board, depth: Int, listener: SolvingListener): Board? {
        return solveInternal(board, depth, listener, config.eliminators)
    }

    /**
     * Internal recursive solving with configurable eliminators and listener support.
     *
     * @param board The puzzle board to solve
     * @param depth Current recursion depth
     * @param listener Listener to receive callbacks
     * @param eliminators Candidate eliminators to apply during solving
     * @return The solved board, or null if no solution exists
     */
    private fun solveInternal(board: Board, depth: Int, listener: SolvingListener, eliminators: List<CandidateEliminator>): Board? {
        if (!board.isValid()) return null
        if (board.isSolved()) return board

        val unresolvedCoord = board.unresolvedCoord() ?: return null
        val candidates = board.candidateValues(unresolvedCoord).toList()
        
        // If this is a guess point (more than one candidate), notify listener
        if (candidates.size > 1) {
            listener.onGuessMade(unresolvedCoord, candidates.first(), candidates.size)
        }

        for (candidateValue in candidates) {
            val newBoard = board.copy()
            newBoard.markValue(unresolvedCoord, candidateValue)
            
            // Notify listener about cell fill
            val explanation = if (candidates.size == 1) {
                "Only candidate"
            } else {
                "Guess (try $candidateValue among ${candidates.size} candidates)"
            }
            listener.onCellFilled(unresolvedCoord, candidateValue, explanation)

            // Apply constraint propagation
            for (eliminator in eliminators) {
                eliminator.eliminate(newBoard)
            }

            val result = solveInternal(newBoard, depth + 1, listener, eliminators)
            if (result != null) {
                return result
            }
            
            // Backtrack
            listener.onBacktracking()
        }

        return null
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Sample puzzle from the Java Solver main() method
            // Original: {7, 0, 0, 0, 4, 0, 2, 0, 0},
            //           {0, 0, 0, 5, 2, 0, 0, 0, 6},
            //           {0, 0, 0, 0, 0, 0, 5, 0, 0},
            //           {0, 7, 0, 0, 0, 0, 9, 6, 0},
            //           {0, 6, 0, 0, 0, 0, 0, 8, 0},
            //           {4, 2, 5, 0, 0, 0, 0, 0, 0},
            //           {0, 0, 0, 0, 0, 9, 0, 3, 1},
            //           {0, 0, 4, 0, 0, 7, 0, 0, 0},
            //           {1, 0, 0, 6, 0, 0, 0, 0, 0}
            val puzzleString = """
                7...4.2..
                ...52...6
                ......5..
                .7....96.
                .6....8..
                425......
                .....9.31
                ..4..7...
                1..6.....
            """.trimIndent()

            val board = BoardReader.readBoard(puzzleString)
            println("Solving:")
            println(board)
            println()

            val solver = Solver()
            val solvedBoard = solver.solve(board)

            if (solvedBoard != null) {
                println("Solved:")
                println(solvedBoard)
            } else {
                println("No solution found")
                System.exit(1)
            }
        }
    }
}