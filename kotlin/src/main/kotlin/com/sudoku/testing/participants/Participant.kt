package com.sudoku.testing.participants

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a participant in the educational user testing program.
 * Designed for children aged 8-14.
 */
data class Participant(
    val id: String,
    val age: Int,
    val gradeLevel: Int,
    val parentEmail: String,
    val consentDate: LocalDate,
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    val previousSudokuExperience: ExperienceLevel = ExperienceLevel.NONE,
    val notes: String = ""
) {
    init {
        require(age in 8..14) { "Participant age must be between 8 and 14. Provided: $age" }
        require(gradeLevel in 2..8) { "Grade level must be between 2 and 8. Provided: $gradeLevel" }
        require(parentEmail.isNotBlank()) { "Parent email cannot be blank" }
        require(!consentDate.isAfter(LocalDate.now())) { "Consent date cannot be in the future" }
    }

    /**
     * Determines if this participant is in the younger age group (8-10).
     */
    val isYoungerGroup: Boolean
        get() = age <= 10

    /**
     * Determines if this participant is in the older age group (11-14).
     */
    val isOlderGroup: Boolean
        get() = age >= 11

    /**
     * Gets the age group category for analysis purposes.
     */
    fun getAgeGroup(): AgeGroup = when {
        age <= 10 -> AgeGroup.YOUNGER
        else -> AgeGroup.OLDER
    }

    /**
     * Checks if the participant has prior Sudoku experience.
     */
    fun hasPriorExperience(): Boolean = previousSudokuExperience != ExperienceLevel.NONE

    /**
     * Creates a summary of the participant for reporting.
     */
    fun createSummary(): ParticipantSummary = ParticipantSummary(
        id = id,
        age = age,
        ageGroup = getAgeGroup(),
        gradeLevel = gradeLevel,
        experienceLevel = previousSudokuExperience,
        hasPriorExperience = hasPriorExperience()
    )
}

/**
 * Enum representing experience levels with Sudoku.
 */
enum class ExperienceLevel {
    NONE,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    companion object {
        fun fromString(input: String): ExperienceLevel {
            return try {
                valueOf(input.uppercase())
            } catch (e: IllegalArgumentException) {
                NONE
            }
        }
    }
}

/**
 * Age group categories for analysis.
 */
enum class AgeGroup {
    YOUNGER,  // Ages 8-10
    OLDER     // Ages 11-14
}

/**
 * Simplified summary of participant information for reports.
 */
data class ParticipantSummary(
    val id: String,
    val age: Int,
    val ageGroup: AgeGroup,
    val gradeLevel: Int,
    val experienceLevel: ExperienceLevel,
    val hasPriorExperience: Boolean
)

/**
 * Represents a testing session for a participant.
 */
data class ParticipantSession(
    val sessionId: String,
    val participantId: String,
    val startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,
    var completed: Boolean = false,
    var puzzlesAttempted: Int = 0,
    var puzzlesSolved: Int = 0,
    var totalHintsUsed: Int = 0,
    var totalTimeSeconds: Long = 0L
) {
    /**
     * Calculates the completion rate for this session.
     */
    fun completionRate(): Double = if (puzzlesAttempted > 0) {
        puzzlesSolved.toDouble() / puzzlesAttempted.toDouble()
    } else 0.0

    /**
     * Marks the session as completed and sets the end time.
     */
    fun complete() {
        completed = true
        endTime = LocalDateTime.now()
        totalTimeSeconds = java.time.Duration.between(startTime, endTime).toSeconds()
    }

    /**
     * Records a puzzle attempt.
     */
    fun recordPuzzleAttempt(solved: Boolean) {
        puzzlesAttempted++
        if (solved) puzzlesSolved++
    }

    /**
     * Records hint usage.
     */
    fun recordHint() {
        totalHintsUsed++
    }
}
