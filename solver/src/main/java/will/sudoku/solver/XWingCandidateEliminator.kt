package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * X-Wing Candidate Eliminator
 *
 * Detects X-Wing patterns and eliminates candidates accordingly.
 *
 * An X-Wing occurs when a candidate value appears in exactly 2 cells in each of 2 rows,
 * and those cells are also in the same 2 columns (or vice versa for columns → rows).
 *
 * When this pattern is found, the candidate can be eliminated from all other cells
 * in those 2 columns (or rows).
 *
 * Example (Row X-Wing):
 * ```
 * Row 2:  X . . | . . . | . X .   (candidate in cols 1, 7)
 * Row 5:  X . . | . . . | . X .   (candidate in cols 1, 7)
 * ```
 * Candidate can be eliminated from columns 1 and 7 in all other rows (not 2 or 5).
 *
 * ## Algorithm
 * 1. For each candidate value 1-9:
 *    a. Find rows where candidate appears in exactly 2 columns
 *    b. If 2 rows have candidate in same 2 columns → X-Wing found
 *    c. Eliminate candidate from those columns in all other rows
 *    d. Repeat for columns → rows
 */
class XWingCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Check X-Wing in rows (eliminate from columns)
        for (value in 1..9) {
            val changed = eliminateRowXWing(board, value)
            if (changed) anyUpdate = true
        }

        // Check X-Wing in columns (eliminate from rows)
        for (value in 1..9) {
            val changed = eliminateColumnXWing(board, value)
            if (changed) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Find and eliminate X-Wing patterns based on rows.
     *
     * When a candidate appears in exactly 2 cells in each of 2 rows,
     * and those cells are in the same 2 columns, eliminate the candidate
     * from those 2 columns in all other rows.
     */
    private fun eliminateRowXWing(board: Board, value: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Build map: row -> set of columns where candidate appears
        val rowToColumns = mutableMapOf<Int, Set<Int>>()

        for (row in 0..8) {
            val columns = mutableSetOf<Int>()
            for (col in 0..8) {
                val coord = Coord(row, col)
                // Check if this cell has the candidate (and is not confirmed)
                if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                    columns.add(col)
                }
            }
            // Only interested in rows with exactly 2 candidate positions
            if (columns.size == 2) {
                rowToColumns[row] = columns
            }
        }

        // Find pairs of rows with same column set
        val processedPairs = mutableSetOf<Pair<Int, Int>>()

        for ((row1, cols1) in rowToColumns) {
            for ((row2, cols2) in rowToColumns) {
                if (row1 >= row2) continue
                if (cols1 != cols2) continue

                val pairKey = Pair(row1, row2)
                if (pairKey in processedPairs) continue
                processedPairs.add(pairKey)

                // X-Wing found! Eliminate from these columns in all other rows
                val xWingColumns = cols1.toList()

                for (row in 0..8) {
                    if (row == row1 || row == row2) continue

                    for (col in xWingColumns) {
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
     * Find and eliminate X-Wing patterns based on columns.
     *
     * When a candidate appears in exactly 2 cells in each of 2 columns,
     * and those cells are in the same 2 rows, eliminate the candidate
     * from those 2 rows in all other columns.
     */
    private fun eliminateColumnXWing(board: Board, value: Int): Boolean {
        val mask = masks[value - 1]
        var anyUpdate = false

        // Build map: column -> set of rows where candidate appears
        val columnToRows = mutableMapOf<Int, Set<Int>>()

        for (col in 0..8) {
            val rows = mutableSetOf<Int>()
            for (row in 0..8) {
                val coord = Coord(row, col)
                // Check if this cell has the candidate (and is not confirmed)
                if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                    rows.add(row)
                }
            }
            // Only interested in columns with exactly 2 candidate positions
            if (rows.size == 2) {
                columnToRows[col] = rows
            }
        }

        // Find pairs of columns with same row set
        val processedPairs = mutableSetOf<Pair<Int, Int>>()

        for ((col1, rows1) in columnToRows) {
            for ((col2, rows2) in columnToRows) {
                if (col1 >= col2) continue
                if (rows1 != rows2) continue

                val pairKey = Pair(col1, col2)
                if (pairKey in processedPairs) continue
                processedPairs.add(pairKey)

                // X-Wing found! Eliminate from these rows in all other columns
                val xWingRows = rows1.toList()

                for (col in 0..8) {
                    if (col == col1 || col == col2) continue

                    for (row in xWingRows) {
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
