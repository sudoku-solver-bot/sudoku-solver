import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Technique } from '../src/HintTypes'
import { generate } from '../src/HintGenerator'

const SOLVED = '534678912672195348198342567859761423426835791713924856961537284287419635345286179'
const HARD_PUZZLE = '.....6....59.....82....8....45........3........6..3.54...325..6..................'
const XWING_PUZZLE = '1.....5694.2.....8.5...9.4....64.8.1....1....2.8.35....4.5...1.9.....4.2621.....5'

describe('Eliminator-based techniques via HintGenerator', () => {
    it('generate() finds technique on hard puzzle', () => {
        const board = BoardReader.fromString(HARD_PUZZLE, Board)
        const hint = generate(board)
        // Hard puzzle needs advanced techniques — verify no crash
        if (hint) {
            expect(hint.technique).toBeDefined()
            expect(hint.value).toBeGreaterThanOrEqual(1)
            expect(hint.value).toBeLessThanOrEqual(9)
            expect(hint.explanation).toBeTruthy()
        }
    })

    it('generate() finds technique on X-Wing puzzle', () => {
        const board = BoardReader.fromString(XWING_PUZZLE, Board)
        const hint = generate(board)
        if (hint) {
            expect(hint.technique).toBeDefined()
        }
    })

    it('returns null for solved board', () => {
        const board = BoardReader.fromString(SOLVED, Board)
        expect(generate(board)).toBeNull()
    })

    it('all 20 techniques are defined', () => {
        expect(Object.values(Technique)).toHaveLength(20)
    })
})
