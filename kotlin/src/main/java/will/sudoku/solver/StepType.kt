package will.sudoku.solver

/**
 * Types of solving steps that can be recorded during puzzle solving.
 */
enum class StepType(val displayName: String, val description: String) {
    // Elimination techniques
    SIMPLE_ELIMINATION("Simple Elimination", "Removed candidates that appear in same row/column/box"),
    NAKED_PAIR("Naked Pair", "Found two cells with same two candidates in a group"),
    NAKED_TRIPLE("Naked Triple", "Found three cells with same three candidates in a group"),
    HIDDEN_SINGLE("Hidden Single", "Found a value that can only go in one cell in a group"),
    HIDDEN_PAIR("Hidden Pair", "Found two values that only appear in two cells in a group"),
    HIDDEN_TRIPLE("Hidden Triple", "Found three values that only appear in three cells in a group"),
    X_WING("X-Wing", "Found X-Wing pattern eliminating candidates"),
    SWORDFISH("Swordfish", "Found Swordfish pattern eliminating candidates"),
    XY_WING("XY-Wing", "Found XY-Wing pattern eliminating candidates"),
    NAKED_SUBSET("Naked Subset", "Found naked pair/triple/quad eliminating candidates in a group"),
    HIDDEN_SUBSET("Hidden Subset", "Found hidden pair/triple/quad values in a group"),

    // Actions
    CELL_FILLED("Cell Filled", "Filled a cell with its only remaining candidate"),
    CANDIDATE_ELIMINATED("Candidate Eliminated", "Removed a candidate from a cell"),
    GUESS_MADE("Guess Made", "Made a guess (backtracking required)"),
    BACKTRACK("Backtrack", "Previous guess was wrong, trying another option"),

    // Generic technique application (when specific technique type is unknown)
    TECHNIQUE_APPLIED("Technique Applied", "A solving technique was applied"),

    // Status
    PUZZLE_SOLVED("Puzzle Solved", "The puzzle has been completely solved"),
    NO_SOLUTION("No Solution", "The puzzle has no valid solution"),
    AMBIGUOUS("Ambiguous", "Puzzle has multiple solutions (not unique)");

    companion object {
        /**
         * Try to find a matching StepType for an eliminator display name.
         * Falls back to TECHNIQUE_APPLIED if no match.
         *
         * Matching strategy:
         * 1. Exact match on displayName or enum name (case-insensitive)
         * 2. Normalized match (strip hyphens, spaces, parentheses)
         * 3. Explicit aliases for generic eliminators
         */
        fun fromTechniqueName(name: String): StepType {
            // Direct match on display name or enum name
            entries.firstOrNull {
                it.displayName.equals(name, ignoreCase = true) ||
                it.name.replace("_", " ").equals(name, ignoreCase = true)
            }?.let { return it }

            // Normalized match: strip hyphens, spaces, parentheses for comparison
            // e.g. "XWing" matches "X-Wing", "XYWing" matches "XY-Wing"
            val normalized = name.lowercase().replace("[-\\s()]", "")
            entries.firstOrNull {
                it.displayName.lowercase().replace("[-\\s()]", "") == normalized ||
                it.name.lowercase().replace("_", "") == normalized
            }?.let { return it }

            // Explicit aliases for generic eliminator names
            return when {
                name.startsWith("Exclusion", ignoreCase = true) -> SIMPLE_ELIMINATION
                else -> TECHNIQUE_APPLIED
            }
        }
    }
}
