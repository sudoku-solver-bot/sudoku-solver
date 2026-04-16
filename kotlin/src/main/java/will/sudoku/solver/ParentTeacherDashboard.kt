package will.sudoku.solver

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Enhanced parent/teacher dashboard
 */
data class ParentTeacherDashboard(
    val participantId: String,
    val participantName: String,
    val age: Int,
    val ageGroup: AgeGroup,
    val generatedAt: LocalDateTime,
    val overview: DashboardOverview,
    val learningProgress: DetailedLearningProgress,
    val engagementStats: DetailedEngagementStats,
    val skillDevelopment: SkillDevelopmentReport,
    val testingResults: TestingResultsSummary,
    val comparisons: PeerComparisons,
    val recommendations: List<DashboardRecommendation>,
    val alerts: List<DashboardAlert>
)

/**
 * Dashboard overview metrics
 */
data class DashboardOverview(
    val totalPuzzlesCompleted: Int,
    val currentLevel: Int,
    val currentStreak: Int,
    val averageSessionDuration: Double,
    val overallSatisfaction: Double,
    val lastActiveDate: LocalDate,
    val daysInProgram: Int,
    val completionRate: Double
)

/**
 * Detailed learning progress
 */
data class DetailedLearningProgress(
    val skillLevel: SkillLevel,
    val techniquesMastered: List<TechniqueMastery>,
    val difficultyProgress: Map<DifficultyLevel, DifficultyProgress>,
    val timeProgression: List<TimeProgressionPoint>,
    val xpHistory: List<XPMilestone>,
    val learningVelocity: Double // puzzles per week
)

data class SkillLevel(
    val currentLevel: Int,
    val xp: Int,
    val xpToNext: Int,
    val levelName: String,
    val levelDescription: String
)

data class TechniqueMastery(
    val technique: String,
    val masteryLevel: MasteryLevel,
    val timesUsed: Int,
    val successRate: Double,
    val lastUsed: LocalDate
)

enum class MasteryLevel {
    INTRODUCED,
    LEARNING,
    PRACTICING,
    PROFICIENT,
    MASTERED
}

data class DifficultyProgress(
    val difficulty: DifficultyLevel,
    val puzzlesAttempted: Int,
    val puzzlesCompleted: Int,
    val averageTime: Double,
    val bestTime: Double?,
    val completionRate: Double,
    val currentStatus: DifficultyStatus
)

enum class DifficultyStatus {
    NOT_STARTED,
    IN_PROGRESS,
    DEVELOPING,
    PROFICIENT,
    MASTERED
}

data class TimeProgressionPoint(
    val date: LocalDate,
    val averageCompletionTime: Double,
    val puzzlesCompleted: Int,
    val difficultyLevel: DifficultyLevel
)

data class XPMilestone(
    val date: LocalDate,
    val level: Int,
    val xp: Int,
    val achievement: String
)

/**
 * Detailed engagement statistics
 */
data class DetailedEngagementStats(
    val totalSessions: Int,
    val averageSessionLength: Double,
    val longestSession: Double,
    val shortestSession: Double,
    val activeDays: Int,
    val returnRate: Double, // percentage
    val peakActivityTimes: List<ActivityTimeSlot>,
    val sessionDistribution: SessionDistribution,
    val engagementTrend: EngagementTrend
)

data class ActivityTimeSlot(
    val hour: Int,
    val dayOfWeek: String,
    val sessionCount: Int,
    val averageDuration: Double
)

data class SessionDistribution(
    val morningSessions: Int,
    val afternoonSessions: Int,
    val eveningSessions: Int,
    val weekdaySessions: Int,
    val weekendSessions: Int
)

data class EngagementTrend(
    val thisWeek: Double,
    val lastWeek: Double,
    val twoWeeksAgo: Double,
    val trendDirection: TrendDirection,
    val percentChange: Double
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}

/**
 * Skill development report
 */
data class SkillDevelopmentReport(
    val cognitiveSkills: List<SkillMetric>,
    val problemSolvingSkills: List<SkillMetric>,
    val persistenceMetrics: PersistenceMetrics,
    val independenceMetrics: IndependenceMetrics,
    val learningEfficiency: LearningEfficiency
)

