package will.sudoku.web

import io.ktor.server.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class HintRequest(
    val puzzle: String,
    val targetDifficulty: String? = null  // EASY, MEDIUM, HARD, EXPERT
)

@Serializable
data class HintResponse(
    val type: String,
    val cell: CellCoord,
    val value: Int,
    val technique: String,
    val explanation: String,
    val confidence: Double,
    val difficulty: String,
    val relatedCells: List<CellCoord>,
    val teachingPoints: List<String>
)

@Serializable
data class CellCoord(
    val row: Int,
    val col: Int
)

fun Route.hintRoutes() {
    val hintProvider = TeachingHintProvider()

    post("/hint", {
        tags = listOf("Teaching")
        description = "Get a teaching hint for the next move"
        request {
            body<HintRequest> {
                description = "Current puzzle state and target difficulty"
                example("sample") {
                    value = HintRequest(
                        puzzle = "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
                        targetDifficulty = "EASY"
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Hint generated successfully"
                body<HintResponse> {
                    example("hint") {
                        value = HintResponse(
                            type = "SINGLE_CANDIDATE",
                            cell = CellCoord(0, 4),
                            value = 6,
                            technique = "Single Candidate",
                            explanation = "Look at cell (row 1, column 5). Only number 6 can go there!",
                            confidence = 1.0,
                            difficulty = "EASY",
                            relatedCells = emptyList(),
                            teachingPoints = listOf(
                                "Look at one cell at a time",
                                "Check which numbers 1-9 can go there",
                                "If only one number fits, that's your answer!"
                            )
                        )
                    }
                }
            }
        }
    }) {
        val request = call.receive<HintRequest>()
        
        // Validate puzzle
        val normalizedPuzzle = BoardReader.normalize(request.puzzle)
        val validationError = BoardReader.validate(normalizedPuzzle)
        if (validationError != null) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to validationError)
            )
            return@post
        }
        
        // Parse target difficulty
        val targetDifficulty = request.targetDifficulty?.let {
            DifficultyLevel.fromString(it) ?: DifficultyLevel.MEDIUM
        } ?: DifficultyLevel.MEDIUM
        
        // Get board
        val board = BoardReader.read(normalizedPuzzle)
        
        // Get hint
        val hint = hintProvider.getHint(board, targetDifficulty)
        
        call.respond(
            HintResponse(
                type = hint.type.name,
                cell = CellCoord(hint.cell.row, hint.cell.col),
                value = hint.value,
                technique = hint.technique,
                explanation = hint.explanation,
                confidence = hint.confidence,
                difficulty = hint.difficulty.name,
                relatedCells = hint.relatedCells.map { CellCoord(it.row, it.col) },
                teachingPoints = hint.teachingPoints
            )
        )
    }
}
