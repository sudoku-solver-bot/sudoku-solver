package will.sudoku.solver

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Data collection system for user testing
 * Handles all data storage, retrieval, and export
 */
class DataCollectionSystem {

    private val participants = ConcurrentHashMap<String, TestParticipant>()
    private val sessions = ConcurrentHashMap<String, TestingSession>()
    private val sessionAnalytics = ConcurrentHashMap<String, UsageAnalytics>()
    private val feedback = ConcurrentHashMap<String, UserFeedback>()
    private val abTestResults = ConcurrentHashMap<String, MutableList<ABTestResult>>()
    private val surveySubmissions = ConcurrentHashMap<String, SurveySubmission>()
    private val learningMetrics = ConcurrentHashMap<String, MutableList<LearningMetrics>>()

    private val analyticsCollector = UsageAnalyticsCollector()

    /**
     * Register a new test participant
     */
    fun registerParticipant(participant: TestParticipant): TestParticipant {
        participants[participant.participantId] = participant
        logDataEvent("PARTICIPANT_REGISTERED", mapOf("participantId" to participant.participantId))
        return participant
    }

    /**
     * Get participant by ID
     */
    fun getParticipant(participantId: String): TestParticipant? {
        return participants[participantId]
    }

    /**
     * List all participants
     */
    fun listParticipants(): List<TestParticipant> {
        return participants.values.toList()
    }

    /**
     * Start a new testing session
     */
    fun startSession(
        sessionId: String,
        participantId: String,
        phase: TestPhase,
        puzzleId: String?,
        difficultyLevel: DifficultyLevel?
    ): TestingSession {
        val session = TestingSession(
            sessionId = sessionId,
            participantId = participantId,
            phase = phase,
            startTime = LocalDateTime.now(),
            status = SessionStatus.IN_PROGRESS,
            puzzleId = puzzleId,
            difficultyLevel = difficultyLevel
        )

        sessions[sessionId] = session
        analyticsCollector.startSession(sessionId, participantId)

        logDataEvent("SESSION_STARTED", mapOf(
            "sessionId" to sessionId,
            "participantId" to participantId,
            "phase" to phase.name
        ))

        return session
    }

    /**
     * End a testing session
     */
    fun endSession(sessionId: String): UsageAnalytics? {
        val session = sessions[sessionId] ?: return null

        val analytics = analyticsCollector.endSession(
            sessionId,
            session.participantId
        )

        if (analytics != null) {
            sessionAnalytics[sessionId] = analytics
            sessions[sessionId] = session.copy(
                endTime = LocalDateTime.now(),
                status = SessionStatus.COMPLETED
            )

            logDataEvent("SESSION_ENDED", mapOf(
                "sessionId" to sessionId,
                "participantId" to session.participantId,
                "puzzlesCompleted" to analytics.metrics.puzzlesCompleted
            ))

            // Update learning metrics
            updateLearningMetrics(session.participantId, analytics)
        }

        return analytics
    }

    /**
     * Track an action during a session
     */
    fun trackAction(
        sessionId: String,
        participantId: String,
        actionType: ActionType,
        details: Map<String, Any> = emptyMap()
    ) {
        analyticsCollector.trackAction(sessionId, participantId, actionType, details)

        // Also record to AB test if applicable
        val participant = participants[participantId]
        if (participant != null) {
            recordABTestMetrics(sessionId, participant, actionType, details)
        }
    }

    /**
     * Record survey feedback
     */
    fun recordFeedback(feedback: UserFeedback): UserFeedback {
        this.feedback[feedback.feedbackId] = feedback

        logDataEvent("FEEDBACK_RECORDED", mapOf(
            "feedbackId" to feedback.feedbackId,
            "participantId" to feedback.participantId,
            "overallRating" to feedback.overallRating
        ))

        return feedback
    }

