package will.sudoku.solver

/**
 * Batch-generates puzzles and catalogs which solving techniques each requires.
 *
 * Finds ideal example puzzles for tutorials by:
 * 1. Generating N puzzles via PuzzleGenerator
 * 2. Solving each with step-by-step recording
 * 3. Recording which techniques were used
 * 4. (Optional) Checking if a technique is *required* by solving without it
 *
 * ## Usage
 * ```kotlin
 * // Find 1000 puzzles that use X-Wing
 * val results = PuzzleCataloger.catalog(count = 1000, targetTechnique = StepType.X_WING)
 * results.forEach { println("${it.puzzle} — first X-Wing at step ${it.firstUseStep}") }
 *
 * // Find puzzles requiring a specific technique (can't solve without it)
 * val required = PuzzleCataloger.catalog(count = 500, targetTechnique = StepType.SIMPLE_COLORING, requireTechnique = true)
 *
 * // Catalog ALL techniques across 1000 puzzles
 * val all = PuzzleCataloger.catalog(count = 1000)
 * ```
 *
 * @see Solver for the underlying solver
 * @see StepRecorder for step recording
 * @see PuzzleGenerator for puzzle generation
 */
object PuzzleCataloger {

    data class CatalogEntry(
        val puzzle: String,
        val techniques: Set<String>,  // canonical technique names
        val stepCount: Int,
        val firstUseStep: Int,
        val solved: Boolean
    )

    data class CatalogResult(
        val totalGenerated: Int,
        val totalSolved: Int,
        val byTechnique: Map<String, List<CatalogEntry>>,  // canonical name → entries
        val unmatched: Int
    )

    /**
     * Technique StepTypes that represent actual elimination methods
     * (not actions like CELL_FILLED or status like PUZZLE_SOLVED).
     */
    private val TECHNIQUE_TYPES: Set<StepType> = setOf(
        StepType.SIMPLE_ELIMINATION,
        StepType.NAKED_PAIR,
        StepType.NAKED_TRIPLE,
        StepType.HIDDEN_SINGLE,
        StepType.HIDDEN_PAIR,
        StepType.HIDDEN_TRIPLE,
        StepType.X_WING,
        StepType.SWORDFISH,
        StepType.XY_WING,
        StepType.NAKED_SUBSET,
        StepType.HIDDEN_SUBSET,
        StepType.TECHNIQUE_APPLIED
    )

    /**
     * Map raw eliminator display names to canonical names for matching.
     */
    private val TECHNIQUE_NAME_ALIASES: Map<String, String> = mapOf(
        "SimpleColoring" to "SIMPLE_COLORING",
        "UniqueRectangles" to "UNIQUE_RECTANGLES",
        "Swordfish" to "SWORDFISH",
        "XWing" to "X_WING",
        "XYWing" to "XY_WING",
        "XYZWing" to "XYZ_WING",
        "WWing" to "W_WING",
        "ForcingChains" to "FORCING_CHAINS",
        "ALSXZ" to "ALS_XZ",
        "FrankenFish" to "FRANKEN_FISH",
        "MutantFish" to "MUTANT_FISH",
        "DeathBlossom" to "DEATH_BLOSSOM",
        "GroupCandidate" to "NAKED_SUBSET",
        "HiddenSubset" to "HIDDEN_SUBSET",
        "Simple Elimination" to "SIMPLE_ELIMINATION",
        "Exclusion" to "SIMPLE_ELIMINATION"
    )

    /**
     * Resolve a technique name from either a StepType or raw eliminator name.
     * Returns a canonical name for grouping.
     */
    private fun resolveTechniqueName(stepType: StepType, explanation: String): String {
        // If it's a specific StepType (not TECHNIQUE_APPLIED), use it
        if (stepType != StepType.TECHNIQUE_APPLIED && stepType in TECHNIQUE_TYPES) {
            return stepType.name
        }
        // For TECHNIQUE_APPLIED, try to extract the eliminator name from the explanation
        // Explanation format: "TechniqueName: ..." or "TechniqueName eliminated ..."
        val colonIdx = explanation.indexOf(':')
        if (colonIdx > 0) {
            val rawName = explanation.substring(0, colonIdx).trim()
            return TECHNIQUE_NAME_ALIASES[rawName] ?: rawName
        }
        return stepType.name
    }

