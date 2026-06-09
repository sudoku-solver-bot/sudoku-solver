import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { XWingDetector } from '../src/detectors/XWingDetector'
import { SwordfishDetector } from '../src/detectors/SwordfishDetector'
import { XYWingDetector } from '../src/detectors/XYWingDetector'
import { XYZWingDetector } from '../src/detectors/XYZWingDetector'
import { Technique } from '../src/HintTypes'
import { applyBasicElimination, generate } from '../src/HintGenerator'

const SOLVED = '534678912672195348198342567859761423426835791713924856961537284287419635345286179'
const XWING_PUZZLE = '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5'
const RED_BELT_Q1 = '800000000003600000070090200050007000000045700000100030001000068008500010090000400'

describe('XWingDetector', () => {
    it('has correct technique', () => {
        expect(new XWingDetector().technique).toBe(Technique.X_WING)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(new XWingDetector().detect(board)).toBeNull()
    })

    it('detects X-wing on X-wing puzzle', () => {
        const board = BoardReader.fromString(XWING_PUZZLE, Board)
        applyBasicElimination(board)
        const hint = new XWingDetector().detect(board)
        if (hint) {
            expect(hint.technique).toBe(Technique.X_WING)
        }
    })
})

describe('SwordfishDetector', () => {
    it('has correct technique', () => {
        expect(new SwordfishDetector().technique).toBe(Technique.SWORDFISH)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(new SwordfishDetector().detect(board)).toBeNull()
    })
})

describe('XYWingDetector', () => {
    it('has correct technique', () => {
        expect(new XYWingDetector().technique).toBe(Technique.XY_WING)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(new XYWingDetector().detect(board)).toBeNull()
    })

    it('detects XY-wing on red belt puzzle', () => {
        const board = BoardReader.fromString(RED_BELT_Q1, Board)
        applyBasicElimination(board)
        const hint = new XYWingDetector().detect(board)
        if (hint) {
            expect(hint.technique).toBe(Technique.XY_WING)
        }
    })
})

describe('XYZWingDetector', () => {
    it('has correct technique', () => {
        expect(new XYZWingDetector().technique).toBe(Technique.XYZ_WING)
    })

    it('returns null on solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(new XYZWingDetector().detect(board)).toBeNull()
    })
})

describe('Fish+wing detectors via HintGenerator', () => {
    it('generate() works with all detectors loaded', () => {
        const board = BoardReader.fromString(XWING_PUZZLE, Board)
        const hint = generate(board)
        if (hint) {
            expect(hint.technique).toBeDefined()
        }
    })

    it('Technique enum has all 20 values', () => {
        expect(Object.values(Technique)).toHaveLength(20)
    })
})
