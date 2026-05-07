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
import will.sudoku.solver.HintType
import will.sudoku.solver.SimpleCandidateEliminator
import will.sudoku.solver.TeachingHintProvider

fun Board.withConstraintPropagation(): Board {
    val eliminator = SimpleCandidateEliminator()
    eliminator.eliminate(this)
    return this
}

@Serializable
data class HintRequest(
    val puzzle: String,
    val technique: String? = null  // Optional: target technique for tutorial mode
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
        val rawBoard: Board = try {
            BoardReader.readBoard(request.puzzle)
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid puzzle: ${e.message}")
            )
        }

        // Check if puzzle was already solved BEFORE constraint propagation
        // Easy puzzles can be fully solved by constraint propagation alone,
        // so checking after would incorrectly report them as "Puzzle Complete"
        if (rawBoard.isSolved()) {
            return@post call.respond(
                HintResponse(
                    type = HintType.COMPLETE.name,
                    cell = null,
                    technique = "Puzzle Complete",
                    explanation = "This puzzle is already solved! All cells are filled correctly. Great job!",
                    teachingPoints = listOf(
                        "You've completed this puzzle — no moves needed",
                        "Challenge yourself with a new puzzle to keep improving"
                    )
                )
            )
        }

        val board = rawBoard.withConstraintPropagation()
        
        // Map requested technique string to HintGenerator.Technique enum
        val targetTechnique = request.technique?.let { techName ->
            HintGenerator.Technique.entries.find {
                it.displayName.equals(techName, ignoreCase = true) ||
                it.name.equals(techName, ignoreCase = true)
            }
        }
        
        val hint = hintProvider.getHint(board, targetTechnique)

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
