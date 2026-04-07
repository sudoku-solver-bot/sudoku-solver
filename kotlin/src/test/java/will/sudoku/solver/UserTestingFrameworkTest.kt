package will.sudoku.solver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

class UserTestingFrameworkTest {

    private lateinit var dataCollection: DataCollectionSystem
    private lateinit var progressSystem: ProgressTrackingSystem

    @BeforeEach
    fun setup() {
        dataCollection = DataCollectionSystem()
        progressSystem = ProgressTrackingSystem(dataCollection)
    }

    @Test
    fun `test age group classification`() {
        assertEquals(AgeGroup.YOUNG, AgeGroup.fromAge(8))
        assertEquals(AgeGroup.YOUNG, AgeGroup.fromAge(10))
        assertEquals(AgeGroup.OLD, AgeGroup.fromAge(11))
        assertEquals(AgeGroup.OLD, AgeGroup.fromAge(14))
    }

    @Test
    fun `test difficulty level for age`() {
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.forAge(8))
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.forAge(10))
        assertEquals(DifficultyLevel.HARD, DifficultyLevel.forAge(12))
        assertEquals(DifficultyLevel.EXPERT, DifficultyLevel.forAge(14))
    }

    @Test
    fun `test participant registration`() {
        val participant = TestParticipant(
            participantId = "test-001",
            age = 9,
            ageGroup = AgeGroup.YOUNG,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        val registered = dataCollection.registerParticipant(participant)

        assertEquals(participant.participantId, registered.participantId)
        assertEquals(AgeGroup.YOUNG, registered.ageGroup)
    }

    @Test
    fun `test session creation and completion`() {
        val participant = TestParticipant(
            participantId = "test-002",
            age = 12,
            ageGroup = AgeGroup.OLD,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        val sessionId = "session-001"
        val session = dataCollection.startSession(
            sessionId = sessionId,
            participantId = participant.participantId,
            phase = TestPhase.PRACTICE,
            puzzleId = "puzzle-001",
            difficultyLevel = DifficultyLevel.MEDIUM
        )

        assertEquals(sessionId, session.sessionId)
        assertEquals(participant.participantId, session.participantId)
        assertEquals(TestPhase.PRACTICE, session.phase)
        assertEquals(SessionStatus.IN_PROGRESS, session.status)

        // Track some actions
        dataCollection.trackAction(
            sessionId = sessionId,
            participantId = participant.participantId,
            actionType = ActionType.PUZZLE_STARTED,
            details = mapOf("puzzleId" to "puzzle-001")
        )

        dataCollection.trackAction(
            sessionId = sessionId,
            participantId = participant.participantId,
            actionType = ActionType.HINT_REQUESTED,
            details = mapOf("hintType" to "single-candidate")
        )

        // End session
        val analytics = dataCollection.endSession(sessionId)

        assertNotNull(analytics)
        assertEquals(sessionId, analytics!!.sessionId)
        assertEquals(participant.participantId, analytics.participantId)
    }

    @Test
    fun `test protocol creation for young learners`() {
        val factory = TestingProtocolFactory()
        val protocol = factory.createProtocolForAgeGroup(AgeGroup.YOUNG)

        assertEquals("young-learners-v1", protocol.protocolId)
        assertEquals(AgeGroup.YOUNG, protocol.ageGroup)
        assertEquals(14, protocol.durationDays)
        assertTrue(protocol.requiredFeatures.contains(EducationalFeature.CELEBRATION))
        assertTrue(protocol.requiredFeatures.contains(EducationalFeature.VISUAL_FEEDBACK))
    }

    @Test
    fun `test protocol creation for older learners`() {
        val factory = TestingProtocolFactory()
        val protocol = factory.createProtocolForAgeGroup(AgeGroup.OLD)

        assertEquals("older-learners-v1", protocol.protocolId)
        assertEquals(AgeGroup.OLD, protocol.ageGroup)
        assertEquals(21, protocol.durationDays)
        assertEquals(6, protocol.phases.size)
    }

    @Test
    fun `test survey creation for young learners`() {
        val factory = SurveyFactory()
        val survey = factory.createPostTutorialSurvey(AgeGroup.YOUNG)

        assertEquals("post-tutorial-young", survey.surveyId)
        assertEquals(AgeGroup.YOUNG, survey.targetAgeGroup)
        assertTrue(survey.questions.any { it.responseType == ResponseType.EMOJI_RATING })
    }

    @Test
    fun `test survey creation for older learners`() {
        val factory = SurveyFactory()
        val survey = factory.createFinalSurvey(AgeGroup.OLD)

        assertEquals("final-survey-old", survey.surveyId)
        assertEquals(AgeGroup.OLD, survey.targetAgeGroup)
        assertTrue(survey.questions.size > 8)
    }

    @Test
    fun `test scenario creation for young learners`() {
        val factory = TestCaseScenarioFactory()
        val scenarios = factory.createScenariosForAgeGroup(AgeGroup.YOUNG)

        assertTrue(scenarios.isNotEmpty())
        assertTrue(scenarios.any { it.difficultyLevel == DifficultyLevel.EASY })
        assertTrue(scenarios.all { it.targetAgeGroup == AgeGroup.YOUNG })
    }

    @Test
    fun `test scenario creation for older learners`() {
        val factory = TestCaseScenarioFactory()
        val scenarios = factory.createScenariosForAgeGroup(AgeGroup.OLD)

        assertTrue(scenarios.isNotEmpty())
        assertTrue(scenarios.any { it.difficultyLevel == DifficultyLevel.EXPERT })
        assertTrue(scenarios.all { it.targetAgeGroup == AgeGroup.OLD })
    }

    @Test
    fun `test AB test creation`() {
        val factory = EducationalABTests()
        val test = factory.createHintSystemTest()

        assertEquals("hint-system-001", test.testId)
        assertEquals(EducationalFeature.HINT_SYSTEM, test.feature)
        assertEquals(3, test.variants.size)
        assertTrue(test.trafficSplit.values.sum() > 99.0) // Should sum to ~100%
    }

    @Test
    fun `test AB test variant assignment`() {
        val engine = ABTestEngine()
        val test = EducationalABTests().createHintSystemTest()
        engine.createTest(test)

        // Assign multiple participants and check distribution
        val assignments = mutableMapOf<ABTestVariant, Int>()

        repeat(100) { i ->
            val variant = engine.assignVariant(test.testId, "participant-$i")!!
            assignments[variant] = assignments.getOrDefault(variant, 0) + 1
        }

        // All variants should have some assignments
        assertEquals(3, assignments.size)
        assertTrue(assignments.values.all { it > 0 })
    }

    @Test
    fun `test feedback recording`() {
        val feedback = UserFeedback(
            feedbackId = "feedback-001",
            sessionId = "session-001",
            participantId = "participant-001",
            timestamp = LocalDateTime.now(),
            ageGroup = AgeGroup.YOUNG,
            responses = mapOf(
                "fun-rating" to SurveyResponse.RatingResponse(5),
                "understanding" to SurveyResponse.BooleanResponse(true)
            ),
            overallRating = 5,
            comments = "Great experience!"
        )

        val recorded = dataCollection.recordFeedback(feedback)

        assertEquals(feedback.feedbackId, recorded.feedbackId)
        assertEquals(feedback.participantId, recorded.participantId)
    }

    @Test
    fun `test survey submission`() {
        val submission = SurveySubmission(
            submissionId = "submission-001",
            surveyId = "post-tutorial-young",
            participantId = "participant-001",
            sessionId = "session-001",
            responses = mapOf(
                "fun-rating" to SurveyResponse.RatingResponse(5),
                "understanding" to SurveyResponse.BooleanResponse(true)
            ),
            submittedAt = LocalDateTime.now()
        )

        val submitted = dataCollection.submitSurvey(submission)

        assertEquals(submission.submissionId, submitted.submissionId)
        assertEquals(submission.surveyId, submitted.surveyId)
    }

    @Test
    fun `test learning metrics calculation`() {
        val participant = TestParticipant(
            participantId = "test-003",
            age = 10,
            ageGroup = AgeGroup.YOUNG,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        // Simulate multiple sessions
        repeat(5) { i ->
            val sessionId = "session-$i"
            dataCollection.startSession(
                sessionId = sessionId,
                participantId = participant.participantId,
                phase = TestPhase.PRACTICE,
                puzzleId = "puzzle-$i",
                difficultyLevel = DifficultyLevel.EASY
            )

            dataCollection.trackAction(
                sessionId = sessionId,
                participantId = participant.participantId,
                actionType = ActionType.PUZZLE_COMPLETED,
                details = mapOf("timeSeconds" to 300.0)
            )

            dataCollection.endSession(sessionId)
        }

        val metrics = dataCollection.getLearningMetrics(participant.participantId)

        assertTrue(metrics.isNotEmpty())
        assertTrue(metrics.any { it.puzzlesCompleted > 0 })
    }

    @Test
    fun `test data export`() {
        val participant = TestParticipant(
            participantId = "test-004",
            age = 11,
            ageGroup = AgeGroup.OLD,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        val export = dataCollection.exportParticipantData(participant.participantId)

        assertEquals(participant.participantId, export.participant.participantId)
        assertNotNull(export.exportedAt)
    }

    @Test
    fun `test aggregated report generation`() {
        // Add some test data
        repeat(10) { i ->
            val participant = TestParticipant(
                participantId = "participant-$i",
                age = if (i % 2 == 0) 9 else 12,
                ageGroup = if (i % 2 == 0) AgeGroup.YOUNG else AgeGroup.OLD,
                parentEmail = "test@example.com",
                childName = "Child $i",
                startDate = LocalDate.now()
            )

            dataCollection.registerParticipant(participant)

            val sessionId = "session-$i"
            dataCollection.startSession(
                sessionId = sessionId,
                participantId = participant.participantId,
                phase = TestPhase.PRACTICE,
                puzzleId = "puzzle-$i",
                difficultyLevel = DifficultyLevel.MEDIUM
            )

            dataCollection.endSession(sessionId)
        }

        val report = dataCollection.generateAggregatedReport()

        assertEquals(10, report.totalParticipants)
        assertTrue(report.totalSessions > 0)
        assertNotNull(report.ageGroupBreakdown)
    }

    @Test
    fun `test progress tracking system`() {
        val participant = TestParticipant(
            participantId = "test-005",
            age = 9,
            ageGroup = AgeGroup.YOUNG,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        // Simulate some activity
        val sessionId = "session-test"
        dataCollection.startSession(
            sessionId = sessionId,
            participantId = participant.participantId,
            phase = TestPhase.PRACTICE,
            puzzleId = "puzzle-test",
            difficultyLevel = DifficultyLevel.EASY
        )

        dataCollection.trackAction(
            sessionId = sessionId,
            participantId = participant.participantId,
            actionType = ActionType.PUZZLE_COMPLETED,
            details = mapOf("timeSeconds" to 180.0)
        )

        dataCollection.endSession(sessionId)

        val progress = progressSystem.getComprehensiveProgress(participant.participantId)

        assertNotNull(progress)
        assertEquals(participant.participantId, progress!!.participantId)
        assertEquals(participant.childName, progress.participantName)
    }

    @Test
    fun `test progress summary`() {
        val participant = TestParticipant(
            participantId = "test-006",
            age = 13,
            ageGroup = AgeGroup.OLD,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        val summary = progressSystem.getProgressSummary(participant.participantId)

        assertNotNull(summary)
        assertEquals(0, summary!!.level) // New participant starts at level 0
        assertEquals(0, summary.xp)
    }

    @Test
    fun `test goal progress`() {
        val participant = TestParticipant(
            participantId = "test-007",
            age = 8,
            ageGroup = AgeGroup.YOUNG,
            parentEmail = "test@example.com",
            childName = "Test Child",
            startDate = LocalDate.now()
        )

        dataCollection.registerParticipant(participant)

        val goals = progressSystem.getGoalProgress(participant.participantId)

        assertNotNull(goals)
        assertFalse(goals.dailyPuzzleGoal.completed) // No puzzles completed yet
        assertFalse(goals.streakGoal.completed) // No streak yet
    }

    @Test
    fun `test educational feature enum values`() {
        assertEquals("Hint System", EducationalFeature.HINT_SYSTEM.displayName)
        assertEquals("Progressive hints when stuck", EducationalFeature.HINT_SYSTEM.description)

        assertEquals("Celebration Animations", EducationalFeature.CELEBRATION.displayName)
        assertEquals("Reward animations on completion", EducationalFeature.CELEBRATION.description)
    }

    @Test
    fun `test action type enum values`() {
        assertEquals(13, ActionType.values().size)
        assertTrue(ActionType.values().contains(ActionType.PUZZLE_STARTED))
        assertTrue(ActionType.values().contains(ActionType.HINT_REQUESTED))
        assertTrue(ActionType.values().contains(ActionType.ERROR_MADE))
    }

    @Test
    fun `test AB test metric enum values`() {
        assertEquals(8, ABTestMetric.values().size)
        assertTrue(ABTestMetric.values().contains(ABTestMetric.COMPLETION_RATE))
        assertTrue(ABTestMetric.values().contains(ABTestMetric.SATISFACTION_SCORE))
    }

    @Test
    fun `test response type enum values`() {
        assertEquals(6, ResponseType.values().size)
        assertTrue(ResponseType.values().contains(ResponseType.EMOJI_RATING))
        assertTrue(ResponseType.values().contains(ResponseType.YES_NO))
    }

    @Test
    fun `test survey response sealed types`() {
        val rating = SurveyResponse.RatingResponse(5)
        val boolean = SurveyResponse.BooleanResponse(true)
        val text = SurveyResponse.TextResponse("Great!")
        val multiple = SurveyResponse.MultipleChoiceResponse(listOf("A", "B"))

        assertTrue(rating is SurveyResponse)
        assertTrue(boolean is SurveyResponse)
        assertTrue(text is SurveyResponse)
        assertTrue(multiple is SurveyResponse)
    }

    @Test
    fun `test difficulty levels match age ranges`() {
        assertEquals("8-9 years", DifficultyLevel.EASY.targetAgeRange)
        assertEquals("10-11 years", DifficultyLevel.MEDIUM.targetAgeRange)
        assertEquals("12-13 years", DifficultyLevel.HARD.targetAgeRange)
        assertEquals("14+ years", DifficultyLevel.EXPERT.targetAgeRange)
    }

    @Test
    fun `test data collection statistics`() {
        val stats = dataCollection.getDataCollectionStats()

        assertEquals(0, stats.totalParticipants) // No participants yet
        assertEquals(0, stats.totalSessions)
        assertEquals(0, stats.totalFeedback)
    }

    @Test
    fun `test scenario evaluation`() {
        val factory = TestCaseScenarioFactory()
        val scenario = factory.createScenariosForAgeGroup(AgeGroup.YOUNG).first()

        val mockAnalytics = UsageAnalytics(
            participantId = "test-participant",
            sessionId = "test-session",
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusMinutes(10),
            actions = listOf(
                SessionAction(
                    actionId = "action-1",
                    sessionId = "test-session",
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.PUZZLE_COMPLETED,
                    details = mapOf("timeSeconds" to 300)
                ),
                SessionAction(
                    actionId = "action-2",
                    sessionId = "test-session",
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.STEP_COMPLETED,
                    details = mapOf("technique" to "Single Candidate")
                )
            ),
            metrics = SessionMetrics(
                totalDurationSeconds = 600,
                activeTimeSeconds = 500,
                idleTimeSeconds = 100,
                puzzlesStarted = 1,
                puzzlesCompleted = 1,
                puzzlesAbandoned = 0,
                hintsRequested = 2,
                hintsAccepted = 2,
                hintsRejected = 0,
                errorsMade = 1,
                errorsCorrectedIndependently = 0,
                errorsCorrectedWithHelp = 1,
                averageTimePerCell = 30.0,
                fastestPuzzleTime = 300,
                slowestPuzzleTime = 300,
                featuresUsed = emptyMap(),
                difficultyLevelsAttempted = mapOf(DifficultyLevel.EASY to 1)
            )
        )

        val evaluation = factory.evaluateScenario(scenario, mockAnalytics)

        assertNotNull(evaluation)
        assertEquals(scenario.scenarioId, evaluation.scenarioId)
        assertTrue(evaluation.percentage >= 0.0)
        assertTrue(evaluation.percentage <= 100.0)
    }
}
