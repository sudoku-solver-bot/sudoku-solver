package will.sudoku.solver

import java.time.LocalDateTime

/**
 * A/B test configuration
 */
data class ABTest(
    val testId: String,
    val name: String,
    val description: String,
    val feature: EducationalFeature,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val variants: Map<ABTestVariant, VariantConfig>,
    val trafficSplit: Map<ABTestVariant, Double>, // Percentage for each variant
    val targetMetrics: List<ABTestMetric>,
    val minimumSampleSize: Int,
    val status: ABTestStatus
)

/**
 * Configuration for each variant
 */
data class VariantConfig(
    val variant: ABTestVariant,
    val name: String,
    val description: String,
    val settings: Map<String, Any>
)

/**
 * A/B test status
 */
enum class ABTestStatus {
    DRAFT,
    RUNNING,
    PAUSED,
    COMPLETED,
    ANALYZED
}

/**
 * A/B test analysis results
 */
data class ABTestAnalysis(
    val testId: String,
    val analysisDate: LocalDateTime,
    val sampleSizes: Map<ABTestVariant, Int>,
    val metricResults: Map<ABTestMetric, MetricComparison>,
    val winner: ABTestVariant?,
    val confidenceLevel: Double, // Statistical confidence
    val significance: Double, // P-value
    val recommendation: String
)

/**
 * Comparison of a metric across variants
 */
data class MetricComparison(
    val metric: ABTestMetric,
    val controlValue: Double,
    val variantAValue: Double,
    val variantBValue: Double?,
    val improvement: Double, // Percentage improvement
    val isSignificant: Boolean,
    val confidenceInterval: ConfidenceInterval?
)

data class ConfidenceInterval(
    val lower: Double,
    val upper: Double,
    val level: Double // e.g., 0.95 for 95% confidence
)

/**
 * A/B testing engine
 */
class ABTestEngine {

    private val activeTests = mutableMapOf<String, ABTest>()
    private val participantAssignments = mutableMapOf<String, MutableMap<String, ABTestVariant>>()
    private val testResults = mutableMapOf<String, MutableList<ABTestResult>>()

    /**
     * Create a new A/B test
     */
    fun createTest(test: ABTest): ABTest {
        activeTests[test.testId] = test
        return test
    }

    /**
     * Assign a participant to a test variant
     */
    fun assignVariant(testId: String, participantId: String): ABTestVariant? {
        val test = activeTests[testId] ?: return null

        // Check if already assigned
        if (participantAssignments[participantId]?.containsKey(testId) == true) {
            return participantAssignments[participantId]?.get(testId)
        }

        // Assign based on traffic split
        val variant = selectVariant(test)
        participantAssignments.getOrPut(participantId) { mutableMapOf() }[testId] = variant

        return variant
    }

    /**
     * Select variant based on traffic split
     */
    private fun selectVariant(test: ABTest): ABTestVariant {
        val random = Math.random()
        var cumulative = 0.0

        test.trafficSplit.forEach { (variant, percentage) ->
            cumulative += percentage
            if (random <= cumulative / 100) {
                return variant
            }
        }

        return ABTestVariant.CONTROL
    }

    /**
     * Record a test result
     */
    fun recordResult(result: ABTestResult) {
        testResults.getOrPut(result.testId) { mutableListOf() }.add(result)
    }

    /**
     * Analyze test results
     */
    fun analyzeTest(testId: String): ABTestAnalysis? {
        val test = activeTests[testId] ?: return null
        val results = testResults[testId] ?: return null

        if (results.size < test.minimumSampleSize) {
            return null // Not enough data
        }

        val sampleSizes = results.groupBy { it.variant }
            .mapValues { it.value.size }

        val metricResults = mutableMapOf<ABTestMetric, MetricComparison>()

        test.targetMetrics.forEach { metric ->
            val comparison = compareMetric(metric, results)
            metricResults[metric] = comparison
        }

        val winner = determineWinner(metricResults)
        val significance = calculateSignificance(metricResults)
        val recommendation = generateRecommendation(winner, metricResults, significance)

        return ABTestAnalysis(
            testId = testId,
            analysisDate = LocalDateTime.now(),
            sampleSizes = sampleSizes,
            metricResults = metricResults,
            winner = winner,
            confidenceLevel = 0.95,
            significance = significance,
            recommendation = recommendation
        )
    }

