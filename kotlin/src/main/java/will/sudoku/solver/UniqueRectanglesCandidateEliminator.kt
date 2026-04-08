package will.sudoku.solver

/**
 * Unique Rectangles Candidate Eliminator
 *
 * Detects deadly rectangle patterns and eliminates candidates based on the uniqueness constraint.
 * A valid Sudoku puzzle should have only one solution, so patterns that would create multiple
 * solutions can be avoided by eliminating certain candidates.
 *
 * ## Deadly Rectangle Pattern
 * A deadly rectangle consists of 4 cells forming a rectangle:
 * - Cells are at positions (r1, c1), (r1, c2), (r2, c1), (r2, c2)
 * - All 4 cells contain exactly the same 2 candidates {X, Y}
 * - Cells are in exactly 2 different rows, 2 different columns, and 2 different boxes
 *
 * This pattern is "deadly" because swapping X and Y between the cells would create
 * a second valid solution, which violates the uniqueness constraint of proper Sudoku puzzles.
 *
 * ## Elimination Types
 *
 * ### Type 1 (Single Extra Candidate)
 * If exactly one of the 4 rectangle cells has additional candidates beyond {X, Y}:
 * - Eliminate X and Y from that cell, leaving only the extra candidates
 *
 * ## Algorithm
 * 1. Find all pairs of rows (r1, r2) where r1 < r2
 * 2. Find all pairs of columns (c1, c2) where c1 < c2
 * 3. For each combination, check if the 4 corner cells form a deadly rectangle
 * 4. Apply Type 1 elimination when pattern is found
 *
 * ## Reference
 * https://www.sudopedia.org/wiki/Unique_Rectangle
 */
class UniqueRectanglesCandidateEliminator : CandidateEliminator {

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false

        // Try all combinations of 2 rows and 2 columns
        for (r1 in 0 until 8) {
            for (r2 in (r1 + 1) until 9) {
                for (c1 in 0 until 8) {
                    for (c2 in (c1 + 1) until 9) {
                        val corners = listOf(
                            Coord(r1, c1),
                            Coord(r1, c2),
                            Coord(r2, c1),
                            Coord(r2, c2)
                        )

                        // Check if this forms a deadly rectangle
                        val rectanglePattern = findDeadlyRectanglePattern(board, corners)
                        if (rectanglePattern != null) {
                            val (x, y) = rectanglePattern

                            // Apply Type 1 elimination (single cell with extra candidates)
                            val type1Result = applyType1Elimination(board, corners, x, y)
                            if (type1Result) anyUpdate = true
                        }
                    }
                }
            }
        }

        return anyUpdate
    }

    /**
     * Check if the 4 corner cells form a deadly rectangle pattern.
     * Returns the pair of candidates (X, Y) if deadly, null otherwise.
     *
     * A deadly rectangle requires:
     * - All 4 cells are in exactly 2 boxes (diagonal corners in same box)
     * - All 4 cells contain the same 2 candidates {X, Y}
     * - At least one cell has extra candidates (otherwise we can't eliminate anything)
     * - None of the cells are confirmed
     */
    private fun findDeadlyRectanglePattern(board: Board, corners: List<Coord>): Pair<Int, Int>? {
        // Check that none of the corners are confirmed
        if (corners.any { board.isConfirmed(it) }) return null

        // Check that corners are in exactly 2 boxes
        val box1 = getBoxIndex(corners[0]) // (r1, c1)
        val box2 = getBoxIndex(corners[1]) // (r1, c2)
        val box3 = getBoxIndex(corners[2]) // (r2, c1)
        val box4 = getBoxIndex(corners[3]) // (r2, c2)

        // Diagonal corners must be in same boxes:
        // (r1,c1) and (r2,c2) in same box, (r1,c2) and (r2,c1) in same box
        // And the two boxes must be different
        if (box1 != box4 || box2 != box3) return null
        if (box1 == box2) return null // All in same box, not a valid rectangle

        // Get candidates for all 4 corners
        val candidatesList = corners.map { board.candidateValues(it).toSet() }

        // Find common candidates that appear in all cells
        val commonCandidates = candidatesList.reduce { acc, set -> acc.intersect(set) }
        if (commonCandidates.size < 2) return null

        // Check if exactly 2 candidates are common to all cells
        if (commonCandidates.size != 2) return null

        val (x, y) = commonCandidates.toList().let { it[0] to it[1] }

        // Verify at least one cell has extra candidates (for Type 1)
        // and at least 3 cells have exactly 2 candidates (the rectangle pattern)
        val cellsWithExtras = candidatesList.count { it.size > 2 }
        val cellsWithTwo = candidatesList.count { it.size == 2 }
        if (cellsWithExtras < 1 || cellsWithTwo < 3) return null

        return Pair(x, y)
    }

    /**
     * Apply Type 1 elimination: If exactly one cell has extra candidates beyond {X, Y},
     * eliminate X and Y from that cell, leaving only the extra candidates.
     *
     * This is safe because if we have a deadly rectangle {X, Y} but one cell
     * also has other candidates, that cell CANNOT be X or Y (otherwise we'd have
     * multiple solutions). So we eliminate X and Y from that cell.
     */
    private fun applyType1Elimination(
        board: Board,
        corners: List<Coord>,
        x: Int,
        y: Int
    ): Boolean {
        var anyUpdate = false

        // Find cells with extra candidates beyond {X, Y}
        val cellsWithExtras = corners.filter { coord ->
            val candidates = board.candidateValues(coord)
            candidates.size > 2 && x in candidates && y in candidates
        }

        // Type 1 requires exactly one cell with extra candidates
        if (cellsWithExtras.size == 1) {
            val targetCoord = cellsWithExtras[0]
            val erasedX = board.eraseCandidateValue(targetCoord, x)
            val erasedY = board.eraseCandidateValue(targetCoord, y)
            if (erasedX || erasedY) anyUpdate = true
        }

        return anyUpdate
    }

    /**
     * Get the box index (0-8) for a coordinate.
     */
    private fun getBoxIndex(coord: Coord): Int {
        val boxRow = coord.row / 3
        val boxCol = coord.col / 3
        return boxRow * 3 + boxCol
    }

    /**
     * Check if two cells are in the same 3x3 region.
     */
    private fun sameRegion(coord1: Coord, coord2: Coord): Boolean {
        val regionRow1 = coord1.row / 3
        val regionCol1 = coord1.col / 3
        val regionRow2 = coord2.row / 3
        val regionCol2 = coord2.col / 3
        return regionRow1 == regionRow2 && regionCol1 == regionCol2
    }
}
