package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects box/line reductions — candidates restricted to one box in a row or column.
 */
class BoxLineReductionDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.BOX_LINE_REDUCTION

    override fun detect(board: Board): HintGenerator.Hint? {
        // Check rows for box/line reduction
        for (row in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        value in board.candidateValues(coord)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        for (r in boxRow * 3 until (boxRow + 1) * 3) {
                            if (r == row) continue
                            for (c in boxCol * 3 until (boxCol + 1) * 3) {
                                val coord = Coord(r, c)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return HintGenerator.Hint(
                                        coord = coord,
                                        value = value,
                                        technique = technique,
                                        explanation = "Value $value in row ${row + 1} " +
                                                "is restricted to box (${boxRow + 1},${boxCol + 1}). " +
                                                "Eliminate $value from other rows in this box."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check columns for box/line reduction
        for (col in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (row in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        value in board.candidateValues(coord)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            if (c == col) continue
                            for (r in boxRow * 3 until (boxRow + 1) * 3) {
                                val coord = Coord(r, c)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return HintGenerator.Hint(
                                        coord = coord,
                                        value = value,
                                        technique = technique,
                                        explanation = "Value $value in column ${col + 1} " +
                                                "is restricted to box (${boxRow + 1},${boxCol + 1}). " +
                                                "Eliminate $value from other columns in this box."
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
