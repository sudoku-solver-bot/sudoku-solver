package will.sudoku.solver

import will.sudoku.solver.detectors.BoxLineReductionDetector
import will.sudoku.solver.detectors.HiddenPairDetector
import will.sudoku.solver.detectors.HiddenSingleDetector
import will.sudoku.solver.detectors.HiddenTripleDetector
import will.sudoku.solver.detectors.NakedPairDetector
import will.sudoku.solver.detectors.NakedSingleDetector
import will.sudoku.solver.detectors.NakedTripleDetector
import will.sudoku.solver.detectors.PointingPairDetector
import will.sudoku.solver.detectors.SwordfishDetector
import will.sudoku.solver.detectors.XYWingDetector
import will.sudoku.solver.detectors.XWingDetector
import will.sudoku.solver.detectors.XYZWingDetector

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
            appendLine("Hint: Look at cell (${coord.row + 1}, ${coord.col + 1})")
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
        var lastHiddenSingle: Hint? = null
        if (exhaustHiddenSingles) {
            lastHiddenSingle = applyHiddenSinglesUntilStable(workingBoard)
        }

        // Step 2.5: Exhaust intermediate techniques (pointing pairs, box/line reductions)
        // before checking advanced techniques. Without this, harder puzzles may mask
        // their first non-trivial technique behind pointing pairs/box-line reductions
        // that need to be resolved first (Refs #610).
        if (exhaustHiddenSingles) {
            applyPointingPairsUntilStable(workingBoard)
            applyBoxLineReductionsUntilStable(workingBoard)
        }

        // Step 2.6: If the board is now solved, return the last hidden single found.
        // Without this, puzzles solvable entirely by hidden singles would fall through
        // to the generic "Scanning" fallback (bug #224).
        if (workingBoard.isSolved() && lastHiddenSingle != null) {
            return lastHiddenSingle
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
    internal fun applyHiddenSinglesUntilStable(board: Board): Hint? {
        var lastHint: Hint? = null
        var foundAny = true
        while (foundAny) {
            foundAny = false
            // Find and apply hidden singles one at a time
            var foundOne: Boolean
            do {
                foundOne = false
                val hint = hiddenSingleDetector.detect(board)
                if (hint != null) {
                    board.markValue(hint.coord, hint.value)
                    lastHint = hint
                    foundAny = true
                    foundOne = true
                    // Re-run constraint propagation after each hidden single
                    // to propagate its effects and potentially reveal new
                    // naked singles or hidden singles
                    applyBasicElimination(board)
                }
            } while (foundOne)
        }
        return lastHint
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
     * Apply box/line reductions to advance the board before checking for advanced techniques.
     *
     * Iteratively finds box/line reductions, eliminates the locked candidates, and re-runs
     * constraint propagation. Continues until no more box/line reductions are found.
     */
    internal fun applyBoxLineReductionsUntilStable(board: Board) {
        var foundAny = true
        while (foundAny) {
            foundAny = false
            val eliminations = findBoxLineReductionEliminations(board)
            if (eliminations.isNotEmpty()) {
                foundAny = true
                for ((coord, value) in eliminations) {
                    board.eraseCandidatePattern(coord, Board.masks[value - 1])
                }
                applyBasicElimination(board)
            }
        }
    }

    /**
     * Find all box/line reduction eliminations on the board.
     * Returns a list of (coord, value) pairs to eliminate.
     *
     * Box/line reduction: When a candidate value in a row (or column) is restricted
     * to cells within a single box, that value can be eliminated from the rest of that box.
     */
    private fun findBoxLineReductionEliminations(board: Board): List<Pair<Coord, Int>> {
        val eliminations = mutableListOf<Pair<Coord, Int>>()

        // Check each row for box/line reductions
        for (row in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (col in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        board.candidateValues(coord).contains(value)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        for (r in boxRow * 3 until (boxRow + 1) * 3) {
                            if (r == row) continue
                            for (c in boxCol * 3 until (boxCol + 1) * 3) {
                                val coord = Coord(r, c)
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

        // Check each column for box/line reductions
        for (col in 0..8) {
            for (value in 1..9) {
                val cells = mutableListOf<Coord>()
                for (row in 0..8) {
                    val coord = Coord(row, col)
                    if (!board.isConfirmed(coord) &&
                        board.candidateValues(coord).contains(value)
                    ) {
                        cells.add(coord)
                    }
                }
                if (cells.size in 2..3) {
                    val boxes = cells.map { Pair(it.row / 3, it.col / 3) }.toSet()
                    if (boxes.size == 1) {
                        val (boxRow, boxCol) = boxes.first()
                        for (c in boxCol * 3 until (boxCol + 1) * 3) {
                            if (c == col) continue
                            for (r in boxRow * 3 until (boxRow + 1) * 3) {
                                val coord = Coord(r, c)
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
    private val nakedSingleDetector = NakedSingleDetector()
    private val hiddenSingleDetector = HiddenSingleDetector()
    private val pointingPairDetector = PointingPairDetector()
    private val boxLineReductionDetector = BoxLineReductionDetector()
    private val nakedPairDetector = NakedPairDetector()
    private val nakedTripleDetector = NakedTripleDetector()
    private val hiddenPairDetector = HiddenPairDetector()
    private val hiddenTripleDetector = HiddenTripleDetector()
    private val xWingDetector = XWingDetector()
    private val swordfishDetector = SwordfishDetector()
    private val xyWingDetector = XYWingDetector()
    private val xyzWingDetector = XYZWingDetector()

    private fun detectTechnique(board: Board, technique: Technique): Hint? {
        return when (technique) {
            Technique.NAKED_SINGLE -> nakedSingleDetector.detect(board)
            Technique.HIDDEN_SINGLE -> hiddenSingleDetector.detect(board)
            Technique.POINTING_PAIR -> pointingPairDetector.detect(board)
            Technique.BOX_LINE_REDUCTION -> boxLineReductionDetector.detect(board)
            Technique.NAKED_PAIR -> nakedPairDetector.detect(board)
            Technique.NAKED_TRIPLE -> nakedTripleDetector.detect(board)
            Technique.HIDDEN_PAIR -> hiddenPairDetector.detect(board)
            Technique.HIDDEN_TRIPLE -> hiddenTripleDetector.detect(board)
            Technique.X_WING -> xWingDetector.detect(board)
            Technique.SWORDFISH -> swordfishDetector.detect(board)
            Technique.XY_WING -> xyWingDetector.detect(board)
            Technique.XYZ_WING -> xyzWingDetector.detect(board)
            Technique.W_WING -> findTechniqueViaEliminator(board, Technique.W_WING, WWingCandidateEliminator())
            Technique.SIMPLE_COLORING -> findTechniqueViaEliminator(board, Technique.SIMPLE_COLORING, SimpleColoringCandidateEliminator())
            Technique.UNIQUE_RECTANGLE -> findTechniqueViaEliminator(board, Technique.UNIQUE_RECTANGLE, UniqueRectanglesCandidateEliminator())
            Technique.ALS_XZ -> findTechniqueViaEliminator(board, Technique.ALS_XZ, ALSXZCandidateEliminator())
            Technique.FRANKEN_FISH -> findTechniqueViaEliminator(board, Technique.FRANKEN_FISH, FrankenFishCandidateEliminator())
            Technique.MUTANT_FISH -> findTechniqueViaEliminator(board, Technique.MUTANT_FISH, MutantFishCandidateEliminator())
            Technique.DEATH_BLOSSOM -> findTechniqueViaEliminator(board, Technique.DEATH_BLOSSOM, DeathBlossomCandidateEliminator())
            Technique.FORCING_CHAINS -> findTechniqueViaEliminator(board, Technique.FORCING_CHAINS, ForcingChainsCandidateEliminator())
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
}