    /**
     * Compare a specific metric across variants
     */
    private fun compareMetric(
        metric: ABTestMetric,
        results: List<ABTestResult>
    ): MetricComparison {
        val controlResults = results.filter { it.variant == ABTestVariant.CONTROL && it.metricType == metric }
        val variantAResults = results.filter { it.variant == ABTestVariant.VARIANT_A && it.metricType == metric }
        val variantBResults = results.filter { it.variant == ABTestVariant.VARIANT_B && it.metricType == metric }

        val controlValue = controlResults.map { it.value }.average()
        val variantAValue = variantAResults.map { it.value }.average()
        val variantBValue = if (variantBResults.isNotEmpty()) {
            variantBResults.map { it.value }.average()
        } else null

        val improvement = if (controlValue > 0) {
            ((variantAValue - controlValue) / controlValue) * 100
        } else 0.0

        val isSignificant = calculateStatisticalSignificance(controlResults, variantAResults)

        val confidenceInterval = if (isSignificant) {
            calculateConfidenceInterval(controlResults, variantAResults)
        } else null

        return MetricComparison(
            metric = metric,
            controlValue = controlValue,
            variantAValue = variantAValue,
            variantBValue = variantBValue,
            improvement = improvement,
            isSignificant = isSignificant,
            confidenceInterval = confidenceInterval
        )
    }

    /**
     * Calculate statistical significance using simple t-test approximation
     */
    private fun calculateStatisticalSignificance(
        control: List<ABTestResult>,
        variant: List<ABTestResult>
    ): Boolean {
        if (control.size < 10 || variant.size < 10) return false

        val controlMean = control.map { it.value }.average()
        val variantMean = variant.map { it.value }.average()
        val controlStd = calculateStdDev(control.map { it.value }, controlMean)
        val variantStd = calculateStdDev(variant.map { it.value }, variantMean)

        // Simple z-score calculation
        val pooledStd = Math.sqrt(
            (controlStd * controlStd / control.size) + (variantStd * variantStd / variant.size)
        )

        val zScore = if (pooledStd > 0) {
            Math.abs((variantMean - controlMean) / pooledStd)
        } else 0.0

        // Z-score > 1.96 for 95% confidence
        return zScore > 1.96
    }

    private fun calculateStdDev(values: List<Double>, mean: Double): Double {
        val variance = values.map { Math.pow(it - mean, 2.0) }.average()
        return Math.sqrt(variance)
    }

    /**
     * Calculate confidence interval
     */
    private fun calculateConfidenceInterval(
        control: List<ABTestResult>,
        variant: List<ABTestResult>
    ): ConfidenceInterval? {
        if (control.size < 10 || variant.size < 10) return null

        val controlMean = control.map { it.value }.average()
        val variantMean = variant.map { it.value }.average()
        val difference = variantMean - controlMean

        val controlStd = calculateStdDev(control.map { it.value }, controlMean)
        val variantStd = calculateStdDev(variant.map { it.value }, variantMean)

        val se = Math.sqrt(
            (controlStd * controlStd / control.size) + (variantStd * variantStd / variant.size)
        )

        // 95% confidence interval
        val margin = 1.96 * se

        return ConfidenceInterval(
            lower = difference - margin,
            upper = difference + margin,
            level = 0.95
        )
    }

    /**
     * Determine overall winner based on all metrics
     */
    private fun determineWinner(metrics: Map<ABTestMetric, MetricComparison>): ABTestVariant? {
        var variantAWins = 0
        var controlWins = 0

        metrics.values.forEach { comparison ->
            if (comparison.isSignificant) {
                if (comparison.improvement > 0) {
                    variantAWins++
                } else {
                    controlWins++
                }
            }
        }

        return when {
            variantAWins > controlWins -> ABTestVariant.VARIANT_A
            controlWins > variantAWins -> ABTestVariant.CONTROL
            else -> null
        }
    }

    /**
     * Calculate overall significance
     */
    private fun calculateSignificance(metrics: Map<ABTestMetric, MetricComparison>): Double {
        val significantMetrics = metrics.values.count { it.isSignificant }
        return if (metrics.isNotEmpty()) {
            significantMetrics.toDouble() / metrics.size
        } else 0.0
    }

