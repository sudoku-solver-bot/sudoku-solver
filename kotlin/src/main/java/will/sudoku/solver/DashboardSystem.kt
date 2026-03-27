package will.sudoku.solver

import java.time.LocalDate

/**
 * Student progress report for parent/teacher dashboard.
 */
data class StudentReport(
    val studentId: String,
    val studentName: String,
    val period: ReportPeriod,
    val stats: StudentStats,
    val achievements: List<AchievementSummary>,
    val progress: LearningProgressSummary,
    val recommendations: List<String>
)

/**
 * Time period for report.
 */
data class ReportPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val daysIncluded: Int
)

/**
 * Student statistics for reporting.
 */
data class StudentStats(
    val puzzlesSolved: Int,
    val puzzlesAttempted: Int,
    val averageSolveTimeSeconds: Int,
    val fastestSolveSeconds: Int,
    val hintsUsedTotal: Int,
    val perfectGames: Int,
    val dailyStreak: Int,
    val longestStreak: Int,
    val tutorialsCompleted: Int,
    val totalPlayTimeMinutes: Int
) {
    val successRate: Double
        get() = if (puzzlesAttempted > 0) {
            puzzlesSolved.toDouble() / puzzlesAttempted
        } else 0.0
    
    val averageHintsPerPuzzle: Double
        get() = if (puzzlesSolved > 0) {
            hintsUsedTotal.toDouble() / puzzlesSolved
        } else 0.0
}

/**
 * Summary of earned achievements.
 */
data class AchievementSummary(
    val id: String,
    val name: String,
    val icon: String,
    val earnedDate: LocalDate,
    val category: String
)

/**
 * Learning progress summary.
 */
data class LearningProgressSummary(
    val currentLevel: Int,
    val totalXP: Int,
    val techniquesLearned: List<String>,
    val techniquesInProgress: List<String>,
    val nextMilestone: String,
    val overallProgress: Int  // Percentage
)

/**
 * Classroom overview for teachers.
 */
data class ClassroomOverview(
    val classroomId: String,
    val classroomName: String,
    val studentCount: Int,
    val topPerformers: List<StudentSummary>,
    val strugglingStudents: List<StudentSummary>,
    val classStats: ClassStats,
    val recentActivity: List<ActivityEvent>
)

/**
 * Brief student summary.
 */
data class StudentSummary(
    val studentId: String,
    val studentName: String,
    val level: Int,
    val puzzlesSolved: Int,
    val currentStreak: Int,
    val lastActiveDate: LocalDate?,
    val status: StudentStatus
)

/**
 * Student status indicator.
 */
enum class StudentStatus {
    EXCELLING,    // High performance
    ON_TRACK,     // Meeting expectations
    NEEDS_HELP,   // Struggling
    INACTIVE      // Haven't played recently
}

/**
 * Aggregate classroom statistics.
 */
data class ClassStats(
    val totalPuzzlesSolved: Int,
    val averageSolveTimeSeconds: Int,
    val classSuccessRate: Double,
    val averageStreak: Double,
    val mostPopularDifficulty: String,
    val totalAchievementsEarned: Int
)

/**
 * Recent activity event.
 */
data class ActivityEvent(
    val timestamp: Long,
    val studentName: String,
    val eventType: String,
    val description: String
)

/**
 * Parent/Teacher dashboard system.
 */
class DashboardSystem {
    
    /**
     * Generate student report for a time period.
     */
    fun generateStudentReport(
        studentId: String,
        studentName: String,
        days: Int = 30
    ): StudentReport {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        // In production, fetch from database
        // Placeholder data
        val stats = StudentStats(
            puzzlesSolved = 45,
            puzzlesAttempted = 48,
            averageSolveTimeSeconds = 180,
            fastestSolveSeconds = 65,
            hintsUsedTotal = 32,
            perfectGames = 12,
            dailyStreak = 5,
            longestStreak = 12,
            tutorialsCompleted = 3,
            totalPlayTimeMinutes = 240
        )
        
        val achievements = listOf(
            AchievementSummary(
                id = "first-solve",
                name = "Problem Solver",
                icon = "🧩",
                earnedDate = LocalDate.now().minusDays(25),
                category = "SOLVING"
            ),
            AchievementSummary(
                id = "daily-player",
                name = "Daily Player",
                icon = "📅",
                earnedDate = LocalDate.now().minusDays(10),
                category = "STREAK"
            )
        )
        
        val progress = LearningProgressSummary(
            currentLevel = 3,
            totalXP = 850,
            techniquesLearned = listOf(
                "Single Candidate",
                "Single Position"
            ),
            techniquesInProgress = listOf(
                "Candidate Lines",
                "Naked Pairs"
            ),
            nextMilestone = "Complete 'Medium' difficulty puzzle without hints",
            overallProgress = 35
        )
        
        val recommendations = generateRecommendations(stats, progress)
        
        return StudentReport(
            studentId = studentId,
            studentName = studentName,
            period = ReportPeriod(startDate, endDate, days),
            stats = stats,
            achievements = achievements,
            progress = progress,
            recommendations = recommendations
        )
    }
    
