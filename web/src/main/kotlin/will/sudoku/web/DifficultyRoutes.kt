package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class GenerateDifficultyRequest(
    val difficulty: String? = null,
    val age: Int? = null
)

@Serializable
data class GenerateDifficultyResponse(
    val puzzle: String,
    val difficulty: String,
    val targetAge: String
)

fun Route.difficultyRoutes() {
    post("/api/v1/generate/difficulty") {
        val request = call.receive<GenerateDifficultyRequest>()
        
        val difficulty = when {
            request.age != null -> DifficultyLevel.forAge(request.age)
            request.difficulty != null -> DifficultyLevel.valueOf(request.difficulty.uppercase())
            else -> DifficultyLevel.MEDIUM
        }
        
        val raterLevel = when (difficulty) {
            DifficultyLevel.EASY -> DifficultyRater.Level.EASY
            DifficultyLevel.MEDIUM -> DifficultyRater.Level.MEDIUM
            DifficultyLevel.HARD -> DifficultyRater.Level.HARD
            DifficultyLevel.EXPERT -> DifficultyRater.Level.EXPERT
        }
        
        val board = PuzzleGenerator.generate(raterLevel)
        
        val puzzleString = StringBuilder().apply {
            for (row in 0..8) {
                for (col in 0..8) {
                    val value = board.value(Coord(row, col))
                    append(if (value == 0) '0' else '0' + value)
                }
            }
        }.toString()
        
        call.respond(
            GenerateDifficultyResponse(
                puzzle = puzzleString,
                difficulty = difficulty.displayName,
                targetAge = difficulty.targetAgeRange
            )
        )
    }
    
    get("/api/v1/generate/difficulty/{level}") {
        val level = call.parameters["level"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing difficulty level")
        )
        
        val difficulty = try {
            DifficultyLevel.valueOf(level.uppercase())
        } catch (e: IllegalArgumentException) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid difficulty level: $level")
            )
        }
        
        val raterLevel = when (difficulty) {
            DifficultyLevel.EASY -> DifficultyRater.Level.EASY
            DifficultyLevel.MEDIUM -> DifficultyRater.Level.MEDIUM
            DifficultyLevel.HARD -> DifficultyRater.Level.HARD
            DifficultyLevel.EXPERT -> DifficultyRater.Level.EXPERT
        }
        
        val board = PuzzleGenerator.generate(raterLevel)
        
        val puzzleString = StringBuilder().apply {
            for (row in 0..8) {
                for (col in 0..8) {
                    val value = board.value(Coord(row, col))
                    append(if (value == 0) '0' else '0' + value)
                }
            }
        }.toString()
        
        call.respond(
            GenerateDifficultyResponse(
                puzzle = puzzleString,
                difficulty = difficulty.displayName,
                targetAge = difficulty.targetAgeRange
            )
        )
    }
}
