package will.sudoku.solver

/**
 * Tutorial module for teaching Sudoku techniques to kids.
 * Each module focuses on one technique with interactive lessons.
 */
data class TutorialModule(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: DifficultyLevel,
    val technique: String,
    val steps: List<TutorialStep>,
    val practicePuzzles: List<String>,
    val estimatedMinutes: Int,
    val prerequisites: List<String> = emptyList()
)

/**
 * Single step in a tutorial lesson.
 */
data class TutorialStep(
    val instruction: String,
    val highlight: List<Coord>,
    val expectedAction: TutorialAction,
    val hint: String,
    val successMessage: String,
    val teachingPoint: String
)

/**
 * Action expected from user in tutorial.
 */
enum class TutorialAction {
    FILL_CELL,
    IDENTIFY_CANDIDATES,
    FIND_PATTERN,
    SELECT_CELL,
    COMPLETE_PUZZLE
}

/**
 * Progress tracking for tutorials.
 */
data class TutorialProgress(
    val moduleId: String,
    val completedSteps: Set<Int>,
    val currentStep: Int,
    val startedAt: Long,
    val completedAt: Long?,
    val attempts: Int,
    val hintsUsed: Int
) {
    val isCompleted: Boolean
        get() = completedAt != null
    
    val completionPercentage: Int
        get() = if (completedSteps.isEmpty()) 0
                else (completedSteps.size * 100 / (completedSteps.size + 1))
}

/**
 * Tutorial system for teaching Sudoku techniques.
 */
class TutorialSystem {
    
    private val modules = listOf(
        // Module 1: Basics - Single Candidate
        TutorialModule(
            id = "single-candidate-basics",
            title = "Finding Single Candidates",
            description = "Learn the most basic Sudoku technique: finding cells where only one number fits!",
            difficulty = DifficultyLevel.EASY,
            technique = "Single Candidate",
            steps = listOf(
                TutorialStep(
                    instruction = "Welcome! Let's learn Sudoku together. Look at the highlighted cell. What numbers could go there?",
                    highlight = listOf(Coord(0, 0)),
                    expectedAction = TutorialAction.IDENTIFY_CANDIDATES,
                    hint = "Check the row, column, and 3x3 box. Which numbers 1-9 are NOT already used?",
                    successMessage = "Great! You found the candidates. Now let's look closer...",
                    teachingPoint = "Every empty cell has 'candidates' - numbers that COULD go there."
                ),
                TutorialStep(
                    instruction = "This cell has only ONE candidate: the number 5! Can you fill it in?",
                    highlight = listOf(Coord(0, 0)),
                    expectedAction = TutorialAction.FILL_CELL,
                    hint = "Click the cell and type 5",
                    successMessage = "Perfect! You just used the Single Candidate technique!",
                    teachingPoint = "When a cell has only one candidate, we know it MUST be that number!"
                )
            ),
            practicePuzzles = listOf(
                "500000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "050000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 5,
            prerequisites = emptyList()
        ),
        
        // Module 2: Single Position
        TutorialModule(
            id = "single-position-basics",
            title = "Single Position in Rows",
            description = "Learn how to find numbers that can only go in ONE place in a row!",
            difficulty = DifficultyLevel.EASY,
            technique = "Single Position",
            steps = listOf(
                TutorialStep(
                    instruction = "Look at row 1. Where can the number 7 go?",
                    highlight = (0..8).map { Coord(0, it) },
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Check each empty cell in the row. Where is 7 NOT blocked?",
                    successMessage = "Excellent! You found that 7 can only go in one spot!",
                    teachingPoint = "Sometimes a number can only fit in ONE cell in a row/column/box!"
                ),
                TutorialStep(
                    instruction = "Now fill in the 7!",
                    highlight = listOf(Coord(0, 4)),
                    expectedAction = TutorialAction.FILL_CELL,
                    hint = "Click the cell and type 7",
                    successMessage = "Great job! You used Single Position!",
                    teachingPoint = "This technique works for rows, columns, AND 3x3 boxes!"
                )
            ),
            practicePuzzles = listOf(
                "123456000000000000000000000000000000000000000000000000000000000000000000000000000",
                "000000080000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 8,
            prerequisites = listOf("single-candidate-basics")
        ),
        
        // Module 3: Putting it Together
        TutorialModule(
            id = "basic-combination",
            title = "Solving Your First Puzzle",
            description = "Use both techniques you learned to solve a complete puzzle!",
            difficulty = DifficultyLevel.EASY,
            technique = "Combined Basic Techniques",
            steps = listOf(
                TutorialStep(
                    instruction = "You know two powerful techniques now! Let's solve a puzzle using both.",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.COMPLETE_PUZZLE,
                    hint = "Look for cells with one candidate, or numbers that can only go one place",
                    successMessage = "Amazing! You solved your first puzzle!",
                    teachingPoint = "Real puzzles need both techniques. Practice makes perfect!"
                )
            ),
            practicePuzzles = listOf(
                "530070000600195000098000060800060003400803001700020006060000280000419005000080079"
            ),
            estimatedMinutes = 15,
            prerequisites = listOf("single-candidate-basics", "single-position-basics")
        )
    )
    
    /**
     * Get all available tutorial modules.
     */
    fun getModules(): List<TutorialModule> = modules
    
    /**
     * Get a specific tutorial module by ID.
     */
    fun getModule(id: String): TutorialModule? = modules.find { it.id == id }
    
    /**
     * Get recommended next module based on completed modules.
     */
    fun getNextModule(completedModuleIds: Set<String>): TutorialModule? {
        return modules.firstOrNull { module ->
            // Module not yet completed
            module.id !in completedModuleIds &&
            // All prerequisites completed
            module.prerequisites.all { it in completedModuleIds }
        }
    }
    
    /**
     * Get modules suitable for a difficulty level.
     */
    fun getModulesForDifficulty(difficulty: DifficultyLevel): List<TutorialModule> {
        return modules.filter { it.difficulty == difficulty }
    }
    
    /**
     * Calculate overall learning progress.
     */
    fun calculateProgress(completedModuleIds: Set<String>): LearningProgress {
        val totalModules = modules.size
        val completedCount = completedModuleIds.size
        val percentage = if (totalModules > 0) (completedCount * 100 / totalModules) else 0
        
        return LearningProgress(
            totalModules = totalModules,
            completedModules = completedCount,
            percentage = percentage,
            nextModule = getNextModule(completedModuleIds)
        )
    }
}

/**
 * Overall learning progress summary.
 */
data class LearningProgress(
    val totalModules: Int,
    val completedModules: Int,
    val percentage: Int,
    val nextModule: TutorialModule?
)
