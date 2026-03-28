package will.sudoku.solver

/**
 * Simplified tutorial system.
 */
data class Tutorial(
    val id: String,
    val title: String,
    val description: String
)

class TutorialSystem {
    fun getTutorials(): List<Tutorial> {
        return listOf(
            Tutorial("1", "Single Candidate", "Learn to find cells with only one possible number"),
            Tutorial("2", "Single Position", "Learn to find where a number can only go in one place"),
            Tutorial("3", "First Puzzle", "Solve your first complete puzzle with guidance")
        )
    }
}
