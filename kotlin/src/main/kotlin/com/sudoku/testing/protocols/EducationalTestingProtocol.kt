package com.sudoku.testing.protocols

import com.sudoku.testing.participants.Participant
import com.sudoku.testing.participants.AgeGroup
import com.sudoku.testing.participants.ExperienceLevel
import com.sudoku.testing.UserTestingFramework
import com.sudoku.testing.feedback.SurveyQuestion
import java.time.LocalDateTime

/**
 * Educational testing protocol for sudoku solver validation with kids aged 8-14.
 * Defines age-appropriate testing scenarios, learning progress tracking, and educational outcomes.
 */
class EducationalTestingProtocol {
    
    private val difficultyProgression = mapOf(
        "easy" to 1,
        "medium" to 2,
        "hard" to 3
    )
    
    private val learningObjectives = listOf(
        "Basic number placement understanding",
        "Row, column, and region constraint mastery",
        "Simple elimination technique application",
        "Hint usage effectiveness",
        "Progressive difficulty adaptation",
        "Problem-solving persistence",
        "Logical reasoning development"
    )
    
    private val surveyQuestions = listOf(
        SurveyQuestion(
            questionId = "enjoyment",
            questionText = "How much did you enjoy playing Sudoku?",
            options = listOf("Very much", "Somewhat", "Not really", "Not at all"),
            ageAppropriate = true
        ),
        SurveyQuestion(
            questionId = "difficulty",
            questionText = "Was the difficulty level about right for you?",
            options = listOf("Too easy", "Just right", "A bit hard", "Too hard"),
            ageAppropriate = true
        ),
        SurveyQuestion(
            questionId = "interface",
            questionText = "How easy was it to use the sudoku board?",
            options = listOf("Very easy", "Easy", "A bit tricky", "Confusing"),
            ageAppropriate = true
        ),
        SurveyQuestion(
            questionId = "help",
            questionText = "Were the hints helpful when you got stuck?",
            options = listOf("Very helpful", "Somewhat helpful", "Not very helpful", "Not helpful"),
            ageAppropriate = true
        ),
        SurveyQuestion(
            questionId = "learn",
            questionText = "Do you feel like you learned something new?",
            options = listOf("Yes, a lot!", "Yes, some", "Not really", "No"),
            ageAppropriate = true
        )
    )

    /**
     * Gets age-appropriate testing scenarios for a participant.
     */
    fun getTestingScenarios(participant: Participant): List<TestingScenario> {
        val scenarios = mutableListOf<TestingScenario>()
        
        // Base scenario for all participants
        scenarios.add(createBaseScenario(participant))
        
        // Age-specific scenarios
        when (participant.getAgeGroup()) {
            AgeGroup.YOUNGER -> {
                scenarios.addAll(createYoungerGroupScenarios(participant))
            }
            AgeGroup.OLDER -> {
                scenarios.addAll(createOlderGroupScenarios(participant))
            }
        }
        
        // Experience-specific scenarios
        when (participant.previousSudokuExperience) {
            ExperienceLevel.NONE -> {
                scenarios.add(createBeginnerScenario(participant))
            }
            ExperienceLevel.BEGINNER -> {
                scenarios.add(createIntermediateScenario(participant))
            }
            ExperienceLevel.INTERMEDIATE, ExperienceLevel.ADVANCED -> {
                scenarios.add(createAdvancedScenario(participant))
            }
        }
        
        return scenarios
    }

    /**
     * Creates a base testing scenario for all participants.
     */
    private fun createBaseScenario(participant: Participant): TestingScenario {
        return TestingScenario(
            id = "base-${participant.id}",
            name = "Basic Sudoku Understanding",
            description = "Fundamental sudoku concepts and basic gameplay",
            ageGroup = participant.getAgeGroup(),
            experienceLevel = participant.previousSudokuExperience,
            difficultyLevel = "easy",
            estimatedDurationMinutes = 15,
            learningObjectives = listOf(
                "Understanding sudoku rules",
                "Basic number placement",
                "Simple constraint recognition"
            ),
            successCriteria = listOf(
                "Complete 3 easy puzzles",
                "Use hints effectively",
                "Report understanding of rules"
            )
        )
    }

