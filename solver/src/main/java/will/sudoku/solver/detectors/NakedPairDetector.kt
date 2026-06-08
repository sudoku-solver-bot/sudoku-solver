package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.CoordGroup
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects naked pairs — two cells in a group with the same two candidates.
 */
class NakedPairDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.NAKED_PAIR

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coordGroup in CoordGroup.all) {
            val pairCells = coordGroup.coords.filter { coord ->
                !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
            }

            for (i in pairCells.indices) {
                for (j in i + 1 until pairCells.size) {
                    val coord1 = pairCells[i]
                    val coord2 = pairCells[j]
                    val candidates1 = board.candidateValues(coord1).toSet()
                    val candidates2 = board.candidateValues(coord2).toSet()

                    if (candidates1 == candidates2 && candidates1.size == 2) {
                        val values = candidates1.toList()
                        return HintGenerator.Hint(
                            coord = coord1,
                            value = values[0],
                            technique = technique,
                            explanation = "Cells (${coord1.row + 1},${coord1.col + 1}) and (${coord2.row + 1},${coord2.col + 1}) " +
                                    "form a naked pair with values ${values[0]} and ${values[1]}. " +
                                    "These values can be eliminated from other cells in this group."
                        )
                    }
                }
            }
        }
        return null
    }
}
