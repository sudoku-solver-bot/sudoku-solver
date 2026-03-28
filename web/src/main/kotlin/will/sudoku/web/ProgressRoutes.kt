package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class ProgressRequest(
    val userId: String
)

@Serializable
data class ProgressResponse(
    val userId: String,
    val level: Int,
    val xp: Int,
    val puzzlesSolved: Int
)

fun Route.progressRoutes() {
    val progressSystem = ProgressSystem()

    post("/api/v1/progress") {
        val request = call.receive<ProgressRequest>()
        val progress = progressSystem.getProgress(request.userId)
        call.respond(
            ProgressResponse(
                userId = progress.userId,
                level = progress.level,
                xp = progress.xp,
                puzzlesSolved = progress.puzzlesSolved
            )
        )
    }
}
