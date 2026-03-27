package will.sudoku.solver

/**
 * Achievement earned by completing challenges.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,  // Emoji or icon identifier
    val category: AchievementCategory,
    val points: Int,
    val requirement: AchievementRequirement
)

/**
 * Achievement categories.
 */
enum class AchievementCategory {
    LEARNING,      // Complete tutorials
    SOLVING,       // Solve puzzles
    STREAK,        // Daily/weekly streaks
    DIFFICULTY,    // Master difficulty levels
    TECHNIQUE,     // Master specific techniques
    SPEED,         // Fast solving
    SPECIAL        // Special accomplishments
}

/**
 * Requirements to unlock achievement.
 */
data class AchievementRequirement(
    val type: RequirementType,
    val target: Int,  // Number to reach
    val metadata: Map<String, String> = emptyMap()
)

enum class RequirementType {
    TUTORIALS_COMPLETED,
    PUZZLES_SOLVED,
    DAILY_STREAK,
    TECHNIQUE_USED_COUNT,
    FAST_SOLVES,
    DIFFICULTY_MASTERED,
    PERFECT_GAMES  // No hints used
}

/**
 * User's overall progress and level.
 */
data class UserProgress(
    val userId: String,
    val level: Int,
    val experiencePoints: Int,
    val totalPoints: Int,  // From achievements
    val achievements: List<UserAchievement>,
    val stats: UserStats,
    val currentStreak: Int,
    val longestStreak: Int
) {
    /**
     * Calculate XP needed for next level.
     * Formula: level^2 * 100
     */
    fun xpForNextLevel(): Int {
        return (level + 1) * (level + 1) * 100
    }
    
    /**
     * Progress percentage to next level.
     */
    fun levelProgress(): Double {
        val currentLevelXP = level * level * 100
        val nextLevelXP = xpForNextLevel()
        val xpInCurrentLevel = experiencePoints - currentLevelXP
        val xpNeeded = nextLevelXP - currentLevelXP
        return if (xpNeeded > 0) xpInCurrentLevel.toDouble() / xpNeeded else 1.0
    }
}

/**
 * User's unlocked achievement.
 */
data class UserAchievement(
    val achievementId: String,
    val unlockedAt: Long,
    val progress: Int = 100  // Percentage complete
)

/**
 * User's gameplay statistics.
 */
data class UserStats(
    val puzzlesSolved: Int,
    val puzzlesAttempted: Int,
    val hintsUsed: Int,
    val tutorialsCompleted: Int,
    val averageSolveTimeMs: Double,
    val fastestSolveMs: Long,
    val perfectGames: Int,  // No hints
    val gamesByDifficulty: Map<String, Int>
)

/**
 * Progress tracking system with gamification.
 */
class ProgressTracker {
    
