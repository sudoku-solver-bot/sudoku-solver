package will.sudoku.solver

/**
 * Death Blossom Candidate Eliminator
 *
 * Most advanced Sudoku elimination technique combining:
 * - Almost Locked Sets (ALS)
 * - Strong Links
 * - Forcing Chains
 *
 * ## Pattern Structure
 *
 * A Death Blossom consists of:
 * 1. **Stem Cell**: Unconfirmed cell with 2+ candidates
 * 2. **ALS Groups**: Multiple ALS connected to the stem
 * 3. **Blossom Cells**: Cells visible to ALL ALS that can have candidates eliminated
 *
 * ## How It Works
 *
 * 1. Find a stem cell with multiple candidates
 * 2. Find ALS that share different candidates with the stem
 * 3. If all ALS contain a common candidate (the "target")
 * 4. Any cell visible to ALL ALS can have that target eliminated
 *
 * The reasoning: The stem must take one of its candidates. If it takes candidate X,
 * then the ALS connected via X loses X, forcing it to use its other candidates.
 * If all ALS contain candidate Z, then Z can never be used in cells that see
 * all the ALS, regardless of which candidate the stem takes.
 *
 * ## Example
 *
 * Stem at r1c1 with candidates {1,2,3}
 * - ALS1 contains {1,4,5} connected via candidate 1
 * - ALS2 contains {2,4,5} connected via candidate 2
 * - ALS3 contains {3,4,5} connected via candidate 3
 *
 * All three ALS contain candidates 4 and 5. Any cell that sees ALL three ALS
 * can have 4 and 5 eliminated.
 *
 * ## Algorithm
 * 1. Find all ALS in the board (2-4 cells with 3-5 candidates)
 * 2. For each candidate value 1-9:
 *    a. Find stem cells containing that value
 *    b. Find ALS connected to each stem
 *    c. Check if 2+ ALS all contain the target value
 *    d. Find blossom cells and eliminate target from them
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/Death_Blossom
 */
class DeathBlossomCandidateEliminator : CandidateEliminator {

    data class ALS(
        val cells: Set<Coord>,
        val candidates: Set<Int>
    )

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Step 1: Find all Almost Locked Sets
        val alsSets = findAllALS(board)

