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
     * Ordered from easiest to hardest.
     */
    enum class Technique(val displayName: String, val description: String) {
        NAKED_SINGLE("Naked Single", "Only one candidate fits in this cell"),
        HIDDEN_SINGLE("Hidden Single", "This value appears only once in a row, column, or region"),
        POINTING_PAIR("Pointing Pair", "A candidate restricted to one row/col in a box"),
        BOX_LINE_REDUCTION("Box/Line Reduction", "Candidates restricted to one box in a row/col"),
        NAKED_PAIR("Naked Pair", "Two cells with same two candidates - eliminate from others"),
        NAKED_TRIPLE("Naked Triple", "Three cells with same three candidates - eliminate from others"),
        HIDDEN_PAIR("Hidden Pair", "Two values appear only in same two cells"),
        HIDDEN_TRIPLE("Hidden Triple", "Three values appear only in same three cells"),
        X_WING("X-Wing", "Pattern in 2 rows/cols allows elimination"),
        SWORDFISH("Swordfish", "Pattern in 3 rows/cols allows elimination"),
        XY_WING("XY-Wing", "Chain of 3 cells allows elimination"),
        XYZ_WING("XYZ-Wing", "XYZ-Wing pattern with pivot and two wings"),
        W_WING("W-Wing", "Two cells linked by a strong link"),
        SIMPLE_COLORING("Simple Coloring", "Coloring chain elimination"),
        UNIQUE_RECTANGLE("Unique Rectangle", "Deadly pattern avoidance"),
        ALS_XZ("ALS-XZ", "Almost Locked Set - XZ rule"),
        FRANKEN_FISH("Franken Fish", "Generalized fish with box constraints"),
        MUTANT_FISH("Mutant Fish", "Mutant fish pattern"),
        DEATH_BLOSSOM("Death Blossom", "Death blossom chain"),
        FORCING_CHAINS("Forcing Chains", "Forcing chain elimination")
    }

    /**
     * Generate a hint for the next move.
     *
     * Strategy:
     * 1. Apply basic elimination (SimpleCandidateEliminator) iteratively to stabilise the board
     * 2. Try techniques from easiest to hardest
     * 3. Return the simplest applicable technique (best for learning)
     *
     * @param board The current board state
     * @param exhaustHiddenSingles If true, apply all hidden singles before checking techniques.
     *   Default is true so the hint API returns the "next technique actually needed"
     *   rather than always returning Hidden Single (the easiest technique).
     *   Set to false when you specifically want to find Hidden Singles.
     * @param targetTechnique If set, this technique is checked first before iterating
     *   from easiest to hardest. Useful for tutorials where the student is learning
     *   a specific technique and should be shown it even if a simpler technique is also
     *   available elsewhere on the board.
     * @return A hint if one is found, null otherwise
     */
    fun generate(board: Board, exhaustHiddenSingles: Boolean = true, targetTechnique: Technique? = null): Hint? {
        // Step 1: Apply basic elimination to reach a stable state
        val workingBoard = board.copy()
        applyBasicElimination(workingBoard)

        // Step 2 (optional): Apply hidden singles to advance the board before checking techniques.
        // This is useful for tutorials where we want to demonstrate a specific advanced technique.
        if (exhaustHiddenSingles) {
            applyHiddenSinglesUntilStable(workingBoard)
        }

        // Step 3: If a target technique is specified, check it first.
        // This allows tutorials to prioritize their taught technique.
        if (targetTechnique != null) {
            val targetHint = detectTechnique(workingBoard, targetTechnique)
            if (targetHint != null) return targetHint
        }

        // Step 4: Try techniques from easiest to hardest
        // (Technique enum is ordered easiest→hardest)
        for (technique in Technique.entries) {
            val hint = detectTechnique(workingBoard, technique)
            if (hint != null) return hint
        }

        return null
    }

    /**
     * Apply basic elimination iteratively until the board is stable.
     */
    private fun applyBasicElimination(board: Board) {
        val eliminator = SimpleCandidateEliminator()
        var changed = true
        while (changed) {
            changed = eliminator.eliminate(board)
        }
    }

    /**
     * Apply hidden singles to advance the board before checking for advanced techniques.
     *
     * Iteratively finds hidden singles (values that can only go in one cell within a group),
     * marks them as confirmed, and re-runs constraint propagation. Continues until no more
     * hidden singles are found anywhere on the board.
     *
     * This ensures that advanced techniques (X-Wing, Swordfish, etc.) are only suggested
     * after all hidden singles have been exhausted — i.e., the hint returns the "next
     * technique actually needed" rather than the "easiest technique present."
     */
    internal fun applyHiddenSinglesUntilStable(board: Board) {
        var foundAny = true
        while (foundAny) {
            foundAny = false
            // Find and apply hidden singles one at a time
            var foundOne: Boolean
            do {
                foundOne = false
                val hint = findHiddenSingle(board)
                if (hint != null) {
                    board.markValue(hint.coord, hint.value)
                    foundAny = true
                    foundOne = true
                    // Re-run constraint propagation after each hidden single
                    // to propagate its effects and potentially reveal new
                    // naked singles or hidden singles
                    applyBasicElimination(board)
                }
            } while (foundOne)
        }
    }

    /**
     * Apply pointing pairs to advance the board before checking for advanced techniques.
     *
     * Iteratively finds pointing pairs, eliminates the locked candidates, and re-runs
     * constraint propagation. Continues until no more pointing pairs are found.
     *
     * Useful for tutorials where pointing pairs mask more advanced techniques like
     * Swordfish, XY-Wing, etc.
     */
    internal fun applyPointingPairsUntilStable(board: Board) {
        var foundAny = true
        while (foundAny) {
            foundAny = false
            // Find all pointing pair eliminations and apply them at once
            val eliminations = findPointingPairEliminations(board)
            if (eliminations.isNotEmpty()) {
                foundAny = true
                for ((coord, value) in eliminations) {
                    board.eraseCandidatePattern(coord, Board.masks[value - 1])
                }
                // Re-run constraint propagation to reveal new eliminations
                applyBasicElimination(board)
            }
        }
    }

    /**
     * Find all pointing pair eliminations on the board.
     * Returns a list of (coord, value) pairs to eliminate.
     */
    private fun findPointingPairEliminations(board: Board): List<Pair<Coord, Int>> {
        val eliminations = mutableListOf<Pair<Coord, Int>>()
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                for (value in 1..9) {
                    val cells = mutableListOf<Coord>()
                    for (r in boxRow * 3 until (boxRow + 1) * 3) {
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            val coord = Coord(r, c)
                            if (!board.isConfirmed(coord) &&
                                board.candidateValues(coord).contains(value)
                            ) {
                                cells.add(coord)
                            }
                        }
                    }
                    if (cells.size in 2..3) {
                        val rows = cells.map { it.row }.toSet()
                        if (rows.size == 1) {
                            val row = rows.first()
                            for (col in 0..8) {
                                if (col / 3 == boxCol) continue
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).contains(value)
                                ) {
                                    eliminations.add(Pair(coord, value))
                                }
                            }
                        }
                        val cols = cells.map { it.col }.toSet()
                        if (cols.size == 1) {
                            val col = cols.first()
                            for (row in 0..8) {
                                if (row / 3 == boxRow) continue
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    board.candidateValues(coord).contains(value)
                                ) {
                                    eliminations.add(Pair(coord, value))
                                }
                            }
                        }
                    }
                }
            }
        }
        return eliminations
    }

    /**
     * Detect a specific technique on the board.
     * Runs the corresponding detection method and returns a hint if found.
     */
    private fun detectTechnique(board: Board, technique: Technique): Hint? {
        return when (technique) {
            Technique.NAKED_SINGLE -> findNakedSingle(board)
            Technique.HIDDEN_SINGLE -> findHiddenSingle(board)
            Technique.POINTING_PAIR -> findPointingPair(board)
            Technique.NAKED_PAIR -> findNakedPair(board)
            Technique.NAKED_TRIPLE -> findNakedTriple(board)
            Technique.HIDDEN_PAIR -> findHiddenPair(board)
            Technique.HIDDEN_TRIPLE -> findHiddenTriple(board)
            Technique.X_WING -> findXWing(board)
            Technique.SWORDFISH -> findSwordfish(board)
            Technique.XY_WING -> findXYWing(board)
            Technique.XYZ_WING -> findXYZWWing(board)
            Technique.W_WING -> findTechniqueViaEliminator(board, Technique.W_WING, WWingCandidateEliminator())
            Technique.SIMPLE_COLORING -> findTechniqueViaEliminator(board, Technique.SIMPLE_COLORING, SimpleColoringCandidateEliminator())
            Technique.UNIQUE_RECTANGLE -> findTechniqueViaEliminator(board, Technique.UNIQUE_RECTANGLE, UniqueRectanglesCandidateEliminator())
            Technique.ALS_XZ -> findTechniqueViaEliminator(board, Technique.ALS_XZ, ALSXZCandidateEliminator())
            Technique.FRANKEN_FISH -> findTechniqueViaEliminator(board, Technique.FRANKEN_FISH, FrankenFishCandidateEliminator())
            Technique.MUTANT_FISH -> findTechniqueViaEliminator(board, Technique.MUTANT_FISH, MutantFishCandidateEliminator())
            Technique.DEATH_BLOSSOM -> findTechniqueViaEliminator(board, Technique.DEATH_BLOSSOM, DeathBlossomCandidateEliminator())
            Technique.FORCING_CHAINS -> findTechniqueViaEliminator(board, Technique.FORCING_CHAINS, ForcingChainsCandidateEliminator())
            Technique.BOX_LINE_REDUCTION -> findBoxLineReduction(board)
        }
    }

    /**
     * Generic technique detection using an eliminator.
     * Copies the board, runs the eliminator, and checks if any progress was made.
     * If so, finds an affected cell to report.
     */
    private fun findTechniqueViaEliminator(
        board: Board,
        technique: Technique,
        eliminator: CandidateEliminator
    ): Hint? {
        val testBoard = board.copy()
        val madeProgress = eliminator.eliminate(testBoard)
        if (!madeProgress) return null

        // Find a cell that was affected (lost candidates)
        for (coord in Coord.all) {
            if (board.isConfirmed(coord) || testBoard.isConfirmed(coord)) continue
            val beforeCandidates = board.candidateValues(coord).toSet()
            val afterCandidates = testBoard.candidateValues(coord).toSet()
            val eliminated = beforeCandidates - afterCandidates
            if (eliminated.isNotEmpty()) {
                val eliminatedStr = eliminated.joinToString(", ")
                return Hint(
                    coord = coord,
                    value = eliminated.first(),
                    technique = technique,
                    explanation = "${technique.description} " +
                            "Candidate $eliminatedStr can be eliminated from cell (${coord.row + 1}, ${coord.col + 1})."
                )
            }
        }

        // If no specific cell found, fall back to any unresolved cell
        for (coord in Coord.all) {
            if (!board.isConfirmed(coord)) {
                val candidates = board.candidateValues(coord)
                if (candidates.isNotEmpty()) {
                    return Hint(
                        coord = coord,
                        value = candidates.first(),
                        technique = technique,
                        explanation = "${technique.description} " +
                                "Look for the ${technique.displayName} pattern to eliminate candidates."
                    )
                }
            }
        }
        return null
    }

    /**
     * Find a naked single (easiest technique).
     * A cell with only one remaining candidate.
     */
    private fun findNakedSingle(board: Board): Hint? {
        for (coord in Coord.all) {
            if (board.isConfirmed(coord)) continue
            val candidates = board.candidateValues(coord)
            if (candidates.size == 1) {
                val value = candidates.first()
                val seen = mutableSetOf<Int>()
                for (i in 0..8) {
                    seen.add(board.value(Coord(coord.row, i)))
                    seen.add(board.value(Coord(i, coord.col)))
                }
                val boxRow = coord.row / 3 * 3
                val boxCol = coord.col / 3 * 3
                for (r in boxRow until boxRow + 3) {
                    for (c in boxCol until boxCol + 3) {
                        seen.add(board.value(Coord(r, c)))
                    }
                }
                seen.remove(0)
                return Hint(
                    coord = coord,
                    value = value,
                    technique = Technique.NAKED_SINGLE,
                    explanation = "Cell (${coord.row + 1}, ${coord.col + 1}) can only be $value! " +
                            "All other numbers ${(1..9).filter { it != value && it !in seen }.joinToString(", ")} " +
                            "are already present in the row, column, or box."
                )
            }
        }
        return null
    }

    /**
     * Find a hidden single.
     */
    internal fun findHiddenSingle(board: Board): Hint? {
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
     * Exhaust all solver techniques on the board without backtracking.
     *
     * Runs the full eliminator chain (constraint propagation) iteratively,
     * filling naked singles as they appear, until no more progress is made.
     * This exhausts all techniques including Pointing Pair, Naked Pair,
     * Hidden Pair, X-Wing, etc. — everything the solver can do without
     * backtracking.
     *
     * After this, any remaining technique check will find only techniques
     * that are genuinely needed (not simpler alternatives).
     */
    fun exhaustAllTechniques(board: Board) {
        val config = SolverConfig()
        var anyProgress = true

        while (anyProgress) {
            anyProgress = false

            // Phase 1: Apply all eliminators iteratively until stable
            var eliminatorProgress = true
            while (eliminatorProgress) {
                eliminatorProgress = false
                for (eliminator in config.eliminators) {
                    val before = board.countTotalCandidates()
                    val changed = eliminator.eliminate(board)
                    if (changed && board.countTotalCandidates() < before) {
                        eliminatorProgress = true
                        anyProgress = true
                    }
                }
            }

            // Phase 2: Fill any naked singles (cells with only one candidate)
            for (coord in Coord.all) {
                if (!board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 1) {
                    val value = board.candidateValues(coord).first()
                    board.markValue(coord, value)
                    anyProgress = true
                }
            }
        }
    }

    /**
     * Find a pointing pair (locked candidates).
     * A candidate appears in a box only within one row or one column,
     * so it can be eliminated from that row/col in other boxes.
     */
    private fun findPointingPair(board: Board): Hint? {
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                for (value in 1..9) {
                    val cells = mutableListOf<Coord>()
                    for (r in boxRow * 3 until (boxRow + 1) * 3) {
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            val coord = Coord(r, c)
                            if (!board.isConfirmed(coord) &&
                                value in board.candidateValues(coord)
                            ) {
                                cells.add(coord)
                            }
                        }
                    }
                    if (cells.size in 2..3) {
                        // Check if all cells are in the same row
                        val rows = cells.map { it.row }.toSet()
                        if (rows.size == 1) {
                            val row = rows.first()
                            // Check if candidate can be eliminated from that row in other boxes
                            for (col in 0..8) {
                                if (col / 3 == boxCol) continue // skip same box
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return Hint(
                                        coord = coord,
                                        value = value,
                                        technique = Technique.POINTING_PAIR,
                                        explanation = "Value $value in box (${boxRow + 1},${boxCol + 1}) " +
                                                "is restricted to row ${row + 1}. " +
                                                "Eliminate $value from row ${row + 1} in other boxes."
                                    )
                                }
                            }
                        }
                        // Check if all cells are in the same column
                        val cols = cells.map { it.col }.toSet()
                        if (cols.size == 1) {
                            val col = cols.first()
                            for (row in 0..8) {
                                if (row / 3 == boxRow) continue // skip same box
                                val coord = Coord(row, col)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return Hint(
                                        coord = coord,
                                        value = value,
                                        technique = Technique.POINTING_PAIR,
                                        explanation = "Value $value in box (${boxRow + 1},${boxCol + 1}) " +
                                                "is restricted to column ${col + 1}. " +
                                                "Eliminate $value from column ${col + 1} in other boxes."
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

    /**
     * Find a box/line reduction (locked candidates type 2).
     * A candidate in a row or column is restricted to one box,
     * so it can be eliminated from other rows/columns in that box.
     */
    private fun findBoxLineReduction(board: Board): Hint? {
        // Check rows for box/line reduction
        for (row in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        value in board.candidateValues(coord)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    // Check if all cells are in the same box
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        // Check if candidate can be eliminated from that box in other rows
                        for (r in boxRow * 3 until (boxRow + 1) * 3) {
                            if (r == row) continue // skip same row
                            for (c in boxCol * 3 until (boxCol + 1) * 3) {
                                val coord = Coord(r, c)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return Hint(
                                        coord = coord,
                                        value = value,
                                        technique = Technique.BOX_LINE_REDUCTION,
                                        explanation = "Value $value in row ${row + 1} " +
                                                "is restricted to box (${boxRow + 1},${boxCol + 1}). " +
                                                "Eliminate $value from other rows in this box."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check columns for box/line reduction
        for (col in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (row in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        value in board.candidateValues(coord)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    // Check if all cells are in the same box
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        // Check if candidate can be eliminated from that box in other columns
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            if (c == col) continue // skip same column
                            for (r in boxRow * 3 until (boxRow + 1) * 3) {
                                val coord = Coord(r, c)
                                if (!board.isConfirmed(coord) &&
                                    value in board.candidateValues(coord)
                                ) {
                                    return Hint(
                                        coord = coord,
                                        value = value,
                                        technique = Technique.BOX_LINE_REDUCTION,
                                        explanation = "Value $value in column ${col + 1} " +
                                                "is restricted to box (${boxRow + 1},${boxCol + 1}). " +
                                                "Eliminate $value from other columns in this box."
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
                            explanation = "Cells (${coord1.row + 1},${coord1.col + 1}) and (${coord2.row + 1},${coord2.col + 1}) " +
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
     * Find a naked triple.
     * Three cells in a group whose candidates are a subset of three values.
     */
    private fun findNakedTriple(board: Board): Hint? {
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
                            // Check if any other cell in the group has one of these candidates
                            for (other in unresolved) {
                                if (other == unresolved[i] || other == unresolved[j] || other == unresolved[k]) continue
                                val otherCandidates = board.candidateValues(other).toSet()
                                val overlap = otherCandidates.intersect(union)
                                if (overlap.isNotEmpty()) {
                                    return Hint(
                                        coord = other,
                                        value = overlap.first(),
                                        technique = Technique.NAKED_TRIPLE,
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

    /**
     * Find a hidden pair.
     * Two values that appear only in the same two cells within a group.
     */
    private fun findHiddenPair(board: Board): Hint? {
        for (coordGroup in CoordGroup.all) {
            // For each value, find which unresolved cells contain it
            val valueToCells = mutableMapOf<Int, MutableList<Coord>>()
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (coord in coordGroup.coords) {
                    if (!board.isConfirmed(coord) && value in board.candidateValues(coord)) {
                        cells.add(coord)
                    }
                }
                if (cells.size == 2) {
                    valueToCells[value] = cells
                }
            }

            // Check for pairs of values that share the same two cells
            val values = valueToCells.keys.toList()
            for (i in values.indices) {
                for (j in i + 1 until values.size) {
                    val v1 = values[i]
                    val v2 = values[j]
                    val cells1 = valueToCells[v1]!!.toSet()
                    val cells2 = valueToCells[v2]!!.toSet()

                    if (cells1 == cells2 && cells1.size == 2) {
                        val cells = cells1.toList()
                        // Check if either cell has more than 2 candidates (if so, hidden pair eliminates extras)
                        val extraCandidates1 = board.candidateValues(cells[0]).filter { it != v1 && it != v2 }
                        val extraCandidates2 = board.candidateValues(cells[1]).filter { it != v1 && it != v2 }

                        if (extraCandidates1.isNotEmpty()) {
                            return Hint(
                                coord = cells[0],
                                value = extraCandidates1.first(),
                                technique = Technique.HIDDEN_PAIR,
                                explanation = "Values $v1 and $v2 form a hidden pair in cells " +
                                        "(${cells[0].row + 1},${cells[0].col + 1}) and " +
                                        "(${cells[1].row + 1},${cells[1].col + 1}). " +
                                        "Other candidates can be eliminated from these cells."
                            )
                        }
                        if (extraCandidates2.isNotEmpty()) {
                            return Hint(
                                coord = cells[1],
                                value = extraCandidates2.first(),
                                technique = Technique.HIDDEN_PAIR,
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

    /**
     * Find a hidden triple.
     * Three values that appear only in the same three cells within a group.
     */
    private fun findHiddenTriple(board: Board): Hint? {
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

            // Try all combinations of 3 values
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
                                    return Hint(
                                        coord = cell,
                                        value = extraCandidates.first(),
                                        technique = Technique.HIDDEN_TRIPLE,
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

    /**
     * Find a Swordfish pattern.
     * Extension of X-Wing to 3 rows/columns.
     */
    private fun findSwordfish(board: Board): Hint? {
        for (value in 1..9) {
            val mask = Board.masks[value - 1]

            // Row-based Swordfish
            val rowToColumns = mutableMapOf<Int, Set<Int>>()
            for (row in 0..8) {
                val columns = mutableSetOf<Int>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        columns.add(col)
                    }
                }
                if (columns.size in 2..3) {
                    rowToColumns[row] = columns
                }
            }

            val rows = rowToColumns.keys.toList()
            for (i in 0 until rows.size - 2) {
                for (j in i + 1 until rows.size - 1) {
                    for (k in j + 1 until rows.size) {
                        val row1 = rows[i]
                        val row2 = rows[j]
                        val row3 = rows[k]
                        val cols1 = rowToColumns[row1]!!
                        val cols2 = rowToColumns[row2]!!
                        val cols3 = rowToColumns[row3]!!

                        if (cols1 == cols2 && cols2 == cols3) {
                            val swordfishCols = cols1.toList()
                            return Hint(
                                coord = Coord(row1, swordfishCols[0]),
                                value = value,
                                technique = Technique.SWORDFISH,
                                explanation = "Swordfish found! Value $value in rows ${row1 + 1}, ${row2 + 1}, ${row3 + 1} " +
                                        "appears only in columns ${swordfishCols.joinToString { (it + 1).toString() }}. " +
                                        "Eliminate $value from these columns in other rows."
                            )
                        }
                    }
                }
            }

            // Column-based Swordfish
            val colToRows = mutableMapOf<Int, Set<Int>>()
            for (col in 0..8) {
                val rows2 = mutableSetOf<Int>()
                for (row in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) && (board.candidatePattern(coord) and mask) != 0) {
                        rows2.add(row)
                    }
                }
                if (rows2.size in 2..3) {
                    colToRows[col] = rows2
                }
            }

            val cols = colToRows.keys.toList()
            for (i in 0 until cols.size - 2) {
                for (j in i + 1 until cols.size - 1) {
                    for (k in j + 1 until cols.size) {
                        val col1 = cols[i]
                        val col2 = cols[j]
                        val col3 = cols[k]
                        val rows1 = colToRows[col1]!!
                        val rows2 = colToRows[col2]!!
                        val rows3 = colToRows[col3]!!

                        if (rows1 == rows2 && rows2 == rows3) {
                            val swordfishRows = rows1.toList()
                            return Hint(
                                coord = Coord(swordfishRows[0], col1),
                                value = value,
                                technique = Technique.SWORDFISH,
                                explanation = "Swordfish found! Value $value in columns ${col1 + 1}, ${col2 + 1}, ${col3 + 1} " +
                                        "appears only in rows ${swordfishRows.joinToString { (it + 1).toString() }}. " +
                                        "Eliminate $value from these rows in other columns."
                            )
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Find an XY-Wing pattern.
     * Pivot {X,Y}, Wing1 {X,Z}, Wing2 {Y,Z} — eliminate Z from cells seeing both wings.
     */
    private fun findXYWing(board: Board): Hint? {
        // Find all cells with exactly 2 candidates
        val biValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 2
        }

        for (pivot in biValueCells) {
            val pivotCandidates = board.candidateValues(pivot).toList()
            if (pivotCandidates.size != 2) continue
            val x = pivotCandidates[0]
            val y = pivotCandidates[1]

            // Find wing cells that see the pivot
            val wingCells = biValueCells.filter { wing ->
                wing != pivot && seesEachOther(wing, pivot)
            }

            for (wing1 in wingCells) {
                val w1Candidates = board.candidateValues(wing1).toSet()
                if (w1Candidates.size != 2) continue

                // Wing1 must share X or Y with pivot and have Z
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

                    // Both wings must share the same Z
                    if (z1 != z2) continue

                    // Find cells that see both wings
                    for (coord in Coord.all) {
                        if (coord == pivot || coord == wing1 || coord == wing2) continue
                        if (board.isConfirmed(coord)) continue
                        if (!seesEachOther(coord, wing1) || !seesEachOther(coord, wing2)) continue
                        if (z1 in board.candidateValues(coord)) {
                            return Hint(
                                coord = coord,
                                value = z1,
                                technique = Technique.XY_WING,
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

    /**
     * Find an XYZ-Wing pattern.
     * Pivot {X,Y,Z}, Wing1 {X,Z}, Wing2 {Y,Z} — eliminate Z from cells seeing all three.
     */
    private fun findXYZWWing(board: Board): Hint? {
        // Find cells with exactly 3 candidates (potential pivots)
        val triValueCells = Coord.all.filter { coord ->
            !board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 3
        }

        for (pivot in triValueCells) {
            val pivotCands = board.candidateValues(pivot).toSet()
            if (pivotCands.size != 3) continue

            // Find bi-value wing cells that see the pivot
            val wings = Coord.all.filter { coord ->
                coord != pivot &&
                        !board.isConfirmed(coord) &&
                        board.candidatePattern(coord).countOneBits() == 2 &&
                        seesEachOther(coord, pivot)
            }

            for (wing1 in wings) {
                val w1Cands = board.candidateValues(wing1).toSet()
                if (pivotCands.containsAll(w1Cands)) continue

                for (wing2 in wings) {
                    if (wing2 == wing1) continue
                    val w2Cands = board.candidateValues(wing2).toSet()
                    if (pivotCands.containsAll(w2Cands)) continue

                    // Find the common Z
                    val zCandidates = w1Cands.intersect(w2Cands)
                    for (z in zCandidates) {
                        if (z !in pivotCands) continue
                        if (w1Cands + w2Cands != pivotCands) continue

                        // Find cells that see all three (pivot, wing1, wing2)
                        for (coord in Coord.all) {
                            if (coord == pivot || coord == wing1 || coord == wing2) continue
                            if (board.isConfirmed(coord)) continue
                            if (!seesEachOther(coord, pivot)) continue
                            if (!seesEachOther(coord, wing1)) continue
                            if (!seesEachOther(coord, wing2)) continue
                            if (z in board.candidateValues(coord)) {
                                return Hint(
                                    coord = coord,
                                    value = z,
                                    technique = Technique.XYZ_WING,
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

    /**
     * Check if two cells see each other (same row, column, or region).
     */
    private fun seesEachOther(coord1: Coord, coord2: Coord): Boolean {
        return coord1.row == coord2.row ||
                coord1.col == coord2.col ||
                sameRegion(coord1, coord2)
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
