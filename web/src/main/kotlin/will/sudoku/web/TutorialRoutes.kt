package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class TutorialModuleSummary(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val technique: String,
    val estimatedMinutes: Int,
    val prerequisites: List<String>
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
    val instruction: String,
    val highlight: List<String>,
    val expectedAction: String,
    val hint: String,
    val successMessage: String,
    val teachingPoint: String
)

@Serializable
data class TutorialProgressRequest(
    val completedModuleIds: List<String>
)

@Serializable
data class TutorialProgressResponse(
    val totalModules: Int,
    val completedModules: Int,
    val percentage: Int,
    val nextModule: TutorialModuleSummary?
)

@Serializable
data class TutorialListResponse(
    val modules: List<TutorialModuleSummary>,
    val totalCount: Int
)

fun Route.tutorialRoutes() {
    val tutorialSystem = TutorialSystem()

    get("/tutorials") {
        val modules = tutorialSystem.getModules().map { module ->
            TutorialModuleSummary(
                id = module.id,
                title = module.title,
                description = module.description,
                difficulty = module.difficulty.name,
                technique = module.technique,
                estimatedMinutes = module.estimatedMinutes,
                prerequisites = module.prerequisites
            )
        }
        
        call.respond(
            TutorialListResponse(
                modules = modules,
                totalCount = modules.size
            )
        )
    }
    
    get("/tutorials/{id}") {
        val moduleId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing module ID")
        )
        
        val module = tutorialSystem.getModule(moduleId)
        if (module == null) {
            return@get call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Module not found")
            )
        }
        
        call.respond(
            TutorialModuleResponse(
                id = module.id,
                title = module.title,
                description = module.description,
                difficulty = module.difficulty.name,
                technique = module.technique,
                steps = module.steps.map { step ->
                    TutorialStepResponse(
                        instruction = step.instruction,
                        highlight = step.highlight.map { "${it.row},${it.col}" },
                        expectedAction = step.expectedAction.name,
                        hint = step.hint,
                        successMessage = step.successMessage,
                        teachingPoint = step.teachingPoint
                    )
                },
                practicePuzzles = module.practicePuzzles,
                estimatedMinutes = module.estimatedMinutes,
                prerequisites = module.prerequisites
            )
        )
    }
    
    post("/tutorials/progress") {
        val request = call.receive<TutorialProgressRequest>()
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
                        description = module.description,
                        difficulty = module.difficulty.name,
                        technique = module.technique,
                        estimatedMinutes = module.estimatedMinutes,
                        prerequisites = module.prerequisites
                    )
                }
            )
        )
    }
}