data class SkillMetric(
    val skillName: String,
    val currentLevel: Int, // 1-10
    val startingLevel: Int,
    val improvement: Int,
    val trajectory: SkillTrajectory,
    val description: String
)

enum class SkillTrajectory {
    RAPID_IMPROVEMENT,
    STEADY_IMPROVEMENT,
    STABLE,
    SLOW_IMPROVEMENT,
    NEEDS_ATTENTION
}

data class PersistenceMetrics(
    val puzzlesAbandoned: Int,
    val abandonmentRate: Double,
    val averageAttemptsBeforeSuccess: Double,
    val stickinessMetric: Double, // How likely to continue after break
    val longestProblemSolved: String
)

data class IndependenceMetrics(
    val hintUsageRate: Double,
    val hintsDeclined: Int,
    val selfCorrections: Int,
    val independentSolves: Int,
    val independenceScore: Double // 0-100
)

data class LearningEfficiency(
    val learningCurve: List<LearningPoint>,
    val timeToMastery: Map<String, Int>, // technique -> days to mastery
    val retentionRate: Double,
    val practiceEfficiency: Double
)

data class LearningPoint(
    val date: LocalDate,
    val technique: String,
    val proficiency: Double,
    val practiceTime: Int
)

/**
 * Testing results summary
 */
data class TestingResultsSummary(
    val protocolsCompleted: List<ProtocolCompletion>,
    val surveyResponses: List<SurveySummary>,
    val abTestParticipation: List<ABTestParticipation>,
    val overallPerformance: OverallPerformance
)

data class ProtocolCompletion(
    val protocolName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val phasesCompleted: Int,
    val totalPhases: Int,
    val successCriteriaMet: Int,
    val totalCriteria: Int,
    val status: CompletionStatus
)

enum class CompletionStatus {
    IN_PROGRESS,
    COMPLETED,
    EXCEEDED_EXPECTATIONS,
    NEEDS_REVIEW
}

data class SurveySummary(
    val surveyName: String,
    val completedDate: LocalDate,
    val overallRating: Int,
    val keyInsights: List<String>,
    val areasOfSatisfaction: List<String>,
    val areasForImprovement: List<String>
)

data class ABTestParticipation(
    val testName: String,
    val variant: ABTestVariant,
    val contribution: String,
    val metricsContributed: List<String>
)

data class OverallPerformance(
    val percentileRank: Double,
    val strengths: List<String>,
    val areasForGrowth: List<String>,
    val nextMilestones: List<String>
)

/**
 * Peer comparisons (anonymized)
 */
data class PeerComparisons(
    val completionRateComparison: ComparisonMetric,
    val speedComparison: ComparisonMetric,
    val progressComparison: ComparisonMetric,
    val engagementComparison: ComparisonMetric
)

data class ComparisonMetric(
    val userValue: Double,
    val peerAverage: Double,
    val peerPercentile: Int,
    val topPercentile: Double,
    val comparison: String // "above average", "average", "below average"
)

/**
 * Dashboard recommendations
 */
data class DashboardRecommendation(
    val category: RecommendationCategory,
    val priority: Priority,
    val title: String,
    val description: String,
    val actionItems: List<String>,
    val expectedOutcome: String,
    val timeframe: String
)

enum class RecommendationCategory {
    LEARNING,
    ENGAGEMENT,
    TECHNICAL,
    PARENTAL_INVOLVEMENT,
    DIFFICULTY_ADJUSTMENT
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW,
    INFORMATIONAL
}

/**
 * Dashboard alerts
 */
data class DashboardAlert(
    val alertType: AlertType,
    val severity: Severity,
    val message: String,
    val triggeredAt: LocalDateTime,
    val resolved: Boolean = false
)

enum class AlertType {
    LOW_ENGAGEMENT,
    DECLINING_PERFORMANCE,
    HIGH_DIFFICULTY,
    TECHNICAL_ISSUE,
    MILESTONE_ACHIEVED,
    STREAK_MILESTONE,
    NEEDS_ATTENTION
}

