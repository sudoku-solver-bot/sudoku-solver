package will.sudoku.solver

/**
 * Validates Sudoku puzzles for correctness and solution uniqueness.
 *
 * ## Validation Checks
 * 1. **Structure validity**: No duplicate values in rows, columns, or regions
 * 2. **Solution uniqueness**: Puzzle has exactly one solution
 *
 * ## Usage
 * ```kotlin
 * val result = PuzzleValidator.validate(board)
 * if (result.isValid) {
 *     println("Valid puzzle")
 *     if (result.hasUniqueSolution) {
 *         println("Has unique solution")
 *     }
 * } else {
 *     println("Invalid: ${result.errors}")
 * }
 * ```
 *
 * @see ValidationResult for detailed results
 */
object PuzzleValidator {

    /**
     * Result of puzzle validation.
     *
     * @property isValid Whether the puzzle is structurally valid (no conflicts)
     * @property hasUniqueSolution Whether the puzzle has exactly one solution
     * @property solutionCount Number of solutions found (0, 1, or 2+)
     * @property errors List of validation errors found
     */
    data class ValidationResult(
        val isValid: Boolean,
        val hasUniqueSolution: Boolean,
        val solutionCount: Int,
        val errors: List<ValidationError>
    ) {
        /**
         * Human-readable summary of the validation result.
         */
        val summary: String
            get() = when {
                !isValid -> "Invalid puzzle: ${errors.firstOrNull()?.message ?: "Unknown error"}"
                solutionCount == 0 -> "Valid puzzle but no solution exists"
                solutionCount == 1 -> "Valid puzzle with unique solution"
                else -> "Valid puzzle but has multiple solutions ($solutionCount+ found)"
            }

        override fun toString(): String = buildString {
            appendLine("Validation Result:")
            appendLine("  Valid: $isValid")
            appendLine("  Unique Solution: $hasUniqueSolution")
            appendLine("  Solution Count: ${if (solutionCount >= 2) "2+" else solutionCount}")
            if (errors.isNotEmpty()) {
                appendLine("  Errors:")
                errors.forEach { appendLine("    - ${it.message}") }
            }
        }
    }

    /**
     * A specific validation error.
     *
     * @property type The type of error
     * @property message Human-readable error message
     * @property coords Coordinates involved in the error (if applicable)
     */
    data class ValidationError(
        val type: ErrorType,
        val message: String,
        val coords: List<Coord> = emptyList()
    )

    /**
     * Types of validation errors.
     */
    enum class ErrorType {
        DUPLICATE_IN_ROW,
        DUPLICATE_IN_COLUMN,
        DUPLICATE_IN_REGION,
        INVALID_VALUE,
        EMPTY_PUZZLE
    }

    /**
     * Validate a puzzle for structural correctness and solution uniqueness.
     *
     * @param board The puzzle board to validate
     * @param checkUniqueness Whether to check for unique solution (slower)
     * @return Validation result with details
     */
    fun validate(board: Board, checkUniqueness: Boolean = true): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check for structural validity
        val structuralErrors = checkStructuralValidity(board)
        errors.addAll(structuralErrors)

        // If structurally invalid, don't check for solutions
        if (errors.isNotEmpty()) {
            return ValidationResult(
                isValid = false,
                hasUniqueSolution = false,
                solutionCount = 0,
                errors = errors
            )
        }

        // Check for solution uniqueness if requested
        if (checkUniqueness) {
            val solutionCount = countSolutions(board, maxCount = 2, config = SolverConfig())
            
            return ValidationResult(
                isValid = true,
                hasUniqueSolution = solutionCount == 1,
                solutionCount = solutionCount,
                errors = emptyList()
            )
        }

