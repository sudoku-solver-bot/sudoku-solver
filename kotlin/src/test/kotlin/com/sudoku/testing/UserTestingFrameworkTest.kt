package com.sudoku.testing

import com.sudoku.testing.participants.Participant
import com.sudoku.testing.participants.ExperienceLevel
import com.sudoku.testing.participants.AgeGroup
import com.sudoku.testing.feedback.FeedbackCategory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

/**
 * Unit tests for the UserTestingFramework class.
 */
class UserTestingFrameworkTest {

    private fun createTestingFramework(): UserTestingFramework {
        return UserTestingFramework()
    }

    private fun createTestParticipant(): Participant {
        return Participant(
            id = "test-participant-123",
            age = 10,
            gradeLevel = 4,
            parentEmail = "parent@example.com",
            consentDate = LocalDate.now(),
            previousSudokuExperience = ExperienceLevel.BEGINNER
        )
    }

    @Test
    fun `Register new participant should succeed`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 12,
            gradeLevel = 6,
            parentEmail = "test@example.com",
            previousSudokuExperience = ExperienceLevel.NONE
        )

        assertEquals(12, participant.age)
        assertEquals(6, participant.gradeLevel)
        assertEquals("test@example.com", participant.parentEmail)
        assertEquals(ExperienceLevel.NONE, participant.previousSudokuExperience)
        assertEquals(AgeGroup.OLDER, participant.getAgeGroup())
    }

    @Test
    fun `Register participant with invalid age should throw exception`() {
        val framework = createTestingFramework()
        
        assertThrows(IllegalArgumentException::class.java) {
            framework.registerParticipant(
                age = 7, // Too young
                gradeLevel = 2,
                parentEmail = "test@example.com"
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            framework.registerParticipant(
                age = 15, // Too old
                gradeLevel = 8,
                parentEmail = "test@example.com"
            )
        }
    }

    @Test
    fun `Register participant with invalid grade should throw exception`() {
        val framework = createTestingFramework()
        
        assertThrows(IllegalArgumentException::class.java) {
            framework.registerParticipant(
                age = 10,
                gradeLevel = 1, // Too low
                parentEmail = "test@example.com"
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            framework.registerParticipant(
                age = 10,
                gradeLevel = 9, // Too high
                parentEmail = "test@example.com"
            )
        }
    }

    @Test
    fun `Get all participants should return empty list initially`() {
        val framework = createTestingFramework()
        val participants = framework.getAllParticipants()
        assertTrue(participants.isEmpty())
    }

    @Test
    fun `Get participants by age group should work correctly`() {
        val framework = createTestingFramework()
        
        // Register participants of different ages
        val youngParticipant = framework.registerParticipant(
            age = 9,
            gradeLevel = 3,
            parentEmail = "young@example.com"
        )
        
        val olderParticipant = framework.registerParticipant(
            age = 12,
            gradeLevel = 6,
            parentEmail = "older@example.com"
        )

        val youngerParticipants = framework.getParticipantsByAgeGroup(AgeGroup.YOUNGER)
        val olderParticipants = framework.getParticipantsByAgeGroup(AgeGroup.OLDER)

        assertEquals(1, youngerParticipants.size)
        assertEquals(1, olderParticipants.size)
        assertEquals(youngParticipant, youngerParticipants.first())
        assertEquals(olderParticipant, olderParticipants.first())
    }

    @Test
    fun `Start and end testing session should work correctly`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 11,
            gradeLevel = 5,
            parentEmail = "test@example.com"
        )

        // Start session
        val session = framework.startTestingSession(participant.id)
        assertEquals(participant.id, session.participantId)
        assertFalse(session.completed)

        // End session
        framework.endTestingSession(session.sessionId)
        assertTrue(session.completed)
        assertNotNull(session.endTime)
    }

    @Test
    fun `Start testing session with invalid participant should throw exception`() {
        val framework = createTestingFramework()
        
        assertThrows(IllegalArgumentException::class.java) {
            framework.startTestingSession("non-existent-participant")
        }
    }

    @Test
    fun `Record puzzle completion should update session`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 10,
            gradeLevel = 4,
            parentEmail = "test@example.com"
        )

        val session = framework.startTestingSession(participant.id)

        // Record puzzle attempts
        framework.recordPuzzleCompletion(session.sessionId, true)  // Solved
        framework.recordPuzzleCompletion(session.sessionId, false) // Not solved
        framework.recordPuzzleCompletion(session.sessionId, true)  // Solved

        framework.endTestingSession(session.sessionId)

        assertEquals(3, session.puzzlesAttempted)
        assertEquals(2, session.puzzlesSolved)
        assertEquals(0.67, session.completionRate(), 0.01)
    }

    @Test
    fun `Record hint usage should update session`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 11,
            gradeLevel = 5,
            parentEmail = "test@example.com"
        )

        val session = framework.startTestingSession(participant.id)

        // Record some hint usage
        framework.recordHintUsage(session.sessionId)
        framework.recordHintUsage(session.sessionId)

        framework.endTestingSession(session.sessionId)

        assertEquals(2, session.totalHintsUsed)
    }

    @Test
    fun `Record feedback should work correctly`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 9,
            gradeLevel = 3,
            parentEmail = "test@example.com"
        )

        val session = framework.startTestingSession(participant.id)
        
        // Record feedback
        framework.recordFeedback(
            sessionId = session.sessionId,
            feedback = "The interface was easy to use!",
            category = FeedbackCategory.INTERFACE_USABILITY
        )

        framework.endTestingSession(session.sessionId)

        val feedback = framework.getParticipantCompletedSessions(participant.id).first()
        assertEquals(session.sessionId, feedback.sessionId)
    }

    @Test
    fun `Get participant progress should calculate metrics correctly`() {
        val framework = createTestingFramework()
        val participant = framework.registerParticipant(
            age = 10,
            gradeLevel = 4,
            parentEmail = "test@example.com"
        )

        // Create and complete a session
        val session = framework.startTestingSession(participant.id)
        framework.recordPuzzleCompletion(session.sessionId, true)
        framework.recordPuzzleCompletion(session.sessionId, true)
        framework.recordPuzzleCompletion(session.sessionId, false)
        framework.endTestingSession(session.sessionId)

        val progress = framework.getParticipantProgress(participant.id)

        assertEquals(1, progress.totalSessions)
        assertEquals(3, progress.totalPuzzlesAttempted)
        assertEquals(2, progress.totalPuzzlesSolved)
        assertEquals(0.67, progress.completionRate, 0.01)
        assertEquals(0, progress.totalHintsUsed)
    }

    @Test
    fun `Calculate educational effectiveness should generate metrics`() {
        val framework = createTestingFramework()
        
        // Register participants
        val participant1 = framework.registerParticipant(
            age = 9,
            gradeLevel = 3,
            parentEmail = "parent1@example.com"
        )
        
        val participant2 = framework.registerParticipant(
            age = 12,
            gradeLevel = 6,
            parentEmail = "parent2@example.com"
        )

        // Complete sessions for both participants
        val session1 = framework.startTestingSession(participant1.id)
        framework.recordPuzzleCompletion(session1.sessionId, true)
        framework.recordPuzzleCompletion(session1.sessionId, true)
        framework.endTestingSession(session1.sessionId)

        val session2 = framework.startTestingSession(participant2.id)
        framework.recordPuzzleCompletion(session2.sessionId, true)
        framework.recordPuzzleCompletion(session2.sessionId, false)
        framework.recordPuzzleCompletion(session2.sessionId, true)
        framework.endTestingSession(session2.sessionId)

        val effectiveness = framework.calculateEducationalEffectiveness()

        assertEquals(2, effectiveness.totalParticipants)
        assertEquals(1, effectiveness.youngerParticipants)
        assertEquals(1, effectiveness.olderParticipants)
        assertEquals(0.83, effectiveness.overallAverageCompletionRate, 0.01)
        assertEquals(1.0, effectiveness.youngerAverageCompletionRate, 0.01)
        assertEquals(0.67, effectiveness.olderAverageCompletionRate, 0.01)
    }

    @Test
    fun `Generate testing report should create comprehensive report`() {
        val framework = createTestingFramework()
        
        // Register and test a participant
        val participant = framework.registerParticipant(
            age = 10,
            gradeLevel = 4,
            parentEmail = "test@example.com"
        )

        val session = framework.startTestingSession(participant.id)
        framework.recordPuzzleCompletion(session.sessionId, true)
        framework.endTestingSession(session.sessionId)

        val report = framework.generateTestingReport()

        assertNotNull(report.generatedAt)
        assertEquals(1, report.totalParticipants)
        assertEquals(1, report.totalSessions)
        assertTrue(report.feedbackSummary.isNotEmpty())
        assertTrue(report.protocolRecommendations.isNotEmpty())
    }

    @Test
    fun `Clear all data should reset framework state`() {
        val framework = createTestingFramework()

        // Add some data
        val participant = framework.registerParticipant(
            age = 10,
            gradeLevel = 4,
            parentEmail = "test@example.com"
        )

        val session = framework.startTestingSession(participant.id)
        framework.recordPuzzleCompletion(session.sessionId, true)
        framework.endTestingSession(session.sessionId)

        // Verify data exists
        assertEquals(1, framework.getAllParticipants().size)

        // Clear all data
        framework.clearAllData()

        // Verify data is cleared
        assertTrue(framework.getAllParticipants().isEmpty())
        assertTrue(framework.getActiveSessions().isEmpty())
    }

    @Test
    fun `Edge case - no participants should return empty lists`() {
        val framework = createTestingFramework()
        
        val participants = framework.getAllParticipants()
        val youngerParticipants = framework.getParticipantsByAgeGroup(AgeGroup.YOUNGER)
        val olderParticipants = framework.getParticipantsByAgeGroup(AgeGroup.OLDER)

        assertTrue(participants.isEmpty())
        assertTrue(youngerParticipants.isEmpty())
        assertTrue(olderParticipants.isEmpty())
    }

    @Test
    fun `Edge case - invalid operations should handle gracefully`() {
        val framework = createTestingFramework()
        
        // Try to start session with non-existent participant
        assertThrows(IllegalArgumentException::class.java) {
            framework.startTestingSession("non-existent")
        }

        // Try to end non-existent session
        assertThrows(IllegalArgumentException::class.java) {
            framework.endTestingSession("non-existent-session")
        }

        // Try to record puzzle completion for non-existent session
        assertThrows(IllegalArgumentException::class.java) {
            framework.recordPuzzleCompletion("non-existent-session", true)
        }
    }
}