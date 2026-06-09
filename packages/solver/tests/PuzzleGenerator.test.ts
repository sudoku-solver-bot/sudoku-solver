import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { Coord } from '../src/Coord'
import { Solver } from '../src/Solver'
import { Level } from '../src/DifficultyRater'
import { generate } from '../src/PuzzleGenerator'

describe('PuzzleGenerator', () => {
  it('generates a valid easy puzzle', () => {
    const puzzle = generate(Level.EASY, 42)
    const solver = new Solver()
    const solution = solver.solve(puzzle.copy())
    expect(solution).not.toBeNull()
  }, 15000)

  it('generates a valid medium puzzle', () => {
    const puzzle = generate(Level.MEDIUM, 42)
    const solver = new Solver()
    const solution = solver.solve(puzzle.copy())
    expect(solution).not.toBeNull()
  }, 15000)

  it('seed produces reproducible puzzles', () => {
    const p1 = generate(Level.MEDIUM, 123)
    const p2 = generate(Level.MEDIUM, 123)
    for (let i = 0; i < 81; i++) {
      expect(p1.candidatePatterns[i]).toBe(p2.candidatePatterns[i])
    }
  }, 15000)

  it('different seeds produce different puzzles', () => {
    const p1 = generate(Level.MEDIUM, 1)
    const p2 = generate(Level.MEDIUM, 2)
    const same = Array.from({ length: 81 }, (_, i) => p1.candidatePatterns[i] === p2.candidatePatterns[i])
      .every(Boolean)
    expect(same).toBe(false)
  }, 15000)

  it('generated puzzles have confirmed cells', () => {
    const puzzle = generate(Level.MEDIUM, 42)
    let confirmed = 0
    for (const c of Coord.all) {
      if (puzzle.isConfirmed(c)) confirmed++
    }
    // Medium should have ~36 cells (81 - 45)
    expect(confirmed).toBeGreaterThan(20)
    expect(confirmed).toBeLessThan(60)
  }, 15000)
})
