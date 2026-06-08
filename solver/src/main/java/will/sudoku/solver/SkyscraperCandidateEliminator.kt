package will.sudoku.solver

/**
 * Skyscraper Candidate Eliminator
 *
 * A Skyscraper is an X-Wing variant where two rows each have a candidate
 * in exactly 2 cells, sharing exactly 1 column. The candidate can be
 * eliminated from cells that see both non-aligned ends.
 *
 * Pattern (rows): Rows r1 and r2 each have candidate in 2 cells.
 * They share column sc. The non-shared columns are c1 (in r1) and c2 (in r2).
 * Eliminate candidate from (r1, c2) and (r2, c1).
 */
class SkyscraperCandidateEliminator : CandidateEliminator {

    override val displayName = "Skyscraper"

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            if (skyscraperRows(board)) { anyUpdate = true; stable = false }
            if (skyscraperCols(board)) { anyUpdate = true; stable = false }
        } while (!stable)
        return anyUpdate
    }

    private fun skyscraperRows(board: Board): Boolean {
        var anyUpdate = false
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            // Find rows where candidate appears in exactly 2 cells
            val rowPositions = mutableMapOf<Int, List<Int>>()
            for (r in 0..8) {
                val cols = (0..8).filter { c ->
                    !board.isConfirmed(Coord(r, c)) && (board.candidatePattern(Coord(r, c)) and mask) != 0
                }
                if (cols.size == 2) rowPositions[r] = cols
            }
            if (rowPositions.size < 2) continue

            val rows = rowPositions.keys.toList()
            for (i in rows.indices) {
                for (j in i + 1 until rows.size) {
                    val r1 = rows[i]
                    val r2 = rows[j]
                    val cols1 = rowPositions[r1]!!
                    val cols2 = rowPositions[r2]!!

                    // Find shared column
                    val shared = cols1.filter { it in cols2 }
                    if (shared.size != 1) continue
                    val sc = shared[0]

                    // Non-shared columns
                    val c1 = cols1.first { it != sc }
                    val c2 = cols2.first { it != sc }

                    // Eliminate from cells that see both non-aligned ends
                    val targets = listOf(Coord(r1, c2), Coord(r2, c1))
                    for (t in targets) {
                        if (board.eraseCandidateValue(t, value)) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }
        return anyUpdate
    }

    private fun skyscraperCols(board: Board): Boolean {
        var anyUpdate = false
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            val colPositions = mutableMapOf<Int, List<Int>>()
            for (c in 0..8) {
                val rows = (0..8).filter { r ->
                    !board.isConfirmed(Coord(r, c)) && (board.candidatePattern(Coord(r, c)) and mask) != 0
                }
                if (rows.size == 2) colPositions[c] = rows
            }
            if (colPositions.size < 2) continue

            val cols = colPositions.keys.toList()
            for (i in cols.indices) {
                for (j in i + 1 until cols.size) {
                    val c1 = cols[i]
                    val c2 = cols[j]
                    val rows1 = colPositions[c1]!!
                    val rows2 = colPositions[c2]!!

                    val shared = rows1.filter { it in rows2 }
                    if (shared.size != 1) continue
                    val sr = shared[0]

                    val r1 = rows1.first { it != sr }
                    val r2 = rows2.first { it != sr }

                    val targets = listOf(Coord(r1, c2), Coord(r2, c1))
                    for (t in targets) {
                        if (board.eraseCandidateValue(t, value)) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}
