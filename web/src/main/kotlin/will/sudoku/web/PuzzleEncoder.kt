package will.sudoku.web

import java.util.Base64

/**
 * Utility for encoding and decoding Sudoku puzzles in URL-safe format.
 * Uses base64url encoding to make puzzle strings safe for URL parameters.
 */
object PuzzleEncoder {

    private val base64Encoder = Base64.getUrlEncoder().withoutPadding()
    private val base64Decoder = Base64.getUrlDecoder()

    /**
     * Encodes an 81-character puzzle string to base64url format.
     * Empty cells should be represented as '0' (not '.') for URL encoding.
     *
     * @param puzzle The 81-character puzzle string (1-9 for values, 0 or . for empty)
     * @return URL-safe base64 encoded string
     * @throws IllegalArgumentException if puzzle is not 81 characters
     */
    fun encode(puzzle: String): String {
        require(puzzle.length == 81) {
            "Puzzle must be 81 characters, got ${puzzle.length}"
        }

        // Normalize: replace '.' with '0' for consistent encoding
        val normalized = puzzle.replace('.', '0')
        val bytes = normalized.toByteArray(Charsets.UTF_8)
        return base64Encoder.encodeToString(bytes)
    }

    /**
     * Decodes a base64url encoded puzzle string back to the original puzzle format.
     * Decoded puzzle uses '.' for empty cells (standard board format).
     *
     * @param encoded The base64url encoded puzzle string
     * @return The 81-character puzzle string (1-9 for values, . for empty)
     * @throws IllegalArgumentException if encoded string is invalid
     */
    fun decode(encoded: String): String {
        try {
            val bytes = base64Decoder.decode(encoded)
            val decoded = String(bytes, Charsets.UTF_8)

            require(decoded.length == 81) {
                "Decoded puzzle must be 81 characters, got ${decoded.length}"
            }

            // Validate characters
            require(decoded.all { it == '0' || it in '1'..'9' }) {
                "Decoded puzzle contains invalid characters: $decoded"
            }

            // Convert back to standard format: '0' -> '.'
            return decoded.replace('0', '.')
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid encoded puzzle: ${e.message}", e)
        }
    }

    /**
     * Validates if a puzzle string can be encoded.
     *
     * @param puzzle The puzzle string to validate
     * @return true if valid, false otherwise
     */
    fun isValidPuzzle(puzzle: String): Boolean {
        if (puzzle.length != 81) return false
        return puzzle.all { it == '.' || it == '0' || it in '1'..'9' }
    }
}
