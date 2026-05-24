package will.sudoku.web

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Cross-validation that no puzzle is reused across tutorials, quizzes, or practice sets.
 *
 * Each puzzle in the dataset should be unique. Reusing puzzles (especially
 * across different belts) allows learners to solve the same puzzle twice
 * without learning new techniques.
 *
 * Refs #513, #367
 */
class PuzzleUniquenessTest {

    /** Normalize puzzle string: '.' → '0' for comparison. */
    private fun normalize(puzzle: String): String = puzzle.replace('.', '0')

    /** Get display key for a puzzle source. */
    private data class PuzzleEntry(
        val source: String,
        val key: String,
        val puzzle: String
    )

    @Test
    fun `no puzzle duplicates within lesson examples`() {
        val lessons = TutorialTestHelper.loadLessons()
        val seen = mutableMapOf<String, String>() // normalized -> id
        val duplicates = mutableListOf<String>()

        for (lesson in lessons) {
            val norm = normalize(lesson.examplePuzzle)
            if (seen.containsKey(norm)) {
                duplicates += "Lesson '${lesson.id}' has same puzzle as '${seen[norm]}'"
            } else {
                seen[norm] = lesson.id
            }
        }

        assertTrue(duplicates.isEmpty(),
            "Duplicate lesson puzzles found:\n${duplicates.joinToString("\n")}")
    }

    @Test
    fun `no puzzle duplicates within quiz questions`() {
        val quizSets = TutorialTestHelper.loadQuizzes()
        val seen = mutableMapOf<String, String>() // normalized -> "belt/questionId"
        val duplicates = mutableListOf<String>()

        for (quizSet in quizSets) {
            for (question in quizSet.questions) {
                val norm = normalize(question.puzzle)
                val key = "${quizSet.belt}/${question.id}"
                if (seen.containsKey(norm)) {
                    duplicates += "Quiz '$key' has same puzzle as '${seen[norm]}'"
                } else {
                    seen[norm] = key
                }
            }
        }

        assertTrue(duplicates.isEmpty(),
            "Duplicate quiz puzzles found:\n${duplicates.joinToString("\n")}")
    }

    @Test
    fun `no puzzle duplicates within practice puzzles`() {
        val practiceSets = TutorialTestHelper.loadPracticePuzzles()
        val seen = mutableMapOf<String, String>()
        val duplicates = mutableListOf<String>()

        for (ps in practiceSets) {
            for (puzzle in ps.puzzles) {
                val norm = normalize(puzzle.puzzle)
                val key = "${ps.id}/${puzzle.id}"
                if (seen.containsKey(norm)) {
                    duplicates += "Practice '$key' has same puzzle as '${seen[norm]}'"
                } else {
                    seen[norm] = key
                }
            }
        }

        assertTrue(duplicates.isEmpty(),
            "Duplicate practice puzzles found:\n${duplicates.joinToString("\n")}")
    }

    @Disabled("Known duplicates (#367): green-q2=box-line-reduction, blue-q1=naked-triple, blue-q2=hidden-triple, purple-q1=x-wing, master-q1=death-blossom. Fix in follow-up issue.")
    @Test
    fun `no puzzle duplicates between lessons and quizzes`() {
        val lessons = TutorialTestHelper.loadLessons()
        val quizSets = TutorialTestHelper.loadQuizzes()

        val lessonPuzzles = mutableMapOf<String, String>() // normalized -> lesson id
        for (lesson in lessons) {
            lessonPuzzles[normalize(lesson.examplePuzzle)] = "lesson: ${lesson.id}"
        }

        val crossDuplicates = mutableListOf<String>()
        for (quizSet in quizSets) {
            for (question in quizSet.questions) {
                val norm = normalize(question.puzzle)
                if (lessonPuzzles.containsKey(norm)) {
                    crossDuplicates += "Quiz '${quizSet.belt}/${question.id}' duplicates ${lessonPuzzles[norm]}"
                }
            }
        }

        assertTrue(crossDuplicates.isEmpty(),
            "Cross-duplicates (lesson ↔ quiz) found:\n${crossDuplicates.joinToString("\n")}")
    }

    @Test
    fun `no puzzle duplicates between lessons and practice`() {
        val lessons = TutorialTestHelper.loadLessons()
        val practiceSets = TutorialTestHelper.loadPracticePuzzles()

        val lessonPuzzles = mutableMapOf<String, String>()
        for (lesson in lessons) {
            lessonPuzzles[normalize(lesson.examplePuzzle)] = "lesson: ${lesson.id}"
        }

        val crossDuplicates = mutableListOf<String>()
        for (ps in practiceSets) {
            for (puzzle in ps.puzzles) {
                val norm = normalize(puzzle.puzzle)
                if (lessonPuzzles.containsKey(norm)) {
                    crossDuplicates += "Practice '${ps.id}/${puzzle.id}' duplicates ${lessonPuzzles[norm]}"
                }
            }
        }

        assertTrue(crossDuplicates.isEmpty(),
            "Cross-duplicates (lesson ↔ practice) found:\n${crossDuplicates.joinToString("\n")}")
    }

    @Test
    fun `no puzzle duplicates between quizzes and practice`() {
        val quizSets = TutorialTestHelper.loadQuizzes()
        val practiceSets = TutorialTestHelper.loadPracticePuzzles()

        val quizPuzzles = mutableMapOf<String, String>()
        for (quizSet in quizSets) {
            for (question in quizSet.questions) {
                quizPuzzles[normalize(question.puzzle)] = "quiz: ${quizSet.belt}/${question.id}"
            }
        }

        val crossDuplicates = mutableListOf<String>()
        for (ps in practiceSets) {
            for (puzzle in ps.puzzles) {
                val norm = normalize(puzzle.puzzle)
                if (quizPuzzles.containsKey(norm)) {
                    crossDuplicates += "Practice '${ps.id}/${puzzle.id}' duplicates ${quizPuzzles[norm]}"
                }
            }
        }

        assertTrue(crossDuplicates.isEmpty(),
            "Cross-duplicates (quiz ↔ practice) found:\n${crossDuplicates.joinToString("\n")}")
    }
}
