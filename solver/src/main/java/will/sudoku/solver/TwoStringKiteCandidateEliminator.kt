package will.sudoku.solver

/**
 * 2-String Kite Candidate Eliminator
 *
 * A 2-String Kite uses a candidate that forms two strong links:
 * one in a row and one in a column, connected through a box where
 * the candidate appears in 2 cells (different row & column).
 * The remote ends of the chain eliminate the candidate from their intersection.
 */
class TwoStringKiteCandidateEliminator : CandidateEliminator {

    override val displayName = "2-String Kite"

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            for (value in 1..9) {
                if (eliminateValue(board, value)) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private fun eliminateValue(board: Board, value: Int): Boolean {
        val mask = Board.masks[value - 1]

        // Build row strong links: rows where candidate appears in exactly 2 cols
        val rowLinks = mutableMapOf<Int, Pair<Int, Int>>()
        for (r in 0..8) {
            val cols = (0..8).filter { c ->
                (board.candidatePattern(Coord(r, c)) and mask) != 0
            }
            if (cols.size == 2) rowLinks[r] = Pair(cols[0], cols[1])
        }

        // Build column strong links: cols where candidate appears in exactly 2 rows
        val colLinks = mutableMapOf<Int, Pair<Int, Int>>()
        for (c in 0..8) {
            val rows = (0..8).filter { r ->
                (board.candidatePattern(Coord(r, c)) and mask) != 0
            }
            if (rows.size == 2) colLinks[c] = Pair(rows[0], rows[1])
        }

        var anyUpdate = false

        // Iterate over all 9 boxes
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val regionCoords = mutableListOf<Coord>()
                for (r in boxRow * 3 until boxRow * 3 + 3) {
                    for (c in boxCol * 3 until boxCol * 3 + 3) {
                        regionCoords.add(Coord(r, c))
                    }
                }

                val regionCells = regionCoords.filter { coord ->
                    (board.candidatePattern(coord) and mask) != 0
                }

                if (regionCells.size != 2) continue
                val a = regionCells[0]
                val b = regionCells[1]
                if (a.row == b.row || a.col == b.col) continue

                // Try: cell A forms row strong link, cell B forms col strong link
                if (rowLinks.containsKey(a.row) && colLinks.containsKey(b.col)) {
                    val (ac1, ac2) = rowLinks[a.row]!!
                    val rowOtherCol = if (ac1 == a.col) ac2 else ac1
                    val (br1, br2) = colLinks[b.col]!!
                    val colOtherRow = if (br1 == b.row) br2 else br1
                    val target = Coord(colOtherRow, rowOtherCol)
                    if (board.eraseCandidateValue(target, value)) anyUpdate = true
                }

                // Try: cell A forms col strong link, cell B forms row strong link
                if (colLinks.containsKey(a.col) && rowLinks.containsKey(b.row)) {
                    val (ar1, ar2) = colLinks[a.col]!!
                    val colOtherRow = if (ar1 == a.row) ar2 else ar1
                    val (bc1, bc2) = rowLinks[b.row]!!
                    val rowOtherCol = if (bc1 == b.col) bc2 else bc1
                    val target = Coord(colOtherRow, rowOtherCol)
                    if (board.eraseCandidateValue(target, value)) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }
}
