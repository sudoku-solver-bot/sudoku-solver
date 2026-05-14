package will.sudoku.web

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Validates that every quiz question's answer data is consistent
 * with the puzzle and the solver.
 */
class TutorialQuizValidationTest {

    private val helper = TutorialTestHelper

    @Test
    fun `all quiz puzzles are valid 81-character strings`() {
        val quizzes = helper.loadQuizzes()
        val problems = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.puzzle.length != 81) {
                    problems.add("${q.id}: puzzle is ${q.puzzle.length} chars (expected 81)")
                }
                val invalidChars = q.puzzle.filter { c -> c !in '1'..'9' && c != '.' && c != '0' }
                if (invalidChars.isNotEmpty()) {
                    problems.add("${q.id}: puzzle contains invalid chars: $invalidChars")
                }
            }
        }

        assertTrue(problems.isEmpty(),
            "Quiz puzzle problems:\n${problems.joinToString("\n  ")}")
    }

    @Test
    fun `answerCell points to an empty cell in the puzzle`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.answerCell !in 0..80) {
                    violations.add("${q.id}: answerCell ${q.answerCell} is out of range [0,80]")
                    continue
                }
                val ch = q.puzzle[q.answerCell]
                if (ch != '.' && ch != '0') {
                    violations.add(
                        "${q.id}: answerCell ${q.answerCell} (${helper.cellLabel(q.answerCell)}) " +
                        "is filled with '$ch' — student cannot click a filled cell"
                    )
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "answerCell violations:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `answerCell is included in highlightCells`() {
        val quizzes = helper.loadQuizzes()
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

        assertTrue(violations.isEmpty(),
            "answerCell not in highlightCells:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `answerValue is a valid candidate at answerCell after elimination`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                val answerVal = q.answerValue.toIntOrNull()
                if (answerVal == null) {
                    violations.add("${q.id}: answerValue '${q.answerValue}' is not a number")
                    continue
                }

                val board = helper.prepareBoardBasic(q.puzzle)
                val candidates = helper.getCandidates(board, q.answerCell)

                if (answerVal !in candidates) {
                    violations.add(
                        "${q.id}: answerValue=$answerVal not in candidates $candidates " +
                        "at ${helper.cellLabel(q.answerCell)} (idx=${q.answerCell})"
                    )
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "answerValue not in candidates:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `all quiz questions have non-empty hints`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.hint.isBlank()) {
                    violations.add("${q.id}: hint is empty")
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "Questions with empty hints:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `all quiz questions have non-empty explanations`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.explanation.isBlank()) {
                    violations.add("${q.id}: explanation is empty")
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "Questions with empty explanations:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `all quiz questions have at least 2 options`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.options.size < 2) {
                    violations.add("${q.id}: only ${q.options.size} option(s) — need at least 2")
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "Questions with too few options:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `correctAnswer is a valid index into options`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                if (q.correctAnswer !in q.options.indices) {
                    violations.add(
                        "${q.id}: correctAnswer=${q.correctAnswer} out of range " +
                        "[0,${q.options.lastIndex}] (options count: ${q.options.size})"
                    )
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "Invalid correctAnswer indices:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `all highlightCells are valid indices 0-80`() {
        val quizzes = helper.loadQuizzes()
        val violations = mutableListOf<String>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                for (cell in q.highlightCells) {
                    if (cell !in 0..80) {
                        violations.add("${q.id}: highlightCell $cell is out of range [0,80]")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
            "Invalid highlightCell indices:\n${violations.joinToString("\n  ")}")
    }

    @Test
    fun `no quiz puzzles are reused across different belts`() {
        val quizzes = helper.loadQuizzes()
        val beltPuzzles = mutableMapOf<String, MutableList<String>>()

        for (quiz in quizzes) {
            for (q in quiz.questions) {
                beltPuzzles.getOrPut(q.puzzle) { mutableListOf() }.add(quiz.belt)
            }
        }

        val violations = mutableListOf<String>()
        for ((puzzle, belts) in beltPuzzles) {
            val distinctBelts = belts.distinct()
            if (distinctBelts.size > 1) {
                violations.add(
                    "Puzzle ${puzzle.take(20)}... shared across belts: ${distinctBelts.joinToString()}"
                )
            }
        }

        assertTrue(violations.isEmpty(),
            "Cross-belt puzzle reuse:\n${violations.joinToString("\n  ")}")
    }
}
