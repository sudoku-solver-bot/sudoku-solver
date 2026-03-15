package will.sudoku.solver

/**
 * Hint Generator
 *
 * Analyzes board state and suggests the next solving move.
 * Helps users learn solving techniques by explaining what technique to use.
 *
 * ## Usage
 * ```kotlin
 * val hint = HintGenerator.generate(board)
 * if (hint != null) {
 *     println("Look at cell (${hint.coord.row}, ${hint.coord.col})")
 *     println("Technique: ${hint.technique}")
 *     println("Explanation: ${hint.explanation}")
 * }
 * ```
 */
object HintGenerator {

    /**
     * A hint for the next solving move.
     *
     * @property coord The cell to focus on
     * @property value The value that can be determined
     * @property technique The solving technique to use
     * @property explanation Explanation of why this works
     */
    data class Hint(
        val coord: Coord,
        val value: Int,
        val technique: Technique,
        val explanation: String
    ) {
        override fun toString(): String = buildString {
            appendLine("Hint: Look at cell (${coord.row}, ${coord.col})")
            appendLine("  Value: $value")
            appendLine("  Technique: ${technique.displayName}")
            appendLine("  Explanation: $explanation")
        }
    }

    /**
     * Solving techniques that can be hinted.
     */
    enum class Technique(val displayName: String, val description: String) {
        HIDDEN_SINGLE("Hidden Single", "This value appears only once in a row, column, or region"),
        NAKED_PAIR("Naked Pair", "Two cells with same two candidates - eliminate from others"),
        NAKED_TRIPLE("Naked Triple", "Three cells with same three candidates - eliminate from others"),
        HIDDEN_PAIR("Hidden Pair", "Two values appear only in same two cells"),
        HIDDEN_TRIPLE("Hidden Triple", "Three values appear only in same three cells"),
        X_WING("X-Wing", "Pattern in 2 rows/cols allows elimination"),
        SWORDFISH("Swordfish", "Pattern in 3 rows/cols allows elimination"),
        XY_WING("XY-Wing", "Chain of 3 cells allows elimination")
    }

    /**
     * Generate a hint for the next move.
     *
     * @param board The current board state
     * @return A hint if one is found, null otherwise
     */
    fun generate(board: Board): Hint? {
        // Try techniques in order of difficulty
        return findHiddenSingle(board)
            ?: findNakedPair(board)
            ?: findXWing(board)
    }

    /**
     * Find a hidden single (easiest technique).
     */
    private fun findHiddenSingle(board: Board): Hint? {
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

                    return Hint(
                        coord = coord,
                        value = value,
                        technique = Technique.HIDDEN_SINGLE,
                        explanation = "Value $value appears only once in $groupName. It must go here!"
                    )
                }
            }
        }

        return null
    }

    /**
     * Find a naked pair.
     */
    private fun findNakedPair(board: Board): Hint? {
        for (coordGroup in CoordGroup.all) {
            // Find cells with exactly 2 candidates
            val pairCells = coordGroup.coords.filter { coord ->
                !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
            }

            // Check for matching pairs
            for (i in pairCells.indices) {
                for (j in i + 1 until pairCells.size) {
                    val coord1 = pairCells[i]
                    val coord2 = pairCells[j]
                    val candidates1 = board.candidateValues(coord1).toSet()
                    val candidates2 = board.candidateValues(coord2).toSet()

                    if (candidates1 == candidates2 && candidates1.size == 2) {
                        val values = candidates1.toList()
                        return Hint(
                            coord = coord1,
                            value = values[0],
                            technique = Technique.NAKED_PAIR,
                            explanation = "Cells (${coord1.row},${coord1.col}) and (${coord2.row},${coord2.col}) " +
                                    "form a naked pair with values ${values[0]} and ${values[1]}. " +
                                    "These values can be eliminated from other cells in this group."
                        )
                    }
                }
            }
        }

        return null
    }

    /**
     * Find an X-Wing pattern.
     */
    private fun findXWing(board: Board): Hint? {
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            // Build map: row -> columns where candidate appears
            val rowToColumns = mutableMapOf<Int, Set<Int>>()
            for (row in 0..8) {
                val columns = mutableSetOf<Int>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        columns.add(col)
                    }
                }
                if (columns.size == 2) {
                    rowToColumns[row] = columns
                }
            }

            // Find pairs of rows with same columns
            for ((row1, cols1) in rowToColumns) {
                for ((row2, cols2) in rowToColumns) {
                    if (row1 >= row2) continue
                    if (cols1 != cols2) continue

                    val cols = cols1.toList()
                    return Hint(
                        coord = Coord(row1, cols[0]),
                        value = value,
                        technique = Technique.X_WING,
                        explanation = "X-Wing found! Value $value appears in rows ${row1 + 1} and ${row2 + 1} " +
                                "only in columns ${cols[0] + 1} and ${cols[1] + 1}. " +
                                "This value can be eliminated from these columns in other rows."
                    )
                }
            }
        }

        return null
    }
}