    /**
     * Creates scenarios specifically for younger participants (8-10 years).
     */
    private fun createYoungerGroupScenarios(participant: Participant): List<TestingScenario> {
        return listOf(
            TestingScenario(
                id = "younger-fun-${participant.id}",
                name = "Fun and Engagement",
                description = "Focus on making sudoku enjoyable for younger learners",
                ageGroup = AgeGroup.YOUNGER,
                experienceLevel = participant.previousSudokuExperience,
                difficultyLevel = "easy",
                estimatedDurationMinutes = 20,
                learningObjectives = listOf(
                    "Enjoyment of puzzle solving",
                    "Positive reinforcement",
                    "Simple achievement moments"
                ),
                successCriteria = listOf(
                    "Complete puzzles with encouragement",
                    "Express enjoyment",
                    "Request to continue playing"
                )
            ),
            TestingScenario(
                id = "younger-guided-${participant.id}",
                name = "Guided Learning",
                description = "Step-by-step guidance with visual cues",
                ageGroup = AgeGroup.YOUNGER,
                experienceLevel = participant.previousSudokuExperience,
                difficultyLevel = "easy",
                estimatedDurationMinutes = 25,
                learningObjectives = listOf(
                    "Following visual instructions",
                    "Pattern recognition",
                    "Sequential thinking"
                ),
                successCriteria = listOf(
                    "Follow guided instructions",
                    "Complete puzzle with minimal help",
                    "Demonstrate understanding of patterns"
                )
            )
        )
    }

    /**
     * Creates scenarios specifically for older participants (11-14 years).
     */
    private fun createOlderGroupScenarios(participant: Participant): List<TestingScenario> {
        return listOf(
            TestingScenario(
                id = "older-strategy-${participant.id}",
                name = "Strategy Development",
                description = "Introduction to logical solving strategies",
                ageGroup = AgeGroup.OLDER,
                experienceLevel = participant.previousSudokuExperience,
                difficultyLevel = "medium",
                estimatedDurationMinutes = 30,
                learningObjectives = listOf(
                    "Logical reasoning development",
                    "Strategic thinking",
                    "Problem-solving approaches"
                ),
                successCriteria = listOf(
                    "Complete medium difficulty puzzles",
                    "Use hints strategically",
                    "Explain reasoning process"
                )
            ),
            TestingScenario(
                id = "older-challenge-${participant.id}",
                name = "Challenge Progression",
                description = "Progressive difficulty adaptation",
                ageGroup = AgeGroup.OLDER,
                experienceLevel = participant.previousSudokuExperience,
                difficultyLevel = "hard",
                estimatedDurationMinutes = 35,
                learningObjectives = listOf(
                    "Persistence in problem-solving",
                    "Advanced strategy application",
                    "Self-assessment of difficulty"
                ),
                successCriteria = listOf(
                    "Attempt hard puzzles",
                    "Use hints appropriately",
                    "Report on challenge level"
                )
            )
        )
    }

    /**
     * Creates scenarios for beginners with no prior experience.
     */
    private fun createBeginnerScenario(participant: Participant): TestingScenario {
        return TestingScenario(
            id = "beginner-intro-${participant.id}",
            name = "Introduction to Sudoku",
            description = "First-time sudoku experience with comprehensive introduction",
            ageGroup = participant.getAgeGroup(),
            experienceLevel = ExperienceLevel.NONE,
            difficultyLevel = "easy",
            estimatedDurationMinutes = 20,
            learningObjectives = listOf(
                "Understanding sudoku rules",
                "Basic navigation",
                "Initial confidence building"
            ),
            successCriteria = listOf(
                "Complete introductory tutorial",
                "Solve first puzzle independently",
                "Express basic understanding"
            )
        )
    }

