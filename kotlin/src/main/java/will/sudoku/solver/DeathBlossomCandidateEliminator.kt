package will.sudoku.solver

import kotlin.random.Random

/**
 * Death Blossom Candidate Eliminator
 *
 * **DISABLED** — the implementation produces invalid board states on
 * hard puzzles (g3/g4). The algorithm's ALS detection, stem-cell
 * assignment, and blossom-cell elimination logic have subtle bugs that
 * cause incorrect candidate removal.
 *
 * TODO: Reimplement with rigorous validation against known puzzles.
 */
class DeathBlossomCandidateEliminator : CandidateEliminator {
    override fun eliminate(board: Board): Boolean {
        // Disabled — see class doc above
        return false
    }
}
