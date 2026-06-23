/**
 * Shared test data for HintGenerator tests.
 *
 * Loads tutorial puzzle data from lessons.json and maps each tutorial to its
 * expected technique. Used by both HintGenerator.test.ts (unit tests) and
 * HintGeneratorTutorials.test.ts (integration tests against all 20 tutorials).
 */

import { readFileSync } from 'fs'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'
import { Technique } from '../src/HintTypes'

const __dirname = dirname(fileURLToPath(import.meta.url))
const lessonsPath = join(__dirname, '../../../web/src/main/resources/tutorials/lessons.json')

export interface TutorialTestDatum {
  id: string
  technique: string
  puzzle: string
  expectedTechnique: Technique
}

const techniqueMap: Record<string, Technique> = {
  'Naked Single': Technique.NAKED_SINGLE,
  'Hidden Single': Technique.HIDDEN_SINGLE,
  'Naked Pair': Technique.NAKED_PAIR,
  'Hidden Pair': Technique.HIDDEN_PAIR,
  'Pointing Pair': Technique.POINTING_PAIR,
  'Pointing Pair / Triple': Technique.POINTING_PAIR,
  'Box/Line Reduction': Technique.BOX_LINE_REDUCTION,
  'Naked Triple': Technique.NAKED_TRIPLE,
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
  'Forcing Chains': Technique.FORCING_CHAINS,
}

/**
 * Load tutorial test data from lessons.json.
 *
 * Returns structured test data: { id, technique, puzzle, expectedTechnique }[]
 * for all 20 tutorial puzzles.
 */
export function loadTutorialTestData(): TutorialTestDatum[] {
  const lessons = JSON.parse(readFileSync(lessonsPath, 'utf-8'))
  return lessons.map((lesson: { id: string; technique: string; examplePuzzle: string }) => ({
    id: lesson.id,
    technique: lesson.technique,
    puzzle: lesson.examplePuzzle,
    expectedTechnique: techniqueMap[lesson.technique] ?? Technique.NAKED_SINGLE,
  }))
}

/**
 * Validate that a puzzle string is well-formed.
 * Returns true if exactly 81 chars and all are '0'-'9' or '.'.
 */
export function isValidPuzzleString(puzzle: string): boolean {
  return puzzle.length === 81 && /^[0-9.]+$/.test(puzzle)
}
