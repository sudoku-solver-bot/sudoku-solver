const STORAGE_KEY = 'sudoku-personal-bests'

interface PersonalBests {
  [difficulty: string]: number
}

export function getPersonalBests(): PersonalBests {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') as PersonalBests
  } catch { return {} }
}

export function savePersonalBest(difficulty: string, timeMs: number): boolean {
  const bests = getPersonalBests()
  const key = difficulty.toUpperCase()
  if (!bests[key] || timeMs < bests[key]) {
    bests[key] = timeMs
    localStorage.setItem(STORAGE_KEY, JSON.stringify(bests))
    return true
  }
  return false
}

export function getPersonalBest(difficulty: string): number | null {
  const bests = getPersonalBests()
  return bests[difficulty.toUpperCase()] || null
}

export function formatTimeMs(ms: number): string {
  const totalSec = Math.floor(ms / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
}
