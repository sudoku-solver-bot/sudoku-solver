package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.SimpleCandidateEliminator
import will.sudoku.solver.TeachingHintProvider

fun Board.withConstraintPropagation(): Board {
    val eliminator = SimpleCandidateEliminator()
    eliminator.eliminate(this)
    return this
}

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

        // Parse board using BoardReader for consistent format handling (accepts 0 and .)
        val board: Board = try {
            BoardReader.readBoard(request.puzzle).withConstraintPropagation()
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
