package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

/**
 * Utility to find puzzles that demonstrate specific techniques.
 *
 * Takes a collection of hard puzzles and tests each one against
 * TeachingHintProvider to find which technique it demonstrates.
 *
 * Output is a mapping of technique -> puzzle string for use in tutorials.
 */
class TutorialPuzzleFinder {

    // Collection of hard puzzles from various sources
    private val candidatePuzzles = listOf(
        // X-Wing puzzles from SudokuWiki
        "100000569402000008050009040000640801000010000208035000040500010900000402621000005",
        "000000004760010050090002081070050010000709000080030060240100070010090045900000000",
        "000001008700030009020000061080009003001040900900300020240000080600090005100600000",
        // XYZ-Wing puzzles from SudokuWiki
        "090001700500200008000030200070004960200060005069700030008090000700003009003800040",
        "600000000500900007820001030340209080000080000080307025050400092900005004000000003",
        "000900300005821000190600000016000290000070000043000670000008069000213800004009000",
        // Naked Single examples from SudokuWiki
        "400000938032094100095300240370609004529001673604703090957008300003900400240030709",
        "400000038002004100005300240070609004020000070600703090057008300003900400240000009",
        "080090030030000069902063158020804590851907046394605870563040987200000015010050020",
        // Hidden Singles from SudokuWiki
        "000000000904607000076804100309701080008000300050308702007502610000403208000000000",
        "720408030080000047401076802810739000000851000000264080209680413340000008168943275",
        "720400030000000047001076802010039000000801000000260080209680400340000000060003075",
        // World's Hardest Sudoku (Arto Inkala)
        "800000000003600000070090200050007000000045700000100030001000068008500010090000400",
        // Another hard puzzle (AI Escargot)
        "100000000020090040003001600000400700050080020008006000001500800060030070000000005",
        // Golden Nugget
        "000000000000003085001020000000507000004000100090000000500000073002010000000000000",
        // Unsolvable #28
        "000000012000000035000000640000000731000000092000000047300000000052000000016000000",
        // Easter Monster
        "100000002090400050006000700050903000000040000000850090900000800040002030007010006",
        // Other hard test puzzles
        "1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5",
        ".98....7.3.........67..8.95.713.......394.7.....18.........9......5.341..3.6.....",
        "654..2...........11.........1.......4..7.....7.38.1.24.9..2..4..4.59....8.6..4.9.",
        "92...1.8.16....4.95...9......2...894.59..4....1.....7.........34....9.2....3...4.",
        "........9..97...6...8.5...3.5.......8.....7..3.7.12.8.9.15....7..3..1..6.6..9...4",
        "8..4.9...2...7.....5.2..3...428.1.3..3.....9..8.9.321...6..7.4.....2...1...6.4..3",
        "3.1..6...7.......4..9.5.2.79...3..4.......9......49..1..4.1.392..3....8.....93...",
        // Some more diverse puzzles
        "......7.2...4..3...39.....8...95...682....9...65..82........6.95...67..3....9..8.",
        ".2...59.661829........7..1.........13.......8..9....2....5.....8.6....349..8..25.",
        ".7...8.29..2.....4854.2......83742.............32617......9.6122.....4..13.6...7.",
        ".........231.9.....65..31....8924...1...5...6...1367....93..57.....1.843.........",
        ".5916......7....9...6....7..647..9.....8..71.......834.25...1....8.7......36.....",
        "...31.........713....5..4.........48765...91.......563...67...4...25........4..71",
    )

