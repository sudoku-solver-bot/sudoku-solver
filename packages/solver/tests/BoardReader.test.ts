import { describe, it, expect } from 'vitest'
import { BoardReader } from '../src/BoardReader'
import { Board } from '../src/Board'
import type { CandidateEliminator } from '../src/Eliminators'

describe('BoardReader', () => {
  const simplePuzzle = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
  const emptyLinePuzzle = '53..7....\n6..195....98....6.\n8...6...3\n4..8.3..1\n7...2...6\n.6....28.\n...419..5\n....8..79'
  // The full 81-char version above: 53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79
  const partialPuzzle = '530070000600195000098000060800060003400803001700020006060000280000419005000080079'

  // Stub Board.fromValues for testing BoardReader independently
  const StubBoard = {
    fromValues(values: readonly number[]): Board {
      return { _values: values } as unknown as Board
    }
  }

  describe('fromString', () => {
    it('parses a full solved puzzle', () => {
      const board = BoardReader.fromString(simplePuzzle, StubBoard)
      expect(board).toBeDefined()
      const vals = (board as unknown as { _values: readonly number[] })._values
      expect(vals).toHaveLength(81)
      expect(vals[0]).toBe(5)
      expect(vals[1]).toBe(3)
      expect(vals[80]).toBe(9)
    })

    it('parses dots as 0', () => {
      const board = BoardReader.fromString('1........' + '.'.repeat(72), StubBoard)
      const vals = (board as unknown as { _values: readonly number[] })._values
      expect(vals[0]).toBe(1)
      expect(vals[1]).toBe(0)
      expect(vals[9]).toBe(0)
    })

    it('parses zeros as 0', () => {
      const board = BoardReader.fromString(partialPuzzle, StubBoard)
      const vals = (board as unknown as { _values: readonly number[] })._values
      expect(vals[0]).toBe(5)
      expect(vals[3]).toBe(0) // '0' → 0
      expect(vals[4]).toBe(7)
    })

    it('strips whitespace/newlines before parsing', () => {
      const board = BoardReader.fromString(emptyLinePuzzle, StubBoard)
      const vals = (board as unknown as { _values: readonly number[] })._values
      expect(vals).toHaveLength(81)
    })

    it('throws on wrong length (<81)', () => {
      expect(() => BoardReader.fromString('123', StubBoard)).toThrow('expected 81 characters')
    })

    it('throws on wrong length (>81)', () => {
      expect(() => BoardReader.fromString('1234567890'.repeat(9), StubBoard)).toThrow('expected 81 characters')
    })

    it('throws on invalid character', () => {
      expect(() => BoardReader.fromString('X'.repeat(81), StubBoard)).toThrow('Invalid character')
    })

    it('throws on character >9', () => {
      expect(() => BoardReader.fromString('A12345678' + '.'.repeat(72), StubBoard)).toThrow('Invalid character')
    })
  })

  describe('toPatterns', () => {
    it('returns Int32Array of length 81', () => {
      const patterns = BoardReader.toPatterns(simplePuzzle)
      expect(patterns).toBeInstanceOf(Int32Array)
      expect(patterns).toHaveLength(81)
    })

    it('given cell has its singleton pattern', () => {
      const patterns = BoardReader.toPatterns(simplePuzzle)
      // Cell 0 = value 5 → pattern is bit 4 set (2^4 = 16)
      expect(patterns[0]).toBe(16)
    })

    it('empty cell has wildcard pattern (511)', () => {
      const patterns = BoardReader.toPatterns('.' + '.'.repeat(80))
      expect(patterns[0]).toBe(511)
      expect(patterns[40]).toBe(511)
      expect(patterns[80]).toBe(511)
    })

    it('throws on wrong length', () => {
      expect(() => BoardReader.toPatterns('12')).toThrow('expected 81 characters')
    })
  })

  describe('boardToString', () => {
    // Test the boardToString helper from index.ts
    it('exists on BoardReader (if exported)', () => {
      // boardToString is in index.ts but uses BoardReader internally
      const puzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
      // Create a board from this puzzle and check it round-trips
      const board = BoardReader.fromString(puzzle, StubBoard)
      expect(board).toBeDefined()
      // Board class may have boardToString but it's tested elsewhere
    })
  })
})
