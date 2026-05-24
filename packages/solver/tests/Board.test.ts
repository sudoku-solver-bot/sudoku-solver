import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { WILDCARD_PATTERN, MASKS, bitCount } from '../src/Bitmask'
import { Coord } from '../src/Coord'
import { CoordGroup } from '../src/CoordGroup'

describe('Board', () => {
  // ---------------------------------------------------------------------------
  // 1. Empty board has all candidates (WILDCARD_PATTERN) for empty cells
  // ---------------------------------------------------------------------------
  describe('empty board', () => {
    it('has wildcard pattern (511) in all 81 cells', () => {
      const board = Board.empty()
      for (let i = 0; i < 81; i++) {
        expect(
          board.candidatePatterns[i],
          `cell ${i} should be WILDCARD_PATTERN`
        ).toBe(WILDCARD_PATTERN)
      }
    })

    it('each cell has 9 candidates', () => {
      const board = Board.empty()
      for (const c of Coord.all) {
        expect(bitCount(board.candidatePatterns[c.index])).toBe(9)
      }
    })

    it('no cell is confirmed on empty board', () => {
      const board = Board.empty()
      for (const c of Coord.all) {
        expect(board.isConfirmed(c)).toBe(false)
      }
    })

    it('countTotalCandidates returns 729 (81 × 9)', () => {
      const board = Board.empty()
      expect(board.countTotalCandidates()).toBe(729)
    })
  })

  // ---------------------------------------------------------------------------
  // 2. Setting a value collapses candidates
  // ---------------------------------------------------------------------------
  describe('setting values', () => {
    it('markValue sets the cell to the exact singleton mask', () => {
      const board = Board.empty()
      const coord = Coord.all[0]
      board.markValue(coord, 5)
      expect(board.candidatePatterns[coord.index]).toBe(MASKS[4]) // value 5 = mask[4]
    })

    it('markValue makes the cell confirmed', () => {
      const board = Board.empty()
      const coord = Coord.all[42]
      board.markValue(coord, 9)
      expect(board.isConfirmed(coord)).toBe(true)
      expect(board.value(coord)).toBe(9)
    })

    it('fromValues creates board with correct patterns for a known easy puzzle', () => {
      // Easy puzzle: 24 givens
      const values = [
        0, 0, 7, 0, 0, 8, 0, 0, 6,
        0, 0, 3, 0, 0, 2, 0, 4, 0,
        1, 0, 0, 0, 9, 0, 0, 0, 0,
        0, 0, 0, 0, 5, 0, 8, 0, 0,
        0, 4, 0, 0, 0, 0, 0, 5, 0,
        0, 0, 1, 0, 2, 0, 0, 0, 0,
        0, 0, 0, 0, 6, 0, 0, 0, 7,
        0, 9, 0, 3, 0, 0, 2, 0, 0,
        8, 0, 0, 4, 0, 0, 1, 0, 0
      ]
      const board = Board.fromValues(values)
      for (let i = 0; i < 81; i++) {
        if (values[i] === 0) {
          expect(board.candidatePatterns[i]).toBe(WILDCARD_PATTERN)
        } else {
          expect(board.candidatePatterns[i]).toBe(MASKS[values[i] - 1])
        }
      }
    })

    it('fromValues does not validate input length (short array produces 0 patterns)', () => {
      // fromValues creates Int32Array(81) and iterates 81 times.
      // Missing entries → undefined → assigned as 0 to Int32Array.
      // Only the constructor validates length (81).
      const board = Board.fromValues([1, 2, 3])
      expect(board.candidatePatterns[0]).toBe(MASKS[0])  // value 1
      expect(board.candidatePatterns[1]).toBe(MASKS[1])  // value 2
      expect(board.candidatePatterns[2]).toBe(MASKS[2])  // value 3
      expect(board.candidatePatterns[3]).toBe(0) // undefined → 0 in Int32Array
    })

    it('constructor throws for wrong length', () => {
      expect(() => new Board(new Int32Array(80))).toThrow('Expected 81 cells')
    })
  })

  // ---------------------------------------------------------------------------
  // 3. Copy produces independent board
  // ---------------------------------------------------------------------------
  describe('copy', () => {
    it('copy equals the original', () => {
      const board = Board.empty()
      board.markValue(Coord.all[0], 5)
      const copy = board.copy()
      expect(copy.equals(board)).toBe(true)
    })

    it('modifying copy does not affect original', () => {
      const board = Board.empty()
      board.markValue(Coord.all[0], 5)
      const copy = board.copy()
      copy.markValue(Coord.all[1], 3)

      // Original should still have wildcard at cell 1
      expect(board.candidatePatterns[1]).toBe(WILDCARD_PATTERN)
      // Copy should have value 3 at cell 1
      expect(copy.candidatePatterns[1]).toBe(MASKS[2])
    })

    it('modifying original after copy does not affect copy', () => {
      const board = Board.empty()
      const copy = board.copy()
      board.markValue(Coord.all[10], 7)

      expect(copy.candidatePatterns[10]).toBe(WILDCARD_PATTERN)
    })

    it('copy shares no array reference with original', () => {
      const board = Board.empty()
      const copy = board.copy()
      expect(copy.candidatePatterns).not.toBe(board.candidatePatterns)
    })
  })

  // ---------------------------------------------------------------------------
  // 4. Peer calculation: cell (0,0) has 20 peers
  // ---------------------------------------------------------------------------
  describe('peers', () => {
    /**
     * Compute all peer coordinates for a given cell.
     * Peers = same row + same column + same region, excluding the cell itself.
     */
    function peersOf(coord: Coord): Coord[] {
      const peerSet = new Set<number>()
      const selfIdx = coord.index

      // Same row
      for (const c of CoordGroup.horizontal[coord.row].coords) {
        if (c.index !== selfIdx) peerSet.add(c.index)
      }
      // Same column
      for (const c of CoordGroup.vertical[coord.col].coords) {
        if (c.index !== selfIdx) peerSet.add(c.index)
      }
      // Same region
      for (const c of CoordGroup.region[coord.region].coords) {
        if (c.index !== selfIdx) peerSet.add(c.index)
      }

      return Array.from(peerSet).map(i => Coord.all[i])
    }

    it('cell (0,0) has 20 peers', () => {
      const peers = peersOf(Coord.all[0])
      expect(peers).toHaveLength(20)
    })

    it('cell (4,4) (center) has 20 peers', () => {
      const center = Coord.all[40] // row 4, col 4
      const peers = peersOf(center)
      expect(peers).toHaveLength(20)
    })

    it('cell (8,8) (bottom-right) has 20 peers', () => {
      const corner = Coord.all[80] // row 8, col 8
      const peers = peersOf(corner)
      expect(peers).toHaveLength(20)
    })

    it('no peer includes the cell itself', () => {
      for (const c of Coord.all) {
        const peers = peersOf(c)
        const selfInPeers = peers.some(p => p.index === c.index)
        expect(selfInPeers).toBe(false)
      }
    })

    it('peers include 8 row-mates, 8 col-mates, and 8 region-mates total', () => {
      // For a corner cell, the breakdown is:
      // - 8 row-mates (entire row except self): 2 share region, 6 don't
      // - 8 col-mates (entire col except self): 2 share region, 6 don't
      // - 4 additional unique cells from the box (not in same row or col)
      // Total region-mates = 8 (2 row + 2 col + 4 unique box-only)
      const corner = Coord.all[0]
      const peers = peersOf(corner)
      const rowMates = peers.filter(p => p.row === corner.row)
      const colMates = peers.filter(p => p.col === corner.col)
      const regionMates = peers.filter(p => p.region === corner.region)
      // The 4 box-only peers: in region 0, not row 0, not col 0
      const boxOnly = peers.filter(
        p => p.region === corner.region && p.row !== corner.row && p.col !== corner.col
      )

      expect(rowMates).toHaveLength(8)
      expect(colMates).toHaveLength(8)
      expect(regionMates).toHaveLength(8) // 2 row + 2 col + 4 box-only
      expect(boxOnly).toHaveLength(4) // the unique box contributions
      expect(peers).toHaveLength(20) // 8 + 8 + 4 = 20
    })
  })

  // ---------------------------------------------------------------------------
  // 5. Candidate count tracking
  // ---------------------------------------------------------------------------
  describe('candidate count tracking', () => {
    it('countTotalCandidates starts at 729 for empty board', () => {
      const board = Board.empty()
      expect(board.countTotalCandidates()).toBe(729)
    })

    it('countTotalCandidates decreases when a value is set', () => {
      const board = Board.empty()
      board.markValue(Coord.all[0], 1)
      // Was 729; now cell 0 has 1 candidate, 80 cells × 9 = 720, + 1 = 721
      expect(board.countTotalCandidates()).toBe(721)
    })

    it('countTotalCandidates is 81 for a fully solved board', () => {
      // Simple solved puzzle (all 1-9 in each row, 81 givens)
      const solvedValues = [
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
      ]
      const board = Board.fromValues(solvedValues)
      expect(board.countTotalCandidates()).toBe(81)
    })

    it('candidateValues returns all values from 1-9 for empty cell', () => {
      const board = Board.empty()
      const values = board.candidateValues(Coord.all[5])
      expect(values).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9])
    })

    it('candidateValues returns single value for confirmed cell', () => {
      const board = Board.empty()
      board.markValue(Coord.all[5], 7)
      const values = board.candidateValues(Coord.all[5])
      expect(values).toEqual([7])
    })

    it('isSolved returns false for empty board', () => {
      const board = Board.empty()
      expect(board.isSolved()).toBe(false)
    })

    it('isSolved returns true for solved board', () => {
      const solvedValues = [
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
      ]
      const board = Board.fromValues(solvedValues)
      expect(board.isSolved()).toBe(true)
    })

    it('isValid returns true for empty board', () => {
      const board = Board.empty()
      expect(board.isValid()).toBe(true)
    })

    it('isValid returns true for partially filled board', () => {
      const board = Board.empty()
      board.markValue(Coord.all[0], 5)
      board.markValue(Coord.all[1], 3)
      expect(board.isValid()).toBe(true)
    })
  })

  // ---------------------------------------------------------------------------
  // 6. Solver helpers
  // ---------------------------------------------------------------------------
  describe('solver helpers', () => {
    it('unresolvedCoord returns a cell on empty board', () => {
      const board = Board.empty()
      const coord = board.unresolvedCoord()
      expect(coord).toBeDefined()
      expect(board.isConfirmed(coord!)).toBe(false)
    })

    it('unresolvedCoord returns undefined on solved board', () => {
      const solvedValues = [
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
      ]
      const board = Board.fromValues(solvedValues)
      expect(board.unresolvedCoord()).toBeUndefined()
    })

    it('unresolvedCoord picks MRV cell (fewest candidates)', () => {
      const board = Board.empty()
      // Give most cells 9 candidates, but cell 42 only 2 candidates
      board.candidatePatterns[42] = MASKS[2] | MASKS[7] // values 3 and 8
      const coord = board.unresolvedCoord()
      expect(coord!.index).toBe(42)
    })

    it('eraseCandidateValue removes a candidate', () => {
      const board = Board.empty()
      const cell = Coord.all[10]
      const before = bitCount(board.candidatePatterns[cell.index])
      board.eraseCandidateValue(cell, 5)
      const after = bitCount(board.candidatePatterns[cell.index])
      expect(after).toBe(before - 1)
    })

    it('eraseCandidateValue returns true when candidate was present', () => {
      const board = Board.empty()
      const changed = board.eraseCandidateValue(Coord.all[0], 3)
      expect(changed).toBe(true)
    })

    it('eraseCandidateValue returns false when candidate already absent', () => {
      const board = Board.empty()
      // Remove 5 first
      board.eraseCandidateValue(Coord.all[0], 5)
      // Try again
      const changed = board.eraseCandidateValue(Coord.all[0], 5)
      expect(changed).toBe(false)
    })
  })

  // ---------------------------------------------------------------------------
  // 7. Display and symbol
  // ---------------------------------------------------------------------------
  describe('display', () => {
    it("symbolAt returns '.' for empty cell", () => {
      const board = Board.empty()
      expect(board.symbolAt(Coord.all[0])).toBe('.')
    })

    it('symbolAt returns correct digit for confirmed cell', () => {
      const board = Board.empty()
      board.markValue(Coord.all[0], 7)
      expect(board.symbolAt(Coord.all[0])).toBe('7')
    })

    it('toString produces non-empty string', () => {
      const board = Board.empty()
      const str = board.toString()
      expect(str.length).toBeGreaterThan(0)
      expect(str).toContain('...')
    })
  })
})
