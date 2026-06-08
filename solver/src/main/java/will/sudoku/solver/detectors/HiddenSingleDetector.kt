package will.sudoku.solver.detectors

import will.sudoku.solver.Board
import will.sudoku.solver.Coord
import will.sudoku.solver.CoordGroup
import will.sudoku.solver.HintGenerator
import will.sudoku.solver.TechniqueDetector

/**
 * Detects hidden singles — values that appear only once in a row, column, or region.
 */
class HiddenSingleDetector : TechniqueDetector {

    override val technique = HintGenerator.Technique.HIDDEN_SINGLE

    override fun detect(board: Board): HintGenerator.Hint? {
        for (coordGroup in CoordGroup.all) {
            val knownValues = coordGroup.coords.map { board.value(it) }.toSet()

            // Count occurrences of each candidate in the group
            val candidateCounts = mutableMapOf<Int, MutableList<Coord>>()

            for (coord in coordGroup.coords) {
                if (board.isConfirmed(coord)) continue
                for (candidate in board.candidateValues(coord)) {
                    candidateCounts.getOrPut(candidate) { mutableListOf() }.add(coord)
                }
            }

            // Find candidates that appear only once
            for ((value, coords) in candidateCounts) {
                if (coords.size == 1 && value !in knownValues) {
                    val coord = coords[0]
                    // Determine which type of group this is based on the coordinates
                    val firstCoord = coordGroup.coords[0]
                    val lastCoord = coordGroup.coords[8]
                    val groupName = when {
                        firstCoord.row == lastCoord.row -> "row ${firstCoord.row + 1}"
                        firstCoord.col == lastCoord.col -> "column ${firstCoord.col + 1}"
                        else -> {
                            val regionRow = coord.row / 3 + 1
                            val regionCol = coord.col / 3 + 1
                            "region ($regionRow, $regionCol)"
                        }
                    }

                    return HintGenerator.Hint(
                        coord = coord,
                        value = value,
                        technique = technique,
                        explanation = "Value $value appears only once in $groupName. Fill it in at (${coord.row + 1}, ${coord.col + 1})!"
                    )
                }
            }
        }

        return null
    }
}
