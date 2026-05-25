import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import {
    allHouses,
    houseContains,
    houseKey,
    findCandidatePositions,
    findHousesWithCandidates,
    buildHousePositionMap,
    generateCombinations,
} from '../src/FishHelpers'

describe('House', () => {
    it('allHouses contains exactly 27 houses', () => {
        expect(allHouses).toHaveLength(27)
        expect(allHouses.filter((h) => h.kind === 'row')).toHaveLength(9)
        expect(allHouses.filter((h) => h.kind === 'col')).toHaveLength(9)
        expect(allHouses.filter((h) => h.kind === 'box')).toHaveLength(9)
    })

    it('houseContains row', () => {
        const h = { kind: 'row' as const, index: 3 }
        expect(houseContains(h, Coord.all[3 * 9 + 4])).toBe(true) // row 3, col 4
        expect(houseContains(h, Coord.all[2 * 9 + 4])).toBe(false) // row 2, col 4
    })

    it('houseContains col', () => {
        const h = { kind: 'col' as const, index: 5 }
        expect(houseContains(h, Coord.all[2 * 9 + 5])).toBe(true) // row 2, col 5
        expect(houseContains(h, Coord.all[2 * 9 + 4])).toBe(false) // row 2, col 4
    })

    it('houseContains box', () => {
        const h = { kind: 'box' as const, index: 0 } // top-left box
        expect(houseContains(h, Coord.all[0])).toBe(true)
        expect(houseContains(h, Coord.all[1])).toBe(true)
        expect(houseContains(h, Coord.all[9])).toBe(true)
        expect(houseContains(h, Coord.all[10])).toBe(true)
        expect(houseContains(h, Coord.all[3 * 9])).toBe(false) // row 3, col 0 → box 3
    })

    it('houseKey is stable', () => {
        expect(houseKey({ kind: 'row', index: 0 })).toBe('row:0')
        expect(houseKey({ kind: 'col', index: 3 })).toBe('col:3')
        expect(houseKey({ kind: 'box', index: 8 })).toBe('box:8')
    })
})

describe('findCandidatePositions', () => {
    it('returns empty for solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
            Board,
        )
        for (let v = 1; v <= 9; v++) {
            expect(findCandidatePositions(board, v)).toHaveLength(0)
        }
    })

    it('finds positions on empty board', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        for (let v = 1; v <= 9; v++) {
            expect(findCandidatePositions(board, v)).toHaveLength(81) // all open cells
        }
    })

    it('finds correct positions on partial board', () => {
        // 4 placed values, 77 empty cells
        const puzzle = '1234.............................................................................'
        const board = BoardReader.fromString(puzzle, Board)
        // Without basic elimination, all 1-9 are candidates in all 77 empty cells
        expect(findCandidatePositions(board, 1)).toHaveLength(77)
        expect(findCandidatePositions(board, 5)).toHaveLength(77)
    })
})

describe('findHousesWithCandidates', () => {
    it('returns houses with at least minCount positions', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        const positions = findCandidatePositions(board, 1)
        // All 27 houses have 9 positions each on empty board
        const houses = findHousesWithCandidates(positions, 2)
        expect(houses).toHaveLength(27)
    })

    it('filters houses below minCount threshold', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        const positions = findCandidatePositions(board, 1)
        const houses = findHousesWithCandidates(positions, 10) // impossible threshold
        expect(houses).toHaveLength(0)
    })

    it('works with sparse positions', () => {
        // Only row 0 has candidate 1 at (0,0) and (0,1)
        const puzzle =
            '.........' +
            '1.1......' +
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
        // Row 1 has 2 positions with 1
        expect(houses.some((h) => h.kind === 'row' && h.index === 1)).toBe(true)
    })
})

describe('buildHousePositionMap', () => {
    it('maps houses to their positions', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        const positions = findCandidatePositions(board, 1)
        const map = buildHousePositionMap(positions)
        // Row 0 appears in map
        expect(map.has('row:0')).toBe(true)
        expect(map.get('row:0')!).toHaveLength(9)
        expect(map.has('col:0')).toBe(true)
        expect(map.get('col:0')!).toHaveLength(9)
    })
})

describe('generateCombinations', () => {
    it('generates correct combinations for k=0', () => {
        expect(generateCombinations([1, 2, 3], 0)).toEqual([[]])
    })

    it('generates correct combinations for k=1', () => {
        expect(generateCombinations([1, 2, 3], 1)).toEqual([[1], [2], [3]])
    })

    it('generates correct combinations for k=2', () => {
        expect(generateCombinations([1, 2, 3], 2)).toEqual([
            [1, 2],
            [1, 3],
            [2, 3],
        ])
    })

    it('generates correct combinations for k=n', () => {
        expect(generateCombinations([1, 2, 3], 3)).toEqual([[1, 2, 3]])
    })

    it('returns empty when k > n', () => {
        expect(generateCombinations([1, 2], 3)).toEqual([])
    })

    it('handles empty input', () => {
        expect(generateCombinations([], 0)).toEqual([[]])
        expect(generateCombinations([], 1)).toEqual([])
    })
})
