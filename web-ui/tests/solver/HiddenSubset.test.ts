import { describe, it, expect } from 'vitest'
import { Board } from '@/solver/Board'
import { BoardReader } from '@/solver/BoardReader'
import { HiddenSubsetCandidateEliminator } from '@/solver/Eliminators'

describe('HiddenSubsetCandidateEliminator', () => {
  it('has correct displayName', () => {
    const eliminator = new HiddenSubsetCandidateEliminator()
    expect(eliminator.displayName).toBe('Hidden Subset')
  })

  it('returns false when no hidden subsets exist (empty board)', () => {
    const puzzle = '.'.repeat(81)
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new HiddenSubsetCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false when board is already fully solved', () => {
    const puzzle = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new HiddenSubsetCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on any valid puzzle', () => {
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5',
      '.....6....59.....82....8....45........3........6..3.54...325..6..................',
    ]
    const eliminator = new HiddenSubsetCandidateEliminator()
    for (const puzzle of puzzles) {
      const board = BoardReader.fromString(puzzle, Board)
      // Should not throw
      eliminator.eliminate(board)
    }
  })
})
