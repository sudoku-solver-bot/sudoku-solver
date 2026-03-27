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
import io.ktor.server.plugins.ratelimit.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module)
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
    
    // Rate limiting: 100 requests per minute per IP
    install(RateLimit) {
        register {
            rateLimiter(100, 1.minutes)
            requestKey { call ->
                call.request.local.remoteAddress
            }
        }
    }
    
    routing {
        // Serve Vue app at root
        get("/") {
            call.respondText(
                javaClass.classLoader.getResource("static/index.html")?.readText() ?: "Not found",
                io.ktor.http.ContentType.Text.Html
            )
        }
        
        // Serve static assets (JS, CSS)
        static("/assets") {
            resources("static/assets")
        }
        
        // Versioned API routes (v1)
        route("api/v1") {
            // Add API version header to all responses
            intercept(ApplicationCallPipeline.Plugins) {
                call.response.headers.append("X-API-Version", "1.0")
            }
            
            healthRoutes()
            solveRoutes()
            hintRoutes()
            generateRoutes()
            validateRoutes()
            stepByStepRoutes()
        }
        
        // Legacy routes (unversioned) - DEPRECATED, will be removed in v2
        // These redirect to /api/v1/* for backward compatibility
        route("api") {
            // Add deprecation warning header
            intercept(ApplicationCallPipeline.Plugins) {
                call.response.headers.append("X-API-Warn", "Deprecated: Use /api/v1/ instead. This endpoint will be removed in v2")
                call.response.headers.append("Deprecation", "true")
            }
            
            healthRoutes()
            solveRoutes()
            hintRoutes()
            generateRoutes()
            validateRoutes()
            stepByStepRoutes()
        }
    }
}
