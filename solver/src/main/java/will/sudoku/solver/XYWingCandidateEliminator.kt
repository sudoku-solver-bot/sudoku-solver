package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * XY-Wing Candidate Eliminator
 *
 * Detects XY-Wing patterns and eliminates candidates accordingly.
 *
 * An XY-Wing uses a chain of 3 cells:
 * - Pivot cell has candidates {X, Y}
 * - Wing 1 has candidates {X, Z} and sees the pivot
 * - Wing 2 has candidates {Y, Z} and sees the pivot
 * - Both wings can see each other
 *
 * Any cell that sees BOTH wings can have Z eliminated as a candidate.
 *
 * Example:
 * ```
 * Pivot (4,4): candidates {2, 5}
 * Wing 1 (4,1): candidates {2, 8}  - same row as pivot
 * Wing 2 (1,4): candidates {5, 8}  - same column as pivot
 * Cell (1,1) sees both wings (same row as wing2, same column as wing1)
 * → Eliminate 8 from (1,1)
 * ```
 *
 * ## Algorithm
 * 1. Find all cells with exactly 2 candidates (potential pivots)
 * 2. For each pivot with candidates {X, Y}:
 *    a. Find wings with {X, Z} that see the pivot
 *    b. Find wings with {Y, Z} that see the pivot
 *    c. If two wings exist and see each other, eliminate Z from cells seeing both
 */
class XYWingCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all cells with exactly 2 candidates (potential pivots)
        val pivots = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
        }

        for (pivot in pivots) {
            val pivotCandidates = board.candidateValues(pivot).toList()
            if (pivotCandidates.size != 2) continue

            val x = pivotCandidates[0]
            val y = pivotCandidates[1]

            // Find wings with {X, Z} and {Y, Z}
            val wingsWithX = findWings(board, pivot, x)
            val wingsWithY = findWings(board, pivot, y)

            // Try all combinations of wings
            for (wing1 in wingsWithX) {
                for (wing2 in wingsWithY) {
                    // Find common candidate Z
                    val wing1Candidates = board.candidateValues(wing1).toSet()
                    val wing2Candidates = board.candidateValues(wing2).toSet()

                    // Wing1 has {X, Z}, Wing2 has {Y, Z}
                    // Z is the candidate in both wings that's not X or Y
                    val z1 = wing1Candidates.find { it != x }
                    val z2 = wing2Candidates.find { it != y }

                    if (z1 == null || z2 == null || z1 != z2) continue
                    val z = z1

                    // Check if wings see each other
                    if (!seesEachOther(wing1, wing2)) continue

                    // Find cells that see both wings and eliminate Z
                    val changed = eliminateFromCommonPeers(board, wing1, wing2, z)
                    if (changed) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find cells with exactly 2 candidates that:
     * - See the pivot
     * - Share one candidate with the pivot (sharedCandidate)
     * - Have a different second candidate (Z)
     */
    private fun findWings(board: Board, pivot: Coord, sharedCandidate: Int): List<Coord> {
        return Coord.all.filter { coord ->
            if (coord == pivot) return@filter false
            if (board.isConfirmed(coord)) return@filter false

            val candidates = board.candidateValues(coord)
            if (candidates.size != 2) return@filter false

            // Must share the specified candidate with pivot
            if (sharedCandidate !in candidates) return@filter false

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
     * Eliminate a candidate from all cells that see both wing1 and wing2.
     */
    private fun eliminateFromCommonPeers(board: Board, wing1: Coord, wing2: Coord, candidate: Int): Boolean {
        var anyUpdate = false

        for (coord in Coord.all) {
            if (coord == wing1 || coord == wing2) continue
            if (board.isConfirmed(coord)) continue

            // Check if this cell sees both wings
            if (seesEachOther(coord, wing1) && seesEachOther(coord, wing2)) {
                val changed = board.eraseCandidateValue(coord, candidate)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }
}
