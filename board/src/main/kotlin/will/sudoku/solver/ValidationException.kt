package will.sudoku.solver

/**
 * Exception thrown when board validation fails.
 *
 * @property message Description of the validation error
 * @property input The invalid input that caused the error
 */
class ValidationException(message: String, val input: String? = null) : RuntimeException(message) {
    override fun toString(): String = "Validation failed: ${if (input != null) "for input '$input'" else ""} - $message"
}
