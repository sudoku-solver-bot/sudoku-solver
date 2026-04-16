package will.sudoku.solver

/**
 * Forcing Chains Candidate Eliminator
 *
 * Detects forcing chain patterns and eliminates candidates based on
 * logical consequences of bi-value cell assumptions.
 *
 * ## Forcing Chains Pattern
 *
 * A forcing chain starts with a bi-value cell (a cell with exactly 2 candidates).
 * We explore the consequences of setting the cell to each of its two candidates:
 *
 * 1. If cell A has candidates {X, Y}, we try both "A=X" and "A=Y"
 * 2. For each assumption, we propagate consequences using simple elimination
 * 3. If both paths lead to the same cell having the same value Z, we can confirm Z
 * 4. If one path leads to a contradiction (empty cell), we can eliminate that candidate
 *
 * ## Algorithm
 * 1. Find all bi-value cells (cells with exactly 2 candidates)
 * 2. For each cell, try both candidate values
 * 3. Propagate consequences by actually applying eliminations on a copy
 * 4. Check for contradictions or convergences
 * 5. Apply eliminations based on findings
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/Forcing_Chains
 */
class ForcingChainsCandidateEliminator : CandidateEliminator {

    companion object {
        // Maximum chain depth to explore (prevents exponential blowup)
        private const val MAX_CHAIN_DEPTH = 10
    }

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all bi-value cells
        val biValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidateValues(coord).size == 2
        }

        for (pivot in biValueCells) {
            val candidates = board.candidateValues(pivot).toList()
            if (candidates.size != 2) continue

            val (x, y) = candidates[0] to candidates[1]

            // Explore both paths on copies of the board
            val boardX = board.copy()
            val boardY = board.copy()

            val resultX = exploreConsequences(boardX, pivot, x)
            val resultY = exploreConsequences(boardY, pivot, y)

            // Check for contradictions
            if (resultX.isContradiction) {
                val erased = board.eraseCandidateValue(pivot, x)
                if (erased) anyUpdate = true
                continue
            }

            if (resultY.isContradiction) {
                val erased = board.eraseCandidateValue(pivot, y)
                if (erased) anyUpdate = true
                continue
            }

            // Contradiction-based eliminations only (safe)
            // Convergence logic is disabled because chain propagation is
            // incomplete and can produce spurious convergences that
            // eliminate the correct value from a cell.
            // TODO: Re-enable convergence with proper propagation if needed.
        }

        return anyUpdate
    }

    /**
     * Explore consequences of setting a cell to a specific value.
     * Actually applies eliminations on the board copy for accurate propagation.
     */
    private fun exploreConsequences(
        board: Board,
        startCoord: Coord,
        startValue: Int
    ): ChainResult {
        val confirmed = mutableMapOf<Coord, Int>()
        val toProcess = ArrayDeque<Triple<Coord, Int, Int>>() // coord, value, depth

        toProcess.add(Triple(startCoord, startValue, 0))

        while (toProcess.isNotEmpty()) {
            val (coord, value, depth) = toProcess.removeFirst()

            if (depth > MAX_CHAIN_DEPTH) continue

            // If this cell is already confirmed to a different value, contradiction
            if (board.isConfirmed(coord)) {
                val existingValue = board.value(coord)
                if (existingValue != 0 && existingValue != value) {
                    return ChainResult(isContradiction = true, confirmedValues = confirmed)
                }
                if (existingValue == value) continue // already set
            }

            // If this cell doesn't have this candidate, contradiction
            if (!board.candidateValues(coord).contains(value)) {
                return ChainResult(isContradiction = true, confirmedValues = confirmed)
            }

            // Actually apply: set the cell to this value
            board.markValue(coord, value)
            confirmed[coord] = value

            // Propagate: eliminate this value from all peers
            val peers = getPeers(coord)
            for (peer in peers) {
                if (board.eraseCandidateValue(peer, value)) {
                    // Check if peer now has zero candidates — contradiction
                    if (board.candidatePattern(peer) == 0) {
                        return ChainResult(isContradiction = true, confirmedValues = confirmed)
                    }
                    // If peer now has exactly one candidate, it's a forced value
                    if (board.isConfirmed(peer) && peer !in confirmed) {
                        val forcedValue = board.value(peer)
                        if (forcedValue != 0) {
                            toProcess.add(Triple(peer, forcedValue, depth + 1))
                        }
                    }
                }
            }
        }

        return ChainResult(isContradiction = false, confirmedValues = confirmed)
    }

    /**
     * Get all peers (cells that share a row, column, or box) of a coordinate.
     */
    private fun getPeers(coord: Coord): Set<Coord> {
        val peers = mutableSetOf<Coord>()
        for (group in CoordGroup.all) {
            if (coord in group.coords) {
                peers.addAll(group.coords)
            }
        }
        peers.remove(coord)
        return peers
    }

    /**
     * Result of exploring a forcing chain.
     */
    private data class ChainResult(
        val isContradiction: Boolean,
        val confirmedValues: Map<Coord, Int>
    )
}
