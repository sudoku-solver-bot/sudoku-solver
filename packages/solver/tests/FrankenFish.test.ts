import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { FrankenFishCandidateEliminator } from '../src/Eliminators'

describe('FrankenFishCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new FrankenFishCandidateEliminator().displayName).toBe('Franken Fish')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new FrankenFishCandidateEliminator().eliminate(board)
    // Empty board: every row/col has 9 positions → not in 2-4 range → no fish
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const board = BoardReader.fromString(
      '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
      Board,
    )
    const changed = new FrankenFishCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on partial board', () => {
    const board = BoardReader.fromString('1' + '.'.repeat(80), Board)
    new FrankenFishCandidateEliminator().eliminate(board)
  })

  it('can run multiple times safely (idempotent)', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const eliminator = new FrankenFishCandidateEliminator()
    eliminator.eliminate(board)
    eliminator.eliminate(board)
    eliminator.eliminate(board)
  })

  it('runs without error on valid boards', () => {
    const puzzles = [
      // Scattered values across boxes
      '1.......2' + '.........' + '.........' +
      '.........' + '3.......4' + '.........' +
      '.........' + '.........' + '5.......6',
      // Center box has 3 values
      '.........' + '.........' + '.........' +
      '.........' + '.123.....' + '.........' +
      '.........' + '.........' + '.........',
    ]
    const eliminator = new FrankenFishCandidateEliminator()
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
      expect(board.isValid()).toBe(true)
    }
  })

  it('does not produce empty candidates', () => {
    const puzzles = [
      '1.......2' + '.........' + '.........' +
      '.........' + '3.......4' + '.........' +
      '.........' + '.........' + '5.......6',
      '.'.repeat(81),
    ]
    const eliminator = new FrankenFishCandidateEliminator()
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
      for (const coord of Coord.all) {
        expect(board.candidatePattern(coord)).toBeGreaterThan(0)
      }
    }
  })

  it('handles edge case: no rows with 2-4 candidates', () => {
    const puzzle =
      '1........' +
      '2........' +
      '3........' +
      '4........' +
      '5........' +
      '6........' +
      '7........' +
      '8........' +
      '9........'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new FrankenFishCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(typeof changed).toBe('boolean')
  })

  it('does not modify confirmed cells', () => {
    const board = BoardReader.fromString('1' + '.'.repeat(80), Board)
    const coord = Coord.all[0]
    const initial = board.value(coord)
    new FrankenFishCandidateEliminator().eliminate(board)
    expect(board.value(coord)).toBe(initial)
  })
})
