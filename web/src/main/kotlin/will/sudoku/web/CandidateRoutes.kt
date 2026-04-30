package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.Coord
import will.sudoku.solver.CoordGroup

@Serializable
data class CandidatesRequest(
    val puzzle: String
)

@Serializable
data class CandidatesResponse(
    val candidates: Map<String, List<Int>>,
    val error: String? = null
)

fun Route.candidateRoutes() {
    post("/candidates") {
        val request = try {
            call.receive<CandidatesRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                CandidatesResponse(candidates = emptyMap(), error = "Invalid request body: ${e.message}")
            )
            return@post
        }

        val board: Board = try {
            BoardReader.readBoard(request.puzzle)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                CandidatesResponse(candidates = emptyMap(), error = "Invalid puzzle: ${e.message}")
            )
            return@post
        }

        // Compute candidates directly: for each empty cell, collect values that
        // do not appear among confirmed (given) cells in its row, column, or box.
        // This avoids cascading constraint propagation which can solve easy puzzles
        // and leave no unconfirmed cells to return.
        val candidates = mutableMapOf<String, List<Int>>()
        for (coord in Coord.all) {
            if (!board.isConfirmed(coord)) {
                val usedValues = mutableSetOf<Int>()
                for (group in CoordGroup.of(coord)) {
                    for (peer in group.coords) {
                        if (peer != coord && board.isConfirmed(peer)) {
                            usedValues.add(board.value(peer))
                        }
                    }
                }
                val availableValues = (1..9).filter { it !in usedValues }
                if (availableValues.isNotEmpty()) {
                    candidates[coord.index.toString()] = availableValues
                }
            }
        }

        call.respond(CandidatesResponse(candidates = candidates))
    }
}
