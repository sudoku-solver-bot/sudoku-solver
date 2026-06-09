import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { SolverWithSteps } from '../src/SolverWithSteps'

const HARD_PUZZLE = '.....6....59.....82....8....45........3........6..3.54...325..6..................'

describe('SolverWithSteps', () => {
  it('solves puzzle and returns progress', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const wrapper = new SolverWithSteps()
    const [solution, progress] = wrapper.solveWithSteps(board)

    expect(solution).not.toBeNull()
    expect(progress.steps.length).toBeGreaterThan(0)
  })

  it('progress has valid steps', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const wrapper = new SolverWithSteps()
    const [, progress] = wrapper.solveWithSteps(board)

    for (const step of progress.steps) {
      expect(step.stepNumber).toBeGreaterThan(0)
      expect(step.explanation).toBeTruthy()
      expect(step.stepType).toBeTruthy()
    }
  })

  it('progress summary is readable', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const wrapper = new SolverWithSteps()
    const [, progress] = wrapper.solveWithSteps(board)

    const summary = progress.summary()
    expect(summary).toContain('Solving Progress')
  })
})
