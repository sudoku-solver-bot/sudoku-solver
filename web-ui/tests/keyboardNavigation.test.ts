import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Keyboard Navigation Audit
 *
 * Verifies that SudokuGrid.vue handles all required keyboard inputs:
 * - Arrow keys (Up, Down, Left, Right) for navigation
 * - Number keys 1-9 for input
 * - Delete and Backspace for clearing
 * - Given cells reject input
 *
 * Refs #705
 */

const SUDOKU_GRID = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SudokuGrid.vue'),
  'utf-8',
)

describe('keyboard navigation handlers', () => {
  it('has a keydown handler function', () => {
    expect(SUDOKU_GRID).toMatch(/const\s+onKeyDown\s*=\s*\(/)
  })

  it('has @keydown binding on input elements', () => {
    expect(SUDOKU_GRID).toMatch(/@keydown\s*=\s*"onKeyDown/)
  })

  describe('arrow key navigation', () => {
    it('handles ArrowUp', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]ArrowUp['"]:/)
    })

    it('handles ArrowDown', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]ArrowDown['"]:/)
    })

    it('handles ArrowLeft', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]ArrowLeft['"]:/)
    })

    it('handles ArrowRight', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]ArrowRight['"]:/)
    })

    it('ArrowUp navigates up (index - 9)', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowUp['"]:\s*[\s\S]*?emit\([^)]*\)/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/index\s*-\s*9/)
    })

    it('ArrowDown navigates down (index + 9)', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowDown['"]:\s*[\s\S]*?emit\([^)]*\)/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/index\s*\+\s*9/)
    })
  })

  describe('number input', () => {
    it('handles number keys 1-9 for input', () => {
      // Should check if event.key is a digit 1-9 and emit update
      expect(SUDOKU_GRID).toMatch(/event\.key\s*>=?\s*['"]1['"]|\/\^\[1-9\]\$\/|>= '1' && <= '9'/)
    })

    it('emits update with the pressed digit', () => {
      // The number input handler should emit('update', index, event.key)
      expect(SUDOKU_GRID).toMatch(/emit\(\s*['"]update['"]\s*,\s*index\s*,\s*event\.key/)
    })
  })

  describe('clearing cells', () => {
    it('handles Backspace key', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]Backspace['"]:/)
    })

    it('handles Delete key', () => {
      expect(SUDOKU_GRID).toMatch(/case\s+['"]Delete['"]:/)
    })

    it('Backspace/Delete emit empty string to clear cell', () => {
      // Find the Backspace/Delete case and verify it emits ''
      const match = SUDOKU_GRID.match(
        /case\s+['"]Backspace['"]:\s*\n\s*case\s+['"]Delete['"]:\s*[\s\S]*?break/,
      )
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/emit\(\s*['"]update['"]\s*,\s*index\s*,\s*['"]['"]\)/)
    })
  })

  describe('given cell input rejection', () => {
    it('checks givenCells before accepting number input', () => {
      // Number input should be rejected for given cells
      expect(SUDOKU_GRID).toMatch(/givenCells\.has\(index\)|givenCells\.has\(props\.|!.*givenCells/)
    })

    it('checks givenCells before clearing (Backspace/Delete)', () => {
      // The Delete/Backspace handler should also check givenCells
      const deleteMatch = SUDOKU_GRID.match(
        /case\s+['"]Backspace['"]:\s*\n\s*case\s+['"]Delete['"]:\s*[\s\S]*?break/,
      )
      expect(deleteMatch).not.toBeNull()
      expect(deleteMatch![0]).toMatch(/givenCells\.has/)
    })
  })

  describe('boundary checks', () => {
    it('ArrowUp checks row > 0 boundary', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowUp['"]:[\s\S]*?break/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/row\s*>\s*0/)
    })

    it('ArrowDown checks row < 8 boundary', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowDown['"]:[\s\S]*?break/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/row\s*<\s*8/)
    })

    it('ArrowLeft checks col > 0 boundary', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowLeft['"]:[\s\S]*?break/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/col\s*>\s*0/)
    })

    it('ArrowRight checks col < 8 boundary', () => {
      const match = SUDOKU_GRID.match(/case\s+['"]ArrowRight['"]:[\s\S]*?break/)
      expect(match).not.toBeNull()
      expect(match![0]).toMatch(/col\s*<\s*8/)
    })
  })
})
