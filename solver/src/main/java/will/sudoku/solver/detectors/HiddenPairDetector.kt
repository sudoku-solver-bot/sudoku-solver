package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.CoordGroup
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects hidden pairs — two values that appear only in the same two cells within a group.
 */
class HiddenPairDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.HIDDEN_PAIR

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coordGroup in CoordGroup.all) {
            val valueToCells = mutableMapOf<Int, MutableList<will.sudoku.solver.Coord>>()
            for (value in 1..9) {
                val cells = mutableListOf<will.sudoku.solver.Coord>()
                for (coord in coordGroup.coords) {
                    if (!board.isConfirmed(coord) && value in board.candidateValues(coord)) {
                        cells.add(coord)
                    }
                }
                if (cells.size == 2) {
                    valueToCells[value] = cells
                }
            }

            val values = valueToCells.keys.toList()
            for (i in values.indices) {
                for (j in i + 1 until values.size) {
                    val v1 = values[i]
                    val v2 = values[j]
                    val cells1 = valueToCells[v1]!!.toSet()
                    val cells2 = valueToCells[v2]!!.toSet()

                    if (cells1 == cells2 && cells1.size == 2) {
                        val cells = cells1.toList()
                        val extraCandidates1 = board.candidateValues(cells[0]).filter { it != v1 && it != v2 }
                        val extraCandidates2 = board.candidateValues(cells[1]).filter { it != v1 && it != v2 }

                        if (extraCandidates1.isNotEmpty()) {
                            return HintGenerator.Hint(
                                coord = cells[0],
                                value = extraCandidates1.first(),
                                technique = technique,
                                explanation = "Values $v1 and $v2 form a hidden pair in cells " +
                                        "(${cells[0].row + 1},${cells[0].col + 1}) and " +
                                        "(${cells[1].row + 1},${cells[1].col + 1}). " +
                                        "Other candidates can be eliminated from these cells."
                            )
                        }
                        if (extraCandidates2.isNotEmpty()) {
                            return HintGenerator.Hint(
                                coord = cells[1],
                                value = extraCandidates2.first(),
                                technique = technique,
                                explanation = "Values $v1 and $v2 form a hidden pair in cells " +
                                        "(${cells[0].row + 1},${cells[0].col + 1}) and " +
                                        "(${cells[1].row + 1},${cells[1].col + 1}). " +
                                        "Other candidates can be eliminated from these cells."
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}
