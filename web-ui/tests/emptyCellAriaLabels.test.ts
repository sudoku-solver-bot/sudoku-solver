import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Empty Cell Rendering + ARIA Labels Audit
 *
 * Verifies that SudokuGrid.vue:
 * 1. Has isEmpty() function for checking empty cells
 * 2. Empty cells render '' (not '0', '-', or placeholder)
 * 3. Input value binding uses isEmpty() check
 * 4. Has getCellLabel() function
 * 5. Every cell has :aria-label binding
 * 6. Label format includes row, column, and value
 *
 * Refs #704
 */

const SUDOKU_GRID = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SudokuGrid.vue'),
  'utf-8',
)

describe('empty cell rendering', () => {
  it('defines isEmpty() function', () => {
    expect(SUDOKU_GRID).toMatch(/const\s+isEmpty\s*=\s*\(/)
  })

  it('isEmpty checks for empty, dot, and zero values', () => {
    // Should check for '', '.', and '0'
    const isEmptyMatch = SUDOKU_GRID.match(
      /const\s+isEmpty\s*=\s*\([^)]*\)\s*=>\s*(.+)/,
    )
    expect(isEmptyMatch).not.toBeNull()
    const body = isEmptyMatch![1]
    expect(body).toMatch(/['"]\.['"]|===?\s*['"]\.['"]/) // checks for '.'
    expect(body).toMatch(/['"]0['"]|===?\s*['"]0['"]/) // checks for '0'
  })

  it('renders empty string for empty cells (not 0 or dash)', () => {
    // The :value binding should use isEmpty() check and return ''
    expect(SUDOKU_GRID).toMatch(
      /:value\s*=\s*"isEmpty\([^)]*\)\s*\?\s*''\s*:/,
    )
  })

  it('does not render 0 or dash for empty cells in value binding', () => {
    // Ensure no :value bindings that would render '0' or '-' for empty cells
    const valueBindings = SUDOKU_GRID.match(/:value\s*=\s*"[^"]*"/g) || []
    for (const binding of valueBindings) {
      // If the binding has a ternary with isEmpty, the false branch should not be '0' or '-'
      if (binding.includes('isEmpty')) {
        // The non-empty branch should render the actual value, not a placeholder
        expect(binding).not.toMatch(/\?\s*['"]0['"]/)
        expect(binding).not.toMatch(/\?\s*['"]-['"]/)
      }
    }
  })
})

describe('ARIA labels', () => {
  it('defines getCellLabel() function', () => {
    expect(SUDOKU_GRID).toMatch(/const\s+getCellLabel\s*=\s*\(/)
  })

  it('every cell has :aria-label binding using getCellLabel', () => {
    // The template should have :aria-label="getCellLabel(...)" on each cell
    expect(SUDOKU_GRID).toMatch(/:aria-label\s*=\s*"getCellLabel\(/)
  })

  it('getCellLabel includes row information', () => {
    const labelMatch = SUDOKU_GRID.match(
      /const\s+getCellLabel\s*=\s*\([^)]*\)\s*=>\s*\{([\s\S]*?)\n\}/,
    )
    expect(labelMatch).not.toBeNull()
    const body = labelMatch![1]
    expect(body.toLowerCase()).toMatch(/row/)
  })

  it('getCellLabel includes column information', () => {
    const labelMatch = SUDOKU_GRID.match(
      /const\s+getCellLabel\s*=\s*\([^)]*\)\s*=>\s*\{([\s\S]*?)\n\}/,
    )
    expect(labelMatch).not.toBeNull()
    const body = labelMatch![1]
    expect(body.toLowerCase()).toMatch(/col/)
  })

  it('getCellLabel includes cell value when non-empty', () => {
    const labelMatch = SUDOKU_GRID.match(
      /const\s+getCellLabel\s*=\s*\([^)]*\)\s*=>\s*\{([\s\S]*?)\n\}/,
    )
    expect(labelMatch).not.toBeNull()
    const body = labelMatch![1]
    // Should reference value in the label when cell is not empty
    expect(body).toMatch(/value|puzzle/)
  })

  it('grid container has aria-label', () => {
    expect(SUDOKU_GRID).toMatch(/aria-label\s*=\s*['"][^'"]+['"]/)
  })
})