    /**
     * Generate recommendation based on results
     */
    private fun generateRecommendation(
        winner: ABTestVariant?,
        metrics: Map<ABTestMetric, MetricComparison>,
        significance: Double
    ): String {
        return when {
            winner == null -> "Insufficient evidence to declare a winner. Continue testing."
            significance < 0.5 -> "Results are not statistically significant. Consider extending the test."
            significance < 0.8 -> "Moderate evidence. Consider additional testing or monitoring."
            winner == ABTestVariant.VARIANT_A -> {
                val improvements = metrics.values.filter { it.improvement > 0 }
                val avgImprovement = improvements.map { it.improvement }.average()
                "Variant A shows statistically significant improvements (avg: ${avgImprovement.toInt()}%). " +
                "Recommend implementing the changes."
            }
            else -> "Control performs better. No changes recommended."
        }
    }

    /**
     * Get variant configuration for a participant
     */
    fun getVariantForParticipant(testId: String, participantId: String): VariantConfig? {
        val test = activeTests[testId] ?: return null
        val variant = participantAssignments[participantId]?.get(testId) ?: return null
        return test.variants[variant]
    }
}

/**
 * Pre-configured A/B tests for educational features
 */
class EducationalABTests {

    fun createHintSystemTest(): ABTest {
        return ABTest(
            testId = "hint-system-001",
            name = "Hint System Style",
            description = "Test different hint delivery methods for effectiveness",
            feature = EducationalFeature.HINT_SYSTEM,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(30),
            variants = mapOf(
                ABTestVariant.CONTROL to VariantConfig(
                    variant = ABTestVariant.CONTROL,
                    name = "Text Hints",
                    description = "Standard text-based hints",
                    settings = mapOf("type" to "text", "detail" to "standard")
                ),
                ABTestVariant.VARIANT_A to VariantConfig(
                    variant = ABTestVariant.VARIANT_A,
                    name = "Visual Hints",
                    description = "Visual highlighting with arrows and colors",
                    settings = mapOf("type" to "visual", "detail" to "standard")
                ),
                ABTestVariant.VARIANT_B to VariantConfig(
                    variant = ABTestVariant.VARIANT_B,
                    name = "Interactive Hints",
                    description = "Interactive guided hints that require user interaction",
                    settings = mapOf("type" to "interactive", "detail" to "standard")
                )
            ),
            trafficSplit = mapOf(
                ABTestVariant.CONTROL to 33.33,
                ABTestVariant.VARIANT_A to 33.33,
                ABTestVariant.VARIANT_B to 33.34
            ),
            targetMetrics = listOf(
                ABTestMetric.COMPLETION_RATE,
                ABTestMetric.HINT_USAGE_RATE,
                ABTestMetric.LEARNING_VELOCITY,
                ABTestMetric.SATISFACTION_SCORE
            ),
            minimumSampleSize = 50,
            status = ABTestStatus.RUNNING
        )
    }

    fun createCelebrationTest(): ABTest {
        return ABTest(
            testId = "celebration-001",
            name = "Celebration Animation Style",
            description = "Test different celebration animations for engagement",
            feature = EducationalFeature.CELEBRATION,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(21),
            variants = mapOf(
                ABTestVariant.CONTROL to VariantConfig(
                    variant = ABTestVariant.CONTROL,
                    name = "Simple Confetti",
                    description = "Basic confetti animation",
                    settings = mapOf("type" to "confetti", "duration" to 2000)
                ),
                ABTestVariant.VARIANT_A to VariantConfig(
                    variant = ABTestVariant.VARIANT_A,
                    name = "Animated Character",
                    description = "Character celebration animation",
                    settings = mapOf("type" to "character", "duration" to 3000)
                ),
                ABTestVariant.VARIANT_B to VariantConfig(
                    variant = ABTestVariant.VARIANT_B,
                    name = "Sound + Visual",
                    description = "Combined sound and visual celebration",
                    settings = mapOf("type" to "hybrid", "duration" to 2500)
                )
            ),
            trafficSplit = mapOf(
                ABTestVariant.CONTROL to 33.33,
                ABTestVariant.VARIANT_A to 33.33,
                ABTestVariant.VARIANT_B to 33.34
            ),
            targetMetrics = listOf(
                ABTestMetric.ENGAGEMENT_TIME,
                ABTestMetric.RETENTION_RATE,
                ABTestMetric.SATISFACTION_SCORE
            ),
            minimumSampleSize = 30,
            status = ABTestStatus.RUNNING
        )
    }

