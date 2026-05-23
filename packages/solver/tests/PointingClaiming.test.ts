import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import {
  PointingCandidateEliminator,
  ClaimingCandidateEliminator,
} from '../src/Eliminators'

describe('PointingCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new PointingCandidateEliminator().displayName).toBe('Pointing')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new PointingCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(solved, Board)
    const changed = new PointingCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on various puzzles', () => {
    const eliminator = new PointingCandidateEliminator()
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '.....6....59.....82....8....45........3........6..3.54...325..6..................',
    ]
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
    }
  })
})

describe('ClaimingCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new ClaimingCandidateEliminator().displayName).toBe('Claiming')
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const changed = new ClaimingCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('returns false on fully solved board', () => {
    const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(solved, Board)
    const changed = new ClaimingCandidateEliminator().eliminate(board)
    expect(changed).toBe(false)
  })

  it('does not throw on various puzzles', () => {
    const eliminator = new ClaimingCandidateEliminator()
    const puzzles = [
      '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79',
      '.....6....59.....82....8....45........3........6..3.54...325..6..................',
    ]
    for (const p of puzzles) {
      const board = BoardReader.fromString(p, Board)
      eliminator.eliminate(board)
    }
  })
})
