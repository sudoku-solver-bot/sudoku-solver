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
        
        // If constraint propagation fully solved the board but the raw board wasn't solved,
        // the puzzle is solvable by basic techniques alone. Find the first empty cell
        // that CP solved and hint at it as a Naked Single instead of reporting "Puzzle Complete".
        // This prevents the misleading "Puzzle Complete" message on unsolved easy puzzles.
        if (board.isSolved() && !rawBoard.isSolved()) {
            for (row in 0..8) {
                for (col in 0..8) {
                    val coord = will.sudoku.solver.Coord(row, col)
                    if (rawBoard.value(coord) == 0 && board.value(coord) != 0) {
                        val value = board.value(coord)
                        return@post call.respond(
                            HintResponse(
                                type = HintType.NAKED_SINGLE.name,
                                cell = CellCoordinate(row, col),
                                technique = "Naked Single",
                                explanation = "Cell (${row + 1}, ${col + 1}) can only be $value! " +
                                    "Check the row, column, and box — every other number is already taken.",
                                teachingPoints = listOf(
                                    "Look at row ${row + 1}, column ${col + 1}, and the 3x3 box",
                                    "Only one number is possible in this cell",
                                    "Easy puzzles can often be solved entirely this way!"
                                )
                            )
                        )
                    }
                }
            }
        }
        
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
