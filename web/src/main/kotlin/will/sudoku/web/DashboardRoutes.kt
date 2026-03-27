package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class StudentReportRequest(
    val studentId: String,
    val studentName: String,
    val days: Int = 30
)

@Serializable
data class StudentReportResponse(
    val studentName: String,
    val period: PeriodResponse,
    val stats: StatsResponse,
    val achievements: List<AchievementSummaryResponse>,
    val progress: ProgressSummaryResponse,
    val recommendations: List<String>
)

@Serializable
data class PeriodResponse(
    val startDate: String,
    val endDate: String,
    val daysIncluded: Int
)

@Serializable
data class StatsResponse(
    val puzzlesSolved: Int,
    val puzzlesAttempted: Int,
    val successRate: Double,
    val averageSolveTimeSeconds: Int,
    val fastestSolveSeconds: Int,
    val hintsUsedTotal: Int,
    val averageHintsPerPuzzle: Double,
    val perfectGames: Int,
    val dailyStreak: Int,
    val longestStreak: Int,
    val tutorialsCompleted: Int,
    val totalPlayTimeMinutes: Int
)

@Serializable
data class AchievementSummaryResponse(
    val name: String,
    val icon: String,
    val earnedDate: String,
    val category: String
)

@Serializable
data class ProgressSummaryResponse(
    val currentLevel: Int,
    val totalXP: Int,
    val techniquesLearned: List<String>,
    val techniquesInProgress: List<String>,
    val nextMilestone: String,
    val overallProgress: Int
)

@Serializable
data class ClassroomOverviewResponse(
    val classroomName: String,
    val studentCount: Int,
    val topPerformers: List<StudentSummaryResponse>,
    val strugglingStudents: List<StudentSummaryResponse>,
    val classStats: ClassStatsResponse,
    val recentActivity: List<ActivityEventResponse>
)

@Serializable
data class StudentSummaryResponse(
    val studentName: String,
    val level: Int,
    val puzzlesSolved: Int,
    val currentStreak: Int,
    val lastActiveDate: String?,
    val status: String
)

@Serializable
data class ClassStatsResponse(
    val totalPuzzlesSolved: Int,
    val averageSolveTimeSeconds: Int,
    val classSuccessRate: Double,
    val averageStreak: Double,
    val mostPopularDifficulty: String,
    val totalAchievementsEarned: Int
)

@Serializable
data class ActivityEventResponse(
    val timestamp: Long,
    val studentName: String,
    val eventType: String,
    val description: String
)

fun Route.dashboardRoutes() {
    val dashboardSystem = DashboardSystem()

    post("/dashboard/student/report") {

        val request = call.receive<StudentReportRequest>()
        
        val report = dashboardSystem.generateStudentReport(
            studentId = request.studentId,
            studentName = request.studentName,
            days = request.days
        )
        
        call.respond(
            StudentReportResponse(
                studentName = report.studentName,
                period = PeriodResponse(
                    startDate = report.period.startDate.toString(),
                    endDate = report.period.endDate.toString(),
                    daysIncluded = report.period.daysIncluded
                ),
                stats = StatsResponse(
                    puzzlesSolved = report.stats.puzzlesSolved,
                    puzzlesAttempted = report.stats.puzzlesAttempted,
                    successRate = report.stats.successRate,
                    averageSolveTimeSeconds = report.stats.averageSolveTimeSeconds,
                    fastestSolveSeconds = report.stats.fastestSolveSeconds,
                    hintsUsedTotal = report.stats.hintsUsedTotal,
                    averageHintsPerPuzzle = report.stats.averageHintsPerPuzzle,
                    perfectGames = report.stats.perfectGames,
                    dailyStreak = report.stats.dailyStreak,
                    longestStreak = report.stats.longestStreak,
                    tutorialsCompleted = report.stats.tutorialsCompleted,
                    totalPlayTimeMinutes = report.stats.totalPlayTimeMinutes
                ),
                achievements = report.achievements.map { achievement ->
                    AchievementSummaryResponse(
                        name = achievement.name,
                        icon = achievement.icon,
                        earnedDate = achievement.earnedDate.toString(),
                        category = achievement.category
                    )
                },
                progress = ProgressSummaryResponse(
                    currentLevel = report.progress.currentLevel,
                    totalXP = report.progress.totalXP,
                    techniquesLearned = report.progress.techniquesLearned,
                    techniquesInProgress = report.progress.techniquesInProgress,
                    nextMilestone = report.progress.nextMilestone,
                    overallProgress = report.progress.overallProgress
                ),
                recommendations = report.recommendations
            )
        )
    }
    
    get("/dashboard/classroom/{classroomId}") {

        val classroomId = call.parameters["classroomId"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing classroomId")
        )
        
        val overview = dashboardSystem.generateClassroomOverview(
            classroomId = classroomId,
            classroomName = "Room 101"
        )
        
        call.respond(
            ClassroomOverviewResponse(
                classroomName = overview.classroomName,
                studentCount = overview.studentCount,
                topPerformers = overview.topPerformers.map { student ->
                    StudentSummaryResponse(
                        studentName = student.studentName,
                        level = student.level,
                        puzzlesSolved = student.puzzlesSolved,
                        currentStreak = student.currentStreak,
                        lastActiveDate = student.lastActiveDate?.toString(),
                        status = student.status.name
                    )
                },
                strugglingStudents = overview.strugglingStudents.map { student ->
                    StudentSummaryResponse(
                        studentName = student.studentName,
                        level = student.level,
                        puzzlesSolved = student.puzzlesSolved,
                        currentStreak = student.currentStreak,
                        lastActiveDate = student.lastActiveDate?.toString(),
                        status = student.status.name
                    )
                },
                classStats = ClassStatsResponse(
                    totalPuzzlesSolved = overview.classStats.totalPuzzlesSolved,
                    averageSolveTimeSeconds = overview.classStats.averageSolveTimeSeconds,
                    classSuccessRate = overview.classStats.classSuccessRate,
                    averageStreak = overview.classStats.averageStreak,
                    mostPopularDifficulty = overview.classStats.mostPopularDifficulty,
                    totalAchievementsEarned = overview.classStats.totalAchievementsEarned
                ),
                recentActivity = overview.recentActivity.map { event ->
                    ActivityEventResponse(
                        timestamp = event.timestamp,
                        studentName = event.studentName,
                        eventType = event.eventType,
                    )
                }
            )
        )
    }
}
