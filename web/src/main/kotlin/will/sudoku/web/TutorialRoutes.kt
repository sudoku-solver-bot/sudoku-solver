package will.sudoku.web

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
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
data class TutorialTutorialProgressResponse(
    val totalModules: Int,
    val completedModules: Int,
    val percentage: Int,
    val nextModule: TutorialModuleSummary?
)

fun Route.tutorialRoutes() {
    val tutorialSystem = TutorialSystem()

    get("/tutorials", {
        tags = listOf("Tutorial")
        description = "Get all available tutorial modules"
        response {
            HttpStatusCode.OK to {
                description = "List of tutorial modules"
                body<TutorialListResponse>()
            }
        }
    }) {
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
                recommended = modules.firstOrNull()
            )
        )
    }
    
    get("/tutorials/{id}", {
        tags = listOf("Tutorial")
        description = "Get a specific tutorial module"
        response {
            HttpStatusCode.OK to {
                description = "Tutorial module details"
                body<TutorialModuleResponse>()
            }
            HttpStatusCode.NotFound to {
                description = "Tutorial not found"
            }
        }
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
                description = module.description,
                difficulty = module.difficulty.name,
                technique = module.technique,
                steps = module.steps.mapIndexed { index, step ->
                    TutorialStepResponse(
                        stepNumber = index + 1,
                        instruction = step.instruction,
                        highlight = step.highlight.map { CellCoord(it.row, it.col) },
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
    
    post("/tutorials/progress", {
        tags = listOf("Tutorial")
        description = "Get learning progress based on completed modules"
        request {
            body<ProgressRequest> {
                example("sample") {
                    value = ProgressRequest(
                        completedModuleIds = listOf("single-candidate-basics")
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Learning progress"
                body<ProgressResponse>()
            }
        }
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