    /**
     * Generate and catalog puzzles.
     *
     * @param count Number of puzzles to generate (max with --stop-after-first)
     * @param difficulty Difficulty level (harder = more techniques used)
     * @param targetTechnique If set, only collect puzzles that use this technique
     * @param requireTechnique If true, verify the puzzle CANNOT be solved without the target technique
     * @param stopAfterFirst If true, stop as soon as a matching puzzle is found
     * @param maxAttempts Maximum puzzles to generate when stopAfterFirst is true (default: 10000)
     * @param startSeed Starting seed for reproducibility
     * @return CatalogResult with entries grouped by technique
     */
    fun catalog(
        count: Int = 1000,
        difficulty: DifficultyRater.Level = DifficultyRater.Level.EXPERT,
        targetTechnique: String? = null,
        requireTechnique: Boolean = false,
        stopAfterFirst: Boolean = false,
        maxAttempts: Int = 10000,
        startSeed: Long = 1
    ): CatalogResult {
        val byTechnique = mutableMapOf<String, MutableList<CatalogEntry>>()
        var totalSolved = 0
        var unmatched = 0

        // Resolve target technique to eliminator classes for the require check
        val targetStepType = targetTechnique?.let { name ->
            StepType.entries.find {
                it.name.equals(name, ignoreCase = true) ||
                it.displayName.equals(name, ignoreCase = true)
            }
        }
        val solver = Solver()
        val solverWithoutTarget = if (requireTechnique && targetStepType != null) {
            Solver(SolverConfig.withoutTechnique(targetStepType))
        } else null

        val effectiveCount = if (stopAfterFirst) maxAttempts else count

        for (i in 0 until effectiveCount) {
            val seed = startSeed + i

            // Generate puzzle
            val board = try {
                PuzzleGenerator.generate(difficulty, seed = seed)
            } catch (e: Exception) {
                continue // skip failed generation
            }

            // Solve with step recording
            val recorder = StepRecorder()
            recorder.setBoardState(boardToString(board))
            val solution = solver.solve(board, recorder)

            if (solution == null) {
                unmatched++
                continue
            }

            totalSolved++
            val progress = recorder.progress

            // Resolve technique names from steps
            val techniques = progress.steps
                .filter { it.stepType in TECHNIQUE_TYPES }
                .map { resolveTechniqueName(it.stepType, it.explanation) }
                .toSet()

            // Find first use step for target technique
            val firstUseStep = if (targetTechnique != null) {
                progress.steps.indexOfFirst {
                    it.stepType in TECHNIQUE_TYPES &&
                    resolveTechniqueName(it.stepType, it.explanation).equals(targetTechnique, ignoreCase = true)
                }.let { if (it >= 0) it + 1 else -1 }
            } else -1

            // If targeting a specific technique, skip if not found
            if (targetTechnique != null && techniques.none { it.equals(targetTechnique, ignoreCase = true) }) {
                continue
            }

            // If requireTechnique, check puzzle can't be solved without target
            if (requireTechnique && solverWithoutTarget != null) {
                val canSolveWithout = solverWithoutTarget.solve(board) != null
                if (canSolveWithout) continue
            }

            val entry = CatalogEntry(
                puzzle = boardToString(board),
                techniques = techniques,
                stepCount = progress.steps.size,
                firstUseStep = firstUseStep,
                solved = true
            )

            // Add to all technique buckets
            for (tech in techniques) {
                byTechnique.getOrPut(tech) { mutableListOf() }.add(entry)
            }

            // Early termination: stop as soon as a matching puzzle is found
            if (stopAfterFirst) {
                return CatalogResult(
                    totalGenerated = i + 1,
                    totalSolved = totalSolved,
                    byTechnique = byTechnique,
                    unmatched = unmatched
                )
            }
        }

        return CatalogResult(
            totalGenerated = if (stopAfterFirst) effectiveCount else count,
            totalSolved = totalSolved,
            byTechnique = byTechnique,
            unmatched = unmatched
        )
    }

    /**
     * Convert board to 81-char string (0 for empty).
     */
    private fun boardToString(board: Board): String {
        return buildString {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val coord = Coord(row, col)
                    if (board.isConfirmed(coord)) {
                        append(board.value(coord))
                    } else {
                        append('0')
                    }
                }
            }
        }
    }
}
