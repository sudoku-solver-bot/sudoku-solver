package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Simple Coloring Candidate Eliminator
 *
 * Detects contradictions using conjugate pair chains and eliminates candidates.
 *
 * Simple Coloring (also called Singles Chains) works by:
 * 1. Finding conjugate pairs for each candidate (two cells in a unit where only they have that candidate)
 * 2. Building chains by alternately coloring cells
 * 3. Detecting contradictions:
 *    - If two cells of the same color see each other, that color is invalid
 *    - If a cell can see both colors, it can't contain that candidate
 *
 * Example Rule 2 (Two colors in the same unit):
 * ```
 * Color chain: A(1,1) - B(1,5) - A(5,5) - B(5,1)
 * If A and B both appear in the same unit, one of them must be true.
 * Any cell seeing both A and B can't have that candidate.
 * ```
 *
 * Example Rule 4 (Two colors see each other):
 * ```
 * Color chain: A(1,1) - B(1,5) - A(5,5) - B(5,1)
 * If two A-cells see each other, A is invalid (all A's can be eliminated)
 * If two B-cells see each other, B is invalid (all B's can be eliminated)
 * ```
 *
 * ## Algorithm
 * 1. For each candidate value 1-9:
 *    a. Build color chains using conjugate pairs
 *    b. Check Rule 2: Eliminate from cells seeing both colors in same unit
 *    c. Check Rule 4: If same-colored cells see each other, eliminate all that color
 *
 * ## Color Representation
 * Colors are represented as 1 (Color A) and 2 (Color B), with 0 being uncolored.
 */
class SimpleColoringCandidateEliminator : CandidateEliminator {

