package will.sudoku.web

import io.ktor.http.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.Board
import will.sudoku.solver.BoardReader

/**
 * Validation result for puzzle input.
 */
@Serializable
data class ValidationResult(
    val valid: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val errorDetails: Map<String, String>? = null
)

/**
 * API-focused puzzle validation utility.
 * Delegates structural validation to backend PuzzleValidator.
 */
object PuzzleValidator {
    
    /**
     * Validates puzzle string for API input.
     * 
     * @param puzzle The 81-character puzzle string
     * @return ValidationResult with details if invalid
     */
    fun validate(puzzle: String?): ValidationResult {
        // Null check
        if (puzzle == null) {
            return ValidationResult(
                valid = false,
                errorCode = "NULL_INPUT",
                errorMessage = "Puzzle input cannot be null"
            )
        }
        
        // Length check
        if (puzzle.length != 81) {
            return ValidationResult(
                valid = false,
                errorCode = "INVALID_LENGTH",
                errorMessage = "Puzzle must be exactly 81 characters",
                errorDetails = mapOf(
                    "expected" to "81",
                    "actual" to puzzle.length.toString()
                )
            )
        }
        
        // Character validation
        val invalidChars = puzzle.filter { it !in '0'..'9' && it != '.' }
        if (invalidChars.isNotEmpty()) {
            return ValidationResult(
                valid = false,
                errorCode = "INVALID_CHARACTERS",
                errorMessage = "Puzzle contains invalid characters. Only digits 0-9 and '.' are allowed",
                errorDetails = mapOf(
                    "invalidCharacters" to invalidChars.take(10).toList().toString(),
                    "positions" to invalidChars.mapIndexedNotNull { idx, char ->
                        if (char !in '0'..'9' && char != '.') idx.toString() else null
                    }.take(10).joinToString(",")
                )
            )
        }
        
        // Check for all empty puzzle
        val filledCount = puzzle.count { it in '1'..'9' }
        if (filledCount == 0) {
            return ValidationResult(
                valid = false,
                errorCode = "EMPTY_PUZZLE",
                errorMessage = "Puzzle has no filled cells (all dots). At least one clue is required"
            )
        }
        
        // Check for too few clues (less than 17 is mathematically impossible to have unique solution)
        if (filledCount < 17) {
            return ValidationResult(
                valid = false,
                errorCode = "TOO_FEW_CLUES",
                errorMessage = "Puzzle has too few clues for a unique solution. Minimum 17 clues required, found $filledCount",
                errorDetails = mapOf(
                    "minimum" to "17",
                    "actual" to filledCount.toString()
                )
            )
        }
        
        // Check for all same number (obvious error)
        val digitCounts = mutableMapOf<Char, Int>()
        puzzle.filter { it in '1'..'9' }.forEach { digit ->
            digitCounts[digit] = digitCounts.getOrDefault(digit, 0) + 1
        }
        
        // Check if any digit appears more than 9 times
        digitCounts.forEach { (digit, count) ->
            if (count > 9) {
                return ValidationResult(
                    valid = false,
                    errorCode = "DUPLICATE_VALUES",
                    errorMessage = "Digit '$digit' appears $count times. Maximum allowed is 9",
                    errorDetails = mapOf(
                        "digit" to digit.toString(),
                        "count" to count.toString(),
                        "maximum" to "9"
                    )
                )
            }
        }
        
        // Delegate structural validation to backend
        try {
            val board = BoardReader.readBoard(puzzle)
            val backendValidation = will.sudoku.solver.PuzzleValidator.isValid(board)
            
            if (!backendValidation) {
                // Backend found structural issues (row/column/region conflicts)
                // Return generic error (backend doesn't provide specific error messages yet)
                return ValidationResult(
                    valid = false,
                    errorCode = "STRUCTURAL_CONFLICT",
                    errorMessage = "Puzzle contains duplicate values in a row, column, or 3x3 region"
                )
            }
        } catch (e: Exception) {
            return ValidationResult(
                valid = false,
                errorCode = "VALIDATION_ERROR",
                errorMessage = "Error validating puzzle: ${e.message}"
            )
        }
        
        // All validations passed
        return ValidationResult(valid = true)
    }
    
    /**
     * Get HTTP status code for validation error.
     */
    fun getHttpStatusCode(errorCode: String): HttpStatusCode {
        return when (errorCode) {
            "NULL_INPUT", "INVALID_LENGTH", "INVALID_CHARACTERS" -> HttpStatusCode.BadRequest
            "EMPTY_PUZZLE", "TOO_FEW_CLUES" -> HttpStatusCode.BadRequest
            "DUPLICATE_VALUES", "STRUCTURAL_CONFLICT" -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.BadRequest
        }
    }
}
