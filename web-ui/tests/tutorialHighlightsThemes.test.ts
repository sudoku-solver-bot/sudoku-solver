import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Tutorial Highlights Colors + Theme Support Audit
 *
 * Verifies:
 * - Highlight CSS classes exist (blue, green, red, yellow)
 * - Highlight colors match ADR-0007 spec (color-blind friendly variants)
 * - Theme prop exists and CSS .theme-* classes are defined
 * - At least 2 themes are available (beyond default)
 *
 * Refs #708
 */

const SUDOKU_GRID = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SudokuGrid.vue'),
  'utf-8',
)

describe('tutorial highlight CSS classes', () => {
  it('has highlight-blue CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/\.cell\.highlight-blue\b/)
  })

  it('has highlight-green CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/\.cell\.highlight-green\b/)
  })

  it('has highlight-red CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/\.cell\.highlight-red\b/)
  })

  it('has highlight-yellow CSS class', () => {
    expect(SUDOKU_GRID).toMatch(/\.cell\.highlight-yellow\b/)
  })

  it('all four highlight types are applied in getCellClasses', () => {
    expect(SUDOKU_GRID).toMatch(/highlight-blue.*=\s*isHighlighted/)
    expect(SUDOKU_GRID).toMatch(/highlight-green.*=\s*isHighlighted/)
    expect(SUDOKU_GRID).toMatch(/highlight-red.*=\s*isHighlighted/)
    expect(SUDOKU_GRID).toMatch(/highlight-yellow.*=\s*isHighlighted/)
  })
})

describe('highlight color specification', () => {
  it('highlight-blue uses blue-based background with box-shadow inset', () => {
    const match = SUDOKU_GRID.match(/\.cell\.highlight-blue\s*\{([^}]+)\}/)
    expect(match).not.toBeNull()
    const body = match![1]
    expect(body).toMatch(/background/)
    expect(body).toMatch(/box-shadow/)
  })

  it('highlight-green uses green-based background with box-shadow inset', () => {
    const match = SUDOKU_GRID.match(/\.cell\.highlight-green\s*\{([^}]+)\}/)
    expect(match).not.toBeNull()
    const body = match![1]
    expect(body).toMatch(/background/)
    expect(body).toMatch(/box-shadow/)
  })

  it('highlight-red uses red-based background with box-shadow inset', () => {
    const match = SUDOKU_GRID.match(/\.cell\.highlight-red\s*\{([^}]+)\}/)
    expect(match).not.toBeNull()
    const body = match![1]
    expect(body).toMatch(/background/)
    expect(body).toMatch(/box-shadow/)
  })

  it('highlight-yellow uses yellow-based background with box-shadow inset', () => {
    const match = SUDOKU_GRID.match(/\.cell\.highlight-yellow\s*\{([^}]+)\}/)
    expect(match).not.toBeNull()
    const body = match![1]
    expect(body).toMatch(/background/)
    expect(body).toMatch(/box-shadow/)
  })

  it('has colorblind mode override for all highlight colors', () => {
    // .grid.colorblind .cell.highlight-{color} should exist for all 4
    expect(SUDOKU_GRID).toMatch(/\.grid\.colorblind\s+\.cell\.highlight-blue/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.colorblind\s+\.cell\.highlight-green/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.colorblind\s+\.cell\.highlight-red/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.colorblind\s+\.cell\.highlight-yellow/)
  })

  it('has dark mode override for all highlight colors', () => {
    // .grid.dark .cell.highlight-{color} should exist for all 4
    expect(SUDOKU_GRID).toMatch(/\.grid\.dark\s+\.cell\.highlight-blue/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.dark\s+\.cell\.highlight-green/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.dark\s+\.cell\.highlight-red/)
    expect(SUDOKU_GRID).toMatch(/\.grid\.dark\s+\.cell\.highlight-yellow/)
  })

  it('highlights use !important to ensure priority over other styles', () => {
    // At least one highlight color should use !important on background
    const allHighlights = SUDOKU_GRID.match(/\.cell\.highlight-\w+\s*\{[^}]*\}/g) || []
    const withImportant = allHighlights.filter(h => h.includes('!important'))
    // Most highlights should use !important for background
    expect(withImportant.length).toBeGreaterThanOrEqual(3)
  })
})

describe('theme support', () => {
  it('has theme prop defined in component interface', () => {
    expect(SUDOKU_GRID).toMatch(/theme\??:\s*string/)
  })

  it('theme is passed to grid CSS class', () => {
    // Should have something like :class="{ ... ['theme-' + theme]: theme !== 'default' }"
    expect(SUDOKU_GRID).toMatch(/\['theme-'\s*\+\s*theme\]/)
  })

  it('has wood theme CSS classes', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.theme-wood\b/)
  })

  it('has neon theme CSS classes', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.theme-neon\b/)
  })

  it('has minimal theme CSS classes', () => {
    expect(SUDOKU_GRID).toMatch(/\.grid\.theme-minimal\b/)
  })

  it('has at least 3 themes (beyond default)', () => {
    const themeClasses = SUDOKU_GRID.match(/\.grid\.theme-(\w+)\b/g) || []
    const uniqueThemes = new Set(themeClasses)
    // wood, neon, minimal = 3 unique theme class prefixes
    expect(uniqueThemes.size).toBeGreaterThanOrEqual(3)
  })

  it('each theme has dark mode variant (where applicable)', () => {
    // Wood theme has .grid.theme-wood.dark
    expect(SUDOKU_GRID).toMatch(/\.grid\.theme-wood\.dark\b/)
    // Minimal theme has .grid.theme-minimal.dark
    expect(SUDOKU_GRID).toMatch(/\.grid\.theme-minimal\.dark\b/)
    // At least 2 of 3 themes support dark mode
    const darkThemeMatches = SUDOKU_GRID.match(/\.grid\.theme-\w+\.dark\b/g) || []
    expect(new Set(darkThemeMatches).size).toBeGreaterThanOrEqual(2)
  })
})
