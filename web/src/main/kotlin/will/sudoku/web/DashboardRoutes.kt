package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class DashboardRequest(
    val studentId: String
)

@Serializable
data class DashboardResponse(
    val studentId: String,
    val studentName: String,
    val puzzlesSolved: Int,
    val averageTimeSeconds: Int,
    val streak: Int,
    val level: Int
)

fun Route.dashboardRoutes() {
    val dashboardSystem = DashboardSystem()

    post("/api/v1/dashboard/report") {
        val request = call.receive<DashboardRequest>()
        
        val report = dashboardSystem.getStudentReport(request.studentId)
        
        call.respond(
            DashboardResponse(
                studentId = report.studentId,
                studentName = report.studentName,
                puzzlesSolved = report.puzzlesSolved,
                averageTimeSeconds = report.averageTimeSeconds,
                streak = report.streak,
                level = report.level
            )
        )
    }
}
