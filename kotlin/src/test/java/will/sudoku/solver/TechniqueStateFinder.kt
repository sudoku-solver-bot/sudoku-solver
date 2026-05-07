package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

class TechniqueStateFinder {

    data class Target(
        val id: String,
        val displayName: String,
        val eliminatorClass: Class<out CandidateEliminator>
    )

    private val targets = listOf(
        Target("swordfish", "Swordfish", SwordfishCandidateEliminator::class.java),
        Target("xy-wing", "XY-Wing", XYWingCandidateEliminator::class.java),
        Target("xyz-wing", "XYZ-Wing", XYZWingCandidateEliminator::class.java),
        Target("unique-rectangle", "Unique Rectangle", UniqueRectanglesCandidateEliminator::class.java),
        Target("simple-coloring", "Simple Coloring", SimpleColoringCandidateEliminator::class.java),
        Target("franken-fish", "Franken Fish", FrankenFishCandidateEliminator::class.java),
        Target("mutant-fish", "Mutant Fish", MutantFishCandidateEliminator::class.java),
        Target("death-blossom", "Death Blossom", DeathBlossomCandidateEliminator::class.java),
        Target("forcing-chains", "Forcing Chains", ForcingChainsCandidateEliminator::class.java),
    )

    private val candidatePuzzles = listOf(
        "100000002090400050006000700050903000000040000000850090900000800040002030007010006", // Easter Monster
        "000000000000003085001020000000507000004000100090000000500000073002010000000000000", // Golden Nugget
        "800000000003600000070090200050007000000045700000100030001000068008500010090000400", // AI Escargot
        "000000010400000000020000000000050407008000300001090000300400200050100000000806000", // Platinum Blonde
        "000000001000002034056000000000040500004000000000601000820000000000007000090000000", // Tarx0134
        "000000000000001020300040000000602300050000070001000500000070004020000010600000000", // Tunguska
        // Additional puzzles
        "005300000800000020070010500400005300010070006003200080060500009004000030000009700", // standard hard
        "020500000000003010003060070800400009000000000500100200090000050400000008000008000",
    )

    @Test
    fun `find technique states in hard puzzles`() {
        val lines = mutableListOf("=== Technique State Finder ===")
        val foundTargets = mutableSetOf<String>()
        val savedStates = mutableMapOf<String, String>()

        for (puzzleStr in candidatePuzzles) {
            if (puzzleStr.length != 81) continue

            val board = BoardReader.readBoard(puzzleStr)
            val elim = SimpleCandidateEliminator()
            elim.eliminate(board)
            HintGenerator.applyHiddenSinglesUntilStable(board)

            val unsolved = Coord.all.count { !board.isConfirmed(it) }
            if (unsolved == 0) continue

            lines.add("")
            lines.add("=== Puzzle (unsolved after basic+HS: $unsolved) ===")

            val config = SolverConfig()
            var iteration = 0
            var anyProgress = true

            while (anyProgress && iteration < 200 && foundTargets.size < targets.size) {
                anyProgress = false
                iteration++

                for (elimInstance in config.eliminators) {
                    val target = targets.find { it.eliminatorClass.isInstance(elimInstance) }
                    if (target != null && target.id !in foundTargets) {
                        val beforeBoard = board.copy()
                        val beforeCands = board.countTotalCandidates()

                        val changed = elimInstance.eliminate(board)
                        val afterCands = board.countTotalCandidates()

                        if (changed && afterCands < beforeCands) {
                            // Fill naked singles in the before-state
                            fillNakedSingles(beforeBoard)
                            val boardStr = boardToString(beforeBoard)
                            val unsolvedAfter = Coord.all.count { !beforeBoard.isConfirmed(it) }

                            foundTargets.add(target.id)
                            savedStates[target.id] = boardStr

                            lines.add("  ✅ ${target.id}: unsolved=$unsolvedAfter, puzzle=$boardStr")
                            anyProgress = true
                        }
                    } else {
                        val beforeCands = board.countTotalCandidates()
                        elimInstance.eliminate(board)
                        if (board.countTotalCandidates() < beforeCands) {
                            anyProgress = true
                        }
                    }
                }

                // Fill naked singles
                var filled = false
                for (coord in Coord.all) {
                    if (!board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 1) {
                        val value = board.candidateValues(coord).first()
                        board.markValue(coord, value)
                        filled = true
                    }
                }
                if (filled) anyProgress = true
            }

            lines.add("  Found so far: ${foundTargets.size}/${targets.size}")
        }

        lines.add("")
        lines.add("=== SUMMARY: Found ${foundTargets.size}/${targets.size} techniques ===")
        lines.add("Found: ${foundTargets.sorted()}")
        val missing = targets.filter { it.id !in foundTargets }.map { it.id }
        lines.add("Missing: $missing")

        // Print the saved board states
        lines.add("")
        lines.add("=== SAVED BOARD STATES ===")
        for (target in targets) {
            val state = savedStates[target.id]
            lines.add("${target.id}: ${state ?: "NOT FOUND"}")
        }

        File("/tmp/technique-states.txt").writeText(lines.joinToString("\n"))
    }

    private fun boardToString(board: Board): String {
        return Coord.all.map { coord ->
            if (board.isConfirmed(coord)) board.value(coord).toString()
            else "0"
        }.joinToString("")
    }

    private fun fillNakedSingles(board: Board) {
        var filled = true
        val simple = SimpleCandidateEliminator()
        while (filled) {
            filled = false
            for (coord in Coord.all) {
                if (!board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 1) {
                    val value = board.candidateValues(coord).first()
                    board.markValue(coord, value)
                    filled = true
                }
            }
            if (filled) {
                simple.eliminate(board)
            }
        }
    }
}