    /**
     * Creates scenarios for participants with beginner experience.
     */
    private fun createIntermediateScenario(participant: Participant): TestingScenario {
        return TestingScenario(
            id = "intermediate-techniques-${participant.id}",
            name = "Technique Mastery",
            description = "Learning and applying basic solving techniques",
            ageGroup = participant.getAgeGroup(),
            experienceLevel = ExperienceLevel.BEGINNER,
            difficultyLevel = "medium",
            estimatedDurationMinutes = 30,
            learningObjectives = listOf(
                "Basic elimination techniques",
                "Pattern recognition",
                "Hint usage optimization"
            ),
            successCriteria = listOf(
                "Apply learned techniques",
                "Complete puzzles with techniques",
                "Reduce hint usage over time"
            )
        )
    }

    /**
     * Creates scenarios for experienced participants.
     */
    private fun createAdvancedScenario(participant: Participant): TestingScenario {
        return TestingScenario(
            id = "advanced-mastery-${participant.id}",
            name = "Advanced Mastery",
            description = "Complex puzzles and advanced strategies",
            ageGroup = participant.getAgeGroup(),
            experienceLevel = participant.previousSudokuExperience,
            difficultyLevel = "hard",
            estimatedDurationMinutes = 40,
            learningObjectives = listOf(
                "Advanced strategy application",
                "Complex pattern recognition",
                "Self-assessment and adaptation"
            ),
            successCriteria = listOf(
                "Solve complex puzzles",
                "Apply multiple techniques",
                "Analyze and reflect on solving process"
            )
        )
    }

    /**
     * Tracks learning progress for a participant.
     */
    fun trackLearningProgress(
        participant: Participant,
        completedScenarios: List<TestingScenario>,
        sessionResults: Map<String, ScenarioResult>
    ): LearningProgress {
        val totalScenarios = completedScenarios.size
        val completedSuccessfully = sessionResults.values.count { it.success }.toDouble()
        val successRate = if (totalScenarios > 0) completedSuccessfully / totalScenarios else 0.0
        
        val masteredObjectives = mutableSetOf<String>()
        completedScenarios.forEach { scenario ->
            scenario.learningObjectives.forEach { objective ->
                sessionResults[scenario.id]?.let { result ->
                    if (result.success) {
                        masteredObjectives.add(objective)
                    }
                }
            }
        }
        
        val improvementAreas = identifyImprovementAreas(sessionResults)
        
        return LearningProgress(
            participantId = participant.id,
            totalScenarios = totalScenarios,
            successRate = successRate,
            masteredObjectives = masteredObjectives.toList(),
            improvementAreas = improvementAreas,
            timeSpentMinutes = sessionResults.values.sumOf { it.timeSpentMinutes },
            averageSolveTime = sessionResults.values.map { it.timeSpentMinutes }.average(),
            hintsUsed = sessionResults.values.sumOf { it.hintsUsed }
        )
    }

    /**
     * Identifies areas where participants might need improvement.
     */
    private fun identifyImprovementAreas(sessionResults: Map<String, ScenarioResult>): List<String> {
        val improvementAreas = mutableListOf<String>()
        
        // Analyze success rates across different difficulty levels
        val easyResults = sessionResults.values.filter { it.scenarioDifficulty == "easy" }
        val mediumResults = sessionResults.values.filter { it.scenarioDifficulty == "medium" }
        val hardResults = sessionResults.values.filter { it.scenarioDifficulty == "hard" }
        
        if (easyResults.isNotEmpty() && easyResults.map { it.success }.count { it } / easyResults.size < 0.8) {
            improvementAreas.add("Basic sudoku fundamentals")
        }
        
        if (mediumResults.isNotEmpty() && mediumResults.map { it.success }.count { it } / mediumResults.size < 0.6) {
            improvementAreas.add("Intermediate strategies")
        }
        
        if (hardResults.isNotEmpty() && hardResults.map { it.success }.count { it } / hardResults.size < 0.4) {
            improvementAreas.add("Advanced techniques")
        }
        
        // Analyze hint usage
        val avgHints = sessionResults.values.map { it.hintsUsed }.average()
        if (avgHints > 3.0) {
            improvementAreas.add("Hint dependency")
        }
        
        return improvementAreas
    }

