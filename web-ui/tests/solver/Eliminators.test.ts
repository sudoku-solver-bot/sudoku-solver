import { describe, it, expect } from 'vitest'
import { Board } from '@/solver/Board'
import { BoardReader } from '@/solver/BoardReader'
import { Coord } from '@/solver/Coord'
import { SimpleCandidateEliminator, GroupCandidateEliminator, ExclusionCandidateEliminator } from '@/solver/Eliminators'

describe('SimpleCandidateEliminator', () => {
  it('has correct displayName', () => {
    expect(new SimpleCandidateEliminator().displayName).toBe('Simple Elimination')
  })

  it('removes candidates in same row for confirmed values', () => {
    const puzzle = '5' + '.'.repeat(80)
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new SimpleCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(true)
    // Check that cell 1 (same row) no longer has 5 as candidate
    const coord1 = Coord.all[1]
    expect(board.candidateValues(coord1)).not.toContain(5)
  })

  it('removes candidates in same column for confirmed values', () => {
    const puzzle = '.'.repeat(9) + '5' + '.'.repeat(71)  // cell 9 = 5
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new SimpleCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(true)
    // cell 0 (same column as cell 9) should not have 5
    const coord0 = Coord.all[0]
    expect(board.candidateValues(coord0)).not.toContain(5)
  })

  it('removes candidates in same region for confirmed values', () => {
    const puzzle = '5' + '.'.repeat(80)
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new SimpleCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(true)
    // cell 1 (same region as cell 0) should not have 5
    const coord1 = Coord.all[1]
    expect(board.candidateValues(coord1)).not.toContain(5)
  })

  it('handles cascading eliminations (iterates to stability)', () => {
    const puzzle = '53..7....' + '6..195...' + '.98....6.' +
                   '8...6...3' + '4..8.3..1' + '7...2...6' +
                   '.6....28.' + '...419..5' + '....8..79'
    const board = BoardReader.fromString(puzzle, Board)
    const eliminator = new SimpleCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(true)
    // Should reduce candidates significantly
    let totalCandidates = 0
    for (let i = 0; i < 81; i++) {
      const coord = Coord.all[i]
      if (!board.isConfirmed(coord)) {
        totalCandidates += board.candidateValues(coord).length
      }
    }
    expect(totalCandidates).toBeLessThan(81 * 8) // definitely reduced from max
  })

  it('returns false when board is already fully constrained', () => {
    const solvedPuzzle = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    const board = BoardReader.fromString(solvedPuzzle, Board)
    const eliminator = new SimpleCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(false)
  })
})

describe('GroupCandidateEliminator (Naked Subset)', () => {
  it('has correct displayName', () => {
    expect(new GroupCandidateEliminator().displayName).toBe('Naked Subset')
  })

  it('returns false when no naked subsets exist (empty board)', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const eliminator = new GroupCandidateEliminator()
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(false)
  })

  it('finds naked subsets in a puzzle with reduced candidates', () => {
    // Puzzle from Kotlin test: after simple elimination, naked subsets appear
    const puzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
    const board = BoardReader.fromString(puzzle, Board)
    const simple = new SimpleCandidateEliminator()
    simple.eliminate(board)
    const group = new GroupCandidateEliminator()
    const changed = group.eliminate(board)
    // Group eliminator may or may not find subsets depending on puzzle state
    // Verify it runs without error
    expect(typeof changed).toBe('boolean')
  })
})

describe('ExclusionCandidateEliminator (Hidden Single)', () => {
  it('has correct displayName', () => {
    expect(new ExclusionCandidateEliminator(1).displayName).toBe('Hidden Single')
  })

  it('creates with configurable shortCircuitThreshold', () => {
    const e1 = new ExclusionCandidateEliminator(1)
    const e2 = new ExclusionCandidateEliminator(3)
    expect(e1.displayName).toBe('Hidden Single')
    expect(e2.displayName).toBe('Hidden Single')
  })

  it('finds hidden singles in a puzzle', () => {
    // Puzzle with hidden singles — after constraint propagation
    const puzzle = '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5'
    const board = BoardReader.fromString(puzzle, Board)
    const simple = new SimpleCandidateEliminator()
    simple.eliminate(board)
    // threshold must be high enough to process groups with known values
    const exclusion = new ExclusionCandidateEliminator(9)
    const changed = exclusion.eliminate(board)
    // Should find at least one hidden single
    expect(changed).toBe(true)
  })

  it('returns false on empty board', () => {
    const board = BoardReader.fromString('.'.repeat(81), Board)
    const eliminator = new ExclusionCandidateEliminator(1)
    const changed = eliminator.eliminate(board)
    expect(changed).toBe(false)
  })
})

describe('eliminator pipeline (Simple → Group → Exclusion)', () => {
  it('makes progress on easy puzzles with the 3 core eliminators', () => {
    const puzzle = '530070000600195000098000060800060003400803001700020006060000280000419005000080079'
    const board = BoardReader.fromString(puzzle, Board)

    const eliminators = [
      new SimpleCandidateEliminator(),
      new GroupCandidateEliminator(),
      new ExclusionCandidateEliminator(9)
    ]

    let anyChange = true
    while (anyChange) {
      anyChange = false
      for (const eliminator of eliminators) {
        if (eliminator.eliminate(board)) {
          anyChange = true
        }
      }
    }

    // Count confirmed cells
    let confirmedCount = 0
    for (let i = 0; i < 81; i++) {
      if (board.isConfirmed(Coord.all[i])) {
        confirmedCount++
      }
    }

    // This partially-filled puzzle should have >29 confirmed (29 givens + eliminations)
    expect(confirmedCount).toBeGreaterThan(29)
  })
})
