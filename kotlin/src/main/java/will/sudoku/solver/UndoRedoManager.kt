package will.sudoku.solver

/**
 * Undo/Redo manager for tracking puzzle state changes.
 * 
 * Allows users to:
 * - Undo their last move(s)
 * - Redo undone moves
 * - See move history
 * 
 * Essential for kids learning - makes the game forgiving!
 */
class UndoRedoManager {
    private val undoStack = ArrayDeque<PuzzleState>()
    private val redoStack = ArrayDeque<PuzzleState>()
    private val maxHistorySize = 100
    
    /**
     * Save current state before making a move.
     */
    fun saveState(state: PuzzleState) {
        undoStack.addLast(state)
        
        // Clear redo stack when new move is made
        redoStack.clear()
        
        // Limit history size
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }
    }
    
    /**
     * Undo the last move.
     * 
     * @return Previous state, or null if no undo available
     */
    fun undo(currentState: PuzzleState): PuzzleState? {
        if (undoStack.isEmpty()) return null
        
        // Save current state to redo stack
        redoStack.addLast(currentState)
        
        // Return previous state
        return undoStack.removeLast()
    }
    
    /**
     * Redo a previously undone move.
     * 
     * @return Next state, or null if no redo available
     */
    fun redo(): PuzzleState? {
        if (redoStack.isEmpty()) return null
        
        val state = redoStack.removeLast()
        
        // Save to undo stack
        undoStack.addLast(state)
        
        return state
    }
    
    /**
     * Check if undo is available.
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    
    /**
     * Check if redo is available.
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    
    /**
     * Get number of undo steps available.
     */
    fun undoCount(): Int = undoStack.size
    
    /**
     * Get number of redo steps available.
     */
    fun redoCount(): Int = redoStack.size
    
    /**
     * Clear all history.
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
    
    /**
     * Get move history summary.
     */
    fun getHistory(): MoveHistory {
        return MoveHistory(
            undoCount = undoStack.size,
            redoCount = redoStack.size,
            canUndo = canUndo(),
            canRedo = canRedo()
        )
    }
}

/**
 * Represents a puzzle state at a point in time.
 */
data class PuzzleState(
    val puzzle: String,
    val move: Move? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a single move.
 */
data class Move(
    val row: Int,
    val col: Int,
    val oldValue: Int,
    val newValue: Int,
    val type: MoveType
)

/**
 * Type of move.
 */
enum class MoveType {
    PLACE,      // Placed a number
    REMOVE,     // Removed a number
    HINT,       // Used hint
    CLEAR       // Cleared cell
}

/**
 * Move history summary.
 */
data class MoveHistory(
    val undoCount: Int,
    val redoCount: Int,
    val canUndo: Boolean,
    val canRedo: Boolean
)
