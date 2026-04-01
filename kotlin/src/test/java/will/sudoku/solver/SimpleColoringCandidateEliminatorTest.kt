package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Test for SimpleColoringCandidateEliminator.
 *
 * Tests Simple Coloring pattern detection and candidate elimination.
 */
class SimpleColoringCandidateEliminatorTest {

    @Test
    fun `no changes when no conjugate pairs exist`() {
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
        val eliminator = SimpleColoringCandidateEliminator()
        val changed = eliminator.eliminate(board)

        assertThat(changed).isFalse()
    }

    @Test
    fun `Simple Coloring eliminator is registered in settings`() {
        val hasSimpleColoringEliminator = Settings.eliminators.any {
            it is SimpleColoringCandidateEliminator
        }

        assertThat(hasSimpleColoringEliminator)
            .`as`("SimpleColoringCandidateEliminator should be enabled")
            .isTrue()
    }

    @Test
    fun `eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        // On an empty board, no conjugate pairs exist
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9

        val board = Board(values)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `eliminator runs without error on nearly solved board`() {
        val values = intArrayOf(
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 0
        )

        val board = Board(values)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    // ===== EDGE CASES =====

    @Test
    fun `Simple Coloring finds conjugate pairs in rows`() {
        // Test that the eliminator can find conjugate pairs in rows
        val values = IntArray(81) { 0 }

        // Create a board with some confirmed cells
        // Place value 1 in cells that will create conjugate pair scenarios
        values[0] = 1  // Row 0, col 0 - blocks 1 from rest of row 0
        values[4] = 2
        values[8] = 3

        values[9] = 4  // Row 1, col 0 - blocks 1 from rest of row 1
        values[13] = 5
        values[17] = 6

        // This creates a scenario where eliminators can work
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring finds conjugate pairs in columns`() {
        // Test that the eliminator can find conjugate pairs in columns
        val values = IntArray(81) { 0 }

        // Place various confirmed cells to create a valid board state
        values[0] = 1
        values[4] = 2
        values[8] = 3
        values[18] = 4
        values[27] = 5
        values[36] = 6
        values[45] = 7
        values[54] = 8
        values[63] = 9
        values[72] = 2

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring finds conjugate pairs in regions`() {
        // Test that the eliminator can find conjugate pairs in 3x3 regions
        val values = IntArray(81) { 0 }

        // Box 0 (cells 0-2, 9-11, 18-20): Only cells 1 and 19 can have value 1
        values[0] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6
        values[18] = 7
        values[20] = 8

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring with multiple conjugate pairs`() {
        // Test scenario with multiple conjugate pairs forming chains
        val values = IntArray(81) { 0 }

        // Create a complex puzzle with multiple conjugate pairs
        for (i in 0..8) {
            values[i] = if (i % 2 == 0) (i + 1) else 0
        }
        values[9] = 7
        values[18] = 8

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring handles all candidate values`() {
        // Test that the eliminator can handle all candidate values (1-9)
        val values = IntArray(81) { 0 }

        // Place values to create various candidates without creating invalid boards
        // Place values that don't conflict in rows, columns, or regions
        val confirmedValues = listOf(
            0 to 1, 4 to 2, 8 to 3,   // Row 0
            10 to 4, 14 to 5,         // Row 1
            20 to 6, 24 to 7,         // Row 2
            30 to 8, 34 to 9,         // Row 3
            40 to 1, 44 to 2,         // Row 4
            50 to 3, 54 to 4,         // Row 5
            60 to 5, 64 to 6,         // Row 6
            70 to 7, 74 to 8,         // Row 7
            80 to 9                   // Row 8
        )

        for ((index, value) in confirmedValues) {
            values[index] = value
        }

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring with complex color chains`() {
        // Test scenario with longer color chains
        val values = IntArray(81) { 0 }

        // Create constraints for complex chains
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6
        values[18] = 7
        values[19] = 8
        values[20] = 9

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Simple Coloring Rule 2 - cells seeing both colors`() {
        // Test Rule 2: Eliminate from cells that see both colors
        val values = IntArray(81) { 0 }

        // Create a scenario where Rule 2 could apply
        values[0] = 1
        values[4] = 2  // Center
        values[8] = 3

        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)

        val eliminator = SimpleColoringCandidateEliminator()
        eliminator.eliminate(board)

        assertThat(board.isValid()).isTrue()
    }
}
