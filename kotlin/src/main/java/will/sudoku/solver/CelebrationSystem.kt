package will.sudoku.solver

/**
 * Celebration triggered when user completes a puzzle.
 */
data class Celebration(
    val type: CelebrationType,
    val intensity: Intensity,
    val duration: Int,  // milliseconds
    val sound: String?,
    val particles: Int,
    val message: String,
    val emoji: List<String>
)

/**
 * Types of celebrations.
 */
enum class CelebrationType {
    CONFETTI,           // Standard confetti burst
    FIREWORKS,          // Multiple explosions
    STARS,              // Star burst pattern
    TROPHY,             // Trophy animation
    BADGE_UNLOCK,       // Achievement badge reveal
    LEVEL_UP,           // Level increase celebration
    PERFECT_GAME,       // No hints used
    SPEED_DEMON,        // Fast solve
    DAILY_COMPLETE,     // Daily challenge done
    STREAK_MILESTONE    // Streak achievement
}

/**
 * Intensity of celebration (affects particle count, duration).
 */
enum class Intensity {
    MINIMAL,    // Small acknowledgment
    STANDARD,   // Normal celebration
    EPIC,       // Big achievement
    LEGENDARY   // Major milestone
}

/**
 * Celebration trigger conditions.
 */
data class CelebrationTrigger(
    val type: CelebrationType,
    val condition: CelebrationCondition
)

data class CelebrationCondition(
    val puzzleSolved: Boolean = false,
    val perfectGame: Boolean = false,  // No hints
    val fastSolve: Boolean = false,     // <1 minute
    val dailyChallenge: Boolean = false,
    val streakDays: Int = 0,
    val levelUp: Boolean = false,
    val achievementUnlock: Boolean = false
)

/**
 * Celebration system for gamification.
 */
class CelebrationSystem {
    
    /**
     * Determine appropriate celebration for a completion.
     */
    fun getCelebration(condition: CelebrationCondition): Celebration {
        return when {
            // Multiple conditions = EPIC celebration
            condition.perfectGame && condition.fastSolve -> {
                createLegendaryCelebration(
                    "AMAZING! Perfect AND Fast! 🌟",
                    CelebrationType.FIREWORKS
                )
            }
            
            // Level up
            condition.levelUp -> {
                createEpicCelebration(
                    "LEVEL UP! 🎊",
                    CelebrationType.LEVEL_UP
                )
            }
            
            // Achievement unlock
            condition.achievementUnlock -> {
                createEpicCelebration(
                    "NEW ACHIEVEMENT! 🏆",
                    CelebrationType.BADGE_UNLOCK
                )
            }
            
            // Streak milestones
            condition.streakDays >= 30 -> {
                createLegendaryCelebration(
                    "30 DAY STREAK! 🔥🔥🔥",
                    CelebrationType.STREAK_MILESTONE
                )
            }
            condition.streakDays >= 7 -> {
                createEpicCelebration(
                    "WEEK STREAK! 🔥",
                    CelebrationType.STREAK_MILESTONE
                )
            }
            condition.streakDays >= 3 -> {
                createStandardCelebration(
                    "3 DAY STREAK! ⭐",
                    CelebrationType.STREAK_MILESTONE
                )
            }
            
            // Perfect game (no hints)
            condition.perfectGame -> {
                createEpicCelebration(
                    "PERFECT! No hints! 💎",
                    CelebrationType.PERFECT_GAME
                )
            }
            
            // Fast solve
            condition.fastSolve -> {
                createStandardCelebration(
                    "SPEED DEMON! ⚡",
                    CelebrationType.SPEED_DEMON
                )
            }
            
            // Daily challenge
            condition.dailyChallenge -> {
                createStandardCelebration(
                    "Daily challenge complete! ✅",
                    CelebrationType.DAILY_COMPLETE
                )
            }
            
            // Standard puzzle solve
            condition.puzzleSolved -> {
                createMinimalCelebration(
                    "Great job! 🎉"
                )
            }
            
            else -> {
                createMinimalCelebration(
                    "Nice try! 👍"
                )
            }
        }
    }
    
    // Celebration factory methods
    private fun createMinimalCelebration(message: String): Celebration {
        return Celebration(
            type = CelebrationType.CONFETTI,
            intensity = Intensity.MINIMAL,
            duration = 1000,
            sound = "pop.mp3",
            particles = 20,
            message = message,
            emoji = listOf("🎉")
        )
    }
    
    private fun createStandardCelebration(message: String, type: CelebrationType): Celebration {
        return Celebration(
            type = type,
            intensity = Intensity.STANDARD,
            duration = 2000,
            sound = "success.mp3",
            particles = 50,
            message = message,
            emoji = listOf("🎉", "⭐", "🎊")
        )
    }
    
    private fun createEpicCelebration(message: String, type: CelebrationType): Celebration {
        return Celebration(
            type = type,
            intensity = Intensity.EPIC,
            duration = 3000,
            sound = "epic.mp3",
            particles = 100,
            message = message,
            emoji = listOf("🎉", "⭐", "🎊", "✨", "💫")
        )
    }
    
    private fun createLegendaryCelebration(message: String, type: CelebrationType): Celebration {
        return Celebration(
            type = type,
            intensity = Intensity.LEGENDARY,
            duration = 5000,
            sound = "legendary.mp3",
            particles = 200,
            message = message,
            emoji = listOf("🎉", "🌟", "🎊", "✨", "💫", "🔥", "🏆", "👑")
        )
    }
    
    /**
     * Get random encouraging messages for kids.
     */
    fun getRandomEncouragement(): String {
        val messages = listOf(
            "Keep going! You've got this! 💪",
            "Almost there! Don't give up! 🌟",
            "You're doing great! 🎯",
            "Think carefully - you can do it! 🧠",
            "Take your time, no rush! ⏰",
            "Every puzzle teaches you something new! 📚",
            "Believe in yourself! ✨",
            "You're getting better with every puzzle! 📈"
        )
        return messages.random()
    }
    
    /**
     * Get celebration sound file based on type.
     */
    fun getSoundFile(type: CelebrationType): String {
        return when (type) {
            CelebrationType.CONFETTI -> "pop.mp3"
            CelebrationType.FIREWORKS -> "fireworks.mp3"
            CelebrationType.STARS -> "sparkle.mp3"
            CelebrationType.TROPHY -> "fanfare.mp3"
            CelebrationType.BADGE_UNLOCK -> "achievement.mp3"
            CelebrationType.LEVEL_UP -> "levelup.mp3"
            CelebrationType.PERFECT_GAME -> "perfect.mp3"
            CelebrationType.SPEED_DEMON -> "speed.mp3"
            CelebrationType.DAILY_COMPLETE -> "daily.mp3"
            CelebrationType.STREAK_MILESTONE -> "streak.mp3"
        }
    }
}
