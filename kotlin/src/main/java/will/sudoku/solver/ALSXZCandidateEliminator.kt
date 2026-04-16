package will.sudoku.solver

/**
 * ALS-XZ (Almost Locked Sets XZ) Candidate Eliminator
 *
 * **DISABLED** — the previous implementation had fundamental bugs in its
 * restricted-common detection and Type 1 elimination logic that caused
 * board corruption (invalid board states). The `isRestrictedCommon` method
 * was a stub that always returned `true`, and even after fixing the visibility
 * check the eliminations remained too aggressive (54 cells modified on a
 * single puzzle, many reduced to a single candidate).
 *
 * A correct ALS-XZ implementation requires:
 * 1. Proper ALS detection with cell-level candidate tracking
 * 2. Accurate restricted common detection (not just visibility)
 * 3. Careful Type 1 elimination that only targets cells seeing ALL Z instances
 *
 * TODO: Reimplement with reference to https://www.sudopedia.org/wiki/ALS-XZ
 */
class ALSXZCandidateEliminator : CandidateEliminator {
    override fun eliminate(board: Board): Boolean {
        // Disabled — see class doc above
        return false
    }
}
