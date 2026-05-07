package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

class TutorialPuzzleDiagnosticTest {

    data class TestCase(
        val id: String,
        val technique: String,
        val puzzle: String,
        val eliminator: CandidateEliminator
    )

    private val eliminator = SimpleCandidateEliminator()

    private val testCases = listOf(
        TestCase("swordfish", "Swordfish",
            ".5916......7....9...6....7..647..9.....8..71.......834.25...1....8.7......36.....",
            SwordfishCandidateEliminator()),
        TestCase("xy-wing", "XY-Wing",
            "...31.........713....5..4.........48765...91.......563...67...4...25........4..71",
            XYWingCandidateEliminator()),
        TestCase("xyz-wing", "XYZ-Wing",
            ".2...59.661829........7..1.........13.......8..9....2....5.....8.6....349..8..25.",
            XYZWingCandidateEliminator()),
        TestCase("unique-rectangle", "Unique Rectangle",
            "......7.2...4..3...39.....8...95...682....9...65..82........6.95...67..3....9..8.",
            UniqueRectanglesCandidateEliminator()),
        TestCase("simple-coloring", "Simple Coloring",
            ".....5..........5.145.67.....8....47..1....6...9...81..56284...2.4.3....8.3......",
            SimpleColoringCandidateEliminator()),
        TestCase("franken-fish", "Franken Fish",
            "92...1.8.16....4.95...9......2...894.59..4....1.....7.........34....9.2....3...4.",
            FrankenFishCandidateEliminator()),
        TestCase("mutant-fish", "Mutant Fish",
            "........9..97...6...8.5...3.5.......8.....7..3.7.12.8.9.15....7..3..1..6.6..9...4",
            MutantFishCandidateEliminator()),
        TestCase("death-blossom", "Death Blossom",
            "8..4.9...2...7.....5.2..3...428.1.3..3.....9..8.9.321...6..7.4.....2...1...6.4..3",
            DeathBlossomCandidateEliminator()),
        TestCase("forcing-chains", "Forcing Chains",
            "3.1..6...7.......4..9.5.2.79...3..4.......9......49..1..4.1.392..3....8.....93...",
            ForcingChainsCandidateEliminator()),
    )

    @Test
    fun `diagnostic - test eliminators directly on tutorial boards`() {
        val lines = mutableListOf("=== Direct Eliminator Test on Tutorial Boards ===")

        for (tc in testCases) {
            try {
                val board = BoardReader.readBoard(tc.puzzle)
                eliminator.eliminate(board)
                HintGenerator.applyHiddenSinglesUntilStable(board)

                val unsolvedCount = Coord.all.count { !board.isConfirmed(it) }
                lines.add("")
                lines.add("--- ${tc.id} (${tc.technique}) [unsolved=$unsolvedCount] ---")

                val testBoard = board.copy()
                val madeProgress = tc.eliminator.eliminate(testBoard)
                lines.add("  Eliminator: ${if (madeProgress) "PROGRESS" else "NO PROGRESS"}")

                if (madeProgress) {
                    var shown = 0
                    for (coord in Coord.all) {
                        if (shown >= 3) break
                        if (board.isConfirmed(coord) || testBoard.isConfirmed(coord)) continue
                        val beforeVals = board.candidateValues(coord).sorted()
                        val afterVals = testBoard.candidateValues(coord).sorted()
                        if (beforeVals.size > afterVals.size) {
                            val removed = beforeVals - afterVals.toSet()
                            lines.add("    Cell (${coord.row + 1},${coord.col + 1}): $beforeVals → $afterVals (removed $removed)")
                            shown++
                        }
                    }
                }

                val targetEnum = HintGenerator.Technique.entries.find { it.displayName == tc.technique }
                val hint = if (targetEnum != null) {
                    HintGenerator.generate(board.copy(), exhaustHiddenSingles = false, targetTechnique = targetEnum)
                } else null
                val hintTech = hint?.technique?.displayName ?: "null"
                lines.add("  HintGenerator: $hintTech (match=${hintTech == tc.technique})")
            } catch (e: Exception) {
                lines.add("  ERROR: ${e.message?.take(120)}")
            }
        }

        File("/tmp/tutorial-diag-eliminators.txt").writeText(lines.joinToString("\n"))
    }

    @Test
    fun `diagnostic - full exhaust then check eliminators`() {
        val lines = mutableListOf("=== Full Exhaust THEN Check Eliminators ===")

        for (tc in testCases) {
            try {
                val board = BoardReader.readBoard(tc.puzzle)
                eliminator.eliminate(board)
                HintGenerator.applyHiddenSinglesUntilStable(board)
                val before = Coord.all.count { !board.isConfirmed(it) }

                HintGenerator.exhaustAllTechniques(board)
                val after = Coord.all.count { !board.isConfirmed(it) }
                lines.add("--- ${tc.id}: $before → $after filled=${before - after}")

                val testBoard = board.copy()
                val mp = tc.eliminator.eliminate(testBoard)
                lines.add("  Eliminator after exhaust: ${if (mp) "PROGRESS" else "no"}")

                if (mp) {
                    // Board state right before the technique works - get it BEFORE exhaust
                    val board2 = BoardReader.readBoard(tc.puzzle)
                    eliminator.eliminate(board2)
                    HintGenerator.applyHiddenSinglesUntilStable(board2)
                    // Now run exhaust until the target eliminator succeeds
                    val config = SolverConfig()
                    var found = false
                    var iterations = 0
                    while (!found && iterations < 100) {
                        iterations++
                        var anyProgress = false
                        for (e in config.eliminators) {
                            val beforeCands = board2.countTotalCandidates()
                            e.eliminate(board2)
                            if (board2.countTotalCandidates() < beforeCands) {
                                anyProgress = true
                            }
                            if (e.javaClass == tc.eliminator.javaClass && board2.countTotalCandidates() < beforeCands) {
                                found = true
                                lines.add("  Found at iteration $iterations")
                                // The board state we want is right before this eliminator ran
                                // But we already mutated board2, so let's log what we can
                                break
                            }
                        }
                        if (!anyProgress) break
                    }
                }
            } catch (e: Exception) {
                lines.add("--- ${tc.id}: ERROR: ${e.message?.take(100)}")
            }
        }

        File("/tmp/tutorial-diag-exhaust.txt").writeText(lines.joinToString("\n"))
    }
}
