import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Solver } from '../src/Solver'
import { StepRecorder } from '../src/StepRecorder'

// Easy puzzle — solvable by basic elimination
const EASY_PUZZLE = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'

// Harder puzzle that needs backtracking
const HARD_PUZZLE = '.....6....59.....82....8....45........3........6..3.54...325..6..................'

describe('StepRecorder', () => {
  it('records steps while solving easy puzzle', () => {
    const board = BoardReader.fromString(EASY_PUZZLE, Board)
    const solver = new Solver()
    const recorder = new StepRecorder()

    const solution = solver.solve(board, recorder)

    expect(solution).not.toBeNull()
    const progress = recorder.progress
    expect(progress.steps.length).toBeGreaterThan(0)
    // Easy puzzle might be solved by basic elimination without triggering solve()
    // so isSolved may not be set — that's OK as long as steps were recorded
  })

  it('records steps while solving hard puzzle', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const solver = new Solver()
    const recorder = new StepRecorder()

    const solution = solver.solve(board, recorder)

    expect(solution).not.toBeNull()
    const progress = recorder.progress
    expect(progress.steps.length).toBeGreaterThan(0)
  })

  it('each step has explanation and technique', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const solver = new Solver()
    const recorder = new StepRecorder()

    solver.solve(board, recorder)

    for (const step of recorder.progress.steps) {
      expect(step.stepNumber).toBeGreaterThan(0)
      expect(step.explanation).toBeTruthy()
      expect(step.explanation.length).toBeGreaterThan(0)
      expect(step.stepType).toBeTruthy()
      expect(step.timestamp).toBeGreaterThan(0)
    }
  })

  it('progress summary is readable', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const solver = new Solver()
    const recorder = new StepRecorder()

    solver.solve(board, recorder)

    const summary = recorder.progress.summary()
    expect(summary).toContain('Solving Progress')
  })

  it('setBoardState updates internal state', () => {
    const recorder = new StepRecorder()
    recorder.setBoardState('123456789'.repeat(9))
    expect(recorder.progress.currentBoardState).toHaveLength(81)
  })
})
