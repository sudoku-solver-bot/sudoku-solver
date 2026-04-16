package will.sudoku.solver

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Age groups for user testing protocols
 */
enum class AgeGroup(val displayName: String, val minAge: Int, val maxAge: Int) {
    YOUNG("Young Learners", 8, 10),
    OLD("Older Learners", 11, 14);

    companion object {
        fun fromAge(age: Int): AgeGroup {
            return if (age <= 10) YOUNG else OLD
        }
    }
}

/**
 * Testing session status
 */
enum class SessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

/**
 * Types of A/B tests for educational features
 */
enum class ABTestVariant {
    CONTROL,
    VARIANT_A,
    VARIANT_B
}

/**
 * Educational feature flags for A/B testing
 */
enum class EducationalFeature(val displayName: String, val description: String) {
    HINT_SYSTEM("Hint System", "Progressive hints when stuck"),
    STEP_BY_STEP("Step-by-Step Guide", "Detailed explanation of each step"),
    VISUAL_FEEDBACK("Visual Feedback", "Color-coded cells and candidates"),
    CELEBRATION("Celebration Animations", "Reward animations on completion"),
    PROGRESS_BAR("Progress Bar", "Visual progress tracking"),
    TIMER("Timer", "Optional timer for solving speed"),
    ERROR_HIGHLIGHTING("Error Highlighting", "Visual indication of mistakes"),
    TUTORIAL_MODE("Tutorial Mode", "Guided tutorials for techniques")
}

/**
 * Test phase in the user testing protocol
 */
enum class TestPhase {
    ONBOARDING,
    TUTORIAL,
    PRACTICE,
    ASSESSMENT,
    FEEDBACK
}

/**
 * User testing participant
 */
data class TestParticipant(
    val participantId: String,
    val age: Int,
    val ageGroup: AgeGroup,
    val parentEmail: String,
    val childName: String,
    val startDate: LocalDate = LocalDate.now(),
    val assignedVariant: ABTestVariant = ABTestVariant.CONTROL,
    val enabledFeatures: Set<EducationalFeature> = emptySet()
)

/**
 * Single testing session for a participant
 */
data class TestingSession(
    val sessionId: String,
    val participantId: String,
    val phase: TestPhase,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val status: SessionStatus = SessionStatus.NOT_STARTED,
    val puzzleId: String? = null,
    val difficultyLevel: DifficultyLevel? = null
)

/**
 * Action taken during a testing session
 */
data class SessionAction(
    val actionId: String,
    val sessionId: String,
    val timestamp: LocalDateTime,
    val actionType: ActionType,
    val details: Map<String, Any> = emptyMap()
)

/**
 * Types of actions users can take
 */
enum class ActionType {
    PUZZLE_STARTED,
    PUZZLE_COMPLETED,
    PUZZLE_ABANDONED,
    HINT_REQUESTED,
    HINT_RECEIVED,
    ERROR_MADE,
    ERROR_CORRECTED,
    STEP_COMPLETED,
    HELP_VIEWED,
    CELEBRATION_TRIGGERED,
    FEATURE_USED,
    DIFFICULTY_CHANGED,
    TIME_ELAPSED
}

/**
 * User feedback from surveys
 */
data class UserFeedback(
    val feedbackId: String,
    val sessionId: String,
    val participantId: String,
    val timestamp: LocalDateTime,
    val ageGroup: AgeGroup,
    val responses: Map<String, SurveyResponse>,
    val overallRating: Int, // 1-5 scale
    val comments: String? = null
)

/**
 * Individual survey response
 */
sealed class SurveyResponse {
    data class RatingResponse(val value: Int) : SurveyResponse()
    data class TextResponse(val text: String) : SurveyResponse()
    data class MultipleChoiceResponse(val choices: List<String>) : SurveyResponse()
    data class BooleanResponse(val value: Boolean) : SurveyResponse()
}

/**
 * Learning metrics tracked over time
 */
data class LearningMetrics(
    val participantId: String,
    val date: LocalDate,
    val puzzlesAttempted: Int,
    val puzzlesCompleted: Int,
    val averageCompletionTime: Double, // seconds
    val hintsUsed: Int,
    val mistakesMade: Int,
    val mistakesCorrectedIndependently: Int,
    val techniquesLearned: Set<String>,
    val currentLevel: Int,
    val xpGained: Int
)

/**
 * A/B test result data
 */
data class ABTestResult(
    val testId: String,
    val feature: EducationalFeature,
    val variant: ABTestVariant,
    val participantId: String,
    val sessionId: String,
    val metricType: ABTestMetric,
    val value: Double,
    val timestamp: LocalDateTime
)

/**
 * Metrics tracked in A/B tests
 */
enum class ABTestMetric {
    COMPLETION_RATE,
    AVERAGE_COMPLETION_TIME,
    HINT_USAGE_RATE,
    ERROR_RATE,
    ENGAGEMENT_TIME,
    SATISFACTION_SCORE,
    LEARNING_VELOCITY,
    RETENTION_RATE
}

/**
 * Survey questions organized by age group
 */
data class SurveyQuestion(
    val questionId: String,
    val questionText: String,
    val ageGroup: AgeGroup,
    val responseType: ResponseType,
    val category: QuestionCategory
)

/**
 * Types of survey responses
 */
enum class ResponseType {
    RATING_1_5,
    RATING_1_10,
    YES_NO,
    MULTIPLE_CHOICE,
    TEXT,
    EMOJI_RATING
}

/**
 * Categories of survey questions
 */
enum class QuestionCategory {
    USABILITY,
    ENGAGEMENT,
    LEARNING_EFFECTIVENESS,
    SATISFACTION,
    DIFFICULTY_APPROPRIATENESS,
    FEATURE_FEEDBACK
}
