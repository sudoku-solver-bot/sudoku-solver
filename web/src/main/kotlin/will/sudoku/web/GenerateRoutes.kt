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
    val difficulty: String? = null,  // EASY, MEDIUM, HARD, EXPERT
    val age: Int? = null,  // Alternative: specify age (8-14+)
    val clues: Int? = null  // Legacy: specify exact clue count
)

@Serializable
data class GenerateResponse(
    val puzzle: String,
    val difficulty: String,
    val targetAge: String,
    val clueCount: Int,
    val techniques: List<String>,
    val description: String
)

fun Route.generateRoutes() {
    val generator = PuzzleGenerator()

    post("/generate") {

        val request = call.receive<GenerateRequest>()
        
        // Determine difficulty level
        val difficulty = when {
            request.difficulty != null -> {
                DifficultyLevel.fromString(request.difficulty)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "error" to "Invalid difficulty. Use: EASY, MEDIUM, HARD, or EXPERT"
                        )
                    )
            }
            request.age != null -> {
                if (request.age < 5 || request.age > 18) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "error" to "Age must be between 5 and 18"
                        )
                    )
                }
                DifficultyLevel.forAge(request.age)
            }
            request.clues != null -> {
                // Legacy mode: map clue count to closest difficulty
                when {
                    request.clues >= 40 -> DifficultyLevel.EASY
                    request.clues >= 32 -> DifficultyLevel.MEDIUM
                    request.clues >= 26 -> DifficultyLevel.HARD
                    else -> DifficultyLevel.EXPERT
                }
            }
            else -> DifficultyLevel.MEDIUM  // Default
        }
        
        // Generate puzzle
        val puzzle = if (request.clues != null && request.difficulty == null && request.age == null) {
            // Legacy mode: use exact clue count
            generator.generate(clues = request.clues)
        } else {
            // New mode: use difficulty level
            generator.generate(difficulty)
        }
        
        // Count clues
        val clueCount = puzzle.values.count { it != 0 }
        
        call.respond(
            GenerateResponse(
                puzzle = puzzle.toString(),
                difficulty = difficulty.displayName,
                targetAge = difficulty.targetAgeRange,
                clueCount = clueCount,
                techniques = difficulty.techniques,
            )
        )
    }
}
