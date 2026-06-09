import type { SolverMetrics } from './SolvingProgress'

/**
 * Difficulty levels for Sudoku puzzles.
 *
 * Equivalent to the Kotlin DifficultyRater.Level enum.
 */
export enum Level {
  EASY = 1,
  MEDIUM = 2,
  HARD = 3,
  EXPERT = 4,
  VERY_HARD = 5,
  MASTER = 6
}

/** Display names for difficulty levels. */
export const LEVEL_NAMES: Record<Level, string> = {
  [Level.EASY]: 'Easy',
  [Level.MEDIUM]: 'Medium',
  [Level.HARD]: 'Hard',
  [Level.EXPERT]: 'Expert',
  [Level.VERY_HARD]: 'Very Hard',
  [Level.MASTER]: 'Master'
}

/** Descriptions for difficulty levels. */
export const LEVEL_DESCRIPTIONS: Record<Level, string> = {
  [Level.EASY]: 'Simple elimination only',
  [Level.MEDIUM]: 'Hidden singles required',
  [Level.HARD]: 'Naked/hidden subsets required',
  [Level.EXPERT]: 'X-Wing technique required',
  [Level.VERY_HARD]: 'Advanced techniques beyond X-Wing',
  [Level.MASTER]: 'Requires backtracking'
}

/**
 * Result of difficulty rating.
 *
 * Equivalent to the Kotlin DifficultyRater.Rating data class.
 */
export interface Rating {
  level: Level
  techniquesUsed: string[]
  backtracking: boolean
}

/**
 * Rates the difficulty of a Sudoku puzzle based on solving metrics.
 *
 * Difficulty is determined by which solving techniques were required:
 * - Level 1 (Easy): Simple elimination only
 * - Level 2 (Medium): Hidden singles needed
 * - Level 3 (Hard): Naked/hidden subsets needed
 * - Level 4 (Expert): X-Wing needed
 * - Level 5 (Master): Requires backtracking (guessing)
 */
export function rate(metrics: SolverMetrics): Rating {
  const techniquesUsed: string[] = []
  let level = Level.EASY

  // Check if backtracking was required (highest difficulty)
  const requiredBacktracking = metrics.backtrackingCount > 0
  if (requiredBacktracking) {
    level = Level.MASTER
    techniquesUsed.push('backtracking')
  }

  const eliminatorNames = [...metrics.eliminatorMetrics.keys()]

  // Check for X-Wing usage (Expert level)
  const usedXWing = eliminatorNames.some(n => n.includes('XWing')) &&
    [...metrics.eliminatorMetrics.values()].some(m => m.eliminations > 0)
  if (usedXWing && level < Level.EXPERT) {
    level = Level.EXPERT
    techniquesUsed.push('X-Wing')
  }

  // Check for hidden subset usage (Hard level)
  const hiddenSubKey = eliminatorNames.find(n => n.includes('HiddenSubset'))
  const usedHiddenSubset = hiddenSubKey != null &&
    (metrics.eliminatorMetrics.get(hiddenSubKey)?.eliminations ?? 0) > 0
  if (usedHiddenSubset && level < Level.HARD) {
    level = Level.HARD
    techniquesUsed.push('hidden subsets')
  }

  // Check for group candidate (naked pairs/triples) usage (Hard level)
  const groupKey = eliminatorNames.find(n => n.includes('Group'))
  const usedGroupCandidate = groupKey != null &&
    (metrics.eliminatorMetrics.get(groupKey)?.eliminations ?? 0) > 0
  if (usedGroupCandidate && level < Level.HARD) {
    level = Level.HARD
    if (!techniquesUsed.includes('naked subsets')) {
      techniquesUsed.push('naked subsets')
    }
  }

  // Check for exclusion (hidden singles) usage (Medium level)
  const exclusionKey = eliminatorNames.find(n => n.includes('Exclusion'))
  const usedExclusion = exclusionKey != null &&
    (metrics.eliminatorMetrics.get(exclusionKey)?.eliminations ?? 0) > 0
  if (usedExclusion && level < Level.MEDIUM) {
    level = Level.MEDIUM
    techniquesUsed.push('hidden singles')
  }

  return { level, techniquesUsed, backtracking: requiredBacktracking }
}

/** Quick difficulty check — returns just the level. */
export function rateLevel(metrics: SolverMetrics): Level {
  return rate(metrics).level
}

/** Check if a puzzle is considered "hard" (requires advanced techniques or backtracking). */
export function isHard(metrics: SolverMetrics): boolean {
  return rate(metrics).level >= Level.HARD
}
