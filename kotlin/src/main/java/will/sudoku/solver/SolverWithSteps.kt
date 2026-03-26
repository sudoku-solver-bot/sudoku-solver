package will.sudoku.solver

/**
 * A solver wrapper that records every step of the solving process.
 *
 * This solver wraps the standard Solver and captures:
 * - Each elimination technique applied
 * - Each cell filled
 * - Backtracking steps (if needed)
 *
 * @property config Solver configuration including eliminators and limits
 */
class SolverWithSteps(private val config: SolverConfig = SolverConfig()) {

    /**
     * Solve a puzzle and return the solution with step-by-step progress.
     */
    fun solveWithSteps(initialBoard: Board): Pair<Board?, SolvingProgress> {
        val initialPuzzleString = writeBoardToString(initialBoard)
        val progress = SolvingProgress.fromPuzzle(initialPuzzleString)
        val board = initialBoard.copy()

        // Track which cells were already confirmed before each iteration
        val previouslyConfirmed = mutableSetOf<Coord>()
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (board.isConfirmed(coord)) {
                    previouslyConfirmed.add(coord)
                }
            }
        }

        // Apply eliminators and record steps
        var stepNumber = 1
        var changed = true
        var iterations = 0
        val maxIterations = 100 // Safety limit

        while (changed && !board.isSolved() && iterations < maxIterations) {
            changed = false
            iterations++

            for (eliminator in config.eliminators) {
                // Apply eliminator
                eliminator.eliminate(board)

                // Check for newly confirmed cells
                for (row in 0 until 9) {
                    for (col in 0 until 9) {
                        val coord = Coord(row, col)
                        if (board.isConfirmed(coord) && coord !in previouslyConfirmed) {
                            val value = board.value(coord)
                            progress.addStep(SolvingStep.cellFilled(
                                stepNumber = stepNumber++,
                                cell = coord,
                                value = value,
                                explanation = "Cell (${row + 1}, ${col + 1}) confirmed as $value",
                                boardState = writeBoardToString(board)
                            ))
                            previouslyConfirmed.add(coord)
                            changed = true
                        }
                    }
                }
            }
        }

        // If not solved by elimination alone, need backtracking
        if (!board.isSolved()) {
            val solution = Solver().solve(board)
            if (solution != null) {
                // Record the remaining cells that were filled by backtracking
                for (row in 0 until 9) {
                    for (col in 0 until 9) {
                        val coord = Coord(row, col)
                        if (solution.isConfirmed(coord) && coord !in previouslyConfirmed) {
                            val value = solution.value(coord)
                            progress.addStep(SolvingStep.guessMade(
                                stepNumber = stepNumber++,
                                cell = coord,
                                guessedValue = value,
                                explanation = "Cell (${row + 1}, ${col + 1}) filled via backtracking"
                            ))
                        }
                    }
                }
                progress.markSolved(writeBoardToString(solution))
                return Pair(solution, progress)
            } else {
                progress.markNoSolution("No valid solution found")
                return Pair(null, progress)
            }
        }

        progress.markSolved(writeBoardToString(board))
        return Pair(board, progress)
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
    }
}
