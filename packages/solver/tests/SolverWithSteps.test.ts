import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { SolverWithSteps } from '../src/SolverWithSteps'
import { SolverConfig } from '../src/Solver'

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

  it('accepts SolverConfig', () => {
    const board = BoardReader.fromString(HARD_PUZZLE, Board)
    const config = new SolverConfig()
    const wrapper = new SolverWithSteps(config)
    const [solution, progress] = wrapper.solveWithSteps(board)

    expect(solution).not.toBeNull()
    expect(progress.steps.length).toBeGreaterThan(0)
  })

  it('static solveWithSteps from puzzle string', () => {
    const [solution, progress] = SolverWithSteps.solveWithSteps(HARD_PUZZLE)

    expect(solution).not.toBeNull()
    expect(progress.steps.length).toBeGreaterThan(0)
    expect(solution!.isValid()).toBe(true)
  })

  it('static solveWithSteps with config', () => {
    const config = new SolverConfig()
    const [solution, progress] = SolverWithSteps.solveWithSteps(HARD_PUZZLE, config)

    expect(solution).not.toBeNull()
    expect(progress.steps.length).toBeGreaterThan(0)
  })
})
