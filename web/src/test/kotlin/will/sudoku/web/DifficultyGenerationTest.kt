package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

/**
 * Difficulty-based puzzle generation tests.
 *
 * Generates puzzles at each difficulty level and validates:
 * - Puzzle is exactly 81 characters
 * - Puzzle is solvable via POST /api/v1/solve
 * - Difficulty field matches requested level
 * - targetAge is present and non-empty
 *
 * Refs #699
 */
@Timeout(value = 15, unit = TimeUnit.SECONDS)
class DifficultyGenerationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun JsonObject.str(key: String): String? =
        this[key]?.jsonPrimitive?.content

    @Test
    fun `GET generate difficulty EASY returns valid puzzle`() = ktorTest {
        val response = client.get("/api/v1/generate/difficulty/EASY")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        val puzzle = body.str("puzzle")
        assertNotNull(puzzle)
        assertEquals(81, puzzle!!.length)
        assertEquals("Easy", body.str("difficulty"))
        assertNotNull(body.str("targetAge"))
        assertTrue(body.str("targetAge")!!.isNotEmpty())
    }

    @Test
    fun `GET generate difficulty MEDIUM returns valid puzzle`() = ktorTest {
        val response = client.get("/api/v1/generate/difficulty/MEDIUM")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        val puzzle = body.str("puzzle")
        assertNotNull(puzzle)
        assertEquals(81, puzzle!!.length)
        assertEquals("Medium", body.str("difficulty"))
        assertNotNull(body.str("targetAge"))
    }

    @Test
    fun `GET generate difficulty HARD returns valid puzzle`() = ktorTest {
        val response = client.get("/api/v1/generate/difficulty/HARD")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()) as JsonObject
        val puzzle = body.str("puzzle")
        assertNotNull(puzzle)
        assertEquals(81, puzzle!!.length)
        assertEquals("Hard", body.str("difficulty"))
        assertNotNull(body.str("targetAge"))
    }

    @Test
    fun `EASY generated puzzle is solvable`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/EASY")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val solveResponse = solvePost(puzzle)
        val solveBody = json.parseToJsonElement(solveResponse.bodyAsText()) as JsonObject
        assertEquals(true, solveBody.str("solved")?.toBooleanStrictOrNull())
        assertNotNull(solveBody.str("solution"))
        assertEquals(81, solveBody.str("solution")!!.length)
    }

    @Test
    fun `MEDIUM generated puzzle is solvable`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/MEDIUM")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val solveResponse = solvePost(puzzle)
        val solveBody = json.parseToJsonElement(solveResponse.bodyAsText()) as JsonObject
        assertEquals(true, solveBody.str("solved")?.toBooleanStrictOrNull())
    }

    @Test
    fun `HARD generated puzzle is solvable`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/HARD")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val solveResponse = solvePost(puzzle)
        val solveBody = json.parseToJsonElement(solveResponse.bodyAsText()) as JsonObject
        assertEquals(true, solveBody.str("solved")?.toBooleanStrictOrNull())
    }

    @Test
    fun `EASY generated puzzle has unique solution`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/EASY")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val validateResponse = validatePost(puzzle)
        val validateBody = json.parseToJsonElement(validateResponse.bodyAsText()) as JsonObject
        assertEquals(true, validateBody.str("valid")?.toBooleanStrictOrNull())
    }

    @Test
    fun `MEDIUM generated puzzle has unique solution`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/MEDIUM")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val validateResponse = validatePost(puzzle)
        val validateBody = json.parseToJsonElement(validateResponse.bodyAsText()) as JsonObject
        assertEquals(true, validateBody.str("valid")?.toBooleanStrictOrNull())
    }

    @Test
    fun `HARD generated puzzle has unique solution`() = ktorTest {
        val genResponse = client.get("/api/v1/generate/difficulty/HARD")
        val genBody = json.parseToJsonElement(genResponse.bodyAsText()) as JsonObject
        val puzzle = genBody.str("puzzle")!!

        val validateResponse = validatePost(puzzle)
        val validateBody = json.parseToJsonElement(validateResponse.bodyAsText()) as JsonObject
        assertEquals(true, validateBody.str("valid")?.toBooleanStrictOrNull())
    }
}
