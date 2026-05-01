package will.sudoku.solver

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import will.sudoku.solver.BoardReader.Companion.readBoard
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension
import kotlin.streams.asSequence

class SolverTest {

    @ParameterizedTest(name="{index}: {0}")
    @MethodSource("boards")
    fun test(boardName: String, board: Board, expected: Board) {
        val solved = Solver().solve(board)
        Assertions.assertThat(solved).isEqualTo(expected)
    }

    /**
     * Regression test for #256: Solver should find a solution for puzzles
     * with multiple solutions, not return null. The fallback to basic
     * eliminators (without uniqueness-assuming techniques) handles this.
     */
    @Test
    fun `should solve puzzle with multiple solutions`() {
        // Puzzle from #256 — known to have 3 valid solutions
        val puzzle = "438.....9..16....3...73.........9.6.8..1..3...76.2....1...4279692...6.3.....17..."
        val board = BoardReader.readBoard(puzzle)
        val solved = Solver().solve(board)

        Assertions.assertThat(solved)
            .`as`("Solver should find a solution for a multi-solution puzzle (fallback)")
            .isNotNull

        Assertions.assertThat(solved!!.isSolved())
            .`as`("Result should be a fully solved board")
            .isTrue

        Assertions.assertThat(solved.isValid())
            .`as`("Result should be a valid board")
            .isTrue
    }

    @ExperimentalPathApi
    companion object {

        @JvmStatic
        fun boards(): Stream<Arguments> {
            return Files.walk(Paths.get(this::class.java.getResource("/solver").toURI()), 2)
                .asSequence()
                .filterNot {it.isDirectory()}
                .groupBy({it.fileName.nameWithoutExtension}, {it.fileName.extension to readBoard(it)})
                .mapValues {  (name, values) ->
                    val boards = values.associate {  it.first to it.second }
                    Arguments.of (name, boards["question"], boards["solution"])
                }.values
                .sortedBy { it.get()[0] as String }
                .stream()
        }

    }

}