package will.sudoku.solver

import will.sudoku.solver.Coord

/**
 * Represents a hint for the next move with educational explanation.
 * Designed to teach kids WHY a move works, not just WHAT to do.
 */
data class TeachingHint(
    val type: HintType,
    val cell: Coord,
    val value: Int,
    val technique: String,
    val explanation: String,
    val confidence: Double,  // 0.0-1.0, how certain is this hint
    val difficulty: DifficultyLevel,
    val relatedCells: List<Coord> = emptyList(),
    val teachingPoints: List<String> = emptyList()
)

/**
 * Types of hints available.
 */
enum class HintType {
    SINGLE_CANDIDATE,      // Only one number can go in this cell
    SINGLE_POSITION,       // This number can only go in one place in row/col/box
    CANDIDATE_LINE,        // Number must be in this line within a box
    NAKED_PAIR,            // Two cells with same two candidates
    HIDDEN_PAIR,           // Two numbers only appear in two cells
    X_WING,                // Advanced pattern
    GUESS,                 // No logical move found, must guess
    NO_HINT                // Puzzle solved or no valid moves
}

/**
 * Educational hint provider that teaches solving techniques.
 */
class TeachingHintProvider {
    
    /**
     * Get the best next hint for a puzzle.
     * Returns hints with explanations suitable for kids.
     */
    fun getHint(board: Board, targetDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM): TeachingHint {
        // Try techniques from easiest to hardest
        return tryEasyTechniques(board, targetDifficulty)
            ?: tryMediumTechniques(board, targetDifficulty)
            ?: tryHardTechniques(board, targetDifficulty)
            ?: tryGuessing(board, targetDifficulty)
            ?: TeachingHint(
                type = HintType.NO_HINT,
                cell = Coord(0, 0),
                value = 0,
                technique = "No hint available",
                explanation = "The puzzle is either solved or has no valid solution.",
                confidence = 1.0,
                difficulty = targetDifficulty
            )
    }
    
    private fun tryEasyTechniques(board: Board, targetDifficulty: DifficultyLevel): TeachingHint? {
        // Try single candidate first
        for (row in 0..8) {
            for (col in 0..8) {
                val coord = Coord(row, col)
                if (board.value(coord) == 0) {
                    val candidates = board.candidateValues(coord)
                    if (candidates.size == 1) {
                        val value = candidates.first()
                        return TeachingHint(
                            type = HintType.SINGLE_CANDIDATE,
                            cell = coord,
                            value = value,
                            technique = "Single Candidate",
                            explanation = generateSingleCandidateExplanation(coord, value, candidates),
                            confidence = 1.0,
                            difficulty = DifficultyLevel.EASY,
                            teachingPoints = listOf(
                                "Look at one cell at a time",
                                "Check which numbers 1-9 can go there",
                                "If only one number fits, that's your answer!"
                            )
                        )
                    }
                }
            }
        }
        
        // Try single position
        // Check each row
        for (row in 0..8) {
            for (num in 1..9) {
                val positions = findPositionsInRow(board, row, num)
                if (positions.size == 1) {
                    return TeachingHint(
                        type = HintType.SINGLE_POSITION,
                        cell = positions.first(),
                        value = num,
                        technique = "Single Position (Row)",
                        explanation = generateSinglePositionRowExplanation(positions.first(), row, num),
                        confidence = 1.0,
                        difficulty = DifficultyLevel.EASY,
                        relatedCells = positions,
                        teachingPoints = listOf(
                            "Look at one number at a time",
                            "Check where it can go in this row",
                            "If only one spot works, that's it!"
                        )
                    )
                }
            }
        }
        
        return null
    }
    
    private fun tryMediumTechniques(board: Board, targetDifficulty: DifficultyLevel): TeachingHint? {
        // Only provide medium hints if target difficulty allows
        if (targetDifficulty == DifficultyLevel.EASY) return null
        
        // Try candidate lines (simplified)
        // Try naked pairs (simplified)
        // For now, return null - can be expanded
        
        return null
    }
    
    private fun tryHardTechniques(board: Board, targetDifficulty: DifficultyLevel): TeachingHint? {
        // Only provide hard hints if target difficulty allows
        if (targetDifficulty == DifficultyLevel.EASY || targetDifficulty == DifficultyLevel.MEDIUM) return null
        
        // Try X-Wing, etc.
        return null
    }
    
    private fun tryGuessing(board: Board, targetDifficulty: DifficultyLevel): TeachingHint? {
        // Find cell with fewest candidates for educated guess
        var bestCell: Coord? = null
        var fewestCandidates = 10
        var bestCandidates = emptySet<Int>()
        
        for (row in 0..8) {
            for (col in 0..8) {
                val coord = Coord(row, col)
                if (board.value(coord) == 0) {
                    val candidates = board.candidateValues(coord)
                    if (candidates.size < fewestCandidates && candidates.isNotEmpty()) {
                        bestCell = coord
                        fewestCandidates = candidates.size
                        bestCandidates = candidates
                    }
                }
            }
        }
        
        return bestCell?.let {
            TeachingHint(
                type = HintType.GUESS,
                cell = it,
                value = bestCandidates.first(),
                technique = "Educated Guess",
                explanation = "No sure moves found. Try cell (${it.row + 1}, ${it.col + 1}) with ${bestCandidates.size} options. Start with ${bestCandidates.first()}!",
                confidence = 0.5,  // Lower confidence for guesses
                difficulty = targetDifficulty,
                teachingPoints = listOf(
                    "Sometimes we need to guess",
                    "Pick cells with fewer options first",
                    "If it doesn't work, try another option"
                )
            )
        }
    }
    
    // Helper functions
    private fun findPositionsInRow(board: Board, row: Int, num: Int): List<Coord> {
        val positions = mutableListOf<Coord>()
        for (col in 0..8) {
            val coord = Coord(row, col)
            if (board.value(coord) == 0 && num in board.candidateValues(coord)) {
                positions.add(coord)
            }
        }
        return positions
    }
    
    // Explanation generators (kid-friendly)
    private fun generateSingleCandidateExplanation(coord: Coord, value: Int, candidates: Set<Int>): String {
        return "Look at cell (row ${coord.row + 1}, column ${coord.col + 1}). " +
               "All the numbers from 1-9 are already used in its row, column, or box, " +
               "except for the number $value! So this cell must be $value."
    }
    
    private fun generateSinglePositionRowExplanation(coord: Coord, row: Int, num: Int): String {
        return "Look at row ${row + 1}. The number $num can only go in one place - " +
               "column ${coord.col + 1}! All the other spots already have $num blocked. " +
               "So this cell must be $num."
    }
}