        // Step 2: Try each candidate value as potential target
        for (targetValue in 1..9) {

            // Step 3: Find stem cells with this target candidate
            val stemCells = findStemCells(board, targetValue)

            // Step 4: For each stem cell
            for (stem in stemCells) {

                // Step 5: Find ALS connected to stem
                val connectedALS = findConnectedALS(alsSets, stem, targetValue, board)

                // Step 6: Need at least 2 ALS for Death Blossom
                if (connectedALS.size < 2) continue

                // Step 7: Try combinations of 2+ ALS
                for (alsGroupSize in 2..connectedALS.size) {
                    val combinations = generateCombinations(connectedALS, alsGroupSize)

                    for (alsGroup in combinations) {

                        // Step 8: Check if all ALS contain targetValue
                        if (allContainTarget(alsGroup, targetValue)) {

                            // Step 9: Find blossom cells (visible to ALL ALS)
                            val blossomCells = findBlossomCells(alsGroup, targetValue, board, stem)

                            // Step 10: Eliminate target from blossom cells
                            for (cell in blossomCells) {
                                if (board.eraseCandidateValue(cell, targetValue)) {
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

    /**
     * Find all Almost Locked Sets in the board.
     * An ALS is a group of N cells with exactly N+1 candidates.
     */
    private fun findAllALS(board: Board): List<ALS> {
        val result = mutableListOf<ALS>()

        // Find ALS in rows
        for (row in 0 until 9) {
            val rowCoords = Coord.all.filter { it.row == row }
            findALSInGroup(board, rowCoords, result)
        }

        // Find ALS in columns
        for (col in 0 until 9) {
            val colCoords = Coord.all.filter { it.col == col }
            findALSInGroup(board, colCoords, result)
        }

        // Find ALS in boxes
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val boxCoords = Coord.all.filter {
                    it.row / 3 == boxRow && it.col / 3 == boxCol
                }
                findALSInGroup(board, boxCoords, result)
            }
        }

        return result
    }

    /**
     * Find ALS in a specific group (row/column/box).
     * Limits to 2-4 cells to avoid combinatorial explosion.
     */
    private fun findALSInGroup(board: Board, coords: List<Coord>, results: MutableList<ALS>) {
        // Get unconfirmed cells in this group
        val unconfirmed = coords.filter { !board.isConfirmed(it) }

        if (unconfirmed.size < 2) return

        // Try combinations of 2-4 cells (ALS of size 2-4)
        for (size in 2 until minOf(5, unconfirmed.size)) {
            val combinations = generateCombinations(unconfirmed, size)

            for (combination in combinations) {
                val allCandidates = combination
                    .flatMap { board.candidateValues(it).toList() }
                    .distinct()
                    .toSet()

                // ALS condition: n cells have exactly n+1 candidates
                if (allCandidates.size == size + 1) {
                    results.add(
                        ALS(
                            cells = combination.toSet(),
                            candidates = allCandidates
                        )
                    )
                }
            }
        }
    }

    /**
     * Find stem cells that contain the target value.
     * Stem cells must have 2+ candidates.
     */
    private fun findStemCells(board: Board, targetValue: Int): List<Coord> {
        return Coord.all.filter { coord ->
            !board.isConfirmed(coord) &&
            board.candidateValues(coord).contains(targetValue) &&
            board.candidateValues(coord).size >= 2
        }
    }

    /**
     * Find ALS that are connected to the stem cell.
     * An ALS is connected if:
     * 1. It contains the target value
     * 2. It shares at least one candidate with the stem
     * 3. At least one cell in the ALS is visible to the stem
     */
    private fun findConnectedALS(
        alsSets: List<ALS>,
        stem: Coord,
        targetValue: Int,
        board: Board
    ): List<ALS> {
        val stemCandidates = board.candidateValues(stem).toSet()

        return alsSets.filter { als ->
            // Must contain target value
            als.candidates.contains(targetValue)

            // Must share at least one candidate with stem (excluding target)
            val sharedCandidates = als.candidates.intersect(stemCandidates) - targetValue
            sharedCandidates.isNotEmpty()

            // At least one cell in ALS must be visible to stem
            val visibleToStem = als.cells.any { alsCell ->
                seesEachOther(stem, alsCell)
            }
            visibleToStem
        }
    }

    /**
     * Check if all ALS in the group contain the target value.
     */
    private fun allContainTarget(alsGroup: List<ALS>, targetValue: Int): Boolean {
        return alsGroup.all { it.candidates.contains(targetValue) }
    }

    /**
     * Find blossom cells that are visible to ALL ALS in the group.
     * Blossom cells must:
     * 1. Be visible to every cell in every ALS
     * 2. Not be part of any ALS or the stem
     * 3. Be able to contain the target value
     */
    private fun findBlossomCells(
        alsGroup: List<ALS>,
        targetValue: Int,
        board: Board,
        stem: Coord
    ): List<Coord> {
        val result = mutableListOf<Coord>()

        // Collect all cells in all ALS
        val allALSCells = alsGroup.flatMap { it.cells }.toSet()

        for (coord in Coord.all) {
            // Skip if coord is in any ALS or is the stem
            if (coord in allALSCells || coord == stem) continue

            // Skip if confirmed or can't contain target
            if (board.isConfirmed(coord)) continue
            if (!board.candidateValues(coord).contains(targetValue)) continue

            // Check if visible to ALL cells in ALL ALS
            val seesAll = allALSCells.all { alsCell ->
                seesEachOther(coord, alsCell)
            }

            if (seesAll) {
                result.add(coord)
            }
        }

        return result
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
     * Generate all combinations of size k from a list.
     */
    private fun <T> generateCombinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(listOf())
        if (list.isEmpty() || k > list.size) return emptyList()

        val result = mutableListOf<List<T>>()
        for (i in list.indices) {
            val head = list[i]
            val remainingCombinations = generateCombinations(list.drop(i + 1), k - 1)
            for (combo in remainingCombinations) {
                result.add(listOf(head) + combo)
            }
        }
        return result
    }
}