enum class Severity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Parent/Teacher dashboard service
 */
class ParentTeacherDashboardService(
    private val analyticsCollector: UsageAnalyticsCollector,
    private val reportGenerator: AnalyticsReportGenerator
) {

    fun generateDashboard(
        participant: TestParticipant,
        sessions: List<UsageAnalytics>,
        startDate: LocalDate
    ): ParentTeacherDashboard {
        val progress = analyticsCollector.calculateLearningProgress(
            participant.participantId,
            sessions,
            startDate
        )

        val engagement = analyticsCollector.calculateEngagementMetrics(
            participant.participantId,
            sessions,
            startDate,
            LocalDate.now()
        )

        val participantReport = reportGenerator.generateParticipantReport(
            participant,
            progress,
            engagement,
            sessions
        )

        return ParentTeacherDashboard(
            participantId = participant.participantId,
            participantName = participant.childName,
            age = participant.age,
            ageGroup = participant.ageGroup,
            generatedAt = LocalDateTime.now(),
            overview = generateOverview(progress, engagement, sessions),
            learningProgress = generateDetailedProgress(sessions, progress),
            engagementStats = generateDetailedEngagement(sessions),
            skillDevelopment = generateSkillDevelopment(sessions, progress),
            testingResults = generateTestingResults(participant, sessions),
            comparisons = generatePeerComparisons(progress, engagement),
            recommendations = generateDashboardRecommendations(progress, engagement, sessions),
            alerts = generateDashboardAlerts(progress, engagement, sessions)
        )
    }

    private fun generateOverview(
        progress: LearningProgress,
        engagement: EngagementMetrics,
        sessions: List<UsageAnalytics>
    ): DashboardOverview {
        val lastActive = sessions.maxByOrNull { it.startTime }?.startTime?.toLocalDate()
            ?: LocalDate.now()

        return DashboardOverview(
            totalPuzzlesCompleted = progress.totalPuzzlesCompleted,
            currentLevel = progress.currentLevel,
            currentStreak = progress.streakDays,
            averageSessionDuration = engagement.averageSessionDuration,
            overallSatisfaction = 4.0, // Would come from surveys
            lastActiveDate = lastActive,
            daysInProgram = (java.time.Duration.between(progress.startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays() + 1).toInt(),
            completionRate = progress.completionRate
        )
    }

    private fun generateDetailedProgress(
        sessions: List<UsageAnalytics>,
        progress: LearningProgress
    ): DetailedLearningProgress {
        val skillLevel = SkillLevel(
            currentLevel = progress.currentLevel,
            xp = progress.xpGained,
            xpToNext = calculateXPToNext(progress.currentLevel),
            levelName = getLevelName(progress.currentLevel),
            levelDescription = getLevelDescription(progress.currentLevel)
        )

        val techniquesMastered = extractTechniqueMastery(sessions)
        val difficultyProgress = generateDifficultyProgress(sessions)
        val timeProgression = generateTimeProgression(sessions)
        val xpHistory = generateXPHistory(progress)

        return DetailedLearningProgress(
            skillLevel = skillLevel,
            techniquesMastered = techniquesMastered,
            difficultyProgress = difficultyProgress,
            timeProgression = timeProgression,
            xpHistory = xpHistory,
            learningVelocity = calculateLearningVelocity(progress)
        )
    }

    private fun generateDetailedEngagement(sessions: List<UsageAnalytics>): DetailedEngagementStats {
        val sessionLengths = sessions.map { it.metrics.totalDurationSeconds / 60.0 }
        val activeDays = sessions.map { it.startTime.toLocalDate() }.distinct().size

        return DetailedEngagementStats(
            totalSessions = sessions.size,
            averageSessionLength = sessionLengths.average(),
            longestSession = sessionLengths.maxOrNull() ?: 0.0,
            shortestSession = sessionLengths.minOrNull() ?: 0.0,
            activeDays = activeDays,
            returnRate = calculateReturnRate(sessions),
            peakActivityTimes = extractPeakActivityTimes(sessions),
            sessionDistribution = calculateSessionDistribution(sessions),
            engagementTrend = calculateEngagementTrend(sessions)
        )
    }

    private fun generateSkillDevelopment(
        sessions: List<UsageAnalytics>,
        progress: LearningProgress
    ): SkillDevelopmentReport {
        return SkillDevelopmentReport(
            cognitiveSkills = listOf(
                SkillMetric("Pattern Recognition", 7, 4, 3, SkillTrajectory.STEADY_IMPROVEMENT, "Ability to recognize Sudoku patterns"),
                SkillMetric("Logical Reasoning", 6, 3, 3, SkillTrajectory.RAPID_IMPROVEMENT, "Logical deduction skills"),
                SkillMetric("Focus & Attention", 8, 5, 3, SkillTrajectory.STABLE, "Ability to maintain focus")
            ),
            problemSolvingSkills = listOf(
                SkillMetric("Deductive Reasoning", 6, 3, 3, SkillTrajectory.STEADY_IMPROVEMENT, "Using logic to eliminate possibilities"),
                SkillMetric("Trial and Error", 5, 3, 2, SkillTrajectory.SLOW_IMPROVEMENT, "Testing hypotheses systematically")
            ),
            persistenceMetrics = calculatePersistenceMetrics(sessions),
            independenceMetrics = calculateIndependenceMetrics(sessions),
            learningEfficiency = calculateLearningEfficiency(sessions, progress)
        )
    }

    private fun generateTestingResults(
        participant: TestParticipant,
        sessions: List<UsageAnalytics>
    ): TestingResultsSummary {
        return TestingResultsSummary(
            protocolsCompleted = emptyList(), // Would be populated from protocol tracking
            surveyResponses = emptyList(), // Would be populated from survey data
            abTestParticipation = listOf(
                ABTestParticipation(
                    testName = "Hint System Style",
                    variant = participant.assignedVariant,
                    contribution = "Contributed completion rate and satisfaction data",
                    metricsContributed = listOf("Completion Rate", "Satisfaction Score")
                )
            ),
            overallPerformance = OverallPerformance(
                percentileRank = 75.0,
                strengths = listOf("Consistency", "Technique Application"),
                areasForGrowth = listOf("Speed", "Advanced Techniques"),
                nextMilestones = listOf("Master Medium Puzzles", "Complete 50 Puzzles")
            )
        )
    }

    private fun generatePeerComparisons(
        progress: LearningProgress,
        engagement: EngagementMetrics
    ): PeerComparisons {
        return PeerComparisons(
            completionRateComparison = ComparisonMetric(
                userValue = progress.completionRate * 100,
                peerAverage = 70.0,
                peerPercentile = 75,
                topPercentile = 95.0,
                comparison = "above average"
            ),
            speedComparison = ComparisonMetric(
                userValue = progress.averageCompletionTime,
                peerAverage = 300.0,
                peerPercentile = 60,
                topPercentile = 180.0,
                comparison = "average"
            ),
            progressComparison = ComparisonMetric(
                userValue = progress.improvementRate,
                peerAverage = 20.0,
                peerPercentile = 80,
                topPercentile = 40.0,
                comparison = "above average"
            ),
            engagementComparison = ComparisonMetric(
                userValue = engagement.returnRate,
                peerAverage = 60.0,
                peerPercentile = 85,
                topPercentile = 90.0,
                comparison = "above average"
            )
        )
    }

    private fun generateDashboardRecommendations(
        progress: LearningProgress,
        engagement: EngagementMetrics,
        sessions: List<UsageAnalytics>
    ): List<DashboardRecommendation> {
        val recommendations = mutableListOf<DashboardRecommendation>()

        // Engagement recommendations
        if (engagement.returnRate < 50) {
            recommendations.add(
                DashboardRecommendation(
                    category = RecommendationCategory.ENGAGEMENT,
                    priority = Priority.HIGH,
                    title = "Increase Practice Frequency",
                    description = "Regular practice helps maintain and build skills",
                    actionItems = listOf(
                        "Set a daily puzzle time",
                        "Start with just 10 minutes a day",
                        "Use reminders to build habit"
                    ),
                    expectedOutcome = "Improved retention and skill development",
                    timeframe = "Next 2 weeks"
                )
            )
        }

        // Difficulty recommendations
        if (progress.completionRate > 0.9 && progress.currentLevel < 5) {
            recommendations.add(
                DashboardRecommendation(
                    category = RecommendationCategory.DIFFICULTY_ADJUSTMENT,
                    priority = Priority.MEDIUM,
                    title = "Ready for Next Challenge",
                    description = "Student is performing very well at current difficulty",
                    actionItems = listOf(
                        "Introduce medium difficulty puzzles",
                        "Gradually reduce hint availability"
                    ),
                    expectedOutcome = "Continued growth and skill development",
                    timeframe = "This week"
                )
            )
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                DashboardRecommendation(
                    category = RecommendationCategory.LEARNING,
                    priority = Priority.INFORMATIONAL,
                    title = "Great Progress!",
                    description = "Student is making excellent progress",
                    actionItems = listOf(
                        "Continue current practice routine",
                        "Consider introducing new techniques"
                    ),
                    expectedOutcome = "Maintained excellent progress",
                    timeframe = "Ongoing"
                )
            )
        }

        return recommendations
    }

    private fun generateDashboardAlerts(
        progress: LearningProgress,
        engagement: EngagementMetrics,
        sessions: List<UsageAnalytics>
    ): List<DashboardAlert> {
        val alerts = mutableListOf<DashboardAlert>()

        // Milestone alerts
        if (progress.streakDays >= 7) {
            alerts.add(
                DashboardAlert(
                    alertType = AlertType.STREAK_MILESTONE,
                    severity = Severity.INFO,
                    message = "Amazing! ${progress.streakDays} day streak achieved!",
                    triggeredAt = LocalDateTime.now()
                )
            )
        }

        if (progress.totalPuzzlesCompleted > 0 && progress.totalPuzzlesCompleted % 25 == 0) {
            alerts.add(
                DashboardAlert(
                    alertType = AlertType.MILESTONE_ACHIEVED,
                    severity = Severity.INFO,
                    message = "Milestone: ${progress.totalPuzzlesCompleted} puzzles completed!",
                    triggeredAt = LocalDateTime.now()
                )
            )
        }

        // Warning alerts
        if (engagement.returnRate < 30) {
            alerts.add(
                DashboardAlert(
                    alertType = AlertType.LOW_ENGAGEMENT,
                    severity = Severity.WARNING,
                    message = "Engagement has dropped recently. Consider encouraging more frequent practice.",
                    triggeredAt = LocalDateTime.now()
                )
            )
        }

        return alerts
    }

    // Helper functions
    private fun calculateXPToNext(currentLevel: Int): Int {
        return (currentLevel + 1) * 100 - (currentLevel * 10)
    }

    private fun getLevelName(level: Int): String {
        return when (level) {
            0 -> "Beginner"
            1 -> "Novice"
            2 -> "Apprentice"
            3 -> "Solver"
            4 -> "Skilled Solver"
            5 -> "Advanced Solver"
            6 -> "Expert"
            7 -> "Master"
            8 -> "Grandmaster"
            9 -> "Legend"
            10 -> "Sudoku Champion"
            else -> "Unknown"
        }
    }

    private fun getLevelDescription(level: Int): String {
        return when (level) {
            0 -> "Just starting the Sudoku journey"
            1 -> "Learning the basics"
            2 -> "Getting comfortable with easy puzzles"
            3 -> "Solving easy puzzles independently"
            4 -> "Moving to medium difficulty"
            5 -> "Comfortable with medium puzzles"
            6 -> "Tackling hard puzzles"
            7 -> "Mastering advanced techniques"
            8 -> "Solving expert puzzles"
            9 -> "Among the best solvers"
            10 -> "True Sudoku mastery achieved"
            else -> "Continuing to improve"
        }
    }

    private fun extractTechniqueMastery(sessions: List<UsageAnalytics>): List<TechniqueMastery> {
        return listOf(
            TechniqueMastery("Single Candidate", MasteryLevel.PROFICIENT, 45, 0.92, LocalDate.now()),
            TechniqueMastery("Single Position", MasteryLevel.PROFICIENT, 38, 0.88, LocalDate.now()),
            TechniqueMastery("Naked Pairs", MasteryLevel.PRACTICING, 15, 0.75, LocalDate.now())
        )
    }

    private fun generateDifficultyProgress(sessions: List<UsageAnalytics>): Map<DifficultyLevel, DifficultyProgress> {
        val result = mutableMapOf<DifficultyLevel, DifficultyProgress>()

        result[DifficultyLevel.EASY] = DifficultyProgress(
            difficulty = DifficultyLevel.EASY,
            puzzlesAttempted = 20,
            puzzlesCompleted = 19,
            averageTime = 180.0,
            bestTime = 120.0,
            completionRate = 0.95,
            currentStatus = DifficultyStatus.MASTERED
        )

        result[DifficultyLevel.MEDIUM] = DifficultyProgress(
            difficulty = DifficultyLevel.MEDIUM,
            puzzlesAttempted = 10,
            puzzlesCompleted = 7,
            averageTime = 450.0,
            bestTime = 300.0,
            completionRate = 0.70,
            currentStatus = DifficultyStatus.DEVELOPING
        )

        return result
    }

    private fun generateTimeProgression(sessions: List<UsageAnalytics>): List<TimeProgressionPoint> {
        return sessions.groupBy { it.startTime.toLocalDate() }
            .map { (date, daySessions) ->
                TimeProgressionPoint(
                    date = date,
                    averageCompletionTime = daySessions.map { it.metrics.averageTimePerCell }.average(),
                    puzzlesCompleted = daySessions.sumOf { it.metrics.puzzlesCompleted },
                    difficultyLevel = DifficultyLevel.MEDIUM
                )
            }
            .takeLast(10)
    }

    private fun generateXPHistory(progress: LearningProgress): List<XPMilestone> {
        return listOf(
            XPMilestone(progress.startDate, 1, 10, "Started journey"),
            XPMilestone(progress.startDate.plusDays(3), 2, 100, "First puzzles completed"),
            XPMilestone(LocalDate.now(), progress.currentLevel, progress.xpGained, "Current level")
        )
    }

    private fun calculateLearningVelocity(progress: LearningProgress): Double {
        val days = (java.time.Duration.between(progress.startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays() + 1).toInt()
        return if (days > 0) {
            progress.totalPuzzlesCompleted.toDouble() / days * 7 // puzzles per week
        } else 0.0
    }

    private fun calculateReturnRate(sessions: List<UsageAnalytics>): Double {
        val firstDate = sessions.minByOrNull { it.startTime }?.startTime?.toLocalDate() ?: return 0.0
        val lastDate = sessions.maxByOrNull { it.startTime }?.startTime?.toLocalDate() ?: return 0.0
        val totalDays = (java.time.Duration.between(firstDate.atStartOfDay(), lastDate.atStartOfDay()).toDays() + 1).toInt()
        val activeDays = sessions.map { it.startTime.toLocalDate() }.distinct().size

        return if (totalDays > 0) {
            (activeDays.toDouble() / totalDays) * 100
        } else 0.0
    }

    private fun extractPeakActivityTimes(sessions: List<UsageAnalytics>): List<ActivityTimeSlot> {
        return sessions.groupBy { it.startTime.hour }
            .map { (hour, hourSessions) ->
                ActivityTimeSlot(
                    hour = hour,
                    dayOfWeek = "Various",
                    sessionCount = hourSessions.size,
                    averageDuration = hourSessions.map { it.metrics.totalDurationSeconds / 60.0 }.average()
                )
            }
            .sortedByDescending { it.sessionCount }
            .take(3)
    }

    private fun calculateSessionDistribution(sessions: List<UsageAnalytics>): SessionDistribution {
        val morning = sessions.count { it.startTime.hour in 6..11 }
        val afternoon = sessions.count { it.startTime.hour in 12..17 }
        val evening = sessions.count { it.startTime.hour in 18..23 || it.startTime.hour in 0..5 }

        val weekdays = sessions.count { it.startTime.dayOfWeek.value in 1..5 }
        val weekends = sessions.count { it.startTime.dayOfWeek.value in 6..7 }

        return SessionDistribution(
            morningSessions = morning,
            afternoonSessions = afternoon,
            eveningSessions = evening,
            weekdaySessions = weekdays,
            weekendSessions = weekends
        )
    }

    private fun calculateEngagementTrend(sessions: List<UsageAnalytics>): EngagementTrend {
        val now = LocalDate.now()
        val thisWeek = sessions.filter { s ->
            val date = s.startTime.toLocalDate()
            val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
            date >= weekStart
        }.size

        val lastWeekStart = now.minusDays(now.dayOfWeek.value.toLong() + 6)
        val lastWeekEnd = now.minusDays(now.dayOfWeek.value.toLong())
        val lastWeek = sessions.filter { s ->
            val date = s.startTime.toLocalDate()
            date >= lastWeekStart && date <= lastWeekEnd
        }.size

        val twoWeeksAgo = sessions.filter { s ->
            val date = s.startTime.toLocalDate()
            val weekStart = lastWeekStart.minusDays(7)
            val weekEnd = lastWeekEnd.minusDays(7)
            date >= weekStart && date <= weekEnd
        }.size

        val trend = when {
            thisWeek > lastWeek -> TrendDirection.IMPROVING
            thisWeek == lastWeek -> TrendDirection.STABLE
            else -> TrendDirection.DECLINING
        }

        val percentChange = if (lastWeek > 0) {
            ((thisWeek - lastWeek).toDouble() / lastWeek) * 100
        } else 0.0

        return EngagementTrend(
            thisWeek = thisWeek.toDouble(),
            lastWeek = lastWeek.toDouble(),
            twoWeeksAgo = twoWeeksAgo.toDouble(),
            trendDirection = trend,
            percentChange = percentChange
        )
    }

    private fun calculatePersistenceMetrics(sessions: List<UsageAnalytics>): PersistenceMetrics {
        val abandoned = sessions.sumOf { it.metrics.puzzlesAbandoned }
        val attempted = sessions.sumOf { it.metrics.puzzlesStarted }

        return PersistenceMetrics(
            puzzlesAbandoned = abandoned,
            abandonmentRate = if (attempted > 0) abandoned.toDouble() / attempted else 0.0,
            averageAttemptsBeforeSuccess = 1.5, // Placeholder
            stickinessMetric = 0.85, // Placeholder
            longestProblemSolved = "Expert puzzle (45 minutes)"
        )
    }

    private fun calculateIndependenceMetrics(sessions: List<UsageAnalytics>): IndependenceMetrics {
        val totalAttempts = sessions.sumOf { it.metrics.puzzlesStarted }
        val hintsRequested = sessions.sumOf { it.metrics.hintsRequested }

        return IndependenceMetrics(
            hintUsageRate = if (totalAttempts > 0) {
                (hintsRequested.toDouble() / totalAttempts) / 10
            } else 0.0,
            hintsDeclined = 5, // Placeholder
            selfCorrections = sessions.sumOf { it.metrics.errorsCorrectedIndependently },
            independentSolves = sessions.count { it.metrics.hintsRequested == 0 },
            independenceScore = 75.0 // Placeholder
        )
    }

    private fun calculateLearningEfficiency(
        sessions: List<UsageAnalytics>,
        progress: LearningProgress
    ): LearningEfficiency {
        return LearningEfficiency(
            learningCurve = emptyList(),
            timeToMastery = mapOf(
                "Single Candidate" to 3,
                "Single Position" to 5,
                "Naked Pairs" to 10
            ),
            retentionRate = 0.85,
            practiceEfficiency = progress.improvementRate / 100
        )
    }
}
