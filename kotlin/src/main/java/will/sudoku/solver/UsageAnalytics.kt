package will.sudoku.solver

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Detailed analytics for user testing
 */
data class UsageAnalytics(
    val participantId: String,
    val sessionId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val actions: List<SessionAction>,
    val metrics: SessionMetrics
)

/**
 * Aggregated metrics for a session
 */
data class SessionMetrics(
    val totalDurationSeconds: Int,
    val activeTimeSeconds: Int,
    val idleTimeSeconds: Int,
    val puzzlesStarted: Int,
    val puzzlesCompleted: Int,
    val puzzlesAbandoned: Int,
    val hintsRequested: Int,
    val hintsAccepted: Int,
    val hintsRejected: Int,
    val errorsMade: Int,
    val errorsCorrectedIndependently: Int,
    val errorsCorrectedWithHelp: Int,
    val averageTimePerCell: Double,
    val fastestPuzzleTime: Int?,
    val slowestPuzzleTime: Int?,
    val featuresUsed: Map<EducationalFeature, Int>,
    val difficultyLevelsAttempted: Map<DifficultyLevel, Int>
)

/**
 * Learning progress tracking
 */
data class LearningProgress(
    val participantId: String,
    val startDate: LocalDate,
    val currentDate: LocalDate,
    val totalSessions: Int,
    val totalPuzzlesAttempted: Int,
    val totalPuzzlesCompleted: Int,
    val completionRate: Double,
    val averageCompletionTime: Double,
    val improvementRate: Double, // % improvement in time
    val techniquesMastered: Set<String>,
    val currentLevel: Int,
    val xpGained: Int,
    val streakDays: Int,
    val lastActivityDate: LocalDate
)

/**
 * Engagement metrics
 */
data class EngagementMetrics(
    val participantId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val sessionsCompleted: Int,
    val averageSessionDuration: Double,
    val averagePuzzlesPerSession: Double,
    val returnRate: Double, // % of days with activity
    val peakActivityTime: String, // hour of day
    val longestStreak: Int,
    val totalActiveTime: Int
)

/**
 * Analytics collector for tracking user behavior
 */
class UsageAnalyticsCollector {

    private val sessionActions = mutableMapOf<String, MutableList<SessionAction>>()
    private val activeSessions = mutableMapOf<String, LocalDateTime>()

    /**
     * Start tracking a session
     */
    fun startSession(sessionId: String, participantId: String) {
        activeSessions[sessionId] = LocalDateTime.now()
        sessionActions[sessionId] = mutableListOf()
        trackAction(
            sessionId = sessionId,
            participantId = participantId,
            actionType = ActionType.PUZZLE_STARTED,
            details = mapOf("sessionStart" to true)
        )
    }

    /**
     * End tracking a session
     */
    fun endSession(sessionId: String, participantId: String): UsageAnalytics? {
        val startTime = activeSessions[sessionId] ?: return null
        val endTime = LocalDateTime.now()
        val actions = sessionActions[sessionId] ?: emptyList()

        trackAction(
            sessionId = sessionId,
            participantId = participantId,
            actionType = ActionType.PUZZLE_COMPLETED,
            details = mapOf("sessionEnd" to true)
        )

        val metrics = calculateMetrics(actions, startTime, endTime)

        activeSessions.remove(sessionId)
        return UsageAnalytics(
            participantId = participantId,
            sessionId = sessionId,
            startTime = startTime,
            endTime = endTime,
            actions = actions,
            metrics = metrics
        )
    }

    /**
     * Track an individual action
     */
    fun trackAction(
        sessionId: String,
        participantId: String,
        actionType: ActionType,
        details: Map<String, Any> = emptyMap()
    ) {
        val action = SessionAction(
            actionId = generateActionId(),
            sessionId = sessionId,
            timestamp = LocalDateTime.now(),
            actionType = actionType,
            details = details
        )
        sessionActions[sessionId]?.add(action)
    }

