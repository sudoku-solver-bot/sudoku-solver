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
        // Try Naked Single (easiest)
        findNakedSingle(board)?.let { return it }
        
        // Try more advanced techniques via HintGenerator
        val hintGenHint = HintGenerator.generate(board)
        if (hintGenHint != null) {
            return TeachingHint(
                type = when (hintGenHint.technique) {
                    HintGenerator.Technique.HIDDEN_SINGLE -> HintType.HIDDEN_SINGLE
                    HintGenerator.Technique.NAKED_PAIR -> HintType.NAKED_PAIR
                    HintGenerator.Technique.HIDDEN_PAIR -> HintType.HIDDEN_PAIR
                    HintGenerator.Technique.NAKED_TRIPLE -> HintType.ADVANCED
                    HintGenerator.Technique.HIDDEN_TRIPLE -> HintType.ADVANCED
                    HintGenerator.Technique.X_WING -> HintType.X_WING
                    HintGenerator.Technique.SWORDFISH -> HintType.ADVANCED
                    HintGenerator.Technique.XY_WING -> HintType.ADVANCED
                },
                cell = hintGenHint.coord,
                technique = hintGenHint.technique.displayName,
                explanation = hintGenHint.explanation,
                teachingPoints = techniqueTeachingPoints(hintGenHint.technique)
            )
        }
        
        // Default fallback hint
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
    
    private fun findNakedSingle(board: Board): TeachingHint? {
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
        return null
    }
    
    private fun techniqueTeachingPoints(technique: HintGenerator.Technique): List<String> {
        return when (technique) {
            HintGenerator.Technique.HIDDEN_SINGLE -> listOf(
                "Look for a number that appears only once in a row, column, or box",
                "Check each row, column, and box systematically",
                "This is called a 'Hidden Single' — the number is hidden among other candidates"
            )
            HintGenerator.Technique.NAKED_PAIR -> listOf(
                "Find two cells in the same row, column, or box with identical candidates",
                "These two cells 'claim' those two numbers",
                "Eliminate those numbers from other cells in the same group"
            )
            HintGenerator.Technique.HIDDEN_PAIR -> listOf(
                "Find two numbers that appear only in the same two cells within a group",
                "Those cells must contain those two numbers",
                "Eliminate other candidates from those two cells"
            )
            HintGenerator.Technique.X_WING -> listOf(
                "Look for a number that appears in exactly 2 cells in each of 2 rows",
                "These occurrences must align in the same 2 columns",
                "Eliminate that number from other cells in those columns"
            )
            else -> listOf(
                "This is an advanced technique",
                "Look for patterns in candidate arrangements",
                "Practice easier techniques first to build familiarity"
            )
        }
    }
}
