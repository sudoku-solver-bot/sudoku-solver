package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for SolverMetrics and SolverWithMetrics.
 *
 * Tests that metrics data structures work correctly.
 */
class SolverMetricsTest {

    @Test
    fun `solves already solved puzzle with metrics`() {
        // Create a solved puzzle
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
        val solver = SolverWithMetrics()
        val result = solver.solveWithMetrics(board)

        // Solution should be the same board
        assertThat(result.solvedBoard).isEqualTo(board)

        // Metrics should be minimal (no work needed)
        assertThat(result.metrics.backtrackingCount).isEqualTo(0)
        assertThat(result.metrics.maxRecursionDepth).isEqualTo(0)
    }

    @Test
    fun `metrics for unsolvable puzzle returns null`() {
        // Create an invalid/unsolvable puzzle
        val values = IntArray(81) { 0 }
        // Create conflict: two 1s in same row
        values[0] = 1
        values[1] = 1

        val board = Board(values)
        val solver = SolverWithMetrics()
        val result = solver.solveWithMetrics(board)

        // No solution should be found
        assertThat(result.solvedBoard).isNull()
    }

    @Test
    fun `eliminator metrics data class works correctly`() {
        val metrics = EliminatorMetrics(
            eliminations = 5,
            passes = 3,
            totalTimeNanos = 1000
        )

        assertThat(metrics.eliminations).isEqualTo(5)
        assertThat(metrics.passes).isEqualTo(3)
        assertThat(metrics.totalTimeNanos).isEqualTo(1000)
    }

    @Test
    fun `solver metrics data class works correctly`() {
        val eliminatorMetrics = mapOf(
            "SimpleEliminator" to EliminatorMetrics(10, 5, 5000)
        )

        val metrics = SolverMetrics(
            totalSolveTimeNanos = 1000000,
            backtrackingCount = 2,
            maxRecursionDepth = 5,
            propagationPasses = 10,
            cellsProcessed = 30,
            eliminatorMetrics = eliminatorMetrics
        )

        assertThat(metrics.totalSolveTimeNanos).isEqualTo(1000000)
        assertThat(metrics.backtrackingCount).isEqualTo(2)
        assertThat(metrics.maxRecursionDepth).isEqualTo(5)
        assertThat(metrics.propagationPasses).isEqualTo(10)
        assertThat(metrics.cellsProcessed).isEqualTo(30)
        assertThat(metrics.eliminatorMetrics).hasSize(1)
    }

    @Test
    fun `solve result data class works correctly`() {
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
        val metrics = SolverMetrics(totalSolveTimeNanos = 1000)
        val result = SolveResult(board, metrics)

        assertThat(result.solvedBoard).isEqualTo(board)
        assertThat(result.metrics.totalSolveTimeNanos).isEqualTo(1000)
    }

    @Test
    fun `metrics toString contains expected fields`() {
        val metrics = SolverMetrics(
            totalSolveTimeNanos = 1000000,
            backtrackingCount = 2,
            maxRecursionDepth = 5,
            propagationPasses = 10,
            cellsProcessed = 30,
            eliminatorMetrics = emptyMap()
        )

        val metricsString = metrics.toString()

        assertThat(metricsString).contains("Solver Metrics")
        assertThat(metricsString).contains("Total Solve Time")
        assertThat(metricsString).contains("Backtracking")
        assertThat(metricsString).contains("Recursion")
        assertThat(metricsString).contains("Propagation")
    }
}
