package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Mutant Fish Candidate Eliminator
 *
 * Detects Mutant Fish patterns and eliminates candidates accordingly.
 *
 * A Mutant Fish is the most advanced fish pattern, extending Franken Fish
 * by allowing MIXED base sets (rows AND columns) and MIXED cover sets
 * (columns AND rows). This makes it significantly more powerful than
 * Franken Fish, X-Wing, Swordfish, etc.
 *
 * ## Key Difference from Franken Fish
 * - Franken Fish: Base sets are all rows OR all columns
 * - Mutant Fish: Base sets can be MIXED (rows + columns)
 * - Mutant Fish: Cover sets can be MIXED (columns + rows)
 *
 * Example (Size 2 Mutant Fish):
 * ```
 * Base: Row 1 + Column 5
 * Cover: Column 3 + Row 7
 * Constraint: All candidate positions must be at intersections
 * ```
 *
 * ## Algorithm
 * 1. For each candidate value 1-9 and each fish size (2-4):
 *    a. Enumerate all combinations of N base sets (mixed rows/cols)
 *    b. For each base set combination, find candidate positions
 *    c. Try to find N cover sets (mixed cols/rows) covering those positions
 *    d. If found, eliminate from cover sets in non-base positions
 *
 * This is computationally expensive but finds patterns other eliminators miss.
 */
class MutantFishCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        for (value in 1..9) {
            for (size in 2..4) {
                val changed = eliminateMutantFish(board, value, size)
                if (changed) anyUpdate = true
            }
        }

        return anyUpdate
    }

    /**
     * Find and eliminate Mutant Fish patterns of a given size.
     *
     * Mutant Fish can use mixed base sets (rows + columns) and
     * mixed cover sets (columns + rows).
     */
    private fun eliminateMutantFish(board: Board, value: Int, size: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Collect all candidate positions for this value
        val candidatePositions = mutableListOf<Coord>()
        for (row in 0..8) {
            for (col in 0..8) {
                val coord = Coord(row, col)
                if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                    candidatePositions.add(coord)
                }
            }
        }

        if (candidatePositions.isEmpty()) return false

        // Get all rows and columns that contain this candidate
        val rowsWithCandidate = candidatePositions.map { it.row }.distinct().sorted()
        val colsWithCandidate = candidatePositions.map { it.col }.distinct().sorted()

        // Enumerate combinations of base sets (mixed rows and columns)
        // We try different splits: k rows + (size-k) columns
        for (k in 0..size) {
            val numRows = k
            val numCols = size - k

            if (numRows > rowsWithCandidate.size || numCols > colsWithCandidate.size) continue
            if (numRows == 0 && numCols == 0) continue

            // Generate combinations of rows
            val rowCombinations = if (numRows > 0) {
                combinations(rowsWithCandidate, numRows)
            } else {
                listOf(emptyList())
            }

            // Generate combinations of columns
            val colCombinations = if (numCols > 0) {
                combinations(colsWithCandidate, numCols)
            } else {
                listOf(emptyList())
            }

            // Try each combination
            for (baseRows in rowCombinations) {
                for (baseCols in colCombinations) {
                    val result = findAndEliminate(board, value, baseRows, baseCols, candidatePositions)
                    if (result) anyUpdate = true
                }
            }
        }

        return anyUpdate
    }

    /**
     * Given a set of base rows and columns, find cover sets and eliminate.
     */
    private fun findAndEliminate(
        board: Board,
        value: Int,
        baseRows: List<Int>,
        baseCols: List<Int>,
        candidatePositions: List<Coord>
    ): Boolean {
        val size = baseRows.size + baseCols.size
        var anyUpdate = false

        // Find all candidate positions in base sets
        val basePositions = candidatePositions.filter { coord ->
            coord.row in baseRows || coord.col in baseCols
        }

        if (basePositions.isEmpty()) return false

        // Extract cover sets (columns from row positions, rows from column positions)
        val coverColsFromRows = basePositions.filter { it.row in baseRows }.map { it.col }.distinct().sorted()
        val coverRowsFromCols = basePositions.filter { it.col in baseCols }.map { it.row }.distinct().sorted()

        val totalCoverSize = coverColsFromRows.size + coverRowsFromCols.size

        // Check if we have exactly N cover sets for N base sets
        if (totalCoverSize == size) {
            // Mutant Fish found! Eliminate from cover sets in non-base positions

            // Eliminate from cover columns in rows not in base
            for (col in coverColsFromRows) {
                for (row in 0..8) {
                    if (row in baseRows) continue
                    if (row in coverRowsFromCols) continue  // Skip if it's a cover row from column

                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord)) {
                        val changed = board.eraseCandidateValue(coord, value)
                        if (changed) anyUpdate = true
                    }
                }
            }

            // Eliminate from cover rows in columns not in base
            for (row in coverRowsFromCols) {
                for (col in 0..8) {
                    if (col in baseCols) continue
                    if (col in coverColsFromRows) continue  // Skip if it's a cover col from row

                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord)) {
                        val changed = board.eraseCandidateValue(coord, value)
                        if (changed) anyUpdate = true
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Generate all k-combinations from a list.
     */
    private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (k > list.size) return emptyList()
        if (k == list.size) return listOf(list)
        if (k == 1) return list.map { listOf(it) }

        val result = mutableListOf<List<T>>()

        fun combine(start: Int, current: List<T>) {
            if (current.size == k) {
                result.add(current)
                return
            }

            for (i in start until list.size) {
                combine(i + 1, current + list[i])
            }
        }

        combine(0, emptyList())
        return result
    }
}
