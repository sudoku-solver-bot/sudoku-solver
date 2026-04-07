package will.sudoku.solver

import java.time.LocalDate

/**
 * Enhanced progress tracking system integrating with user testing framework
 */
class ProgressTrackingSystem(
    private val dataCollection: DataCollectionSystem = DataCollection.getInstance()
) {

    /**
     * Get comprehensive progress for a participant
     */
    fun getComprehensiveProgress(participantId: String): ComprehensiveProgress? {
        val participant = dataCollection.getParticipant(participantId) ?: return null
        val analytics = dataCollection.getParticipantAnalytics(participantId)
        val learningMetrics = dataCollection.getLearningMetrics(participantId)

        if (analytics.isEmpty()) {
            return createInitialProgress(participant)
        }

        val collector = UsageAnalyticsCollector()
        val learningProgress = collector.calculateLearningProgress(
            participantId,
            analytics,
            participant.startDate
        )

        val engagement = collector.calculateEngagementMetrics(
            participantId,
            analytics,
            participant.startDate,
            LocalDate.now()
        )

        val skillAssessment = assessSkills(analytics, learningMetrics)

        return ComprehensiveProgress(
            participantId = participantId,
            participantName = participant.childName,
            ageGroup = participant.ageGroup,
            level = learningProgress.currentLevel,
            xp = learningProgress.xpGained,
            puzzlesCompleted = learningProgress.totalPuzzlesCompleted,
            completionRate = learningProgress.completionRate,
            averageTime = learningProgress.averageCompletionTime,
            streak = learningProgress.streakDays,
            techniques = learningProgress.techniquesMastered,
            skillAssessment = skillAssessment,
            engagement = engagement,
            learningProgress = learningProgress,
            nextMilestone = calculateNextMilestone(learningProgress),
            recommendations = generateProgressRecommendations(learningProgress, skillAssessment)
        )
    }

    /**
     * Get progress summary for display
     */
    fun getProgressSummary(participantId: String): ProgressSummary? {
        val comprehensive = getComprehensiveProgress(participantId) ?: return null

        return ProgressSummary(
            level = comprehensive.level,
            xp = comprehensive.xp,
            xpToNext = calculateXPToNext(comprehensive.level),
            puzzlesCompleted = comprehensive.puzzlesCompleted,
            completionRate = comprehensive.completionRate,
            streak = comprehensive.streak,
            techniquesCount = comprehensive.techniques.size,
            overallSkillLevel = comprehensive.skillAssessment.overallLevel
        )
    }

    /**
     * Update progress after a puzzle completion
     */
    fun updateProgressFromPuzzle(
        participantId: String,
        sessionId: String,
        completed: Boolean,
        timeSeconds: Int,
        hintsUsed: Int,
        errors: Int
    ): ProgressUpdate {
        val current = getComprehensiveProgress(participantId)

        val newXp = (current?.xp ?: 0) + if (completed) 10 else 0
        val newPuzzles = (current?.puzzlesCompleted ?: 0) + if (completed) 1 else 0
        val newLevel = calculateLevelFromXP(newXp)

        val xpGained = if (completed) 10 else 0
        val leveledUp = newLevel > (current?.level ?: 0)

        return ProgressUpdate(
            participantId = participantId,
            previousLevel = current?.level ?: 0,
            newLevel = newLevel,
            xpGained = xpGained,
            totalXP = newXp,
            puzzlesCompleted = newPuzzles,
            leveledUp = leveledUp,
            achievementsUnlocked = if (leveledUp) listOf("Level $newLevel") else emptyList()
        )
    }

    /**
     * Get progress toward specific goals
     */
    fun getGoalProgress(participantId: String): GoalProgress {
        val comprehensive = getComprehensiveProgress(participantId)

        return GoalProgress(
            dailyPuzzleGoal = Goal(
                name = "Daily Puzzle",
                target = 1,
                current = comprehensive?.engagement?.sessionsCompleted ?: 0,
                unit = "puzzles",
                completed = (comprehensive?.engagement?.sessionsCompleted ?: 0) >= 1
            ),
            weeklyPuzzlesGoal = Goal(
                name = "Weekly Puzzles",
                target = 7,
                current = comprehensive?.engagement?.sessionsCompleted ?: 0,
                unit = "puzzles",
                completed = (comprehensive?.engagement?.sessionsCompleted ?: 0) >= 7
            ),
            techniqueGoal = Goal(
                name = "Learn 5 Techniques",
                target = 5,
                current = comprehensive?.techniques?.size ?: 0,
                unit = "techniques",
                completed = (comprehensive?.techniques?.size ?: 0) >= 5
            ),
            streakGoal = Goal(
                name = "7-Day Streak",
                target = 7,
                current = comprehensive?.streak ?: 0,
                unit = "days",
                completed = (comprehensive?.streak ?: 0) >= 7
            )
        )
    }

    /**
     * Get progress history for charts
     */
    fun getProgressHistory(participantId: String, days: Int = 30): List<ProgressSnapshot> {
        val learningMetrics = dataCollection.getLearningMetrics(participantId)
        val cutoffDate = LocalDate.now().minusDays(days.toLong())

        return learningMetrics
            .filter { it.date >= cutoffDate }
            .map { metric ->
                ProgressSnapshot(
                    date = metric.date,
                    puzzlesCompleted = metric.puzzlesCompleted,
                    averageTime = metric.averageCompletionTime,
                    techniquesCount = metric.techniquesLearned.size,
                    level = metric.currentLevel
                )
            }
            .sortedBy { it.date }
    }

    /**
     * Create initial progress for new participants
     */
    private fun createInitialProgress(participant: TestParticipant): ComprehensiveProgress {
        return ComprehensiveProgress(
            participantId = participant.participantId,
            participantName = participant.childName,
            ageGroup = participant.ageGroup,
            level = 0,
            xp = 0,
            puzzlesCompleted = 0,
            completionRate = 0.0,
            averageTime = 0.0,
            streak = 0,
            techniques = emptySet(),
            skillAssessment = SkillAssessment(
                overallLevel = 1,
                cognitiveSkills = mapOf(
                    "Pattern Recognition" to 1,
                    "Logical Reasoning" to 1,
                    "Focus" to 1
                ),
                problemSolvingSkills = mapOf(
                    "Deduction" to 1,
                    "Strategy" to 1
                ),
                strengths = emptyList(),
                areasToImprove = listOf("Basic Techniques", "Confidence")
            ),
            engagement = EngagementMetrics(
                participantId = participant.participantId,
                periodStart = participant.startDate,
                periodEnd = LocalDate.now(),
                sessionsCompleted = 0,
                averageSessionDuration = 0.0,
                averagePuzzlesPerSession = 0.0,
                returnRate = 0.0,
                peakActivityTime = "N/A",
                longestStreak = 0,
                totalActiveTime = 0
            ),
            learningProgress = LearningProgress(
                participantId = participant.participantId,
                startDate = participant.startDate,
                currentDate = LocalDate.now(),
                totalSessions = 0,
                totalPuzzlesAttempted = 0,
                totalPuzzlesCompleted = 0,
                completionRate = 0.0,
                averageCompletionTime = 0.0,
                improvementRate = 0.0,
                techniquesMastered = emptySet(),
                currentLevel = 0,
                xpGained = 0,
                streakDays = 0,
                lastActivityDate = participant.startDate
            ),
            nextMilestone = "Complete first puzzle",
            recommendations = listOf("Start with easy puzzles", "Use hints when needed")
        )
    }

    /**
     * Assess skills from analytics and metrics
     */
    private fun assessSkills(
        analytics: List<UsageAnalytics>,
        learningMetrics: List<LearningMetrics>
    ): SkillAssessment {
        val techniques = learningMetrics.flatMap { it.techniquesLearned }.toSet()

        val cognitiveLevel = when {
            techniques.size >= 5 -> 4
            techniques.size >= 3 -> 3
            techniques.size >= 2 -> 2
            else -> 1
        }

        val overallLevel = when {
            techniques.size >= 8 -> 5
            techniques.size >= 5 -> 4
            techniques.size >= 3 -> 3
            techniques.size >= 1 -> 2
            else -> 1
        }

        val strengths = mutableListOf<String>()
        val improvements = mutableListOf<String>()

        if (techniques.contains("Single Candidate")) strengths.add("Basic Techniques")
        else improvements.add("Basic Techniques")

        if (techniques.contains("Naked Pairs")) strengths.add("Pattern Recognition")
        else improvements.add("Advanced Patterns")

        return SkillAssessment(
            overallLevel = overallLevel,
            cognitiveSkills = mapOf(
                "Pattern Recognition" to cognitiveLevel,
                "Logical Reasoning" to cognitiveLevel,
                "Focus" to (cognitiveLevel + 1).coerceAtMost(5)
            ),
            problemSolvingSkills = mapOf(
                "Deduction" to cognitiveLevel,
                "Strategy" to (cognitiveLevel - 1).coerceAtLeast(1)
            ),
            strengths = strengths,
            areasToImprove = improvements
        )
    }

    /**
     * Calculate next milestone
     */
    private fun calculateNextMilestone(progress: LearningProgress): String {
        return when {
            progress.totalPuzzlesCompleted == 0 -> "Complete your first puzzle"
            progress.totalPuzzlesCompleted < 10 -> "Complete ${10 - progress.totalPuzzlesCompleted} more puzzles to reach 10"
            progress.currentLevel < 3 -> "Reach Level 3"
            progress.streakDays < 7 -> "Build a 7-day streak"
            else -> "Master medium difficulty puzzles"
        }
    }

    /**
     * Generate progress recommendations
     */
    private fun generateProgressRecommendations(
        progress: LearningProgress,
        skillAssessment: SkillAssessment
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (progress.completionRate < 0.5) {
            recommendations.add("Try easier puzzles to build confidence")
        }

        if (progress.improvementRate < 10) {
            recommendations.add("Review tutorials for techniques you find difficult")
        }

        if (progress.streakDays == 0) {
            recommendations.add("Try solving at least one puzzle daily")
        }

        skillAssessment.areasToImprove.forEach { area ->
            recommendations.add("Practice: $area")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Excellent progress! Keep up the great work!")
        }

        return recommendations
    }

    private fun calculateLevelFromXP(xp: Int): Int {
        return when {
            xp >= 1000 -> 10
            xp >= 810 -> 9
            xp >= 640 -> 8
            xp >= 490 -> 7
            xp >= 360 -> 6
            xp >= 250 -> 5
            xp >= 160 -> 4
            xp >= 90 -> 3
            xp >= 40 -> 2
            xp >= 10 -> 1
            else -> 0
        }
    }

    private fun calculateXPToNext(level: Int): Int {
        return when (level) {
            0 -> 10
            1 -> 30
            2 -> 50
            3 -> 70
            4 -> 90
            5 -> 110
            6 -> 130
            7 -> 150
            8 -> 170
            9 -> 190
            else -> 0
        }
    }
}

