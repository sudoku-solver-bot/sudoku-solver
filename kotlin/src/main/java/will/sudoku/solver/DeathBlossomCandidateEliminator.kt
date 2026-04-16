package will.sudoku.solver

/**
 * Death Blossom Candidate Eliminator
 *
 * A Death Blossom consists of:
 * 1. A **stem** cell with N candidates
 * 2. N **petal** ALS (one per stem candidate), where:
 *    - The petal ALS has the stem's candidate as one of its own candidates
 *    - Every cell in the ALS with that candidate can see the stem
 *    - ALSes don't overlap with each other or the stem
 * 3. A common candidate Z present in ALL petal ALSes but NOT in the stem
 * 4. An outside cell seeing every Z-position in every petal ALS → eliminate Z
 *
 * ## Reasoning
 *
 * For each stem candidate X:
 * - If the stem takes X, the petal ALS for X loses X (stem sees all X-cells in that ALS)
 * - The ALS becomes a locked set (N cells → N candidates)
 * - Since all ALSes contain Z, Z must be placed in each locked ALS
 * - Therefore any outside cell seeing all Z-positions can't be Z
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/Death_Blossom
 */
class DeathBlossomCandidateEliminator : CandidateEliminator {

    private data class ALS(
        val cells: Set<Coord>,
        val candidates: Set<Int>
    )

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Find all ALS in the board
        val allALS = findAllALS(board)
        if (allALS.isEmpty()) return false

        // Try each unconfirmed cell as a potential stem
        for (stem in Coord.all) {
            if (board.isConfirmed(stem)) continue
            val stemCandidates = board.candidateValues(stem).toSet()
            if (stemCandidates.size < 2) continue

            // For each stem candidate, find eligible petal ALS
            val petalsByCandidate = mutableMapOf<Int, List<ALS>>()

            for (x in stemCandidates) {
                val eligibleALS = allALS.filter { als ->
                    // ALS must contain candidate X
                    x in als.candidates &&
                    // ALS must not contain the stem cell
                    stem !in als.cells &&
                    // Every cell in ALS with candidate X must see the stem
                    als.cells.all { cell ->
                        board.candidateValues(cell).contains(x) &&
                        seesEachOther(cell, stem)
                    }
                }
                if (eligibleALS.isNotEmpty()) {
                    petalsByCandidate[x] = eligibleALS
                }
            }

            // Need at least one ALS per stem candidate
            if (petalsByCandidate.size < stemCandidates.size) continue

            // Generate all combinations: one ALS per stem candidate
            val candidateList = stemCandidates.toList()
            val combos = generatePetalCombinations(petalsByCandidate, candidateList)

            for (petals in combos) {
                // Check non-overlap
                val allCells = petals.flatMap { it.cells }
                if (allCells.size != allCells.toSet().size) continue

                // Find common candidates across all petals (excluding stem candidates)
                val commonCandidates = petals
                    .map { it.candidates }
                    .reduce { acc, set -> acc.intersect(set) }
                    .minus(stemCandidates)

                if (commonCandidates.isEmpty()) continue

                // For each common Z, eliminate from outside cells seeing all Z-positions
                for (z in commonCandidates) {
                    val zCellsPerALS = petals.map { als ->
                        als.cells.filter { board.candidateValues(it).contains(z) }
                    }

                    for (coord in Coord.all) {
                        if (coord == stem) continue
                        if (allCells.contains(coord)) continue
                        if (board.isConfirmed(coord)) continue
                        if (!board.candidateValues(coord).contains(z)) continue

                        val seesAllZ = zCellsPerALS.all { zCells ->
                            zCells.all { seesEachOther(coord, it) }
                        }

                        if (seesAllZ) {
                            if (board.eraseCandidateValue(coord, z)) {
                                anyUpdate = true
                            }
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Generate all combinations picking one ALS per stem candidate.
     */
    private fun generatePetalCombinations(
        petalsByCandidate: Map<Int, List<ALS>>,
        candidates: List<Int>
    ): List<List<ALS>> {
        if (candidates.isEmpty()) return listOf(emptyList())

        val head = candidates.first()
        val tail = candidates.drop(1)
        val headALS = petalsByCandidate[head] ?: return emptyList()

        val restCombos = generatePetalCombinations(petalsByCandidate, tail)
        if (restCombos.isEmpty() && tail.isNotEmpty()) return emptyList()

        val result = mutableListOf<List<ALS>>()
        for (als in headALS) {
            if (tail.isEmpty()) {
                result.add(listOf(als))
            } else {
                for (rest in restCombos) {
                    result.add(listOf(als) + rest)
                }
            }
        }
        return result
    }

    private fun findAllALS(board: Board): List<ALS> {
        val result = mutableListOf<ALS>()

        for (row in 0..8) {
            val coords = Coord.all.filter { it.row == row }
            findALSInGroup(board, coords, result)
        }
        for (col in 0..8) {
            val coords = Coord.all.filter { it.col == col }
            findALSInGroup(board, coords, result)
        }
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val coords = Coord.all.filter {
                    it.row / 3 == boxRow && it.col / 3 == boxCol
                }
                findALSInGroup(board, coords, result)
            }
        }

        return result.distinctBy { it.cells.map { c -> c.index }.sorted() }
    }

    private fun findALSInGroup(board: Board, coords: List<Coord>, results: MutableList<ALS>) {
        val unconfirmed = coords.filter { !board.isConfirmed(it) }
        if (unconfirmed.size < 2) return

        for (size in 2..minOf(4, unconfirmed.size)) {
            for (combo in generateCombinations(unconfirmed, size)) {
                val allCandidates = combo.flatMap { board.candidateValues(it).toList() }.toSet()
                if (allCandidates.size == size + 1) {
                    results.add(ALS(combo.toSet(), allCandidates))
                }
            }
        }
    }

    private fun seesEachOther(c1: Coord, c2: Coord): Boolean {
        return c1.row == c2.row ||
                c1.col == c2.col ||
                (c1.row / 3 == c2.row / 3 && c1.col / 3 == c2.col / 3)
    }

    private fun <T> generateCombinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (list.isEmpty() || k > list.size) return emptyList()
        val result = mutableListOf<List<T>>()
        for (i in list.indices) {
            for (rest in generateCombinations(list.drop(i + 1), k - 1)) {
                result.add(listOf(list[i]) + rest)
            }
        }
        return result
    }
}
