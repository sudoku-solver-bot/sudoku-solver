import { describe, it, expect } from 'vitest'
import { StepType, STEP_TYPE_DESCRIPTIONS, stepTypeFromTechniqueName } from '../src/StepType'
import { SolvingStep, cellFilled, candidateEliminated, guessMade, backtrack, techniqueApplied, formatStep } from '../src/SolvingStep'
import { SolvingProgress } from '../src/SolvingProgress'
import { Level, LEVEL_NAMES, rate, rateLevel, isHard } from '../src/DifficultyRater'
import { emptyMetrics } from '../src/SolvingProgress'
import { Coord } from '../src/Coord'

describe('StepType', () => {
  it('has all 21 values', () => {
    expect(Object.values(StepType)).toHaveLength(21)
  })

  it('has descriptions for all types', () => {
    for (const st of Object.values(StepType)) {
      expect(STEP_TYPE_DESCRIPTIONS[st as StepType]).toBeTruthy()
    }
  })

  it('stepTypeFromTechniqueName matches exact names', () => {
    expect(stepTypeFromTechniqueName('X-Wing')).toBe(StepType.X_WING)
    expect(stepTypeFromTechniqueName('Hidden Single')).toBe(StepType.HIDDEN_SINGLE)
    expect(stepTypeFromTechniqueName('Naked Pair')).toBe(StepType.NAKED_PAIR)
  })

  it('stepTypeFromTechniqueName normalizes names', () => {
    expect(stepTypeFromTechniqueName('XWing')).toBe(StepType.X_WING)
    expect(stepTypeFromTechniqueName('XYWing')).toBe(StepType.XY_WING)
  })

  it('stepTypeFromTechniqueName falls back to TECHNIQUE_APPLIED', () => {
    expect(stepTypeFromTechniqueName('Unknown Technique')).toBe(StepType.TECHNIQUE_APPLIED)
  })
})

describe('SolvingStep', () => {
  it('cellFilled creates correct step', () => {
    const coord = Coord.all[0]
    const step = cellFilled(1, coord, 5, 'Test fill')
    expect(step.stepNumber).toBe(1)
    expect(step.stepType).toBe(StepType.CELL_FILLED)
    expect(step.affectedCells).toHaveLength(1)
    expect(step.values.has(5)).toBe(true)
    expect(step.explanation).toBe('Test fill')
  })

  it('candidateEliminated creates correct step', () => {
    const coord = Coord.all[5]
    const step = candidateEliminated(2, coord, 3, StepType.X_WING, 'Eliminated 3')
    expect(step.stepType).toBe(StepType.X_WING)
    expect(step.values.has(3)).toBe(true)
  })

  it('guessMade creates correct step', () => {
    const coord = Coord.all[10]
    const step = guessMade(3, coord, 7, 'Guessing 7')
    expect(step.stepType).toBe(StepType.GUESS_MADE)
  })

  it('backtrack creates correct step', () => {
    const coord = Coord.all[10]
    const step = backtrack(4, coord, 7, 'Wrong guess')
    expect(step.stepType).toBe(StepType.BACKTRACK)
  })

  it('formatStep produces readable output', () => {
    const coord = Coord.all[0]
    const step = cellFilled(1, coord, 5, 'Test')
    const formatted = formatStep(step)
    expect(formatted).toContain('Step 1')
    expect(formatted).toContain('Cell Filled')
  })
})

describe('SolvingProgress', () => {
  it('creates from puzzle', () => {
    const progress = new SolvingProgress('123456789'.repeat(9))
    expect(progress.originalPuzzle).toHaveLength(81)
    expect(progress.steps).toHaveLength(0)
    expect(progress.isSolved).toBe(false)
  })

  it('tracks steps', () => {
    const progress = new SolvingProgress('123456789'.repeat(9))
    progress.addStep(cellFilled(1, Coord.all[0], 5, 'Test'))
    expect(progress.steps).toHaveLength(1)
    expect(progress.nextStepNumber()).toBe(2)
  })

  it('marks solved', () => {
    const progress = new SolvingProgress('123456789'.repeat(9))
    progress.markSolved('123456789'.repeat(9))
    expect(progress.isSolved).toBe(true)
    expect(progress.solveTimeMs()).not.toBeNull()
  })

  it('marks no solution', () => {
    const progress = new SolvingProgress('123456789'.repeat(9))
    progress.markNoSolution('Contradiction found')
    expect(progress.hasNoSolution).toBe(true)
  })

  it('produces summary', () => {
    const progress = new SolvingProgress('123456789'.repeat(9))
    progress.addStep(cellFilled(1, Coord.all[0], 5, 'Test'))
    const summary = progress.summary()
    expect(summary).toContain('Solving Progress')
    expect(summary).toContain('Steps: 1')
  })
})

describe('DifficultyRater', () => {
  it('Level enum has 6 values', () => {
    // Numeric enums have reverse mappings, so filter to string keys only
    const keys = Object.keys(Level).filter(k => isNaN(Number(k)))
    expect(keys).toHaveLength(6)
  })

  it('LEVEL_NAMES covers all levels', () => {
    expect(LEVEL_NAMES[Level.EASY]).toBe('Easy')
    expect(LEVEL_NAMES[Level.MASTER]).toBe('Master')
  })

  it('empty metrics → EASY', () => {
    const result = rate(emptyMetrics())
    expect(result.level).toBe(Level.EASY)
    expect(result.backtracking).toBe(false)
  })

  it('backtracking → MASTER', () => {
    const metrics = emptyMetrics()
    metrics.backtrackingCount = 1
    const result = rate(metrics)
    expect(result.level).toBe(Level.MASTER)
    expect(result.backtracking).toBe(true)
  })

  it('rateLevel returns just the level', () => {
    expect(rateLevel(emptyMetrics())).toBe(Level.EASY)
  })

  it('isHard returns false for easy', () => {
    expect(isHard(emptyMetrics())).toBe(false)
  })
})
