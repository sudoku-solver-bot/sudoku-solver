package will.sudoku.solver

import java.util.*

/**
 * Generates shareable links and codes for puzzles.
 */
object PuzzleSharing {
    
    /**
     * Generate a short shareable code for a puzzle.
     * Uses Base64 URL-safe encoding for compact representation.
     */
    fun generateShareCode(puzzle: String): String {
        // Validate puzzle format
        val normalized = puzzle.filter { it.isDigit() }
        require(normalized.length == 81) { "Puzzle must be 81 digits" }
        
        // Encode to Base64
        val bytes = normalized.toByteArray(Charsets.UTF_8)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    /**
     * Decode a share code back to puzzle string.
     */
    fun decodeShareCode(code: String): String {
        return try {
            val bytes = Base64.getUrlDecoder().decode(code)
            val puzzle = String(bytes, Charsets.UTF_8)
            
            // Validate decoded puzzle
            require(puzzle.length == 81) { "Invalid puzzle length" }
            require(puzzle.all { it.isDigit() }) { "Invalid puzzle format" }
            
            puzzle
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid share code: ${e.message}")
        }
    }
    
    /**
     * Generate a shareable URL for a puzzle.
     */
    fun generateShareUrl(baseUrl: String, puzzle: String): String {
        val code = generateShareCode(puzzle)
        return "${baseUrl.trimEnd('/')}/puzzle/$code"
    }
    
    /**
     * Generate social sharing text with puzzle link.
     */
    fun generateSocialText(puzzle: String, difficulty: SolverConfig.DifficultyLevel? = null): SocialShareText {
        val clueCount = puzzle.count { it != '0' }
        val difficultyEmoji = when (difficulty) {
            SolverConfig.DifficultyLevel.EASY -> "🟢"
            SolverConfig.DifficultyLevel.MEDIUM -> "🟡"
            SolverConfig.DifficultyLevel.HARD -> "🟠"
            SolverConfig.DifficultyLevel.EXPERT -> "🔴"
            null -> "🧩"
        }
        
        val difficultyName = difficulty?.name?.lowercase()?.capitalize() ?: "Unknown"
        
        return SocialShareText(
            title = "Sudoku Puzzle",
            shortText = "Try this Sudoku puzzle! $difficultyEmoji ($clueCount clues)",
            mediumText = "Can you solve this Sudoku puzzle? $difficultyEmoji $clueCount clues - think you can crack it?",
            longText = """
                🧩 Sudoku Challenge! 🧩
                
                Difficulty: $difficultyName $difficultyEmoji
                Clues: $clueCount
                
                Think you can solve it? Give it a try!
                
                #Sudoku #Puzzle #Challenge
            """.trimIndent(),
            hashtags = listOf("Sudoku", "Puzzle", "Challenge", "BrainTeaser")
        )
    }
    
    /**
     * Generate QR code data URL for a puzzle.
     * Returns a simple data URL that can be used in img tags.
     * Note: For production, use a proper QR code library.
     */
    fun generateQrDataUrl(puzzle: String): String {
        // In production, this would generate an actual QR code
        // For now, return a placeholder
        val code = generateShareCode(puzzle)
        return "data:text/plain;charset=utf-8,$code"
    }
}

/**
 * Social sharing text variations.
 */
data class SocialShareText(
    val title: String,
    val shortText: String,      // Twitter (280 chars)
    val mediumText: String,     // Facebook/LinkedIn
    val longText: String,       // Email/Blog
    val hashtags: List<String>
) {
    val hashtagText: String
        get() = hashtags.joinToString(" ") { "#$it" }
    
    val twitterText: String
        get() = "$shortText $hashtagText".take(280)
}