    /**
     * Calculate session metrics from actions
     */
    private fun calculateMetrics(
        actions: List<SessionAction>,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): SessionMetrics {
        val totalDuration = java.time.Duration.between(startTime, endTime).seconds.toInt()
        var activeTime = 0
        var idleTime = 0

        var puzzlesStarted = 0
        var puzzlesCompleted = 0
        var puzzlesAbandoned = 0

        var hintsRequested = 0
        var hintsAccepted = 0
        var hintsRejected = 0

        var errorsMade = 0
        var errorsCorrectedIndependently = 0
        var errorsCorrectedWithHelp = 0

        val featuresUsed = mutableMapOf<EducationalFeature, Int>()
        val difficultyLevels = mutableMapOf<DifficultyLevel, Int>()

        var totalTimeForCells = 0
        var cellCount = 0
        val puzzleTimes = mutableListOf<Int>()

        actions.forEach { action ->
            when (action.actionType) {
                ActionType.PUZZLE_STARTED -> {
                    puzzlesStarted++
                    action.details["difficulty"]?.let { diff ->
                        val level = DifficultyLevel.valueOf(diff as String)
                        difficultyLevels[level] = difficultyLevels.getOrDefault(level, 0) + 1
                    }
                }
                ActionType.PUZZLE_COMPLETED -> {
                    puzzlesCompleted++
                    action.details["timeSeconds"]?.let { time ->
                        val timeValue = when (time) {
                            is Int -> time
                            is Double -> time.toInt()
                            else -> 0
                        }
                        puzzleTimes.add(timeValue)
                    }
                }
                ActionType.PUZZLE_ABANDONED -> puzzlesAbandoned++
                ActionType.HINT_REQUESTED -> hintsRequested++
                ActionType.HINT_RECEIVED -> hintsAccepted++
                ActionType.ERROR_MADE -> errorsMade++
                ActionType.ERROR_CORRECTED -> {
                    val withHelp = action.details["withHelp"] as? Boolean ?: false
                    if (withHelp) {
                        errorsCorrectedWithHelp++
                    } else {
                        errorsCorrectedIndependently++
                    }
                }
                ActionType.FEATURE_USED -> {
                    action.details["feature"]?.let { feature ->
                        val featureEnum = EducationalFeature.valueOf(feature as String)
                        featuresUsed[featureEnum] = featuresUsed.getOrDefault(featureEnum, 0) + 1
                    }
                }
                ActionType.TIME_ELAPSED -> {
                    activeTime += action.details["activeSeconds"] as? Int ?: 0
                    idleTime += action.details["idleSeconds"] as? Int ?: 0
                }
                else -> {}
            }
        }

        val avgTimePerCell = if (cellCount > 0) totalTimeForCells.toDouble() / cellCount else 0.0

        return SessionMetrics(
            totalDurationSeconds = totalDuration,
            activeTimeSeconds = activeTime,
            idleTimeSeconds = idleTime,
            puzzlesStarted = puzzlesStarted,
            puzzlesCompleted = puzzlesCompleted,
            puzzlesAbandoned = puzzlesAbandoned,
            hintsRequested = hintsRequested,
            hintsAccepted = hintsAccepted,
            hintsRejected = hintsRejected,
            errorsMade = errorsMade,
            errorsCorrectedIndependently = errorsCorrectedIndependently,
            errorsCorrectedWithHelp = errorsCorrectedWithHelp,
            averageTimePerCell = avgTimePerCell,
            fastestPuzzleTime = puzzleTimes.minOrNull(),
            slowestPuzzleTime = puzzleTimes.maxOrNull(),
            featuresUsed = featuresUsed,
            difficultyLevelsAttempted = difficultyLevels
        )
    }

