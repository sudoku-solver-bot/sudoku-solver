package will.sudoku.solver

/**
 * Teaching hint that explains WHY a move is correct.
 */
data class TeachingHint(
    val type: HintType,
    val cell: Coord?,
    val technique: String,
    val explanation: String,
    val teachingPoints: List<String> = emptyList()
)

/**
 * Types of hints.
 */
enum class HintType {
    NAKED_SINGLE,
    HIDDEN_SINGLE,
    POINTING_PAIR,
    BOX_LINE_REDUCTION,
    NAKED_PAIR,
    HIDDEN_PAIR,
    X_WING,
    ADVANCED
}

/**
 * Teaching hint provider.
 */
class TeachingHintProvider {
    
    fun getHint(board: Board): TeachingHint {
        // Find naked single
        for (row in 0..8) {
            for (col in 0..8) {
                val coord = Coord(row, col)
                if (board.value(coord) == 0) {
                    val candidates = board.candidateValues(coord).toMutableSet()
                    
                    if (candidates.size == 1) {
                        val value = candidates.first()
                        return TeachingHint(
                            type = HintType.NAKED_SINGLE,
                            cell = coord,
                            technique = "Naked Single",
                            explanation = "Cell (${row + 1}, ${col + 1}) can only be $value! This is the only number that fits here.",
                            teachingPoints = listOf(
                                "Look at row ${row + 1}, column ${col + 1}, and the 3x3 box",
                                "Only one number is possible in this cell",
                                "This is called a 'Naked Single' because the answer is obvious"
                            )
                        )
                    }
                }
            }
        }
        
        // Default hint
        return TeachingHint(
            type = HintType.ADVANCED,
            cell = null,
            technique = "Scanning",
            explanation = "Look for cells with fewer candidates. Start with rows, columns, or boxes that are almost complete.",
            teachingPoints = listOf(
                "Scan for rows/columns/boxes with many filled cells",
                "Look for numbers that appear frequently",
                "Use pencil marks to track candidates"
            )
        )
    }
}
