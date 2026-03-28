package will.sudoku.solver

/**
 * Celebration triggered when user completes a puzzle.
 */
data class Celebration(
    val type: CelebrationType,
    val intensity: String,
    val duration: Int,
    val sound: String?,
    val message: String,
    val emoji: List<String>
)

/**
 * Types of celebrations.
 */
enum class CelebrationType {
    CONFETTI,
    FIREWORKS,
    STARS,
    TROPHY,
    LEVEL_UP,
    PERFECT_GAME,
    SPEED_DEMON,
    DAILY_COMPLETE,
    STREAK_MILESTONE
}

/**
 * Celebration system for kids.
 */
class CelebrationSystem {
    
    fun getCelebration(solved: Boolean, perfect: Boolean = false, fast: Boolean = false): Celebration {
        return when {
            perfect && fast -> Celebration(
                type = CelebrationType.FIREWORKS,
                intensity = "LEGENDARY",
                duration = 3000,
                sound = "epic_win.mp3",
                message = "🌟 AMAZING! Perfect AND Fast! You're a Sudoku Master! 🌟",
                emoji = listOf("🎉", "⭐", "🏆", "💫", "🌟")
            )
            perfect -> Celebration(
                type = CelebrationType.STARS,
                intensity = "EPIC",
                duration = 2000,
                sound = "perfect.mp3",
                message = "⭐ Perfect Game! No hints needed! ⭐",
                emoji = listOf("⭐", "🌟", "✨", "💫")
            )
            fast -> Celebration(
                type = CelebrationType.CONFETTI,
                intensity = "STANDARD",
                duration = 1500,
                sound = "fast_solve.mp3",
                message = "🚀 Speed Demon! That was fast!",
                emoji = listOf("🚀", "⚡", "💨")
            )
            else -> Celebration(
                type = CelebrationType.CONFETTI,
                intensity = "STANDARD",
                duration = 1500,
                sound = "solve.mp3",
                message = "🎉 Great job! Puzzle solved! 🎉",
                emoji = listOf("🎉", "✨", "👏")
            )
        }
    }
}
