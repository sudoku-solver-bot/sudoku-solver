package com.sudoku.testing

import com.sudoku.testing.participants.Participant
import com.sudoku.testing.participants.ParticipantSession
import com.sudoku.testing.participants.ExperienceLevel
import com.sudoku.testing.feedback.FeedbackCollector
import com.sudoku.testing.protocols.EducationalTestingProtocol
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Core testing framework for educational validation of sudoku solver for kids aged 8-14.
 * Manages participants, sessions, testing protocols, and results.
 */
class UserTestingFramework {
    private val participants = mutableMapOf<String, Participant>()
    private val sessions = mutableMapOf<String, ParticipantSession>()
    private val feedbackCollector = FeedbackCollector()
    private val testingProtocol = EducationalTestingProtocol()
    private val testingHistory = mutableListOf<TestingSessionRecord>()

    /**
     * Registers a new participant in the testing program.
     */
    fun registerParticipant(
        age: Int,
        gradeLevel: Int,
        parentEmail: String,
        previousSudokuExperience: ExperienceLevel = ExperienceLevel.NONE,
        notes: String = ""
    ): Participant {
        require(age in 8..14) { "Participant age must be between 8 and 14" }
        require(gradeLevel in 2..8) { "Grade level must be between 2 and 8" }
        require(parentEmail.isNotBlank()) { "Parent email cannot be blank" }

        val participant = Participant(
            id = UUID.randomUUID().toString(),
            age = age,
            gradeLevel = gradeLevel,
            parentEmail = parentEmail,
            consentDate = LocalDate.now(),
            previousSudokuExperience = previousSudokuExperience,
            notes = notes
        )

        participants[participant.id] = participant
        return participant
    }

    /**
     * Gets all registered participants.
     */
    fun getAllParticipants(): List<Participant> = participants.values.toList()

    /**
     * Gets participants by age group.
     */
    fun getParticipantsByAgeGroup(ageGroup: com.sudoku.testing.participants.AgeGroup): List<Participant> {
        return participants.values.filter { it.getAgeGroup() == ageGroup }
    }

    /**
     * Gets participants by experience level.
     */
    fun getParticipantsByExperienceLevel(experienceLevel: ExperienceLevel): List<Participant> {
        return participants.values.filter { it.previousSudokuExperience == experienceLevel }
    }

    /**
     * Starts a testing session for a participant.
     */
    fun startTestingSession(participantId: String): ParticipantSession {
        val participant = participants[participantId]
            ?: throw IllegalArgumentException("Participant with ID $participantId not found")

        val sessionId = UUID.randomUUID().toString()
        val session = ParticipantSession(
            sessionId = sessionId,
            participantId = participantId
        )

        sessions[sessionId] = session
        return session
    }

    /**
     * Ends a testing session.
     */
    fun endTestingSession(sessionId: String) {
        val session = sessions[sessionId]
            ?: throw IllegalArgumentException("Session with ID $sessionId not found")

        session.complete()
        testingHistory.add(TestingSessionRecord(session, LocalDateTime.now()))
    }

    /**
     * Gets all active sessions.
     */
    fun getActiveSessions(): List<ParticipantSession> {
        return sessions.values.filter { !it.completed }
    }

    /**
     * Gets completed sessions for a participant.
     */
    fun getParticipantCompletedSessions(participantId: String): List<ParticipantSession> {
        return sessions.values.filter { it.participantId == participantId && it.completed }
    }

    /**
     * Records puzzle completion in a session.
     */
    fun recordPuzzleCompletion(sessionId: String, solved: Boolean) {
        val session = sessions[sessionId]
            ?: throw IllegalArgumentException("Session with ID $sessionId not found")

        session.recordPuzzleAttempt(solved)
    }

    /**
     * Records hint usage in a session.
     */
    fun recordHintUsage(sessionId: String) {
        val session = sessions[sessionId]
            ?: throw IllegalArgumentException("Session with ID $sessionId not found")

        session.recordHint()
    }

    /**
     * Records feedback for a session.
     */
    fun recordFeedback(sessionId: String, feedback: String, category: com.sudoku.testing.feedback.FeedbackCategory) {
        val session = sessions[sessionId]
            ?: throw IllegalArgumentException("Session with ID $sessionId not found")

        val feedbackEntry = com.sudoku.testing.feedback.FeedbackEntry(
            participantId = session.participantId,
            sessionId = sessionId,
            category = category,
            comments = feedback
        )

        feedbackCollector.recordFeedback(feedbackEntry)
    }

    /**
     * Gets testing progress for a participant.
     */
    fun getParticipantProgress(participantId: String): ParticipantProgress {
        val participant = participants[participantId]
            ?: throw IllegalArgumentException("Participant with ID $participantId not found")

        val completedSessions = getParticipantCompletedSessions(participantId)
        val totalSessions = completedSessions.size
        val totalPuzzlesAttempted = completedSessions.sumOf { it.puzzlesAttempted }
        val totalPuzzlesSolved = completedSessions.sumOf { it.puzzlesSolved }
        val totalHintsUsed = completedSessions.sumOf { it.totalHintsUsed }
        val totalTimeSeconds = completedSessions.sumOf { it.totalTimeSeconds }

        return ParticipantProgress(
            participant = participant,
            totalSessions = totalSessions,
            totalPuzzlesAttempted = totalPuzzlesAttempted,
            totalPuzzlesSolved = totalPuzzlesSolved,
            completionRate = if (totalPuzzlesAttempted > 0) {
                totalPuzzlesSolved.toDouble() / totalPuzzlesAttempted.toDouble()
            } else 0.0,
            totalHintsUsed = totalHintsUsed,
            totalTimeSeconds = totalTimeSeconds,
            averageSessionTime = if (totalSessions > 0) totalTimeSeconds / totalSessions else 0L
        )
    }

