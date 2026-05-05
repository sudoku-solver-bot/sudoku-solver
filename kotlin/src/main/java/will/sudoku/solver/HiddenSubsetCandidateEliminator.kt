package will.sudoku.solver

/**
 * Hidden Subset Candidate Eliminator
 *
 * Detects hidden pairs, triples, and quads within groups (rows, columns, regions).
 *
 * A hidden subset occurs when N candidates appear in exactly N cells within a group,
 * even if those cells also contain other candidates. In this case, all other candidates
 * can be removed from those N cells.
 *
 * Example (Hidden Pair):
 * In a group, candidates {2,3} only appear in cells A and B (possibly among other candidates).
 * This means cells A and B MUST be 2 or 3, so all other candidates can be removed from A and B.
 *
 * Algorithm:
 * 1. For each group, find candidate values that appear in 2-4 cells
 * 2. Look for subsets where N candidates appear in exactly N cells
 * 3. Remove all other candidates from those N cells
 *
 * This eliminator can handle:
 * - Hidden pairs (2 candidates in 2 cells)
 * - Hidden triples (3 candidates in 3 cells)
 * - Hidden quads (4 candidates in 4 cells)
 */
class HiddenSubsetCandidateEliminator : CandidateEliminator {
    override val displayName = "Hidden Subset"

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean

        do {
            stable = true
            for (coordGroup in CoordGroup.all) {
                val updated = eliminateHiddenSubsets(board, coordGroup)
                if (updated) {
                    anyUpdate = true
                    stable = false
                }
            }
        } while (!stable)

        return anyUpdate
    }

    /**
     * Find and eliminate hidden subsets in a single group.
     *
     * Process subsets of size 2 (pairs), 3 (triples), and 4 (quads).
     */
    private fun eliminateHiddenSubsets(board: Board, group: CoordGroup): Boolean {
        // Build a map: candidate value -> list of cells containing it
        val candidateToCells = mutableMapOf<Int, MutableList<Coord>>()

        for (coord in group.coords) {
            val candidates = board.candidateValues(coord)
            for (candidate in candidates) {
                candidateToCells.getOrPut(candidate) { mutableListOf() }.add(coord)
            }
        }

        // Filter to candidates that appear in 2-4 cells (suitable for hidden subsets)
        val limitedCandidates = candidateToCells.filterValues { cells ->
            cells.size in 2..4
        }

        if (limitedCandidates.isEmpty()) return false

        var anyUpdate = false

        // Check for hidden pairs (2 candidates in 2 cells)
        anyUpdate = anyUpdate or checkHiddenSubset(board, limitedCandidates, 2)

        // Check for hidden triples (3 candidates in 3 cells)
        anyUpdate = anyUpdate or checkHiddenSubset(board, limitedCandidates, 3)

        // Check for hidden quads (4 candidates in 4 cells)
        anyUpdate = anyUpdate or checkHiddenSubset(board, limitedCandidates, 4)

        return anyUpdate
    }

    /**
     * Check for a hidden subset of the given size.
     *
     * @param candidateToCells Map of candidate values to cells containing them
     * @param subsetSize Size of subset to check (2=pair, 3=triple, 4=quad)
     * @return true if any eliminations were made
     */
    private fun checkHiddenSubset(
        board: Board,
        candidateToCells: Map<Int, List<Coord>>,
        subsetSize: Int
    ): Boolean {
        // Get all candidates that appear in <= subsetSize cells
        val eligibleCandidates = candidateToCells.filterValues { it.size <= subsetSize }

        if (eligibleCandidates.size < subsetSize) return false

        // Try all combinations of subsetSize candidates
        val candidates = eligibleCandidates.keys.toList()
        var anyUpdate = false

        for (candidateCombo in candidates.combinations(subsetSize)) {
            // Get all cells that contain these candidates
            val cellsWithCandidates = candidateCombo.flatMap { candidate ->
                candidateToCells[candidate] ?: emptyList()
            }.toSet()

            // Check if the cells count equals the subset size
            if (cellsWithCandidates.size == subsetSize) {
                // Found a hidden subset! Remove all other candidates from these cells
                val comboMasks = candidateCombo.map { Board.masks[it - 1] }
                val candidatesToRemove = Board.masks.filterNot { mask ->
                    mask in comboMasks
                }

                for (cell in cellsWithCandidates) {
                    for (candidateToRemove in candidatesToRemove) {
                        val updated = board.eraseCandidatePattern(cell, candidateToRemove)
                        if (updated) {
                            anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }
}

/**
 * Extension function to generate all combinations of the given size from a list.
 */
private fun <T> List<T>.combinations(size: Int): List<List<T>> {
    if (size == 0) return listOf(emptyList())
    if (size > this.size) return emptyList()

    return if (size == 1) {
        this.map { listOf(it) }
    } else {
        val result = mutableListOf<List<T>>()
        for (i in 0..this.size - size) {
            val first = this[i]
            val rest = this.subList(i + 1, this.size)
            for (combo in rest.combinations(size - 1)) {
                result.add(listOf(first) + combo)
            }
        }
        result
    }
}
