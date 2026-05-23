package will.sudoku.solver

interface CandidateEliminator {

    /**
     * Human-readable display name for this technique (e.g., "Naked Single", "X-Wing").
     * Default derives from the class simple name. Override for better display names.
     */
    val displayName: String get() = javaClass.simpleName.removeSuffix("CandidateEliminator")

    /**
     * Specific technique name from the last elimination run.
     * Override for eliminators that report different technique names based on context
     * (e.g., Naked Pair vs Naked Triple vs Naked Subset).
     * Falls back to displayName by default.
     */
    val lastTechniqueName: String get() = displayName

    fun eliminate(board: Board): Boolean
}