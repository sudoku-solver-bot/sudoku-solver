package will.sudoku.web

import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for POST /api/v1/hint
 *
 * Tests hint generation and puzzle validation.
 *
 * Refs #611
 */
class HintRouteTest {

    @Test
    fun `hint with valid puzzle returns 200`() = ktorTest {
        val response = hintPost(SAMPLE_PUZZLE)
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `hint with duplicate in row returns 400`() = ktorTest {
        val puzzle = "110000000000000000000000000000000000000000000000000000000000000000000000000000000"
        val response = hintPost(puzzle)
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("Duplicate value 1"), "Should mention duplicate value")
        assertTrue(body.contains("row 1"), "Should mention the row")
    }

    @Test
    fun `hint with duplicate in column returns 400`() = ktorTest {
        val puzzle = "500000000500000000000000000000000000000000000000000000000000000000000000000000000"
        val response = hintPost(puzzle)
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("Duplicate value 5"), "Should mention duplicate value 5")
        assertTrue(body.contains("column 1"), "Should mention the column")
    }

    @Test
    fun `hint with duplicate in box returns 400`() = ktorTest {
        // Two 3's in box (1,1): positions (0,0) and (1,1)
        val puzzle = "300000000030000000000000000000000000000000000000000000000000000000000000000000000"
        val response = hintPost(puzzle)
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("Duplicate value 3"), "Should mention duplicate value 3")
        assertTrue(body.contains("region"), "Should mention region/box")
    }
}
