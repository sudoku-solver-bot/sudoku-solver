package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.PuzzleValidator

@Serializable
data class ValidateRequest(
    val puzzle: String,
    val checkUniqueness: Boolean = true
)

@Serializable
data class ValidateResponse(
    val valid: Boolean,
    val uniqueSolution: Boolean? = null,
    val solutionCount: Int? = null,
    val errors: List<ValidationErrorData> = emptyList(),
    val error: String? = null
)

@Serializable
data class ValidationErrorData(
    val type: String,
    val message: String
)

fun Route.validateRoutes() {
    post("/validate") {
        val request = try {
            call.receive<ValidateRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ValidateResponse(valid = false, error = "Invalid request body: ${e.message}")
            )
            return@post
        }

        // Validate puzzle length
        if (request.puzzle.length != 81) {
            call.respond(
                HttpStatusCode.BadRequest,
                ValidateResponse(
                    valid = false,
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
                ValidateResponse(valid = false, error = "Invalid puzzle: ${e.message}")
            )
            return@post
        }

        // Validate
        val result = PuzzleValidator.validate(board, request.checkUniqueness)

        call.respond(
            ValidateResponse(
                valid = result.isValid,
                uniqueSolution = if (request.checkUniqueness) result.hasUniqueSolution else null,
                solutionCount = if (request.checkUniqueness) result.solutionCount else null,
                errors = result.errors.map { 
                    ValidationErrorData(
                        type = it.type.name,
                        message = it.message
                    )
                }
            )
        )
    }
}
