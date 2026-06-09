import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { join } from 'path'

/**
 * Automated localStorage key existence checks.
 * Verifies each critical key has both getItem and setItem paths in the frontend source.
 *
 * Refs #709
 */

const CRITICAL_KEYS = [
  { name: 'sudoku-current-game', set: true, get: true },
  { name: 'sudokuDarkMode', set: true, get: true },
  { name: 'sudokuColorBlind', set: true, get: true },
  { name: 'sudokuHighContrast', set: true, get: true },
  { name: 'sudoku-theme', set: true, get: true },
  { name: 'sudoku-challenge', set: true, get: true },
  { name: 'sudokuCompletedTutorials', set: true, get: true },
  { name: 'sudoku-version', set: true, get: true },
  { name: 'sudoku-dojo-sound', set: true, get: true },
  { name: 'sudoku-dojo-saves', set: true, get: true },
  { name: 'sudoku-personal-bests', set: true, get: true }
]

/** Recursively find all .ts and .vue files in a directory. */
function findSourceFiles(dir: string): string[] {
  const { readdirSync, statSync } = require('fs') as typeof import('fs')
  const { join: j } = require('path') as typeof import('path')
  const results: string[] = []
  for (const entry of readdirSync(dir)) {
    const full = j(dir, entry)
    const stat = statSync(full)
    if (stat.isDirectory() && !entry.startsWith('.') && entry !== 'node_modules') {
      results.push(...findSourceFiles(full))
    } else if (/\.(ts|vue)$/.test(entry)) {
      results.push(full)
    }
  }
  return results
}

function readFile(path: string): string {
  return readFileSync(path, 'utf-8')
}

/** Check if a key appears anywhere in localStorage-using files. */
function keyUsed(files: { file: string; content: string }[], keyName: string, method: 'getItem' | 'setItem'): string[] {
  return files.filter(f => {
    const hasKey = f.content.includes(keyName)
    if (!hasKey) return false
    // For setItem, check if key and setItem both exist (they might be far apart but in same logical block)
    if (method === 'setItem') {
      return f.content.includes('setItem')
    }
    return f.content.includes('getItem')
  }).map(f => f.file)
}

describe('localStorage key existence checks', () => {
  const srcDir = join(process.cwd(), '..', '..', 'web-ui', 'src')
  const sourceFiles = findSourceFiles(srcDir)
  const allContent = sourceFiles.map(f => ({ file: f.replace(/.*\/web-ui\/src\//, ''), content: readFile(f) }))
    .filter(f => f.content.includes('localStorage'))

  for (const key of CRITICAL_KEYS) {
    if (key.get) {
      it(`${key.name} has getItem path`, () => {
        const filesWithGet = keyUsed(allContent, key.name, 'getItem')
        expect(filesWithGet.length).toBeGreaterThan(0)
      })
    }

    if (key.set) {
      it(`${key.name} has setItem path`, () => {
        const filesWithSet = keyUsed(allContent, key.name, 'setItem')
        expect(filesWithSet.length).toBeGreaterThan(0)
      })
    }
  }
})
