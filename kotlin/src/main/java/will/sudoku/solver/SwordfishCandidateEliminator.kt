package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Swordfish Candidate Eliminator
 *
 * Detects Swordfish patterns and eliminates candidates accordingly.
 *
 * A Swordfish is an extension of X-Wing to 3 rows/columns. When a candidate value
 * appears in exactly 3 cells in each of 3 rows, and those cells are in the same 3 columns,
 * the candidate can be eliminated from those columns in all other rows (or vice versa).
 *
 * Example (Row Swordfish):
 * ```
 * Row 1:  X . X | . . . | . X .   (candidate in cols 1, 3, 7)
 * Row 4:  X . X | . . . | . X .   (candidate in cols 1, 3, 7)
 * Row 7:  X . X | . . . | . X .   (candidate in cols 1, 3, 7)
 * ```
 * Candidate can be eliminated from columns 1, 3, and 7 in all other rows.
 *
 * ## Algorithm
 * 1. For each candidate value 1-9:
 *    a. Find rows where candidate appears in exactly 2-3 columns
 *    b. If 3 rows have candidate in same set of columns (size 2-3) → Swordfish found
 *    c. Eliminate candidate from those columns in all other rows
 *    d. Repeat for columns → rows
 */
class SwordfishCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Check Swordfish in rows (eliminate from columns)
        for (value in 1..9) {
            val changed = eliminateRowSwordfish(board, value)
            if (changed) anyUpdate = true
        }

        // Check Swordfish in columns (eliminate from rows)
        for (value in 1..9) {
            val changed = eliminateColumnSwordfish(board, value)
            if (changed) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Find and eliminate Swordfish patterns based on rows.
     */
    private fun eliminateRowSwordfish(board: Board, value: Int): Boolean {
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
            // Only interested in rows with 2-3 candidate positions
            if (columns.size in 2..3) {
                rowToColumns[row] = columns
            }
        }

        // Find triplets of rows with same column set (size 2-3)
        val rows = rowToColumns.keys.toList()
        for (i in 0 until rows.size - 2) {
            for (j in i + 1 until rows.size - 1) {
                for (k in j + 1 until rows.size) {
                    val row1 = rows[i]
                    val row2 = rows[j]
                    val row3 = rows[k]

                    val cols1 = rowToColumns[row1]!!
                    val cols2 = rowToColumns[row2]!!
                    val cols3 = rowToColumns[row3]!!

                    // Check if all three have the same columns
                    if (cols1 == cols2 && cols2 == cols3) {
                        // Swordfish found! Eliminate from these columns in all other rows
                        val swordfishColumns = cols1.toList()

                        for (row in 0..8) {
                            if (row == row1 || row == row2 || row == row3) continue

                            for (col in swordfishColumns) {
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord)) {
                                    val changed = board.eraseCandidateValue(coord, value)
                                    if (changed) anyUpdate = true
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
     * Find and eliminate Swordfish patterns based on columns.
     */
    private fun eliminateColumnSwordfish(board: Board, value: Int): Boolean {
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
            // Only interested in columns with 2-3 candidate positions
            if (rows.size in 2..3) {
                columnToRows[col] = rows
            }
        }

        // Find triplets of columns with same row set (size 2-3)
        val cols = columnToRows.keys.toList()
        for (i in 0 until cols.size - 2) {
            for (j in i + 1 until cols.size - 1) {
                for (k in j + 1 until cols.size) {
                    val col1 = cols[i]
                    val col2 = cols[j]
                    val col3 = cols[k]

                    val rows1 = columnToRows[col1]!!
                    val rows2 = columnToRows[col2]!!
                    val rows3 = columnToRows[col3]!!

                    // Check if all three have the same rows
                    if (rows1 == rows2 && rows2 == rows3) {
                        // Swordfish found! Eliminate from these rows in all other columns
                        val swordfishRows = rows1.toList()

                        for (col in 0..8) {
                            if (col == col1 || col == col2 || col == col3) continue

                            for (row in swordfishRows) {
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord)) {
                                    val changed = board.eraseCandidateValue(coord, value)
                                    if (changed) anyUpdate = true
                                }
                            }
                        }
                    }
                }
            }
        }

        return anyUpdate
    }
}
