package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects Swordfish patterns — extension of X-Wing to 3 rows/columns.
 */
class SwordfishDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.SWORDFISH

    override fun detect(board: Board): HintGenerator.Hint? {
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            // Row-based Swordfish
            val rowToColumns = mutableMapOf<Int, Set<Int>>()
            for (row in 0..8) {
                val columns = mutableSetOf<Int>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        columns.add(col)
                    }
                }
                if (columns.size in 2..3) {
                    rowToColumns[row] = columns
                }
            }

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

                        if (cols1 == cols2 && cols2 == cols3) {
                            val swordfishCols = cols1.toList()
                            return HintGenerator.Hint(
                                coord = Coord(row1, swordfishCols[0]),
                                value = value,
                                technique = technique,
                                explanation = "Swordfish found! Value $value in rows ${row1 + 1}, ${row2 + 1}, ${row3 + 1} " +
                                        "appears only in columns ${swordfishCols.joinToString { (it + 1).toString() }}. " +
                                        "Eliminate $value from these columns in other rows."
                            )
                        }
                    }
                }
            }

            // Column-based Swordfish
            val colToRows = mutableMapOf<Int, Set<Int>>()
            for (col in 0..8) {
                val rowsSet = mutableSetOf<Int>()
                for (row in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        rowsSet.add(row)
                    }
                }
                if (rowsSet.size in 2..3) {
                    colToRows[col] = rowsSet
                }
            }

            val cols = colToRows.keys.toList()
            for (i in 0 until cols.size - 2) {
                for (j in i + 1 until cols.size - 1) {
                    for (k in j + 1 until cols.size) {
                        val col1 = cols[i]
                        val col2 = cols[j]
                        val col3 = cols[k]
                        val rows1 = colToRows[col1]!!
                        val rows2 = colToRows[col2]!!
                        val rows3 = colToRows[col3]!!

                        if (rows1 == rows2 && rows2 == rows3) {
                            val swordfishRows = rows1.toList()
                            return HintGenerator.Hint(
                                coord = Coord(swordfishRows[0], col1),
                                value = value,
                                technique = technique,
                                explanation = "Swordfish found! Value $value in columns ${col1 + 1}, ${col2 + 1}, ${col3 + 1} " +
                                        "appears only in rows ${swordfishRows.joinToString { (it + 1).toString() }}. " +
                                        "Eliminate $value from these rows in other columns."
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}
