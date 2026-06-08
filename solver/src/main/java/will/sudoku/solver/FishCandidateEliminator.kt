package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Generic Fish Candidate Eliminator
 *
 * Unified eliminator for X-Wing (size=2) and Swordfish (size=2-3).
 * Parameterized by [fishSize] to handle both patterns with the same logic.
 *
 * A fish pattern occurs when a candidate value appears in exactly `fishSize` cells
 * in each of `fishSize` rows, and those cells are all in the same set of columns.
 * The candidate can then be eliminated from those columns in all other rows
 * (or vice versa for column-based fish).
 *
 * ## Algorithm
 * 1. For each candidate value 1-9:
 *    a. Find rows where candidate appears in 2-fishSize columns
 *    b. If fishSize rows have candidate in the same column set → fish found
 *    c. Eliminate candidate from those columns in all other rows
 *    d. Repeat for columns → rows
 *
 * @param fishSize Number of rows/columns in the fish pattern (2 = X-Wing, 3 = Swordfish)
 */
class FishCandidateEliminator(private val fishSize: Int) : CandidateEliminator {

    override val displayName: String
        get() = when (fishSize) {
            2 -> "XWing"
            3 -> "Swordfish"
            else -> "Fish($fishSize)"
        }

    init {
        require(fishSize in 2..3) { "Fish size must be 2 (X-Wing) or 3 (Swordfish), got $fishSize" }
    }

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Check fish in rows (eliminate from columns)
        for (value in 1..9) {
            val changed = eliminateRowFish(board, value)
            if (changed) anyUpdate = true
        }

        // Check fish in columns (eliminate from rows)
        for (value in 1..9) {
            val changed = eliminateColumnFish(board, value)
            if (changed) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Find and eliminate fish patterns based on rows.
     */
    private fun eliminateRowFish(board: Board, value: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Build map: row -> set of columns where candidate appears
        val rowToColumns = mutableMapOf<Int, Set<Int>>()

        for (row in 0..8) {
            val columns = mutableSetOf<Int>()
            for (col in 0..8) {
                val coord = Coord(row, col)
                if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                    columns.add(col)
                }
            }
            // Only interested in rows with 2..fishSize candidate positions
            if (columns.size in 2..fishSize) {
                rowToColumns[row] = columns
            }
        }

        // Find groups of fishSize rows with the same column set
        val rows = rowToColumns.keys.toList()
        val combinations = combinations(rows, fishSize)

        for (combination in combinations) {
            val colSets = combination.map { rowToColumns[it]!! }
            if (colSets.all { it == colSets[0] }) {
                // Fish found! Eliminate from these columns in all other rows
                val fishColumns = colSets[0].toList()

                for (row in 0..8) {
                    if (row in combination) continue

                    for (col in fishColumns) {
                        val coord = Coord(row, col)
                        if (!board.isConfirmed(coord)) {
                            val changed = board.eraseCandidateValue(coord, value)
                            if (changed) anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Find and eliminate fish patterns based on columns.
     */
    private fun eliminateColumnFish(board: Board, value: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Build map: column -> set of rows where candidate appears
        val columnToRows = mutableMapOf<Int, Set<Int>>()

        for (col in 0..8) {
            val rows = mutableSetOf<Int>()
            for (row in 0..8) {
                val coord = Coord(row, col)
                if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                    rows.add(row)
                }
            }
            // Only interested in columns with 2..fishSize candidate positions
            if (rows.size in 2..fishSize) {
                columnToRows[col] = rows
            }
        }

        // Find groups of fishSize columns with the same row set
        val cols = columnToRows.keys.toList()
        val combinations = combinations(cols, fishSize)

        for (combination in combinations) {
            val rowSets = combination.map { columnToRows[it]!! }
            if (rowSets.all { it == rowSets[0] }) {
                // Fish found! Eliminate from these rows in all other columns
                val fishRows = rowSets[0].toList()

                for (col in 0..8) {
                    if (col in combination) continue

                    for (row in fishRows) {
                        val coord = Coord(row, col)
                        if (!board.isConfirmed(coord)) {
                            val changed = board.eraseCandidateValue(coord, value)
                            if (changed) anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    companion object {
        /**
         * Generate all combinations of [size] elements from [elements].
         */
        private fun <T> combinations(elements: List<T>, size: Int): List<List<T>> {
            if (size == 0) return listOf(emptyList())
            if (elements.isEmpty()) return emptyList()

            val result = mutableListOf<List<T>>()
            for (i in 0..elements.size - size) {
                val rest = combinations(elements.subList(i + 1, elements.size), size - 1)
                for (combo in rest) {
                    result.add(listOf(elements[i]) + combo)
                }
            }
            return result
        }
    }
}
