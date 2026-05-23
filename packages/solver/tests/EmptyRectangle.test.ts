import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { EmptyRectangleCandidateEliminator } from '../src/Eliminators'

describe('EmptyRectangleCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new EmptyRectangleCandidateEliminator().displayName).toBe('Empty Rectangle')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new EmptyRectangleCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(solved, Board)
    const changed = new EmptyRectangleCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on various puzzles', () => {
    const eliminator = new EmptyRectangleCandidateEliminator()
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
})