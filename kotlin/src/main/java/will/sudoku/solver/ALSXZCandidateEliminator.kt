package will.sudoku.solver

/**
 * ALS-XZ (Almost Locked Sets XZ) Candidate Eliminator
 *
 * Detects Almost Locked Set patterns and eliminates candidates using
 * the Restricted Common (X) and non-restricted common (Z) candidates.
 *
 * ## Almost Locked Set (ALS)
 *
 * An Almost Locked Set is a group of n cells that contain exactly n+1
 * unique candidates. For example:
 * - 2 cells with 3 unique candidates
 * - 3 cells with 4 unique candidates
 * - 4 cells with 5 unique candidates
 *
 * ## ALS-XZ Pattern
 *
 * When two ALSs share a "Restricted Common" (X):
 *
 * A candidate X is restricted between ALS1 and ALS2 if **every cell in ALS1
 * that has X can see every cell in ALS2 that has X** (and vice versa).
 * This means X must be placed in one ALS or the other, but not both.
 *
 * ### Type 1 Elimination
 * If both ALSs share another candidate Z (non-restricted common):
 * - Any cell that sees ALL cells with Z in both ALSs can have Z eliminated
 *
 * This works because: since X is restricted, one ALS will "lose" X and become
 * a locked set, meaning Z must appear in that ALS. If a cell sees all Z positions
 * in both ALSs, then regardless of which ALS locks, that cell cannot be Z.
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/ALS-XZ
 */
class ALSXZCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all ALSs in each house
        val allALS = mutableListOf<ALS>()

        for (row in 0 until 9) {
            val rowCoords = Coord.all.filter { it.row == row }
            findALSInGroup(board, rowCoords, allALS)
        }

        for (col in 0 until 9) {
            val colCoords = Coord.all.filter { it.col == col }
            findALSInGroup(board, colCoords, allALS)
        }

        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val boxCoords = Coord.all.filter {
                    it.row / 3 == boxRow && it.col / 3 == boxCol
                }
                findALSInGroup(board, boxCoords, allALS)
            }
        }

        // Deduplicate ALSs (same set of coords found in different houses)
        val uniqueALS = allALS.distinctBy { it.coords.map { c -> c.index }.sorted() }

        // Check all pairs of ALSs for XZ patterns
        for (i in uniqueALS.indices) {
            for (j in (i + 1) until uniqueALS.size) {
                val als1 = uniqueALS[i]
                val als2 = uniqueALS[j]

                // Skip overlapping ALSs (sharing a cell)
                if (als1.coords.any { it in als2.coordsSet }) continue

                // Find all restricted commons between these two ALSs
                val restrictedCommons = findRestrictedCommons(board, als1, als2)
                if (restrictedCommons.isEmpty()) continue

                // Find non-restricted commons (candidates shared but not restricted)
                val sharedCandidates = als1.candidates.intersect(als2.candidates)
                val nonRestrictedCommons = sharedCandidates - restrictedCommons.toSet()

                for (z in nonRestrictedCommons) {
                    val result = applyType1Elimination(board, als1, als2, z)
                    if (result) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find all ALSs in a group of coordinates (house).
     */
    private fun findALSInGroup(
        board: Board,
        coords: List<Coord>,
        results: MutableList<ALS>
    ) {
        val unconfirmed = coords.filter { !board.isConfirmed(it) }
        if (unconfirmed.size < 2) return

        for (size in 2..minOf(5, unconfirmed.size)) {
            findALSOfSpecificSize(board, unconfirmed, size, results)
        }
    }

    /**
     * Find ALSs of a specific size in a group.
     * An ALS of size n must have exactly n+1 unique candidates.
     */
    private fun findALSOfSpecificSize(
        board: Board,
        coords: List<Coord>,
        size: Int,
        results: MutableList<ALS>
    ) {
        val combinations = generateCombinations(coords, size)

        for (combination in combinations) {
            val allCandidates = combination
                .flatMap { board.candidateValues(it).toList() }
                .toSet()

            if (allCandidates.size == size + 1) {
                results.add(ALS(combination, allCandidates))
            }
        }
    }

    /**
     * Find all restricted common candidates between two ALSs.
     *
     * A candidate X is restricted between ALS1 and ALS2 if:
     * - X appears in both ALSs
     * - Every cell with X in ALS1 can see every cell with X in ALS2
     *
     * This means X can only appear in one of the two ALSs, making it
     * a mutually exclusive choice.
     */
    private fun findRestrictedCommons(
        board: Board,
        als1: ALS,
        als2: ALS,
    ): List<Int> {
        val sharedCandidates = als1.candidates.intersect(als2.candidates)
        val restricted = mutableListOf<Int>()

        for (x in sharedCandidates) {
            val cellsWithX1 = als1.coords.filter { board.candidateValues(it).contains(x) }
            val cellsWithX2 = als2.coords.filter { board.candidateValues(it).contains(x) }

            if (cellsWithX1.isEmpty() || cellsWithX2.isEmpty()) continue

            // Check: every cell with X in ALS1 must see every cell with X in ALS2
            val allSee = cellsWithX1.all { c1 ->
                cellsWithX2.all { c2 -> seesEachOther(c1, c2) }
            }

            if (allSee) {
                restricted.add(x)
            }
        }

        return restricted
    }

    /**
     * Apply Type 1 elimination for ALS-XZ.
     *
     * If both ALSs contain candidate Z (non-restricted common) and share
     * a restricted common X, eliminate Z from any cell that sees ALL cells
     * with Z in both ALSs.
     */
    private fun applyType1Elimination(
        board: Board,
        als1: ALS,
        als2: ALS,
        z: Int
    ): Boolean {
        var anyUpdate = false

        val cellsWithZ1 = als1.coords.filter { board.candidateValues(it).contains(z) }
        val cellsWithZ2 = als2.coords.filter { board.candidateValues(it).contains(z) }

        if (cellsWithZ1.isEmpty() || cellsWithZ2.isEmpty()) return false

        for (coord in Coord.all) {
            if (coord in als1.coordsSet || coord in als2.coordsSet) continue
            if (board.isConfirmed(coord)) continue
            if (!board.candidateValues(coord).contains(z)) continue

            val seesAllZ = cellsWithZ1.all { seesEachOther(coord, it) } &&
                    cellsWithZ2.all { seesEachOther(coord, it) }

            if (seesAllZ) {
                val erased = board.eraseCandidateValue(coord, z)
                if (erased) anyUpdate = true
            }
        }

        return anyUpdate
    }

    private fun seesEachOther(c1: Coord, c2: Coord): Boolean {
        return c1.row == c2.row ||
                c1.col == c2.col ||
                sameRegion(c1, c2)
    }

    private fun sameRegion(c1: Coord, c2: Coord): Boolean {
        return c1.row / 3 == c2.row / 3 && c1.col / 3 == c2.col / 3
    }

    private fun <T> generateCombinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (list.isEmpty() || k > list.size) return emptyList()

        val result = mutableListOf<List<T>>()
        for (i in list.indices) {
            val head = list[i]
            for (combo in generateCombinations(list.drop(i + 1), k - 1)) {
                result.add(listOf(head) + combo)
            }
        }
        return result
    }

    /**
     * Represents an Almost Locked Set: n cells with exactly n+1 candidates.
     */
    private class ALS(
        val coords: List<Coord>,
        val candidates: Set<Int>
    ) {
        val coordsSet: Set<Coord> = coords.toSet()
    }
}
