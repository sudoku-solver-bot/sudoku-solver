import { describe, it, expect } from 'vitest'
import { readFileSync, readdirSync, statSync, existsSync } from 'fs'
import { join, extname } from 'path'

/**
 * localStorage Key Existence Tests
 *
 * Verifies that all critical localStorage keys have both set (setItem)
 * and get (getItem) paths in the frontend source code.
 *
 * Refs #709
 */

const SRC_DIR = join(__dirname, '..', 'src')

// Recursively collect all .ts and .vue source files
function collectSourceFiles(dir: string, files: string[] = []): string[] {
  const entries = readdirSync(dir)
  for (const entry of entries) {
    const fullPath = join(dir, entry)
    const stat = statSync(fullPath)
    if (stat.isDirectory()) {
      collectSourceFiles(fullPath, files)
    } else {
      const ext = extname(fullPath)
      if (ext === '.ts' || ext === '.vue') {
        files.push(fullPath)
      }
    }
  }
  return files
}

// Read all source files into a single searchable blob
function readAllSource(): string {
  const files = collectSourceFiles(SRC_DIR)
  return files.map(f => readFileSync(f, 'utf-8')).join('\n')
}

// Extract localStorage key names from source by finding string literals
// near localStorage.setItem/getItem calls
function extractKeys(source: string): Set<string> {
  const keys = new Set<string>()
  // Match localStorage.setItem('key', ...) and localStorage.getItem('key')
  const patterns = [
    /localStorage\.setItem\(\s*['"]([^'"]+)['"]/g,
    /localStorage\.getItem\(\s*['"]([^'"]+)['"]/g,
    /localStorage\.removeItem\(\s*['"]([^'"]+)['"]/g,
  ]
  // Also match const VAR = 'key-name' patterns used with localStorage
  const constPattern = /const\s+(\w+KEY|\w+_KEY|STORAGE_KEY|SAVES_KEY|SOUND_KEY|DISMISS_KEY)\s*=\s*['"]([^'"]+)['"]/g
  let match
  while ((match = constPattern.exec(source)) !== null) {
    keys.add(match[2])
  }
  for (const pattern of patterns) {
    while ((match = pattern.exec(source)) !== null) {
      keys.add(match[1])
    }
  }
  return keys
}

// Check if a key has setItem in source
function hasSetItem(source: string, key: string): boolean {
  // Direct usage
  const direct = new RegExp(`localStorage\\.setItem\\(\\s*['"]${escapeRegex(key)}['"]`)
  if (direct.test(source)) return true
  // Via const alias — find const X = 'key', then check localStorage.setItem(X
  const constDef = new RegExp(`const\\s+(\\w+)\\s*=\\s*['"]${escapeRegex(key)}['"]`)
  const constMatch = source.match(constDef)
  if (constMatch) {
    const alias = constMatch[1]
    const aliasUsage = new RegExp(`localStorage\\.setItem\\(\\s*${alias}\\b`)
    if (aliasUsage.test(source)) return true
  }
  return false
}

// Check if a key has getItem in source
function hasGetItem(source: string, key: string): boolean {
  const direct = new RegExp(`localStorage\\.getItem\\(\\s*['"]${escapeRegex(key)}['"]`)
  if (direct.test(source)) return true
  const constDef = new RegExp(`const\\s+(\\w+)\\s*=\\s*['"]${escapeRegex(key)}['"]`)
  const constMatch = source.match(constDef)
  if (constMatch) {
    const alias = constMatch[1]
    const aliasUsage = new RegExp(`localStorage\\.getItem\\(\\s*${alias}\\b`)
    if (aliasUsage.test(source)) return true
  }
  return false
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

describe('localStorage key existence checks', () => {
  const source = readAllSource()
  const foundKeys = extractKeys(source)

  // Critical keys that must have both set and get paths
  const criticalKeys = [
    'sudoku-current-game',
    'sudokuDarkMode',
    'sudokuColorBlind',
    'sudokuHighContrast',
    'sudoku-theme',
    'sudoku-challenge',
    'sudokuCompletedTutorials',
    'sudoku-version',
    'sudoku-dojo-sound',
    'sudoku-dojo-saves',
    'sudoku-personal-bests',
  ]

  it('source files are readable and non-empty', () => {
    expect(source.length).toBeGreaterThan(0)
    expect(foundKeys.size).toBeGreaterThan(0)
  })

  describe.each(criticalKeys)('key: %s', (key) => {
    it('has localStorage.setItem() call', () => {
      expect(hasSetItem(source, key)).toBe(true)
    })

    it('has localStorage.getItem() call', () => {
      expect(hasGetItem(source, key)).toBe(true)
    })
  })

  it('all critical keys are found in source', () => {
    const missing = criticalKeys.filter(k => !foundKeys.has(k))
    if (missing.length > 0) {
      console.error('Missing keys:', missing)
      console.log('Found keys:', [...foundKeys].sort())
    }
    expect(missing).toEqual([])
  })
})
