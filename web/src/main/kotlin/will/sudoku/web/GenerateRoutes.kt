package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class GenerateRequest(
    val difficulty: String = "MEDIUM",
    val seed: Long? = null
)

@Serializable
data class GenerateResponse(
    val puzzle: String,
    val difficulty: String,
    val error: String? = null
)

fun Route.generateRoutes() {
    post("/generate") {
        val request = try {
            call.receive<GenerateRequest>()
        } catch (e: Exception) {
            // Use defaults if body is empty or invalid
            GenerateRequest()
        }

        // Parse difficulty level
        val level = try {
            DifficultyRater.Level.valueOf(request.difficulty.uppercase())
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                GenerateResponse(
                    puzzle = "",
                    difficulty = "",
                    error = "Invalid difficulty: ${request.difficulty}. Valid values: EASY, MEDIUM, HARD, EXPERT, MASTER"
                )
            )
            return@post
        }

        // Generate puzzle
        val board = if (request.seed != null) {
            PuzzleGenerator.generate(level, request.seed)
        } else {
            PuzzleGenerator.generate(level)
        }

        // Convert to string format
        val puzzleString = boardToPuzzleString(board)

        call.respond(
            GenerateResponse(
                puzzle = puzzleString,
                difficulty = level.displayName
            )
        )
    }

    // GET endpoint for convenience
    get("/generate") {
        val difficultyParam = call.request.queryParameters["difficulty"] ?: "MEDIUM"
        val seedParam = call.request.queryParameters["seed"]?.toLong()

        // Parse difficulty level
        val level = try {
            DifficultyRater.Level.valueOf(difficultyParam.uppercase())
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                GenerateResponse(
                    puzzle = "",
                    difficulty = "",
                    error = "Invalid difficulty: $difficultyParam. Valid values: EASY, MEDIUM, HARD, EXPERT, MASTER"
                )
            )
            return@get
        }

        // Generate puzzle
        val board = if (seedParam != null) {
            PuzzleGenerator.generate(level, seedParam)
        } else {
            PuzzleGenerator.generate(level)
        }

        val puzzleString = boardToPuzzleString(board)

        call.respond(
            GenerateResponse(
                puzzle = puzzleString,
                difficulty = level.displayName
            )
        )
    }
}

/**
 * Convert a board to a puzzle string (81 characters).
 */
private fun boardToPuzzleString(board: Board): String {
    return buildString {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (board.isConfirmed(coord)) {
                    append(board.value(coord))
                } else {
                    append('.')
                }
            }
        }
    }
}
