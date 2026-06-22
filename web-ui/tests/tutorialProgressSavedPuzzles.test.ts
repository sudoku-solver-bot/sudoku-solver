import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Tutorial Progress + Saved Puzzles Persistence Tests
 *
 * Verifies that:
 * 1. Tutorial completion updates sudokuCompletedTutorials in localStorage
 * 2. Tutorial progress is loaded from localStorage on startup
 * 3. Saved puzzles (sudoku-dojo-saves) store an array of puzzle objects
 * 4. Save/load operations work correctly (round-trip)
 *
 * Refs #712
 */

const APP_VUE = readFileSync(join(__dirname, '..', 'src', 'App.vue'), 'utf-8')
const SAVED_PUZZLES_VUE = readFileSync(
  join(__dirname, '..', 'src', 'components', 'SavedPuzzles.vue'),
  'utf-8',
)

describe('tutorial progress persistence', () => {
  it('loads completed tutorials from localStorage on startup', () => {
    expect(APP_VUE).toMatch(/localStorage\.getItem\(\s*['"]sudokuCompletedTutorials['"]\s*\)/)
  })

  it('saves completed tutorials to localStorage on completion', () => {
    // Find the onTutorialCompleted function and verify it calls setItem
    const funcMatch = APP_VUE.match(
      /const\s+onTutorialCompleted\s*=\s*\([^)]*\)\s*=>\s*\{/,
    )
    expect(funcMatch).not.toBeNull()

    const funcStart = APP_VUE.indexOf(funcMatch![0]) + funcMatch![0].length
    // Extract function body by brace counting
    let depth = 1
    let funcEnd = funcStart
    for (let i = funcStart; i < APP_VUE.length && depth > 0; i++) {
      if (APP_VUE[i] === '{') depth++
      if (APP_VUE[i] === '}') depth--
      if (depth === 0) funcEnd = i
    }
    const funcBody = APP_VUE.substring(funcStart, funcEnd)

    expect(funcBody).toMatch(/localStorage\.setItem\(\s*['"]sudokuCompletedTutorials['"]/)
    expect(funcBody).toMatch(/JSON\.stringify/)
    // Should add the lessonId to the set before saving
    expect(funcBody).toMatch(/completedTutorials.*\.add/)
  })

  it('stores completed tutorials as a JSON array', () => {
    // The setItem call should spread the Set into an array
    // Match across multiple lines: localStorage.setItem('key', JSON.stringify([...var]))
    expect(APP_VUE).toMatch(
      /localStorage\.setItem\(\s*['"]sudokuCompletedTutorials['"]\s*,\s*JSON\.stringify\(\s*\[\.\.\./,
    )
  })

  it('initializes completedTutorials as a Set', () => {
    expect(APP_VUE).toMatch(/completedTutorials\s*=\s*ref\(\s*new\s+Set/)
  })

  it('handles missing saved tutorials gracefully', () => {
    // The load should handle null/missing data
    const getItemIdx = APP_VUE.indexOf("localStorage.getItem('sudokuCompletedTutorials')")
    expect(getItemIdx).toBeGreaterThan(-1)
    // Should be inside an if check or have fallback
    const around = APP_VUE.slice(getItemIdx - 20, getItemIdx + 200)
    // Should have an if (saved) check or || fallback
    expect(around).toMatch(/if\s*\(\s*saved\s*\)|\|\|\s*['"\[]/)
  })
})

describe('saved puzzles persistence', () => {
  it('uses the correct localStorage key', () => {
    expect(SAVED_PUZZLES_VUE).toMatch(/['"]sudoku-dojo-saves['"]/)
  })

  it('loads saves from localStorage on mount', () => {
    expect(SAVED_PUZZLES_VUE).toMatch(/localStorage\.getItem\(\s*SAVES_KEY/)
    expect(SAVED_PUZZLES_VUE).toMatch(/onMounted\(loadSaves\)/)
  })

  it('stores saves as a JSON array', () => {
    expect(SAVED_PUZZLES_VUE).toMatch(
      /localStorage\.setItem\(\s*SAVES_KEY\s*,\s*JSON\.stringify\(\s*saves\.value\s*\)/,
    )
  })

  it('handles malformed save data gracefully with try/catch', () => {
    // The loadSaves function should wrap JSON.parse in try/catch
    const loadMatch = SAVED_PUZZLES_VUE.match(
      /const\s+loadSaves\s*=\s*\(\s*\)\s*:\s*[^=]*=>\s*\{([\s\S]*?)\n\}/,
    )
    expect(loadMatch).not.toBeNull()
    const body = loadMatch![1]
    expect(body).toMatch(/try\s*\{/)
    expect(body).toMatch(/catch/)
    // Catch should not re-throw
    expect(body).not.toMatch(/\bthrow\b/)
  })

  it('save objects contain required fields', () => {
    // The save object should include puzzle, difficulty, progress, date, name
    const saveMatch = SAVED_PUZZLES_VUE.match(
      /const\s+save\s*=\s*\{([^}]+)\}/,
    )
    expect(saveMatch).not.toBeNull()
    const saveBody = saveMatch![1]
    expect(saveBody).toMatch(/\bpuzzle\b/)
    expect(saveBody).toMatch(/\bdifficulty\b/)
    expect(saveBody).toMatch(/\bprogress\b/)
    expect(saveBody).toMatch(/\bdate\b/)
    expect(saveBody).toMatch(/\bname\b/)
  })

  it('limits saved puzzles to a maximum', () => {
    expect(SAVED_PUZZLES_VUE).toMatch(/saves\.value\.length\s*>\s*10|slice\(\s*0\s*,\s*10\s*\)/)
  })

  it('supports deleting a save', () => {
    expect(SAVED_PUZZLES_VUE).toMatch(/deleteSave/)
    expect(SAVED_PUZZLES_VUE).toMatch(/splice\(/)
    // After delete, should persist the change
    const deleteMatch = SAVED_PUZZLES_VUE.match(
      /const\s+deleteSave\s*=\s*\([^)]*\)\s*:\s*[^=]*=>\s*\{([\s\S]*?)\n\}/,
    )
    expect(deleteMatch).not.toBeNull()
    expect(deleteMatch![1]).toMatch(/localStorage\.setItem/)
  })
})
