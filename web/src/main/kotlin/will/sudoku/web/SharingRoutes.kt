package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import will.sudoku.solver.*

@Serializable
data class GenerateShareCodeRequest(
    val puzzle: String
)

@Serializable
data class GenerateShareCodeResponse(
    val code: String,
    val url: String,
    val social: SocialShareTextResponse
)

@Serializable
data class DecodeShareCodeRequest(
    val code: String
)

@Serializable
data class DecodeShareCodeResponse(
    val puzzle: String,
    val clueCount: Int
)

@Serializable
data class SocialShareTextResponse(
    val title: String,
    val shortText: String,
    val mediumText: String,
    val longText: String,
    val hashtags: List<String>,
    val twitterText: String
)

fun Route.sharingRoutes() {
    val baseUrl = System.getenv("BASE_URL") ?: "https://sudoku-solver-r5y8.onrender.com"

    post("/sharing/code") {
        val request = call.receive<GenerateShareCodeRequest>()
        
        try {
            val code = PuzzleSharing.generateShareCode(request.puzzle)
            val url = PuzzleSharing.generateShareUrl(baseUrl, request.puzzle)
            val social = PuzzleSharing.generateSocialText(request.puzzle)
            
            call.respond(
                GenerateShareCodeResponse(
                    code = code,
                    url = url,
                    social = SocialShareTextResponse(
                        title = social.title,
                        shortText = social.shortText,
                        mediumText = social.mediumText,
                        longText = social.longText,
                        hashtags = social.hashtags,
                        twitterText = social.twitterText
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Invalid puzzle"))
            )
        }
    }
    
    get("/sharing/decode/{code}") {
        val code = call.parameters["code"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing share code")
        )
        
        try {
            val puzzle = PuzzleSharing.decodeShareCode(code)
            val clueCount = puzzle.count { it != '0' }
            
            call.respond(
                DecodeShareCodeResponse(
                    puzzle = puzzle,
                    clueCount = clueCount
                )
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Invalid share code"))
            )
        }
    }
    
    post("/sharing/social") {
        val request = call.receive<GenerateShareCodeRequest>()
        
        try {
            val social = PuzzleSharing.generateSocialText(request.puzzle)
            
            call.respond(
                SocialShareTextResponse(
                    title = social.title,
                    shortText = social.shortText,
                    mediumText = social.mediumText,
                    longText = social.longText,
                    hashtags = social.hashtags,
                    twitterText = social.twitterText
                )
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (e.message ?: "Invalid puzzle"))
            )
        }
    }
}