/**
 * Comprehensive progress information
 */
data class ComprehensiveProgress(
    val participantId: String,
    val participantName: String,
    val ageGroup: AgeGroup,
    val level: Int,
    val xp: Int,
    val puzzlesCompleted: Int,
    val completionRate: Double,
    val averageTime: Double,
    val streak: Int,
    val techniques: Set<String>,
    val skillAssessment: SkillAssessment,
    val engagement: EngagementMetrics,
    val learningProgress: LearningProgress,
    val nextMilestone: String,
    val recommendations: List<String>
)

/**
 * Skill assessment
 */
data class SkillAssessment(
    val overallLevel: Int,
    val cognitiveSkills: Map<String, Int>,
    val problemSolvingSkills: Map<String, Int>,
    val strengths: List<String>,
    val areasToImprove: List<String>
)

/**
 * Progress summary for quick display
 */
data class ProgressSummary(
    val level: Int,
    val xp: Int,
    val xpToNext: Int,
    val puzzlesCompleted: Int,
    val completionRate: Double,
    val streak: Int,
    val techniquesCount: Int,
    val overallSkillLevel: Int
)

/**
 * Progress update after an action
 */
data class ProgressUpdate(
    val participantId: String,
    val previousLevel: Int,
    val newLevel: Int,
    val xpGained: Int,
    val totalXP: Int,
    val puzzlesCompleted: Int,
    val leveledUp: Boolean,
    val achievementsUnlocked: List<String>
)

/**
 * Goal progress
 */
data class GoalProgress(
    val dailyPuzzleGoal: Goal,
    val weeklyPuzzlesGoal: Goal,
    val techniqueGoal: Goal,
    val streakGoal: Goal
)

data class Goal(
    val name: String,
    val target: Int,
    val current: Int,
    val unit: String,
    val completed: Boolean
)

/**
 * Progress snapshot for history
 */
data class ProgressSnapshot(
    val date: LocalDate,
    val puzzlesCompleted: Int,
    val averageTime: Double,
    val techniquesCount: Int,
    val level: Int
)
