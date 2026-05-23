package will.sudoku.web

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for POST /api/v1/solve
 *
 * Tests the solve endpoint end-to-end: valid puzzles, invalid inputs,
 * edge cases.
 *
 * Refs #478
 */
class SolveRouteTest {

    @Test
    fun `solve valid puzzle returns solution`() = ktorTest {
        val response = solvePost(SAMPLE_PUZZLE)
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertTrue(json.solved, "Should be solved")
        assertNotNull(json.solution, "Should have a solution")
        assertEquals(81, json.solution!!.length, "Solution should be 81 chars")
        // Solution should have no dots (all cells filled)
        assertFalse(json.solution!!.contains("."), "Solution should have no empty cells")
        assertNull(json.error, "Should have no error")
    }

    @Test
    fun `solve with wrong-length puzzle returns error`() = ktorTest {
        val response = solvePost("12345")  // Too short
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertFalse(json.solved, "Should not be solved")
        assertNotNull(json.error, "Should have an error message")
        assertTrue(json.error!!.contains("81", ignoreCase = true),
            "Error should mention expected length")
    }

    @Test
    fun `solve with invalid characters returns error`() = ktorTest {
        val response = solvePost("53007000060019500X098000060800060003400803001700020006060000280000419005000080079")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertFalse(json.solved, "Should not be solved")
        assertNotNull(json.error, "Should have an error message")
    }

    @Test
    fun `solve with duplicate in row returns error`() = ktorTest {
        val response = solvePost(INVALID_PUZZLE)
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertFalse(json.solved, "Should not be solved with duplicate values")
        assertNotNull(json.error, "Should have an error message")
    }

    @Test
    fun `solve with all-zeros puzzle returns error`() = ktorTest {
        val response = solvePost(EMPTY_PUZZLE)
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertFalse(json.solved, "Should not be solved with no clues")
        assertNotNull(json.error, "Should have an error message")
    }

    @Test
    fun `solve with too few clues returns error`() = ktorTest {
        // Only 1 clue — below the 17-clue minimum
        val puzzle = "000000000000000000000000000000000000000000000000000000000000000000000001000000000"
        val response = solvePost(puzzle)
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertFalse(json.solved, "Should not be solved with too few clues")
        assertNotNull(json.error, "Should have an error message")
    }

    @Test
    fun `solve with metrics returns metrics in response`() = ktorTest {
        val response = client.post("/api/v1/solve") {
            contentType(ContentType.Application.Json)
            setBody("""{"puzzle":"${SAMPLE_PUZZLE}","includeMetrics":true}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertTrue(json.solved, "Should be solved")
        assertNotNull(json.metrics, "Should include metrics")
        assertTrue(json.metrics!!.solveTimeMs >= 0, "Solve time should be non-negative")
        assertTrue(json.metrics!!.backtrackingCount >= 0, "Backtracking count should be non-negative")
    }

    @Test
    fun `solve solution is valid sudoku`() = ktorTest {
        val response = solvePost(SAMPLE_PUZZLE)
        val body = response.bodyAsText()
        val json = testJson.decodeFromString<SolveResponse>(body)
        assertTrue(json.solved)

        val solution = json.solution!!
        // Verify each row has digits 1-9
        for (row in 0..8) {
            val rowDigits = solution.substring(row * 9, (row + 1) * 9)
                .map { it.digitToInt() }.toSet()
            assertEquals(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), rowDigits,
                "Row ${row + 1} should contain all digits 1-9")
        }
        // Verify each column has digits 1-9
        for (col in 0..8) {
            val colDigits = (0..8).map { solution[it * 9 + col].digitToInt() }.toSet()
            assertEquals(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), colDigits,
                "Column ${col + 1} should contain all digits 1-9")
        }
    }
}
