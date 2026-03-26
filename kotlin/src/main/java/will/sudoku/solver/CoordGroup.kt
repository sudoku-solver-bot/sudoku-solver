package will.sudoku.solver

import will.sudoku.solver.BoardSettings.regionSize
import will.sudoku.solver.BoardSettings.size

/**
 * Represents a group of coordinates (row, column, or 3x3 region).
 *
 * CoordGroups are used by the solver's constraint propagation to validate that no
 * group contains duplicate values or invalid cell arrangements.
 *
 * ## Group Types
 * - **Vertical**: All cells in the same column
 * - **Horizontal**: All cells in the same row
 * - **Region**: All cells in the same 3x3 subgrid
 *
 * ## Example
 * ```kotlin
 * // Get the first row group
 * val rowGroup = CoordGroup.verticalOf(Coord(0, 0))
 * // rowGroup.coords // Returns all coordinates in row 0
 *
 * // Get a 3x3 region (top-left)
 * val regionGroup = CoordGroup.regionOf(Coord(0, 0))
 * // regionGroup.coords // Returns all 9 cells in that region
 * ```
 *
 * @property coords The list of coordinates in this group.
 */
data class CoordGroup private constructor(val coords: List<Coord>) {
    /**
     * Returns a list of all coordinate groups for the entire board.
     *
     * This includes:
     * - 9 vertical groups (columns)
     * - 9 horizontal groups (rows)
     * - 9 region groups (3x3 subgrids)
     *
     * Used by the solver to check constraint violations.
     *
     * @return List of all CoordGroup instances.
     */
    companion object {
        /**
         * Pre-computed group indices for efficient group lookup.
         *
         * Groups are indexed 0-8:
         * - 0-2: Columns 0-2
         * - 3-5: Columns 3-5
         * - 6-8: Columns 6-8
         *
         * Rows are indexed 0-8:
         * - 0-2: Rows 0-2
         * - 3-5: Rows 3-5
         * - 6-8: Rows 6-8
         *
         * Regions are indexed 0-8:
         * - 0-2: Top-left, Top-center, Top-right
         * - 3-5: Middle-left, Middle-center, Middle-right
         * - 6-8: Bottom-left, Bottom-center, Bottom-right
         */
        private val indices = (0 until Settings.size)

        /**
         * Pre-computed vertical coordinate groups (columns).
         *
         * Cached for O(1) access when getting vertical groups.
         */
        private val verticalCoordGroups = indices.map { groupIndex -> CoordGroup(indices, groupIndex..groupIndex) }

        /**
         * Pre-computed horizontal coordinate groups (rows).
         *
         * Cached for O(1) access when getting horizontal groups.
         */
        private val horizontalCoordGroups = indices.map { groupIndex -> CoordGroup(groupIndex..groupIndex, indices) }

        /**
         * Pre-computed region coordinate groups (3x3 subgrids).
         *
         * Cached for O(1) access when getting region groups.
         */
        private val regionCoordGroups = indices.map {
            val regionRow = it / regionSize
            val regionCol = it % regionSize
            val rowStart = regionRow * regionSize
            val colStart = regionCol * regionSize
            val rows = rowStart until (rowStart + regionSize)
            val cols = colStart until (colStart + regionSize)
            CoordGroup(rows, cols)
        }

        /**
         * All coordinate groups for the 9x9 board.
         *
         * Returns vertical + horizontal + region groups (total 27 groups).
         */
        val all = listOf<CoordGroup>()
            .plus(verticalCoordGroups)
            .plus(horizontalCoordGroups)
            .plus(regionCoordGroups)

        /**
         * Creates a CoordGroup for all coordinates in the specified column.
         *
         * ## Example
         * ```kotlin
         * val columnGroup = CoordGroup.verticalOf(Coord(0, 0))
         * // columnGroup.coords contains all 9 cells in column 0
         * ```
         *
         * @param colIndex The column index (0-8).
         * @return A CoordGroup containing all cells in the specified column.
         */
        fun verticalOf(coord: Coord): CoordGroup {
            return verticalCoordGroups[coord.col]
        }

        /**
         * Creates a CoordGroup for all coordinates in the specified row.
         *
         * ## Example
         * ```kotlin
         * val rowGroup = CoordGroup.horizontalOf(Coord(0, 0))
         * // rowGroup.coords contains all 9 cells in row 0
         * ```
         *
         * @param rowIndex The row index (0-8).
         * @return A CoordGroup containing all cells in the specified row.
         */
        fun horizontalOf(coord: Coord): CoordGroup {
            return horizontalCoordGroups[coord.row]
        }

        /**
         * Creates a CoordGroup for the 3x3 region containing the specified coordinate.
         *
         * Regions are indexed 0-8:
         * - 0: Top-left, 1: Top-center, 2: Top-right
         * - 3: Middle-left, 4: Middle-center, 5: Middle-right
         * - 6: Bottom-left, 7: Bottom-center, 8: Bottom-right
         *
         * ## Example
         * ```kotlin
         * val topLeftRegion = CoordGroup.regionOf(Coord(0, 0))
         * // topLeftRegion.coords contains 9 cells in top-left 3x3 region
         * ```
         *
         * @param coord The coordinate within the region.
         * @return A CoordGroup for the region containing the specified coordinate.
         */
        fun regionOf(coord: Coord): CoordGroup {
            return regionCoordGroups[coord.region]
        }

        /**
         * Creates a CoordGroup for all groups containing the specified coordinate.
         *
         * This is useful for iterating over all groups that a particular coordinate
         * belongs to (vertical, horizontal, and region).
         *
         * ## Example
         * ```kotlin
         * val allGroups = CoordGroup.of(Coord(4, 5))
         * // allGroups is a list of 3 groups containing Coord(4,5)
         * ```
         *
         * @param coords The coordinates to get groups for.
         * @return A list of CoordGroup instances for each group type.
         */
        fun of(coord: Coord): List<CoordGroup> {
            return listOf(verticalOf(coord), horizontalOf(coord), regionOf(coord))
        }

        /**
         * Creates a CoordGroup for a rectangular range of cells.
         *
         * This is useful for creating custom groups of cells for advanced solving
         * strategies or partial board analysis.
         *
         * ## Example
         * ```kotlin
         * val centerRow = CoordGroup.invoke(4, 3..5) // Row 4, columns 3-5
         * val centerCol = CoordGroup.invoke(3..5, 4)   // Rows 3-5, column 4
         * val centerRegion = CoordGroup.invoke(4..5, 3..5) // 3x3 center region
         * ```
         *
         * @param rows Range of row indices (inclusive).
         * @param cols Range of column indices (inclusive).
         * @return A CoordGroup for the specified cell range.
         */
        operator fun invoke(rows: IntRange, cols: IntRange): CoordGroup {
            return CoordGroup(
                sequence {
                    for (row in rows)
                        for (col in cols)
                            yield(Coord(row, col))
                }.toList()
            )
        }
    }
}