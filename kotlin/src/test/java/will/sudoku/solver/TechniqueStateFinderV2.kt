package will.sudoku.solver

import org.junit.jupiter.api.Test

/**
 * Finds puzzle states where hidden singles are EXHAUSTED and the target technique is next.
 * These states can be used as tutorial examplePuzzle values.
 */
class TechniqueStateFinderV2 {

    private val hardPuzzles = listOf(
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
        "..41.....6...4..3...17.6..4....7...8.....2.......1.96.1.2...3.6476.....9..58.....",
        ".16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51.",
        "5.46.8.12672..5.4819.342567859...4..4268...91713...8.6961...2.4287...6.5345...1.9",
        ".....5..........5.145.67.....8....47..1....6...9...81..56284...2.4.3....8.3......",
        // Even harder puzzles for advanced techniques
        "000900300005821000190600000016000290000070000043000670000008069000213800004009000",
        "720408030080000047401076802810739000000851000000264080209680413340000008168943275",
    )

    @Test
    fun `extract states after hidden singles exhaustion`() {
        val eliminator = SimpleCandidateEliminator()
        val techniqueStates = mutableMapOf<String, String>() 
        val foundTechniques = mutableSetOf<String>()

        for (puzzleStr in hardPuzzles) {
            val board = try {
                BoardReader.readBoard(puzzleStr)
            } catch (e: Exception) {
                println("SKIP invalid: ${e.message?.take(60)}")
                continue
            }
            
            // Apply basic elimination
            eliminator.eliminate(board)
            
            // Exhaust hidden singles (same as HintGenerator.applyHiddenSinglesUntilStable)
            exhaustHiddenSingles(board, eliminator)
            
            // Now record the state and check what technique is next
            val boardStr = confirmedCellsString(board)
            val hint = HintGenerator.generate(board.copy())
            val technique = hint?.technique?.displayName ?: "Scanning"
            
            if (technique !in foundTechniques && 
                technique != "Hidden Single" && 
                technique != "Naked Single" &&
                technique != "Scanning") {
                foundTechniques.add(technique)
                techniqueStates[technique] = boardStr
                
                // Count confirmed cells
                val confirmedCount = boardStr.count { it != '.' && it != '0' }
                println("RECORDED $technique (confirmed=$confirmedCount) from: $puzzleStr")
                println("  State: $boardStr")
            }
        }
        
        println("\n=== FOUND TECHNIQUE STATES (after hidden singles exhaustion) ===")
        techniqueStates.forEach { (technique, state) ->
            val confirmedCount = state.count { it != '.' && it != '0' }
            println("  $technique ($confirmedCount confirmed): \"$state\"")
        }
    }

    private fun exhaustHiddenSingles(board: Board, eliminator: SimpleCandidateEliminator) {
        var foundAny = true
        while (foundAny) {
            foundAny = false
            var foundOne: Boolean
            do {
                foundOne = false
                val hint = HintGenerator.findHiddenSingle(board)
                if (hint != null) {
                    board.markValue(hint.coord, hint.value)
                    foundAny = true
                    foundOne = true
                    eliminator.eliminate(board)
                }
            } while (foundOne)
        }
    }

    private fun confirmedCellsString(board: Board): String = buildString {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val coord = Coord(row, col)
                if (board.isConfirmed(coord)) append(board.value(coord)) else append('.')
            }
        }
    }
}
