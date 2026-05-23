package will.sudoku.solver

import kotlin.random.Random

/**
 * Sudoku Puzzle Generator
 *
 * Generates valid Sudoku puzzles with unique solutions.
 *
 * ## Usage
 * ```kotlin
 * // Generate easy puzzle
 * val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.EASY)
 *
 * // Generate with seed for reproducibility
 * val puzzle = PuzzleGenerator.generate(DifficultyRater.Level.MEDIUM, seed = 42)
 * ```
 */
object PuzzleGenerator {

    /**
     * Generate a Sudoku puzzle of the specified difficulty.
     *
     * @param difficulty The target difficulty level
     * @param seed Optional seed for reproducible generation
     * @return A puzzle board (partially filled)
     */
    fun generate(difficulty: DifficultyRater.Level = DifficultyRater.Level.MEDIUM, seed: Long? = null): Board {
        val random = seed?.let { Random(it) } ?: Random.Default

        // Step 1: Generate a complete solved board
        val solvedBoard = generateSolvedBoard(random)

        // Step 2: Remove cells based on difficulty
        val cellsToRemove = getCellsToRemove(difficulty)
        val puzzleBoard = removeCells(solvedBoard, cellsToRemove, random)

        return puzzleBoard
    }

    /**
     * Generate a complete solved Sudoku board.
     */
    private fun generateSolvedBoard(random: Random): Board {
        // Start with empty board and fill using backtracking
        val board = Board(IntArray(81) { 0 })

        // Fill diagonal 3x3 regions first (they don't affect each other)
        fillDiagonalRegions(board, random)

        // Solve the rest — use basic config for reliable generation.
        // Advanced eliminators aren't needed for filling a board;
        // they only matter during puzzle analysis/rating.
        val solver = Solver(SolverConfig.basic())
        val solved = solver.solve(board)

        return solved ?: throw IllegalStateException("Failed to generate solved board")
    }

    /**
     * Fill the diagonal 3x3 regions with random valid values.
     */
    private fun fillDiagonalRegions(board: Board, random: Random) {
        for (regionIndex in 0..2) {
            val startRow = regionIndex * 3
            val startCol = regionIndex * 3

            val values = (1..9).shuffled(random)
            var index = 0

            for (row in startRow until startRow + 3) {
                for (col in startCol until startCol + 3) {
                    board.markValue(Coord(row, col), values[index++])
                }
            }
        }
    }

    /**
     * Get number of cells to remove based on difficulty.
     */
    private fun getCellsToRemove(difficulty: DifficultyRater.Level): Int {
        return when (difficulty) {
            DifficultyRater.Level.EASY -> 35      // 46 cells remaining
            DifficultyRater.Level.MEDIUM -> 45    // 36 cells remaining
            DifficultyRater.Level.HARD -> 52      // 29 cells remaining
            DifficultyRater.Level.EXPERT -> 56    // 25 cells remaining
            DifficultyRater.Level.MASTER -> 60    // 21 cells remaining
        }
    }

    /**
     * Remove cells from a solved board to create a puzzle.
     * Verifies uniqueness after each removal — if removing a cell
     * creates multiple solutions, the cell is restored and another
     * cell is tried instead.
     */
    private fun removeCells(board: Board, count: Int, random: Random): Board {
        val puzzle = board.copy()
        val positions = (0..80).shuffled(random)
        val wildcardPattern = (1 shl 9) - 1

        var removed = 0
        for (pos in positions) {
            if (removed >= count) break

            val row = pos / 9
            val col = pos % 9
            val coord = Coord(row, col)

            // Skip already-empty cells
            val originalValue = puzzle.value(coord)
            if (originalValue == 0) continue

            // Save original pattern for restoration
            val originalPattern = puzzle.candidatePatterns[pos]

            // Clear the cell (set to all candidates)
            puzzle.candidatePatterns[pos] = wildcardPattern

            // Verify puzzle still has exactly one solution
            if (!PuzzleValidator.hasUniqueSolution(puzzle)) {
                // Removing this cell broke uniqueness — restore it
                puzzle.candidatePatterns[pos] = originalPattern
                continue
            }

            removed++
        }

        return puzzle
    }

    /**
     * Generate a puzzle with minimum clues (harder, more unique).
     * Note: True minimum is 17 clues, but we target ~22 for reasonable generation.
     */
    fun generateMinimal(seed: Long? = null): Board {
        return generate(DifficultyRater.Level.MASTER, seed)
    }
}
