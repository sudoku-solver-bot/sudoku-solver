package will.sudoku.solver

import java.time.LocalDate
import java.time.ZoneId

/**
 * Daily challenge puzzle with tracking.
 */
data class DailyChallenge(
    val date: LocalDate,
    val difficulty: DifficultyLevel,
    val puzzle: String,
    val solution: String,
    val seed: Long  // For reproducible generation
)

/**
 * User's daily challenge completion.
 */
data class DailyChallengeCompletion(
    val date: LocalDate,
    val userId: String,
    val completed: Boolean,
    val timeSeconds: Int?,
    val hintsUsed: Int,
    val perfectSolve: Boolean  // No hints
)

/**
 * User's streak information.
 */
data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: LocalDate?,
    val totalCompleted: Int
) {
    /**
     * Check if streak is still active (completed yesterday or today).
     */
    fun isStreakActive(today: LocalDate = LocalDate.now()): Boolean {
        if (currentStreak == 0) return false
        val lastDate = lastCompletedDate ?: return false
        val yesterday = today.minusDays(1)
        return lastDate == today || lastDate == yesterday
    }
    
    /**
     * Get encouraging streak message.
     */
    fun getStreakMessage(): String {
        return when {
            currentStreak >= 30 -> "Incredible! 30+ day streak! You're a Sudoku master! 🔥🔥🔥"
            currentStreak >= 14 -> "Amazing! 2-week streak! You're on fire! 🔥🔥"
            currentStreak >= 7 -> "One week streak! Keep it up! 🔥"
            currentStreak >= 3 -> "3-day streak! You're building a habit! ⭐"
            currentStreak >= 1 -> "Great start! Come back tomorrow! 🌟"
            else -> "Start your streak today! 🎯"
        }
    }
}

/**
 * Daily challenge system with streaks and history.
 */
class DailyChallengeSystem(
    private val generator: PuzzleGenerator = PuzzleGenerator()
) {
    // Store generated daily challenges (in production, use database)
    private val challenges = mutableMapOf<LocalDate, DailyChallenge>()
    
    /**
     * Get or generate today's daily challenge.
     * Uses date as seed for reproducible puzzles.
     */
    fun getTodayChallenge(timezone: ZoneId = ZoneId.of("Asia/Hong_Kong")): DailyChallenge {
        val today = LocalDate.now(timezone)
        return getChallenge(today)
    }
    
    /**
     * Get challenge for a specific date.
     */
    fun getChallenge(date: LocalDate): DailyChallenge {
        return challenges.getOrPut(date) {
            generateChallenge(date)
        }
    }
    
    /**
     * Generate a daily challenge for a date.
     * Uses date as seed for reproducibility.
     */
    private fun generateChallenge(date: LocalDate): DailyChallenge {
        // Generate seed from date (YYYYMMDD as number)
        val seed = date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
        
        // Determine difficulty based on day of week
        val difficulty = when (date.dayOfWeek.value) {
            1, 2 -> DifficultyLevel.EASY      // Mon-Tue
            3, 4 -> DifficultyLevel.MEDIUM    // Wed-Thu
            5, 6 -> DifficultyLevel.HARD      // Fri-Sat
            7 -> DifficultyLevel.EXPERT       // Sunday = hardest
            else -> DifficultyLevel.MEDIUM
        }
        
        // Generate puzzle with deterministic seed
        val seededGenerator = PuzzleGenerator(random = java.util.Random(seed))
        val puzzle = seededGenerator.generate(difficulty)
        
        // Solve it to get solution
        val solver = Solver()
        val solution = solver.solve(puzzle) ?: puzzle
        
        return DailyChallenge(
            date = date,
            difficulty = difficulty,
            puzzle = puzzle.toString(),
            solution = solution.toString(),
            seed = seed
        )
    }
    
    /**
     * Calculate streak from completion history.
     */
    fun calculateStreak(
        completions: List<DailyChallengeCompletion>,
        today: LocalDate = LocalDate.now()
    ): StreakInfo {
        if (completions.isEmpty()) {
            return StreakInfo(
                currentStreak = 0,
                longestStreak = 0,
                lastCompletedDate = null,
                totalCompleted = 0
            )
        }
        
        val sortedDates = completions
            .filter { it.completed }
            .map { it.date }
            .sortedDescending()
            .distinct()
        
        if (sortedDates.isEmpty()) {
            return StreakInfo(0, 0, null, 0)
        }
        
        var currentStreak = 0
        var longestStreak = 0
        var streak = 0
        var lastDate: LocalDate? = null
        
        for (date in sortedDates) {
            if (lastDate == null) {
                // First date
                val yesterday = today.minusDays(1)
                if (date == today || date == yesterday) {
                    streak = 1
                    currentStreak = 1
                }
            } else {
                // Check if consecutive
                val expectedPrev = lastDate.minusDays(1)
                if (date == expectedPrev) {
                    streak++
                    if (streak > longestStreak) {
                        longestStreak = streak
                    }
                } else {
                    // Streak broken
                    if (streak > longestStreak) {
                        longestStreak = streak
                    }
                    streak = 1
                }
            }
            lastDate = date
        }
        
        // Final check for current streak
        if (streak > 0 && lastDate != null) {
            val yesterday = today.minusDays(1)
            if (lastDate == today || lastDate == yesterday) {
                currentStreak = streak
            } else {
                currentStreak = 0
            }
        }
        
        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, streak),
            lastCompletedDate = sortedDates.firstOrNull(),
            totalCompleted = sortedDates.size
        )
    }
    
    /**
     * Get available challenges for past N days.
     */
    fun getRecentChallenges(days: Int = 7, timezone: ZoneId = ZoneId.of("Asia/Hong_Kong")): List<DailyChallenge> {
        val today = LocalDate.now(timezone)
        return (0 until days).map { daysAgo ->
            getChallenge(today.minusDays(daysAgo.toLong()))
        }.reversed()
    }
}
