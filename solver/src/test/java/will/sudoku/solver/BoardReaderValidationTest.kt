package will.sudoku.solver

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

/**
 * Tests for BoardReader input validation.
 */
@DisplayName("BoardReader Validation Tests")
class BoardReaderValidationTest {

    @Test
    @DisplayName("Valid board should parse successfully")
    fun testValidBoard() {
        // Simple valid 9x9 board (all empty cells)
        val boardString = "................................................................................."

        val board = BoardReader.readBoard(boardString)
        Assertions.assertEquals(81, board.candidatePatterns.size)
    }

    @Test
    @DisplayName("Empty board should throw exception")
    fun testEmptyBoard() {
        val boardString = ""

        val exception = Assertions.assertThrows(ValidationException::class.java) {
            BoardReader.readBoard(boardString)
        }

        Assertions.assertTrue(exception.message!!.contains("Expected 81 cells"))
        Assertions.assertTrue(exception.message!!.contains("but found"))
    }

    @Test
    @DisplayName("Board with only separators should throw exception")
    fun testOnlySeparators() {
        val boardString = "---!---!---"

        val exception = Assertions.assertThrows(ValidationException::class.java) {
            BoardReader.readBoard(boardString)
        }

        Assertions.assertTrue(exception.message!!.contains("Expected 81 cells"))
        Assertions.assertTrue(exception.message!!.contains("but found"))
    }

    @Test
    @DisplayName("Zero-format puzzle should parse successfully (0 = empty cell)")
    fun testZeroFormatPuzzle() {
        // A valid puzzle using 0 for empty cells instead of .
        val boardString = "530070000600195000098000060800060003400803001700020006060000280000419005000080079"

        val board = BoardReader.readBoard(boardString)
        Assertions.assertEquals(81, board.candidatePatterns.size)

        // Verify specific cells: first cell should be 5 (index 5 in symbols = value 5)
        Assertions.assertEquals(5, board.value(Coord(0, 0)))
        // Position with 0 should be empty
        Assertions.assertEquals(0, board.value(Coord(0, 3)))
    }

    @Test
    @DisplayName("Mixed format puzzle should parse successfully (both 0 and . for empty)")
    fun testMixedFormatPuzzle() {
        val boardString = "5.0070000600195000098000060800060003400803001700020006060000280000419005000080079"

        val board = BoardReader.readBoard(boardString)
        Assertions.assertEquals(81, board.candidatePatterns.size)
        Assertions.assertEquals(5, board.value(Coord(0, 0)))
        Assertions.assertEquals(0, board.value(Coord(0, 1))) // Was '.'
        Assertions.assertEquals(0, board.value(Coord(0, 2))) // Was '0'
    }

    @Test
    @DisplayName("Dot-format puzzle should still parse correctly (backward compat)")
    fun testDotFormatPuzzle() {
        val boardString = "53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79"

        val board = BoardReader.readBoard(boardString)
        Assertions.assertEquals(81, board.candidatePatterns.size)
        Assertions.assertEquals(5, board.value(Coord(0, 0)))
        Assertions.assertEquals(0, board.value(Coord(0, 2))) // Was '.'
    }
}
