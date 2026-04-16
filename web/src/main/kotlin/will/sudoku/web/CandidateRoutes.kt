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
import will.sudoku.solver.SimpleCandidateEliminator

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

        // Run constraint propagation to populate candidates
        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        // Collect candidates for all unconfirmed cells
        val candidates = mutableMapOf<String, List<Int>>()
        for (coord in Coord.all) {
            if (!board.isConfirmed(coord)) {
                val values = board.candidateValues(coord).toList()
                if (values.isNotEmpty()) {
                    candidates[coord.index.toString()] = values
                }
            }
        }

        call.respond(CandidatesResponse(candidates = candidates))
    }
}
