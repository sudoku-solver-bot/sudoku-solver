package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Franken Fish Candidate Eliminator
 *
 * Detects Franken Fish patterns and eliminates candidates accordingly.
 *
 * A Franken Fish is an advanced fish pattern that extends X-Wing and Swordfish.
 * It can use N base sets (rows or columns) and N cover sets (columns or rows),
 * where N ranges from 2 to 4.
 *
 * Unlike basic fish patterns, Franken Fish can be more flexible in the
 * arrangement of candidates while still maintaining the constraint that
 * N base sets cover N cover sets.
 *
 * Example (Size 2 Franken Fish - similar to X-Wing):
 * ```
 * Row 1:  X . X | . . . | . . .   (candidate in cols 1, 3)
 * Row 5:  X . X | . . . | . . .   (candidate in cols 1, 3)
 * ```
 * Candidate can be eliminated from columns 1 and 3 in all other rows.
 *
 * ## Algorithm
 * 1. For each candidate value 1-9:
 *    a. Find rows where candidate appears in 2-4 columns
 *    b. Find N rows with same column set (size N, where N is 2-4)
 *    c. Eliminate candidate from those columns in all other rows
 *    d. Repeat for columns → rows
 */
class FrankenFishCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Check Franken Fish in rows (eliminate from columns)
        for (value in 1..9) {
            val changed = eliminateRowFrankenFish(board, value)
            if (changed) anyUpdate = true
        }

        // Check Franken Fish in columns (eliminate from rows)
        for (value in 1..9) {
            val changed = eliminateColumnFrankenFish(board, value)
            if (changed) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Find and eliminate Franken Fish patterns based on rows.
     * Supports fish sizes 2-4.
     */
    private fun eliminateRowFrankenFish(board: Board, value: Int): Boolean {
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
            // Interested in rows with 2-4 candidate positions
            if (columns.size in 2..4) {
                rowToColumns[row] = columns
            }
        }

        // Group rows by their column sets
        val columnSetToRows = mutableMapOf<Set<Int>, MutableList<Int>>()
        for ((row, cols) in rowToColumns) {
            columnSetToRows.getOrPut(cols) { mutableListOf() }.add(row)
        }

        // For each column set, check if we have N rows for N columns
        for ((cols, rows) in columnSetToRows) {
            val n = cols.size
            if (rows.size >= n && n in 2..4) {
                // Take only n rows to form the fish
                val fishRows = rows.take(n)

                // Eliminate from these columns in all other rows
                for (row in 0..8) {
                    if (row in fishRows) continue

                    for (col in cols) {
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
     * Find and eliminate Franken Fish patterns based on columns.
     * Supports fish sizes 2-4.
     */
    private fun eliminateColumnFrankenFish(board: Board, value: Int): Boolean {
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
            // Interested in columns with 2-4 candidate positions
            if (rows.size in 2..4) {
                columnToRows[col] = rows
            }
        }

        // Group columns by their row sets
        val rowSetToColumns = mutableMapOf<Set<Int>, MutableList<Int>>()
        for ((col, rows) in columnToRows) {
            rowSetToColumns.getOrPut(rows) { mutableListOf() }.add(col)
        }

        // For each row set, check if we have N columns for N rows
        for ((rows, cols) in rowSetToColumns) {
            val n = rows.size
            if (cols.size >= n && n in 2..4) {
                // Take only n columns to form the fish
                val fishCols = cols.take(n)

                // Eliminate from these rows in all other columns
                for (col in 0..8) {
                    if (col in fishCols) continue

                    for (row in rows) {
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
}
