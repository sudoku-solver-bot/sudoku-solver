package will.sudoku.web

import java.util.Base64

/**
 * Encodes and decodes Sudoku puzzle strings for URL-safe usage.
 * Uses Base64URL encoding (no +, /, or = characters).
 */
object PuzzleEncoder {

    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    /**
     * Encode a puzzle string to a URL-safe base64 string.
     * Accepts '.' or '0' for empty cells, '1'-'9' for filled cells.
     */
    fun encode(puzzle: String): String {
        require(puzzle.length == 81) { "Puzzle must be exactly 81 characters, got ${puzzle.length}" }
        return encoder.encodeToString(puzzle.toByteArray())
    }

    /**
     * Decode a URL-safe base64 string back to a puzzle string.
     * Returns a string using '.' for empty cells and '1'-'9' for filled cells.
     */
    fun decode(encoded: String): String {
        val bytes = try {
            decoder.decode(encoded)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid base64 input", e)
        }
        val decoded = String(bytes)
        require(decoded.length == 81) { "Decoded puzzle must be exactly 81 characters, got ${decoded.length}" }
        // Normalize zeros to dots
        return decoded.map { if (it == '0') '.' else it }.joinToString("")
    }

    /**
     * Check if a string is a valid puzzle representation.
     */
    fun isValidPuzzle(puzzle: String): Boolean {
        if (puzzle.length != 81) return false
        return puzzle.all { it in '1'..'9' || it == '.' || it == '0' }
    }
}
