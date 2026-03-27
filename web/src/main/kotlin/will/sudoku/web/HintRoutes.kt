package will.sudoku.web

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

    post("/hint") {

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
