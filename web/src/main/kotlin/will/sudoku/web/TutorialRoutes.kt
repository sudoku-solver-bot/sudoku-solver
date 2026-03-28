package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class TutorialResponse(
    val id: String,
    val title: String,
    val description: String
)

fun Route.tutorialRoutes() {
    val tutorialSystem = TutorialSystem()

    get("/api/v1/tutorials") {
        val tutorials = tutorialSystem.getTutorials().map {
            TutorialResponse(it.id, it.title, it.description)
        }
        call.respond(tutorials)
    }
}
