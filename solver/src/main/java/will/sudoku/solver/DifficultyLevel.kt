package will.sudoku.solver

/**
 * Difficulty levels for age-appropriate puzzles.
 */
enum class DifficultyLevel(
    val displayName: String,
    val targetAgeRange: String,
    val minClues: Int,
    val maxClues: Int
) {
    EASY("Easy", "8-9 years", 36, 45),
    MEDIUM("Medium", "10-11 years", 30, 35),
    HARD("Hard", "12-13 years", 26, 29),
    EXPERT("Expert", "14+ years", 22, 25),
    MASTER("Master", "Expert+", 17, 21);
    
    companion object {
        fun forAge(age: Int): DifficultyLevel {
            return when {
                age <= 9 -> EASY
                age <= 11 -> MEDIUM
                age <= 13 -> HARD
                else -> EXPERT
            }
        }
    }
}
