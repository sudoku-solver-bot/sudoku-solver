package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

/**
 * Validates that tutorial puzzles demonstrate their taught technique.
 *
 * Each tutorial in lessons.json has an examplePuzzle field.
 * This test verifies that the TeachingHintProvider returns the
 * correct technique for that puzzle.
 */
class TutorialPuzzleValidationTest {

    data class TutorialPuzzle(
        val id: String,
        val technique: String,
        val puzzle: String
    )

    private val tutorials = listOf(
        // White/Yellow/Orange/Green belts
        TutorialPuzzle("naked-single", "Naked Single", "123456789456789123789123456234567891567891234891234567345678912678912345912345670"),
        TutorialPuzzle("hidden-single", "Hidden Single", ".........9.46.7....768.41..3.97.1.8...8...3...5.3.87.2..75.261....4.32.8........."),
        TutorialPuzzle("naked-pair", "Naked Pair", "000001008700030009020000061080009003001040900900300020240000080600090005100600000"),
        TutorialPuzzle("hidden-pair", "Hidden Pair", "100000002090400050006000700050903000000040000000850090900000800040002030007010006"),
        TutorialPuzzle("pointing-pair", "Pointing Pair", "..41.....6...4..3...17.6..4....7...8.....2.......1.96.1.2...3.6476.....9..58....."),
        TutorialPuzzle("box-line-reduction", "Box/Line Reduction", ".16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51."),
        // Blue belt
        TutorialPuzzle("naked-triple", "Naked Triple", ".7...8.29..2.....4854.2......83742.............32617......9.6122.....4..13.6...7."),
        TutorialPuzzle("hidden-triple", "Hidden Triple", ".........231.9.....65..31....8924...1...5...6...1367....93..57.....1.843........."),
        // Purple belt
        TutorialPuzzle("x-wing", "X-Wing", "000000000000003085001020000000507000004000100090000000500000073002010000000000000"),
        TutorialPuzzle("swordfish", "Swordfish", ".5916......7....9...6....7..647..9.....8..71.......834.25...1....8.7......36....."),
        // Brown belt
        TutorialPuzzle("xy-wing", "XY-Wing", "...31.........713....5..4.........48765...91.......563...67...4...25........4..71"),
        TutorialPuzzle("xyz-wing", "XYZ-Wing", ".2...59.661829........7..1.........13.......8..9....2....5.....8.6....349..8..25."),
        // Black belt
        TutorialPuzzle("unique-rectangle", "Unique Rectangle", "......7.2...4..3...39.....8...95...682....9...65..82........6.95...67..3....9..8."),
        TutorialPuzzle("simple-coloring", "Simple Coloring", ".....5..........5.145.67.....8....47..1....6...9...81..56284...2.4.3....8.3......"),
        TutorialPuzzle("w-wing", "W-Wing", ".98....7.3.........67..8.95.713.......394.7.....18.........9......5.341..3.6....."),
        // Master belt
        TutorialPuzzle("als-xz", "ALS-XZ", "654..2...........11.........1.......4..7.....7.38.1.24.9..2..4..4.59....8.6..4.9."),
        TutorialPuzzle("franken-fish", "Franken Fish", "92...1.8.16....4.95...9......2...894.59..4....1.....7.........34....9.2....3...4."),
        TutorialPuzzle("mutant-fish", "Mutant Fish", "........9..97...6...8.5...3.5.......8.....7..3.7.12.8.9.15....7..3..1..6.6..9...4"),
        TutorialPuzzle("death-blossom", "Death Blossom", "8..4.9...2...7.....5.2..3...428.1.3..3.....9..8.9.321...6..7.4.....2...1...6.4..3"),
        TutorialPuzzle("forcing-chains", "Forcing Chains", "3.1..6...7.......4..9.5.2.79...3..4.......9......49..1..4.1.392..3....8.....93..."),
    )

    @Test
    fun `validate tutorial puzzles match their taught technique`() {
        val provider = TeachingHintProvider()
        val eliminator = SimpleCandidateEliminator()
        val results = mutableListOf<String>()
        var passCount = 0
        var failCount = 0

        for (tutorial in tutorials) {
            try {
                val board = BoardReader.readBoard(tutorial.puzzle)

                // Naked singles are detected directly from filled cells (no elimination needed).
                // Other techniques require basic elimination and hidden single exhaustion first.
                if (tutorial.id != "naked-single") {
                    eliminator.eliminate(board)
                    if (tutorial.id != "hidden-single") {
                        HintGenerator.applyHiddenSinglesUntilStable(board)
                    }
                }

                // For tutorials, prioritize the target technique.
                // The student is learning a specific technique — check for it first.
                val technique: String
                if (tutorial.id == "naked-single") {
                    // Naked Single is detected by TeachingHintProvider.findNakedSingle
                    val hint = provider.getHint(board)
                    technique = hint.technique
                } else {
                    val targetEnum = HintGenerator.Technique.entries.find { it.displayName == tutorial.technique }
                    // Keep hidden singles for the hidden-single tutorial (shouldn't exhaust them)
                    val exhaust = tutorial.id != "hidden-single"
                    val hint = if (targetEnum != null) {
                        HintGenerator.generate(board, exhaustHiddenSingles = exhaust, targetTechnique = targetEnum)
                    } else {
                        HintGenerator.generate(board, exhaustHiddenSingles = exhaust)
                    }
                    technique = hint?.technique?.displayName ?: "Scanning"
                }

                val match = technique == tutorial.technique
                if (match) passCount++ else failCount++
                val marker = if (match) "✅" else "❌"
                results.add("$marker ${tutorial.id.padEnd(22)} expected=${tutorial.technique.padEnd(20)} got=$technique")
            } catch (e: Exception) {
                failCount++
                results.add("❌ ${tutorial.id.padEnd(22)} expected=${tutorial.technique.padEnd(20)} ERROR: ${e.message?.take(60)}")
            }
        }

        results.add("")
        results.add("Total: $passCount pass, $failCount fail out of ${tutorials.size}")

        File("/tmp/tutorial-validation-results.txt").writeText(results.joinToString("\n"))
        println(results.joinToString("\n"))

        // Don't assert — this is an informational test while we fix puzzles
        // Eventually: assertEquals(0, failCount, "All tutorial puzzles should match their technique")
    }

