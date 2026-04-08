package will.sudoku.solver

/**
 * Complete survey with questions
 */
data class Survey(
    val surveyId: String,
    val name: String,
    val description: String,
    val targetAgeGroup: AgeGroup,
    val questions: List<SurveyQuestion>,
    val estimatedTimeMinutes: Int
)

/**
 * Survey response collection
 */
data class SurveySubmission(
    val submissionId: String,
    val surveyId: String,
    val participantId: String,
    val sessionId: String,
    val responses: Map<String, SurveyResponse>,
    val submittedAt: java.time.LocalDateTime
)

/**
 * Factory for creating age-appropriate surveys
 */
class SurveyFactory {

    fun createPostTutorialSurvey(ageGroup: AgeGroup): Survey {
        return when (ageGroup) {
            AgeGroup.YOUNG -> createPostTutorialSurveyYoung()
            AgeGroup.OLD -> createPostTutorialSurveyOld()
        }
    }

    fun createPostPracticeSurvey(ageGroup: AgeGroup): Survey {
        return when (ageGroup) {
            AgeGroup.YOUNG -> createPostPracticeSurveyYoung()
            AgeGroup.OLD -> createPostPracticeSurveyOld()
        }
    }

    fun createFinalSurvey(ageGroup: AgeGroup): Survey {
        return when (ageGroup) {
            AgeGroup.YOUNG -> createFinalSurveyYoung()
            AgeGroup.OLD -> createFinalSurveyOld()
        }
    }

