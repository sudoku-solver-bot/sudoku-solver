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
        ),
        
        // Module 4: Candidate Lines (Medium technique)
        TutorialModule(
            id = "candidate-lines",
            title = "Candidate Lines",
            description = "Learn to spot numbers that must be in a line within a box!",
            difficulty = DifficultyLevel.MEDIUM,
            technique = "Candidate Lines",
            steps = listOf(
                TutorialStep(
                    instruction = "Sometimes a number must be in one ROW or COLUMN within a box. This helps us eliminate it from other places!",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Look at a 3x3 box. If a number can only go in one row or column within that box, it can't go in that row/column in other boxes!",
                    successMessage = "Great! You found a candidate line!",
                    teachingPoint = "When a number is limited to one line in a box, it can't be in that line in other boxes!"
                )
            ),
            practicePuzzles = listOf(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 12,
            prerequisites = listOf("basic-combination")
        ),
        
        // Module 5: Naked Pairs (Medium technique)
        TutorialModule(
            id = "naked-pairs",
            title = "Naked Pairs",
            description = "Two cells, two numbers - eliminate candidates!",
            difficulty = DifficultyLevel.MEDIUM,
            technique = "Naked Pairs",
            steps = listOf(
                TutorialStep(
                    instruction = "When two cells in a row/column/box have the same TWO candidates, those numbers can't be anywhere else in that group!",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Look for two cells that share exactly two candidate numbers. Those two numbers are 'locked' into those cells!",
                    successMessage = "Excellent! You found a naked pair!",
                    teachingPoint = "Naked pairs let you eliminate those candidates from other cells in the same row/column/box!"
                )
            ),
            practicePuzzles = listOf(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 15,
            prerequisites = listOf("candidate-lines")
        ),
        
        // Module 6: Hidden Pairs (Medium technique)
        TutorialModule(
            id = "hidden-pairs",
            title = "Hidden Pairs",
            description = "Two numbers hiding in only two cells!",
            difficulty = DifficultyLevel.MEDIUM,
            technique = "Hidden Pairs",
            steps = listOf(
                TutorialStep(
                    instruction = "When two numbers appear in only TWO cells in a group, those cells can't have any other numbers!",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Look for two numbers that only appear in the same two cells. Those cells must contain ONLY those two numbers!",
                    successMessage = "You found the hidden pair!",
                    teachingPoint = "Hidden pairs help you eliminate other candidates from those cells, even if the cells have more candidates!"
                )
            ),
            practicePuzzles = listOf(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 15,
            prerequisites = listOf("naked-pairs")
        ),
        
        // Module 7: X-Wing (Hard technique)
        TutorialModule(
            id = "x-wing",
            title = "X-Wing Pattern",
            description = "Master this powerful elimination technique!",
            difficulty = DifficultyLevel.HARD,
            technique = "X-Wing",
            steps = listOf(
                TutorialStep(
                    instruction = "When a candidate appears in the same two positions in two rows, you can eliminate it from those columns in other rows!",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Look for a rectangle pattern with the same candidate at all four corners. This 'locks' the number to those four positions!",
                    successMessage = "X-Wing found! You're becoming a master!",
                    teachingPoint = "X-Wing is like finding a box in the grid that locks a number in place across multiple rows and columns!"
                )
            ),
            practicePuzzles = listOf(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 20,
            prerequisites = listOf("hidden-pairs")
        ),
        
        // Module 8: XY-Wing (Expert technique)
        TutorialModule(
            id = "xy-wing",
            title = "XY-Wing Technique",
            description = "Advanced technique for expert solvers!",
            difficulty = DifficultyLevel.EXPERT,
            technique = "XY-Wing",
            steps = listOf(
                TutorialStep(
                    instruction = "The XY-Wing uses three cells to eliminate candidates. Find the pivot cell with two candidates, and two wing cells that share one candidate each with the pivot!",
                    highlight = emptyList(),
                    expectedAction = TutorialAction.FIND_PATTERN,
                    hint = "Look for: Pivot (AB), Wing1 (AC), Wing2 (BC). The candidate C can be eliminated from any cell that sees both wings!",
                    successMessage = "XY-Wing master! You're solving like a champion!",
                    teachingPoint = "XY-Wing is powerful but requires practice to spot quickly. Look for cells with exactly two candidates!"
                )
            ),
            practicePuzzles = listOf(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"
            ),
            estimatedMinutes = 25,
            prerequisites = listOf("x-wing")
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
