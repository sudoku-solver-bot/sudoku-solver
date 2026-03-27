package will.sudoku.web

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class UserStatsRequest(
    val userId: String,
    val puzzlesSolved: Int,
    val puzzlesAttempted: Int,
    val hintsUsed: Int,
    val tutorialsCompleted: Int,
    val averageSolveTimeMs: Double,
    val fastestSolveMs: Long,
    val perfectGames: Int,
    val gamesByDifficulty: Map<String, Int>,
    val currentStreak: Int
)

@Serializable
data class ProgressResponse(
    val level: Int,
    val experiencePoints: Int,
    val xpForNextLevel: Int,
    val levelProgress: Double,
    val totalPoints: Int,
    val achievements: List<AchievementResponse>,
    val stats: UserStatsResponse,
    val nextAchievements: List<AchievementProgressResponse>
)

@Serializable
data class AchievementResponse(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val points: Int,
    val unlocked: Boolean,
    val unlockedAt: Long?
)

@Serializable
data class UserStatsResponse(
    val puzzlesSolved: Int,
    val puzzlesAttempted: Int,
    val hintsUsed: Int,
    val tutorialsCompleted: Int,
    val averageSolveTimeMs: Double,
    val fastestSolveMs: Long,
    val perfectGames: Int,
    val gamesByDifficulty: Map<String, Int>,
    val currentStreak: Int,
    val longestStreak: Int
)

@Serializable
data class AchievementProgressResponse(
    val achievement: AchievementResponse,
    val current: Int,
    val target: Int,
    val percentage: Int
)

@Serializable
data class AchievementsListResponse(
    val achievements: List<AchievementResponse>,
    val categories: List<String>
)

fun Route.progressRoutes() {
    val progressTracker = ProgressTracker()

    get("/achievements", {
        tags = listOf("Progress")
        description = "Get all available achievements"
        response {
            HttpStatusCode.OK to {
                description = "List of all achievements"
                body<AchievementsListResponse>()
            }
        }
    }) {
        val allAchievements = progressTracker.getAchievements()
        
        call.respond(
            AchievementsListResponse(
                achievements = allAchievements.map { achievement ->
                    AchievementResponse(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        icon = achievement.icon,
                        category = achievement.category.name,
                        points = achievement.points,
                        unlocked = false,  // Client should track
                        unlockedAt = null
                    )
                },
                categories = AchievementCategory.values().map { it.name }
            )
        )
    }
    
    post("/progress", {
        tags = listOf("Progress")
        description = "Calculate user progress and achievements"
        request {
            body<UserStatsRequest> {
                example("sample") {
                    value = UserStatsRequest(
                        userId = "user123",
                        puzzlesSolved = 15,
                        puzzlesAttempted = 18,
                        hintsUsed = 8,
                        tutorialsCompleted = 2,
                        averageSolveTimeMs = 45000.0,
                        fastestSolveMs = 12000,
                        perfectGames = 7,
                        gamesByDifficulty = mapOf("EASY" to 10, "MEDIUM" to 5),
                        currentStreak = 4
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "User progress and achievements"
                body<ProgressResponse>()
            }
        }
    }) {
        val request = call.receive<UserStatsRequest>()
        
        val stats = UserStats(
            puzzlesSolved = request.puzzlesSolved,
            puzzlesAttempted = request.puzzlesAttempted,
            hintsUsed = request.hintsUsed,
            tutorialsCompleted = request.tutorialsCompleted,
            averageSolveTimeMs = request.averageSolveTimeMs,
            fastestSolveMs = request.fastestSolveMs,
            perfectGames = request.perfectGames,
            gamesByDifficulty = request.gamesByDifficulty
        )
        
        // Check earned achievements
        val earnedAchievements = progressTracker.checkAchievements(stats, request.currentStreak)
        val totalPoints = earnedAchievements.sumOf { it.points }
        val level = progressTracker.calculateLevel(totalPoints)
        
        // Calculate XP (1 puzzle = 10 XP, 1 tutorial = 50 XP, achievements = points)
        val experiencePoints = (request.puzzlesSolved * 10) + 
                              (request.tutorialsCompleted * 50) + 
                              totalPoints
        
        // Get next achievements in each category
        val nextAchievements = AchievementCategory.values().mapNotNull { category ->
            progressTracker.getNextAchievementProgress(stats, request.currentStreak, category)
        }.take(3)  // Top 3 closest achievements
        
        // Build response
        val allAchievements = progressTracker.getAchievements()
        val earnedIds = earnedAchievements.map { it.id }.toSet()
        
        call.respond(
            ProgressResponse(
                level = level,
                experiencePoints = experiencePoints,
                xpForNextLevel = (level + 1) * (level + 1) * 100,
                levelProgress = calculateLevelProgress(experiencePoints, level),
                totalPoints = totalPoints,
                achievements = allAchievements.map { achievement ->
                    val isEarned = achievement.id in earnedIds
                    AchievementResponse(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        icon = achievement.icon,
                        category = achievement.category.name,
                        points = achievement.points,
                        unlocked = isEarned,
                        unlockedAt = if (isEarned) System.currentTimeMillis() else null
                    )
                },
                stats = UserStatsResponse(
                    puzzlesSolved = stats.puzzlesSolved,
                    puzzlesAttempted = stats.puzzlesAttempted,
                    hintsUsed = stats.hintsUsed,
                    tutorialsCompleted = stats.tutorialsCompleted,
                    averageSolveTimeMs = stats.averageSolveTimeMs,
                    fastestSolveMs = stats.fastestSolveMs,
                    perfectGames = stats.perfectGames,
                    gamesByDifficulty = stats.gamesByDifficulty,
                    currentStreak = request.currentStreak,
                    longestStreak = request.currentStreak  // Client should track
                ),
                nextAchievements = nextAchievements.map { progress ->
                    AchievementProgressResponse(
                        achievement = AchievementResponse(
                            id = progress.achievement.id,
                            name = progress.achievement.name,
                            description = progress.achievement.description,
                            icon = progress.achievement.icon,
                            category = progress.achievement.category.name,
                            points = progress.achievement.points,
                            unlocked = false,
                            unlockedAt = null
                        ),
                        current = progress.current,
                        target = progress.target,
                        percentage = progress.percentage
                    )
                }
            )
        )
    }
}

private fun calculateLevelProgress(xp: Int, level: Int): Double {
    val currentLevelXP = level * level * 100
    val nextLevelXP = (level + 1) * (level + 1) * 100
    val xpInCurrentLevel = xp - currentLevelXP
    val xpNeeded = nextLevelXP - currentLevelXP
    return if (xpNeeded > 0) xpInCurrentLevel.toDouble() / xpNeeded else 1.0
}