    /**
     * Post-tutorial survey for young learners (8-10)
     * Simple, emoji-based, engaging
     */
    private fun createPostTutorialSurveyYoung(): Survey {
        return Survey(
            surveyId = "post-tutorial-young",
            name = "How was your Sudoku adventure?",
            description = "Tell us what you thought about learning Sudoku!",
            targetAgeGroup = AgeGroup.YOUNG,
            estimatedTimeMinutes = 5,
            questions = listOf(
                SurveyQuestion(
                    questionId = "fun-rating",
                    questionText = "How much FUN did you have learning Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.EMOJI_RATING,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "understanding",
                    questionText = "Do you understand how to play Sudoku now?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "favorite-part",
                    questionText = "What was your FAVORITE part?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "difficulty",
                    questionText = "Was it too hard, too easy, or just right?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                ),
                SurveyQuestion(
                    questionId = "celebration",
                    questionText = "Did you like the celebration animations?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "want-more",
                    questionText = "Do you want to solve more puzzles?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.ENGAGEMENT
                )
            )
        )
    }

    /**
     * Post-practice survey for young learners
     */
    private fun createPostPracticeSurveyYoung(): Survey {
        return Survey(
            surveyId = "post-practice-young",
            name = "How are you doing with Sudoku?",
            description = "Tell us about your practice sessions!",
            targetAgeGroup = AgeGroup.YOUNG,
            estimatedTimeMinutes = 5,
            questions = listOf(
                SurveyQuestion(
                    questionId = "getting-better",
                    questionText = "Do you feel like you're getting BETTER at Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "boring-fun",
                    questionText = "Is doing Sudoku still fun, or is it getting boring?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "help-needed",
                    questionText = "When you got stuck, did the hints help?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "proud",
                    questionText = "Do you feel PROUD when you finish a puzzle?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "too-easy-hard",
                    questionText = "Are the puzzles too easy, too hard, or just right?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                )
            )
        )
    }

    /**
     * Final survey for young learners
     */
    private fun createFinalSurveyYoung(): Survey {
        return Survey(
            surveyId = "final-survey-young",
            name = "Great job finishing! Tell us what you think",
            description = "You did an amazing job! Now tell us about your whole Sudoku journey!",
            targetAgeGroup = AgeGroup.YOUNG,
            estimatedTimeMinutes = 8,
            questions = listOf(
                SurveyQuestion(
                    questionId = "overall-fun",
                    questionText = "Overall, how much FUN did you have?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.EMOJI_RATING,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "learned-lot",
                    questionText = "Did you learn A LOT about Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "best-thing",
                    questionText = "What was the BEST thing about Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "confident-now",
                    questionText = "Do you feel CONFIDENT solving puzzles now?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "tell-friends",
                    questionText = "Would you tell your FRIENDS to try Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "keep-playing",
                    questionText = "Do you want to KEEP PLAYING Sudoku?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "favorite-feature",
                    questionText = "What did you like the MOST?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "own-words",
                    questionText = "Is there anything else you want to tell us?",
                    ageGroup = AgeGroup.YOUNG,
                    responseType = ResponseType.TEXT,
                    category = QuestionCategory.SATISFACTION
                )
            )
        )
    }

    /**
     * Post-tutorial survey for older learners (11-14)
     * More detailed, analytical
     */
    private fun createPostTutorialSurveyOld(): Survey {
        return Survey(
            surveyId = "post-technique-old",
            name = "Technique Learning Feedback",
            description = "Share your thoughts on the techniques you learned",
            targetAgeGroup = AgeGroup.OLD,
            estimatedTimeMinutes = 7,
            questions = listOf(
                SurveyQuestion(
                    questionId = "clarity-rating",
                    questionText = "How clear were the technique explanations?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.USABILITY
                ),
                SurveyQuestion(
                    questionId = "technique-understanding",
                    questionText = "Which techniques do you feel you mastered?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "practice-adequacy",
                    questionText = "Was there enough practice to learn each technique?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "step-by-step-helpful",
                    questionText = "How helpful was the step-by-step guidance?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "visual-feedback-useful",
                    questionText = "Did the visual feedback (color highlighting) help?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "pacing-appropriate",
                    questionText = "Was the pacing of new techniques appropriate?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                )
            )
        )
    }

    /**
     * Post-practice survey for older learners
     */
    private fun createPostPracticeSurveyOld(): Survey {
        return Survey(
            surveyId = "post-practice-old",
            name = "Practice Phase Feedback",
            description = "Tell us about your practice experience",
            targetAgeGroup = AgeGroup.OLD,
            estimatedTimeMinutes = 8,
            questions = listOf(
                SurveyQuestion(
                    questionId = "improvement-sensed",
                    questionText = "Do you feel your solving skills improved?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "difficulty-progression",
                    questionText = "How was the progression from medium to hard puzzles?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                ),
                SurveyQuestion(
                    questionId = "hint-strategy",
                    questionText = "How did you use hints during practice?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.USABILITY
                ),
                SurveyQuestion(
                    questionId = "timer-pressure",
                    questionText = "Did the timer feature help or hurt your focus?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "engagement-level",
                    questionText = "How engaged did you feel during practice sessions?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "challenging-enough",
                    questionText = "Were the puzzles challenging enough?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                ),
                SurveyQuestion(
                    questionId = "error-feedback-helpful",
                    questionText = "Was error highlighting helpful for learning?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.FEATURE_FEEDBACK
                )
            )
        )
    }

    /**
     * Final survey for older learners
     */
    private fun createFinalSurveyOld(): Survey {
        return Survey(
            surveyId = "final-survey-old",
            name = "Program Completion Survey",
            description = "Complete feedback on your Sudoku learning journey",
            targetAgeGroup = AgeGroup.OLD,
            estimatedTimeMinutes = 12,
            questions = listOf(
                SurveyQuestion(
                    questionId = "overall-satisfaction",
                    questionText = "Overall, how satisfied are you with the program?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_10,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "learning-goals-met",
                    questionText = "Did you achieve your learning goals?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "confidence-improvement",
                    questionText = "How much has your confidence in solving Sudoku improved?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "most-useful-feature",
                    questionText = "Which feature was most useful for your learning?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "least-useful-feature",
                    questionText = "Which feature was least useful or could be improved?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.FEATURE_FEEDBACK
                ),
                SurveyQuestion(
                    questionId = "difficulty-appropriate",
                    questionText = "Was the overall difficulty level appropriate for you?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.MULTIPLE_CHOICE,
                    category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
                ),
                SurveyQuestion(
                    questionId = "would-recommend",
                    questionText = "Would you recommend this program to others?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.RATING_1_5,
                    category = QuestionCategory.SATISFACTION
                ),
                SurveyQuestion(
                    questionId = "continue-interest",
                    questionText = "Do you plan to continue playing Sudoku?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.ENGAGEMENT
                ),
                SurveyQuestion(
                    questionId = "skills-transfer",
                    questionText = "Do you think these skills help in other areas?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.YES_NO,
                    category = QuestionCategory.LEARNING_EFFECTIVENESS
                ),
                SurveyQuestion(
                    questionId = "technical-issues",
                    questionText = "Did you experience any technical issues?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.TEXT,
                    category = QuestionCategory.USABILITY
                ),
                SurveyQuestion(
                    questionId = "suggestions",
                    questionText = "What suggestions do you have to improve the program?",
                    ageGroup = AgeGroup.OLD,
                    responseType = ResponseType.TEXT,
                    category = QuestionCategory.SATISFACTION
                )
            )
        )
    }
}

/**
 * Parent/Teacher feedback survey
 */
data class ParentTeacherSurvey(
    val surveyId: String = "parent-teacher-v1",
    val questions: List<SurveyQuestion> = listOf(
        SurveyQuestion(
            questionId = "child-engagement",
            questionText = "How engaged did your child/student seem while using the program?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.RATING_1_5,
            category = QuestionCategory.ENGAGEMENT
        ),
        SurveyQuestion(
            questionId = "skill-improvement-observed",
            questionText = "Have you observed improvement in their problem-solving skills?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.YES_NO,
            category = QuestionCategory.LEARNING_EFFECTIVENESS
        ),
        SurveyQuestion(
            questionId = "difficulty-appropriate-child",
            questionText = "Was the difficulty level appropriate for your child/student?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.MULTIPLE_CHOICE,
            category = QuestionCategory.DIFFICULTY_APPROPRIATENESS
        ),
        SurveyQuestion(
            questionId = "time-spent-appropriate",
            questionText = "Was the time commitment appropriate?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.MULTIPLE_CHOICE,
            category = QuestionCategory.USABILITY
        ),
        SurveyQuestion(
            questionId = "dashboard-useful",
            questionText = "How useful was the parent/teacher dashboard?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.RATING_1_5,
            category = QuestionCategory.USABILITY
        ),
        SurveyQuestion(
            questionId = "would-recommend-parent",
            questionText = "Would you recommend this to other parents/teachers?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.YES_NO,
            category = QuestionCategory.SATISFACTION
        ),
        SurveyQuestion(
            questionId = "parent-comments",
            questionText = "Any additional comments or observations?",
            ageGroup = AgeGroup.YOUNG,
            responseType = ResponseType.TEXT,
            category = QuestionCategory.SATISFACTION
        )
    )
)
