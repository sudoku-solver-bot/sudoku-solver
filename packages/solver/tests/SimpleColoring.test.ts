import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import {
    SimpleCandidateEliminator,
    SimpleColoringCandidateEliminator,
} from '../src/Eliminators'

describe('SimpleColoringCandidateEliminator', () => {
    it('no changes when board is completely solved', () => {
        const values = new Int8Array([
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 9,
        ])

        const board = Board.fromValues(values)
        const eliminator = new SimpleColoringCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(false)
    })

    it('runs without error on empty board', () => {
        const values = new Int8Array(81)
        const board = Board.fromValues(values)
        const eliminator = new SimpleColoringCandidateEliminator()

        expect(() => eliminator.eliminate(board)).not.toThrow()
    })

    it('runs without error on partial board', () => {
        const values = new Int8Array(81)
        values[0] = 1
        values[40] = 5
        values[80] = 9

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new SimpleColoringCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
    })

    it('runs without error on nearly solved board', () => {
        const values = new Int8Array([
            5, 4, 9, 3, 7, 8, 1, 6, 2,
            2, 1, 7, 4, 6, 5, 3, 9, 8,
            6, 3, 8, 2, 9, 1, 4, 7, 5,
            9, 2, 3, 5, 4, 6, 7, 8, 1,
            1, 7, 4, 8, 2, 9, 5, 3, 6,
            8, 6, 5, 7, 1, 3, 9, 2, 4,
            4, 5, 2, 9, 8, 7, 6, 1, 3,
            3, 9, 1, 6, 5, 2, 8, 4, 7,
            7, 8, 6, 1, 3, 4, 2, 5, 0,
        ])

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new SimpleColoringCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
    })

    it('handles board with conjugate pairs correctly', () => {
        // Create a board with conjugate pair scenarios
        const puzzle = [
            '1.......2',
            '.........',
            '.........',
            '.........',
            '.........',
            '.........',
            '.........',
            '.........',
            '3.......4',
        ].join('')

        const board = BoardReader.fromString(puzzle, Board)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new SimpleColoringCandidateEliminator()
        const changed = eliminator.eliminate(board)

        // Should run without errors; may or may not find eliminations
        expect(board.isValid()).toBe(true)
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(typeof changed).toBe('boolean')
    })

    it('eliminator is exported and has correct display name', () => {
        const eliminator = new SimpleColoringCandidateEliminator()
        expect(eliminator.displayName).toBe('Simple Coloring')
    })
})
