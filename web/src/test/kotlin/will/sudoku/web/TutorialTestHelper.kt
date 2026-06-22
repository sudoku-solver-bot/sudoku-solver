package will.sudoku.web

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import will.sudoku.solver.*

/**
 * Shared helper for tutorial and quiz validation tests.
 *
 * Loads lesson/quiz data from resources and provides utilities
 * for board preparation and candidate inspection.
 */
object TutorialTestHelper {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Data Models ──────────────────────────────────────────

    @Serializable
    data class LessonStep(
        val order: Int,
        val type: String,
        val cells: List<Int> = emptyList(),
        val text: String = "",
        val eliminatedValue: Int? = null,
        val answerValue: String? = null,
        val answer: Int? = null,
        val highlightColor: String = "blue"
    )

    @Serializable
    data class TutorialLesson(
        val id: String,
        val title: String = "",
        val belt: String = "",
        val technique: String = "",
        val examplePuzzle: String,
        val steps: List<LessonStep> = emptyList()
    )

    @Serializable
    data class QuizQuestion(
        val id: String,
        val puzzle: String,
        val question: String = "",
        val hint: String = "",
        val answerCell: Int,
        val answerValue: Int,
        val explanation: String = "",
        val highlightCells: List<Int> = emptyList(),
        val highlightColor: String = "blue",
        val options: List<String> = emptyList(),
        val correctAnswer: Int = 0
    )

    @Serializable
    data class QuizSet(
        val id: String,
        val belt: String,
        val beltName: String = "",
        val questions: List<QuizQuestion> = emptyList()
    )

    @Serializable
    data class PracticePuzzleEntry(
        val id: String,
        val puzzle: String,
        val description: String = ""
    )

    @Serializable
    data class PracticePuzzleSet(
        val id: String,
        val technique: String = "",
        val tutorialId: String = "",
        val belt: String = "",
        val puzzles: List<PracticePuzzleEntry> = emptyList()
    )

    // ── Data Loading ─────────────────────────────────────────

    fun loadLessons(): List<TutorialLesson> {
        val text = javaClass.classLoader
            ?.getResource("tutorials/lessons.json")
            ?.readText()
            ?: error("tutorials/lessons.json not found in classpath")
        return json.decodeFromString(text)
    }

    fun loadQuizzes(): List<QuizSet> {
        val text = javaClass.classLoader
            ?.getResource("tutorials/quizzes.json")
            ?.readText()
            ?: error("tutorials/quizzes.json not found in classpath")
        return json.decodeFromString(text)
    }

    fun loadPracticePuzzles(): List<PracticePuzzleSet> {
        val text = javaClass.classLoader
            ?.getResource("tutorials/practice-puzzles.json")
            ?.readText()
            ?: error("tutorials/practice-puzzles.json not found in classpath")
        return json.decodeFromString(text)
    }

    // ── Board Utilities ──────────────────────────────────────

    /**
     * Parse a puzzle string into a Board.
     */
    fun parseBoard(puzzle: String): Board {
        return BoardReader.readBoard(puzzle)
    }

    /**
     * Run basic elimination (simple + exclusion) on a fresh board.
     * Returns the board with candidates populated.
     */
    fun prepareBoardBasic(puzzle: String): Board {
        val board = parseBoard(puzzle)
        SimpleCandidateEliminator().eliminate(board)
        return board
    }

    /**
     * Run elimination pipeline up to the technique's level.
     * Stops BEFORE the target technique so its candidates are still present.
     *
     * Strategy: run just enough basic elimination (SimpleCandidateEliminator only)
     * to populate candidates. Don't run hidden singles or advanced techniques
     * because the tutorial's reveal step shows what the technique eliminates,
     * and those candidates must still be present.
     */
    fun prepareBoardForTechnique(puzzle: String, techniqueId: String): Board {
        val board = parseBoard(puzzle)
        // Phase 1: Simple elimination — just populate candidates from peers
        SimpleCandidateEliminator().eliminate(board)
        return board
    }

    /**
     * Get the set of candidate values at a given cell index (0–80).
     */
    fun getCandidates(board: Board, cellIndex: Int): Set<Int> {
        val row = cellIndex / 9
        val col = cellIndex % 9
        return board.candidateValues(Coord(row, col)).toSet()
    }

    /**
     * Check if a cell at the given index is filled (confirmed value).
     */
    fun isFilled(board: Board, cellIndex: Int): Boolean {
        val row = cellIndex / 9
        val col = cellIndex % 9
        return board.isConfirmed(Coord(row, col))
    }

    /**
     * Get the confirmed value at a cell index, or null if not filled.
     */
    fun getValue(board: Board, cellIndex: Int): Int? {
        val row = cellIndex / 9
        val col = cellIndex % 9
        return if (board.isConfirmed(Coord(row, col))) board.value(Coord(row, col)) else null
    }

    /**
     * Convert a cell index to human-readable "R{row}C{col}" format.
     */
    fun cellLabel(cellIndex: Int): String {
        val row = cellIndex / 9 + 1
        val col = cellIndex % 9 + 1
        return "R${row}C${col}"
    }
}
