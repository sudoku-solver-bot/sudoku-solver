import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { findALSes, deduplicateALS, seesEachOther, generateCombinations } from '../src/ALSHelper'

describe('ALSHelper', () => {
    describe('generateCombinations', () => {
        it('generates all k-combinations', () => {
            const result = generateCombinations([1, 2, 3], 2)
            expect(result).toEqual([[1, 2], [1, 3], [2, 3]])
        })

        it('returns empty for k > list length', () => {
            expect(generateCombinations([1, 2], 3)).toEqual([])
        })

        it('returns single empty combo for k=0', () => {
            expect(generateCombinations([1, 2, 3], 0)).toEqual([[]])
        })

        it('returns empty for empty list', () => {
            expect(generateCombinations([], 2)).toEqual([])
        })
    })

    describe('seesEachOther', () => {
        it('detects same row', () => {
            expect(seesEachOther(Coord.all[0], Coord.all[1])).toBe(true) // (0,0) and (0,1)
        })

        it('detects same column', () => {
            expect(seesEachOther(Coord.all[0], Coord.all[9])).toBe(true) // (0,0) and (1,0)
        })

        it('detects same box', () => {
            expect(seesEachOther(Coord.all[0], Coord.all[10])).toBe(true) // (0,0) and (1,1) — same box
        })

        it('returns false for unrelated cells', () => {
            expect(seesEachOther(Coord.all[0], Coord.all[30])).toBe(false) // (0,0) and (3,3) — different boxes
        })
    })

    describe('findALSes', () => {
        it('finds no ALSes on empty board', () => {
            const board = BoardReader.fromString('.'.repeat(81), Board)
            const als = findALSes(board)
            expect(als).toEqual([])
        })

        it('finds no ALSes on solved board', () => {
            const solved = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
            const board = BoardReader.fromString(solved, Board)
            const als = findALSes(board)
            expect(als).toEqual([])
        })

        it('finds ALS with 2 cells and 3 candidates', () => {
            // Construct a board where row 0 has exactly 2 unresolved cells
            // with 3 total candidates between them (2 cells + 1 extra = ALS)
            const puzzle = '123456789' +
                '456789123' +
                '789123456' +
                '231564897' +
                '564897231' +
                '897231564' +
                '312645978' +
                '645978312' +
                '978312000' // cells (8,6)=3, (8,7)=1, (8,8)=2 — but we need 2 cells with 3 cands
            // Actually let me construct more carefully
            // Row 8: 9 7 8 3 1 2 _ _ _
            // If cells (8,6), (8,7), (8,8) are empty, they see each other (same row)
            // Candidates for each depend on what's in their col/box
            // Let me just test with a known board state
            
            // Simple approach: create a board where 2 cells in a row have 3 candidates
            const patterns = new Int32Array(81)
            for (let i = 0; i < 81; i++) patterns[i] = 0x1FF // all 9 candidates
            
            // Set confirmed values to constrain candidates
            // Row 0: cells 0-8, set most to confirmed
            const values = [
                1,2,3,4,5,6,7,8,9,
                4,5,6,7,8,9,1,2,3,
                7,8,9,1,2,3,4,5,6,
                2,3,1,5,6,4,8,9,7,
                5,6,4,8,9,7,2,3,1,
                8,9,7,2,3,1,5,6,4,
                3,1,2,6,4,5,9,7,8,
                6,4,5,9,7,8,3,1,2,
                9,7,8,3,1,2,0,0,0, // last 3 cells empty
            ]
            for (let i = 0; i < 81; i++) {
                if (values[i] > 0) {
                    patterns[i] = 1 << (values[i] - 1)
                }
            }
            // Cells (8,6), (8,7), (8,8) have candidates from their cols
            // Col 6: has 7,1,4,8,2,5,9,3 → missing 6 → cell (8,6) = {6}
            // Col 7: has 8,2,5,9,3,6,7,1 → missing 4 → cell (8,7) = {4}
            // Col 8: has 9,3,6,7,1,4,8,2 → missing 5 → cell (8,8) = {5}
            // So each cell has exactly 1 candidate — no ALS
            
            // Let me make it so 2 cells share 3 candidates
            // Clear cells (8,6) and (8,7), give them candidates {4,5} each
            patterns[8 * 9 + 6] = (1 << 3) | (1 << 4) // candidates 4,5
            patterns[8 * 9 + 7] = (1 << 3) | (1 << 4) // candidates 4,5
            patterns[8 * 9 + 8] = 1 << 5 // confirmed? no, candidate 6 only
            
            // Wait, 2 cells with 2 candidates each = 2 cells, 2 candidates. Not an ALS.
            // ALS needs N cells with N+1 candidates.
            // 2 cells need 3 candidates total.
            // Give cell (8,6) candidates {4,5} and cell (8,7) candidates {4,5,6}
            // Total candidates = {4,5,6} = 3. 2 cells, 3 candidates = ALS!
            patterns[8 * 9 + 6] = (1 << 3) | (1 << 4) // {4,5}
            patterns[8 * 9 + 7] = (1 << 3) | (1 << 4) | (1 << 5) // {4,5,6}
            
            const board = new Board(patterns)
            const als = findALSes(board)
            
            // Should find at least one ALS with cells (8,6) and (8,7)
            const found = als.some(a =>
                a.cells.length === 2 &&
                a.cellsSet.has(Coord.all[8 * 9 + 6]) &&
                a.cellsSet.has(Coord.all[8 * 9 + 7]) &&
                a.candidates.size === 3
            )
            expect(found).toBe(true)
        })

        it('respects maxSize parameter', () => {
            const board = BoardReader.fromString('.'.repeat(81), Board)
            const als2 = findALSes(board, 2)
            const als5 = findALSes(board, 5)
            // With maxSize=2, should find fewer or equal ALSes than maxSize=5
            expect(als2.length).toBeLessThanOrEqual(als5.length)
            // All ALSes from als2 should have size <= 2
            for (const a of als2) {
                expect(a.cells.length).toBeLessThanOrEqual(2)
            }
        })
    })

    describe('deduplicateALS', () => {
        it('removes duplicate ALSes', () => {
            const als1 = {
                cells: [Coord.all[0], Coord.all[1]],
                candidates: new Set([1, 2, 3]),
                cellsSet: new Set([Coord.all[0], Coord.all[1]]),
            }
            const als2 = {
                cells: [Coord.all[1], Coord.all[0]], // same cells, different order
                candidates: new Set([1, 2, 3]),
                cellsSet: new Set([Coord.all[0], Coord.all[1]]),
            }
            const als3 = {
                cells: [Coord.all[0], Coord.all[2]], // different cells
                candidates: new Set([1, 2, 4]),
                cellsSet: new Set([Coord.all[0], Coord.all[2]]),
            }
            const result = deduplicateALS([als1, als2, als3])
            expect(result.length).toBe(2)
        })
    })
})
