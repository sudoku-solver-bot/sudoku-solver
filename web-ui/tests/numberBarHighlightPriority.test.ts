import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * NumberBar + Highlight Priority Audit
 *
 * Verifies:
 * - NumberBar.vue: remaining count display, digit disabling at count >= 9,
 *   progress bar width reflects count
 * - SudokuGrid.vue: CSS class application priority — selected cell gets
 *   highest priority, same-value and peer highlighting only when not selected
 *
 * Refs #707
 */

const NUMBER_BAR = readFileSync(
  join(__dirname, '..', 'src', 'components', 'NumberBar.vue'),
  'utf-8',
)
const SUDOKU_GRID = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SudokuGrid.vue'),
  'utf-8',
)

describe('NumberBar — remaining count display', () => {
  it('shows remaining count for each digit', () => {
    expect(NUMBER_BAR).toMatch(/remaining/)
  })

  it('remaining count is 9 - count[n]', () => {
    expect(NUMBER_BAR).toMatch(/9\s*-\s*counts/)
  })

  it('renders remaining as a separate span', () => {
    expect(NUMBER_BAR).toMatch(/digit-remaining/)
  })
})

describe('NumberBar — digit disabling', () => {
  it('applies complete class when count >= 9', () => {
    // The :class binding should have complete: counts[n] >= 9
    expect(NUMBER_BAR).toMatch(/complete:\s*counts\[/i)
    expect(NUMBER_BAR).toMatch(/counts\[[^\]]*\]\s*>=?\s*9/)
  })

  it('complete class applies opacity reduction', () => {
    // Source verification: .complete should set opacity < 1
    expect(NUMBER_BAR).toMatch(/\.complete[^{]*\{[^}]*opacity\s*:/)
  })
})

describe('NumberBar — progress bar', () => {
  it('has progress bar for each digit', () => {
    expect(NUMBER_BAR).toMatch(/digit-progress/)
    expect(NUMBER_BAR).toMatch(/digit-fill/)
  })

  it('progress bar width reflects count / 9 * 100', () => {
    expect(NUMBER_BAR).toMatch(/counts\[[^\]]*\]\s*\/\s*9\s*\*\s*100/)
  })
})

describe('SudokuGrid — highlight class priority', () => {
  it('defines selected class', () => {
    expect(SUDOKU_GRID).toMatch(/'?selected'?:\s*.*selected/)
  })

  it('selected class uses selectedCell prop', () => {
    expect(SUDOKU_GRID).toMatch(/selected:\s*props\.selectedCell\s*===\s*index/)
  })

  it('same-value highlighting only when selectedCell >= 0', () => {
    // The same-value conditional should be inside a selectedCell >= 0 guard
    const match = SUDOKU_GRID.match(/if\s*\(\s*props\.selectedCell\s*>=?\s*0\s*\).*\{([\s\S]*?)\n\s*\}/)
    expect(match).not.toBeNull()
    expect(match![1]).toMatch(/same-value/)
  })

  it('peer highlighting (related-row/col/region) only when selectedCell >= 0', () => {
    const match = SUDOKU_GRID.match(/if\s*\(\s*props\.selectedCell\s*>=?\s*0\s*\).*\{([\s\S]*?)\n\s*\}/)
    expect(match).not.toBeNull()
    expect(match![1]).toMatch(/related-row/)
    expect(match![1]).toMatch(/related-col/)
    expect(match![1]).toMatch(/related-region/)
  })

  it('same-value excludes the selected cell itself', () => {
    // classes['same-value'] = ... !isEmpty(...) && ... && index !== props.selectedCell
    expect(SUDOKU_GRID).toMatch(/same-value[\s\S]*?index\s*!==\s*props\.selectedCell/)
  })

  it('related-row excludes the selected cell itself', () => {
    // classes['related-row'] = ... && index !== props.selectedCell
    expect(SUDOKU_GRID).toMatch(/related-row.*index\s*!==?\s*props\.selectedCell/)
  })

  it('selected class defined in the classes object (pre-same-value scope)', () => {
    // selected is defined in the base classes object (before the if block)
    const classesObj = SUDOKU_GRID.match(/const\s+classes\s*=\s*\{([\s\S]*?)\n\s*\}/)
    expect(classesObj).not.toBeNull()
    expect(classesObj![1]).toMatch(/selected/)
    // same-value and related-row are defined OUTSIDE the base object
    // (inside the if block), so selected always takes precedence
  })
})