    @Test
    fun `find technique matches for tutorial puzzles`() {
        val provider = TeachingHintProvider()
        val eliminator = SimpleCandidateEliminator()
        val results = mutableListOf<String>()

        for (puzzle in candidatePuzzles) {
            try {
                // Validate puzzle is 81 chars
                if (puzzle.length != 81) {
                    results.add("SKIP invalid-length=${puzzle.length} $puzzle")
                    continue
                }
                val board = BoardReader.readBoard(puzzle)
                // Don't apply extra elimination — getHint does it internally
                val hint = provider.getHint(board)
                results.add("TECHNIQUE=${hint.technique.padEnd(20)} puzzle=$puzzle")
            } catch (e: Exception) {
                results.add("ERROR ${e.message?.take(50)} puzzle=$puzzle")
            }
        }

        // Group by technique
        val grouped = results.groupBy { line ->
            if (line.startsWith("TECHNIQUE=")) {
                line.substringAfter("TECHNIQUE=").substringBefore(" ").trim()
            } else {
                "OTHER"
            }
        }

        println("=== Puzzle Finder Results ===")
        for ((technique, lines) in grouped) {
            println("\n--- $technique (${lines.size} puzzles) ---")
            lines.forEach { println("  $it") }
        }

        File("/tmp/tutorial-puzzle-finder-results.txt").writeText(results.joinToString("\n"))
    }

    /**
     * Step-by-step solver: takes a puzzle and records the technique
     * needed at each step along with the board state.
     * This generates ideal tutorial puzzles.
     */
    @Test
    fun `step by step technique extraction`() {
        val puzzles = listOf(
            // Hard puzzles that should exercise various techniques
            "100000569402000008050009040000640801000010000208035000040500010900000402621000005",
            "600000000500900007820001030340209080000080000080307025050400092900005004000000003",
            "1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5",
            // Easter Monster
            "100000002090400050006000700050903000000040000000850090900000800040002030007010006",
        )

        val provider = TeachingHintProvider()
        val eliminator = SimpleCandidateEliminator()
        val techniqueStates = mutableMapOf<String, String>() // technique -> board state

        for (puzzle in puzzles) {
            println("\n=== Step-by-step for puzzle: $puzzle ===")
            val board = BoardReader.readBoard(puzzle)
            eliminator.eliminate(board)

            var step = 0
            val maxSteps = 15

            while (step < maxSteps && !board.isSolved()) {
                val hint = provider.getHint(board.copy())
                step++

                val boardStr = board.confirmedCellsString()

                println("  Step $step: technique=${hint.technique.padEnd(18)} cell=${hint.cell?.let { "(${it.row+1},${it.col+1})" } ?: "none"}")

                // Record board state for this technique (only if not already recorded)
                if (hint.technique !in techniqueStates && hint.technique != "Scanning" && hint.technique != "Naked Single" && hint.technique != "Hidden Single") {
                    techniqueStates[hint.technique] = boardStr
                    println("    -> RECORDED board state for ${hint.technique}")
                }

                // Apply the hint (fill naked singles and hidden singles)
                when {
                    hint.type == HintType.COMPLETE || hint.type == HintType.ADVANCED -> break
                    hint.type == HintType.NAKED_SINGLE && hint.cell != null -> {
                        val value = board.candidateValues(hint.cell).firstOrNull()
                        if (value != null) board.markValue(hint.cell, value)
                    }
                    hint.type == HintType.HIDDEN_SINGLE && hint.cell != null -> {
                        val cell = hint.cell
                        for (group in CoordGroup.of(cell)) {
                            val presentValues = group.coords.map { c: Coord -> board.value(c) }.toSet()
                            for (value in 1..9) {
                                if (value in presentValues) continue
                                val possibleCells = group.coords.filter { c: Coord ->
                                    !board.isConfirmed(c) && value in board.candidateValues(c)
                                }
                                if (possibleCells.size == 1 && possibleCells[0] == cell) {
                                    board.markValue(cell, value)
                                    break
                                }
                            }
                        }
                    }
                    else -> break // Can't apply advanced techniques automatically
                }
                eliminator.eliminate(board)
            }
            println("  Final: solved=${board.isSolved()} steps=$step")
        }

        println("\n=== RECORDED TECHNIQUE STATES ===")
        techniqueStates.forEach { (technique, state) ->
            println("  $technique: $state")
        }
    }

    /**
     * Helper to get confirmed cells as a 81-char puzzle string.
     */
    private fun Board.confirmedCellsString(): String = buildString {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (isConfirmed(coord)) append(value(coord)) else append('.')
            }
        }
    }
}
