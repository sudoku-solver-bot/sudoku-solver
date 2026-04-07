package will.sudoku.solver

/**
 * Test case scenario for user testing
 */
data class TestCaseScenario(
    val scenarioId: String,
    val name: String,
    val description: String,
    val targetAgeGroup: AgeGroup,
    val difficultyLevel: DifficultyLevel,
    val puzzleTemplate: PuzzleTemplate,
    val objectives: List<String>,
    val expectedBehaviors: List<ExpectedBehavior>,
    val successCriteria: List<SuccessCriterion>,
    val timeLimitMinutes: Int,
    val hintsAvailable: Int,
    val techniqueFocus: List<String>
)

/**
 * Puzzle template for test cases
 */
data class PuzzleTemplate(
    val templateId: String,
    val initialClues: Int,
    val symmetry: PuzzleSymmetry,
    val requiredTechniques: List<String>,
    val guaranteedSingleCandidates: Int,
    val maxComplexity: Int // 1-10 scale
)

enum class PuzzleSymmetry {
    NONE,
    ROTATIONAL_180,
    FULL_DIAGONAL,
    VERTICAL,
    HORIZONTAL
}

/**
 * Expected user behavior during test
 */
data class ExpectedBehavior(
    val behaviorType: BehaviorType,
    val description: String,
    val frequency: Frequency,
    val isPositive: Boolean
)

enum class BehaviorType {
    HINT_REQUEST,
    ERROR_MAKING,
    SELF_CORRECTION,
    COMPLETION_ATTEMPT,
    TECHNIQUE_APPLICATION,
    ABANDONMENT,
    HELP_SEEKING
}

enum class Frequency {
    NEVER,
    RARELY, // 1-2 times
    OCCASIONALLY, // 3-5 times
    FREQUENTLY, // 6+ times
    ALWAYS
}

/**
 * Success criteria for test scenarios
 */
data class SuccessCriterion(
    val criterionType: CriterionType,
    val threshold: Double,
    val comparison: Comparison
)

enum class CriterionType {
    COMPLETION_RATE,
    TIME_UNDER_LIMIT,
    ERROR_BELOW_THRESHOLD,
    HINT_USAGE_BELOW,
    TECHNIQUE_DEMONSTRATION,
    SATISFACTION_SCORE
}

enum class Comparison {
    LESS_THAN,
    GREATER_THAN,
    EQUAL_TO,
    PERCENTAGE_ABOVE,
    PERCENTAGE_BELOW
}

/**
 * Test case scenario factory
 */
class TestCaseScenarioFactory {

    fun createScenariosForAgeGroup(ageGroup: AgeGroup): List<TestCaseScenario> {
        return when (ageGroup) {
            AgeGroup.YOUNG -> createYoungLearnerScenarios()
            AgeGroup.OLD -> createOlderLearnerScenarios()
        }
    }

    /**
     * Test scenarios for young learners (8-10)
     * Focus on engagement, basic techniques, positive reinforcement
     */
    private fun createYoungLearnerScenarios(): List<TestCaseScenario> {
        return listOf(
            createEasyIntroduction(),
            createEasyConfidenceBuilder(),
            createEasyChallenge(),
            createMediumTransition(),
            createMediumPractice()
        )
    }