    /**
     * Submit survey
     */
    fun submitSurvey(submission: SurveySubmission): SurveySubmission {
        surveySubmissions[submission.submissionId] = submission

        logDataEvent("SURVEY_SUBMITTED", mapOf(
            "submissionId" to submission.submissionId,
            "surveyId" to submission.surveyId,
            "participantId" to submission.participantId
        ))

        return submission
    }

    /**
     * Record A/B test result
     */
    fun recordABTestResult(result: ABTestResult) {
        abTestResults.getOrPut(result.testId) { mutableListOf() }.add(result)

        logDataEvent("AB_TEST_RESULT", mapOf(
            "testId" to result.testId,
            "variant" to result.variant.name,
            "metric" to result.metricType.name,
            "value" to result.value
        ))
    }

    /**
     * Get A/B test results for a test
     */
    fun getABTestResults(testId: String): List<ABTestResult> {
        return abTestResults[testId] ?: emptyList()
    }

    /**
     * Get analytics for a participant
     */
    fun getParticipantAnalytics(participantId: String): List<UsageAnalytics> {
        return sessionAnalytics.values.filter { it.participantId == participantId }
    }

    /**
     * Get analytics for a session
     */
    fun getSessionAnalytics(sessionId: String): UsageAnalytics? {
        return sessionAnalytics[sessionId]
    }

    /**
     * Get feedback for a participant
     */
    fun getParticipantFeedback(participantId: String): List<UserFeedback> {
        return feedback.values.filter { it.participantId == participantId }
    }

    /**
     * Get learning metrics for a participant
     */
    fun getLearningMetrics(participantId: String): List<LearningMetrics> {
        return learningMetrics[participantId] ?: emptyList()
    }

    /**
     * Update learning metrics based on session analytics
     */
    private fun updateLearningMetrics(participantId: String, analytics: UsageAnalytics) {
        val participant = participants[participantId] ?: return

        val metrics = LearningMetrics(
            participantId = participantId,
            date = LocalDate.now(),
            puzzlesAttempted = analytics.metrics.puzzlesStarted,
            puzzlesCompleted = analytics.metrics.puzzlesCompleted,
            averageCompletionTime = analytics.metrics.averageTimePerCell,
            hintsUsed = analytics.metrics.hintsRequested,
            mistakesMade = analytics.metrics.errorsMade,
            mistakesCorrectedIndependently = analytics.metrics.errorsCorrectedIndependently,
            techniquesLearned = extractTechniquesFromAnalytics(analytics),
            currentLevel = calculateLevelFromAnalytics(analytics),
            xpGained = analytics.metrics.puzzlesCompleted * 10
        )

        learningMetrics.getOrPut(participantId) { mutableListOf<LearningMetrics>() }.add(metrics)
    }

    /**
     * Extract techniques learned from analytics
     */
    private fun extractTechniquesFromAnalytics(analytics: UsageAnalytics): Set<String> {
        return analytics.actions
            .filter { it.actionType == ActionType.STEP_COMPLETED }
            .mapNotNull { it.details["technique"] as? String }
            .toSet()
    }

