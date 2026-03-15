package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    
    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
    }
    
    routing {
        // Serve index.html at root
        get("/") {
            call.respondText(
                javaClass.classLoader.getResource("static/index.html")?.readText() ?: "Not found",
                io.ktor.http.ContentType.Text.Html
            )
        }
        
        route("api") {
            healthRoutes()
            solveRoutes()
            hintRoutes()
            generateRoutes()
            validateRoutes()
        }
    }
}
