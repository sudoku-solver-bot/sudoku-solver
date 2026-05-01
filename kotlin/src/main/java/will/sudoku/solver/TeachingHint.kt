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
    NAKED_TRIPLE,
    HIDDEN_PAIR,
    HIDDEN_TRIPLE,
    X_WING,
    SWORDFISH,
    XY_WING,
    XYZ_WING,
    W_WING,
    SIMPLE_COLORING,
    UNIQUE_RECTANGLE,
    ALS_XZ,
    FRANKEN_FISH,
    MUTANT_FISH,
    DEATH_BLOSSOM,
    FORCING_CHAINS,
    ADVANCED,
    COMPLETE
}

/**
 * Teaching hint provider.
 */
class TeachingHintProvider {

    fun getHint(board: Board): TeachingHint {
        // Check if puzzle is already solved
        if (board.isSolved()) {
            return TeachingHint(
                type = HintType.COMPLETE,
                cell = null,
                technique = "Puzzle Complete",
                explanation = "This puzzle is already solved! All cells are filled correctly. Great job!",
                teachingPoints = listOf(
                    "You've completed this puzzle — no moves needed",
                    "Challenge yourself with a new puzzle to keep improving"
                )
            )
        }
        
        // Try Naked Single (easiest)
        findNakedSingle(board)?.let { return it }

        // Try all techniques via HintGenerator
        val hintGenHint = HintGenerator.generate(board)
        if (hintGenHint != null) {
            return TeachingHint(
                type = mapTechniqueToHintType(hintGenHint.technique),
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

    private fun mapTechniqueToHintType(technique: HintGenerator.Technique): HintType {
        return when (technique) {
            HintGenerator.Technique.HIDDEN_SINGLE -> HintType.HIDDEN_SINGLE
            HintGenerator.Technique.POINTING_PAIR -> HintType.POINTING_PAIR
            HintGenerator.Technique.BOX_LINE_REDUCTION -> HintType.BOX_LINE_REDUCTION
            HintGenerator.Technique.NAKED_PAIR -> HintType.NAKED_PAIR
            HintGenerator.Technique.NAKED_TRIPLE -> HintType.NAKED_TRIPLE
            HintGenerator.Technique.HIDDEN_PAIR -> HintType.HIDDEN_PAIR
            HintGenerator.Technique.HIDDEN_TRIPLE -> HintType.HIDDEN_TRIPLE
            HintGenerator.Technique.X_WING -> HintType.X_WING
            HintGenerator.Technique.SWORDFISH -> HintType.SWORDFISH
            HintGenerator.Technique.XY_WING -> HintType.XY_WING
            HintGenerator.Technique.XYZ_WING -> HintType.XYZ_WING
            HintGenerator.Technique.W_WING -> HintType.W_WING
            HintGenerator.Technique.SIMPLE_COLORING -> HintType.SIMPLE_COLORING
            HintGenerator.Technique.UNIQUE_RECTANGLE -> HintType.UNIQUE_RECTANGLE
            HintGenerator.Technique.ALS_XZ -> HintType.ALS_XZ
            HintGenerator.Technique.FRANKEN_FISH -> HintType.FRANKEN_FISH
            HintGenerator.Technique.MUTANT_FISH -> HintType.MUTANT_FISH
            HintGenerator.Technique.DEATH_BLOSSOM -> HintType.DEATH_BLOSSOM
            HintGenerator.Technique.FORCING_CHAINS -> HintType.FORCING_CHAINS
        }
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
            HintGenerator.Technique.POINTING_PAIR -> listOf(
                "Look for a candidate that appears in only one row or column within a box",
                "That candidate must go in that row/column in that box",
                "Eliminate the candidate from that row/column in other boxes"
            )
            HintGenerator.Technique.NAKED_PAIR -> listOf(
                "Find two cells in the same row, column, or box with identical candidates",
                "These two cells 'claim' those two numbers",
                "Eliminate those numbers from other cells in the same group"
            )
            HintGenerator.Technique.NAKED_TRIPLE -> listOf(
                "Find three cells in the same group whose candidates are a subset of three numbers",
                "Those three cells claim those three numbers",
                "Eliminate those three numbers from other cells in the group"
            )
            HintGenerator.Technique.HIDDEN_PAIR -> listOf(
                "Find two numbers that appear only in the same two cells within a group",
                "Those cells must contain those two numbers",
                "Eliminate other candidates from those two cells"
            )
            HintGenerator.Technique.HIDDEN_TRIPLE -> listOf(
                "Find three numbers that appear only in the same three cells within a group",
                "Those cells must contain those three numbers",
                "Eliminate other candidates from those three cells"
            )
            HintGenerator.Technique.X_WING -> listOf(
                "Look for a number that appears in exactly 2 cells in each of 2 rows",
                "These occurrences must align in the same 2 columns",
                "Eliminate that number from other cells in those columns"
            )
            HintGenerator.Technique.SWORDFISH -> listOf(
                "Extension of X-Wing to 3 rows/columns",
                "Look for a number appearing in exactly 2-3 cells in 3 rows",
                "If they align in the same 3 columns, eliminate from those columns in other rows"
            )
            HintGenerator.Technique.XY_WING -> listOf(
                "Find a pivot cell with 2 candidates and two wing cells that see the pivot",
                "Wings share one candidate with the pivot and have a common third candidate",
                "Eliminate the common candidate from cells seeing both wings"
            )
            HintGenerator.Technique.XYZ_WING -> listOf(
                "Find a pivot with 3 candidates and two bi-value wings that see the pivot",
                "Wings share subsets of the pivot's candidates",
                "Eliminate the common Z candidate from cells seeing all three"
            )
            else -> listOf(
                "This is an advanced technique",
                "Look for patterns in candidate arrangements",
                "Practice easier techniques first to build familiarity"
            )
        }
    }
}
