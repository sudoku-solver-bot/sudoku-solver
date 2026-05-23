package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for SimpleCandidateEliminator.
 *
 * Tests that confirmed values are correctly removed from peer cells.
 */
class SimpleCandidateEliminatorTest {

    @Test
    fun `eliminates confirmed value from row peers`() {
        // Create board with value 1 at (0, 0)
        val values = IntArray(81) { 0 }
        values[0] = 1  // Row 0, Col 0 = 1

        val board = Board(values)
        val eliminator = SimpleCandidateEliminator()
        val changed = eliminator.eliminate(board)

        // Should have eliminated value 1 from row 0
        assertThat(changed).isTrue()

        // Verify value 1 is eliminated from row 0 peers
        for (col in 1..8) {
            val coord = Coord(0, col)
            assertThat(board.candidateValues(coord))
                .`as`("Cell (0,$col) should not have value 1 as candidate")
                .doesNotContain(1)
        }
    }

    @Test
    fun `eliminates confirmed value from column peers`() {
        // Create board with value 5 at (4, 4)
        val values = IntArray(81) { 0 }
        values[4 * 9 + 4] = 5  // Row 4, Col 4 = 5

        val board = Board(values)
        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        // Verify value 5 is eliminated from column 4 peers
        for (row in 0..8) {
            if (row != 4) {
                val coord = Coord(row, 4)
                assertThat(board.candidateValues(coord))
                    .`as`("Cell ($row,4) should not have value 5 as candidate")
                    .doesNotContain(5)
            }
        }
    }

    @Test
    fun `eliminates confirmed value from region peers`() {
        // Create board with value 9 at (1, 1) - top-left region
        val values = IntArray(81) { 0 }
        values[1 * 9 + 1] = 9  // Row 1, Col 1 = 9

        val board = Board(values)
        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        // Verify value 9 is eliminated from top-left region peers (rows 0-2, cols 0-2)
        for (row in 0..2) {
            for (col in 0..2) {
                if (row != 1 || col != 1) {
                    val coord = Coord(row, col)
                    assertThat(board.candidateValues(coord))
                        .`as`("Cell ($row,$col) in region should not have value 9 as candidate")
                        .doesNotContain(9)
                }
            }
        }
    }

    @Test
    fun `returns false when no eliminations possible`() {
        // Create a fully solved board
        val values = intArrayOf(
            5, 3, 4, 6, 7, 8, 9, 1, 2,
            6, 7, 2, 1, 9, 5, 3, 4, 8,
            1, 9, 8, 3, 4, 2, 5, 6, 7,
            8, 5, 9, 7, 6, 1, 4, 2, 3,
            4, 2, 6, 8, 5, 3, 7, 9, 1,
            7, 1, 3, 9, 2, 4, 8, 5, 6,
            9, 6, 1, 5, 3, 7, 2, 8, 4,
            2, 8, 7, 4, 1, 9, 6, 3, 5,
            3, 4, 5, 2, 8, 6, 1, 7, 9
        )

        val board = Board(values)
        val eliminator = SimpleCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `eliminates multiple values from same peer`() {
        // Create board with values 1, 2, 3 in same row
        val values = IntArray(81) { 0 }
        values[0 * 9 + 0] = 1  // (0, 0) = 1
        values[0 * 9 + 1] = 2  // (0, 1) = 2
        values[0 * 9 + 2] = 3  // (0, 2) = 3

        val board = Board(values)
        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        // Cell (0, 3) should not have 1, 2, or 3 as candidates
        val candidates = board.candidateValues(Coord(0, 3))
        assertThat(candidates)
            .`as`("Cell (0,3) should not have values 1, 2, or 3")
            .doesNotContain(1, 2, 3)
    }
}
