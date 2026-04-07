package will.sudoku.solver

import java.time.LocalDate

/**
 * Testing protocol definition for age groups
 */
data class TestingProtocol(
    val protocolId: String,
    val ageGroup: AgeGroup,
    val name: String,
    val description: String,
    val durationDays: Int,
    val sessionsPerDay: Int,
    val puzzlesPerSession: Int,
    val phases: List<ProtocolPhase>,
    val requiredFeatures: Set<EducationalFeature>,
    val surveySchedule: List<SurveyTrigger>
)

/**
 * Individual phase within a testing protocol
 */
data class ProtocolPhase(
    val phase: TestPhase,
    val durationDays: Int,
    val objectives: List<String>,
    val activities: List<TestingActivity>,
    val successCriteria: List<String>
)

/**
 * Activity within a testing phase
 */
data class TestingActivity(
    val activityId: String,
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val difficultyLevel: DifficultyLevel,
    val instructions: String,
    val expectedOutcomes: List<String>
)

/**
 * When to trigger surveys
 */
data class SurveyTrigger(
    val triggerType: TriggerType,
    val phase: TestPhase,
    val surveyId: String
)

enum class TriggerType {
    PHASE_START,
    PHASE_END,
    PUZZLE_COMPLETE,
    SESSION_COMPLETE,
    DAILY,
    PROTOCOL_COMPLETE
}

/**
 * Factory for creating age-appropriate testing protocols
 */
class TestingProtocolFactory {

    fun createProtocolForAgeGroup(ageGroup: AgeGroup): TestingProtocol {
        return when (ageGroup) {
            AgeGroup.YOUNG -> createYoungLearnersProtocol()
            AgeGroup.OLD -> createOlderLearnersProtocol()
        }
    }

    /**
     * Protocol for young learners (8-10 years)
     * Focus: Engagement, basic skills, positive reinforcement
     */
    private fun createYoungLearnersProtocol(): TestingProtocol {
        return TestingProtocol(
            protocolId = "young-learners-v1",
            ageGroup = AgeGroup.YOUNG,
            name = "Young Explorers Sudoku Journey",
            description = "A gentle introduction to Sudoku designed for children aged 8-10, focusing on fun, engagement, and building confidence.",
            durationDays = 14,
            sessionsPerDay = 1,
            puzzlesPerSession = 2,
            phases = listOf(
                createOnboardingPhaseYoung(),
                createTutorialPhaseYoung(),
                createPracticePhaseYoung(),
                createAssessmentPhaseYoung(),
                createFeedbackPhaseYoung()
            ),
            requiredFeatures = setOf(
                EducationalFeature.VISUAL_FEEDBACK,
                EducationalFeature.CELEBRATION,
                EducationalFeature.PROGRESS_BAR,
                EducationalFeature.HINT_SYSTEM,
                EducationalFeature.TUTORIAL_MODE
            ),
            surveySchedule = listOf(
                SurveyTrigger(TriggerType.PHASE_END, TestPhase.TUTORIAL, "post-tutorial-young"),
                SurveyTrigger(TriggerType.PHASE_END, TestPhase.PRACTICE, "post-practice-young"),
                SurveyTrigger(TriggerType.PROTOCOL_COMPLETE, TestPhase.FEEDBACK, "final-survey-young")
            )
        )
    }

