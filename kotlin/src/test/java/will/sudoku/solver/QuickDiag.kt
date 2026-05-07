package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

class QuickDiag {

    @Test
    fun `quick diagnostic for all 9 failing tutorials`() {
        val elim = SimpleCandidateEliminator()
        val lines = mutableListOf<String>()

        data class TC(val id: String, val tech: String, val puzzle: String, val eliminator: CandidateEliminator)

        val cases = listOf(
            TC("swordfish", "Swordfish", ".5916......7....9...6....7..647..9.....8..71.......834.25...1....8.7......36.....",
                SwordfishCandidateEliminator()),
            TC("xy-wing", "XY-Wing", "...31.........713....5..4.........48765...91.......563...67...4...25........4..71",
                XYWingCandidateEliminator()),
            TC("xyz-wing", "XYZ-Wing", ".2...59.661829........7..1.........13.......8..9....2....5.....8.6....349..8..25.",
                XYZWingCandidateEliminator()),
            TC("unique-rectangle", "Unique Rectangle", "......7.2...4..3...39.....8...95...682....9...65..82........6.95...67..3....9..8.",
                UniqueRectanglesCandidateEliminator()),
            TC("simple-coloring", "Simple Coloring", ".....5..........5.145.67.....8....47..1....6...9...81..56284...2.4.3....8.3......",
                SimpleColoringCandidateEliminator()),
            TC("franken-fish", "Franken Fish", "92...1.8.16....4.95...9......2...894.59..4....1.....7.........34....9.2....3...4.",
                FrankenFishCandidateEliminator()),
            TC("mutant-fish", "Mutant Fish", "........9..97...6...8.5...3.5.......8.....7..3.7.12.8.9.15....7..3..1..6.6..9...4",
                MutantFishCandidateEliminator()),
            TC("death-blossom", "Death Blossom", "8..4.9...2...7.....5.2..3...428.1.3..3.....9..8.9.321...6..7.4.....2...1...6.4..3",
                DeathBlossomCandidateEliminator()),
            TC("forcing-chains", "Forcing Chains", "3.1..6...7.......4..9.5.2.79...3..4.......9......49..1..4.1.392..3....8.....93...",
                ForcingChainsCandidateEliminator()),
        )

        for (tc in cases) {
            val board = BoardReader.readBoard(tc.puzzle)
            elim.eliminate(board)
            HintGenerator.applyHiddenSinglesUntilStable(board)
            val unsolved = Coord.all.count { !board.isConfirmed(it) }
            lines.add("${tc.id}: unsolved after basic+HS=$unsolved")

            // Try direct eliminator
            val testBoard = board.copy()
            val madeProgress = tc.eliminator.eliminate(testBoard)
            lines.add("  Direct eliminator: ${if (madeProgress) "PROGRESS ✅" else "no progress ❌"}")
            if (madeProgress) {
                var cnt = 0
                for (c in Coord.all) {
                    if (!board.isConfirmed(c) && !testBoard.isConfirmed(c)) {
                        val b = board.candidateValues(c).size
                        val a = testBoard.candidateValues(c).size
                        if (b > a && cnt < 3) {
                            lines.add("    Cell (${c.row+1},${c.col+1}): ${b}→${a} candidates")
                            cnt++
                        }
                    }
                }
            }

            // Try HintGenerator detection
            val targetEnum = HintGenerator.Technique.entries.find { it.displayName == tc.tech }
            val hint = HintGenerator.generate(board.copy(), exhaustHiddenSingles = false, targetTechnique = targetEnum)
            val hintTech = hint?.technique?.displayName ?: "null"
            lines.add("  HintGenerator: $hintTech (match=${hintTech == tc.tech})")

            // Now try exhaustive solving first, then check
            val board2 = BoardReader.readBoard(tc.puzzle)
            elim.eliminate(board2)
            HintGenerator.applyHiddenSinglesUntilStable(board2)
            HintGenerator.exhaustAllTechniques(board2)
            val unsolved2 = Coord.all.count { !board2.isConfirmed(it) }
            val testBoard2 = board2.copy()
            val mp2 = tc.eliminator.eliminate(testBoard2)
            lines.add("  After ALL techniques: unsolved=$unsolved2, eliminator: ${if (mp2) "PROGRESS ✅" else "no ❌"}")
        }

        File("/tmp/quick-diag.txt").writeText(lines.joinToString("\n"))
    }
}
