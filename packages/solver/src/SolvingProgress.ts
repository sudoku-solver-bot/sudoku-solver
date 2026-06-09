import { StepType } from './StepType'
import type { SolvingStep } from './SolvingStep'

/**
 * Metrics collected for a single eliminator.
 *
 * Equivalent to the Kotlin EliminatorMetrics data class.
 */
export interface EliminatorMetrics {
  eliminations: number
  passes: number
  totalTimeNanos: number
}

/**
 * Metrics collected during Sudoku solving process.
 *
 * Equivalent to the Kotlin SolverMetrics data class.
 */
export interface SolverMetrics {
  totalSolveTimeNanos: number
  backtrackingCount: number
  maxRecursionDepth: number
  propagationPasses: number
  cellsProcessed: number
  eliminatorMetrics: Map<string, EliminatorMetrics>
}

/** Create default empty metrics. */
export function emptyMetrics(): SolverMetrics {
  return {
    totalSolveTimeNanos: 0,
    backtrackingCount: 0,
    maxRecursionDepth: 0,
    propagationPasses: 0,
    cellsProcessed: 0,
    eliminatorMetrics: new Map()
  }
}

/**
 * Tracks the overall progress of solving a Sudoku puzzle step by step.
 *
 * Equivalent to the Kotlin SolvingProgress data class.
 */
export class SolvingProgress {
  originalPuzzle: string
  steps: SolvingStep[] = []
  currentBoardState: string
  isSolved = false
  hasNoSolution = false
  startTime: number
  endTime?: number

  constructor(puzzle: string) {
    this.originalPuzzle = puzzle
    this.currentBoardState = puzzle
    this.startTime = Date.now()
  }

  addStep(step: SolvingStep): void {
    this.steps.push(step)
  }

  nextStepNumber(): number {
    return this.steps.length + 1
  }

  markSolved(finalBoardState: string): void {
    this.isSolved = true
    this.currentBoardState = finalBoardState
    this.endTime = Date.now()
    this.addStep({
      stepNumber: this.nextStepNumber(),
      stepType: StepType.PUZZLE_SOLVED,
      affectedCells: [],
      values: new Set(),
      explanation: `Puzzle solved successfully in ${this.steps.length} steps!`,
      timestamp: Date.now()
    })
  }

  markNoSolution(reason = 'No valid solution exists'): void {
    this.hasNoSolution = true
    this.endTime = Date.now()
    this.addStep({
      stepNumber: this.nextStepNumber(),
      stepType: StepType.NO_SOLUTION,
      affectedCells: [],
      values: new Set(),
      explanation: reason,
      timestamp: Date.now()
    })
  }

  solveTimeMs(): number | null {
    return this.endTime != null ? this.endTime - this.startTime : null
  }

  techniqueStats(): Map<StepType, number> {
    const stats = new Map<StepType, number>()
    for (const step of this.steps) {
      stats.set(step.stepType, (stats.get(step.stepType) ?? 0) + 1)
    }
    return stats
  }

  summary(): string {
    const timeStr = this.solveTimeMs() != null ? `${this.solveTimeMs()}ms` : 'in progress'
    const status = this.isSolved ? 'Solved ✓' : this.hasNoSolution ? 'No Solution ✗' : 'In Progress...'
    const stats = this.techniqueStats()
    const techniqueCounts = [...stats.entries()]
      .filter(([k]) => k !== StepType.PUZZLE_SOLVED && k !== StepType.NO_SOLUTION)
      .map(([k, v]) => `${k}: ${v}`)
      .join(', ')

    let s = `Solving Progress\n${'─'.repeat(40)}\n`
    s += `Status: ${status}\n`
    s += `Steps: ${this.steps.length}\n`
    s += `Time: ${timeStr}\n`
    if (techniqueCounts) s += `Techniques: ${techniqueCounts}\n`
    return s
  }
}
