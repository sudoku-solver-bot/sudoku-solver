package will.sudoku.solver

class GroupCandidateEliminator : CandidateEliminator {
    override val displayName = "Naked Subset"
    override fun eliminate(board: Board): Boolean {
        var anyUpdate = false
        var stable: Boolean
        do {
            stable = true
            for (coordGroup in CoordGroup.all) {
                // find candidate pattern which occurrence equals to its bit count
                val candidatePatterns = coordGroup.coords
                    .groupingBy { board.candidatePattern(it) }
                    .eachCount()
                    .filter {
                        val bitCount = it.key.countOneBits()
                        bitCount == it.value && bitCount != 9
                    }
                    .keys

                for (candidatePattern in candidatePatterns) {
                    // and then exclude that pattern from other cells in the same group
                    val updated = coordGroup.coords
                        .filterNot { board.candidatePattern(it) == candidatePattern }
                        .map { board.eraseCandidatePattern(it, candidatePattern) }
                        .any { it }

                    if (updated) {
                        anyUpdate = true
                        stable = false
                    }
                }
            }
        } while (!stable)

        return anyUpdate

    }
}