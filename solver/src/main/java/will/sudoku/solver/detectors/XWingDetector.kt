package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects X-Wing patterns — candidate in 2 rows restricted to same 2 columns (or vice versa).
 */
class XWingDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.X_WING

    override fun detect(board: Board): HintGenerator.Hint? {
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            val rowToColumns = mutableMapOf<Int, Set<Int>>()
            for (row in 0..8) {
                val columns = mutableSetOf<Int>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        columns.add(col)
                    }
                }
                if (columns.size == 2) {
                    rowToColumns[row] = columns
                }
            }

            for ((row1, cols1) in rowToColumns) {
                for ((row2, cols2) in rowToColumns) {
                    if (row1 >= row2) continue
                    if (cols1 != cols2) continue

                    val cols = cols1.toList()
                    return HintGenerator.Hint(
                        coord = Coord(row1, cols[0]),
                        value = value,
                        technique = technique,
                        explanation = "X-Wing found! Value $value appears in rows ${row1 + 1} and ${row2 + 1} " +
                                "only in columns ${cols[0] + 1} and ${cols[1] + 1}. " +
                                "This value can be eliminated from these columns in other rows."
                    )
                }
            }
        }

        return null
    }
}
