package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

/**
 * Shared test infrastructure for Ktor integration tests.
 *
 * Provides:
 * - Application setup via [ktorTest] (configures all routes)
 * - JSON parsing utilities
 * - Common request builders for solve, validate, step-by-step, tutorials
 *
 * Usage:
 * ```kotlin
 * @Test
 * fun `my test`() = ktorTest {
 *     val response = solvePost("000000000...")
 *     assertEquals(HttpStatusCode.OK, response.status)
 * }
 * ```
 */

/**
 * Lenient JSON parser for reading response bodies in tests.
 */
val testJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

// ── Puzzle Constants ─────────────────────────────────────

/**
 * A simple, valid puzzle string for quick tests.
 * Known solution exists.
 */
const val SAMPLE_PUZZLE = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"

/**
 * An invalid puzzle string (duplicate 5 in first row).
 */
const val INVALID_PUZZLE = "550000000000000000000000000000000000000000000000000000000000000000000000000000000"

/**
 * An empty puzzle (all zeros) — valid but under-constrained.
 */
const val EMPTY_PUZZLE = "000000000000000000000000000000000000000000000000000000000000000000000000000000000"

// ── Request Builders (extension functions on ApplicationTestBuilder) ─

/**
 * POST a puzzle to /api/v1/solve and return the raw response.
 */
suspend fun ApplicationTestBuilder.solvePost(puzzle: String): HttpResponse {
    return client.post("/api/v1/solve") {
        contentType(ContentType.Application.Json)
        setBody("""{"puzzle":"$puzzle"}""")
    }
}

/**
 * POST a puzzle to /api/v1/validate and return the raw response.
 */
suspend fun ApplicationTestBuilder.validatePost(puzzle: String): HttpResponse {
    return client.post("/api/v1/validate") {
        contentType(ContentType.Application.Json)
        setBody("""{"puzzle":"$puzzle"}""")
    }
}

/**
 * POST a puzzle to /api/v1/step-by-step and return the raw response.
 */
suspend fun ApplicationTestBuilder.stepByStepPost(puzzle: String): HttpResponse {
    return client.post("/api/v1/step-by-step") {
        contentType(ContentType.Application.Json)
        setBody("""{"puzzle":"$puzzle"}""")
    }
}

/**
 * POST a puzzle to /api/v1/hint and return the raw response.
 */
suspend fun ApplicationTestBuilder.hintPost(puzzle: String): HttpResponse {
    return client.post("/api/v1/hint") {
        contentType(ContentType.Application.Json)
        setBody("""{"puzzle":"$puzzle"}""")
    }
}

/**
 * GET /api/v1/tutorials and return the raw response.
 */
suspend fun ApplicationTestBuilder.tutorialsGet(): HttpResponse {
    return client.get("/api/v1/tutorials")
}

/**
 * GET /api/v1/tutorials/{id} and return the raw response.
 */
suspend fun ApplicationTestBuilder.tutorialGet(id: String): HttpResponse {
    return client.get("/api/v1/tutorials/$id")
}

// ── Application Setup ────────────────────────────────────

/**
 * Run a test with the full application configured.
 * Shorthand for testApplication + module().
 */
fun ktorTest(test: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    application {
        module()
    }
    test()
}