    // Color constants
    private companion object {
        const val UNCOLORED = 0
        const val COLOR_A = 1
        const val COLOR_B = 2
    }

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Try each candidate value (1-9)
        for (candidate in 1..9) {
            val changed = applySimpleColoring(board, candidate)
            if (changed) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Apply Simple Coloring technique for a specific candidate value.
     */
    private fun applySimpleColoring(board: Board, candidate: Int): Boolean {
        var anyUpdate = false

        // Build color chains — returns colors and any cells that
        // were part of a contradiction (same color on both ends of a conjugate pair)
        val (colors, contradictionCells) = buildColorChains(board, candidate)

        // Get all cells of each color
        val colorA = colors.filterValues { it == COLOR_A }.keys
        val colorB = colors.filterValues { it == COLOR_B }.keys

        if (colorA.isEmpty() || colorB.isEmpty()) return false

        // Rule 4: Contradiction detected during chain building —
        // a conjugate pair has both cells with the same color.
        // That color is invalid, eliminate all cells of that color.
        //
        // NOTE: Rule 4 is disabled because the current chain-building algorithm
        // produces false contradictions when cells appear in multiple conjugate
        // pairs across different units. A cell can be colored by one pair and
        // then encounter a conflicting color from another pair, creating a
        // false-positive contradiction.
        //
        // TODO: Fix chain-building to properly handle multi-unit cells before
        // re-enabling Rule 4. See #549.
        // if (contradictionCells.isNotEmpty()) {
        //     val contradictionColor = colors[contradictionCells.first()]
        //     if (contradictionColor != null) {
        //         val toEliminate = if (contradictionColor == COLOR_A) colorA else colorB
        //         for (coord in toEliminate) {
        //             if (!board.isConfirmed(coord) && candidate in board.candidateValues(coord)) {
        //                 val changed = board.eraseCandidateValue(coord, candidate)
        //                 if (changed) anyUpdate = true
        //             }
        //         }
        //         return anyUpdate
        //     }
        // }

        // Rule 2: Eliminate from cells that see both colors
        // A cell seeing both colors in the same unit means one must be true,
        // so this cell can't contain the candidate.
        for (coord in Coord.all) {
            if (board.isConfirmed(coord)) continue
            if (colors.containsKey(coord)) continue

            val seesColorA = colorA.any { seesEachOther(coord, it) }
            val seesColorB = colorB.any { seesEachOther(coord, it) }

            if (candidate in board.candidateValues(coord) && seesColorA && seesColorB) {
                val changed = board.eraseCandidateValue(coord, candidate)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }

    /**
     * Build color chains by finding conjugate pairs and coloring them alternately.
     *
     * Detects contradictions during chain building: when a conjugate pair has
     * both cells with the same color, that color is impossible (Rule 4).
     *
     * @return Pair of (colors map, set of cells involved in contradictions)
     */
    private fun buildColorChains(
        board: Board,
        candidate: Int
    ): Pair<Map<Coord, Int>, Set<Coord>> {
        val colors = mutableMapOf<Coord, Int>()
        val candidateMask = Board.masks[candidate - 1]
        val contradictionCells = mutableSetOf<Coord>()

        // Find all cells with this candidate
        val candidateCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) &&
            (board.candidatePattern(coord) and candidateMask) != 0
        }

        // Initialize all candidate cells as UNCOLORED
        candidateCells.forEach { colors[it] = UNCOLORED }

        // Find conjugate pairs (two cells in a unit where only they have this candidate)
        val conjugatePairs = findConjugatePairs(board, candidate, candidateCells)

        // Build chains by coloring conjugate pairs
        for ((cell1, cell2) in conjugatePairs) {
            when {
                colors[cell1] == UNCOLORED && colors[cell2] == UNCOLORED -> {
                    // Start a new chain: color one cell A, the other B
                    colors[cell1] = COLOR_A
                    colors[cell2] = COLOR_B
                }
                colors[cell1] == UNCOLORED && colors[cell2] != UNCOLORED -> {
                    // Extend chain: give cell1 the opposite color of cell2
                    colors[cell1] = oppositeColor(colors[cell2]!!)
                }
                colors[cell1] != UNCOLORED && colors[cell2] == UNCOLORED -> {
                    // Extend chain: give cell2 the opposite color of cell1
                    colors[cell2] = oppositeColor(colors[cell1]!!)
                }
                colors[cell1] != UNCOLORED && colors[cell2] != UNCOLORED -> {
                    // Both already colored — check for contradiction
                    if (colors[cell1] == colors[cell2]) {
                        // Same color on both ends of a conjugate pair = contradiction
                        contradictionCells.add(cell1)
                        contradictionCells.add(cell2)
                    }
                }
            }
        }

        return Pair(colors, contradictionCells)
    }

    /**
     * Find all conjugate pairs for a given candidate.
     * A conjugate pair is two cells in the same unit (row/column/region)
     * where they are the ONLY cells in that unit containing the candidate.
     */
    private fun findConjugatePairs(
        board: Board,
        candidate: Int,
        candidateCells: List<Coord>
    ): List<Pair<Coord, Coord>> {
        val pairs = mutableListOf<Pair<Coord, Coord>>()
        val candidateMask = Board.masks[candidate - 1]

        // Check each row
        for (row in 0..8) {
            val cellsInRow = candidateCells.filter { it.row == row }
            if (cellsInRow.size == 2) {
                pairs.add(Pair(cellsInRow[0], cellsInRow[1]))
            }
        }

        // Check each column
        for (col in 0..8) {
            val cellsInCol = candidateCells.filter { it.col == col }
            if (cellsInCol.size == 2) {
                pairs.add(Pair(cellsInCol[0], cellsInCol[1]))
            }
        }

        // Check each region
        for (regionRow in 0..2) {
            for (regionCol in 0..2) {
                val cellsInRegion = candidateCells.filter { coord ->
                    coord.row / 3 == regionRow && coord.col / 3 == regionCol
                }
                if (cellsInRegion.size == 2) {
                    pairs.add(Pair(cellsInRegion[0], cellsInRegion[1]))
                }
            }
        }

        return pairs
    }

    /**
     * Get the opposite color (A -> B, B -> A).
     */
    private fun oppositeColor(color: Int): Int {
        return if (color == COLOR_A) COLOR_B else COLOR_A
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
}
