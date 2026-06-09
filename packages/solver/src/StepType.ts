/**
 * Types of solving steps that can be recorded during puzzle solving.
 *
 * Equivalent to the Kotlin StepType enum.
 */
export enum StepType {
  // Elimination techniques
  SIMPLE_ELIMINATION = 'Simple Elimination',
  NAKED_PAIR = 'Naked Pair',
  NAKED_TRIPLE = 'Naked Triple',
  HIDDEN_SINGLE = 'Hidden Single',
  HIDDEN_PAIR = 'Hidden Pair',
  HIDDEN_TRIPLE = 'Hidden Triple',
  X_WING = 'X-Wing',
  SWORDFISH = 'Swordfish',
  XY_WING = 'XY-Wing',
  UNIQUE_RECTANGLE = 'Unique Rectangle',
  SIMPLE_COLORING = 'Simple Coloring',
  NAKED_SUBSET = ' Naked Subset',
  HIDDEN_SUBSET = 'Hidden Subset',

  // Actions
  CELL_FILLED = 'Cell Filled',
  CANDIDATE_ELIMINATED = 'Candidate Eliminated',
  GUESS_MADE = 'Guess Made',
  BACKTRACK = 'Backtrack',

  // Generic
  TECHNIQUE_APPLIED = 'Technique Applied',

  // Status
  PUZZLE_SOLVED = 'Puzzle Solved',
  NO_SOLUTION = 'No Solution',
  AMBIGUOUS = 'Ambiguous'
}

/** Human-readable descriptions for each step type. */
export const STEP_TYPE_DESCRIPTIONS: Record<StepType, string> = {
  [StepType.SIMPLE_ELIMINATION]: 'Removed candidates that appear in same row/column/box',
  [StepType.NAKED_PAIR]: 'Found two cells with same two candidates in a group',
  [StepType.NAKED_TRIPLE]: 'Found three cells with same three candidates in a group',
  [StepType.HIDDEN_SINGLE]: 'Found a value that can only go in one cell in a group',
  [StepType.HIDDEN_PAIR]: 'Found two values that only appear in two cells in a group',
  [StepType.HIDDEN_TRIPLE]: 'Found three values that only appear in three cells in a group',
  [StepType.X_WING]: 'Found X-Wing pattern eliminating candidates',
  [StepType.SWORDFISH]: 'Found Swordfish pattern eliminating candidates',
  [StepType.XY_WING]: 'Found XY-Wing pattern eliminating candidates',
  [StepType.UNIQUE_RECTANGLE]: 'Found Unique Rectangle pattern eliminating candidates',
  [StepType.SIMPLE_COLORING]: 'Found Simple Coloring pattern eliminating candidates',
  [StepType.NAKED_SUBSET]: 'Found naked pair/triple/quad eliminating candidates in a group',
  [StepType.HIDDEN_SUBSET]: 'Found hidden pair/triple/quad values in a group',
  [StepType.CELL_FILLED]: 'Filled a cell with its only remaining candidate',
  [StepType.CANDIDATE_ELIMINATED]: 'Removed a candidate from a cell',
  [StepType.GUESS_MADE]: 'Made a guess (backtracking required)',
  [StepType.BACKTRACK]: 'Previous guess was wrong, trying another option',
  [StepType.TECHNIQUE_APPLIED]: 'A solving technique was applied',
  [StepType.PUZZLE_SOLVED]: 'The puzzle has been completely solved',
  [StepType.NO_SOLUTION]: 'The puzzle has no valid solution',
  [StepType.AMBIGUOUS]: 'Puzzle has multiple solutions (not unique)'
}

/**
 * Try to find a matching StepType for an eliminator display name.
 * Falls back to TECHNIQUE_APPLIED if no match.
 */
export function stepTypeFromTechniqueName(name: string): StepType {
  // Direct match on display name or enum name
  for (const st of Object.values(StepType)) {
    if (st === name || st.toLowerCase() === name.toLowerCase()) {
      return st as StepType
    }
  }

  // Normalized match: strip hyphens, spaces, parentheses
  const normalized = name.toLowerCase().replace(/[-\s()]/g, '')
  for (const st of Object.values(StepType)) {
    if (st.toLowerCase().replace(/[-\s()]/g, '') === normalized ||
        (st as string).toLowerCase().replace(/_/g, '') === normalized) {
      return st as StepType
    }
  }

  // Explicit aliases
  if (name.startsWith('Exclusion')) return StepType.SIMPLE_ELIMINATION
  if (name === 'UniqueRectangles') return StepType.UNIQUE_RECTANGLE
  if (name === 'SimpleColoring') return StepType.SIMPLE_COLORING

  return StepType.TECHNIQUE_APPLIED
}