    /**
     * Gets recommendations based on effectiveness metrics.
     */
    fun getRecommendations(effectivenessMetrics: com.sudoku.testing.EducationalEffectivenessMetrics): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Analyze overall completion rate
        when {
            effectivenessMetrics.overallAverageCompletionRate >= 0.8 -> {
                recommendations.add("Excellent completion rate! Consider introducing more challenging scenarios.")
            }
            effectivenessMetrics.overallAverageCompletionRate >= 0.6 -> {
                recommendations.add("Good completion rate. Maintain current difficulty progression.")
            }
            effectivenessMetrics.overallAverageCompletionRate < 0.6 -> {
                recommendations.add("Consider reducing difficulty or providing more guidance.")
            }
        }
        
        // Compare age groups
        if (effectivenessMetrics.youngerAverageCompletionRate < effectivenessMetrics.olderAverageCompletionRate - 0.2) {
            recommendations.add("Younger participants may need additional support and simplified interfaces.")
        }
        
        // Analyze hint usage
        when {
            effectivenessMetrics.overallAverageHintsUsed > 4.0 -> {
                recommendations.add("High hint usage detected. Consider improving hint clarity or reducing puzzle difficulty.")
            }
            effectivenessMetrics.overallAverageHintsUsed < 1.0 -> {
                recommendations.add("Low hint usage suggests puzzles may be too easy. Consider increasing difficulty.")
            }
        }
        
        // Analyze session time
        if (effectivenessMetrics.overallAverageSessionTime > 1800) { // 30 minutes
            recommendations.add("Sessions are quite long. Consider breaking into shorter sessions or adding breaks.")
        }
        
        return recommendations
    }

    /**
     * Gets all survey questions for the testing protocol.
     */
    fun getSurveyQuestions(): List<SurveyQuestion> = surveyQuestions

    /**
     * Gets learning objectives for the program.
     */
    fun getLearningObjectives(): List<String> = learningObjectives

    /**
     * Validates testing protocol configuration.
     */
    fun validateConfiguration(): List<String> {
        val validationErrors = mutableListOf<String>()
        
        if (surveyQuestions.isEmpty()) {
            validationErrors.add("No survey questions defined")
        }
        
        if (learningObjectives.isEmpty()) {
            validationErrors.add("No learning objectives defined")
        }
        
        if (difficultyProgression.isEmpty()) {
            validationErrors.add("No difficulty progression defined")
        }
        
        return validationErrors
    }
}

/**
 * Represents a testing scenario for educational validation.
 */
data class TestingScenario(
    val id: String,
    val name: String,
    val description: String,
    val ageGroup: AgeGroup,
    val experienceLevel: ExperienceLevel,
    val difficultyLevel: String,
    val estimatedDurationMinutes: Int,
    val learningObjectives: List<String>,
    val successCriteria: List<String>
)

/**
 * Represents the result of a testing scenario.
 */
data class ScenarioResult(
    val scenarioId: String,
    val scenarioName: String,
    val scenarioDifficulty: String,
    val success: Boolean,
    val timeSpentMinutes: Double,
    val hintsUsed: Int,
    val notes: String = ""
)

/**
 * Represents learning progress tracking.
 */
data class LearningProgress(
    val participantId: String,
    val totalScenarios: Int,
    val successRate: Double,
    val masteredObjectives: List<String>,
    val improvementAreas: List<String>,
    val timeSpentMinutes: Double,
    val averageSolveTime: Double,
    val hintsUsed: Int
)