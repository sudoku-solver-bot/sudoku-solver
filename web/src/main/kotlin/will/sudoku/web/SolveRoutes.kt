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
import will.sudoku.solver.Solver
import will.sudoku.solver.SolverWithMetrics

@Serializable
data class SolveRequest(
    val puzzle: String,
    val includeMetrics: Boolean = false
)

@Serializable
data class SolveResponse(
    val solved: Boolean,
    val solution: String? = null,
    val metrics: SolverMetricsResponse? = null,
    val error: String? = null
)

@Serializable
data class SolverMetricsResponse(
    val solveTimeMs: Double,
    val backtrackingCount: Int,
    val maxRecursionDepth: Int,
    val propagationPasses: Int,
    val cellsProcessed: Int
)

fun Route.solveRoutes() {
    post("/solve") {
        val request = try {
            call.receive<SolveRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                SolveResponse(solved = false, error = "Invalid request body: ${e.message}")
            )
            return@post
        }

        // Validate puzzle length
        if (request.puzzle.length != 81) {
            call.respond(
                HttpStatusCode.BadRequest,
                SolveResponse(
                    solved = false,
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
                SolveResponse(solved = false, error = "Invalid puzzle: ${e.message}")
            )
            return@post
        }

        // Solve with or without metrics
        if (request.includeMetrics) {
            val solver = SolverWithMetrics()
            val result = solver.solveWithMetrics(board)
            val solvedBoard = result.solvedBoard

            if (solvedBoard != null) {
                call.respond(
                    SolveResponse(
                        solved = true,
                        solution = boardToString(solvedBoard),
                        metrics = SolverMetricsResponse(
                            solveTimeMs = result.metrics.totalSolveTimeNanos / 1_000_000.0,
                            backtrackingCount = result.metrics.backtrackingCount,
                            maxRecursionDepth = result.metrics.maxRecursionDepth,
                            propagationPasses = result.metrics.propagationPasses,
                            cellsProcessed = result.metrics.cellsProcessed
                        )
                    )
                )
            } else {
                call.respond(
                    SolveResponse(
                        solved = false,
                        error = "No solution found",
                        metrics = SolverMetricsResponse(
                            solveTimeMs = result.metrics.totalSolveTimeNanos / 1_000_000.0,
                            backtrackingCount = result.metrics.backtrackingCount,
                            maxRecursionDepth = result.metrics.maxRecursionDepth,
                            propagationPasses = result.metrics.propagationPasses,
                            cellsProcessed = result.metrics.cellsProcessed
                        )
                    )
                )
            }
        } else {
            val solver = Solver()
            val solvedBoard = solver.solve(board)

            if (solvedBoard != null) {
                call.respond(
                    SolveResponse(
                        solved = true,
                        solution = boardToString(solvedBoard)
                    )
                )
            } else {
                call.respond(
                    SolveResponse(
                        solved = false,
                        error = "No solution found"
                    )
                )
            }
        }
    }
}

/**
 * Convert a solved board back to a simple 81-character string.
 */
private fun boardToString(board: Board): String {
    return buildString {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (board.isConfirmed(coord)) {
                    val candidates = board.candidateValues(coord)
                    append(candidates.first())
                } else {
                    append('.')
                }
            }
        }
    }
}
