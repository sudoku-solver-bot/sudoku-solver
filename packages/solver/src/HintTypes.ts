import type { Board } from './Board'
import { Coord } from './Coord'

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

/** A hint for the next solving move. */
export interface Hint {
  coord: Coord
  value: number
  technique: Technique
  explanation: string
}

/** Options for hint generation. */
export interface HintOptions {
  /** If true, exhaust hidden singles before checking techniques. Default: true. */
  exhaustHiddenSingles?: boolean
  /** If set, check this technique first before iterating. */
  targetTechnique?: Technique
}

// ---------------------------------------------------------------------------
// Technique enum (ordered easiest → hardest)
// ---------------------------------------------------------------------------

export enum Technique {
  NAKED_SINGLE = 'Naked Single',
  HIDDEN_SINGLE = 'Hidden Single',
  POINTING_PAIR = 'Pointing Pair',
  BOX_LINE_REDUCTION = 'Box/Line Reduction',
  NAKED_PAIR = 'Naked Pair',
  NAKED_TRIPLE = 'Naked Triple',
  HIDDEN_PAIR = 'Hidden Pair',
  HIDDEN_TRIPLE = 'Hidden Triple',
  X_WING = 'X-Wing',
  SWORDFISH = 'Swordfish',
  XY_WING = 'XY-Wing',
  XYZ_WING = 'XYZ-Wing',
  W_WING = 'W-Wing',
  SIMPLE_COLORING = 'Simple Coloring',
  UNIQUE_RECTANGLE = 'Unique Rectangle',
  ALS_XZ = 'ALS-XZ',
  FRANKEN_FISH = 'Franken Fish',
  MUTANT_FISH = 'Mutant Fish',
  DEATH_BLOSSOM = 'Death Blossom',
  FORCING_CHAINS = 'Forcing Chains'
}

/** Human-readable descriptions for each technique. */
export const TECHNIQUE_DESCRIPTIONS: Record<Technique, string> = {
  [Technique.NAKED_SINGLE]: 'Only one candidate fits in this cell',
  [Technique.HIDDEN_SINGLE]: 'This value appears only once in a row, column, or region',
  [Technique.POINTING_PAIR]: 'A candidate restricted to one row/col in a box',
  [Technique.BOX_LINE_REDUCTION]: 'Candidates restricted to one box in a row/col',
  [Technique.NAKED_PAIR]: 'Two cells with same two candidates - eliminate from others',
  [Technique.NAKED_TRIPLE]: 'Three cells with same three candidates - eliminate from others',
  [Technique.HIDDEN_PAIR]: 'Two values appear only in same two cells',
  [Technique.HIDDEN_TRIPLE]: 'Three values appear only in same three cells',
  [Technique.X_WING]: 'Pattern in 2 rows/cols allows elimination',
  [Technique.SWORDFISH]: 'Pattern in 3 rows/cols allows elimination',
  [Technique.XY_WING]: 'Chain of 3 cells allows elimination',
  [Technique.XYZ_WING]: 'XYZ-Wing pattern with pivot and two wings',
  [Technique.W_WING]: 'Two cells linked by a strong link',
  [Technique.SIMPLE_COLORING]: 'Coloring chain elimination',
  [Technique.UNIQUE_RECTANGLE]: 'Deadly pattern avoidance',
  [Technique.ALS_XZ]: 'Almost Locked Set - XZ rule',
  [Technique.FRANKEN_FISH]: 'Generalized fish with box constraints',
  [Technique.MUTANT_FISH]: 'Mutant fish pattern',
  [Technique.DEATH_BLOSSOM]: 'Death blossom chain',
  [Technique.FORCING_CHAINS]: 'Forcing chain elimination'
}

// ---------------------------------------------------------------------------
// TechniqueDetector interface
// ---------------------------------------------------------------------------

export interface TechniqueDetector {
  technique: Technique
  detect(board: Board): Hint | null
}
