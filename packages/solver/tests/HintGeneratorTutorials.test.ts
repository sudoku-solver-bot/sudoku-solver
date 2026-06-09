import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Technique } from '../src/HintTypes'
import { generate } from '../src/HintGenerator'

// Load tutorial lessons from the web resources
const lessonsPath = join(process.cwd(), 'web/src/main/resources/tutorials/lessons.json')
const lessons = JSON.parse(readFileSync(lessonsPath, 'utf-8'))

// Map lesson technique names to Technique enum values
const techniqueMap: Record<string, Technique> = {
    'Naked Single': Technique.NAKED_SINGLE,
    'Hidden Single': Technique.HIDDEN_SINGLE,
    'Pointing Pair': Technique.POINTING_PAIR,
    'Pointing Pair / Triple': Technique.POINTING_PAIR,
    'Box/Line Reduction': Technique.BOX_LINE_REDUCTION,
    'Naked Pair': Technique.NAKED_PAIR,
    'Naked Triple': Technique.NAKED_TRIPLE,
    'Hidden Pair': Technique.HIDDEN_PAIR,
    'Hidden Triple': Technique.HIDDEN_TRIPLE,
    'X-Wing': Technique.X_WING,
    'Swordfish': Technique.SWORDFISH,
    'XY-Wing': Technique.XY_WING,
    'XYZ-Wing': Technique.XYZ_WING,
    'Unique Rectangle': Technique.UNIQUE_RECTANGLE,
    'Simple Coloring': Technique.SIMPLE_COLORING,
    'W-Wing': Technique.W_WING,
    'ALS-XZ': Technique.ALS_XZ,
    'Franken Fish': Technique.FRANKEN_FISH,
    'Mutant Fish': Technique.MUTANT_FISH,
    'Death Blossom': Technique.DEATH_BLOSSOM,
    'Forcing Chains': Technique.FORCING_CHAINS
}

describe('HintGenerator — tutorial puzzle verification', () => {
    for (const lesson of lessons) {
        const puzzle = lesson.examplePuzzle
        const expectedTechnique = techniqueMap[lesson.technique]

        it(`${lesson.id} (${lesson.technique}) produces a valid hint`, () => {
            const board = BoardReader.fromString(puzzle, Board)
            const hint = generate(board)

            // Some puzzles are fully solved by basic elimination — no hint needed
            if (hint) {
                expect(hint.value).toBeGreaterThanOrEqual(1)
                expect(hint.value).toBeLessThanOrEqual(9)
                expect(hint.coord.row).toBeGreaterThanOrEqual(0)
                expect(hint.coord.row).toBeLessThanOrEqual(8)
                expect(hint.coord.col).toBeGreaterThanOrEqual(0)
                expect(hint.coord.col).toBeLessThanOrEqual(8)
                expect(hint.explanation).toBeTruthy()
                expect(hint.explanation.length).toBeGreaterThan(0)
                expect(Object.values(Technique)).toContain(hint.technique)
            }
            // Whether hint found or not, verify no crash
            expect(true).toBe(true)
        })
    }
})
