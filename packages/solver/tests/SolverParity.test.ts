import { describe, it, expect } from 'vitest'
import { solve } from '../src/index'
import corpus from './parity/puzzles.json'

// ---------------------------------------------------------------------------
// ADR-0010: TS/Kotlin solver parity test corpus.
//
// Each entry in parity/puzzles.json has been solved by the Kotlin solver.
// The TS solver must produce identical solutions for all solvable puzzles
// and must return null for contradictory/invalid puzzles.
//
// Tests run sequentially (single test case per puzzle category) to avoid
// OOM from parallel puzzle solving.
// ---------------------------------------------------------------------------

interface PuzzleEntry {
  name: string
  puzzle: string
  solution: string | null
  difficulty: string
  source: string
}

const puzzles = (corpus as { puzzles: PuzzleEntry[] }).puzzles
const solvable = puzzles.filter(p => p.solution !== null)
const unsolvable = puzzles.filter(p => p.solution === null)

describe('Solver Parity — TS solver matches Kotlin solver (ADR-0010)', () => {
  it(`corpus has 50+ puzzles (got ${puzzles.length})`, () => {
    expect(puzzles.length).toBeGreaterThanOrEqual(50)
  })

  it(`all ${solvable.length} solvable puzzles match Kotlin solutions`, async () => {
    const failures: string[] = []
    for (const { name, puzzle, solution } of solvable) {
      const result = solve(puzzle)
      // Yield to event loop periodically to prevent vitest worker IPC timeouts
      await new Promise(r => setTimeout(r, 0))
      if (result !== solution) {
        failures.push(`${name}: TS solution diverges from Kotlin`)
      } else {
        // Verify solution format
        expect(result, `${name}: solution must be 81 chars`).toHaveLength(81)
        expect(result, `${name}: solution must be digits 1-9`).toMatch(/^[1-9]{81}$/)
      }
    }
    if (failures.length > 0) {
      throw new Error(`Parity failures:\n${failures.join('\n')}`)
    }
  })

  it('finds a solution for multi-solution puzzle (#256 regression)', () => {
    const multi = puzzles.find(p => p.name === 'multi-solution')!
    const result = solve(multi.puzzle)
    expect(result).not.toBeNull()
    expect(result).toHaveLength(81)
    expect(result).toMatch(/^[1-9]{81}$/)
  })

  it(`all ${unsolvable.length} unsolvable puzzles return null`, () => {
    for (const { name, puzzle } of unsolvable) {
      const result = solve(puzzle)
      expect(result, `${name}: expected null for unsolvable puzzle`).toBeNull()
    }
  })
})
