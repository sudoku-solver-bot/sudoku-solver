package will.sudoku.solver

import org.junit.jupiter.api.Test
import java.io.File

class SolverActivityLogger {
    @Test
    fun `log all eliminator activity on Easter Monster`() {
        val puzzle = "100000002090400050006000700050903000000040000000850090900000800040002030007010006"
        val board = BoardReader.readBoard(puzzle)
        val elim = SimpleCandidateEliminator()
        elim.eliminate(board)
        HintGenerator.applyHiddenSinglesUntilStable(board)

        val lines = mutableListOf("=== Solver Eliminator Activity Log ===")
        lines.add("Starting unsolved: ${Coord.all.count { !board.isConfirmed(it) }}")

        val config = SolverConfig()
        var iteration = 0
        var anyProgress = true

        while (anyProgress && iteration < 100) {
            anyProgress = false
            iteration++
            var elimProgress = true

            // Phase 1: run eliminators
            while (elimProgress) {
                elimProgress = false
                for (ei in config.eliminators) {
                    val before = board.countTotalCandidates()
                    val changed = ei.eliminate(board)
                    val after = board.countTotalCandidates()
                    if (changed && after < before) {
                        elimProgress = true
                        anyProgress = true
                        lines.add("  iter=$iteration ${ei.javaClass.simpleName}: candidates ${before}→${after}")
                    }
                }
            }

            // Phase 2: fill naked singles
            var filled = false
            for (coord in Coord.all) {
                if (!board.isConfirmed(coord) && board.candidatePattern(coord).countOneBits() == 1) {
                    val value = board.candidateValues(coord).first()
                    board.markValue(coord, value)
                    filled = true
                }
            }
            if (filled) anyProgress = true
            lines.add("  iter=$iteration: after fill, unsolved=${Coord.all.count { !board.isConfirmed(it) }}")
        }

        File("/tmp/solver-log.txt").writeText(lines.joinToString("\n"))
    }
}