    // Define all achievements
    private val achievements = listOf(
        // Learning achievements
        Achievement(
            id = "first-steps",
            name = "First Steps",
            description = "Complete your first tutorial",
            icon = "🎯",
            category = AchievementCategory.LEARNING,
            points = 10,
            requirement = AchievementRequirement(
                type = RequirementType.TUTORIALS_COMPLETED,
                target = 1
            )
        ),
        Achievement(
            id = "student",
            name = "Eager Student",
            description = "Complete 5 tutorials",
            icon = "📚",
            category = AchievementCategory.LEARNING,
            points = 25,
            requirement = AchievementRequirement(
                type = RequirementType.TUTORIALS_COMPLETED,
                target = 5
            )
        ),
        Achievement(
            id = "master-student",
            name = "Master Student",
            description = "Complete all tutorials",
            icon = "🎓",
            category = AchievementCategory.LEARNING,
            points = 100,
            requirement = AchievementRequirement(
                type = RequirementType.TUTORIALS_COMPLETED,
                target = 3  // Total tutorials available
            )
        ),
        
        // Solving achievements
        Achievement(
            id = "first-solve",
            name = "Problem Solver",
            description = "Solve your first puzzle",
            icon = "🧩",
            category = AchievementCategory.SOLVING,
            points = 15,
            requirement = AchievementRequirement(
                type = RequirementType.PUZZLES_SOLVED,
                target = 1
            )
        ),
        Achievement(
            id = "puzzle-fan",
            name = "Puzzle Fan",
            description = "Solve 10 puzzles",
            icon = "🔥",
            category = AchievementCategory.SOLVING,
            points = 50,
            requirement = AchievementRequirement(
                type = RequirementType.PUZZLES_SOLVED,
                target = 10
            )
        ),
        Achievement(
            id = "puzzle-master",
            name = "Puzzle Master",
            description = "Solve 100 puzzles",
            icon = "👑",
            category = AchievementCategory.SOLVING,
            points = 200,
            requirement = AchievementRequirement(
                type = RequirementType.PUZZLES_SOLVED,
                target = 100
            )
        ),
        
        // Streak achievements
        Achievement(
            id = "daily-player",
            name = "Daily Player",
            description = "Play 3 days in a row",
            icon = "📅",
            category = AchievementCategory.STREAK,
            points = 30,
            requirement = AchievementRequirement(
                type = RequirementType.DAILY_STREAK,
                target = 3
            )
        ),
        Achievement(
            id = "dedicated",
            name = "Dedicated Solver",
            description = "Play 7 days in a row",
            icon = "⭐",
            category = AchievementCategory.STREAK,
            points = 75,
            requirement = AchievementRequirement(
                type = RequirementType.DAILY_STREAK,
                target = 7
            )
        ),
        Achievement(
            id = "unstoppable",
            name = "Unstoppable",
            description = "Play 30 days in a row",
            icon = "🏆",
            category = AchievementCategory.STREAK,
            points = 300,
            requirement = AchievementRequirement(
                type = RequirementType.DAILY_STREAK,
                target = 30
            )
        ),
        
        // Speed achievements
        Achievement(
            id = "quick-thinker",
            name = "Quick Thinker",
            description = "Solve a puzzle in under 1 minute",
            icon = "⚡",
            category = AchievementCategory.SPEED,
            points = 40,
            requirement = AchievementRequirement(
                type = RequirementType.FAST_SOLVES,
                target = 1,
                metadata = mapOf("maxTimeMs" to "60000")
            )
        ),
        
        // Perfect games
        Achievement(
            id = "perfectionist",
            name = "Perfectionist",
            description = "Solve 5 puzzles without hints",
            icon = "💎",
            category = AchievementCategory.SPECIAL,
            points = 60,
            requirement = AchievementRequirement(
                type = RequirementType.PERFECT_GAMES,
                target = 5
            )
        )
    )
    
    /**
     * Get all available achievements.
     */
    fun getAchievements(): List<Achievement> = achievements
    
    /**
     * Check which achievements a user has earned based on their stats.
     */
    fun checkAchievements(stats: UserStats, streak: Int): List<Achievement> {
        return achievements.filter { achievement ->
            when (achievement.requirement.type) {
                RequirementType.TUTORIALS_COMPLETED ->
                    stats.tutorialsCompleted >= achievement.requirement.target
                
                RequirementType.PUZZLES_SOLVED ->
                    stats.puzzlesSolved >= achievement.requirement.target
                
                RequirementType.DAILY_STREAK ->
                    streak >= achievement.requirement.target
                
                RequirementType.PERFECT_GAMES ->
                    stats.perfectGames >= achievement.requirement.target
                
                RequirementType.FAST_SOLVES -> {
                    val maxTime = achievement.requirement.metadata["maxTimeMs"]?.toLong() ?: 0
                    stats.fastestSolveMs <= maxTime && stats.fastestSolveMs > 0
                }
                
                else -> false
            }
        }
    }
    
    /**
     * Calculate level from total XP.
     */
    fun calculateLevel(totalXP: Int): Int {
        // Level = sqrt(totalXP / 100)
        return kotlin.math.sqrt(totalXP.toDouble() / 100).toInt()
    }
    
    /**
     * Get progress toward next achievement in a category.
     */
    fun getNextAchievementProgress(
        stats: UserStats,
        streak: Int,
        category: AchievementCategory
    ): AchievementProgress? {
        val categoryAchievements = achievements
            .filter { it.category == category }
            .sortedBy { it.requirement.target }
        
        for (achievement in categoryAchievements) {
            val current = when (achievement.requirement.type) {
                RequirementType.TUTORIALS_COMPLETED -> stats.tutorialsCompleted
                RequirementType.PUZZLES_SOLVED -> stats.puzzlesSolved
                RequirementType.DAILY_STREAK -> streak
                RequirementType.PERFECT_GAMES -> stats.perfectGames
                else -> 0
            }
            
            if (current < achievement.requirement.target) {
                return AchievementProgress(
                    achievement = achievement,
                    current = current,
                    target = achievement.requirement.target,
                    percentage = (current * 100 / achievement.requirement.target)
                )
            }
        }
        
        return null  // All achievements in category completed
    }
}

/**
 * Progress toward a specific achievement.
 */
data class AchievementProgress(
    val achievement: Achievement,
    val current: Int,
    val target: Int,
    val percentage: Int
)
