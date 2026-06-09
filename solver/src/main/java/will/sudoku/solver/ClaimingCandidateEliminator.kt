package will.sudoku.solver

/**
 * Claiming Candidate Eliminator
 *
 * When a candidate value in a row or column is confined to a single box,
 * that value can be eliminated from other cells in that box outside the row/column.
 */
class ClaimingCandidateEliminator : CandidateEliminator {

    override val displayName = "Claiming"

    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            for (i in 0..8) {
                if (eliminateFromRow(board, i)) { anyUpdate = true; stable = false }
                if (eliminateFromCol(board, i)) { anyUpdate = true; stable = false }
            }
        } while (!stable)
        return anyUpdate
    }

    private fun eliminateFromRow(board: Board, row: Int): Boolean {
        var anyUpdate = false
        for (value in 1..9) {
            val mask = Board.masks[value - 1]
            val cellsWithCandidate = (0..8).filter { col ->
                !board.isConfirmed(Coord(row, col)) && (board.candidatePattern(Coord(row, col)) and mask) != 0
            }
            if (cellsWithCandidate.size < 2) continue

            val firstBox = cellsWithCandidate[0] / 3
            if (cellsWithCandidate.all { it / 3 == firstBox }) {
                val boxRow = row / 3
                val colStart = firstBox * 3
                for (r in boxRow * 3 until boxRow * 3 + 3) {
                    if (r == row) continue
                    for (c in colStart until colStart + 3) {
                        val cell = Coord(r, c)
                        if (!board.isConfirmed(cell) && board.eraseCandidateValue(cell, value)) anyUpdate = true
                    }
                }
            }
        }
        return anyUpdate
    }

    private fun eliminateFromCol(board: Board, col: Int): Boolean {
        var anyUpdate = false
        for (value in 1..9) {
            val mask = Board.masks[value - 1]
            val cellsWithCandidate = (0..8).filter { row ->
                !board.isConfirmed(Coord(row, col)) && (board.candidatePattern(Coord(row, col)) and mask) != 0
            }
            if (cellsWithCandidate.size < 2) continue

            val firstBox = cellsWithCandidate[0] / 3
            if (cellsWithCandidate.all { it / 3 == firstBox }) {
                val boxCol = col / 3
                val rowStart = firstBox * 3
                for (r in rowStart until rowStart + 3) {
                    for (c in boxCol * 3 until boxCol * 3 + 3) {
                        if (c == col) continue
                        val cell = Coord(r, c)
                        if (!board.isConfirmed(cell) && board.eraseCandidateValue(cell, value)) anyUpdate = true
                    }
                }
            }
        }
        return anyUpdate
    }
}
