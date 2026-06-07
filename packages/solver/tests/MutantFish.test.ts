import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { MutantFishCandidateEliminator } from '../src/Eliminators'
import {
    findCandidatePositions,
    findHousesWithCandidates,
} from '../src/FishHelpers'

describe('MutantFishCandidateEliminator', { timeout: 15000 }, () => {
    it('has correct displayName', () => {
        expect(new MutantFishCandidateEliminator().displayName).toBe('Mutant Fish')
    })

    it('returns false on fully solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
            Board,
        )
        const changed = new MutantFishCandidateEliminator().eliminate(board)
        expect(changed).toBe(false)
    })

    it('does not throw on a real-world puzzle', () => {
        const board = BoardReader.fromString(
            '53..7....' +
            '6..195...' +
            '.98....6.' +
            '8...6...3' +
            '4..8.3..1' +
            '7...2...6' +
            '.6....28.' +
            '...419..5' +
            '....8..79',
            Board,
        )
        new MutantFishCandidateEliminator().eliminate(board)
    })

    it('does not modify confirmed cells', () => {
        const board = BoardReader.fromString(
            '53..7....' +
            '6..195...' +
            '.98....6.' +
            '8...6...3' +
            '4..8.3..1' +
            '7...2...6' +
            '.6....28.' +
            '...419..5' +
            '....8..79',
            Board,
        )
        // Snapshot all confirmed values
        const before: Map<Coord, number> = new Map()
        for (const coord of Coord.all) {
            if (board.isConfirmed(coord)) {
                before.set(coord, board.value(coord))
            }
        }
        new MutantFishCandidateEliminator().eliminate(board)
        for (const [coord, val] of before) {
            expect(board.value(coord)).toBe(val)
        }
    })

    it('finds correct candidate positions via helpers', () => {
        const puzzle =
            '1........' +
            '.1.......' +
            '..1......' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........'
        const board = BoardReader.fromString(puzzle, Board)
        const positions = findCandidatePositions(board, 1)
        // 3 placed 1s → 78 empty cells still have 1 as candidate
        expect(positions.length).toBe(78)
    })

    it('enumerates base houses with at least 2 candidate positions', () => {
        const puzzle =
            '11.......' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........'
        const board = BoardReader.fromString(puzzle, Board)
        const positions = findCandidatePositions(board, 1)
        const houses = findHousesWithCandidates(positions, 2)
        expect(houses.some((h) => h.kind === 'row' && h.index === 0)).toBe(true)
    })

    it('produces valid results on a real-world puzzle', () => {
        const eliminator = new MutantFishCandidateEliminator()
        const puzzle =
            '53..7....' +
            '6..195...' +
            '.98....6.' +
            '8...6...3' +
            '4..8.3..1' +
            '7...2...6' +
            '.6....28.' +
            '...419..5' +
            '....8..79'
        const board = BoardReader.fromString(puzzle, Board)
        const changed = eliminator.eliminate(board)
        // Must not break board validity
        expect(board.isValid()).toBe(true)
        if (changed) {
            expect(board.isSolved() || board.isValid()).toBe(true)
        }
    })
})
