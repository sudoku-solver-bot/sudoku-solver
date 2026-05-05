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
    val technique: String,
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

@Serializable
data class QuizQuestion(
    val id: String,
    val puzzle: String,
    val question: String,
    val hint: String,
    val answerCell: Int,
    val answerValue: String,
    val highlightCells: List<Int> = emptyList(),
    val highlightColor: String = "blue"
)

@Serializable
data class QuizSet(
    val id: String,
    val belt: String,
    val beltName: String,
    val beltEmoji: String,
    val beltColor: String,
    val technique: String,
    val questions: List<QuizQuestion>
)

@Serializable
data class PracticePuzzle(
    val id: String,
    val puzzle: String,
    val description: String
)

@Serializable
data class PracticeSet(
    val id: String,
    val technique: String,
    val tutorialId: String,
    val belt: String,
    val puzzles: List<PracticePuzzle>
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

    // --- Quiz & Practice Routes ---

    // Load quiz data
    val quizzesJson = javaClass.classLoader
        .getResource("tutorials/quizzes.json")
        ?.readText()
    val quizzes = if (quizzesJson != null) json.decodeFromString<List<QuizSet>>(quizzesJson) else emptyList()
    val quizzesByBelt = quizzes.associateBy { it.belt }

    // Load practice puzzle data
    val practiceJson = javaClass.classLoader
        .getResource("tutorials/practice-puzzles.json")
        ?.readText()
    val practiceSets = if (practiceJson != null) json.decodeFromString<List<PracticeSet>>(practiceJson) else emptyList()
    val practiceByTutorialId = practiceSets.associateBy { it.tutorialId }

    // GET /tutorials/quizzes — list all quiz sets
    get("/tutorials/quizzes") {
        call.respond(quizzes)
    }

    // GET /tutorials/practice-sets — list all practice sets
    get("/tutorials/practice-sets") {
        call.respond(practiceSets)
    }

    // GET /tutorials/practice — also list all practice sets (convenience alias)
    get("/tutorials/practice") {
        call.respond(practiceSets)
    }

    // GET /tutorials/quizzes/{belt} — get quiz for a specific belt
    get("/tutorials/quizzes/{belt}") {
        val belt = call.parameters["belt"] ?: ""
        val quiz = quizzesByBelt[belt]
        if (quiz == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Quiz not found for belt: $belt"))
            return@get
        }
        call.respond(quiz)
    }

    // GET /tutorials/quizzes/{belt}/board — get quiz puzzle with candidates
    get("/tutorials/quizzes/{belt}/board") {
        val belt = call.parameters["belt"] ?: ""
        val quiz = quizzesByBelt[belt]
        if (quiz == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Quiz not found for belt: $belt"))
            return@get
        }

        val question = quiz.questions.firstOrNull()
        if (question == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "No questions in quiz"))
            return@get
        }

        val board: Board = try {
            BoardReader.readBoard(question.puzzle)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid puzzle: ${e.message}"))
            return@get
        }

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

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
            puzzle = question.puzzle,
            candidates = candidates
        ))
    }

    // GET /tutorials/practice — list all practice sets
    get("/tutorials/practice") {
        call.respond(practiceSets)
    }

    // GET /tutorials/practice/{tutorialId} — get practice puzzles for a technique
    get("/tutorials/practice/{tutorialId}") {
        val tutorialId = call.parameters["tutorialId"] ?: ""
        val set = practiceByTutorialId[tutorialId]
        if (set == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Practice puzzles not found for: $tutorialId"))
            return@get
        }
        call.respond(set)
    }

    // GET /tutorials/practice/{tutorialId}/{puzzleId}/board — get practice puzzle with candidates
    get("/tutorials/practice/{tutorialId}/{puzzleId}/board") {
        val tutorialId = call.parameters["tutorialId"] ?: ""
        val puzzleId = call.parameters["puzzleId"] ?: ""
        val set = practiceByTutorialId[tutorialId]
        if (set == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Practice set not found for: $tutorialId"))
            return@get
        }

        val puzzleData = set.puzzles.find { it.id == puzzleId }
        if (puzzleData == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Puzzle not found: $puzzleId"))
            return@get
        }

        val board: Board = try {
            BoardReader.readBoard(puzzleData.puzzle)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid puzzle: ${e.message}"))
            return@get
        }

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

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
            puzzle = puzzleData.puzzle,
            candidates = candidates
        ))
    }
}
