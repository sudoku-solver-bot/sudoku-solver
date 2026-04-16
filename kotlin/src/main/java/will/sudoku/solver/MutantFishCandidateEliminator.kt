package will.sudoku.solver

import will.sudoku.solver.Board.Companion.masks

/**
 * Mutant Fish Candidate Eliminator
 *
 * **DISABLED** — the implementation produces invalid board states on
 * hard puzzles. The mixed base/cover set logic incorrectly eliminates
 * candidates that are part of the solution.
 *
 * TODO: Reimplement with proper validation against known puzzles.
 * Reference: https://www.sudopedia.org/wiki/Mutant_Fish
 */
class MutantFishCandidateEliminator : CandidateEliminator {
    override fun eliminate(board: Board): Boolean {
        // Disabled — see class doc above
        return false
    }
}
