package will.sudoku.solver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CoordTest {

    @Test
    fun testCoordGroup() {

        assertThat(CoordGroup.verticalOf(Coord(0, 0)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(1, 0), Coord(2, 0),
                Coord(3, 0), Coord(4, 0), Coord(5, 0),
                Coord(6, 0), Coord(7, 0), Coord(8, 0)
            )
        )

        assertThat(CoordGroup.verticalOf(Coord(5, 0)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(1, 0), Coord(2, 0),
                Coord(3, 0), Coord(4, 0), Coord(5, 0),
                Coord(6, 0), Coord(7, 0), Coord(8, 0)
            )
        )

        assertThat(CoordGroup.verticalOf(Coord(8, 8)).coords).isEqualTo(
            listOf(
                Coord(0, 8), Coord(1, 8), Coord(2, 8),
                Coord(3, 8), Coord(4, 8), Coord(5, 8),
                Coord(6, 8), Coord(7, 8), Coord(8, 8)
            )
        )

        assertThat(CoordGroup.horizontalOf(Coord(0, 0)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(0, 1), Coord(0, 2),
                Coord(0, 3), Coord(0, 4), Coord(0, 5),
                Coord(0, 6), Coord(0, 7), Coord(0, 8)
            )
        )
        assertThat(CoordGroup.horizontalOf(Coord(0, 5)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(0, 1), Coord(0, 2),
                Coord(0, 3), Coord(0, 4), Coord(0, 5),
                Coord(0, 6), Coord(0, 7), Coord(0, 8)
            )
        )
        assertThat(CoordGroup.horizontalOf(Coord(8, 8)).coords).isEqualTo(
            listOf(
                Coord(8, 0), Coord(8, 1), Coord(8, 2),
                Coord(8, 3), Coord(8, 4), Coord(8, 5),
                Coord(8, 6), Coord(8, 7), Coord(8, 8)
            )
        )

        assertThat(CoordGroup.regionOf(Coord(0, 0)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(0, 1), Coord(0, 2),
                Coord(1, 0), Coord(1, 1), Coord(1, 2),
                Coord(2, 0), Coord(2, 1), Coord(2, 2)
            )
        )
        assertThat(CoordGroup.regionOf(Coord(1, 1)).coords).isEqualTo(
            listOf(
                Coord(0, 0), Coord(0, 1), Coord(0, 2),
                Coord(1, 0), Coord(1, 1), Coord(1, 2),
                Coord(2, 0), Coord(2, 1), Coord(2, 2)
            )
        )
        assertThat(CoordGroup.regionOf(Coord(5, 5)).coords).isEqualTo(
            listOf(
                Coord(3, 3), Coord(3, 4), Coord(3, 5),
                Coord(4, 3), Coord(4, 4), Coord(4, 5),
                Coord(5, 3), Coord(5, 4), Coord(5, 5)
            )
        )


    }

}