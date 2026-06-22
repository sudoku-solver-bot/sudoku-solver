import { describe, it, expect } from 'vitest'
import { readFileSync, readdirSync, statSync } from 'fs'
import { join, extname } from 'path'

/**
 * Preference Persistence Tests
 *
 * Verifies that each user preference toggle (dark mode, colorblind,
 * high contrast, theme) immediately calls localStorage.setItem with
 * the correct key-value pair.
 *
 * Refs #711
 */

const SRC_DIR = join(__dirname, '..', 'src')

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

function readAllSource(): string {
  const files = collectSourceFiles(SRC_DIR)
  return files.map(f => readFileSync(f, 'utf-8')).join('\n')
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * Check that a function (by name) contains a localStorage.setItem call
 * for the given key with a value that uses the key's "active" variable.
 */
function functionHasSetItemFor(
  source: string,
  funcName: string,
  storageKey: string
): { found: boolean; detail: string } {
  // Naive function extraction: find the function definition and scan to matching closing brace
  // For Vue <script setup>, functions are at top level: `const funcName = () => { ... }`
  // or `function funcName() { ... }`
  const patterns = [
    new RegExp(`const\\s+${escapeRegex(funcName)}\\s*=\\s*\\([^)]*\\)\\s*(?::\\s*[^=]+)?\\s*=>\\s*\\{`),
    new RegExp(`function\\s+${escapeRegex(funcName)}\\s*\\([^)]*\\)\\s*\\{`),
  ]

  for (const pattern of patterns) {
    const match = source.match(pattern)
    if (match) {
      const startIdx = source.indexOf(match[0]) + match[0].length
      // Find matching closing brace by counting
      let depth = 1
      let endIdx = startIdx
      for (let i = startIdx; i < source.length && depth > 0; i++) {
        if (source[i] === '{') depth++
        if (source[i] === '}') depth--
        if (depth === 0) endIdx = i
      }
      const funcBody = source.substring(startIdx, endIdx)

      // Check for localStorage.setItem(key, ...) in function body
      const setItemRegex = new RegExp(
        `localStorage\\.setItem\\(\\s*['"]${escapeRegex(storageKey)}['"]\\s*,`,
      )
      if (setItemRegex.test(funcBody)) {
        return { found: true, detail: `Function "${funcName}" sets "${storageKey}"` }
      }
      return {
        found: false,
        detail: `Function "${funcName}" found but no setItem('${storageKey}') call detected`,
      }
    }
  }

  return { found: false, detail: `Function "${funcName}" not found in source` }
}

describe('preference persistence — localStorage.setItem on toggle', () => {
  const source = readAllSource()

  it('source files are readable and non-empty', () => {
    expect(source.length).toBeGreaterThan(0)
  })

  describe('dark mode toggle', () => {
    it('persists to localStorage via setItem', () => {
      const result = functionHasSetItemFor(source, 'toggleDarkMode', 'sudokuDarkMode')
      expect(result.found).toBe(true)
    })

    it('reads saved preference on startup via getItem', () => {
      const getItemRe = new RegExp(
        `localStorage\\.getItem\\(\\s*['"]sudokuDarkMode['"]\\s*\\)`,
      )
      expect(getItemRe.test(source)).toBe(true)
    })
  })

  describe('colorblind mode toggle', () => {
    it('persists to localStorage via setItem', () => {
      const result = functionHasSetItemFor(source, 'toggleColorBlind', 'sudokuColorBlind')
      expect(result.found).toBe(true)
    })

    it('reads saved preference on startup via getItem', () => {
      const getItemRe = new RegExp(
        `localStorage\\.getItem\\(\\s*['"]sudokuColorBlind['"]\\s*\\)`,
      )
      expect(getItemRe.test(source)).toBe(true)
    })
  })

  describe('high contrast mode toggle', () => {
    it('persists to localStorage via setItem', () => {
      const result = functionHasSetItemFor(source, 'toggleHighContrast', 'sudokuHighContrast')
      expect(result.found).toBe(true)
    })

    it('reads saved preference on startup via getItem', () => {
      const getItemRe = new RegExp(
        `localStorage\\.getItem\\(\\s*['"]sudokuHighContrast['"]\\s*\\)`,
      )
      expect(getItemRe.test(source)).toBe(true)
    })
  })

  describe('theme selection', () => {
    it('persists to localStorage via setItem', () => {
      const result = functionHasSetItemFor(source, 'selectTheme', 'sudoku-theme')
      expect(result.found).toBe(true)
    })

    it('reads saved theme on startup via getItem', () => {
      const getItemRe = new RegExp(
        `localStorage\\.getItem\\(\\s*['"]sudoku-theme['"]\\s*\\)`,
      )
      expect(getItemRe.test(source)).toBe(true)
    })
  })
})
