import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { DeathBlossomCandidateEliminator } from '../src/Eliminators'

// Source: sudoku-solver-bot Kotlin test suite
// https://github.com/sudoku-solver-bot/sudoku-solver

const SOLVED = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'

describe('DeathBlossomCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new DeathBlossomCandidateEliminator().displayName).toBe('Death Blossom')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new DeathBlossomCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const board = BoardReader.fromString(SOLVED, Board)
    const changed = new DeathBlossomCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on various puzzles', () => {
    const eliminator = new DeathBlossomCandidateEliminator()
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5',
      '.....6....59.....82....8....45........3........6..3.54...325..6..................',
    ]
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
    }
  })

  it('can run multiple times safely (idempotent)', () => {
    const board = BoardReader.fromString(SOLVED, Board)
    const eliminator = new DeathBlossomCandidateEliminator()
    eliminator.eliminate(board)
    eliminator.eliminate(board)
    eliminator.eliminate(board)
    // Must not throw
  })

  it('does not modify confirmed cells', () => {
    const puzzle = '1'.padEnd(81, '.')
    const board = BoardReader.fromString(puzzle, Board)
    const coord = Coord.all[0]
    const initial = board.candidatePattern(coord)
    const eliminator = new DeathBlossomCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.candidatePattern(coord)).toBe(initial)
  })

  it('handles board after basic elimination', () => {
    const puzzle = '1...5...9.2..........8......7......3...........4......6..........................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new DeathBlossomCandidateEliminator()
    const result = eliminator.eliminate(board)
    expect(typeof result).toBe('boolean')
  })

  it('handles partial board with ALS in rows', () => {
    const puzzle = '1234.............................................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new DeathBlossomCandidateEliminator()
    eliminator.eliminate(board)
    // Must not throw
  })

  it('handles partial board with ALS in columns', () => {
    const puzzle = '1........2........3........4.....................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new DeathBlossomCandidateEliminator()
    eliminator.eliminate(board)
  })

  it('handles partial board with ALS in boxes', () => {
    const puzzle = '123.45...........................................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new DeathBlossomCandidateEliminator()
    eliminator.eliminate(board)
  })

  it('does not produce empty candidates', () => {
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5',
    ]
    const eliminator = new DeathBlossomCandidateEliminator()
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
      // No cells should have zero candidates
      for (const coord of Coord.all) {
        expect(board.candidatePattern(coord)).toBeGreaterThan(0)
      }
    }
  })
})