    /**
     * Calculate learning progress over multiple sessions
     */
    fun calculateLearningProgress(
        participantId: String,
        sessions: List<UsageAnalytics>,
        startDate: LocalDate
    ): LearningProgress {
        val totalSessions = sessions.size
        val totalPuzzlesAttempted = sessions.sumOf { it.metrics.puzzlesStarted }
        val totalPuzzlesCompleted = sessions.sumOf { it.metrics.puzzlesCompleted }

        val completionRate = if (totalPuzzlesAttempted > 0) {
            totalPuzzlesCompleted.toDouble() / totalPuzzlesAttempted
        } else 0.0

        val completionTimes = sessions.flatMap { session ->
            session.actions.filter { it.actionType == ActionType.PUZZLE_COMPLETED }
                .mapNotNull { it.details["timeSeconds"] as? Int }
        }

        val averageCompletionTime = if (completionTimes.isNotEmpty()) {
            completionTimes.average()
        } else 0.0

        // Calculate improvement rate (compare first half to second half)
        val improvementRate = if (completionTimes.size >= 4) {
            val midpoint = completionTimes.size / 2
            val firstHalf = completionTimes.take(midpoint).average()
            val secondHalf = completionTimes.drop(midpoint).average()
            if (firstHalf > 0) {
                ((firstHalf - secondHalf) / firstHalf) * 100
            } else 0.0
        } else 0.0

        // Calculate streak
        val activityDates = sessions.map { it.startTime.toLocalDate() }.distinct().sorted()
        var streak = 0
        var maxStreak = 0
        var current = LocalDate.now()

        for (date in activityDates.reversed()) {
            if (date == current || date == current.minusDays(1)) {
                streak++
                if (streak > maxStreak) maxStreak = streak
                current = date
            } else {
                break
            }
        }

        return LearningProgress(
            participantId = participantId,
            startDate = startDate,
            currentDate = LocalDate.now(),
            totalSessions = totalSessions,
            totalPuzzlesAttempted = totalPuzzlesAttempted,
            totalPuzzlesCompleted = totalPuzzlesCompleted,
            completionRate = completionRate,
            averageCompletionTime = averageCompletionTime,
            improvementRate = improvementRate,
            techniquesMastered = extractTechniques(sessions),
            currentLevel = calculateLevel(totalPuzzlesCompleted),
            xpGained = totalPuzzlesCompleted * 10,
            streakDays = maxStreak,
            lastActivityDate = sessions.maxByOrNull { it.startTime }?.startTime?.toLocalDate()
                ?: LocalDate.now()
        )
    }

    /**
     * Extract techniques learned from session actions
     */
    private fun extractTechniques(sessions: List<UsageAnalytics>): Set<String> {
        val techniques = mutableSetOf<String>()

        sessions.forEach { session ->
            session.actions.forEach { action ->
                when (action.actionType) {
                    ActionType.STEP_COMPLETED -> {
                        action.details["technique"]?.let { tech ->
                            techniques.add(tech as String)
                        }
                    }
                    else -> {}
                }
            }
        }

        return techniques
    }

    /**
     * Calculate level based on puzzles completed
     */
    private fun calculateLevel(puzzlesCompleted: Int): Int {
        return when {
            puzzlesCompleted >= 100 -> 10
            puzzlesCompleted >= 81 -> 9
            puzzlesCompleted >= 64 -> 8
            puzzlesCompleted >= 49 -> 7
            puzzlesCompleted >= 36 -> 6
            puzzlesCompleted >= 25 -> 5
            puzzlesCompleted >= 16 -> 4
            puzzlesCompleted >= 9 -> 3
            puzzlesCompleted >= 4 -> 2
            puzzlesCompleted >= 1 -> 1
            else -> 0
        }
    }

