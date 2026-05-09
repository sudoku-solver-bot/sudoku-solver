package will.sudoku.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class QuizQuestion(
    val id: String,
    val puzzle: String,
    val question: String,
    val hint: String,
    val answerCell: Int,
    val answerValue: String,
    val highlightCells: List<Int>,
    val highlightColor: String
)

@Serializable
data class Quiz(
    val id: String,
    val belt: String,
    val beltName: String,
    val questions: List<QuizQuestion>
)

class QuizDataValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadQuizzes(): List<Quiz> {
        val quizzesJson = javaClass.classLoader
            .getResource("tutorials/quizzes.json")
            ?.readText()
            ?: error("tutorials/quizzes.json not found")
        return json.decodeFromString(quizzesJson)
    }

    @Test
    fun `all quiz puzzles are valid 81-character strings`() {
        val quizzes = loadQuizzes()
        val problems = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.puzzle.length != 81) {
                    problems.add("${q.id}: puzzle is ${q.puzzle.length} chars (expected 81)")
                }
                val invalidChars = q.puzzle.filter { it !in '1'..'9' && it != '.' && it != '0' }
                if (invalidChars.isNotEmpty()) {
                    problems.add("${q.id}: puzzle contains invalid chars: $invalidChars")
                }
            }
        }

        assertTrue(problems.isEmpty(), "Quiz puzzle problems:\n${problems.joinToString("\n")}")
    }

    @Test
    fun `all answerCells point to empty cells in their puzzle`() {
        val quizzes = loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.answerCell < 0 || q.answerCell >= 81) {
                    violations.add("${q.id}: answerCell ${q.answerCell} is out of range [0,80]")
                    continue
                }
                val cellValue = q.puzzle[q.answerCell]
                if (cellValue != '.' && cellValue != '0') {
                    val row = q.answerCell / 9 + 1
                    val col = q.answerCell % 9 + 1
                    violations.add(
                        "${q.id}: answerCell ${q.answerCell} (R${row}C${col}) " +
                        "is filled with '$cellValue' — student cannot click a filled cell"
                    )
                }
            }
        }

        assertTrue(violations.isEmpty(), "answerCell violations:\n${violations.joinToString("\n")}")
    }

    @Test
    fun `all highlightCells point to valid cell indices`() {
        val quizzes = loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                for (cell in q.highlightCells) {
                    if (cell < 0 || cell >= 81) {
                        violations.add("${q.id}: highlightCell $cell is out of range [0,80]")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "highlightCell violations:\n${violations.joinToString("\n")}")
    }

    @Test
    fun `answerCell is included in highlightCells for each question`() {
        val quizzes = loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.answerCell !in q.highlightCells) {
                    violations.add(
                        "${q.id}: answerCell ${q.answerCell} not in highlightCells ${q.highlightCells}"
                    )
                }
            }
        }

        assertTrue(violations.isEmpty(), "answerCell not in highlightCells:\n${violations.joinToString("\n")}")
    }
}
