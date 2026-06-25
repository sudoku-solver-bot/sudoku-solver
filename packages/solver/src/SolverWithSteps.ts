import { Board } from './Board'
import { BoardReader } from './BoardReader'
import { Coord } from './Coord'
import { Solver, SolverConfig } from './Solver'
import { StepRecorder } from './StepRecorder'
import { SolvingProgress } from './SolvingProgress'

/**
 * A solver wrapper that records every step of the solving process.
 *
 * Creates a Solver + StepRecorder internally, runs solve, and returns
 * the solution along with step-by-step progress.
 *
 * Equivalent to the Kotlin SolverWithSteps class.
 *
 * @example
 * ```ts
 * const wrapper = new SolverWithSteps()
 * const [solution, progress] = wrapper.solveWithSteps(board)
 * if (solution) {
 *   console.log(`Solved in ${progress.steps.length} steps`)
 * }
 * ```
 */
export class SolverWithSteps {
  private solver: Solver

  constructor(config?: SolverConfig) {
    this.solver = new Solver(config)
  }

  /**
   * Solve a puzzle and return the solution with step-by-step progress.
   */
  solveWithSteps(initialBoard: Board): [Board | null, SolvingProgress] {
    const recorder = new StepRecorder()
    recorder.setBoardState(boardToString(initialBoard))

    const solution = this.solver.solve(initialBoard, recorder)

    if (solution != null) {
      recorder.setBoardState(boardToString(solution))
    }

    return [solution, recorder.progress]
  }

  /**
   * Convenience method to solve a puzzle string and get step-by-step results.
   */
  static solveWithSteps(puzzleString: string): [Board | null, SolvingProgress]
  static solveWithSteps(puzzleString: string, config: SolverConfig): [Board | null, SolvingProgress]
  static solveWithSteps(puzzleString: string, config?: SolverConfig): [Board | null, SolvingProgress] {
    const board = BoardReader.fromString(puzzleString, Board)
    return new SolverWithSteps(config).solveWithSteps(board)
  }
}

/** Convert board to string representation (81 chars, '.' for empty). */
function boardToString(board: Board): string {
  let s = ''
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      const coord = Coord.all[row * 9 + col]
      if (board.isConfirmed(coord)) {
        s += board.value(coord)
      } else {
        s += '.'
      }
    }
  }
  return s
}
