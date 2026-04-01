package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * XYZ-Wing Candidate Eliminator
 *
 * Detects XYZ-Wing patterns and eliminates candidates accordingly.
 *
 * An XYZ-Wing uses a chain of 3 cells:
 * - Pivot cell has candidates {X, Y, Z} (3 candidates)
 * - Wing 1 has candidates {X, Z} and sees the pivot
 * - Wing 2 has candidates {Y, Z} and sees the pivot
 * - Both wings see the pivot
 *
 * Any cell that sees ALL THREE cells (pivot and both wings) can have Z eliminated.
 *
 * Example:
 * ```
 * Pivot (4,4): candidates {2, 5, 8}
 * Wing 1 (4,1): candidates {2, 8}  - same row as pivot
 * Wing 2 (1,4): candidates {5, 8}  - same column as pivot
 * Cell (1,1) sees pivot, wing1, and wing2
 * → Eliminate 8 from (1,1)
 * ```
 *
 * ## Algorithm
 * 1. Find all cells with exactly 3 candidates (potential pivots)
 * 2. For each pivot with candidates {X, Y, Z}:
 *    a. Find wings with {X, Z} that see the pivot
 *    b. Find wings with {Y, Z} that see the pivot
 *    c. If two wings exist, eliminate Z from cells seeing all three
 *
 * ## Difference from XY-Wing
 * - XY-Wing: Pivot has 2 candidates, eliminates from cells seeing both wings
 * - XYZ-Wing: Pivot has 3 candidates, eliminates from cells seeing all three cells
 */
class XYZWingCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all cells with exactly 3 candidates (potential pivots)
        val pivots = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 3
        }

        for (pivot in pivots) {
            val pivotCandidates = board.candidateValues(pivot).toList()
            if (pivotCandidates.size != 3) continue

            val x = pivotCandidates[0]
            val y = pivotCandidates[1]
            val z = pivotCandidates[2]

            // Try all combinations of which candidate is Z (the common one)
            for (zCandidate in listOf(x, y, z)) {
                val remainingCandidates = pivotCandidates.filter { it != zCandidate }
                if (remainingCandidates.size != 2) continue

                val xCandidate = remainingCandidates[0]
                val yCandidate = remainingCandidates[1]

                // Find wings with {X, Z} and {Y, Z}
                val wingsWithX = findWings(board, pivot, xCandidate, zCandidate)
                val wingsWithY = findWings(board, pivot, yCandidate, zCandidate)

                // Try all combinations of wings
                for (wing1 in wingsWithX) {
                    for (wing2 in wingsWithY) {
                        // Find cells that see all three (pivot, wing1, wing2) and eliminate Z
                        val changed = eliminateFromCommonPeers(board, pivot, wing1, wing2, zCandidate)
                        if (changed) anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find cells with exactly 2 candidates that:
     * - See the pivot
     * - Have the specified two candidates (c1 and c2)
     */
    private fun findWings(board: Board, pivot: Coord, c1: Int, c2: Int): List<Coord> {
        return Coord.all.filter { coord ->
            if (coord == pivot) return@filter false
            if (board.isConfirmed(coord)) return@filter false

            val candidates = board.candidateValues(coord).toSet()
            if (candidates.size != 2) return@filter false

            // Must have exactly the two specified candidates
            if (candidates != setOf(c1, c2)) return@filter false

            // Must see the pivot (same row, column, or region)
            seesEachOther(coord, pivot)
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
     * Eliminate a candidate from all cells that see all three cells (pivot, wing1, wing2).
     */
    private fun eliminateFromCommonPeers(
        board: Board,
        pivot: Coord,
        wing1: Coord,
        wing2: Coord,
        candidate: Int
    ): Boolean {
        var anyUpdate = false

        for (coord in Coord.all) {
            if (coord == pivot || coord == wing1 || coord == wing2) continue
            if (board.isConfirmed(coord)) continue

            // Check if this cell sees all three cells
            if (seesEachOther(coord, pivot) &&
                seesEachOther(coord, wing1) &&
                seesEachOther(coord, wing2)) {
                val changed = board.eraseCandidateValue(coord, candidate)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }
}
