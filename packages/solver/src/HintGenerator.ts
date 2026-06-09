import type { Board } from './Board'
import { Coord } from './Coord'
import { SimpleCandidateEliminator } from './Eliminators'
import {
  Technique,
  TECHNIQUE_DESCRIPTIONS,
} from './HintTypes'
import type { Hint, HintOptions, TechniqueDetector } from './HintTypes'

export { Technique, TECHNIQUE_DESCRIPTIONS } from './HintTypes'
export type { Hint, HintOptions, TechniqueDetector } from './HintTypes'

// ---------------------------------------------------------------------------
// Detectors (imported from detector modules)
// ---------------------------------------------------------------------------

import { NakedSingleDetector } from './detectors/NakedSingleDetector'
import { HiddenSingleDetector } from './detectors/HiddenSingleDetector'
import { PointingPairDetector } from './detectors/PointingPairDetector'
import { BoxLineReductionDetector } from './detectors/BoxLineReductionDetector'
import { NakedPairDetector } from './detectors/NakedPairDetector'
import { NakedTripleDetector } from './detectors/NakedTripleDetector'
import { HiddenPairDetector } from './detectors/HiddenPairDetector'
import { HiddenTripleDetector } from './detectors/HiddenTripleDetector'

const detectors: TechniqueDetector[] = [
  new NakedSingleDetector(),
  new HiddenSingleDetector(),
  new PointingPairDetector(),
  new BoxLineReductionDetector(),
  new NakedPairDetector(),
  new NakedTripleDetector(),
  new HiddenPairDetector(),
  new HiddenTripleDetector()
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
  const hiddenSingleDetector = detectors.find(d => d.technique === Technique.HIDDEN_SINGLE)!
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
  const pointingPairDetector = detectors.find(d => d.technique === Technique.POINTING_PAIR)!
  let foundAny = true
  while (foundAny) {
    foundAny = false
    const hint = pointingPairDetector.detect(board)
    if (hint != null) {
      board.eraseCandidateValue(hint.coord, hint.value)
      foundAny = true
      applyBasicElimination(board)
    }
  }
}

/**
 * Apply box/line reductions until no more are found.
 */
export function applyBoxLineReductionsUntilStable(board: Board): void {
  const boxLineDetector = detectors.find(d => d.technique === Technique.BOX_LINE_REDUCTION)!
  let foundAny = true
  while (foundAny) {
    foundAny = false
    const hint = boxLineDetector.detect(board)
    if (hint != null) {
      board.eraseCandidateValue(hint.coord, hint.value)
      foundAny = true
      applyBasicElimination(board)
    }
  }
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