    /**
     * Generate classroom overview.
     */
    fun generateClassroomOverview(
        classroomId: String,
        classroomName: String
    ): ClassroomOverview {
        // In production, query database for all students in classroom
        val students = listOf(
            StudentSummary(
                studentId = "student1",
                studentName = "Emma",
                level = 5,
                puzzlesSolved = 120,
                currentStreak = 15,
                lastActiveDate = LocalDate.now(),
                status = StudentStatus.EXCELLING
            ),
            StudentSummary(
                studentId = "student2",
                studentName = "Lucas",
                level = 3,
                puzzlesSolved = 45,
                currentStreak = 5,
                lastActiveDate = LocalDate.now().minusDays(1),
                status = StudentStatus.ON_TRACK
            ),
            StudentSummary(
                studentId = "student3",
                studentName = "Sophie",
                level = 2,
                puzzlesSolved = 20,
                currentStreak = 0,
                lastActiveDate = LocalDate.now().minusDays(5),
                status = StudentStatus.NEEDS_HELP
            )
        )
        
        val topPerformers = students
            .filter { it.status == StudentStatus.EXCELLING || it.status == StudentStatus.ON_TRACK }
            .sortedByDescending { it.puzzlesSolved }
            .take(5)
        
        val strugglingStudents = students
            .filter { it.status == StudentStatus.NEEDS_HELP }
        
        val classStats = ClassStats(
            totalPuzzlesSolved = 185,
            averageSolveTimeSeconds = 195,
            classSuccessRate = 0.87,
            averageStreak = 6.7,
            mostPopularDifficulty = "MEDIUM",
            totalAchievementsEarned = 45
        )
        
        val recentActivity = listOf(
            ActivityEvent(
                timestamp = System.currentTimeMillis() - 3600000,
                studentName = "Emma",
                eventType = "achievement",
                description = "Earned 'Puzzle Master' achievement"
            ),
            ActivityEvent(
                timestamp = System.currentTimeMillis() - 7200000,
                studentName = "Lucas",
                eventType = "daily",
                description = "Completed daily challenge"
            )
        )
        
        return ClassroomOverview(
            classroomId = classroomId,
            classroomName = classroomName,
            studentCount = students.size,
            topPerformers = topPerformers,
            strugglingStudents = strugglingStudents,
            classStats = classStats,
            recentActivity = recentActivity
        )
    }
    
    /**
     * Generate personalized recommendations.
     */
    private fun generateRecommendations(
        stats: StudentStats,
        progress: LearningProgressSummary
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Check for low success rate
        if (stats.successRate < 0.7) {
            recommendations.add(
                "Try easier puzzles to build confidence. Focus on EASY difficulty for now."
            )
        }
        
        // Check for hint over-reliance
        if (stats.averageHintsPerPuzzle > 3) {
            recommendations.add(
                "Practice solving puzzles with fewer hints. Try to use at most 2 hints per puzzle."
            )
        }
        
        // Check streak
        if (stats.dailyStreak == 0) {
            recommendations.add(
                "Start a daily streak! Completing puzzles daily helps build problem-solving habits."
            )
        } else if (stats.dailyStreak < 7) {
            recommendations.add(
                "Keep the ${stats.dailyStreak}-day streak going! Try to reach 7 days."
            )
        }
        
        // Check tutorials
        if (stats.tutorialsCompleted < 3) {
            recommendations.add(
                "Complete more tutorials to learn advanced techniques like ${progress.techniquesInProgress.firstOrNull() ?: "Candidate Lines"}."
            )
        }
        
        // Check solve time
        if (stats.averageSolveTimeSeconds > 300) {
            recommendations.add(
                "Focus on accuracy over speed. Take time to think through each move."
            )
        } else if (stats.averageSolveTimeSeconds < 90) {
            recommendations.add(
                "Great speed! Now try solving puzzles with fewer hints."
            )
        }
        
        // Always encourage
        if (recommendations.isEmpty()) {
            recommendations.add(
                "Excellent progress! Keep challenging yourself with harder puzzles."
            )
        }
        
        return recommendations
    }
}
