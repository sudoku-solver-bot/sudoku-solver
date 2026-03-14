package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for Board class.
 *
 * Tests board creation, candidate management, and state checking.
 */
class BoardTest {

    @Test
    fun `creates board from values array`() {
        val values = IntArray(81) { 0 }
        values[0] = 5  // First cell = 5

        val board = Board(values)

        assertThat(board.value(Coord(0, 0))).isEqualTo(5)
        assertThat(board.isConfirmed(Coord(0, 0))).isTrue()
    }

    @Test
    fun `empty cell has all candidates`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val candidates = board.candidateValues(Coord(0, 0))

        // Empty cell should have all values 1-9 as candidates
        assertThat(candidates).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9)
    }

    @Test
    fun `confirmed cell has single value`() {
        val values = IntArray(81) { 0 }
        values[0] = 7

        val board = Board(values)

        assertThat(board.isConfirmed(Coord(0, 0))).isTrue()
        assertThat(board.value(Coord(0, 0))).isEqualTo(7)
        assertThat(board.candidateValues(Coord(0, 0))).containsExactly(7)
    }

    @Test
    fun `mark value updates candidates`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        board.markValue(Coord(4, 4), 5)

        assertThat(board.value(Coord(4, 4))).isEqualTo(5)
        assertThat(board.isConfirmed(Coord(4, 4))).isTrue()
    }

    @Test
    fun `isSolved returns true for solved board`() {
        val values = intArrayOf(
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 9
        )

        val board = Board(values)

        assertThat(board.isSolved()).isTrue()
    }

    @Test
    fun `isSolved returns false for unsolved board`() {
        val values = IntArray(81) { 0 }  // Empty board

        val board = Board(values)

        assertThat(board.isSolved()).isFalse()
    }

    @Test
    fun `isValid returns true for valid board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[1] = 2

        val board = Board(values)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `isValid returns false for invalid board with duplicate in row`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[1] = 1  // Duplicate in same row

        val board = Board(values)

        assertThat(board.isValid()).isFalse()
    }

    @Test
    fun `copy creates independent board`() {
        val values = IntArray(81) { 0 }
        val original = Board(values)

        val copy = original.copy()
        copy.markValue(Coord(0, 0), 5)

        // Original should not be affected
        assertThat(original.value(Coord(0, 0))).isEqualTo(0)
        assertThat(copy.value(Coord(0, 0))).isEqualTo(5)
    }

    @Test
    fun `equals returns true for identical boards`() {
        val values = IntArray(81) { 0 }
        values[0] = 5

        val board1 = Board(values)
        val board2 = Board(values)

        assertThat(board1).isEqualTo(board2)
    }

    @Test
    fun `equals returns false for different boards`() {
        val values1 = IntArray(81) { 0 }
        values1[0] = 5

        val values2 = IntArray(81) { 0 }
        values2[0] = 6

        val board1 = Board(values1)
        val board2 = Board(values2)

        assertThat(board1).isNotEqualTo(board2)
    }

    @Test
    fun `unresolvedCoord returns first unresolved cell`() {
        val values = IntArray(81) { 0 }
        // Fill first cell
        values[0] = 1

        val board = Board(values)
        val unresolved = board.unresolvedCoord()

        // Should return the first empty cell (index 1)
        assertThat(unresolved).isNotNull
        assertThat(unresolved!!.row).isEqualTo(0)
        assertThat(unresolved.col).isEqualTo(1)
    }

    @Test
    fun `unresolvedCoord returns null for solved board`() {
        val values = intArrayOf(
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 9
        )

        val board = Board(values)
        val unresolved = board.unresolvedCoord()

        assertThat(unresolved).isNull()
    }
}
