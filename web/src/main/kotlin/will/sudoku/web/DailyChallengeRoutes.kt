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
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class DailyChallengeResponse(
    val date: String,
    val difficulty: String,
    val puzzle: String,
    val dayOfWeek: String,
    val message: String
)

@Serializable
data class StreakResponse(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompleted: Int,
    val streakActive: Boolean,
    val message: String,
    val encouragement: String
)

@Serializable
data class CompletionRequest(
    val userId: String,
    val date: String,
    val timeSeconds: Int?,
    val hintsUsed: Int
)

@Serializable
data class CompletionResponse(
    val success: Boolean,
    val newStreak: Int,
    val message: String,
    val celebration: String?
)

@Serializable
data class RecentChallengesResponse(
    val challenges: List<DailyChallengeSummary>
)

@Serializable
data class DailyChallengeSummary(
    val date: String,
    val difficulty: String,
    val dayOfWeek: String,
    val completed: Boolean
)

fun Route.dailyChallengeRoutes() {
    val dailySystem = DailyChallengeSystem()

    get("/daily", {
        tags = listOf("Daily Challenge")
        description = "Get today's daily challenge puzzle"
        response {
            HttpStatusCode.OK to {
                description = "Today's challenge"
                body<DailyChallengeResponse>()
            }
        }
    }) {
        val timezone = ZoneId.of("Asia/Hong_Kong")  // Kid-friendly timezone
        val challenge = dailySystem.getTodayChallenge(timezone)
        val today = LocalDate.now(timezone)
        
        val dayNames = listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
        
        val message = when (challenge.difficulty) {
            DifficultyLevel.EASY -> "Start the week with a gentle puzzle! 🌱"
            DifficultyLevel.MEDIUM -> "Mid-week challenge! You've got this! 💪"
            DifficultyLevel.HARD -> "Weekend warrior mode! 🎯"
            DifficultyLevel.EXPERT -> "Sunday special - for masters only! 👑"
        }
        
        call.respond(
            DailyChallengeResponse(
                date = challenge.date.toString(),
                difficulty = challenge.difficulty.displayName,
                puzzle = challenge.puzzle,
                dayOfWeek = dayNames[challenge.date.dayOfWeek.value - 1],
                message = message
            )
        )
    }
    
    post("/daily/complete", {
        tags = listOf("Daily Challenge")
        description = "Mark daily challenge as completed"
        request {
            body<CompletionRequest> {
                example("sample") {
                    value = CompletionRequest(
                        userId = "user123",
                        date = "2026-03-27",
                        timeSeconds = 120,
                        hintsUsed = 2
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Completion recorded"
                body<CompletionResponse>()
            }
        }
    }) {
        val request = call.receive<CompletionRequest>()
        val date = LocalDate.parse(request.date)
        
        // In production, save to database
        val completion = DailyChallengeCompletion(
            date = date,
            userId = request.userId,
            completed = true,
            timeSeconds = request.timeSeconds,
            hintsUsed = request.hintsUsed,
            perfectSolve = request.hintsUsed == 0
        )
        
        // Calculate new streak (simplified - in production, query from DB)
        val newStreak = 1  // Placeholder
        
        val (message, celebration) = when {
            request.hintsUsed == 0 && (request.timeSeconds ?: 0) < 60 -> {
                "Perfect AND fast!" to "LEGENDARY"
            }
            request.hintsUsed == 0 -> {
                "Perfect solve!" to "EPIC"
            }
            newStreak >= 7 -> {
                "Week streak maintained!" to "EPIC"
            }
            newStreak >= 3 -> {
                "3-day streak!" to "STANDARD"
            }
            else -> {
                "Daily challenge complete!" to "STANDARD"
            }
        }
        
        call.respond(
            CompletionResponse(
                success = true,
                newStreak = newStreak,
                message = message,
                celebration = celebration
            )
        )
    }
    
    get("/daily/streak/{userId}", {
        tags = listOf("Daily Challenge")
        description = "Get user's streak information"
        response {
            HttpStatusCode.OK to {
                description = "Streak info"
                body<StreakResponse>()
            }
        }
    }) {
        val userId = call.parameters["userId"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing userId")
        )
        
        // In production, query from database
        // Placeholder data
        val streak = StreakInfo(
            currentStreak = 5,
            longestStreak = 12,
            lastCompletedDate = LocalDate.now().minusDays(1),
            totalCompleted = 45
        )
        
        call.respond(
            StreakResponse(
                currentStreak = streak.currentStreak,
                longestStreak = streak.longestStreak,
                totalCompleted = streak.totalCompleted,
                streakActive = streak.isStreakActive(),
                message = streak.getStreakMessage(),
                encouragement = if (streak.currentStreak > 0) {
                    "Come back tomorrow to keep your streak alive! 🔥"
                } else {
                    "Start a new streak today! 🎯"
                }
            )
        )
    }
    
    get("/daily/recent", {
        tags = listOf("Daily Challenge")
        description = "Get recent daily challenges"
        response {
            HttpStatusCode.OK to {
                description = "Recent challenges"
                body<RecentChallengesResponse>()
            }
        }
    }) {
        val recentChallenges = dailySystem.getRecentChallenges(7)
        
        val dayNames = listOf(
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        )
        
        call.respond(
            RecentChallengesResponse(
                challenges = recentChallenges.map { challenge ->
                    DailyChallengeSummary(
                        date = challenge.date.toString(),
                        difficulty = challenge.difficulty.displayName,
                        dayOfWeek = dayNames[challenge.date.dayOfWeek.value - 1],
                        completed = false  // In production, check from DB
                    )
                }
            )
        )
    }
}
