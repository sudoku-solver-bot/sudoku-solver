package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for FishCandidateEliminator (X-Wing and Swordfish).
 *
 * FishCandidateEliminator is parameterized by fish size:
 * - size 2 = X-Wing
 * - size 3 = Swordfish
 */
class FishCandidateEliminatorTest {

    @Test
    fun `X-Wing eliminator is registered in default config`() {
        val hasXWing = SolverConfig.defaultEliminators().any {
            it is FishCandidateEliminator && it.displayName == "XWing"
        }
        assertThat(hasXWing).`as`("Solver should include FishCandidateEliminator(2)").isTrue()
    }

    @Test
    fun `Swordfish eliminator is registered in default config`() {
        val hasSwordfish = SolverConfig.defaultEliminators().any {
            it is FishCandidateEliminator && it.displayName == "Swordfish"
        }
        assertThat(hasSwordfish).`as`("Solver should include FishCandidateEliminator(3)").isTrue()
    }

    @Test
    fun `no changes when no X-Wing pattern exists`() {
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
        val eliminator = FishCandidateEliminator(2)
        val changed = eliminator.eliminate(board)
        assertThat(changed).isFalse()
    }

    @Test
    fun `no changes when no Swordfish pattern exists`() {
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
        val eliminator = FishCandidateEliminator(3)
        val changed = eliminator.eliminate(board)
        assertThat(changed).isFalse()
    }

    @Test
    fun `X-Wing eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)
        val eliminator = FishCandidateEliminator(2)
        val changed = eliminator.eliminate(board)
        assertThat(changed).isFalse()
    }

    @Test
    fun `Swordfish eliminator runs without error on empty board`() {
        val values = IntArray(81) { 0 }
        val board = Board(values)
        val eliminator = FishCandidateEliminator(3)
        val changed = eliminator.eliminate(board)
        assertThat(changed).isFalse()
    }

    @Test
    fun `X-Wing eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9
        val board = Board(values)
        val eliminator = FishCandidateEliminator(2)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish eliminator runs without error on partial board`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[40] = 5
        values[80] = 9
        val board = Board(values)
        val eliminator = FishCandidateEliminator(3)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `solver with FishCandidateEliminator solves AI Escargot`() {
        // AI Escargot — famous hard puzzle requiring advanced techniques
        val puzzle = "100007090030020008009600500005300900010080002600004000300000010040000007007000300"
        val board = BoardReader.readBoard(puzzle)
        val solver = Solver()
        val solution = solver.solve(board)

        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }

    @Test
    fun `solver with FishCandidateEliminator solves Easter Monster`() {
        // Easter Monster — requires multiple advanced techniques
        val puzzle = "100000002090400050006000700050903000000040000000850090900000800040002030007010006"
        val board = BoardReader.readBoard(puzzle)
        val solver = Solver()
        val solution = solver.solve(board)

        assertThat(solution).isNotNull
        assertThat(solution!!.isSolved()).isTrue()
    }

    @Test
    fun `X-Wing eliminator handles various puzzle states`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[4] = 2
        values[8] = 3
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        val eliminator = FishCandidateEliminator(2)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish eliminator handles various puzzle states`() {
        val values = IntArray(81) { 0 }
        values[0] = 1
        values[4] = 2
        values[8] = 3
        val board = Board(values)
        SimpleCandidateEliminator().eliminate(board)
        val eliminator = FishCandidateEliminator(3)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `X-Wing eliminator with nearly solved board`() {
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
        val eliminator = FishCandidateEliminator(2)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `Swordfish eliminator with nearly solved board`() {
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
        val eliminator = FishCandidateEliminator(3)
        eliminator.eliminate(board)
        assertThat(board.isValid()).isTrue()
    }

    @Test
    fun `invalid fish size throws`() {
        try {
            FishCandidateEliminator(4)
            assertThat(false).`as`("Should have thrown IllegalArgumentException").isTrue()
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Fish size must be 2")
        }
    }
}
