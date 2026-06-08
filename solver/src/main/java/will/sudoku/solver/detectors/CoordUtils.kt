package will.sudoku.solver.detectors

import will.sudoku.solver.Coord

/**
 * Utility functions for coordinate-based checks used by technique detectors.
 */
internal object CoordUtils {

    /**
     * Check if two cells see each other (same row, column, or region).
     */
    fun seesEachOther(coord1: Coord, coord2: Coord): Boolean {
        return coord1.row == coord2.row ||
                coord1.col == coord2.col ||
                sameRegion(coord1, coord2)
    }

    /**
     * Check if two cells are in the same 3x3 region.
     */
    fun sameRegion(coord1: Coord, coord2: Coord): Boolean {
        return coord1.row / 3 == coord2.row / 3 && coord1.col / 3 == coord2.col / 3
    }
}
