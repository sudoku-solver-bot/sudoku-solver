package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Reproduction test for bug #229: /api/v1/candidates returns incomplete candidate sets.
 */
class CandidateReproductionTest {

    @Test
    fun `candidates should include all originally empty cells not just those with exactly 2 candidates`() {
        val puzzleBoard = PuzzleGenerator.generate(DifficultyRater.Level.EASY, 42L)
        val puzzleString = boardToPuzzleString(puzzleBoard)
        println("Empty cells in puzzle: ${puzzleString.count { it == '.' }}")

        val board = BoardReader.readBoard(puzzleString)

        // Record originally empty cells (matching the fix in CandidateRoutes)
        val originallyEmpty = Coord.all.filter { !board.isConfirmed(it) }.toSet()
        println("Originally empty cells: ${originallyEmpty.size}")

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        // Collect candidates for all originally empty cells
        val candidates = mutableMapOf<String, List<Int>>()
        for (coord in originallyEmpty) {
            val values = board.candidateValues(coord).toList()
            if (values.isNotEmpty()) {
                candidates[coord.index.toString()] = values
            }
        }

        println("Candidates entries: ${candidates.size}")

        val bySize = candidates.values.groupBy { it.size }.mapValues { it.value.size }
        println("By candidate count: $bySize")

        // Should include all originally empty cells
        assertThat(candidates.size)
            .`as`("Should return candidates for all originally empty cells")
            .isEqualTo(originallyEmpty.size)

        // Should have cells with different numbers of candidates
        val hasVariety = candidates.values.map { it.size }.toSet().size >= 2
        assertThat(hasVariety)
            .`as`("Should have cells with different numbers of candidates (1, 2, 3+)")
            .isTrue()
    }

    @Test
    fun `candidates response should include cells with varied candidate counts`() {
        val puzzle = "53..7...." +
                     "6..195..." +
                     ".98....6." +
                     "8...6...3" +
                     "4..8.3..1" +
                     "7...2...6" +
                     ".6....28." +
                     "...419..5" +
                     "....8..79"

        val board = BoardReader.readBoard(puzzle)
        val originallyEmpty = Coord.all.filter { !board.isConfirmed(it) }.toSet()

        val eliminator = SimpleCandidateEliminator()
        eliminator.eliminate(board)

        var cellsWith1Candidate = 0
        var cellsWith2Candidates = 0
        var cellsWith3Plus = 0

        for (coord in originallyEmpty) {
            val values = board.candidateValues(coord).toList()
            when {
                values.size == 1 -> cellsWith1Candidate++
                values.size == 2 -> cellsWith2Candidates++
                values.size >= 3 -> cellsWith3Plus++
            }
        }

        println("Cells with 1 candidate: $cellsWith1Candidate")
        println("Cells with 2 candidates: $cellsWith2Candidates")
        println("Cells with 3+ candidates: $cellsWith3Plus")

        // Easy puzzle after SCE should have some cells with 1 candidate (solved by elimination)
        assertThat(cellsWith1Candidate)
            .`as`("Should include cells solved to 1 candidate by elimination")
            .isGreaterThan(0)

        // Total should match originally empty count
        assertThat(cellsWith1Candidate + cellsWith2Candidates + cellsWith3Plus)
            .`as`("Should account for all originally empty cells")
            .isEqualTo(originallyEmpty.size)
    }

    private fun boardToPuzzleString(board: Board): String {
        return buildString {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val coord = Coord(row, col)
                    if (board.isConfirmed(coord)) {
                        append(board.value(coord))
                    } else {
                        append('.')
                    }
                }
            }
        }
    }
}
