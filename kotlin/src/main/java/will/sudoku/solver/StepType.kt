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
         */
        fun fromTechniqueName(name: String): StepType {
            return entries.firstOrNull {
                it.displayName.equals(name, ignoreCase = true) ||
                it.name.replace("_", " ").equals(name, ignoreCase = true)
            } ?: TECHNIQUE_APPLIED
        }
    }
}
