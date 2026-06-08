package will.sudoku.solver

/**
 * Empty Rectangle Candidate Eliminator
 *
 * In a 3x3 box, if a candidate's cells form an L-shape (confined to
 * specific rows and columns), find a strong link outside the box that
 * intersects and eliminate the candidate from the chain's remote intersection.
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
                (board.candidatePattern(Coord(r, c)) and mask) != 0
            }
            if (rows.size == 2) colLinks[c] = Pair(rows[0], rows[1])
        }

        // Build row strong links
        val rowLinks = mutableMapOf<Int, Pair<Int, Int>>()
        for (r in 0..8) {
            val cols = (0..8).filter { c ->
                (board.candidatePattern(Coord(r, c)) and mask) != 0
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
                    (board.candidatePattern(coord) and mask) != 0
                }
                if (cells.size < 2) continue

                val candidateRows = cells.map { it.row }.toSet()
                val candidateCols = cells.map { it.col }.toSet()

                // Need L-shaped pattern: candidate occupies multiple rows AND columns
                if (candidateRows.size < 2 || candidateCols.size < 2) continue

                val regionIndex = boxRow * 3 + boxCol

                for (erRow in candidateRows) {
                    for (erCol in candidateCols) {
                        // Check for strong link in erRow — one end must be in the box
                        val rowLink = rowLinks[erRow]
                        if (rowLink != null) {
                            val (r1, r2) = rowLink
                            val r1InBox = Coord(r1, erCol).region == regionIndex
                            val r2InBox = Coord(r2, erCol).region == regionIndex
                            if (r1InBox && !r2InBox) {
                                val target = Coord(r2, erCol)
                                if ((board.candidatePattern(target) and mask) != 0) {
                                    board.eraseCandidateValue(target, value)
                                    anyUpdate = true
                                }
                            } else if (r2InBox && !r1InBox) {
                                val target = Coord(r1, erCol)
                                if ((board.candidatePattern(target) and mask) != 0) {
                                    board.eraseCandidateValue(target, value)
                                    anyUpdate = true
                                }
                            }
                        }

                        // Check for strong link in erCol — one end must be in the box
                        val colLink = colLinks[erCol]
                        if (colLink != null) {
                            val (c1, c2) = colLink
                            val c1InBox = Coord(erRow, c1).region == regionIndex
                            val c2InBox = Coord(erRow, c2).region == regionIndex
                            if (c1InBox && !c2InBox) {
                                val target = Coord(erRow, c2)
                                if ((board.candidatePattern(target) and mask) != 0) {
                                    board.eraseCandidateValue(target, value)
                                    anyUpdate = true
                                }
                            } else if (c2InBox && !c1InBox) {
                                val target = Coord(erRow, c1)
                                if ((board.candidatePattern(target) and mask) != 0) {
                                    board.eraseCandidateValue(target, value)
                                    anyUpdate = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return anyUpdate
    }
}
