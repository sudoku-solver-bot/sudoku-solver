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
 * ## Example
 *
 * Cell A has candidates {1, 2}:
 * - If A=1, then through a chain of deductions, cell B=3
 * - If A=2, then through a chain of deductions, cell B=3
 * - Therefore, cell B MUST be 3 (both paths agree)
 *
 * Or:
 * - If A=1, we eventually reach a contradiction (cell C has no candidates)
 * - Therefore, A cannot be 1, so eliminate 1 from A
 *
 * ## Algorithm
 * 1. Find all bi-value cells (cells with exactly 2 candidates)
 * 2. For each cell, try both candidate values
 * 3. Propagate consequences using simple elimination (limited depth to avoid exponential blowup)
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

            // Explore both paths
            val resultX = exploreConsequences(board.copy(), pivot, x)
            val resultY = exploreConsequences(board.copy(), pivot, y)

            // Check for contradictions
            if (resultX.isContradiction) {
                // X leads to contradiction, eliminate X from pivot
                val erased = board.eraseCandidateValue(pivot, x)
                if (erased) anyUpdate = true
                // Continue to next pivot (we modified the board)
                continue
            }

            if (resultY.isContradiction) {
                // Y leads to contradiction, eliminate Y from pivot
                val erased = board.eraseCandidateValue(pivot, y)
                if (erased) anyUpdate = true
                continue
            }

            // Check for convergences (both paths lead to same conclusion)
            val convergence = findConvergence(resultX, resultY)
            if (convergence != null) {
                val (coord, value) = convergence
                if (!board.isConfirmed(coord)) {
                    val originalPattern = board.candidatePattern(coord)
                    val newValue = Board.masks[value - 1]

                    // If cell doesn't already have this value confirmed
                    if (originalPattern != newValue) {
                        // Mark the value (eliminates all other candidates)
                        val changed = markCandidate(board, coord, value)
                        if (changed) anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Explore consequences of setting a cell to a specific value.
     *
     * Returns a ChainResult containing:
     * - isContradiction: whether the chain led to a contradiction
     * - confirmedValues: map of coordinates to confirmed values
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

            // Check depth limit
            if (depth > MAX_CHAIN_DEPTH) {
                continue
            }

            // If we already processed this coord with a different value, contradiction
            if (coord in confirmed) {
                if (confirmed[coord] != value) {
                    return ChainResult(isContradiction = true, confirmedValues = confirmed)
                }
                continue
            }

            // If this cell doesn't have this candidate, contradiction
            if (!board.candidateValues(coord).contains(value)) {
                return ChainResult(isContradiction = true, confirmedValues = confirmed)
            }

            // Mark this cell as having this value
            confirmed[coord] = value

            // Get consequences using simple elimination
            val consequences = getConsequences(board, coord, value)

            // Add consequences to queue
            for ((nextCoord, nextValue) in consequences) {
                toProcess.add(Triple(nextCoord, nextValue, depth + 1))
            }
        }

        return ChainResult(isContradiction = false, confirmedValues = confirmed)
    }

    /**
     * Get immediate consequences of setting a cell to a value.
     * Returns a list of (coord, value) pairs that can be deduced.
     */
    private fun getConsequences(
        board: Board,
        coord: Coord,
        value: Int
    ): List<Pair<Coord, Int>> {
        val consequences = mutableListOf<Pair<Coord, Int>>()

        // For each group that contains this cell
        val groups = listOf(
            CoordGroup.verticalOf(coord),
            CoordGroup.horizontalOf(coord),
            CoordGroup.regionOf(coord)
        )

        for (group in groups) {
            // Check for hidden singles in the group
            for (candidate in 1..9) {
                // Find all cells in this group that can have this candidate
                val cellsWithCandidate = group.coords.filter { c ->
                    !board.isConfirmed(c) && board.candidateValues(c).contains(candidate)
                }

                // If only one cell can have this candidate, it's a hidden single
                if (cellsWithCandidate.size == 1 && cellsWithCandidate[0] != coord) {
                    consequences.add(cellsWithCandidate[0] to candidate)
                }
            }

            // Check for naked singles (cells with only one candidate left)
            for (c in group.coords) {
                if (c == coord) continue
                if (board.isConfirmed(c)) continue

                val candidates = board.candidateValues(c)
                if (candidates.size == 1) {
                    consequences.add(c to candidates[0])
                }
            }
        }

        return consequences
    }

    /**
     * Find convergence between two chain results.
     * Returns a pair of (coord, value) if both paths agree on a value for a cell.
     */
    private fun findConvergence(
        result1: ChainResult,
        result2: ChainResult
    ): Pair<Coord, Int>? {
        for ((coord, value1) in result1.confirmedValues) {
            val value2 = result2.confirmedValues[coord]
            if (value2 != null && value1 == value2) {
                // Both paths agree on this value for this cell
                return Pair(coord, value1)
            }
        }
        return null
    }

    /**
     * Mark a candidate value in a cell, eliminating all other candidates.
     * Returns true if the board was modified.
     */
    private fun markCandidate(board: Board, coord: Coord, value: Int): Boolean {
        var anyUpdate = false
        val candidates = board.candidateValues(coord)

        for (candidate in candidates) {
            if (candidate != value) {
                val erased = board.eraseCandidateValue(coord, candidate)
                if (erased) anyUpdate = true
            }
        }

        return anyUpdate
    }

    /**
     * Result of exploring a forcing chain.
     */
    private data class ChainResult(
        val isContradiction: Boolean,
        val confirmedValues: Map<Coord, Int>
    )
}
