package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects pointing pairs — candidates restricted to one row/col within a box.
 */
class PointingPairDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.POINTING_PAIR

    override fun detect(board: Board): HintGenerator.Hint? {
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                for (value in 1..9) {
                    val cells = mutableListOf<Coord>()
                    for (r in boxRow * 3 until (boxRow + 1) * 3) {
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            val coord = Coord(r, c)
                            if (!board.isConfirmed(coord) &&
                                value in board.candidateValues(coord)
                            ) {
                                cells.add(coord)
                            }
                        }
                    }
                    if (cells.size in 2..3) {
                        // Check if all cells are in the same row
                        val rows = cells.map { it.row }.toSet()
                        if (rows.size == 1) {
                            val row = rows.first()
                            for (col in 0..8) {
                                if (col / 3 == boxCol) continue
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return HintGenerator.Hint(
                                        coord = coord,
                                        value = value,
                                        technique = technique,
                                        explanation = "Value $value in box (${boxRow + 1},${boxCol + 1}) " +
                                                "is restricted to row ${row + 1}. " +
                                                "Eliminate $value from row ${row + 1} in other boxes."
                                    )
                                }
                            }
                        }
                        // Check if all cells are in the same column
                        val cols = cells.map { it.col }.toSet()
                        if (cols.size == 1) {
                            val col = cols.first()
                            for (row in 0..8) {
                                if (row / 3 == boxRow) continue
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return HintGenerator.Hint(
                                        coord = coord,
                                        value = value,
                                        technique = technique,
                                        explanation = "Value $value in box (${boxRow + 1},${boxCol + 1}) " +
                                                "is restricted to column ${col + 1}. " +
                                                "Eliminate $value from column ${col + 1} in other boxes."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
