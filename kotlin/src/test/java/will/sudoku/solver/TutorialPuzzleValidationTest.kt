package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

/**
 * Validates that tutorial puzzles demonstrate their taught technique.
 */
class TutorialPuzzleValidationTest {

    data class TutorialPuzzle(
        val id: String,
        val technique: String,
        val puzzle: String
    )

    private val tutorials = listOf(
        TutorialPuzzle("naked-single", "Naked Single", "5.46.8.12672..5.4819.342567859...4..4268...91713...8.6961...2.4287...6.5345...1.9"),
        TutorialPuzzle("hidden-single", "Hidden Single", ".........9.46.7....768.41..3.97.1.8...8...3...5.3.87.2..75.261....4.32.8........."),
        TutorialPuzzle("naked-pair", "Naked Pair", "4.....938.32.941...953..24..7.6.9..4.2.....7.6..7.3.9..57..83....39..4..24......9"),
        TutorialPuzzle("hidden-pair", "Hidden Pair", ".28.6.5973.5..8.627.642518.87123..452..614..96...872....78...16.8....35..6.351..8"),
        TutorialPuzzle("pointing-pair", "Pointing Pair", "..41.....6...4..3...17.6..4....7...8.....2.......1.96.1.2...3.6476.....9..58....."),
        TutorialPuzzle("box-line-reduction", "Box/Line Reduction", ".16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51."),
    )

    @Test
    fun `validate tutorial puzzles match their taught technique`() {
        val provider = TeachingHintProvider()
        val eliminator = SimpleCandidateEliminator()
        val results = mutableListOf<String>()

        for (tutorial in tutorials) {
            val board = BoardReader.readBoard(tutorial.puzzle)
            eliminator.eliminate(board)

            val hint = provider.getHint(board)
            val technique = hint.technique
            val match = technique == tutorial.technique

            results.add("${if (match) "OK" else "FAIL"} ${tutorial.id.padEnd(20)} expected=${tutorial.technique.padEnd(18)} got=$technique")
        }

        File("/tmp/tutorial-validation-results.txt").writeText(results.joinToString("\n"))
        println(results.joinToString("\n"))
    }
}
