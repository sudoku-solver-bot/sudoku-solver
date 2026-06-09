import type { Coord } from './Coord'
import { StepType } from './StepType'

/**
 * Represents a single step in the solving process.
 *
 * Each step captures:
 * - The type of solving action performed
 * - The cell(s) affected
 * - The value(s) involved
 * - A human-readable explanation
 * - The board state after this step (optional, for replay)
 *
 * Equivalent to the Kotlin SolvingStep data class.
 */
export interface SolvingStep {
  stepNumber: number
  stepType: StepType
  affectedCells: Coord[]
  values: Set<number>
  explanation: string
  boardState?: string
  timestamp: number
}

/** Create a step for filling a cell. */
export function cellFilled(
  stepNumber: number,
  cell: Coord,
  value: number,
  explanation: string,
  boardState?: string
): SolvingStep {
  return {
    stepNumber,
    stepType: StepType.CELL_FILLED,
    affectedCells: [cell],
    values: new Set([value]),
    explanation,
    boardState,
    timestamp: Date.now()
  }
}

/** Create a step for candidate elimination. */
export function candidateEliminated(
  stepNumber: number,
  cell: Coord,
  eliminatedValue: number,
  technique: StepType,
  explanation: string
): SolvingStep {
  return {
    stepNumber,
    stepType: technique,
    affectedCells: [cell],
    values: new Set([eliminatedValue]),
    explanation,
    timestamp: Date.now()
  }
}

/** Create a step for a guess (backtracking). */
export function guessMade(
  stepNumber: number,
  cell: Coord,
  guessedValue: number,
  explanation: string
): SolvingStep {
  return {
    stepNumber,
    stepType: StepType.GUESS_MADE,
    affectedCells: [cell],
    values: new Set([guessedValue]),
    explanation,
    timestamp: Date.now()
  }
}

/** Create a step for backtracking. */
export function backtrack(
  stepNumber: number,
  cell: Coord,
  wrongValue: number,
  explanation: string
): SolvingStep {
  return {
    stepNumber,
    stepType: StepType.BACKTRACK,
    affectedCells: [cell],
    values: new Set([wrongValue]),
    explanation,
    timestamp: Date.now()
  }
}

/** Create a step for a technique application. */
export function techniqueApplied(
  stepNumber: number,
  techniqueName: string,
  _eliminations: number,
  explanation: string
): SolvingStep {
  return {
    stepNumber,
    stepType: StepType.TECHNIQUE_APPLIED,
    affectedCells: [],
    values: new Set(),
    explanation,
    timestamp: Date.now()
  }
}

/** Format a step as a human-readable string. */
export function formatStep(step: SolvingStep): string {
  const cellStr = step.affectedCells.map(c => `(${c.row + 1}, ${c.col + 1})`).join(', ')
  const valueStr = [...step.values].join(', ')
  return `Step ${step.stepNumber}: ${step.stepType}\n  Cells: ${cellStr}\n  Values: ${valueStr}\n  ${step.explanation}`
}
