package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import will.sudoku.solver.*

fun Route.userTestingRoutes() {
    // In-memory storage for demo purposes (would be replaced with database)
    val participants = mutableMapOf<String, TestParticipant>()
    val sessions = mutableMapOf<String, TestingSession>()
    val surveySubmissions = mutableMapOf<String, SurveySubmission>()
    val learningMetrics = mutableMapOf<String, List<LearningMetrics>>()
    
    post("/user-testing/participant") {
        try {
            val request = call.receive<CreateParticipantRequest>()
            
            val participant = TestParticipant(
                participantId = request.participantId,
                age = request.age,
                ageGroup = AgeGroup.fromAge(request.age),
                parentEmail = request.parentEmail,
                childName = request.childName,
                assignedVariant = request.assignedVariant
            )
            
            participants[participant.participantId] = participant
            
            call.respond(mapOf(
                "success" to true,
                "participantId" to participant.participantId,
                "ageGroup" to participant.ageGroup.displayName,
                "assignedVariant" to participant.assignedVariant.name
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    post("/user-testing/session") {
        try {
            val request = call.receive<CreateSessionRequest>()
            
            val session = TestingSession(
                sessionId = request.sessionId,
                participantId = request.participantId,
                phase = request.phase,
                startTime = java.time.LocalDateTime.now(),
                puzzleId = request.puzzleId,
                difficultyLevel = request.difficultyLevel
            )
            
            sessions[session.sessionId] = session
            
            call.respond(mapOf(
                "success" to true,
                "sessionId" to session.sessionId,
                "phase" to session.phase.name,
                "startTime" to session.startTime.toString()
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    post("/user-testing/session/{sessionId}/complete") {
        try {
            val sessionId = call.parameters["sessionId"] ?: throw IllegalArgumentException("Session ID required")
            val session = sessions[sessionId] ?: throw IllegalArgumentException("Session not found")
            
            val completedSession = session.copy(
                endTime = java.time.LocalDateTime.now(),
                status = SessionStatus.COMPLETED
            )
            
            sessions[sessionId] = completedSession
            
            call.respond(mapOf(
                "success" to true,
                "sessionId" to completedSession.sessionId,
                "completionTime" to completedSession.endTime.toString(),
                "durationSeconds" to java.time.Duration.between(
                    completedSession.startTime, 
                    completedSession.endTime
                ).seconds
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    post("/user-testing/action") {
        try {
            val request = call.receive<RecordActionRequest>()
            
            val action = SessionAction(
                actionId = java.util.UUID.randomUUID().toString(),
                sessionId = request.sessionId,
                timestamp = java.time.LocalDateTime.now(),
                actionType = request.actionType,
                details = request.details
            )
            
            // In real implementation, save action to database
            // For demo, just acknowledge receipt
            call.respond(mapOf(
                "success" to true,
                "actionId" to action.actionId,
                "timestamp" to action.timestamp.toString()
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    post("/user-testing/survey/submit") {
        try {
            val request = call.receive<SubmitSurveyRequest>()
            
            val submission = SurveySubmission(
                submissionId = java.util.UUID.randomUUID().toString(),
                surveyId = request.surveyId,
                participantId = request.participantId,
                sessionId = request.sessionId,
                responses = request.responses,
                submittedAt = java.time.LocalDateTime.now()
            )
            
            surveySubmissions[submission.submissionId] = submission
            
            call.respond(mapOf(
                "success" to true,
                "submissionId" to submission.submissionId,
                "surveyId" to submission.surveyId,
                "submittedAt" to submission.submittedAt.toString()
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    get("/user-testing/protocol/{ageGroup}") {
        try {
            val ageGroupParam = call.parameters["ageGroup"] ?: throw IllegalArgumentException("Age group required")
            val ageGroup = AgeGroup.valueOf(ageGroupParam)
            
            val protocol = TestingProtocolFactory().createProtocolForAgeGroup(ageGroup)
            
            call.respond(protocol)
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    get("/user-testing/survey/{surveyId}") {
        try {
            val surveyId = call.parameters["surveyId"] ?: throw IllegalArgumentException("Survey ID required")
            val ageGroupParam = call.parameters["ageGroup"] ?: throw IllegalArgumentException("Age group required")
            val ageGroup = AgeGroup.valueOf(ageGroupParam)
            
            val survey = when (surveyId) {
                "post-tutorial" -> SurveyFactory().createPostTutorialSurvey(ageGroup)
                "post-practice" -> SurveyFactory().createPostPracticeSurvey(ageGroup)
                "final" -> SurveyFactory().createFinalSurvey(ageGroup)
                else -> throw IllegalArgumentException("Unknown survey ID")
            }
            
            call.respond(survey)
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    get("/user-testing/participant/{participantId}/progress") {
        try {
            val participantId = call.parameters["participantId"] ?: throw IllegalArgumentException("Participant ID required")
            
            val metrics = learningMetrics[participantId] ?: emptyList()
            
            call.respond(mapOf(
                "participantId" to participantId as Any,
                "metrics" to metrics.map { m -> mapOf(
                    "puzzlesCompleted" to m.puzzlesCompleted,
                    "currentLevel" to m.currentLevel,
                    "xpGained" to m.xpGained
                ) } as Any,
                "totalPuzzles" to metrics.sumOf { it.puzzlesCompleted } as Any,
                "currentLevel" to (metrics.maxOfOrNull { it.currentLevel } ?: 1) as Any,
                "totalXP" to metrics.sumOf { it.xpGained } as Any
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    get("/user-testing/features/{variant}") {
        try {
            val variantParam = call.parameters["variant"] ?: throw IllegalArgumentException("Variant required")
            val variant = ABTestVariant.valueOf(variantParam)
            
            val features = when (variant) {
                ABTestVariant.CONTROL -> setOf(
                    EducationalFeature.VISUAL_FEEDBACK,
                    EducationalFeature.PROGRESS_BAR,
                    EducationalFeature.TIMER
                )
                ABTestVariant.VARIANT_A -> setOf(
                    EducationalFeature.VISUAL_FEEDBACK,
                    EducationalFeature.PROGRESS_BAR,
                    EducationalFeature.HINT_SYSTEM,
                    EducationalFeature.CELEBRATION
                )
                ABTestVariant.VARIANT_B -> setOf(
                    EducationalFeature.VISUAL_FEEDBACK,
                    EducationalFeature.PROGRESS_BAR,
                    EducationalFeature.STEP_BY_STEP,
                    EducationalFeature.TUTORIAL_MODE,
                    EducationalFeature.CELEBRATION
                )
            }
            
            call.respond(mapOf(
                "variant" to variant.name,
                "features" to features.map { it.name to it.displayName }.toMap()
            ))
        } catch (e: Exception) {
            call.respond(status = io.ktor.http.HttpStatusCode.BadRequest, mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}

// Request DTOs
@Serializable
data class CreateParticipantRequest(
    val participantId: String,
    val age: Int,
    val parentEmail: String,
    val childName: String,
    val assignedVariant: ABTestVariant = ABTestVariant.CONTROL
)

@Serializable
data class CreateSessionRequest(
    val sessionId: String,
    val participantId: String,
    val phase: TestPhase,
    val puzzleId: String? = null,
    val difficultyLevel: DifficultyLevel? = null
)

@Serializable
data class RecordActionRequest(
    val sessionId: String,
    val actionType: ActionType,
    val details: Map<String, @Contextual Any> = emptyMap()
)

@Serializable
data class SubmitSurveyRequest(
    val surveyId: String,
    val participantId: String,
    val sessionId: String,
    val responses: Map<String, @Contextual SurveyResponse>,
    val overallRating: Int? = null,
    val comments: String? = null
)