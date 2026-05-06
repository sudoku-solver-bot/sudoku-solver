package will.sudoku.solver

class SimpleCandidateEliminator : CandidateEliminator {
    override val displayName = "Simple Elimination"
    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            for (coordGroup in CoordGroup.all) {
                for (coord in coordGroup.coords) {
                    if (board.isConfirmed(coord)) {
                        for (peerCoord in coordGroup.coords) {
                            val updated = (coord != peerCoord) &&
                                    board.eraseCandidatePattern(peerCoord, board.candidatePattern(coord))
                            if (updated) {
                                anyUpdate = true
                                stable = false
                            }
                        }
                    }
                }
            }
        } while (!stable)
        return anyUpdate
    }
}
