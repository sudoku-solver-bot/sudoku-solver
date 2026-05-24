import { describe, it, expect } from 'vitest'
import { solve } from '../src/index'

// ---------------------------------------------------------------------------
// Puzzle/solution pairs verified against the Kotlin solver.
// Source: solver/src/test/resources/solver/www.sudokuweb.org/
// Each puzzle has a companion .solution file that the Kotlin SolverTest
// asserts against. These parity tests verify the TS solver produces
// identical solutions.
// ---------------------------------------------------------------------------

// From existing ClientApi.test.ts — verified against Kotlin
const EASY_PUZZLE = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
const EASY_SOLUTION = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'

// From solver/src/test/resources/solver/www.sudokuweb.org/g1
const G1_PUZZLE = '.4.3.81..21..65...6......7.9.3.467811.48295.68.5....2.4.....6.3...6.2.47.8..3....'
const G1_SOLUTION = '549378162217465398638291475923546781174829536865713924452987613391652847786134259'

// From solver/src/test/resources/solver/www.sudokuweb.org/g2
const G2_PUZZLE = '1..496..23.6.1.7...8...31.6..5.6...8.63.85..9...3.45.16.2...9.48..6.9.5.5.982.6..'
const G2_SOLUTION = '157496832396218745284753196415962378763185429928374561672531984831649257549827613'

// From solver/src/test/resources/solver/www.sudokuweb.org/g3
const G3_PUZZLE = '.6...5....7......1....634....3.8....21..9...54....78....16...84.......5.8...4.61.'
const G3_SOLUTION = '962415378374928561185763429753284196218396745496157832531672984649831257827549613'

// From solver/src/test/resources/solver/www.sudokuweb.org/g4
const G4_PUZZLE = '7...4.2.....52...6......5...7....96..6.....8.425...........9.31..4..7...1..6.....'
const G4_SOLUTION = '756841293849523176213796548378214965961375482425968317682459731534187629197632854'

// From SolverTest.kt regression test for #256 — multi-solution puzzle
const MULTI_SOLUTION_PUZZLE = '438.....9..16....3...73.........9.6.8..1..3...76.2....1...4279692...6.3.....17...'

describe('Kotlin parity — TS solver matches Kotlin solver solutions', () => {
  it('easy puzzle (g1) matches Kotlin solution', () => {
    const result = solve(G1_PUZZLE)
    expect(result).toBe(G1_SOLUTION)
  })

  it('medium puzzle (g2) matches Kotlin solution', () => {
    const result = solve(G2_PUZZLE)
    expect(result).toBe(G2_SOLUTION)
  })

  it('medium puzzle (g3) matches Kotlin solution', () => {
    const result = solve(G3_PUZZLE)
    expect(result).toBe(G3_SOLUTION)
  })

  it('hard puzzle (g4) matches Kotlin solution', () => {
    const result = solve(G4_PUZZLE)
    expect(result).toBe(G4_SOLUTION)
  })

  it('well-known easy puzzle matches Kotlin solution', () => {
    const result = solve(EASY_PUZZLE)
    expect(result).toBe(EASY_SOLUTION)
  })

  it('solutions are 81-char strings of digits 1-9', () => {
    const puzzles = [G1_PUZZLE, G2_PUZZLE, G3_PUZZLE, G4_PUZZLE, EASY_PUZZLE]
    for (const puzzle of puzzles) {
      const result = solve(puzzle)
      expect(result).toHaveLength(81)
      expect(result).toMatch(/^[1-9]{81}$/)
    }
  })

  it('TS solver finds a solution for multi-solution puzzle (#256 regression)', () => {
    // Kotlin SolverTest.kt line 30-45: Solver should find a solution
    // for puzzles with multiple solutions, not return null.
    const result = solve(MULTI_SOLUTION_PUZZLE)
    expect(result).not.toBeNull()
    expect(result).toHaveLength(81)
    expect(result).toMatch(/^[1-9]{81}$/)
  })

  it('TS solver returns null for contradictory puzzle', () => {
    // Same behavior as Kotlin: null for unsolvable
    const badPuzzle = '5' + '.'.repeat(8) + '5' + '.'.repeat(71)
    const result = solve(badPuzzle)
    expect(result).toBeNull()
  })
})
