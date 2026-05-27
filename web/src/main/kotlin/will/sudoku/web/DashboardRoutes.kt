package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class DashboardRequest(
    val studentId: String
)

@Serializable
data class DashboardResponse(
    val studentId: String,
    val studentName: String = "Student",
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val puzzlesSolved: Int = 0,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val averageTimeSeconds: Int = 0,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val streak: Int = 0,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val level: Int = 1
)

fun Route.dashboardRoutes() {
    post("/dashboard/report") {
        val request = call.receive<DashboardRequest>()
        call.respond(
            DashboardResponse(
                studentId = request.studentId,
                studentName = request.studentId,
                puzzlesSolved = 0,
                averageTimeSeconds = 0,
                streak = 0,
                level = 1
            )
        )
    }
}
