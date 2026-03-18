package will.sudoku.solver

/**
 * A solver wrapper that records every step of the solving process.
 *
 * This solver wraps the standard Solver and captures:
 * - Each elimination technique applied
 * - Each cell filled
 * - Backtracking steps (if needed)
 */
class SolverWithSteps {

    /**
     * Solve a puzzle and return the solution with step-by-step progress.
     */
    fun solveWithSteps(initialBoard: Board): Pair<Board?, SolvingProgress> {
        val initialPuzzleString = writeBoardToString(initialBoard)
        val progress = SolvingProgress.fromPuzzle(initialPuzzleString)
        val board = initialBoard.copy()

        // Track which cells were already confirmed before each iteration
        val previouslyConfirmed = mutableSetOf<Coord>()
        for (coord in board.allCells()) {
            if (board.isConfirmed(coord)) {
                previouslyConfirmed.add(coord)
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

            for (eliminator in Settings.getEliminators()) {
                // Apply eliminator
                eliminator.eliminate(board)

                // Check for newly confirmed cells
                for (coord in board.allCells()) {
                    if (board.isConfirmed(coord) && coord !in previouslyConfirmed) {
                        val value = board.value(coord)
                        progress.addStep(SolvingStep.cellFilled(
                            stepNumber = stepNumber++,
                            cell = coord,
                            value = value,
                            explanation = "Cell (${coord.row + 1}, ${coord.col + 1}) confirmed as $value",
                            boardState = writeBoardToString(board)
                        ))
                        previouslyConfirmed.add(coord)
                        changed = true
                    }
                }
            }
        }

        // If not solved by elimination alone, need backtracking
        if (!board.isSolved()) {
            val solution = backtrackSolve(board, progress, stepNumber, previouslyConfirmed)
            if (solution != null) {
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
     * Backtracking solver that records each guess and backtrack step.
     */
    private fun backtrackSolve(
        board: Board,
        progress: SolvingProgress,
        initialStepNumber: Int,
        previouslyConfirmed: MutableSet<Coord>
    ): Board? {
        var stepNumber = initialStepNumber

        fun solve(currentBoard: Board, depth: Int = 0): Board? {
            if (currentBoard.isSolved()) return currentBoard

            // Find first unconfirmed cell with fewest candidates (MRV heuristic)
            val unconfirmedCell = currentBoard.unconfirmedCells()
                .minByOrNull { currentBoard.candidateValues(it).size }
                ?: return null

            val candidates = currentBoard.candidateValues(unconfirmedCell)
            if (candidates.isEmpty()) {
                progress.addStep(SolvingStep(
                    stepNumber = stepNumber++,
                    stepType = StepType.BACKTRACK,
                    affectedCells = listOf(unconfirmedCell),
                    values = emptySet(),
                    explanation = "No candidates for cell (${unconfirmedCell.row + 1}, ${unconfirmedCell.col + 1}), backtracking [depth: $depth]"
                ))
                return null
            }

            // Try each candidate
            for (candidate in candidates.sorted()) {
                progress.addStep(SolvingStep.guessMade(
                    stepNumber = stepNumber++,
                    cell = unconfirmedCell,
                    guessedValue = candidate,
                    explanation = "Guessing $candidate at cell (${unconfirmedCell.row + 1}, ${unconfirmedCell.col + 1}) [depth: $depth]"
                ))

                // Create new board with this guess
                val newBoard = currentBoard.copy()
                // Remove all other candidates from this cell
                for (other in candidates) {
                    if (other != candidate) {
                        newBoard.removeCandidate(unconfirmedCell, other)
                    }
                }

                // Apply eliminators
                val localConfirmed = mutableSetOf<Coord>()
                for (eliminator in Settings.getEliminators()) {
                    eliminator.eliminate(newBoard)
                }

                val result = solve(newBoard, depth + 1)
                if (result != null) return result
            }

            return null
        }

        return solve(board)
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
