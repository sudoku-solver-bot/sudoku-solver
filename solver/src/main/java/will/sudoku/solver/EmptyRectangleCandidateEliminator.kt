package will.sudoku.solver

/**
 * Empty Rectangle Candidate Eliminator
 *
 * Algorithm (SudokuWiki-based):
 * 1. Identify ERIs (Empty Rectangle Intersections): candidate cells that
 *    have other candidates in both their row and column within the box
 *    (L-shaped pattern).
 * 2. For each row/column strong link, check if one end (the "near end")
 *    sees the ERI (same row or column).
 * 3. Eliminate candidate from the cell at the intersection of the far end
 *    and the ERI:
 *    - Row link, near end shares col → target at (ERI.row, farEnd.col)
 *    - Column link, near end shares row → target at (farEnd.row, ERI.col)
 * 4. Both the near end and the elimination target must be outside the
 *    ERI's box.
 *
 * Reference: https://www.sudokuwiki.org/Empty_Rectangles
 */
class EmptyRectangleCandidateEliminator : CandidateEliminator {

    override val displayName = "Empty Rectangle"

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
        var anyUpdate = false

        // Build column strong links
        val colLinks = mutableMapOf<Int, Pair<Int, Int>>()
        for (c in 0..8) {
            val rows = (0..8).filter { r ->
                !board.isConfirmed(Coord(r, c)) && (board.candidatePattern(Coord(r, c)) and mask) != 0
            }
            if (rows.size == 2) colLinks[c] = Pair(rows[0], rows[1])
        }

        // Build row strong links
        val rowLinks = mutableMapOf<Int, Pair<Int, Int>>()
        for (r in 0..8) {
            val cols = (0..8).filter { c ->
                !board.isConfirmed(Coord(r, c)) && (board.candidatePattern(Coord(r, c)) and mask) != 0
            }
            if (cols.size == 2) rowLinks[r] = Pair(cols[0], cols[1])
        }

        // Iterate over all 9 boxes
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val regionCoords = mutableListOf<Coord>()
                for (r in boxRow * 3 until boxRow * 3 + 3) {
                    for (c in boxCol * 3 until boxCol * 3 + 3) {
                        regionCoords.add(Coord(r, c))
                    }
                }

                val cells = regionCoords.filter { coord ->
                    !board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0
                }
                if (cells.size < 2) continue

                val regionIndex = boxRow * 3 + boxCol

                for (eri in cells) {
                    // ERI must have other candidates in both its row and column
                    // (within the box) — forming an L-shaped pattern.
                    val hasRowBuddy = cells.any { c -> c !== eri && c.row == eri.row }
                    val hasColBuddy = cells.any { c -> c !== eri && c.col == eri.col }
                    if (!hasRowBuddy || !hasColBuddy) continue

                    val eriBox = eri.region
                    val eriRow = eri.row
                    val eriCol = eri.col

                    // Row strong links: near end shares column with ERI
                    // Target = (ERI.row, farEnd.col)
                    for ((slRow, link) in rowLinks) {
                        val (c1, c2) = link
                        val farCol = when {
                            c1 == eriCol -> c2
                            c2 == eriCol -> c1
                            else -> null
                        } ?: continue

                        val nearEnd = Coord(slRow, eriCol)
                        if (nearEnd.region == eriBox) continue

                        val target = Coord(eriRow, farCol)
                        if (target.region == eriBox) continue
                        if (!board.isConfirmed(target) && (board.candidatePattern(target) and mask) != 0) {
                            board.eraseCandidateValue(target, value)
                            anyUpdate = true
                        }
                    }

                    // Column strong links: near end shares row with ERI
                    // Target = (farEnd.row, ERI.col)
                    for ((slCol, link) in colLinks) {
                        val (r1, r2) = link
                        val farRow = when {
                            r1 == eriRow -> r2
                            r2 == eriRow -> r1
                            else -> null
                        } ?: continue

                        val nearEnd = Coord(eriRow, slCol)
                        if (nearEnd.region == eriBox) continue

                        val target = Coord(farRow, eriCol)
                        if (target.region == eriBox) continue
                        if (!board.isConfirmed(target) && (board.candidatePattern(target) and mask) != 0) {
                            board.eraseCandidateValue(target, value)
                            anyUpdate = true
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}
