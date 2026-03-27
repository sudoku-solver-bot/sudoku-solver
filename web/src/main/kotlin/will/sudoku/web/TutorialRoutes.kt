package will.sudoku.web

import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class TutorialListResponse(
    val modules: List<TutorialModuleSummary>,
    val recommended: TutorialModuleSummary?
)

@Serializable
data class TutorialModuleSummary(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val technique: String,
    val estimatedMinutes: Int,
    val prerequisites: List<String>,
    val completed: Boolean = false
)

@Serializable
data class TutorialModuleResponse(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val technique: String,
    val steps: List<TutorialStepResponse>,
    val practicePuzzles: List<String>,
    val estimatedMinutes: Int,
    val prerequisites: List<String>
)

@Serializable
data class TutorialStepResponse(
    val stepNumber: Int,
    val instruction: String,
    val highlight: List<CellCoord>,
    val expectedAction: String,
    val hint: String,
    val successMessage: String,
    val teachingPoint: String
)

@Serializable
data class ProgressRequest(
    val completedModuleIds: List<String>
)

@Serializable
data class TutorialProgressResponse(
    val totalModules: Int,
    val completedModules: Int,
    val percentage: Int,
    val nextModule: TutorialModuleSummary?
)

fun Route.tutorialRoutes() {
    val tutorialSystem = TutorialSystem()

    get("/tutorials", {
    }) {
        val modules = tutorialSystem.getModules().map { module ->
            TutorialModuleSummary(
                id = module.id,
                title = module.title,
        
        call.respond(
            TutorialListResponse(
                modules = modules,
                recommended = modules.firstOrNull()
            )
        )
    }
    
    get("/tutorials/{id}", {
    }) {
        val moduleId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing tutorial ID")
        )
        
        val module = tutorialSystem.getModule(moduleId)
            ?: return@get call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Tutorial not found: $moduleId")
            )
        
        call.respond(
            TutorialModuleResponse(
                id = module.id,
                title = module.title,
    
    post("/tutorials/progress") {
    }) {
        val request = call.receive<ProgressRequest>()
        val completedIds = request.completedModuleIds.toSet()
        
        val progress = tutorialSystem.calculateProgress(completedIds)
        
        call.respond(
            TutorialProgressResponse(
                totalModules = progress.totalModules,
                completedModules = progress.completedModules,
                percentage = progress.percentage,
                nextModule = progress.nextModule?.let { module ->
                    TutorialModuleSummary(
                        id = module.id,
                        title = module.title,
            )
        )
    }
}