        return ValidationResult(
            isValid = true,
            hasUniqueSolution = false, // Not checked
            solutionCount = -1, // Unknown
            errors = emptyList()
        )
    }

    /**
     * Quick validation - only checks structural validity (fast).
     *
     * @param board The puzzle board to validate
     * @return True if structurally valid, false otherwise
     */
    fun isValid(board: Board): Boolean {
        return checkStructuralValidity(board).isEmpty()
    }

    /**
     * Check if a puzzle has exactly one solution.
     *
     * @param board The puzzle board to check
     * @return True if puzzle has unique solution
     */
    fun hasUniqueSolution(board: Board): Boolean {
        if (!isValid(board)) return false
        return countSolutions(board, maxCount = 2, config = SolverConfig()) == 1
    }

    /**
     * Check structural validity of the puzzle.
     *
     * Looks for duplicate values in rows, columns, and regions.
     */
    private fun checkStructuralValidity(board: Board): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Check each row
        for (row in 0..8) {
            val duplicates = findDuplicatesInGroup(
                (0..8).map { col -> Coord(row, col) },
                board
            )
            if (duplicates != null) {
                errors.add(ValidationError(
                    type = ErrorType.DUPLICATE_IN_ROW,
                    message = "Duplicate value ${duplicates.first} in row ${row + 1}",
                    coords = duplicates.second
                ))
            }
        }

        // Check each column
        for (col in 0..8) {
            val duplicates = findDuplicatesInGroup(
                (0..8).map { row -> Coord(row, col) },
                board
            )
            if (duplicates != null) {
                errors.add(ValidationError(
                    type = ErrorType.DUPLICATE_IN_COLUMN,
                    message = "Duplicate value ${duplicates.first} in column ${col + 1}",
                    coords = duplicates.second
                ))
            }
        }

        // Check each 3x3 region
        for (regionRow in 0..2) {
            for (regionCol in 0..2) {
                val coords = mutableListOf<Coord>()
                for (row in regionRow * 3 until (regionRow + 1) * 3) {
                    for (col in regionCol * 3 until (regionCol + 1) * 3) {
                        coords.add(Coord(row, col))
                    }
                }
                val duplicates = findDuplicatesInGroup(coords, board)
                if (duplicates != null) {
                    val regionNum = regionRow * 3 + regionCol + 1
                    errors.add(ValidationError(
                        type = ErrorType.DUPLICATE_IN_REGION,
                        message = "Duplicate value ${duplicates.first} in region $regionNum",
                        coords = duplicates.second
                    ))
                }
            }
        }

        return errors
    }

    /**
     * Find duplicate values in a group of cells.
     *
     * @return Pair of (duplicate value, list of coords) or null if no duplicates
     */
    private fun findDuplicatesInGroup(coords: List<Coord>, board: Board): Pair<Int, List<Coord>>? {
        val valueToCoords = mutableMapOf<Int, MutableList<Coord>>()

        for (coord in coords) {
            val value = board.value(coord)
            if (value != 0) {
                valueToCoords.getOrPut(value) { mutableListOf() }.add(coord)
            }
        }

        // Return first duplicate found
        for ((value, coordList) in valueToCoords) {
            if (coordList.size > 1) {
                return Pair(value, coordList)
            }
        }

        return null
    }

    /**
     * Count solutions for a puzzle (up to maxCount).
     *
     * Uses a modified solver that stops after finding maxCount solutions.
     */
    private fun countSolutions(board: Board, maxCount: Int, config: SolverConfig): Int {
        var count = 0

        fun solveWithCount(currentBoard: Board): Boolean {
            if (count >= maxCount) return true // Stop early

            if (!currentBoard.isValid()) return false
            if (currentBoard.isSolved()) {
                count++
                return count >= maxCount
            }

            val unresolvedCoord = currentBoard.unresolvedCoord() ?: return false

            for (candidateValue in currentBoard.candidateValues(unresolvedCoord)) {
                val newBoard = currentBoard.copy()
                newBoard.markValue(unresolvedCoord, candidateValue)

                // Apply eliminators from config
                for (eliminator in config.eliminators) {
                    eliminator.eliminate(newBoard)
                }

                if (solveWithCount(newBoard)) {
                    if (count >= maxCount) return true
                }
            }

            return false
        }

        // Make a copy and apply initial eliminators from config
        val workingBoard = board.copy()
        for (eliminator in config.eliminators) {
            eliminator.eliminate(workingBoard)
        }

        solveWithCount(workingBoard)
        return count
    }
}
