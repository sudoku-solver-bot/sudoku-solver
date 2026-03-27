package will.sudoku.solver

/**
 * Puzzle difficulty levels designed for kids aged 8-14.
 * 
 * Levels are designed to be progressively challenging:
 * - EASY (8-9 years): More clues, only basic techniques needed
 * - MEDIUM (10-11 years): Moderate clues, requires basic strategies
 * - HARD (12-13 years): Fewer clues, intermediate techniques required
 * - EXPERT (14+ years): Minimal clues, advanced techniques needed
 */
enum class DifficultyLevel(
    val displayName: String,
    val minClues: Int,
    val maxClues: Int,
    val targetAgeRange: String,
    val description: String,
    val techniques: List<String>
) {
    EASY(
        displayName = "Easy",
        minClues = 40,
        maxClues = 45,
        targetAgeRange = "8-9 years",
        description = "Great for beginners! Uses basic logic.",
        techniques = listOf(
            "Single candidate",
            "Single position"
        )
    ),
    
    MEDIUM(
        displayName = "Medium",
        minClues = 32,
        maxClues = 39,
        targetAgeRange = "10-11 years",
        description = "Requires some strategy. Good for learning!",
        techniques = listOf(
            "Single candidate",
            "Single position",
            "Candidate lines",
            "Double pairs"
        )
    ),
    
    HARD(
        displayName = "Hard",
        minClues = 26,
        maxClues = 31,
        targetAgeRange = "12-13 years",
        description = "Challenging! Needs advanced techniques.",
        techniques = listOf(
            "All medium techniques",
            "Naked pairs/triples",
            "Hidden pairs/triples",
            "X-Wing"
        )
    ),
    
    EXPERT(
        displayName = "Expert",
        minClues = 22,
        maxClues = 25,
        targetAgeRange = "14+ years",
        description = "Very challenging! For puzzle masters.",
        techniques = listOf(
            "All hard techniques",
            "XY-Wing",
            "Swordfish",
            "Forcing chains"
        )
    );
    
    companion object {
        /**
         * Get difficulty level by name (case-insensitive).
         */
        fun fromString(name: String): DifficultyLevel? {
            return values().find { 
                it.name.equals(name, ignoreCase = true) ||
                it.displayName.equals(name, ignoreCase = true)
            }
        }
        
        /**
         * Get recommended difficulty for age.
         */
        fun forAge(age: Int): DifficultyLevel {
            return when {
                age <= 9 -> EASY
                age <= 11 -> MEDIUM
                age <= 13 -> HARD
                else -> EXPERT
            }
        }
    }
    
    /**
     * Get random clue count within difficulty range.
     */
    fun randomClueCount(): Int {
        return (minClues..maxClues).random()
    }
}
