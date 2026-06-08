package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects XYZ-Wing patterns — pivot {X,Y,Z}, Wing1 {X,Z}, Wing2 {Y,Z}, eliminate Z from cells seeing all three.
 */
class XYZWingDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.XYZ_WING

    override fun detect(board: Board): HintGenerator.Hint? {
        val triValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 3
        }

        for (pivot in triValueCells) {
            val pivotCands = board.candidateValues(pivot).toSet()
            if (pivotCands.size != 3) continue

            val wings = Coord.all.filter { coord ->
                coord != pivot &&
                        !board.isConfirmed(coord) &&
                        board.candidatePattern(coord).countOneBits() == 2 &&
                        CoordUtils.seesEachOther(coord, pivot)
            }

            for (wing1 in wings) {
                val w1Cands = board.candidateValues(wing1).toSet()
                if (pivotCands.containsAll(w1Cands)) continue

                for (wing2 in wings) {
                    if (wing2 == wing1) continue
                    val w2Cands = board.candidateValues(wing2).toSet()
                    if (pivotCands.containsAll(w2Cands)) continue

                    val zCandidates = w1Cands.intersect(w2Cands)
                    for (z in zCandidates) {
                        if (z !in pivotCands) continue
                        if (w1Cands + w2Cands != pivotCands) continue

                        for (coord in Coord.all) {
                            if (coord == pivot || coord == wing1 || coord == wing2) continue
                            if (board.isConfirmed(coord)) continue
                            if (!CoordUtils.seesEachOther(coord, pivot)) continue
                            if (!CoordUtils.seesEachOther(coord, wing1)) continue
                            if (!CoordUtils.seesEachOther(coord, wing2)) continue
                            if (z in board.candidateValues(coord)) {
                                return HintGenerator.Hint(
                                    coord = coord,
                                    value = z,
                                    technique = technique,
                                    explanation = "XYZ-Wing found! Pivot (${pivot.row + 1},${pivot.col + 1}) " +
                                            "has {$pivotCands}, Wing1 (${wing1.row + 1},${wing1.col + 1}) " +
                                            "has {$w1Cands}, Wing2 (${wing2.row + 1},${wing2.col + 1}) " +
                                            "has {$w2Cands}. Eliminate $z from cells seeing all three."
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
