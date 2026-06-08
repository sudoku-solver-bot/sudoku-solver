package will.sudoku.solver

/**
 * Pointing Candidate Eliminator
 *
 * When a candidate value within a box is confined to a single row or column,
 * that value can be eliminated from other cells in that row/column outside the box.
 */
class PointingCandidateEliminator : CandidateEliminator {

    override val displayName = "Pointing"

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            for (boxRow in 0..2) {
                for (boxCol in 0..2) {
                    if (eliminateFromBox(board, boxRow, boxCol)) {
                        anyUpdate = true
                        stable = false
                    }
                }
            }
        } while (!stable)
        return anyUpdate
    }

    private fun eliminateFromBox(board: Board, boxRow: Int, boxCol: Int): Boolean {
        var anyUpdate = false
        for (value in 1..9) {
            val mask = Board.masks[value - 1]
            val cellsWithCandidate = mutableListOf<Coord>()

            for (r in boxRow * 3 until boxRow * 3 + 3) {
                for (c in boxCol * 3 until boxCol * 3 + 3) {
                    val coord = Coord(r, c)
                    if ((board.candidatePattern(coord) and mask) != 0) {
                        cellsWithCandidate.add(coord)
                    }
                }
            }

            if (cellsWithCandidate.size < 2) continue

            // Check if all cells share the same row
            val firstRow = cellsWithCandidate[0].row
            if (cellsWithCandidate.all { it.row == firstRow }) {
                for (col in 0..8) {
                    if (col / 3 == boxCol) continue
                    val cell = Coord(firstRow, col)
                    if (board.eraseCandidateValue(cell, value)) anyUpdate = true
                }
            }

            // Check if all cells share the same column
            val firstCol = cellsWithCandidate[0].col
            if (cellsWithCandidate.all { it.col == firstCol }) {
                for (row in 0..8) {
                    if (row / 3 == boxRow) continue
                    val cell = Coord(row, firstCol)
                    if (board.eraseCandidateValue(cell, value)) anyUpdate = true
                }
            }
        }
        return anyUpdate
    }
}
