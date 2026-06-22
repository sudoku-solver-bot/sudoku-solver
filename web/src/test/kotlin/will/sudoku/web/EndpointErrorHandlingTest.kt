package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Endpoint-specific error handling tests.
 *
 * Verifies that API endpoints return the correct response body shape
 * (solved: false, valid: false, error messages) for invalid input —
 * not just that they avoid 500 errors (which is covered by ErrorHandlingTest).
 *
 * Refs #701
 */
class EndpointErrorHandlingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun JsonObject.bool(key: String): Boolean? =
        this[key]?.jsonPrimitive?.content?.toBooleanStrictOrNull()

    private fun JsonObject.str(key: String): String? =
        this[key]?.jsonPrimitive?.content

    // ── POST /api/v1/solve ──────────────────────────────────

    @Test
    fun `POST solve with short puzzle returns solved false with error`() = ktorTest {
        val response = client.post("/api/v1/solve") {
            contentType(ContentType.Application.Json)
            setBody("""{"puzzle":"12345"}""")
        }

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        assertEquals(false, body.bool("solved"))
        assertNotNull(body.str("error"))
    }

    @Test
    fun `POST solve with empty puzzle returns solved false with error`() = ktorTest {
        val response = solvePost(EMPTY_PUZZLE)

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        assertEquals(false, body.bool("solved"))
        assertNotNull(body.str("error"))
    }

    @Test
    fun `POST solve with puzzle containing invalid characters returns solved false`() = ktorTest {
        val badPuzzle = "X".repeat(81)
        val response = client.post("/api/v1/solve") {
            contentType(ContentType.Application.Json)
            setBody("""{"puzzle":"$badPuzzle"}""")
        }

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        assertEquals(false, body.bool("solved"))
        assertNotNull(body.str("error"))
    }

    // ── POST /api/v1/validate ───────────────────────────────

    @Test
    fun `POST validate with short puzzle returns valid false with error`() = ktorTest {
        val response = client.post("/api/v1/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"puzzle":"abc"}""")
        }

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        assertEquals(false, body.bool("valid"))
        assertNotNull(body.str("error"))
    }

    @Test
    fun `POST validate with structurally invalid puzzle returns valid false`() = ktorTest {
        val response = validatePost(INVALID_PUZZLE)

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        val valid = body.bool("valid")
        assertFalse(valid == true)
    }

    // ── GET /api/v1/generate/difficulty/{level} ─────────────

    @Test
    fun `GET generate difficulty with IMPOSSIBLE level returns 400 Bad Request`() = ktorTest {
        val response = client.get("/api/v1/generate/difficulty/IMPOSSIBLE")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
