package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.HintGenerator

@Serializable
data class HintRequest(
    val puzzle: String
)

@Serializable
data class HintResponse(
    val hasHint: Boolean,
    val hint: HintData? = null,
    val error: String? = null
)

@Serializable
data class HintData(
    val row: Int,
    val col: Int,
    val value: Int,
    val technique: String,
    val explanation: String
)

fun Route.hintRoutes() {
    post("/hint") {
        val request = try {
            call.receive<HintRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                HintResponse(hasHint = false, error = "Invalid request body: ${e.message}")
            )
            return@post
        }

        // Validate puzzle length
        if (request.puzzle.length != 81) {
            call.respond(
                HttpStatusCode.BadRequest,
                HintResponse(
                    hasHint = false,
                    error = "Puzzle must be 81 characters, got ${request.puzzle.length}"
                )
            )
            return@post
        }

        // Parse the puzzle
        val board: Board = try {
            BoardReader.readBoard(request.puzzle)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                HintResponse(hasHint = false, error = "Invalid puzzle: ${e.message}")
            )
            return@post
        }

        // Generate hint
        val hint = HintGenerator.generate(board)

        if (hint != null) {
            call.respond(
                HintResponse(
                    hasHint = true,
                    hint = HintData(
                        row = hint.coord.row,
                        col = hint.coord.col,
                        value = hint.value,
                        technique = hint.technique.displayName,
                        explanation = hint.explanation
                    )
                )
            )
        } else {
            call.respond(
                HintResponse(
                    hasHint = false,
                    error = "No hint available. The puzzle may require guessing or is already solved."
                )
            )
        }
    }
}
