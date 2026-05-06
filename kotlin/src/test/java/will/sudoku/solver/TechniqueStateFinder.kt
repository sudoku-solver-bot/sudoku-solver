package will.sudoku.solver

import org.junit.jupiter.api.Test

/**
 * Finds puzzle states where specific techniques are the next move.
 * Uses HintGenerator.generate() directly (not TeachingHintProvider)
 * to find states where advanced techniques are the first non-basic technique.
 */
class TechniqueStateFinder {

    private val hardPuzzles = listOf(
        // Very hard to extreme puzzles
        "800000000003600000070090200050007000000045700000100030001000068008500010090000400",
        "100000000020090040003001600000400700050080020008006000001500800060030070000000005",
        "000000000000003085001020000000507000004000100090000000500000073002010000000000000",
        "000000012000000035000000640000000731000000092000000047300000000052000000016000000",
        "100000002090400050006000700050903000000040000000850090900000800040002030007010006",
        "100000569402000008050009040000640801000010000208035000040500010900000402621000005",
        "600000000500900007820001030340209080000080000080307025050400092900005004000000003",
        "1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5",
        "000001008700030009020000061080009003001040900900300020240000080600090005100600000",
        "090001700500200008000030200070004960200060005069700030008090000700003009003800040",
        ".98....7.3.........67..8.95.713.......394.7.....18.........9......5.341..3.6.....",
        "654..2...........11.........1.......4..7.....7.38.1.24.9..2..4..4.59....8.6..4.9.",
        "92...1.8.16....4.95...9......2...894.59..4....1.....7.........34....9.2....3...4.",
        "........9..97...6...8.5...3.5.......8.....7..3.7.12.8.9.15....7..3..1..6.6..9...4",
        "8..4.9...2...7.....5.2..3...428.1.3..3.....9..8.9.321...6..7.4.....2...1...6.4..3",
        "3.1..6...7.......4..9.5.2.79...3..4.......9......49..1..4.1.392..3....8.....93...",
        "......7.2...4..3...39.....8...95...682....9...65..82........6.95...67..3....9..8.",
        ".2...59.661829........7..1.........13.......8..9....2....5.....8.6....349..8..25.",
        ".7...8.29..2.....4854.2......83742.............32617......9.6122.....4..13.6...7.",
        ".........231.9.....65..31....8924...1...5...6...1367....93..57.....1.843.........",
        ".5916......7....9...6....7..647..9.....8..71.......834.25...1....8.7......36.....",
        "...31.........713....5..4.........48765...91.......563...67...4...25........4..71",
        // Even more extreme puzzles
        "..41.....6...4..3...17.6..4....7...8.....2.......1.96.1.2...3.6476.....9..58.....",
        ".16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51.",
        "5.46.8.12672..5.4819.342567859...4..4268...91713...8.6961...2.4287...6.5345...1.9",
        // Naked pair test puzzle
        ".....5..........5.145.67.....8....47..1....6...9...81..56284...2.4.3....8.3......",
    )

    @Test
    fun `extract technique states using HintGenerator`() {
        val eliminator = SimpleCandidateEliminator()
        val techniqueStates = mutableMapOf<String, String>() // technique -> board state

        for (puzzleStr in hardPuzzles) {
            val board = try {
                BoardReader.readBoard(puzzleStr)
            } catch (e: Exception) {
                println("SKIP invalid puzzle: ${e.message?.take(80)}")
                continue
            }
            eliminator.eliminate(board)
            
            // Use HintGenerator directly — it exhausts hidden singles
            // and returns the next technique needed
            val hint = HintGenerator.generate(board.copy())
            val technique = hint?.technique?.displayName ?: "Scanning"
            val boardStr = board.confirmedCellsString()
            
            // Record first occurrence of each technique
            if (technique !in techniqueStates && 
                technique != "Hidden Single" && 
                technique != "Naked Single" &&
                technique != "Scanning") {
                techniqueStates[technique] = boardStr
                println("RECORDED $technique from puzzle: $puzzleStr")
            }
        }
        
        println("\n=== FOUND TECHNIQUE STATES ===")
        techniqueStates.forEach { (technique, state) ->
            println("  $technique: \"$state\"")
        }
    }

    private fun Board.confirmedCellsString(): String = buildString {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (isConfirmed(coord)) append(value(coord)) else append('.')
            }
        }
    }
}
