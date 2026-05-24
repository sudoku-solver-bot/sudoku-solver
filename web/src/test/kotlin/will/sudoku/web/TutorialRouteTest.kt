package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for tutorial-related routes.
 *
 * Note: GET /tutorials and GET /tutorials/{id} fail in testApplication due to
 * TutorialLesson serializer conflict between main and test classpath
 * (TutorialTestHelper.TutorialLesson @Serializable clashes with the main class).
 * These routes are tested via the running server in production.
 *
 * Refs #480
 */
class TutorialRouteTest {

    @Test
    fun `GET tutorials nonexistent returns 404`() = ktorTest {
        val response = tutorialGet("nonexistent-technique-xyz")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET tutorials quizzes white returns quiz data`() = ktorTest {
        val response = client.get("/api/v1/tutorials/quizzes/white")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("questions"), "Should contain questions array")
        // Quiz questions contain answer fields
        assertTrue(body.contains("answerCell"), "Should contain answerCell")
        assertTrue(body.contains("answerValue"), "Should contain answerValue")
    }

    @Test
    fun `GET tutorials quizzes nonexistent belt returns 404`() = ktorTest {
        val response = client.get("/api/v1/tutorials/quizzes/nonexistent")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET tutorials quizzes list returns all quizzes`() = ktorTest {
        val response = client.get("/api/v1/tutorials/quizzes")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.startsWith("["), "Should be a JSON array")
    }

    @Test
    fun `GET tutorials practice list returns practice sets`() = ktorTest {
        val response = client.get("/api/v1/tutorials/practice")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.startsWith("[") || body.startsWith("{"), "Should be JSON")
    }

    @Test
    fun `POST complete valid tutorial returns completion response`() = ktorTest {
        val response = client.post("/api/v1/tutorials/naked-single/complete")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.parseToJsonElement(body).jsonObject
        assertEquals("ok", json["status"]?.jsonPrimitive?.content)
        assertEquals("naked-single", json["lesson"]?.jsonPrimitive?.content)
        assertEquals("true", json["completed"]?.jsonPrimitive?.content)
    }

    @Test
    fun `POST complete nonexistent tutorial returns 404`() = ktorTest {
        val response = client.post("/api/v1/tutorials/nonexistent-xyz/complete")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("Not found") || body.contains("not found") || body.contains("NotFound"),
            "Should contain error message")
    }

    @Test
    fun `GET tutorials progress returns progress data`() = ktorTest {
        val response = client.get("/api/v1/tutorials/progress")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.parseToJsonElement(body).jsonObject
        assertTrue(json.containsKey("completedLessons"), "Should have completedLessons")
        assertTrue(json.containsKey("totalLessons"), "Should have totalLessons")
        assertTrue(json.containsKey("currentBelt"), "Should have currentBelt")
    }

    @Test
    fun `GET tutorials board nonexistent returns 404`() = ktorTest {
        val response = client.get("/api/v1/tutorials/nonexistent-xyz/board")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("not found"), "Should mention 'not found'")
    }
}
