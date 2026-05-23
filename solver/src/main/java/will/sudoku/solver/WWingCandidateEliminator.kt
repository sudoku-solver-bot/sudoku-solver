package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * W-Wing Candidate Eliminator
 *
 * Detects W-Wing patterns and eliminates candidates accordingly.
 *
 * A W-Wing uses 4 cells in a specific configuration:
 * - Cell A has candidates {X, Y}
 * - Cell B has candidates {X, Y}
 * - Cell C has candidate X and is linked to cell A (same unit)
 * - Cell D has candidate X and is linked to cell B (same unit)
 * - Cells C and D are connected (same row, column, or region)
 *
 * Any cell that sees BOTH cell A and cell B can have Y eliminated.
 *
 * Example:
 * ```
 * Cell A (1,2): candidates {2, 8}
 * Cell B (5,7): candidates {2, 8}
 * Cell C (1,8): has value 2 (same row as A)
 * Cell D (5,8): has value 2 (same row as B, same column as C)
 * Cell (1,7) sees both A and B
 * → Eliminate 8 from (1,7)
 * ```
 *
 * ## Algorithm
 * 1. Find all pairs of bi-value cells with the same candidates {X, Y}
 * 2. For each pair, check if they're connected via a strong link on X
 * 3. A strong link exists if there's a cell with X that sees one of the pair,
 *    and another cell with X that sees the other, and these two X-cells see each other
 * 4. If the pattern exists, eliminate Y from cells seeing both bi-value cells
 *
 * ## Key Insight
 * The W-Wing works because at least one of the bi-value cells must contain X.
 * If cell A has X, then cell B must have Y (and vice versa).
 * Therefore, any cell seeing both cannot have Y as a candidate.
 */
class WWingCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all bi-value cells (exactly 2 candidates)
        val biValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
        }

        // Group bi-value cells by their candidate pairs
        val cellsByCandidatePair = biValueCells.groupBy { coord ->
            val candidates = board.candidateValues(coord).toSet()
            candidates to candidates  // Use pair as key for grouping
        }.mapValues { (_, cells) ->
            cells.map { it to board.candidateValues(it).toSet() }
        }

        // Try all pairs of bi-value cells with the same candidates
        for ((_, cellList) in cellsByCandidatePair) {
            for (i in cellList.indices) {
                for (j in i + 1 until cellList.size) {
                    val (cell1, candidates1) = cellList[i]
                    val (cell2, candidates2) = cellList[j]

                    if (candidates1 != candidates2) continue
                    if (candidates1.size != 2) continue

                    val candidateList = candidates1.toList()
                    val x = candidateList[0]
                    val y = candidateList[1]

                    // Check if there's a strong link on X or Y
                    val changed = checkWWing(board, cell1, cell2, x, y) ||
                                  checkWWing(board, cell1, cell2, y, x)

                    if (changed) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    /**
     * Check if a W-Wing exists with the given configuration.
     *
     * @param cell1 First bi-value cell with candidates {link, target}
     * @param cell2 Second bi-value cell with candidates {link, target}
     * @param link The candidate that forms the strong link
     * @param target The candidate to eliminate from cells seeing both bi-value cells
     * @return true if any candidates were eliminated
     */
    private fun checkWWing(board: Board, cell1: Coord, cell2: Coord, link: Int, target: Int): Boolean {
        // Find cells that have 'link' as a candidate and see cell1
        val linkCells1 = findLinkCells(board, cell1, link)

        // Find cells that have 'link' as a candidate and see cell2
        val linkCells2 = findLinkCells(board, cell2, link)

        // Check if any pair of link cells see each other (forming the bridge)
        for (linkCell1 in linkCells1) {
            for (linkCell2 in linkCells2) {
                // Skip if link cells are the same (would create a degenerate W-Wing)
                if (linkCell1 == linkCell2) continue

                if (seesEachOther(linkCell1, linkCell2)) {
                    // Verify this is a proper W-Wing by checking the strong link condition:
                    // The two link cells should be the only cells in their shared unit with 'link'
                    if (isStrongLink(board, linkCell1, linkCell2, link)) {
                        // Found a valid W-Wing! Eliminate 'target' from cells seeing both bi-value cells
                        return eliminateFromCommonPeers(board, cell1, cell2, target)
                    }
                }
            }
        }

        return false
    }

    /**
     * Check if two cells form a strong link on a candidate.
     *
     * A strong link exists when both cells are in the same unit (row/col/region)
     * and they are the ONLY two cells in that unit with the specified candidate.
     *
     * @param cell1 First cell
     * @param cell2 Second cell
     * @param candidate The candidate to check
     * @return true if a strong link exists
     */
    private fun isStrongLink(board: Board, cell1: Coord, cell2: Coord, candidate: Int): Boolean {
        // If they're not in the same unit, no strong link
        if (!seesEachOther(cell1, cell2)) return false

        val candidateMask = Board.masks[candidate - 1]

        // Check each shared unit
        val sharedUnits = mutableListOf<List<Coord>>()

        // Same row?
        if (cell1.row == cell2.row) {
            sharedUnits.add(CoordGroup.horizontalOf(cell1).coords)
        }

        // Same column?
        if (cell1.col == cell2.col) {
            sharedUnits.add(CoordGroup.verticalOf(cell1).coords)
        }

        // Same region?
        if (sameRegion(cell1, cell2)) {
            sharedUnits.add(CoordGroup.regionOf(cell1).coords)
        }

        // For each shared unit, check if they're the only two cells with the candidate
        for (unit in sharedUnits) {
            val cellsWithCandidate = unit.filter { coord ->
                !board.isConfirmed(coord) &&
                (board.candidatePattern(coord) and candidateMask) != 0
            }

            // Strong link exists if exactly 2 cells in the unit have this candidate
            if (cellsWithCandidate.size == 2 &&
                cellsWithCandidate.contains(cell1) &&
                cellsWithCandidate.contains(cell2)) {
                return true
            }
        }

        return false
    }

    /**
     * Find cells that have the specified candidate and see the given cell.
     */
    private fun findLinkCells(board: Board, cell: Coord, candidate: Int): List<Coord> {
        return Coord.all.filter { coord ->
            if (coord == cell) return@filter false
            // Check if coord has 'candidate' as a possible value
            if ((board.candidatePattern(coord) and Board.masks[candidate - 1]) == 0) return@filter false
            seesEachOther(coord, cell)
        }
    }

    /**
     * Check if two cells see each other (same row, column, or region).
     */
    private fun seesEachOther(coord1: Coord, coord2: Coord): Boolean {
        return coord1.row == coord2.row ||
                coord1.col == coord2.col ||
                sameRegion(coord1, coord2)
    }

    /**
     * Check if two cells are in the same 3x3 region.
     */
    private fun sameRegion(coord1: Coord, coord2: Coord): Boolean {
        val regionRow1 = coord1.row / 3
        val regionCol1 = coord1.col / 3
        val regionRow2 = coord2.row / 3
        val regionCol2 = coord2.col / 3
        return regionRow1 == regionRow2 && regionCol1 == regionCol2
    }

    /**
     * Eliminate a candidate from all cells that see both cell1 and cell2.
     */
    private fun eliminateFromCommonPeers(board: Board, cell1: Coord, cell2: Coord, candidate: Int): Boolean {
        var anyUpdate = false

        for (coord in Coord.all) {
            if (coord == cell1 || coord == cell2) continue
            if (board.isConfirmed(coord)) continue

            // Check if this cell has the candidate and sees both bi-value cells
            if (candidate in board.candidateValues(coord) &&
                seesEachOther(coord, cell1) && seesEachOther(coord, cell2)) {
                val changed = board.eraseCandidateValue(coord, candidate)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }
}
