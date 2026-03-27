package will.sudoku.web

import io.ktor.http.*
import kotlinx.serialization.Serializable

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
 * Comprehensive puzzle validation utility.
 */
object PuzzleValidator {
    
    /**
     * Validates puzzle string for correctness.
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
        
        // Check for obvious conflicts in rows
        val rowConflict = findRowConflict(puzzle)
        if (rowConflict != null) {
            return ValidationResult(
                valid = false,
                errorCode = "ROW_CONFLICT",
                errorMessage = "Row ${rowConflict.first + 1} contains duplicate value '${rowConflict.second}'",
                errorDetails = mapOf(
                    "row" to (rowConflict.first + 1).toString(),
                    "value" to rowConflict.second.toString()
                )
            )
        }
        
        // Check for obvious conflicts in columns
        val colConflict = findColumnConflict(puzzle)
        if (colConflict != null) {
            return ValidationResult(
                valid = false,
                errorCode = "COLUMN_CONFLICT",
                errorMessage = "Column ${colConflict.first + 1} contains duplicate value '${colConflict.second}'",
                errorDetails = mapOf(
                    "column" to (colConflict.first + 1).toString(),
                    "value" to colConflict.second.toString()
                )
            )
        }
        
        // Check for obvious conflicts in 3x3 regions
        val regionConflict = findRegionConflict(puzzle)
        if (regionConflict != null) {
            return ValidationResult(
                valid = false,
                errorCode = "REGION_CONFLICT",
                errorMessage = "3x3 region contains duplicate value '${regionConflict.second}'",
                errorDetails = mapOf(
                    "region" to (regionConflict.first + 1).toString(),
                    "value" to regionConflict.second.toString()
                )
            )
        }
        
        // All validations passed
        return ValidationResult(valid = true)
    }
    
    /**
     * Find duplicate in any row.
     * @return Pair of (row index, duplicate digit) or null if no conflict
     */
    private fun findRowConflict(puzzle: String): Pair<Int, Char>? {
        for (row in 0 until 9) {
            val digits = mutableMapOf<Char, Int>()
            for (col in 0 until 9) {
                val char = puzzle[row * 9 + col]
                if (char in '1'..'9') {
                    if (digits.containsKey(char)) {
                        return Pair(row, char)
                    }
                    digits[char] = col
                }
            }
        }
        return null
    }
    
    /**
     * Find duplicate in any column.
     * @return Pair of (column index, duplicate digit) or null if no conflict
     */
    private fun findColumnConflict(puzzle: String): Pair<Int, Char>? {
        for (col in 0 until 9) {
            val digits = mutableMapOf<Char, Int>()
            for (row in 0 until 9) {
                val char = puzzle[row * 9 + col]
                if (char in '1'..'9') {
                    if (digits.containsKey(char)) {
                        return Pair(col, char)
                    }
                    digits[char] = row
                }
            }
        }
        return null
    }
    
    /**
     * Find duplicate in any 3x3 region.
     * @return Pair of (region index, duplicate digit) or null if no conflict
     */
    private fun findRegionConflict(puzzle: String): Pair<Int, Char>? {
        for (regionRow in 0 until 3) {
            for (regionCol in 0 until 3) {
                val digits = mutableMapOf<Char, Int>()
                val regionIndex = regionRow * 3 + regionCol
                
                for (localRow in 0 until 3) {
                    for (localCol in 0 until 3) {
                        val row = regionRow * 3 + localRow
                        val col = regionCol * 3 + localCol
                        val char = puzzle[row * 9 + col]
                        
                        if (char in '1'..'9') {
                            if (digits.containsKey(char)) {
                                return Pair(regionIndex, char)
                            }
                            digits[char] = row * 9 + col
                        }
                    }
                }
            }
        }
        return null
    }
    
    /**
     * Get HTTP status code for validation error.
     */
    fun getHttpStatusCode(errorCode: String): HttpStatusCode {
        return when (errorCode) {
            "NULL_INPUT", "INVALID_LENGTH", "INVALID_CHARACTERS" -> HttpStatusCode.BadRequest
            "EMPTY_PUZZLE", "TOO_FEW_CLUES" -> HttpStatusCode.BadRequest
            "DUPLICATE_VALUES", "ROW_CONFLICT", "COLUMN_CONFLICT", "REGION_CONFLICT" -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.BadRequest
        }
    }
}
