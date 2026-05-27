package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class ProgressRequest(
    val userId: String
)

@Serializable
data class ProgressResponse(
    val userId: String,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val level: Int = 1,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val xp: Int = 0,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val puzzlesSolved: Int = 0
)

fun Route.progressRoutes() {
    post("/progress") {
        val request = call.receive<ProgressRequest>()
        call.respond(
            ProgressResponse(
                userId = request.userId,
                level = 1,
                xp = 0,
                puzzlesSolved = 0
            )
        )
    }
}
