import type { Board } from './Board'
import type { Coord } from './Coord'
import type { SolvingListener, Elimination } from './Solver'
import { StepType, stepTypeFromTechniqueName } from './StepType'
import type { SolvingStep } from './SolvingStep'
import { cellFilled, backtrack as backtrackStep } from './SolvingStep'
import { SolvingProgress } from './SolvingProgress'

/**
 * Records each step of the solving process.
 *
 * Implements SolvingListener to capture every cell fill, backtracking event,
 * and eliminator application into a SolvingProgress.
 *
 * Equivalent to the Kotlin StepRecorder class.
 *
 * @example
 * ```ts
 * const solver = new Solver()
 * const recorder = new StepRecorder()
 * const solution = solver.solve(board, recorder)
 * if (solution) {
 *   console.log(`Solved in ${recorder.progress.steps.length} steps`)
 * }
 * ```
 */
export class StepRecorder implements SolvingListener {
  private _steps: SolvingStep[] = []
  private _stepNumber = 0
  private _currentBoardState = ''
  private _solved = false
  private _noSolution = false
  private _noSolutionReason = ''

  /** The solving progress recorded so far. */
  get progress(): SolvingProgress {
    const progressObj = new SolvingProgress(this._currentBoardState)
    for (const step of this._steps) {
      progressObj.addStep(step)
    }
    if (this._solved) {
      progressObj.markSolved(this._currentBoardState)
    } else if (this._noSolution) {
      progressObj.markNoSolution(this._noSolutionReason)
    }
    return progressObj
  }

  /** Set the current board state (called by solver). */
  setBoardState(boardString: string): void {
    this._currentBoardState = boardString
  }

  onCellFilled(coord: Coord, value: number, explanation: string): void {
    this._stepNumber++
    this._steps.push(cellFilled(
      this._stepNumber,
      coord,
      value,
      explanation,
      this._currentBoardState
    ))
  }

  onBacktracking(): void {
    this._stepNumber++
    this._steps.push(backtrackStep(
      this._stepNumber,
      { row: 0, col: 0, index: 0 }, // Placeholder — no specific cell for backtracking
      0, // Placeholder — no specific value
      'Backtracking — no valid candidates found'
    ))
  }

  onCandidatesEliminated(techniqueName: string, eliminations: Elimination[]): void {
    if (eliminations.length === 0) return

    this._stepNumber++
    const affectedCells = eliminations.map(e => e.coord)
    const allValues = new Set<number>()
    let totalEliminated = 0
    for (const e of eliminations) {
      for (const v of e.eliminatedValues) {
        allValues.add(v)
        totalEliminated++
      }
    }

    // Build human-readable per-cell explanation
    const cellDescs = [...eliminations]
      .sort((a, b) => a.coord.row * 9 + a.coord.col - (b.coord.row * 9 + b.coord.col))
      .map(e => {
        const cellLabel = `(${e.coord.row + 1},${e.coord.col + 1})`
        const vals = e.eliminatedValues.sort().join(',')
        return `${cellLabel}: [${vals}]`
      })
      .join('; ')

    const stepType = stepTypeFromTechniqueName(techniqueName)
    const explanation = `${techniqueName}: ${totalEliminated} candidate(s) eliminated — ${cellDescs}`

    this._steps.push({
      stepNumber: this._stepNumber,
      stepType,
      affectedCells,
      values: allValues,
      explanation,
      timestamp: Date.now()
    })
  }

  onSolveComplete(solved: boolean, _timeNanos: number, _backtracks: number): void {
    if (solved) {
      this._solved = true
    } else {
      this._noSolution = true
      this._noSolutionReason = 'No valid solution found'
    }
  }

  // No-op callbacks for optional listener methods
  onPropagationPassStarted(): void {}
  onEliminatorApplied(_techniqueName: string, _totalEliminated: number): void {}
  onGuessMade(_coord: Coord, _firstCandidate: number, _totalCandidates: number): void {}
}
