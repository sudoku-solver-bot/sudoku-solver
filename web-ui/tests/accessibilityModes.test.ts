import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Accessibility Modes Audit
 *
 * Verifies dark mode, colorblind mode, and high contrast mode:
 * - localStorage persistence (getItem on load, setItem on toggle)
 * - CSS class application (dark, colorblind, high-contrast)
 *
 * Refs #706
 */

const APP_VUE = readFileSync(join(__dirname, '..', 'src', 'App.vue'), 'utf-8')
const SUDOKU_GRID = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SudokuGrid.vue'),
  'utf-8',
)

describe('dark mode', () => {
  it('reads from localStorage on startup', () => {
    expect(APP_VUE).toMatch(/localStorage\.getItem\(\s*['"]sudokuDarkMode['"]\s*\)/)
  })

  it('persists to localStorage on toggle', () => {
    expect(APP_VUE).toMatch(/localStorage\.setItem\(\s*['"]sudokuDarkMode['"]\s*,/)
  })

  it('applies dark CSS class when enabled', () => {
    // App.vue root or SudokuGrid should have :class="{ dark: isDark }"
    expect(APP_VUE).toMatch(/dark:\s*isDark|class.*dark.*isDark/)
  })

  it('has dark mode CSS rules in SudokuGrid', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.dark\b/)
  })

  it('passes isDark prop to child components', () => {
    expect(APP_VUE).toMatch(/:is-dark\s*=\s*"isDark"/)
  })
})

describe('colorblind mode', () => {
  it('reads from localStorage on startup', () => {
    expect(APP_VUE).toMatch(/localStorage\.getItem\(\s*['"]sudokuColorBlind['"]\s*\)/)
  })

  it('persists to localStorage on toggle', () => {
    expect(APP_VUE).toMatch(/localStorage\.setItem\(\s*['"]sudokuColorBlind['"]\s*,/)
  })

  it('passes colorBlind prop to SudokuGrid', () => {
    expect(APP_VUE).toMatch(/:color-blind\s*=\s*"colorBlindMode"/)
  })

  it('SudokuGrid applies colorblind CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/colorblind:\s*colorBlind/)
  })

  it('has colorblind CSS rules', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.colorblind\b|\.colorblind\b/)
  })
})

describe('high contrast mode', () => {
  it('reads from localStorage on startup', () => {
    expect(APP_VUE).toMatch(/localStorage\.getItem\(\s*['"]sudokuHighContrast['"]\s*\)/)
  })

  it('persists to localStorage on toggle', () => {
    expect(APP_VUE).toMatch(/localStorage\.setItem\(\s*['"]sudokuHighContrast['"]\s*,/)
  })

  it('passes highContrast prop to SudokuGrid', () => {
    expect(APP_VUE).toMatch(/:high-contrast\s*=\s*"highContrastMode"/)
  })

  it('SudokuGrid applies high-contrast CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/'high-contrast':\s*highContrast/)
  })

  it('has high-contrast CSS rules', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.high-contrast\b|\.high-contrast\b/)
  })
})

describe('accessibility mode toggles', () => {
  it('has toggleColorBlind function', () => {
    expect(APP_VUE).toMatch(/const\s+toggleColorBlind\s*=/)
  })

  it('has toggleHighContrast function', () => {
    expect(APP_VUE).toMatch(/const\s+toggleHighContrast\s*=/)
  })

  it('colorblind toggle emits event to parent', () => {
    // Should be wired via @toggle-colorblind
    expect(APP_VUE).toMatch(/@toggle-colorblind\s*=\s*"toggleColorBlind"/)
  })

  it('dark mode toggle function exists', () => {
    expect(APP_VUE).toMatch(/const\s+toggleDarkMode\s*=/)
  })
})
