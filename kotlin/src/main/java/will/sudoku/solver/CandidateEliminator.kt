package will.sudoku.solver

interface CandidateEliminator {

    /**
     * Human-readable display name for this technique (e.g., "Naked Single", "X-Wing").
     * Default derives from the class simple name. Override for better display names.
     */
    val displayName: String get() = javaClass.simpleName.removeSuffix("CandidateEliminator")

    fun eliminate(board: Board): Boolean
}