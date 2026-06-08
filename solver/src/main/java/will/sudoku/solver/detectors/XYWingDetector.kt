package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects XY-Wing patterns — pivot {X,Y}, Wing1 {X,Z}, Wing2 {Y,Z}, eliminate Z from cells seeing both wings.
 */
class XYWingDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.XY_WING

    override fun detect(board: Board): HintGenerator.Hint? {
        val biValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
        }

        for (pivot in biValueCells) {
            val pivotCandidates = board.candidateValues(pivot).toList()
            if (pivotCandidates.size != 2) continue
            val x = pivotCandidates[0]
            val y = pivotCandidates[1]

            val wingCells = biValueCells.filter { wing ->
                wing != pivot && CoordUtils.seesEachOther(wing, pivot)
            }

            for (wing1 in wingCells) {
                val w1Candidates = board.candidateValues(wing1).toSet()
                if (w1Candidates.size != 2) continue

                val shared1 = if (x in w1Candidates) x else if (y in w1Candidates) y else null
                if (shared1 == null) continue
                val z1 = w1Candidates.find { it != shared1 } ?: continue

                for (wing2 in wingCells) {
                    if (wing2 == wing1) continue
                    val w2Candidates = board.candidateValues(wing2).toSet()
                    if (w2Candidates.size != 2) continue

                    val shared2 = if (x in w2Candidates) x else if (y in w2Candidates) y else null
                    if (shared2 == null || shared2 == shared1) continue
                    val z2 = w2Candidates.find { it != shared2 } ?: continue

                    if (z1 != z2) continue

                    for (coord in Coord.all) {
                        if (coord == pivot || coord == wing1 || coord == wing2) continue
                        if (board.isConfirmed(coord)) continue
                        if (!CoordUtils.seesEachOther(coord, wing1) || !CoordUtils.seesEachOther(coord, wing2)) continue
                        if (z1 in board.candidateValues(coord)) {
                            return HintGenerator.Hint(
                                coord = coord,
                                value = z1,
                                technique = technique,
                                explanation = "XY-Wing found! Pivot (${pivot.row + 1},${pivot.col + 1}) " +
                                        "has {$x,$y}, Wing1 (${wing1.row + 1},${wing1.col + 1}) " +
                                        "has {$shared1,$z1}, Wing2 (${wing2.row + 1},${wing2.col + 1}) " +
                                        "has {$shared2,$z1}. Eliminate $z1 from cells seeing both wings."
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}
