package com.sudoku.testing.feedback

import java.time.LocalDateTime

/**
 * Collects and manages feedback from participants during educational testing sessions.
 */
class FeedbackCollector {
    private val feedbackEntries = mutableListOf<FeedbackEntry>()

    /**
     * Records feedback from a participant.
     */
    fun recordFeedback(feedback: FeedbackEntry) {
        feedbackEntries.add(feedback)
    }

    /**
     * Gets all feedback entries.
     */
    fun getAllFeedback(): List<FeedbackEntry> = feedbackEntries.toList()

    /**
     * Gets feedback for a specific participant.
     */
    fun getFeedbackForParticipant(participantId: String): List<FeedbackEntry> {
        return feedbackEntries.filter { it.participantId == participantId }
    }

    /**
     * Gets feedback by category.
     */
    fun getFeedbackByCategory(category: FeedbackCategory): List<FeedbackEntry> {
        return feedbackEntries.filter { it.category == category }
    }

    /**
     * Gets feedback for a specific session.
     */
    fun getFeedbackForSession(sessionId: String): List<FeedbackEntry> {
        return feedbackEntries.filter { it.sessionId == sessionId }
    }

    /**
     * Clears all feedback entries (useful for testing).
     */
    fun clear() {
        feedbackEntries.clear()
    }

    /**
     * Aggregates quantitative feedback metrics.
     */
    fun aggregateMetrics(): FeedbackMetrics {
        if (feedbackEntries.isEmpty()) {
            return FeedbackMetrics()
        }

        val enjoymentScores = feedbackEntries.mapNotNull { it.enjoymentRating }
        val difficultyScores = feedbackEntries.mapNotNull { it.difficultyRating }
        val usabilityScores = feedbackEntries.mapNotNull { it.usabilityRating }

        return FeedbackMetrics(
            averageEnjoyment = if (enjoymentScores.isNotEmpty()) enjoymentScores.average() else 0.0,
            averageDifficulty = if (difficultyScores.isNotEmpty()) difficultyScores.average() else 0.0,
            averageUsability = if (usabilityScores.isNotEmpty()) usabilityScores.average() else 0.0,
            totalEntries = feedbackEntries.size,
            categoriesWithFeedback = feedbackEntries.map { it.category }.toSet().size
        )
    }
}

/**
 * Represents a single feedback entry from a participant.
 */
data class FeedbackEntry(
    val participantId: String,
    val sessionId: String,
    val category: FeedbackCategory,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val enjoymentRating: Int? = null,      // 1-5 scale
    val difficultyRating: Int? = null,     // 1-5 scale
    val usabilityRating: Int? = null,      // 1-5 scale
    val comments: String = ""
) {
    init {
        enjoymentRating?.let { require(it in 1..5) { "Enjoyment rating must be 1-5" } }
        difficultyRating?.let { require(it in 1..5) { "Difficulty rating must be 1-5" } }
        usabilityRating?.let { require(it in 1..5) { "Usability rating must be 1-5" } }
    }
}

/**
 * Categories of feedback for organization.
 */
enum class FeedbackCategory {
    PUZZLE_DIFFICULTY,
    INTERFACE_USABILITY,
    LEARNING_PROGRESS,
    HINT_EFFECTIVENESS,
    MOTIVATION_ENGAGEMENT,
    TECHNICAL_ISSUES,
    GENERAL_SATISFACTION,
    SUGGESTIONS_IMPROVEMENTS
}

/**
 * Aggregated metrics from feedback entries.
 */
data class FeedbackMetrics(
    val averageEnjoyment: Double = 0.0,
    val averageDifficulty: Double = 0.0,
    val averageUsability: Double = 0.0,
    val totalEntries: Int = 0,
    val categoriesWithFeedback: Int = 0
) {
    /**
     * Calculates overall satisfaction score (0-100 scale).
     */
    fun overallSatisfactionScore(): Double {
        if (totalEntries == 0) return 0.0
        val averageRating = (averageEnjoyment + averageUsability) / 2.0
        return (averageRating / 5.0) * 100.0
    }

    /**
     * Creates a human-readable summary.
     */
    fun createSummary(): String {
        return """
            |Feedback Metrics Summary:
            | - Total Entries: $totalEntries
            | - Categories Covered: $categoriesWithFeedback
            | - Average Enjoyment: ${String.format("%.2f", averageEnjoyment)}/5.0
            | - Average Difficulty: ${String.format("%.2f", averageDifficulty)}/5.0
            | - Average Usability: ${String.format("%.2f", averageUsability)}/5.0
            | - Overall Satisfaction: ${String.format("%.1f", overallSatisfactionScore())}%
        """.trimMargin()
    }
}

/**
 * Collects responses to specific survey questions.
 */
class SurveyResponseCollector {
    private val responses = mutableMapOf<String, SurveyResponse>()

    /**
     * Records a survey response.
     */
    fun recordResponse(response: SurveyResponse) {
        responses[response.responseId] = response
    }

    /**
     * Gets all responses.
     */
    fun getAllResponses(): List<SurveyResponse> = responses.values.toList()

    /**
     * Gets responses for a specific participant.
     */
    fun getResponsesForParticipant(participantId: String): List<SurveyResponse> {
        return responses.values.filter { it.participantId == participantId }
    }

    /**
     * Clears all responses.
     */
    fun clear() {
        responses.clear()
    }
}

/**
 * Represents a response to a survey question.
 */
data class SurveyResponse(
    val responseId: String,
    val participantId: String,
    val sessionId: String,
    val questionId: String,
    val answer: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Multiple choice question definition for surveys.
 */
data class SurveyQuestion(
    val questionId: String,
    val questionText: String,
    val options: List<String>,
    val allowsMultipleChoice: Boolean = false,
    val ageAppropriate: Boolean = true  // Designed for 8-14 age range
) {
    init {
        require(options.isNotEmpty()) { "Question must have at least one option" }
        require(questionText.isNotBlank()) { "Question text cannot be blank" }
    }
}
