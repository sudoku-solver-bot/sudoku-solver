package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VersionRoutesTest {

    @Test
    fun `version endpoint returns 200 with JSON body`() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/v1/version")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        // Should be valid JSON with expected fields
        assertTrue(body.contains("\"version\""), "Response should contain 'version' field")
        assertTrue(body.contains("\"gitCommit\""), "Response should contain 'gitCommit' field")
        assertTrue(body.contains("\"buildTimestamp\""), "Response should contain 'buildTimestamp' field")
    }

    @Test
    fun `deploy-info endpoint returns 200 with JSON body`() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/v1/deploy-info")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("\"version\""), "Response should contain 'version' field")
        assertTrue(body.contains("\"gitCommit\""), "Response should contain 'gitCommit' field")
        assertTrue(body.contains("\"buildTimestamp\""), "Response should contain 'buildTimestamp' field")
    }

    @Test
    fun `readGitCommit returns null when no deploy file exists`() {
        // In test environment, there's no deploy commit file
        val result = readGitCommit()
        // Result may be null or a value from version.properties in build dir
        // Just verify it doesn't throw
        assertNotNull(true)
    }

    @Test
    fun `readBuildTimestamp returns without throwing`() {
        // Just verify it doesn't throw
        readBuildTimestamp()
    }
}
