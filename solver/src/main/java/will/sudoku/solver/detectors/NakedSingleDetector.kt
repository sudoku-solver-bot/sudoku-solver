package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects naked singles — cells with only one remaining candidate.
 */
class NakedSingleDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.NAKED_SINGLE

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coord in Coord.all) {
            if (board.isConfirmed(coord)) continue
            val candidates = board.candidateValues(coord)
            if (candidates.size == 1) {
                val value = candidates.first()
                val seen = mutableSetOf<Int>()
                for (i in 0..8) {
                    seen.add(board.value(Coord(coord.row, i)))
                    seen.add(board.value(Coord(i, coord.col)))
                }
                val boxRow = coord.row / 3 * 3
                val boxCol = coord.col / 3 * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        seen.add(board.value(Coord(r, c)))
                    }
                }
                seen.remove(0)
                return HintGenerator.Hint(
                    coord = coord,
                    value = value,
                    technique = technique,
                    explanation = "Cell (${coord.row + 1}, ${coord.col + 1}) can only be $value! " +
                            "All other numbers ${(1..9).filter { it != value && it !in seen }.joinToString(", ")} " +
                            "are already present in the row, column, or box."
                )
            }
        }
        return null
    }
}
