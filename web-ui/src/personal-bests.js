// Personal best times per difficulty
const STORAGE_KEY = 'sudoku-personal-bests'

export function getPersonalBests() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch { return {} }
}

export function savePersonalBest(difficulty, timeMs) {
  const bests = getPersonalBests()
  const key = difficulty.toUpperCase()
  if (!bests[key] || timeMs < bests[key]) {
    bests[key] = timeMs
    localStorage.setItem(STORAGE_KEY, JSON.stringify(bests))
    return true // new record!
  }
  return false
}

export function getPersonalBest(difficulty) {
  const bests = getPersonalBests()
  return bests[difficulty.toUpperCase()] || null
}

export function formatTimeMs(ms) {
  const totalSec = Math.floor(ms / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
}
