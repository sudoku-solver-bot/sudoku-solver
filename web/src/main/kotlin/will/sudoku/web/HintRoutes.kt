package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.TeachingHintProvider

@Serializable
data class HintRequest(
    val puzzle: String
)

@Serializable
data class HintResponse(
    val type: String,
    val cell: CellCoordinate?,
    val technique: String,
    val explanation: String,
    val teachingPoints: List<String>
)

@Serializable
data class CellCoordinate(
    val row: Int,
    val col: Int
)

fun Route.hintRoutes() {
    val hintProvider = TeachingHintProvider()

    post("/hint") {
        val request = call.receive<HintRequest>()

        // Validate puzzle string
        val validation = PuzzleValidator.validate(request.puzzle)
        if (!validation.valid) {
            return@post call.respond(
                PuzzleValidator.getHttpStatusCode(validation.errorCode!!),
                mapOf("error" to validation.errorMessage)
            )
        }

        // Parse board using BoardReader for consistent format handling (accepts 0 and .)
        val board: Board = try {
            BoardReader.readBoard(request.puzzle)
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid puzzle: ${e.message}")
            )
        }

        val hint = hintProvider.getHint(board)
        
        call.respond(
            HintResponse(
                type = hint.type.name,
                cell = hint.cell?.let { CellCoordinate(it.row, it.col) },
                technique = hint.technique,
                explanation = hint.explanation,
                teachingPoints = hint.teachingPoints
            )
        )
    }
}
