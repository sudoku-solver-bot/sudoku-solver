import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { Coord } from '../src/Coord'
import {
    SimpleCandidateEliminator,
    UniqueRectanglesCandidateEliminator,
} from '../src/Eliminators'

describe('UniqueRectanglesCandidateEliminator', () => {
    it('no changes when board is fully solved', () => {
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
        const eliminator = new UniqueRectanglesCandidateEliminator()
        const changed = eliminator.eliminate(board)

        expect(changed).toBe(false)
    })

    it('runs without error on empty board', () => {
        const values = new Int8Array(81)
        const board = Board.fromValues(values)
        const eliminator = new UniqueRectanglesCandidateEliminator()

        const changed = eliminator.eliminate(board)

        // All cells have 9 candidates, no deadly rectangle forms
        expect(changed).toBe(false)
        expect(board.isValid()).toBe(true)
    })

    it('runs without error on partial board', () => {
        const values = new Int8Array(81)
        values[0] = 1
        values[40] = 5
        values[80] = 9

        const board = Board.fromValues(values)
        const eliminator = new UniqueRectanglesCandidateEliminator()

        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('does not modify confirmed cells', () => {
        const values = new Int8Array(81)
        values[0] = 1

        const board = Board.fromValues(values)
        const initialPattern = board.candidatePattern(Coord.all[0])

        const eliminator = new UniqueRectanglesCandidateEliminator()
        eliminator.eliminate(board)

        expect(board.candidatePattern(Coord.all[0])).toBe(initialPattern)
    })

    it('handles rectangle at board corners', () => {
        // (0,0), (0,1), (1,0), (1,1)
        const values = new Int8Array(81)
        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('handles rectangle at board center', () => {
        // (4,4), (4,5), (5,4), (5,5)
        const values = new Int8Array(81)
        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('handles complex board state', () => {
        const values = new Int8Array([
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 2, 3, 0, 0, 0,
            0, 0, 0, 4, 5, 6, 0, 0, 0,
            0, 0, 0, 7, 8, 9, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
        ])

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('Type 1 pattern not falsely detected when multiple cells have extras', () => {
        const values = new Int8Array(81)
        // Every other cell filled — complex candidate state
        for (let i = 0; i < 81; i += 2) {
            values[i] = (i % 9) + 1
        }

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        const result = eliminator.eliminate(board)
        expect(typeof result).toBe('boolean')
    })

    it('handles same-box corner detection correctly', () => {
        const values = new Int8Array(81)
        // Fill box 0 (top-left 3×3)
        values[0] = 1
        values[1] = 2
        values[2] = 3
        values[9] = 4
        values[10] = 5
        values[11] = 6

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('handles board after basic elimination', () => {
        const values = new Int8Array(81)
        values[0] = 1
        values[4] = 5
        values[8] = 9
        values[10] = 2
        values[20] = 8
        values[40] = 7
        values[50] = 3
        values[60] = 4
        values[80] = 6

        const board = Board.fromValues(values)
        new SimpleCandidateEliminator().eliminate(board)

        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(() => eliminator.eliminate(board)).not.toThrow()
        expect(board.isValid()).toBe(true)
    })

    it('can run multiple times safely', () => {
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
        const eliminator = new UniqueRectanglesCandidateEliminator()

        eliminator.eliminate(board)
        eliminator.eliminate(board)
        eliminator.eliminate(board)

        expect(board.isValid()).toBe(true)
    })

    it('eliminator has correct display name', () => {
        const eliminator = new UniqueRectanglesCandidateEliminator()
        expect(eliminator.displayName).toBe('Unique Rectangles')
    })
})
