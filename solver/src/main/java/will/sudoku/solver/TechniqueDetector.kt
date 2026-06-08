package will.sudoku.solver

/**
 * Interface for technique-specific hint detection.
 *
 * Each implementation detects a single sudoku technique and returns
 * a [HintGenerator.Hint] if the technique is applicable to the given board state.
 *
 * Implementations should be stateless and side-effect-free — they read the board
 * but do not modify it.
 */
interface TechniqueDetector {
    /**
     * The technique this detector identifies.
     */
    val technique: HintGenerator.Technique

    /**
     * Detect whether this technique applies to the given board.
     *
     * @param board The current board state (must not be modified)
     * @return A hint if the technique is found, null otherwise
     */
    fun detect(board: Board): HintGenerator.Hint?
}