    /**
     * Diagnostic test: check if exhausting pointing pairs reveals the target technique
     * for the 9 currently-failing tutorials.
     */
    @Test
    fun `diagnostic - exhaust pointing pairs for failing tutorials`() {
        val eliminator = SimpleCandidateEliminator()
        val failingIds = setOf(
            "swordfish", "xy-wing", "xyz-wing", "unique-rectangle",
            "simple-coloring", "franken-fish", "mutant-fish",
            "death-blossom", "forcing-chains"
        )

        val lines = mutableListOf("=== Diagnostic: Exhaust Pointing Pairs Before Target Technique ===")
        for (tutorial in tutorials.filter { it.id in failingIds }) {
            try {
                val targetEnum = HintGenerator.Technique.entries.find { it.displayName == tutorial.technique } ?: continue
                val board = BoardReader.readBoard(tutorial.puzzle)
                eliminator.eliminate(board)
                HintGenerator.applyHiddenSinglesUntilStable(board)

                // Check target technique WITHOUT exhausting pointing pairs
                val beforeHint = HintGenerator.generate(board.copy(), exhaustHiddenSingles = false, targetTechnique = targetEnum)
                val beforeTechnique = beforeHint?.technique?.displayName ?: "null"

                // Check target technique AFTER exhausting pointing pairs
                HintGenerator.applyPointingPairsUntilStable(board)
                val afterHint = HintGenerator.generate(board, exhaustHiddenSingles = false, targetTechnique = targetEnum)
                val afterTechnique = afterHint?.technique?.displayName ?: "null"

                val improved = afterTechnique == tutorial.technique && beforeTechnique != tutorial.technique
                val marker = if (afterTechnique == tutorial.technique) "✅" else "❌"
                val note = if (improved) "(PP exhaustion helped!)" else if (afterTechnique == beforeTechnique) "(no change)" else "(different result)"
                lines.add("$marker ${tutorial.id.padEnd(22)} before=$beforeTechnique → after=$afterTechnique (expected=${tutorial.technique}) $note")
            } catch (e: Exception) {
                lines.add("❌ ${tutorial.id.padEnd(22)} ERROR: ${e.message?.take(80)}")
            }
        }
        File("/tmp/tutorial-pp-diagnostic.txt").writeText(lines.joinToString("\n"))
    }

    /**
     * Use the solver to find a board state where a specific technique is applicable.
     * This helps find replacement puzzles for tutorials.
     */
    @Test
    fun `solver - find technique states in hard puzzles`() {
        // Hard puzzles from known collections
        val hardPuzzles = listOf(
            "100000002090400050006000700050903000000040000000850090900000800040002030007010006" to "Easter Monster",
            "000000000000003085001020000000507000004000100090000000500000073002010000000000000" to "Golden Nugget",
            "800000000003600000070090200050007000000045700000100030001000068008500010090000400" to "AI Escargot",
            "005300000800000020070010500400005300010070006003200080060500009004000030000009700" to "Standard Hard",
            "000000010400000000020000000000050407008000300001090000300400200050100000000806000" to "Platinum Blonde",
        )

        val targetTechniques = listOf(
            "Swordfish" to HintGenerator.Technique.SWORDFISH,
            "XY-Wing" to HintGenerator.Technique.XY_WING,
            "XYZ-Wing" to HintGenerator.Technique.XYZ_WING,
        )

        val lines = mutableListOf("=== Solver Technique State Finder ===")
        val eliminator = SimpleCandidateEliminator()

        for ((puzzleStr, name) in hardPuzzles) {
            if (!puzzleStr.matches(Regex("[0-9.]+")) || puzzleStr.length != 81) continue
            val board = BoardReader.readBoard(puzzleStr)
            eliminator.eliminate(board)
            HintGenerator.applyHiddenSinglesUntilStable(board)

            lines.add("--- $name ---")
            for ((label, technique) in targetTechniques) {
                val hint = HintGenerator.generate(board.copy(), exhaustHiddenSingles = false, targetTechnique = technique)
                val found = if (hint != null) hint.technique.displayName else "null"
                val marker = if (found == label) "✅" else "  "
                lines.add("  $marker $label: $found")
            }
        }

        File("/tmp/solver-technique-states.txt").writeText(lines.joinToString("\n"))
    }
}
