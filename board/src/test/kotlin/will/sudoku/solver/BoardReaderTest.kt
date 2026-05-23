package will.sudoku.solver

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class BoardReaderTest {

    @Test
    fun testBoardReading() {
        val board = BoardReader.readBoard(
            this.javaClass.getResourceAsStream("/solver/www.sudokuweb.org/g1.question")
        )

        Assertions.assertThat(board).isEqualTo(
            Board(
                intArrayOf(
                    0, 4, 0, 3, 0, 8, 1, 0, 0,
                    2, 1, 0, 0, 6, 5, 0, 0, 0,
                    6, 0, 0, 0, 0, 0, 0, 7, 0,
                    9, 0, 3, 0, 4, 6, 7, 8, 1,
                    1, 0, 4, 8, 2, 9, 5, 0, 6,
                    8, 0, 5, 0, 0, 0, 0, 2, 0,
                    4, 0, 0, 0, 0, 0, 6, 0, 3,
                    0, 0, 0, 6, 0, 2, 0, 4, 7,
                    0, 8, 0, 0, 3, 0, 0, 0, 0
                )
            )
        )
    }

}