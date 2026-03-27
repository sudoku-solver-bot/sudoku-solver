package will.sudoku.solver

/**
 * Accessibility settings for users with different needs.
 */
data class AccessibilitySettings(
    val fontSize: FontSize = FontSize.MEDIUM,
    val highContrast: Boolean = false,
    val colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    val keyboardOnly: Boolean = false,
    val screenReader: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeTargets: Boolean = false,  // Larger touch targets
    val soundEnabled: Boolean = true,
    val visualFeedback: Boolean = true
)

/**
 * Font size options for readability.
 */
enum class FontSize(val scale: Double, val displayName: String) {
    SMALL(0.8, "Small"),
    MEDIUM(1.0, "Medium"),
    LARGE(1.3, "Large"),
    EXTRA_LARGE(1.6, "Extra Large")
}

/**
 * Color blind modes for different types of color vision.
 */
enum class ColorBlindMode(val displayName: String) {
    NONE("Normal"),
    PROTANOPIA("Red-Green (Protanopia)"),
    DEUTERANOPIA("Red-Green (Deuteranopia)"),
    TRITANOPIA("Blue-Yellow"),
    ACHROMATOPSIA("Grayscale")
}

/**
 * Accessible puzzle representation with additional context.
 */
data class AccessiblePuzzle(
    val grid: List<List<AccessibleCell>>,
    val description: String,
    val difficulty: String,
    val estimatedTime: String
)

/**
 * Single cell with accessibility information.
 */
data class AccessibleCell(
    val row: Int,
    val col: Int,
    val value: Int?,
    val isGiven: Boolean,
    val candidates: Set<Int>,
    val ariaLabel: String
) {
    /**
     * Generate screen reader friendly description.
     */
    fun toScreenReaderText(): String {
        val position = "Row ${row + 1}, Column ${col + 1}"
        return when {
            value != null && isGiven -> "$position, given value $value"
            value != null -> "$position, filled with $value"
            candidates.isNotEmpty() -> "$position, empty, possible values: ${candidates.sorted().joinToString(", ")}"
            else -> "$position, empty"
        }
    }
}

/**
 * Accessibility helper functions.
 */
object AccessibilityHelper {
    
    /**
     * Convert board to accessible format.
     */
    fun toAccessiblePuzzle(board: Board, difficulty: DifficultyLevel): AccessiblePuzzle {
        val grid = (0..8).map { row ->
            (0..8).map { col ->
                val coord = Coord(row, col)
                val value = board.value(coord)
                val isGiven = value != 0
                val candidates = if (value == 0) {
                    board.candidateValues(coord)
                } else {
                    emptySet()
                }
                
                AccessibleCell(
                    row = row,
                    col = col,
                    value = if (value == 0) null else value,
                    isGiven = isGiven,
                    candidates = candidates,
                    ariaLabel = "Cell row ${row + 1} column ${col + 1}"
                )
            }
        }
        
        val description = "Sudoku puzzle, ${difficulty.displayName} difficulty, ${difficulty.targetAgeRange}"
        val estimatedTime = when (difficulty) {
            DifficultyLevel.EASY -> "5-10 minutes"
            DifficultyLevel.MEDIUM -> "10-15 minutes"
            DifficultyLevel.HARD -> "15-25 minutes"
            DifficultyLevel.EXPERT -> "25-40 minutes"
        }
        
        return AccessiblePuzzle(
            grid = grid,
            description = description,
            difficulty = difficulty.displayName,
            estimatedTime = estimatedTime
        )
    }
    
    /**
     * Get color scheme for color blind mode.
     */
    fun getColorScheme(mode: ColorBlindMode): ColorScheme {
        return when (mode) {
            ColorBlindMode.NONE -> ColorScheme(
                given = "#333333",
                filled = "#0066cc",
                error = "#cc0000",
                highlight = "#ffeb3b"
            )
            ColorBlindMode.PROTANOPIA,
            ColorBlindMode.DEUTERANOPIA -> ColorScheme(
                given = "#000000",
                filled = "#0077b3",  // Blue instead of red/green
                error = "#ff6600",   // Orange
                highlight = "#9933ff" // Purple
            )
            ColorBlindMode.TRITANOPIA -> ColorScheme(
                given = "#333333",
                filled = "#009933",  // Green
                error = "#cc0066",   // Pink
                highlight = "#ffcc00" // Yellow
            )
            ColorBlindMode.ACHROMATOPSIA -> ColorScheme(
                given = "#000000",
                filled = "#666666",  // Gray scale
                error = "#333333",
                highlight = "#cccccc"
            )
        }
    }
    
    /**
     * Get keyboard navigation instructions.
     */
    fun getKeyboardInstructions(): List<String> {
        return listOf(
            "Use arrow keys to navigate between cells",
            "Press numbers 1-9 to fill in a cell",
            "Press Delete or Backspace to clear a cell",
            "Press H for a hint",
            "Press T to hear current cell",
            "Press S to hear full grid summary",
            "Press Escape to close modals"
        )
    }
    
    /**
     * Generate ARIA landmark structure.
     */
    fun generateAriaStructure(): String {
        return """
            <div role="application" aria-label="Sudoku puzzle game">
                <div role="grid" aria-label="9x9 Sudoku grid">
                    <!-- Grid rows with role="row" -->
                    <!-- Cells with role="gridcell" -->
                </div>
            </div>
        """.trimIndent()
    }
}

/**
 * Color scheme configuration.
 */
data class ColorScheme(
    val given: String,
    val filled: String,
    val error: String,
    val highlight: String
)