    /**
     * Calculate engagement metrics for a time period
     */
    fun calculateEngagementMetrics(
        participantId: String,
        sessions: List<UsageAnalytics>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): EngagementMetrics {
        val periodSessions = sessions.filter { session ->
            val date = session.startTime.toLocalDate()
            date >= periodStart && date <= periodEnd
        }

        val sessionsCompleted = periodSessions.size
        val totalDuration = periodSessions.sumOf { it.metrics.totalDurationSeconds }
        val averageSessionDuration = if (sessionsCompleted > 0) {
            totalDuration.toDouble() / sessionsCompleted
        } else 0.0

        val totalPuzzles = periodSessions.sumOf { it.metrics.puzzlesCompleted }
        val averagePuzzlesPerSession = if (sessionsCompleted > 0) {
            totalPuzzles.toDouble() / sessionsCompleted
        } else 0.0

        val totalDays = java.time.Duration.between(periodStart.atStartOfDay(), periodEnd.atStartOfDay()).toDays() + 1
        val activeDays = periodSessions.map { it.startTime.toLocalDate() }.distinct().size
        val returnRate = if (totalDays > 0) {
            (activeDays.toDouble() / totalDays) * 100
        } else 0.0

        // Find peak activity time
        val hourCounts = periodSessions.groupingBy { it.startTime.hour }.eachCount()
        val peakHour = hourCounts.maxByOrNull { it.value }?.key ?: 10

        // Calculate longest streak
        val activityDates = periodSessions.map { it.startTime.toLocalDate() }.distinct().sorted()
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in activityDates) {
            if (previousDate == null || date == previousDate.plusDays(1)) {
                currentStreak++
                if (currentStreak > longestStreak) longestStreak = currentStreak
            } else {
                currentStreak = 1
            }
            previousDate = date
        }

        return EngagementMetrics(
            participantId = participantId,
            periodStart = periodStart,
            periodEnd = periodEnd,
            sessionsCompleted = sessionsCompleted,
            averageSessionDuration = averageSessionDuration,
            averagePuzzlesPerSession = averagePuzzlesPerSession,
            returnRate = returnRate,
            peakActivityTime = "$peakHour:00",
            longestStreak = longestStreak,
            totalActiveTime = totalDuration
        )
    }

    private fun generateActionId(): String {
        return "action-${System.currentTimeMillis()}-${(0..999).random()}"
    }
}

/**
 * Analytics report generator
 */
class AnalyticsReportGenerator {

    fun generateParticipantReport(
        participant: TestParticipant,
        progress: LearningProgress,
        engagement: EngagementMetrics,
        recentSessions: List<UsageAnalytics>
    ): ParticipantReport {
        return ParticipantReport(
            participantId = participant.participantId,
            participantName = participant.childName,
            ageGroup = participant.ageGroup,
            generatedAt = LocalDateTime.now(),
            summary = generateSummary(progress),
            progress = progress,
            engagement = engagement,
            recentActivity = recentSessions.takeLast(10),
            recommendations = generateRecommendations(progress, engagement)
        )
    }

    private fun generateSummary(progress: LearningProgress): String {
        return """
            |Student has completed ${progress.totalPuzzlesCompleted} puzzles
            |with a ${(progress.completionRate * 100).toInt()}% completion rate.
            |Overall improvement: ${progress.improvementRate.toInt()}% faster solving time.
            |Current level: ${progress.currentLevel} with ${progress.xpGained} XP earned.
            |Current streak: ${progress.streakDays} days.
        """.trimMargin().replace("\n", " ")
    }

    private fun generateRecommendations(
        progress: LearningProgress,
        engagement: EngagementMetrics
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (progress.completionRate < 0.5) {
            recommendations.add("Consider using more hints to build confidence")
        }

        if (progress.improvementRate < 10) {
            recommendations.add("Try reviewing tutorial steps for techniques you're finding difficult")
        }

        if (engagement.returnRate < 50) {
            recommendations.add("Regular practice helps maintain skills - try solving at least one puzzle daily")
        }

        if (progress.streakDays >= 7) {
            recommendations.add("Great consistency! Ready to try harder puzzles")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Excellent progress! Keep up the great work!")
        }

        return recommendations
    }
}

/**
 * Complete participant report
 */
data class ParticipantReport(
    val participantId: String,
    val participantName: String,
    val ageGroup: AgeGroup,
    val generatedAt: LocalDateTime,
    val summary: String,
    val progress: LearningProgress,
    val engagement: EngagementMetrics,
    val recentActivity: List<UsageAnalytics>,
    val recommendations: List<String>
)