    fun createTutorialModeTest(): ABTest {
        return ABTest(
            testId = "tutorial-001",
            name = "Tutorial Delivery Method",
            description = "Test different tutorial presentation styles",
            feature = EducationalFeature.TUTORIAL_MODE,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(45),
            variants = mapOf(
                ABTestVariant.CONTROL to VariantConfig(
                    variant = ABTestVariant.CONTROL,
                    name = "Sequential Tutorial",
                    description = "Step-by-step linear tutorial",
                    settings = mapOf("type" to "sequential", "interactive" to false)
                ),
                ABTestVariant.VARIANT_A to VariantConfig(
                    variant = ABTestVariant.VARIANT_A,
                    name = "Interactive Tutorial",
                    description = "Interactive, choose-your-path tutorial",
                    settings = mapOf("type" to "interactive", "interactive" to true)
                )
            ),
            trafficSplit = mapOf(
                ABTestVariant.CONTROL to 50.0,
                ABTestVariant.VARIANT_A to 50.0
            ),
            targetMetrics = listOf(
                ABTestMetric.LEARNING_VELOCITY,
                ABTestMetric.COMPLETION_RATE,
                ABTestMetric.SATISFACTION_SCORE
            ),
            minimumSampleSize = 40,
            status = ABTestStatus.RUNNING
        )
    }

    fun createProgressTrackingTest(): ABTest {
        return ABTest(
            testId = "progress-001",
            name = "Progress Visualization",
            description = "Test different ways to show progress",
            feature = EducationalFeature.PROGRESS_BAR,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(30),
            variants = mapOf(
                ABTestVariant.CONTROL to VariantConfig(
                    variant = ABTestVariant.CONTROL,
                    name = "Percentage Bar",
                    description = "Simple percentage completion bar",
                    settings = mapOf("type" to "percentage", "detailed" to false)
                ),
                ABTestVariant.VARIANT_A to VariantConfig(
                    variant = ABTestVariant.VARIANT_A,
                    name = "Detailed Progress",
                    description = "Detailed progress with milestones and levels",
                    settings = mapOf("type" to "detailed", "detailed" to true)
                )
            ),
            trafficSplit = mapOf(
                ABTestVariant.CONTROL to 50.0,
                ABTestVariant.VARIANT_A to 50.0
            ),
            targetMetrics = listOf(
                ABTestMetric.ENGAGEMENT_TIME,
                ABTestMetric.RETENTION_RATE,
                ABTestMetric.COMPLETION_RATE
            ),
            minimumSampleSize = 60,
            status = ABTestStatus.RUNNING
        )
    }

    fun createErrorHighlightingTest(): ABTest {
        return ABTest(
            testId = "error-001",
            name = "Error Feedback Style",
            description = "Test different error indication methods",
            feature = EducationalFeature.ERROR_HIGHLIGHTING,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(21),
            variants = mapOf(
                ABTestVariant.CONTROL to VariantConfig(
                    variant = ABTestVariant.CONTROL,
                    name = "Immediate Highlight",
                    description = "Highlight errors immediately when made",
                    settings = mapOf("timing" to "immediate", "style" to "highlight")
                ),
                ABTestVariant.VARIANT_A to VariantConfig(
                    variant = ABTestVariant.VARIANT_A,
                    name = "Delayed Highlight",
                    description = "Highlight errors after user checks work",
                    settings = mapOf("timing" to "delayed", "style" to "highlight")
                ),
                ABTestVariant.VARIANT_B to VariantConfig(
                    variant = ABTestVariant.VARIANT_B,
                    name = "Gentle Correction",
                    description = "Suggest correction without highlighting error",
                    settings = mapOf("timing" to "immediate", "style" to "suggestive")
                )
            ),
            trafficSplit = mapOf(
                ABTestVariant.CONTROL to 33.33,
                ABTestVariant.VARIANT_A to 33.33,
                ABTestVariant.VARIANT_B to 33.34
            ),
            targetMetrics = listOf(
                ABTestMetric.ERROR_RATE,
                ABTestMetric.LEARNING_VELOCITY,
                ABTestMetric.SATISFACTION_SCORE
            ),
            minimumSampleSize = 50,
            status = ABTestStatus.RUNNING
        )
    }
}