    /**
     * Calculate level from analytics
     */
    private fun calculateLevelFromAnalytics(analytics: UsageAnalytics): Int {
        val puzzlesCompleted = analytics.metrics.puzzlesCompleted
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
     * Record A/B test metrics from user actions
     */
    private fun recordABTestMetrics(
        sessionId: String,
        participant: TestParticipant,
        actionType: ActionType,
        details: Map<String, Any>
    ) {
        // Map actions to A/B test metrics
        when (actionType) {
            ActionType.PUZZLE_COMPLETED -> {
                details["timeSeconds"]?.let { time ->
                    recordABTestResult(
                        ABTestResult(
                            testId = "hint-system-001",
                            feature = EducationalFeature.HINT_SYSTEM,
                            variant = participant.assignedVariant,
                            participantId = participant.participantId,
                            sessionId = sessionId,
                            metricType = ABTestMetric.AVERAGE_COMPLETION_TIME,
                            value = time as Double,
                            timestamp = LocalDateTime.now()
                        )
                    )
                }

                recordABTestResult(
                    ABTestResult(
                        testId = "hint-system-001",
                        feature = EducationalFeature.HINT_SYSTEM,
                        variant = participant.assignedVariant,
                        participantId = participant.participantId,
                        sessionId = sessionId,
                        metricType = ABTestMetric.COMPLETION_RATE,
                        value = 1.0,
                        timestamp = LocalDateTime.now()
                    )
                )
            }

            ActionType.HINT_REQUESTED -> {
                recordABTestResult(
                    ABTestResult(
                        testId = "hint-system-001",
                        feature = EducationalFeature.HINT_SYSTEM,
                        variant = participant.assignedVariant,
                        participantId = participant.participantId,
                        sessionId = sessionId,
                        metricType = ABTestMetric.HINT_USAGE_RATE,
                        value = 1.0,
                        timestamp = LocalDateTime.now()
                    )
                )
            }

            ActionType.ERROR_MADE -> {
                recordABTestResult(
                    ABTestResult(
                        testId = "error-001",
                        feature = EducationalFeature.ERROR_HIGHLIGHTING,
                        variant = participant.assignedVariant,
                        participantId = participant.participantId,
                        sessionId = sessionId,
                        metricType = ABTestMetric.ERROR_RATE,
                        value = 1.0,
                        timestamp = LocalDateTime.now()
                    )
                )
            }

            else -> {}
        }
    }

    /**
     * Export data for analysis
     */
    fun exportParticipantData(participantId: String): ParticipantDataExport {
        val participant = participants[participantId]
            ?: throw IllegalArgumentException("Participant not found: $participantId")

        return ParticipantDataExport(
            participant = participant,
            sessions = sessions.values.filter { it.participantId == participantId },
            analytics = getParticipantAnalytics(participantId),
            feedback = getParticipantFeedback(participantId),
            learningMetrics = getLearningMetrics(participantId),
            surveys = surveySubmissions.values.filter { it.participantId == participantId },
            exportedAt = LocalDateTime.now()
        )
    }

    /**
     * Export all data for backup/research
     */
    fun exportAllData(): CompleteDataExport {
        return CompleteDataExport(
            participants = participants.values.toList(),
            sessions = sessions.values.toList(),
            analytics = sessionAnalytics.values.toList(),
            feedback = feedback.values.toList(),
            abTestResults = abTestResults.flatMap { it.value },
            surveySubmissions = surveySubmissions.values.toList(),
            learningMetrics = learningMetrics.flatMap { it.value },
            exportedAt = LocalDateTime.now()
        )
    }

    /**
     * Generate aggregated reports
     */
    fun generateAggregatedReport(): AggregatedReport {
        val allAnalytics = sessionAnalytics.values.toList()

        return AggregatedReport(
            totalParticipants = participants.size,
            totalSessions = sessions.size,
            totalPuzzlesCompleted = allAnalytics.sumOf { it.metrics.puzzlesCompleted },
            averageCompletionRate = if (allAnalytics.isNotEmpty()) {
                allAnalytics.map { analytics ->
                    if (analytics.metrics.puzzlesStarted > 0) {
                        analytics.metrics.puzzlesCompleted.toDouble() / analytics.metrics.puzzlesStarted
                    } else 0.0
                }.average()
            } else 0.0,
            averageHintsPerPuzzle = if (allAnalytics.isNotEmpty()) {
                allAnalytics.map { it.metrics.hintsRequested }.average()
            } else 0.0,
            ageGroupBreakdown = participants.values.groupBy { it.ageGroup }
                .mapValues { it.value.size },
            difficultyDistribution = allAnalytics.flatMap { analytics ->
                analytics.metrics.difficultyLevelsAttempted.entries.map { entry ->
                    Pair(entry.key.name, entry.value)
                }
            }.groupBy { it.first }
                .mapValues { entry ->
                    try {
                        DifficultyLevel.valueOf(entry.key) to entry.value.sumOf { it.second }
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                .mapNotNull { it.value }
                .toMap(),
            generatedAt = LocalDateTime.now()
        )
    }

    /**
     * Log data event for audit trail
     */
    private fun logDataEvent(eventType: String, details: Map<String, Any>) {
        // In production, this would log to a file or monitoring system
        val timestamp = LocalDateTime.now()
        val event = mapOf(
            "timestamp" to timestamp,
            "eventType" to eventType,
            "details" to details
        )
        // println("Data Event: $event") // Uncomment for debugging
    }

    /**
     * Clear all data (use with caution)
     */
    fun clearAllData() {
        participants.clear()
        sessions.clear()
        sessionAnalytics.clear()
        feedback.clear()
        abTestResults.clear()
        surveySubmissions.clear()
        learningMetrics.clear()

        logDataEvent("DATA_CLEARED", emptyMap())
    }

    /**
     * Get data collection statistics
     */
    fun getDataCollectionStats(): DataCollectionStats {
        return DataCollectionStats(
            totalParticipants = participants.size,
            totalSessions = sessions.size,
            activeSessions = sessions.values.count { it.status == SessionStatus.IN_PROGRESS },
            completedSessions = sessions.values.count { it.status == SessionStatus.COMPLETED },
            totalFeedback = feedback.size,
            totalSurveySubmissions = surveySubmissions.size,
            totalABTestResults = abTestResults.values.sumOf { it.size },
            totalLearningMetrics = learningMetrics.values.sumOf { it.size },
            storageSizeBytes = estimateStorageSize()
        )
    }

    /**
     * Estimate storage size of collected data
     */
    private fun estimateStorageSize(): Long {
        // Rough estimation in bytes
        val participantSize = participants.size * 500L
        val sessionSize = sessions.size * 1000L
        val analyticsSize = sessionAnalytics.size * 5000L
        val feedbackSize = feedback.size * 2000L
        val abTestSize = abTestResults.values.sumOf { it.size } * 200L

        return participantSize + sessionSize + analyticsSize + feedbackSize + abTestSize
    }
}

/**
 * Export data for a single participant
 */
data class ParticipantDataExport(
    val participant: TestParticipant,
    val sessions: List<TestingSession>,
    val analytics: List<UsageAnalytics>,
    val feedback: List<UserFeedback>,
    val learningMetrics: List<LearningMetrics>,
    val surveys: List<SurveySubmission>,
    val exportedAt: LocalDateTime
)

/**
 * Complete data export
 */
data class CompleteDataExport(
    val participants: List<TestParticipant>,
    val sessions: List<TestingSession>,
    val analytics: List<UsageAnalytics>,
    val feedback: List<UserFeedback>,
    val abTestResults: List<ABTestResult>,
    val surveySubmissions: List<SurveySubmission>,
    val learningMetrics: List<LearningMetrics>,
    val exportedAt: LocalDateTime
)

/**
 * Aggregated report for analysis
 */
data class AggregatedReport(
    val totalParticipants: Int,
    val totalSessions: Int,
    val totalPuzzlesCompleted: Int,
    val averageCompletionRate: Double,
    val averageHintsPerPuzzle: Double,
    val ageGroupBreakdown: Map<AgeGroup, Int>,
    val difficultyDistribution: Map<DifficultyLevel, Int>,
    val generatedAt: LocalDateTime
)

/**
 * Data collection statistics
 */
data class DataCollectionStats(
    val totalParticipants: Int,
    val totalSessions: Int,
    val activeSessions: Int,
    val completedSessions: Int,
    val totalFeedback: Int,
    val totalSurveySubmissions: Int,
    val totalABTestResults: Int,
    val totalLearningMetrics: Int,
    val storageSizeBytes: Long
)

/**
 * Singleton instance for easy access
 */
object DataCollection {
    private val instance = DataCollectionSystem()

    fun getInstance(): DataCollectionSystem = instance
}
