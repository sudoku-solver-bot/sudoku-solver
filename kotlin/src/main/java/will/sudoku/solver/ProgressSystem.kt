package will.sudoku.solver

/**
 * Simplified progress tracking.
 */
data class Progress(
    val userId: String,
    val level: Int,
    val xp: Int,
    val puzzlesSolved: Int
)

class ProgressSystem {
    fun getProgress(userId: String): Progress {
        return Progress(
            userId = userId,
            level = 3,
            xp = 250,
            puzzlesSolved = 25
        )
    }
}
