package will.sudoku.solver

import will.sudoku.solver.Settings.eliminators

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
 * @see Settings.eliminators for the list of configured eliminators
 */
class Solver {

    /**
     * Solves the given Sudoku puzzle.
     *
     * @param board The puzzle board to solve
     * @return The solved board, or null if no solution exists
     */
    fun solve(board: Board): Board? {
        return solve(board, 0)
    }

    /**
     * Recursive solving with depth tracking.
     *
     * @param board The puzzle board to solve
     * @param depth Current recursion depth (for backtracking tracking)
     * @return The solved board, or null if no solution exists
     */
    fun solve(board: Board, depth: Int): Board? {
        if (!board.isValid()) return null
        if (board.isSolved()) return board

        val moves = sequence {
            val unresolvedCoord = board.unresolvedCoord()!!
            for (candidateValue in board.candidateValues(unresolvedCoord)) {
                yield(Pair(unresolvedCoord, candidateValue))
            }
        }

        return moves.map { move ->
            val newBoard = board.copy()
            newBoard.markValue(move.first, move.second)

            for (eliminator in eliminators) {
                eliminator.eliminate(newBoard)
            }

            solve(newBoard, depth + 1)
        }.firstOrNull { it != null }
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