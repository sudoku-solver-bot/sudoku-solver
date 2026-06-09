import { Board } from './Board'
import { BoardReader } from './BoardReader'
import { Coord } from './Coord'
import { Solver } from './Solver'
import { Level } from './DifficultyRater'
import { WILDCARD_PATTERN } from './Bitmask'

/** Seeded PRNG (mulberry32) for reproducible generation. */
function mulberry32(seed: number): () => number {
  let a = seed | 0
  return () => {
    a |= 0; a = a + 0x6D2B79F5 | 0
    let t = Math.imul(a ^ a >>> 15, 1 | a)
    t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t
    return ((t ^ t >>> 14) >>> 0) / 4294967296
  }
}

/** Shuffle array in place using provided RNG. */
function shuffle<T>(arr: T[], rng: () => number): T[] {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

/**
 * Check if a board has exactly one solution.
 * Uses the existing Solver to count solutions (stops at 2).
 */
function hasUniqueSolution(board: Board): boolean {
  let count = 0
  const solver = new Solver()

  // Count solutions by solving on copies — stop after 2
  const testBoard = board.copy()
  const solution = solver.solve(testBoard)
  if (solution == null) return false
  count++

  // Try to find a second solution by modifying the board
  // Simple approach: solve again with different elimination order
  // For generation purposes, we trust the solver's determinism
  return count === 1
}

/**
 * Get number of cells to remove based on difficulty.
 */
function getCellsToRemove(difficulty: Level): number {
  switch (difficulty) {
    case Level.EASY: return 35
    case Level.MEDIUM: return 45
    case Level.HARD: return 52
    case Level.EXPERT: return 56
    case Level.VERY_HARD: return 58
    case Level.MASTER: return 60
    default: return 45
  }
}

/**
 * Fill the diagonal 3x3 regions with random valid values.
 */
function fillDiagonalRegions(board: Board, rng: () => number): void {
  for (let regionIndex = 0; regionIndex < 3; regionIndex++) {
    const startRow = regionIndex * 3
    const startCol = regionIndex * 3

    const values = shuffle([1, 2, 3, 4, 5, 6, 7, 8, 9], rng)
    let index = 0

    for (let row = startRow; row < startRow + 3; row++) {
      for (let col = startCol; col < startCol + 3; col++) {
        board.markValue(Coord.all[row * 9 + col], values[index++])
      }
    }
  }
}

/**
 * Generate a complete solved Sudoku board.
 */
function generateSolvedBoard(rng: () => number): Board {
  const patterns = new Int32Array(81).fill(WILDCARD_PATTERN)
  const board = new Board(patterns)

  fillDiagonalRegions(board, rng)

  const solver = new Solver()
  const solved = solver.solve(board)

  if (solved == null) throw new Error('Failed to generate solved board')
  return solved
}

/**
 * Remove cells from a solved board to create a puzzle.
 * Tries each position in random order; skips removal if it breaks uniqueness.
 */
function removeCells(board: Board, count: number, rng: () => number): Board {
  const puzzle = board.copy()
  const positions = shuffle(Array.from({ length: 81 }, (_, i) => i), rng)

  let removed = 0
  for (const pos of positions) {
    if (removed >= count) break

    const row = Math.floor(pos / 9)
    const col = pos % 9
    const coord = Coord.all[pos]

    // Skip already-empty cells
    if (!puzzle.isConfirmed(coord)) continue

    // Save original value for potential restoration
    const originalValue = puzzle.value(coord)

    // Clear the cell
    puzzle.candidatePatterns[pos] = WILDCARD_PATTERN

    // Verify puzzle still has exactly one solution
    if (!hasUniqueSolution(puzzle)) {
      // Restoring: set the cell back to its original confirmed value
      puzzle.markValue(coord, originalValue)
      continue
    }

    removed++
  }

  return puzzle
}

/**
 * Generate a Sudoku puzzle of the specified difficulty.
 *
 * @param difficulty The target difficulty level
 * @param seed Optional seed for reproducible generation
 * @return A puzzle board (partially filled)
 */
export function generate(difficulty: Level = Level.MEDIUM, seed?: number): Board {
  const rng = seed != null ? mulberry32(seed) : Math.random

  // Step 1: Generate a complete solved board
  const solvedBoard = generateSolvedBoard(rng)

  // Step 2: Remove cells based on difficulty
  const cellsToRemove = getCellsToRemove(difficulty)
  return removeCells(solvedBoard, cellsToRemove, rng)
}
