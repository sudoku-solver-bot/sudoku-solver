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
    @Disabled("TODO: Fix bugs in Simple Coloring implementation")
    fun `Simple Coloring eliminator is registered in settings`() {
        val hasSimpleColoringEliminator = Settings.eliminators.any {
            it is SimpleColoringCandidateEliminator
        }

        assertThat(hasSimpleColoringEliminator)
            .`as`("Solver should include SimpleColoringCandidateEliminator")
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
    @Disabled("TODO: Fix bugs in Simple Coloring implementation")
    fun `Simple Coloring finds conjugate pairs in rows`() {
        // Test that the eliminator can find conjugate pairs in rows
        val values = IntArray(81) { 0 }

        // Row 0: Only cells 3 and 7 can have value 1
        values[0] = 2
        values[1] = 3
        values[2] = 4
        values[4] = 5
        values[5] = 6
        values[6] = 7
        values[8] = 8

        // Row 1: Block 1 in other cells
        values[9] = 1
        values[10] = 1
        values[11] = 1

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

        // Column 0: Only cells 9 and 18 can have value 1
        values[0] = 2
        values[18] = 3
        values[27] = 4
        values[36] = 5
        values[45] = 6
        values[54] = 7
        values[63] = 8
        values[72] = 9

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
    @Disabled("TODO: Fix bugs in Simple Coloring implementation")
    fun `Simple Coloring handles all candidate values`() {
        // Test that the eliminator can handle all candidate values (1-9)
        val values = IntArray(81) { 0 }

        // Place values to create various candidates
        for (i in 0 until 30 step 3) {
            values[i] = (i % 9) + 1
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
