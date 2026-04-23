package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class CelebrationRequest(
    val solved: Boolean = false,
    val perfect: Boolean = false,
    val fast: Boolean = false
)

@Serializable
data class CelebrationResponse(
    val type: String,
    val intensity: String,
    val duration: Int,
    val sound: String?,
    val message: String,
    val emoji: List<String>
)

fun Route.celebrationRoutes() {
    val celebrationSystem = CelebrationSystem()

    post("/celebration") {
        val request = call.receive<CelebrationRequest>()
        
        val celebration = celebrationSystem.getCelebration(
            solved = request.solved,
            perfect = request.perfect,
            fast = request.fast
        )
        
        call.respond(
            CelebrationResponse(
                type = celebration.type.name,
                intensity = celebration.intensity,
                duration = celebration.duration,
                sound = celebration.sound,
                message = celebration.message,
                emoji = celebration.emoji
            )
        )
    }
}
