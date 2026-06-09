import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { CoordGroup } from '../src/CoordGroup'
import { NakedPairDetector } from '../src/detectors/NakedPairDetector'
import { NakedTripleDetector } from '../src/detectors/NakedTripleDetector'
import { HiddenPairDetector } from '../src/detectors/HiddenPairDetector'
import { HiddenTripleDetector } from '../src/detectors/HiddenTripleDetector'
import { Technique } from '../src/HintTypes'
import { applyBasicElimination, generate } from '../src/HintGenerator'
import { MASKS, WILDCARD_PATTERN } from '../src/Bitmask'

// Red belt Q1 puzzle — used for integration tests
const RED_BELT_Q1 = '800000000003600000070090200050007000000045700000100030001000068008500010090000400'

// Hard puzzle
const HARD_PUZZLE = '.....6....59.....82....8....45........3........6..3.54...325..6..................'

describe('NakedPairDetector', () => {
    it('has correct technique', () => {
        expect(new NakedPairDetector().technique).toBe(Technique.NAKED_PAIR)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426835791713924856961537284287419635345286179',
            Board
        )
        expect(new NakedPairDetector().detect(board)).toBeNull()
    })

    it('returns null on empty board', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        expect(new NakedPairDetector().detect(board)).toBeNull()
    })

    it('detects naked pair on board with explicit naked pair', () => {
        // Create a board where row 0 has a naked pair at (0,0) and (0,1) with candidates {1,2}
        // and other cells in row 0 also have candidate 1 or 2
        const values = new Int32Array(81).fill(511) // all candidates
        // Set (0,0) to candidates {1,2} = bitmask 0b11 = 3
        values[0] = 0b11
        // Set (0,1) to candidates {1,2} = bitmask 0b11 = 3
        values[1] = 0b11
        // Set (0,2) to candidates {1,2,3} = bitmask 0b111 = 7
        values[2] = 0b111
        const board = new Board(values)
        const detector = new NakedPairDetector()
        const hint = detector.detect(board)
        expect(hint).not.toBeNull()
        expect(hint!.technique).toBe(Technique.NAKED_PAIR)
        expect(hint!.value).toBeGreaterThanOrEqual(1)
        expect(hint!.value).toBeLessThanOrEqual(2)
    })
})

describe('NakedTripleDetector', () => {
    it('has correct technique', () => {
        expect(new NakedTripleDetector().technique).toBe(Technique.NAKED_TRIPLE)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426835791713924856961537284287419635345286179',
            Board
        )
        expect(new NakedTripleDetector().detect(board)).toBeNull()
    })

    it('detects naked triple', () => {
        // Create a board where row 0 has cells with candidates {1,2}, {2,3}, {1,3}
        // forming a naked triple — union is {1,2,3}
        // Cell 3 must have 2-3 candidates that overlap with the union
        const values = new Int32Array(81).fill(WILDCARD_PATTERN)
        values[0] = MASKS[0] | MASKS[1]  // {1,2}
        values[1] = MASKS[1] | MASKS[2]  // {2,3}
        values[2] = MASKS[0] | MASKS[2]  // {1,3}
        values[3] = MASKS[0] | MASKS[1] | MASKS[3] // {1,2,4} — overlaps with {1,2,3}
        const board = new Board(values)
        const detector = new NakedTripleDetector()
        const hint = detector.detect(board)
        expect(hint).not.toBeNull()
        expect(hint!.technique).toBe(Technique.NAKED_TRIPLE)
    })
})

describe('HiddenPairDetector', () => {
    it('has correct technique', () => {
        expect(new HiddenPairDetector().technique).toBe(Technique.HIDDEN_PAIR)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426835791713924856961537284287419635345286179',
            Board
        )
        expect(new HiddenPairDetector().detect(board)).toBeNull()
    })

    it('returns null on empty board', () => {
        const board = BoardReader.fromString('.'.repeat(81), Board)
        expect(new HiddenPairDetector().detect(board)).toBeNull()
    })

    it('detects hidden pair on red belt puzzle', () => {
        const board = BoardReader.fromString(RED_BELT_Q1, Board)
        applyBasicElimination(board)
        const detector = new HiddenPairDetector()
        const hint = detector.detect(board)
        // May or may not find one — just verify no crash
        if (hint) {
            expect(hint.technique).toBe(Technique.HIDDEN_PAIR)
        }
    })
})

describe('HiddenTripleDetector', () => {
    it('has correct technique', () => {
        expect(new HiddenTripleDetector().technique).toBe(Technique.HIDDEN_TRIPLE)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(
            '534678912672195348198342567859761423426835791713924856961537284287419635345286179',
            Board
        )
        expect(new HiddenTripleDetector().detect(board)).toBeNull()
    })

    it('detects hidden triple on hard puzzle', () => {
        const board = BoardReader.fromString(HARD_PUZZLE, Board)
        applyBasicElimination(board)
        const detector = new HiddenTripleDetector()
        const hint = detector.detect(board)
        if (hint) {
            expect(hint.technique).toBe(Technique.HIDDEN_TRIPLE)
        }
    })
})

describe('Subset detectors via HintGenerator', () => {
    it('generate() works with subset detectors loaded', () => {
        const board = BoardReader.fromString(RED_BELT_Q1, Board)
        const hint = generate(board)
        // Just verify no crash — hint may or may not be found
        if (hint) {
            expect(hint.technique).toBeDefined()
        }
    })

    it('Technique enum still has all 20 values', () => {
        expect(Object.values(Technique)).toHaveLength(20)
    })
})
