package will.sudoku.web

import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.get
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

    post("/generate", {
        tags = listOf("Generation")
        description = "Generate a new Sudoku puzzle with specified difficulty"
        request {
            body<GenerateRequest> {
                description = "Puzzle generation options"
                example("easy") {
                    value = GenerateRequest(difficulty = "EASY")
                }
                example("for-age-10") {
                    value = GenerateRequest(age = 10)
                }
                example("legacy-clues") {
                    value = GenerateRequest(clues = 35)
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Puzzle generated successfully"
                body<GenerateResponse> {
                    example("generated") {
                        value = GenerateResponse(
                            puzzle = "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
                            difficulty = "EASY",
                            targetAge = "8-9 years",
                            clueCount = 42,
                            techniques = listOf("Single candidate", "Single position"),
                            description = "Great for beginners! Uses basic logic."
                        )
                    }
                }
            }
        }
    }) {
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
                description = difficulty.description
            )
        )
    }
}
