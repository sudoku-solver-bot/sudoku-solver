package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.CoordGroup
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects naked triples — three cells in a group whose candidates are a subset of three values.
 */
class NakedTripleDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.NAKED_TRIPLE

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coordGroup in CoordGroup.all) {
            val unresolved = coordGroup.coords.filter { !board.isConfirmed(it) && board.candidatePattern(it).countOneBits() in 2..3 }

            if (unresolved.size < 3) continue

            for (i in 0 until unresolved.size - 2) {
                for (j in i + 1 until unresolved.size - 1) {
                    for (k in j + 1 until unresolved.size) {
                        val candidates1 = board.candidateValues(unresolved[i]).toSet()
                        val candidates2 = board.candidateValues(unresolved[j]).toSet()
                        val candidates3 = board.candidateValues(unresolved[k]).toSet()
                        val union = candidates1 + candidates2 + candidates3

                        if (union.size == 3) {
                            for (other in unresolved) {
                                if (other == unresolved[i] || other == unresolved[j] || other == unresolved[k]) continue
                                val otherCandidates = board.candidateValues(other).toSet()
                                val overlap = otherCandidates.intersect(union)
                                if (overlap.isNotEmpty()) {
                                    return HintGenerator.Hint(
                                        coord = other,
                                        value = overlap.first(),
                                        technique = technique,
                                        explanation = "Cells (${unresolved[i].row + 1},${unresolved[i].col + 1}), " +
                                                "(${unresolved[j].row + 1},${unresolved[j].col + 1}), and " +
                                                "(${unresolved[k].row + 1},${unresolved[k].col + 1}) " +
                                                "form a naked triple. " +
                                                "Eliminate these values from other cells in this group."
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
