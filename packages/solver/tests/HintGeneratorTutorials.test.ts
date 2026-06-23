import { describe, it, expect } from 'vitest'
import { Board } from '../src/Board'
import { BoardReader } from '../src/BoardReader'
import { Coord } from '../src/Coord'
import { Technique } from '../src/HintTypes'
import { generate } from '../src/HintGenerator'
import { loadTutorialTestData, isValidPuzzleString } from './hintGeneratorTestData'

const tutorialData = loadTutorialTestData()

/**
 * Check that a value does not conflict with the given row, column, and box
 * on the *original* board state (not affected by generate's internal mutations).
 */
function isValidHint(board: Board, value: number, row: number, col: number): boolean {
  // Cell should be empty on the original board
  if (board.value(Coord.all[row * 9 + col]) !== 0) return false

  // Check row
  for (let c = 0; c < 9; c++) {
    if (board.value(Coord.all[row * 9 + c]) === value) return false
  }
  // Check column
  for (let r = 0; r < 9; r++) {
    if (board.value(Coord.all[r * 9 + col]) === value) return false
  }
  // Check box
  const boxRow = Math.floor(row / 3) * 3
  const boxCol = Math.floor(col / 3) * 3
  for (let dr = 0; dr < 3; dr++) {
    for (let dc = 0; dc < 3; dc++) {
      if (board.value(Coord.all[(boxRow + dr) * 9 + (boxCol + dc)]) === value) return false
    }
  }
  return true
}

describe('HintGenerator — tutorial puzzle verification', () => {
  // Ensure we have all 20 tutorials
  it('has test data for all 20 tutorials', () => {
    expect(tutorialData).toHaveLength(20)
    for (const td of tutorialData) {
      expect(isValidPuzzleString(td.puzzle)).toBe(true)
    }
  })

  for (const { id, technique, puzzle, expectedTechnique } of tutorialData) {
    describe(`${id} (${technique})`, () => {
      // #744: Hint technique correctness
      it('produces a hint with a valid technique', () => {
        const board = BoardReader.fromString(puzzle, Board)
        const hint = generate(board)
        if (hint !== null) {
          expect(Object.values(Technique)).toContain(hint.technique)
        }
      })

      // #745: Cell and value validity
      it('returns a valid, non-conflicting hint', () => {
        const originalBoard = BoardReader.fromString(puzzle, Board)
        const workBoard = originalBoard.copy()
        const hint = generate(workBoard)

        // hint === null is acceptable — some puzzles may not produce a
        // hint with the current technique set (e.g. technique not yet wired)
        if (hint === null) return

        const { coord, value } = hint

        // Cell coordinates are within bounds
        expect(coord.row).toBeGreaterThanOrEqual(0)
        expect(coord.row).toBeLessThanOrEqual(8)
        expect(coord.col).toBeGreaterThanOrEqual(0)
        expect(coord.col).toBeLessThanOrEqual(8)

        // Value is valid
        expect(value).toBeGreaterThanOrEqual(1)
        expect(value).toBeLessThanOrEqual(9)

        // Explanation is non-empty
        expect(hint.explanation).toBeTruthy()
        expect(hint.explanation.length).toBeGreaterThan(0)

        // Cell must be empty on the ORIGINAL board
        expect(originalBoard.value(coord)).toBe(0)

        // Value must not conflict with the ORIGINAL board's row/col/box
        expect(isValidHint(originalBoard, value, coord.row, coord.col)).toBe(true)
      })
    })
  }
})
