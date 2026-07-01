import { describe, it, expect } from 'vitest'
import { solve } from '../src/index'

// ---------------------------------------------------------------------------
// Solver Parity Tests (ADR-0010)
//
// These tests verify the TypeScript solver produces solutions identical to
// the Kotlin solver. Run by CI solver-parity workflow on every PR.
//
// Section A: Kotlin-verified puzzles — solutions from Kotlin .solution files.
// Section B: Extended corpus — JSON-driven TS-snapshot puzzles for regression.
// ---------------------------------------------------------------------------

// ── A. Kotlin-verified puzzles ────────────────────────────────────────────
// Each puzzle has a companion .solution file in
// solver/src/test/resources/solver/www.sudokuweb.org/

const G1_PUZZLE = '.4.3.81..21..65...6......7.9.3.467811.48295.68.5....2.4.....6.3...6.2.47.8..3....'
const G1_SOLUTION = '549378162217465398638291475923546781174829536865713924452987613391652847786134259'

const G2_PUZZLE = '1..496..23.6.1.7...8...31.6..5.6...8.63.85..9...3.45.16.2...9.48..6.9.5.5.982.6..'
const G2_SOLUTION = '157496832396218745284753196415962378763185429928374561672531984831649257549827613'

const G3_PUZZLE = '.6...5....7......1....634....3.8....21..9...54....78....16...84.......5.8...4.61.'
const G3_SOLUTION = '962415378374928561185763429753284196218396745496157832531672984649831257827549613'

const G4_PUZZLE = '7...4.2.....52...6......5...7....96..6.....8.425...........9.31..4..7...1..6.....'
const G4_SOLUTION = '756841293849523176213796548378214965961375482425968317682459731534187629197632854'

const EASY_PUZZLE = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
const EASY_SOLUTION = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'

const MULTI_SOLUTION_PUZZLE = '438.....9..16....3...73.........9.6.8..1..3...76.2....1...4279692...6.3.....17...'

// ── B. Extended corpus from parity/puzzles.json ────────────────────────────
import corpus from './parity/puzzles.json'

describe('Kotlin parity — TS solver matches Kotlin solver solutions', () => {
  it('g1 (easy) matches Kotlin solution', () => {
    expect(solve(G1_PUZZLE)).toBe(G1_SOLUTION)
  })

  it('g2 (medium) matches Kotlin solution', () => {
    expect(solve(G2_PUZZLE)).toBe(G2_SOLUTION)
  })

  it('g3 (medium) matches Kotlin solution', () => {
    expect(solve(G3_PUZZLE)).toBe(G3_SOLUTION)
  })

  it('g4 (hard) matches Kotlin solution', () => {
    expect(solve(G4_PUZZLE)).toBe(G4_SOLUTION)
  })

  it('well-known easy puzzle matches Kotlin solution', () => {
    expect(solve(EASY_PUZZLE)).toBe(EASY_SOLUTION)
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
    const result = solve(MULTI_SOLUTION_PUZZLE)
    expect(result).not.toBeNull()
    expect(result).toHaveLength(81)
    expect(result).toMatch(/^[1-9]{81}$/)
  })

  it('TS solver returns null for contradictory puzzle', () => {
    expect(solve('5' + '.'.repeat(8) + '5' + '.'.repeat(71))).toBeNull()
  })
})

describe('Extended parity corpus — TS solver regression guard', () => {
  const solved = corpus.filter((p: { solution: string | null }) => p.solution !== null)

  it(`corpus contains ${solved.length} solvable puzzles (target: 50+)`, () => {
    // Target: 50+ unique puzzles. Each addition is a data change in puzzles.json.
    expect(solved.length).toBeGreaterThanOrEqual(37)
  })

  for (const entry of solved) {
    it(`[${entry.difficulty}] ${entry.name} matches expected solution`, { timeout: 30000 }, () => {
      const result = solve(entry.puzzle)
      expect(result).not.toBeNull()
      expect(result).toHaveLength(81)
      expect(result).toMatch(/^[1-9]{81}$/)
      expect(result).toBe(entry.solution)
    })
  }

  // Unsolvable/edge-case puzzles must return null
  const unsolved = corpus.filter((p: { solution: string | null }) => p.solution === null)
  for (const entry of unsolved) {
    it(`[${entry.difficulty}] ${entry.name} returns null (no unique solution)`, () => {
      const result = solve(entry.puzzle)
      expect(result).toBeNull()
    })
  }
})
