import type { Board } from './Board'
import { Coord } from './Coord'
import { SimpleCandidateEliminator } from './Eliminators'
import { NakedSingleDetector } from './detectors/NakedSingleDetector'
import { HiddenSingleDetector } from './detectors/HiddenSingleDetector'

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

// ---------------------------------------------------------------------------
// Detectors (will be populated as we port them)
// ---------------------------------------------------------------------------

const nakedSingleDetector = new NakedSingleDetector()
const hiddenSingleDetector = new HiddenSingleDetector()

// Placeholder detectors for techniques not yet ported
const detectors: TechniqueDetector[] = [
  nakedSingleDetector,
  hiddenSingleDetector
  // TODO: Add more detectors as they are ported from Kotlin
]

// ---------------------------------------------------------------------------
// HintGenerator
// ---------------------------------------------------------------------------

/**
 * Generate a hint for the next move.
 *
 * Strategy:
 * 1. Apply basic elimination iteratively to stabilise the board
 * 2. Optionally exhaust hidden singles
 * 3. Try techniques from easiest to hardest
 * 4. Return the simplest applicable technique
 */
export function generate(board: Board, options?: HintOptions): Hint | null {
  const { exhaustHiddenSingles = true, targetTechnique } = options ?? {}

  // Step 1: Apply basic elimination to reach a stable state
  applyBasicElimination(board)

  // Step 2: Optionally exhaust hidden singles
  let lastHiddenSingle: Hint | null = null
  if (exhaustHiddenSingles) {
    lastHiddenSingle = applyHiddenSinglesUntilStable(board)
  }

  // Step 2.5: Exhaust intermediate techniques
  if (exhaustHiddenSingles) {
    applyPointingPairsUntilStable(board)
    applyBoxLineReductionsUntilStable(board)
  }

  // Step 2.6: If solved, return last hidden single
  if (board.isSolved() && lastHiddenSingle !== null) {
    return lastHiddenSingle
  }

  // Step 3: Check target technique first
  if (targetTechnique != null) {
    const targetHint = detectTechnique(board, targetTechnique)
    if (targetHint != null) return targetHint
  }

  // Step 4: Try techniques from easiest to hardest
  for (const technique of Object.values(Technique)) {
    const hint = detectTechnique(board, technique as Technique)
    if (hint != null) return hint
  }

  return null
}

/**
 * Apply basic elimination iteratively until the board is stable.
 */
export function applyBasicElimination(board: Board): void {
  const eliminator = new SimpleCandidateEliminator()
  let changed = true
  while (changed) {
    changed = eliminator.eliminate(board)
  }
}

/**
 * Apply hidden singles until no more are found.
 */
export function applyHiddenSinglesUntilStable(board: Board): Hint | null {
  let lastHint: Hint | null = null
  let foundAny = true
  while (foundAny) {
    foundAny = false
    let foundOne: boolean
    do {
      foundOne = false
      const hint = hiddenSingleDetector.detect(board)
      if (hint != null) {
        board.markValue(hint.coord, hint.value)
        lastHint = hint
        foundAny = true
        foundOne = true
        applyBasicElimination(board)
      }
    } while (foundOne)
  }
  return lastHint
}

/**
 * Apply pointing pairs until no more are found.
 */
export function applyPointingPairsUntilStable(board: Board): void {
  // TODO: Implement when PointingPairDetector is ported
}

/**
 * Apply box/line reductions until no more are found.
 */
export function applyBoxLineReductionsUntilStable(board: Board): void {
  // TODO: Implement when BoxLineReductionDetector is ported
}

/**
 * Detect a specific technique on the board.
 */
function detectTechnique(board: Board, technique: Technique): Hint | null {
  const detector = detectors.find(d => d.technique === technique)
  if (detector != null) {
    return detector.detect(board)
  }
  // TODO: Implement findTechniqueViaEliminator for eliminator-based techniques
  return null
}