    private fun createEasyIntroduction(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "young-easy-intro-001",
            name = "First Steps - Introduction",
            description = "Very easy puzzle to introduce basic concepts and build confidence",
            targetAgeGroup = AgeGroup.YOUNG,
            difficultyLevel = DifficultyLevel.EASY,
            puzzleTemplate = PuzzleTemplate(
                templateId = "intro-easy-001",
                initialClues = 42,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position"),
                guaranteedSingleCandidates = 15,
                maxComplexity = 2
            ),
            objectives = listOf(
                "Complete at least 50% of the puzzle independently",
                "Understand the concept of no repeats in rows/columns",
                "Experience success with positive reinforcement"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.COMPLETION_ATTEMPT,
                    description = "Should attempt to place numbers",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.HINT_REQUEST,
                    description = "May request hints when stuck",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.ERROR_MAKING,
                    description = "May make errors, should be corrected",
                    frequency = Frequency.RARELY,
                    isPositive = false
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.5, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TIME_UNDER_LIMIT, 15.0, Comparison.LESS_THAN),
                SuccessCriterion(CriterionType.SATISFACTION_SCORE, 4.0, Comparison.GREATER_THAN)
            ),
            timeLimitMinutes = 15,
            hintsAvailable = 10,
            techniqueFocus = listOf("Single Candidate", "Single Position")
        )
    }

    private fun createEasyConfidenceBuilder(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "young-easy-confidence-002",
            name = "Confidence Builder",
            description = "Easy puzzle to reinforce learning and build confidence",
            targetAgeGroup = AgeGroup.YOUNG,
            difficultyLevel = DifficultyLevel.EASY,
            puzzleTemplate = PuzzleTemplate(
                templateId = "confidence-easy-001",
                initialClues = 40,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position"),
                guaranteedSingleCandidates = 12,
                maxComplexity = 3
            ),
            objectives = listOf(
                "Complete puzzle with minimal assistance",
                "Demonstrate understanding of basic rules",
                "Show increased confidence"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should apply single-candidate technique",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.HINT_REQUEST,
                    description = "Should use hints strategically",
                    frequency = Frequency.RARELY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.SELF_CORRECTION,
                    description = "Should catch and fix own mistakes",
                    frequency = Frequency.RARELY,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.7, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.ERROR_BELOW_THRESHOLD, 3.0, Comparison.LESS_THAN),
                SuccessCriterion(CriterionType.HINT_USAGE_BELOW, 5.0, Comparison.LESS_THAN)
            ),
            timeLimitMinutes = 20,
            hintsAvailable = 7,
            techniqueFocus = listOf("Single Candidate", "Single Position")
        )
    }

    private fun createEasyChallenge(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "young-easy-challenge-003",
            name = "Easy Challenge",
            description = "Slightly harder easy puzzle to test skill retention",
            targetAgeGroup = AgeGroup.YOUNG,
            difficultyLevel = DifficultyLevel.EASY,
            puzzleTemplate = PuzzleTemplate(
                templateId = "challenge-easy-001",
                initialClues = 38,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position", "Simple Elimination"),
                guaranteedSingleCandidates = 10,
                maxComplexity = 4
            ),
            objectives = listOf(
                "Complete puzzle independently",
                "Apply learned techniques without prompting",
                "Maintain engagement throughout"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should use multiple techniques",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.SELF_CORRECTION,
                    description = "Should independently correct errors",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.ABANDONMENT,
                    description = "Should not abandon the puzzle",
                    frequency = Frequency.NEVER,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.8, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 2.0, Comparison.GREATER_THAN),
                SuccessCriterion(CriterionType.HINT_USAGE_BELOW, 4.0, Comparison.LESS_THAN)
            ),
            timeLimitMinutes = 25,
            hintsAvailable = 5,
            techniqueFocus = listOf("Single Candidate", "Single Position", "Simple Elimination")
        )
    }

    private fun createMediumTransition(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "young-medium-transition-001",
            name = "Moving to Medium",
            description = "First medium-difficulty puzzle with support",
            targetAgeGroup = AgeGroup.YOUNG,
            difficultyLevel = DifficultyLevel.MEDIUM,
            puzzleTemplate = PuzzleTemplate(
                templateId = "transition-medium-001",
                initialClues = 35,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position", "Naked Pairs"),
                guaranteedSingleCandidates = 8,
                maxComplexity = 5
            ),
            objectives = listOf(
                "Attempt medium difficulty puzzle",
                "Learn to look for naked pairs",
                "Understand that harder puzzles take more time"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.HINT_REQUEST,
                    description = "May request more hints for new techniques",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.HELP_SEEKING,
                    description = "Should ask for explanation of new techniques",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.5, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TIME_UNDER_LIMIT, 30.0, Comparison.LESS_THAN)
            ),
            timeLimitMinutes = 30,
            hintsAvailable = 8,
            techniqueFocus = listOf("Single Candidate", "Single Position", "Naked Pairs")
        )
    }

    private fun createMediumPractice(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "young-medium-practice-001",
            name = "Medium Practice",
            description = "Medium puzzle to practice developing skills",
            targetAgeGroup = AgeGroup.YOUNG,
            difficultyLevel = DifficultyLevel.MEDIUM,
            puzzleTemplate = PuzzleTemplate(
                templateId = "practice-medium-001",
                initialClues = 33,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position", "Naked Pairs", "Hidden Singles"),
                guaranteedSingleCandidates = 6,
                maxComplexity = 6
            ),
            objectives = listOf(
                "Complete medium puzzle with support",
                "Demonstrate learned techniques",
                "Show persistence through difficulty"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should apply multiple techniques",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.ABANDONMENT,
                    description = "Should persist through challenges",
                    frequency = Frequency.NEVER,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.6, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 3.0, Comparison.GREATER_THAN)
            ),
            timeLimitMinutes = 35,
            hintsAvailable = 7,
            techniqueFocus = listOf("Single Candidate", "Single Position", "Naked Pairs", "Hidden Singles")
        )
    }

    /**
     * Test scenarios for older learners (11-14)
     * Focus on technique mastery, efficiency, problem-solving
     */
    private fun createOlderLearnerScenarios(): List<TestCaseScenario> {
        return listOf(
            createMediumBaseline(),
            createMediumTechnique(),
            createHardIntroduction(),
            createHardPractice(),
            createExpertChallenge()
        )
    }

    private fun createMediumBaseline(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "old-medium-baseline-001",
            name = "Baseline Assessment",
            description = "Establish baseline skills at medium difficulty",
            targetAgeGroup = AgeGroup.OLD,
            difficultyLevel = DifficultyLevel.MEDIUM,
            puzzleTemplate = PuzzleTemplate(
                templateId = "baseline-medium-001",
                initialClues = 32,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Single Candidate", "Single Position", "Naked Pairs", "Hidden Singles"),
                guaranteedSingleCandidates = 5,
                maxComplexity = 5
            ),
            objectives = listOf(
                "Establish current skill level",
                "Measure baseline completion time",
                "Identify areas needing improvement"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should demonstrate various techniques",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.SELF_CORRECTION,
                    description = "Should catch and correct errors",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.7, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.ERROR_BELOW_THRESHOLD, 5.0, Comparison.LESS_THAN),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 3.0, Comparison.GREATER_THAN)
            ),
            timeLimitMinutes = 20,
            hintsAvailable = 3,
            techniqueFocus = listOf("Single Candidate", "Single Position", "Naked Pairs", "Hidden Singles")
        )
    }

    private fun createMediumTechnique(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "old-medium-technique-001",
            name = "Technique Mastery",
            description = "Focus on applying specific techniques efficiently",
            targetAgeGroup = AgeGroup.OLD,
            difficultyLevel = DifficultyLevel.MEDIUM,
            puzzleTemplate = PuzzleTemplate(
                templateId = "technique-medium-001",
                initialClues = 30,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Naked Pairs", "Hidden Singles", "Pointing Pairs"),
                guaranteedSingleCandidates = 4,
                maxComplexity = 6
            ),
            objectives = listOf(
                "Apply naked pairs technique",
                "Use hidden singles efficiently",
                "Demonstrate pointing pairs"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should use advanced techniques",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.HINT_REQUEST,
                    description = "Should request hints strategically",
                    frequency = Frequency.RARELY,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.8, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 4.0, Comparison.GREATER_THAN),
                SuccessCriterion(CriterionType.TIME_UNDER_LIMIT, 25.0, Comparison.LESS_THAN)
            ),
            timeLimitMinutes = 25,
            hintsAvailable = 3,
            techniqueFocus = listOf("Naked Pairs", "Hidden Singles", "Pointing Pairs")
        )
    }

    private fun createHardIntroduction(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "old-hard-intro-001",
            name = "Hard Puzzle Introduction",
            description = "First hard puzzle with step-by-step guidance",
            targetAgeGroup = AgeGroup.OLD,
            difficultyLevel = DifficultyLevel.HARD,
            puzzleTemplate = PuzzleTemplate(
                templateId = "intro-hard-001",
                initialClues = 28,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Naked Triples", "Hidden Pairs", "X-Wing"),
                guaranteedSingleCandidates = 3,
                maxComplexity = 7
            ),
            objectives = listOf(
                "Attempt hard difficulty puzzle",
                "Learn advanced techniques with guidance",
                "Build persistence for harder challenges"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.HELP_SEEKING,
                    description = "Should seek guidance on new techniques",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.ABANDONMENT,
                    description = "Should not abandon despite difficulty",
                    frequency = Frequency.NEVER,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.5, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 2.0, Comparison.GREATER_THAN)
            ),
            timeLimitMinutes = 40,
            hintsAvailable = 6,
            techniqueFocus = listOf("Naked Triples", "Hidden Pairs", "X-Wing")
        )
    }

    private fun createHardPractice(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "old-hard-practice-001",
            name = "Hard Puzzle Practice",
            description = "Practice hard puzzles with decreasing support",
            targetAgeGroup = AgeGroup.OLD,
            difficultyLevel = DifficultyLevel.HARD,
            puzzleTemplate = PuzzleTemplate(
                templateId = "practice-hard-001",
                initialClues = 27,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("Naked Triples", "Hidden Pairs", "X-Wing", "Swordfish"),
                guaranteedSingleCandidates = 2,
                maxComplexity = 8
            ),
            objectives = listOf(
                "Complete hard puzzle with minimal assistance",
                "Apply multiple advanced techniques",
                "Improve solving efficiency"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should use advanced techniques independently",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.SELF_CORRECTION,
                    description = "Should independently catch errors",
                    frequency = Frequency.OCCASIONALLY,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.6, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 3.0, Comparison.GREATER_THAN),
                SuccessCriterion(CriterionType.ERROR_BELOW_THRESHOLD, 4.0, Comparison.LESS_THAN)
            ),
            timeLimitMinutes = 45,
            hintsAvailable = 4,
            techniqueFocus = listOf("Naked Triples", "Hidden Pairs", "X-Wing", "Swordfish")
        )
    }

    private fun createExpertChallenge(): TestCaseScenario {
        return TestCaseScenario(
            scenarioId = "old-expert-challenge-001",
            name = "Expert Challenge",
            description = "Expert-level puzzle for assessment",
            targetAgeGroup = AgeGroup.OLD,
            difficultyLevel = DifficultyLevel.EXPERT,
            puzzleTemplate = PuzzleTemplate(
                templateId = "challenge-expert-001",
                initialClues = 24,
                symmetry = PuzzleSymmetry.ROTATIONAL_180,
                requiredTechniques = listOf("XY-Wing", "Swordfish", "Forcing Chains"),
                guaranteedSingleCandidates = 1,
                maxComplexity = 10
            ),
            objectives = listOf(
                "Attempt expert-level puzzle",
                "Apply all learned techniques",
                "Demonstrate problem-solving persistence"
            ),
            expectedBehaviors = listOf(
                ExpectedBehavior(
                    behaviorType = BehaviorType.TECHNIQUE_APPLICATION,
                    description = "Should use expert techniques",
                    frequency = Frequency.FREQUENTLY,
                    isPositive = true
                ),
                ExpectedBehavior(
                    behaviorType = BehaviorType.ABANDONMENT,
                    description = "Should persist despite difficulty",
                    frequency = Frequency.NEVER,
                    isPositive = true
                )
            ),
            successCriteria = listOf(
                SuccessCriterion(CriterionType.COMPLETION_RATE, 0.3, Comparison.PERCENTAGE_ABOVE),
                SuccessCriterion(CriterionType.TECHNIQUE_DEMONSTRATION, 2.0, Comparison.GREATER_THAN),
                SuccessCriterion(CriterionType.SATISFACTION_SCORE, 3.0, Comparison.GREATER_THAN)
            ),
            timeLimitMinutes = 60,
            hintsAvailable = 5,
            techniqueFocus = listOf("XY-Wing", "Swordfish", "Forcing Chains")
        )
    }

    /**
     * Evaluate test scenario completion
     */
    fun evaluateScenario(
        scenario: TestCaseScenario,
        analytics: UsageAnalytics
    ): ScenarioEvaluation {
        val results = mutableMapOf<SuccessCriterion, Boolean>()

        scenario.successCriteria.forEach { criterion ->
            val passed = when (criterion.criterionType) {
                CriterionType.COMPLETION_RATE -> {
                    val rate = if (analytics.metrics.puzzlesStarted > 0) {
                        analytics.metrics.puzzlesCompleted.toDouble() / analytics.metrics.puzzlesStarted
                    } else 0.0
                    when (criterion.comparison) {
                        Comparison.PERCENTAGE_ABOVE -> rate >= criterion.threshold
                        Comparison.GREATER_THAN -> rate > criterion.threshold
                        else -> false
                    }
                }
                CriterionType.TIME_UNDER_LIMIT -> {
                    val time = analytics.metrics.totalDurationSeconds / 60.0
                    time <= criterion.threshold
                }
                CriterionType.ERROR_BELOW_THRESHOLD -> {
                    analytics.metrics.errorsMade <= criterion.threshold
                }
                CriterionType.HINT_USAGE_BELOW -> {
                    analytics.metrics.hintsRequested <= criterion.threshold
                }
                CriterionType.TECHNIQUE_DEMONSTRATION -> {
                    val techniques = analytics.actions
                        .filter { it.actionType == ActionType.STEP_COMPLETED }
                        .mapNotNull { it.details["technique"] as? String }
                        .distinct()
                        .size
                    techniques >= criterion.threshold
                }
                CriterionType.SATISFACTION_SCORE -> {
                    // This would come from survey data
                    true // Placeholder
                }
            }
            results[criterion] = passed
        }

        val passedCount = results.values.count { it }
        val totalCount = results.size
        val passed = passedCount >= (totalCount * 0.6) // 60% pass threshold

        return ScenarioEvaluation(
            scenarioId = scenario.scenarioId,
            passed = passed,
            criteriaResults = results,
            passedCount = passedCount,
            totalCount = totalCount,
            percentage = if (totalCount > 0) (passedCount.toDouble() / totalCount) * 100 else 0.0
        )
    }
}

/**
 * Result of scenario evaluation
 */
data class ScenarioEvaluation(
    val scenarioId: String,
    val passed: Boolean,
    val criteriaResults: Map<SuccessCriterion, Boolean>,
    val passedCount: Int,
    val totalCount: Int,
    val percentage: Double
)
