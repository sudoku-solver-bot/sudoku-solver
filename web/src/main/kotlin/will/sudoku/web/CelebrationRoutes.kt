package will.sudoku.web

import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class CelebrationRequest(
    val puzzleSolved: Boolean = false,
    val perfectGame: Boolean = false,
    val fastSolve: Boolean = false,
    val dailyChallenge: Boolean = false,
    val streakDays: Int = 0,
    val levelUp: Boolean = false,
    val achievementUnlock: Boolean = false
)

@Serializable
data class CelebrationResponse(
    val type: String,
    val intensity: String,
    val duration: Int,
    val sound: String?,
    val particles: Int,
    val message: String,
    val emoji: List<String>
)

@Serializable
data class EncouragementResponse(
    val message: String
)

fun Route.celebrationRoutes() {
    val celebrationSystem = CelebrationSystem()

    post("/celebration", {
        tags = listOf("Gamification")
        description = "Get appropriate celebration for a completion"
        request {
            body<CelebrationRequest> {
                example("perfect-fast") {
                    value = CelebrationRequest(
                        puzzleSolved = true,
                        perfectGame = true,
                        fastSolve = true
                    )
                }
                example("standard") {
                    value = CelebrationRequest(
                        puzzleSolved = true
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Celebration configuration"
                body<CelebrationResponse>()
            }
        }
    }) {
        val request = call.receive<CelebrationRequest>()
        
        val condition = CelebrationCondition(
            puzzleSolved = request.puzzleSolved,
            perfectGame = request.perfectGame,
            fastSolve = request.fastSolve,
            dailyChallenge = request.dailyChallenge,
            streakDays = request.streakDays,
            levelUp = request.levelUp,
            achievementUnlock = request.achievementUnlock
        )
        
        val celebration = celebrationSystem.getCelebration(condition)
        
        call.respond(
            CelebrationResponse(
                type = celebration.type.name,
                intensity = celebration.intensity.name,
                duration = celebration.duration,
                sound = celebration.sound,
                particles = celebration.particles,
                message = celebration.message,
                emoji = celebration.emoji
            )
        )
    }
    
    get("/encouragement", {
        tags = listOf("Gamification")
        description = "Get random encouraging message for kids"
        response {
            HttpStatusCode.OK to {
                description = "Encouraging message"
                body<EncouragementResponse>()
            }
        }
    }) {
        val message = celebrationSystem.getRandomEncouragement()
        
        call.respond(
            EncouragementResponse(message = message)
        )
    }
}