    private fun createOnboardingPhaseYoung(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.ONBOARDING,
            durationDays = 1,
            objectives = listOf(
                "Introduce the child to the Sudoku interface",
                "Build excitement about learning Sudoku",
                "Establish comfort with basic interactions"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "welcome-intro",
                    name = "Welcome & Introduction",
                    description = "Interactive welcome explaining what Sudoku is",
                    durationMinutes = 5,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Show the child around the interface with animated guide",
                    expectedOutcomes = listOf(
                        "Child can navigate to the puzzle area",
                        "Child understands the basic goal"
                    )
                ),
                TestingActivity(
                    activityId = "first-cell",
                    name = "First Cell Experience",
                    description = "Complete a single cell with guidance",
                    durationMinutes = 5,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Walk through placing their very first number with celebration",
                    expectedOutcomes = listOf(
                        "Child successfully places a number",
                        "Child experiences positive reinforcement"
                    )
                )
            ),
            successCriteria = listOf(
                "Child completes onboarding without asking to stop",
                "Child expresses willingness to continue"
            )
        )
    }

    private fun createTutorialPhaseYoung(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.TUTORIAL,
            durationDays = 3,
            objectives = listOf(
                "Learn basic Sudoku rules through guided play",
                "Master finding single candidates",
                "Understand rows and columns"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "row-column-basics",
                    name = "Row and Column Adventure",
                    description = "Learn how numbers can't repeat in rows/columns",
                    durationMinutes = 10,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Use visual highlighting to show rows and columns",
                    expectedOutcomes = listOf(
                        "Child identifies one row and one column",
                        "Child understands no repeats rule"
                    )
                ),
                TestingActivity(
                    activityId = "single-candidate-hunt",
                    name = "Number Detective",
                    description = "Find cells with only one possible number",
                    durationMinutes = 15,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Highlight cells with single candidates, let child find them",
                    expectedOutcomes = listOf(
                        "Child finds at least 3 single-candidate cells independently",
                        "Child places numbers correctly"
                    )
                ),
                TestingActivity(
                    activityId = "first-puzzle-guided",
                    name = "First Complete Puzzle",
                    description = "Solve a complete easy puzzle with hints available",
                    durationMinutes = 20,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Provide encouragement and hints when stuck, celebrate completion",
                    expectedOutcomes = listOf(
                        "Child completes at least 80% of puzzle",
                        "Child asks for help appropriately"
                    )
                )
            ),
            successCriteria = listOf(
                "Child completes at least 3 puzzles over 3 days",
                "Child demonstrates understanding of basic rules",
                "Child shows interest in continuing"
            )
        )
    }

    private fun createPracticePhaseYoung(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.PRACTICE,
            durationDays = 7,
            objectives = listOf(
                "Build independence in solving",
                "Practice easy and medium puzzles",
                "Develop problem-solving confidence"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "daily-puzzles",
                    name = "Daily Puzzle Time",
                    description = "Complete 2 puzzles per session",
                    durationMinutes = 20,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Gradually reduce hint availability, encourage independence",
                    expectedOutcomes = listOf(
                        "Child completes puzzles with decreasing help",
                        "Child shows improved speed"
                    )
                ),
                TestingActivity(
                    activityId = "progress-challenge",
                    name = "Level Up Challenge",
                    description = "Attempt medium difficulty puzzles",
                    durationMinutes = 15,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Introduce medium puzzles with extra visual support",
                    expectedOutcomes = listOf(
                        "Child attempts at least one medium puzzle",
                        "Child understands increased difficulty"
                    )
                )
            ),
            successCriteria = listOf(
                "Child completes at least 10 puzzles total",
                "Completion rate improves over time",
                "Child maintains engagement"
            )
        )
    }

    private fun createAssessmentPhaseYoung(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.ASSESSMENT,
            durationDays = 2,
            objectives = listOf(
                "Measure independent problem-solving ability",
                "Assess retention of learned techniques",
                "Evaluate engagement and enjoyment"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "independent-solve",
                    name = "Show What You Know",
                    description = "Solve puzzles with minimal assistance",
                    durationMinutes = 25,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Observe without intervening unless completely stuck",
                    expectedOutcomes = listOf(
                        "Child completes puzzle independently",
                        "Child uses learned strategies"
                    )
                )
            ),
            successCriteria = listOf(
                "Child completes at least one puzzle without hints",
                "Child demonstrates rule understanding",
                "Child expresses positive feelings about the experience"
            )
        )
    }

    private fun createFeedbackPhaseYoung(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.FEEDBACK,
            durationDays = 1,
            objectives = listOf(
                "Gather child's feedback on experience",
                "Assess overall satisfaction",
                "Document learning progress"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "final-survey",
                    name = "Share Your Thoughts",
                    description = "Complete age-appropriate survey",
                    durationMinutes = 10,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Read questions aloud, record responses",
                    expectedOutcomes = listOf(
                        "Child answers all questions",
                        "Child provides honest feedback"
                    )
                )
            ),
            successCriteria = listOf(
                "Survey completed",
                "Final metrics recorded"
            )
        )
    }

    /**
     * Protocol for older learners (11-14 years)
     * Focus: Skill development, technique mastery, challenge
     */
    private fun createOlderLearnersProtocol(): TestingProtocol {
        return TestingProtocol(
            protocolId = "older-learners-v1",
            ageGroup = AgeGroup.OLD,
            name = "Sudoku Mastery Challenge",
            description = "A comprehensive Sudoku learning program for ages 11-14, focusing on technique development and problem-solving skills.",
            durationDays = 21,
            sessionsPerDay = 2,
            puzzlesPerSession = 3,
            phases = listOf(
                createOnboardingPhaseOld(),
                createTechniqueLearningPhaseOld(),
                createPracticePhaseOld(),
                createChallengePhaseOld(),
                createAssessmentPhaseOld(),
                createFeedbackPhaseOld()
            ),
            requiredFeatures = setOf(
                EducationalFeature.HINT_SYSTEM,
                EducationalFeature.STEP_BY_STEP,
                EducationalFeature.VISUAL_FEEDBACK,
                EducationalFeature.ERROR_HIGHLIGHTING,
                EducationalFeature.TUTORIAL_MODE,
                EducationalFeature.TIMER
            ),
            surveySchedule = listOf(
                SurveyTrigger(TriggerType.PHASE_END, TestPhase.ONBOARDING, "post-onboarding-old"),
                SurveyTrigger(TriggerType.PHASE_END, TestPhase.TUTORIAL, "post-technique-old"),
                SurveyTrigger(TriggerType.PHASE_END, TestPhase.PRACTICE, "post-practice-old"),
                SurveyTrigger(TriggerType.PROTOCOL_COMPLETE, TestPhase.FEEDBACK, "final-survey-old")
            )
        )
    }

    private fun createOnboardingPhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.ONBOARDING,
            durationDays = 1,
            objectives = listOf(
                "Introduce Sudoku rules and interface",
                "Set learning goals",
                "Establish baseline ability"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "interface-tour",
                    name = "Interface Orientation",
                    description = "Learn to use all features",
                    durationMinutes = 10,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Quick walkthrough of all interface elements",
                    expectedOutcomes = listOf(
                        "User navigates interface confidently",
                        "User understands available features"
                    )
                ),
                TestingActivity(
                    activityId = "baseline-assessment",
                    name = "Skills Assessment",
                    description = "Complete a baseline puzzle",
                    durationMinutes = 15,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Solve one puzzle to establish baseline",
                    expectedOutcomes = listOf(
                        "Baseline completion time recorded",
                        "Baseline error rate recorded"
                    )
                )
            ),
            successCriteria = listOf(
                "Onboarding completed",
                "Baseline metrics recorded"
            )
        )
    }

    private fun createTechniqueLearningPhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.TUTORIAL,
            durationDays = 5,
            objectives = listOf(
                "Learn fundamental solving techniques",
                "Understand candidate elimination",
                "Master single position and single candidate"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "single-position",
                    name = "Single Position Technique",
                    description = "Learn and practice single position",
                    durationMinutes = 20,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Interactive tutorial on finding single positions",
                    expectedOutcomes = listOf(
                        "Learner identifies single positions",
                        "Learner applies technique independently"
                    )
                ),
                TestingActivity(
                    activityId = "candidate-elimination",
                    name = "Candidate Elimination",
                    description = "Learn systematic elimination",
                    durationMinutes = 25,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Practice marking and eliminating candidates",
                    expectedOutcomes = listOf(
                        "Learner marks candidates correctly",
                        "Learner eliminates impossible options"
                    )
                ),
                TestingActivity(
                    activityId = "technique-practice",
                    name = "Technique Practice Puzzles",
                    description = "Apply learned techniques",
                    durationMinutes = 30,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Solve puzzles focusing on new techniques",
                    expectedOutcomes = listOf(
                        "Learner completes puzzles using techniques",
                        "Learner explains reasoning"
                    )
                )
            ),
            successCriteria = listOf(
                "Learner demonstrates all basic techniques",
                "At least 8 puzzles completed",
                "Technique understanding verified"
            )
        )
    }

    private fun createPracticePhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.PRACTICE,
            durationDays = 10,
            objectives = listOf(
                "Build fluency with techniques",
                "Improve solving speed",
                "Tackle harder puzzles"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "daily-practice",
                    name = "Technique Reinforcement",
                    description = "Daily practice puzzles",
                    durationMinutes = 25,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Complete puzzles focusing on technique application",
                    expectedOutcomes = listOf(
                        "Speed improves over sessions",
                        "Technique use becomes automatic"
                    )
                ),
                TestingActivity(
                    activityId = "hard-challenges",
                    name = "Hard Puzzle Challenges",
                    description = "Attempt hard difficulty",
                    durationMinutes = 30,
                    difficultyLevel = DifficultyLevel.HARD,
                    instructions = "Introduce hard puzzles with full hint system",
                    expectedOutcomes = listOf(
                        "Learner attempts hard puzzles",
                        "Learner uses hints strategically"
                    )
                )
            ),
            successCriteria = listOf(
                "At least 15 puzzles completed",
                "Measurable improvement in speed",
                "Progression to harder difficulties"
            )
        )
    }

    private fun createChallengePhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.ASSESSMENT,
            durationDays = 3,
            objectives = listOf(
                "Test skills with limited assistance",
                "Measure problem-solving independence",
                "Prepare for final assessment"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "timed-challenges",
                    name = "Timed Solving",
                    description = "Solve puzzles against the clock",
                    durationMinutes = 20,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Complete puzzles with timer, minimal hints",
                    expectedOutcomes = listOf(
                        "Learner completes within time limit",
                        "Learner manages time effectively"
                    )
                ),
                TestingActivity(
                    activityId = "expert-attempts",
                    name = "Expert Challenges",
                    description = "Try expert-level puzzles",
                    durationMinutes = 30,
                    difficultyLevel = DifficultyLevel.EXPERT,
                    instructions = "Attempt expert puzzles with step-by-step guidance",
                    expectedOutcomes = listOf(
                        "Learner attempts expert puzzle",
                        "Learner uses advanced features"
                    )
                )
            ),
            successCriteria = listOf(
                "Completion rate > 70%",
                "Demonstrated skill improvement"
            )
        )
    }

    private fun createAssessmentPhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.ASSESSMENT,
            durationDays = 1,
            objectives = listOf(
                "Final skills evaluation",
                "Compare to baseline",
                "Measure learning gains"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "final-evaluation",
                    name = "Final Assessment",
                    description = "Complete evaluation puzzles",
                    durationMinutes = 30,
                    difficultyLevel = DifficultyLevel.MEDIUM,
                    instructions = "Solve puzzles without assistance",
                    expectedOutcomes = listOf(
                        "Independent completion",
                        "Demonstrated mastery"
                    )
                )
            ),
            successCriteria = listOf(
                "Significant improvement from baseline",
                "High completion rate",
                "Positive self-assessment"
            )
        )
    }

    private fun createFeedbackPhaseOld(): ProtocolPhase {
        return ProtocolPhase(
            phase = TestPhase.FEEDBACK,
            durationDays = 1,
            objectives = listOf(
                "Collect detailed feedback",
                "Assess satisfaction",
                "Document recommendations"
            ),
            activities = listOf(
                TestingActivity(
                    activityId = "comprehensive-survey",
                    name = "Feedback Survey",
                    description = "Complete comprehensive feedback form",
                    durationMinutes = 15,
                    difficultyLevel = DifficultyLevel.EASY,
                    instructions = "Provide detailed feedback on all aspects",
                    expectedOutcomes = listOf(
                        "All questions answered",
                        "Detailed comments provided"
                    )
                )
            ),
            successCriteria = listOf(
                "Survey completed",
                "Learning report generated"
            )
        )
    }
}
