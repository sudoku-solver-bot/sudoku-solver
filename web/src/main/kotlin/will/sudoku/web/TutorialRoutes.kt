package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader
import will.sudoku.solver.SimpleCandidateEliminator
import will.sudoku.solver.Coord
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class TutorialStep(
    val order: Int,
    val type: String,
    val cells: List<Int> = emptyList(),
    val highlightColor: String = "blue",
    val text: String,
    val answer: Int? = null,
    val answerValue: String? = null,
    val eliminatedValue: Int? = null
)

@Serializable
data class TutorialLesson(
    val id: String,
    val order: Int,
    val title: String,
    val belt: String,
    val beltColor: String,
    val beltEmoji: String,
    val beltName: String,
    val description: String,
    val concept: String,
    val examplePuzzle: String,
    val steps: List<TutorialStep>
)

@Serializable
data class TutorialSummary(
    val id: String,
    val order: Int,
    val title: String,
    val belt: String,
    val beltColor: String,
    val beltEmoji: String,
    val beltName: String,
    val description: String,
    val completed: Boolean = false
)

@Serializable
data class TutorialBoardResponse(
    val puzzle: String,
    val candidates: Map<String, List<Int>>
)

@Serializable
data class TutorialProgress(
    val completedLessons: List<String>,
    val totalLessons: Int,
    val currentBelt: String,
    val currentBeltEmoji: String
)

fun Route.tutorialRoutes() {
    // Load lessons from JSON resource
    val lessonsJson = javaClass.classLoader
        .getResource("tutorials/lessons.json")
        ?.readText()
        ?: throw RuntimeException("tutorials/lessons.json not found")

    val json = Json { ignoreUnknownKeys = true }
    val lessons = json.decodeFromString<List<TutorialLesson>>(lessonsJson)
    val lessonsById = lessons.associateBy { it.id }

    // In-memory completion tracking
    val completedTutorials = ConcurrentHashMap<String, Boolean>()

    // GET /tutorials — list all tutorials with completion status
    get("/tutorials") {
        val summaries = lessons.map { lesson ->
            TutorialSummary(
                id = lesson.id,
                order = lesson.order,
                title = lesson.title,
                belt = lesson.belt,
                beltColor = lesson.beltColor,
                beltEmoji = lesson.beltEmoji,
                beltName = lesson.beltName,
                description = lesson.description,
                completed = completedTutorials.containsKey(lesson.id)
            )
        }
        call.respond(summaries)
    }

    // GET /tutorials/progress — overall progress
    get("/tutorials/progress") {
        val completed = completedTutorials.keys.toList()
        val currentLesson = lessons.firstOrNull { !completedTutorials.containsKey(it.id) }
        call.respond(TutorialProgress(
            completedLessons = completed,
            totalLessons = lessons.size,
            currentBelt = currentLesson?.beltName ?: "Master",
            currentBeltEmoji = currentLesson?.beltEmoji ?: "🎓"
        ))
    }

    // GET /tutorials/{id} — get lesson details
    get("/tutorials/{id}") {
        val id = call.parameters["id"] ?: ""
        val lesson = lessonsById[id]
        if (lesson == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tutorial not found: $id"))
            return@get
        }
        call.respond(lesson)
    }

    // GET /tutorials/{id}/board — get example puzzle with candidates
    get("/tutorials/{id}/board") {
        val id = call.parameters["id"] ?: ""
        val lesson = lessonsById[id]
        if (lesson == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tutorial not found: $id"))
            return@get
        }

        val board: Board = try {
            BoardReader.readBoard(lesson.examplePuzzle)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid puzzle: ${e.message}"))
            return@get
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

        call.respond(TutorialBoardResponse(
            puzzle = lesson.examplePuzzle,
            candidates = candidates
        ))
    }

    // POST /tutorials/{id}/complete — mark tutorial as completed
    post("/tutorials/{id}/complete") {
        val id = call.parameters["id"] ?: ""
        if (lessonsById.containsKey(id)) {
            completedTutorials[id] = true
            call.respond(mapOf("status" to "ok", "lesson" to id, "completed" to true))
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Tutorial not found: $id"))
        }
    }
}
