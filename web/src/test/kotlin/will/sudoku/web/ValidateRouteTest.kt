package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for POST /api/v1/validate
 *
 * Refs #479
 */
class ValidateRouteTest {

    @Test
    fun `validate valid puzzle returns valid true`() = ktorTest {
        val response = validatePost(SAMPLE_PUZZLE)
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<ValidateResponse>(body)
        assertTrue(json.valid, "Should be valid")
        assertTrue(json.uniqueSolution ?: false, "Should have unique solution")
        assertNull(json.error, "Should have no error")
    }

    @Test
    fun `validate wrong-length puzzle returns 400 error`() = ktorTest {
        val response = validatePost("12345")
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<ValidateResponse>(body)
        assertFalse(json.valid, "Should not be valid")
        assertNotNull(json.error, "Should have error")
        assertTrue(json.error!!.contains("81"), "Error should mention 81 chars")
    }

    @Test
    fun `validate invalid puzzle returns errors`() = ktorTest {
        val response = validatePost(INVALID_PUZZLE)
        // Invalid puzzle (duplicate 5s in row) returns error
        val body = response.bodyAsText()
        val json = testJson.decodeFromString<ValidateResponse>(body)
        assertFalse(json.valid, "Should not be valid with duplicates")
    }

    @Test
    fun `validate with checkUniqueness false skips solution check`() = ktorTest {
        val response = client.post("/api/v1/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"puzzle":"$SAMPLE_PUZZLE","checkUniqueness":false}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<ValidateResponse>(body)
        assertTrue(json.valid, "Should be valid")
        assertNull(json.uniqueSolution, "Should not check uniqueness when disabled")
        assertNull(json.solutionCount, "Should not report solution count when disabled")
    }
}