    /**
     * Calculates educational effectiveness metrics.
     */
    fun calculateEducationalEffectiveness(): EducationalEffectivenessMetrics {
        val allParticipants = getAllParticipants()
        val youngerParticipants = getParticipantsByAgeGroup(com.sudoku.testing.participants.AgeGroup.YOUNGER)
        val olderParticipants = getParticipantsByAgeGroup(com.sudoku.testing.participants.AgeGroup.OLDER)

        val overallProgress = allParticipants.map { getParticipantProgress(it.id) }
        val youngerProgress = youngerParticipants.map { getParticipantProgress(it.id) }
        val olderProgress = olderParticipants.map { getParticipantProgress(it.id) }

        val overallAverageCompletionRate = overallProgress.map { it.completionRate }.average()
        val youngerAverageCompletionRate = youngerProgress.map { it.completionRate }.average()
        val olderAverageCompletionRate = olderProgress.map { it.completionRate }.average()

        val overallAverageHintsUsed = overallProgress.map { it.totalHintsUsed }.average()
        val youngerAverageHintsUsed = youngerProgress.map { it.totalHintsUsed }.average()
        val olderAverageHintsUsed = olderProgress.map { it.totalHintsUsed }.average()

        val overallAverageSessionTime = overallProgress.map { it.averageSessionTime }.average()
        val youngerAverageSessionTime = youngerProgress.map { it.averageSessionTime }.average()
        val olderAverageSessionTime = olderProgress.map { it.averageSessionTime }.average()

        return EducationalEffectivenessMetrics(
            totalParticipants = allParticipants.size,
            youngerParticipants = youngerParticipants.size,
            olderParticipants = olderParticipants.size,
            overallAverageCompletionRate = overallAverageCompletionRate,
            youngerAverageCompletionRate = youngerAverageCompletionRate,
            olderAverageCompletionRate = olderAverageCompletionRate,
            overallAverageHintsUsed = overallAverageHintsUsed,
            youngerAverageHintsUsed = youngerAverageHintsUsed,
            olderAverageHintsUsed = olderAverageHintsUsed,
            overallAverageSessionTime = overallAverageSessionTime,
            youngerAverageSessionTime = youngerAverageSessionTime,
            olderAverageSessionTime = olderAverageSessionTime,
            feedbackMetrics = feedbackCollector.aggregateMetrics()
        )
    }

    /**
     * Gets testing session history.
     */
    fun getTestingHistory(): List<TestingSessionRecord> = testingHistory.toList()

    /**
     * Generates a comprehensive testing report.
     */
    fun generateTestingReport(): TestingReport {
        val effectiveness = calculateEducationalEffectiveness()
        val feedbackMetrics = feedbackCollector.aggregateMetrics()
        
        return TestingReport(
            generatedAt = LocalDateTime.now(),
            totalParticipants = participants.size,
            totalSessions = sessions.values.count { it.completed },
            totalTestingHistory = testingHistory.size,
            effectivenessMetrics = effectiveness,
            feedbackSummary = feedbackMetrics.createSummary(),
            protocolRecommendations = testingProtocol.getRecommendations(effectiveness)
        )
    }

    /**
     * Clears all testing data (useful for testing).
     */
    fun clearAllData() {
        participants.clear()
        sessions.clear()
        feedbackCollector.clear()
        testingHistory.clear()
    }
}

/**
 * Represents progress tracking for a participant.
 */
data class ParticipantProgress(
    val participant: Participant,
    val totalSessions: Int,
    val totalPuzzlesAttempted: Int,
    val totalPuzzlesSolved: Int,
    val completionRate: Double,
    val totalHintsUsed: Int,
    val totalTimeSeconds: Long,
    val averageSessionTime: Long
)

/**
 * Represents educational effectiveness metrics.
 */
data class EducationalEffectivenessMetrics(
    val totalParticipants: Int,
    val youngerParticipants: Int,
    val olderParticipants: Int,
    val overallAverageCompletionRate: Double,
    val youngerAverageCompletionRate: Double,
    val olderAverageCompletionRate: Double,
    val overallAverageHintsUsed: Double,
    val youngerAverageHintsUsed: Double,
    val olderAverageHintsUsed: Double,
    val overallAverageSessionTime: Double, // in seconds
    val youngerAverageSessionTime: Double,
    val olderAverageSessionTime: Double,
    val feedbackMetrics: com.sudoku.testing.feedback.FeedbackMetrics
)

/**
 * Represents a record of a completed testing session.
 */
data class TestingSessionRecord(
    val session: ParticipantSession,
    val completedAt: LocalDateTime
)

/**
 * Represents a comprehensive testing report.
 */
data class TestingReport(
    val generatedAt: LocalDateTime,
    val totalParticipants: Int,
    val totalSessions: Int,
    val totalTestingHistory: Int,
    val effectivenessMetrics: EducationalEffectivenessMetrics,
    val feedbackSummary: String,
    val protocolRecommendations: List<String>
)