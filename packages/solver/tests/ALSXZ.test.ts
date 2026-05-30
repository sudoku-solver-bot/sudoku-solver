import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { ALSXZCandidateEliminator } from '../src/Eliminators'

// Source: sudoku-solver-bot Kotlin test suite
// https://github.com/sudoku-solver-bot/sudoku-solver

const SOLVED = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'

describe('ALSXZCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new ALSXZCandidateEliminator().displayName).toBe('ALS-XZ')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new ALSXZCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const board = BoardReader.fromString(SOLVED, Board)
    const changed = new ALSXZCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on partial board', () => {
    const puzzle = '1'.padEnd(81, '.')
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('does not modify confirmed cells', () => {
    const puzzle = '1'.padEnd(81, '.')
    const board = BoardReader.fromString(puzzle, Board)
    const coord = Coord.all[0]
    const initial = board.candidatePattern(coord)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.candidatePattern(coord)).toBe(initial)
  })

  it('can run multiple times safely (idempotent)', () => {
    const board = BoardReader.fromString(SOLVED, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    eliminator.eliminate(board)
    eliminator.eliminate(board)
    // Must not throw
  })

  it('handles complex board state', () => {
    // Fill middle rows to create ALS potential
    const puzzle = '.............................................123......456......789...............'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('handles ALS detection in rows', () => {
    const puzzle = '1234.............................................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('handles ALS detection in columns', () => {
    const puzzle = '1........2........3........4.....................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('handles ALS detection in boxes', () => {
    const puzzle = '12..34...........................................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('handles board with ALS candidates', () => {
    // Create a partial fill in top rows
    const puzzle = '12345....45678....78912..........................................................'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('handles edge cases gracefully', () => {
    // Center + corners filled
    const puzzle =
      '1.......9' +
      '.........' +
      '.........' +
      '.........' +
      '....5....' +
      '.........' +
      '.........' +
      '.........' +
      '3.......7'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new ALSXZCandidateEliminator()
    eliminator.eliminate(board)
    expect(board.isValid()).toBe(true)
  })

  it('does not produce empty candidates', () => {
    const puzzles = [
      '1234.............................................................................',
      '12..34...........................................................................',
      '1.......9.........5.........7.........3.................................9.......5',
    ]
    const eliminator = new ALSXZCandidateEliminator()
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
      for (const coord of Coord.all) {
        expect(board.candidatePattern(coord)).toBeGreaterThan(0)
      }
    }
  })
})
