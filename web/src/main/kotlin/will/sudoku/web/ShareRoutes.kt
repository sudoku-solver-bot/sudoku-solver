package will.sudoku.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Request/response models for puzzle sharing API.
 */

@Serializable
data class ShareRequest(
    val puzzle: String
)

@Serializable
data class ShareResponse(
    val success: Boolean,
    val puzzle: String? = null,
    val encoded: String? = null,
    val shareUrl: String? = null,
    val error: String? = null
)

/**
 * Routes for puzzle sharing functionality.
 * Allows encoding and decoding puzzles via URL parameters.
 */
fun Route.shareRoutes() {
    // Encode puzzle to shareable URL format
    post("/share") {
        val request = try {
            call.receive<ShareRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ShareResponse(
                    success = false,
                    error = "Invalid request body: ${e.message}"
                )
            )
            return@post
        }

        // Validate puzzle
        if (!PuzzleEncoder.isValidPuzzle(request.puzzle)) {
            call.respond(
                HttpStatusCode.BadRequest,
                ShareResponse(
                    success = false,
                    error = "Invalid puzzle format. Must be 81 characters (1-9 for values, . or 0 for empty)"
                )
            )
            return@post
        }

        // Encode puzzle
        val encoded = try {
            PuzzleEncoder.encode(request.puzzle)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ShareResponse(
                    success = false,
                    error = "Failed to encode puzzle: ${e.message}"
                )
            )
            return@post
        }

        // Generate share URL (use the scheme and host from the request)
        val scheme = if (call.request.headers.contains(HttpHeaders.XForwardedProto)) "https" else "http"
        val host = call.request.headers[HttpHeaders.Host]?.firstOrNull() ?: "localhost:8080"
        val shareUrl = "$scheme://$host?p=$encoded"

        call.respond(
            ShareResponse(
                success = true,
                puzzle = request.puzzle,
                encoded = encoded,
                shareUrl = shareUrl
            )
        )
    }

    // Decode puzzle from URL parameter (for verification)
    get("/share") {
        val encoded = call.request.queryParameters["puzzle"]

        if (encoded == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ShareResponse(
                    success = false,
                    error = "Missing 'puzzle' query parameter"
                )
            )
            return@get
        }

        // Decode puzzle
        val decoded = try {
            PuzzleEncoder.decode(encoded)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ShareResponse(
                    success = false,
                    error = "Invalid encoded puzzle: ${e.message}"
                )
            )
            return@get
        }

        call.respond(
            ShareResponse(
                success = true,
                puzzle = decoded,
                encoded = encoded
            )
        )
    }
}
