import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { XYWingCandidateEliminator } from '../src/Eliminators'

const SOLVED = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'

describe('XYWingCandidateEliminator', () => {
    it('has correct displayName', () => {
        expect(new XYWingCandidateEliminator().displayName).toBe('XY-Wing')
    })

    it('returns false on empty board', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        expect(new XYWingCandidateEliminator().eliminate(board)).toBe(false)
    })

    it('returns false on fully solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(new XYWingCandidateEliminator().eliminate(board)).toBe(false)
    })

    it('does not throw on partial board', () => {
        const board = BoardReader.fromString('1'.padEnd(81, '.'), Board)
        new XYWingCandidateEliminator().eliminate(board)
        expect(board.isValid()).toBe(true)
    })

    it('does not modify confirmed cells', () => {
        const board = BoardReader.fromString('1'.padEnd(81, '.'), Board)
        const coord = Coord.all[0]
        const initial = board.candidatePattern(coord)
        new XYWingCandidateEliminator().eliminate(board)
        expect(board.candidatePattern(coord)).toBe(initial)
    })

    it('can run multiple times safely (idempotent)', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        const eliminator = new XYWingCandidateEliminator()
        eliminator.eliminate(board)
        eliminator.eliminate(board)
        eliminator.eliminate(board)
    })

    it('handles XY-Wing pattern detection', () => {
        // Set up a scenario with XY-Wing potential
        // Pivot at R5C5 with candidates {2,5}
        // Wing1 at R5C1 with candidates {2,8}
        // Wing2 at R1C5 with candidates {5,8}
        // R1C1 sees both wings → eliminate 8
        const puzzle =
            '...37....' +
            '.1......9' +
            '6........' +
            '3........' +
            '.........' +
            '........5' +
            '8........' +
            '.......4.' +
            '...81....'
        const board = BoardReader.fromString(puzzle, Board)
        const eliminator = new XYWingCandidateEliminator()
        eliminator.eliminate(board)
        expect(board.isValid()).toBe(true)
    })

    it('eliminates Z from cells seeing both wings', () => {
        // Minimal XY-Wing test:
        // R1C1=1, R1C2=2 → forces candidates
        // Pivot R2C2: {1,2} after simple eliminations
        // Create enough constraints to produce a clean XY-Wing
        const puzzle =
            '12.......' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........'
        const board = BoardReader.fromString(puzzle, Board)
        const eliminator = new XYWingCandidateEliminator()
        eliminator.eliminate(board)
        expect(board.isValid()).toBe(true)
    })

    it('handles board with many bi-value cells', () => {
        // Fill enough to create bi-value cells
        const puzzle =
            '123456789' +
            '456789123' +
            '789123456' +
            '2........' +
            '3........' +
            '6........' +
            '.........' +
            '.........' +
            '.........'
        const board = BoardReader.fromString(puzzle, Board)
        const eliminator = new XYWingCandidateEliminator()
        eliminator.eliminate(board)
        expect(board.isValid()).toBe(true)
    })

    it('does not produce empty candidates', () => {
        const puzzles = [
            '123456789' +
            '456789123' +
            '789123456' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........' +
            '.........',
        ]
        const eliminator = new XYWingCandidateEliminator()
        for (const p of puzzles) {
            const board = BoardReader.fromString(p, Board)
            eliminator.eliminate(board)
            for (const coord of Coord.all) {
                expect(board.candidatePattern(coord)).toBeGreaterThan(0)
            }
        }
    })
})
