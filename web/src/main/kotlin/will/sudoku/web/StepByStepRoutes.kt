package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.BoardReader
import will.sudoku.solver.SolverWithSteps
import will.sudoku.solver.SolvingProgress

@Serializable
data class SolveStepsRequest(
    val puzzle: String
)

@Serializable
data class SolveStepsResponse(
    val solved: Boolean,
    val solution: String? = null,
    val steps: List<StepResponse>,
    val totalSteps: Int,
    val solveTimeMs: Long? = null,
    val error: String? = null
)

@Serializable
data class StepResponse(
    val stepNumber: Int,
    val stepType: String,
    val affectedCells: List<CellCoord>,
    val values: List<Int>,
    val explanation: String,
    val boardState: String? = null
)

@Serializable
data class CellCoord(
    val row: Int,
    val col: Int
)

fun Route.stepByStepRoutes() {
    post("/solve/steps") { call.handleSolveSteps() }
    post("/step-by-step") { call.handleSolveSteps() }
}

private suspend fun ApplicationCall.handleSolveSteps() {
    val request = try {
        receive<SolveStepsRequest>()
    } catch (e: Exception) {
        respond(
            HttpStatusCode.BadRequest,
            SolveStepsResponse(
                solved = false,
                steps = emptyList(),
                totalSteps = 0,
                error = "Invalid request body: ${e.message}"
            )
        )
        return
    }

    // Validate puzzle length
    if (request.puzzle.length != 81) {
        respond(
            HttpStatusCode.BadRequest,
            SolveStepsResponse(
                solved = false,
                steps = emptyList(),
                totalSteps = 0,
                error = "Puzzle must be 81 characters, got ${request.puzzle.length}"
            )
        )
        return
    }

    // Parse and solve with steps
    val board = try {
        BoardReader.readBoard(request.puzzle)
    } catch (e: Exception) {
        respond(
            HttpStatusCode.BadRequest,
            SolveStepsResponse(
                solved = false,
                steps = emptyList(),
                totalSteps = 0,
                error = "Invalid puzzle: ${e.message}"
            )
        )
        return
    }

    val (solvedBoard, progress) = SolverWithSteps().solveWithSteps(board)

    // Convert steps to response format
    val stepResponses = progress.steps.map { step ->
        StepResponse(
            stepNumber = step.stepNumber,
            stepType = step.stepType.displayName,
            affectedCells = step.affectedCells.map { CellCoord(it.row, it.col) },
            values = step.values.toList(),
            explanation = step.explanation,
            boardState = step.boardState
        )
    }

    // Convert solution to string
    val solutionString = solvedBoard?.let { b ->
        buildString {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val coord = will.sudoku.solver.Coord(row, col)
                    if (b.isConfirmed(coord)) {
                        append(b.value(coord))
                    } else {
                        append('.')
                    }
                }
            }
        }
    }

    respond(
        SolveStepsResponse(
            solved = progress.isSolved,
            solution = solutionString,
            steps = stepResponses,
            totalSteps = progress.steps.size,
            solveTimeMs = progress.solveTimeMs()
        )
    )
}
