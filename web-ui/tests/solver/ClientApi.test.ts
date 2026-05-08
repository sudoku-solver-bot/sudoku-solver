import { describe, it, expect } from 'vitest'
import { solve, validate, getCandidates } from '@/solver'

const EASY_PUZZLE = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
const EASY_SOLUTION = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
const HARD_PUZZLE = '.....6....59.....82....8....45........3........6..3.54...325..6..................'
const INVALID_SHORT = '123'
const INVALID_CHAR = 'X' + '.'.repeat(80)

describe('solve()', () => {
  it('solves an easy puzzle', () => {
    const result = solve(EASY_PUZZLE)
    expect(result).toBe(EASY_SOLUTION)
  })

  it('returns the solved 81-character string', () => {
    const result = solve(EASY_PUZZLE)
    expect(result).toHaveLength(81)
    expect(result).not.toContain('.')
  })

  it('solved puzzle contains only digits 1-9', () => {
    const result = solve(EASY_PUZZLE)
    expect(result).toMatch(/^[1-9]{81}$/)
  })

  it('returns null for unsolvable puzzle', () => {
    // A puzzle that is clearly contradictory
    const badPuzzle = '5' + '.'.repeat(8) + '5' + '.'.repeat(71)  // two 5s in row 0
    const result = solve(badPuzzle)
    expect(result).toBeNull()
  })

  it('returns solution for puzzle with zeros as empties', () => {
    const puzzle = '530070000600195000098000060800060003400803001700020006060000280000419005000080079'
    const result = solve(puzzle)
    expect(result).toBe(EASY_SOLUTION)
  })

  it('handles multi-line puzzle input', () => {
    const puzzle = '53..7....\n6..195...\n.98....6.\n8...6...3\n4..8.3..1\n7...2...6\n.6....28.\n...419..5\n....8..79'
    const result = solve(puzzle)
    expect(result).toBe(EASY_SOLUTION)
  })

  it('returns consistent solution for the same puzzle', () => {
    const a = solve(EASY_PUZZLE)
    const b = solve(EASY_PUZZLE)
    expect(a).toBe(b)
  })
})

describe('validate()', () => {
  it('validates a valid puzzle as valid', () => {
    const result = validate(EASY_PUZZLE)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('rejects puzzle shorter than 81 chars', () => {
    const result = validate(INVALID_SHORT)
    expect(result.valid).toBe(false)
    expect(result.error).toContain('exactly 81')
  })

  it('rejects puzzle with invalid characters', () => {
    const result = validate(INVALID_CHAR)
    expect(result.valid).toBe(false)
    expect(result.error).toContain('Invalid character')
  })

  it('rejects contradictory puzzle', () => {
    // Two 5s in row 0 makes it contradictory
    const result = validate('5' + '.'.repeat(8) + '5' + '.'.repeat(71))
    expect(result.valid).toBe(false)
  })

  it('validates zero-based puzzle correctly', () => {
    const result = validate('530070000600195000098000060800060003400803001700020006060000280000419005000080079')
    expect(result.valid).toBe(true)
  })

  it('rejects empty string', () => {
    const result = validate('')
    expect(result.valid).toBe(false)
  })
})

describe('getCandidates()', () => {
  it('returns candidates for a cell in an incomplete puzzle', () => {
    // Cell (0, 0) has '5' confirmed — should return [5]
    const result = getCandidates(EASY_PUZZLE, 0, 0)
    // Confirmed cell returns the confirmed value
    expect(result).toEqual([5])
  })

  it('returns empty array for invalid row/col but solvable cell', () => {
    // Cell (0, 2) is '.' in the puzzle (empty)
    const result = getCandidates(EASY_PUZZLE, 0, 2)
    expect(result.length).toBeGreaterThan(0) // has candidates
    expect(result.every(v => v >= 1 && v <= 9)).toBe(true)
  })

  it('returns subset of [1-9]', () => {
    const result = getCandidates(EASY_PUZZLE, 4, 4)
    for (const v of result) {
      expect(v).toBeGreaterThanOrEqual(1)
      expect(v).toBeLessThanOrEqual(9)
    }
  })
})
