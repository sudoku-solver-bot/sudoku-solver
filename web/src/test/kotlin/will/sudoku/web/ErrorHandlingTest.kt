package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Cross-cutting error handling tests.
 *
 * Verifies that ALL routes handle edge cases gracefully — no 500 errors
 * from bad input, malformed JSON, or missing parameters.
 *
 * Refs #510
 */
class ErrorHandlingTest {

    @Test
    fun `POST solve with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/solve") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/solve")
    }

    @Test
    fun `POST solve with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/solve") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/solve")
    }

    @Test
    fun `POST hint with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/hint") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/hint")
    }

    @Test
    fun `POST hint with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/hint") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/hint")
    }

    @Test
    fun `POST validate with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/validate") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/validate")
    }

    @Test
    fun `POST validate with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/validate") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/validate")
    }

    @Test
    fun `POST generate with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/generate") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/generate")
    }

    @Test
    fun `POST generate with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/generate") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/generate")
    }

    @Test
    fun `POST step-by-step with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/step-by-step") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/step-by-step")
    }

    @Test
    fun `POST step-by-step with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/step-by-step") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/step-by-step")
    }

    @Test
    fun `POST candidates with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/candidates") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/candidates")
    }

    @Test
    fun `POST candidates with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/candidates") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/candidates")
    }

    @Test
    fun `POST solve-steps with empty body does not return 500`() = ktorTest {
        val response = client.post("/api/v1/solve/steps") {
            contentType(ContentType.Application.Json)
            setBody("")
        }
        assertNot500(response, "/api/v1/solve/steps")
    }

    @Test
    fun `POST solve-steps with malformed JSON does not return 500`() = ktorTest {
        val response = client.post("/api/v1/solve/steps") {
            contentType(ContentType.Application.Json)
            setBody("{bad json")
        }
        assertNot500(response, "/api/v1/solve/steps")
    }

    @Test
    fun `GET generate difficulty with invalid level does not return 500`() = ktorTest {
        val response = client.get("/api/v1/generate/difficulty/impossible")
        assertNot500(response, "/api/v1/generate/difficulty/{level}")
    }

    @Test
    fun `missing content-type on POST solve does not return 500`() = ktorTest {
        val response = client.post("/api/v1/solve") {
            setBody("{}")
        }
        assertNot500(response, "/api/v1/solve (no content-type)")
    }

    // ── Helpers ────────────────────────────────────────────

    private suspend fun assertNot500(response: HttpResponse, route: String) {
        val body = response.bodyAsText()
        assertNotEquals(
            HttpStatusCode.InternalServerError,
            response.status,
            "$route should not return 500; got: ${response.status} body: ${body.take(200)}"
        )
    }
}
