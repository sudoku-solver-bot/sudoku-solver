package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class CelebrationRequest(
    val userId: String,
    val eventType: String,
    val difficulty: String? = null
)

@Serializable
data class CelebrationResponse(
    val type: String,
    val intensity: String,
    val message: String,
    val animation: String,
    val sound: String?,
    val duration: Int
)

fun Route.celebrationRoutes() {
    val celebrationSystem = CelebrationSystem()

    post("/celebration") {
        val request = call.receive<CelebrationRequest>()
        
        val difficulty = request.difficulty?.let { 
            DifficultyLevel.valueOf(it) 
        } ?: DifficultyLevel.MEDIUM
        
        val celebration = celebrationSystem.getCelebration(
            eventType = CelebrationType.valueOf(request.eventType),
            difficulty = difficulty
        )
        
        call.respond(
            CelebrationResponse(
                type = celebration.type.name,
                intensity = celebration.intensity.name,
                message = celebration.message,
                animation = celebration.animation,
                sound = celebration.sound,
                duration = celebration.duration
            )
        )
    }
}
