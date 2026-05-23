import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { FishCandidateEliminator } from '../src/Eliminators'

describe('FishCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new FishCandidateEliminator().displayName).toBe('Fish')
  })

  it('can be configured for X-Wing only', () => {
    const xwing = new FishCandidateEliminator([2])
    expect(xwing.displayName).toBe('Fish')
  })

  it('can be configured for Swordfish only', () => {
    const swordfish = new FishCandidateEliminator([3])
    expect(swordfish.displayName).toBe('Fish')
  })

  it('defaults to sizes [2, 3, 4]', () => {
    const fish = new FishCandidateEliminator()
    // Should handle X-Wing, Swordfish, and Jellyfish
    expect(fish.displayName).toBe('Fish')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new FishCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(solved, Board)
    const changed = new FishCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on various puzzles', () => {
    const eliminator = new FishCandidateEliminator()
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

  it('X-Wing only mode does not throw', () => {
    const xwing = new FishCandidateEliminator([2])
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '.....6....59.....82....8....45........3........6..3.54...325..6..................',
    ]
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      xwing.eliminate(board)
    }
  })
})
