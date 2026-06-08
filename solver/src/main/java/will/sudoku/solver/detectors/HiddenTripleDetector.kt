package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.CoordGroup
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects hidden triples — three values that appear only in the same three cells within a group.
 */
class HiddenTripleDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.HIDDEN_TRIPLE

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coordGroup in CoordGroup.all) {
            val valueToCells = mutableMapOf<Int, Set<Coord>>()
            for (value in 1..9) {
                val cells = mutableSetOf<Coord>()
                for (coord in coordGroup.coords) {
                    if (!board.isConfirmed(coord) && value in board.candidateValues(coord)) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    valueToCells[value] = cells
                }
            }

            val values = valueToCells.keys.toList()
            if (values.size < 3) continue
            for (i in 0 until values.size - 2) {
                for (j in i + 1 until values.size - 1) {
                    for (k in j + 1 until values.size) {
                        val v1 = values[i]
                        val v2 = values[j]
                        val v3 = values[k]
                        val union = valueToCells[v1]!! + valueToCells[v2]!! + valueToCells[v3]!!

                        if (union.size == 3) {
                            val cells = union.toList()
                            for (cell in cells) {
                                val extraCandidates = board.candidateValues(cell).filter { it != v1 && it != v2 && it != v3 }
                                if (extraCandidates.isNotEmpty()) {
                                    return HintGenerator.Hint(
                                        coord = cell,
                                        value = extraCandidates.first(),
                                        technique = technique,
                                        explanation = "Values $v1, $v2, and $v3 form a hidden triple. " +
                                                "Other candidates can be eliminated from these cells."
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
