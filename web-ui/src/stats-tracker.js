// Stats tracking utility for Sudoku Dojo
const STATS_KEY = 'sudoku-dojo-stats'
const HISTORY_KEY = 'sudoku-dojo-history'

export function getStats() {
  try {
    return JSON.parse(localStorage.getItem(STATS_KEY) || '{}')
  } catch (e) { return {} }
}

export function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  } catch (e) { return [] }
}

export function recordSolve({ time, hints, difficulty, type = 'free' }) {
  const stats = getStats()
  const history = getHistory()

  // Update totals
  stats.totalSolved = (stats.totalSolved || 0) + 1
  stats.totalTime = (stats.totalTime || 0) + time

  // Best time
  if (!stats.bestTime || time < stats.bestTime) {
    stats.bestTime = time
  }

  // Perfect solves (no hints)
  if (hints === 0) {
    stats.perfectSolves = (stats.perfectSolves || 0) + 1
  }

  // By difficulty
  if (!stats.byDifficulty) stats.byDifficulty = {}
  const diffKey = (difficulty || 'medium').toLowerCase()
  stats.byDifficulty[diffKey] = (stats.byDifficulty[diffKey] || 0) + 1

  // By best time per difficulty
  if (!stats.bestTimeByDifficulty) stats.bestTimeByDifficulty = {}
  if (!stats.bestTimeByDifficulty[diffKey] || time < stats.bestTimeByDifficulty[diffKey]) {
    stats.bestTimeByDifficulty[diffKey] = time
  }

  // Time-of-day tracking
  const hour = new Date().getHours()
  if (hour >= 0 && hour < 5) stats.nightSolve = true
  if (hour >= 5 && hour < 7) stats.earlySolve = true

  // Save stats
  localStorage.setItem(STATS_KEY, JSON.stringify(stats))

  // Add to history
  history.push({
    id: Date.now().toString(36),
    timestamp: Date.now(),
    type,
    time,
    hints,
    difficulty: diffKey,
    text: `${diffKey} puzzle solved in ${formatTimeMs(time)}${hints > 0 ? ` (${hints} hints)` : ''}`
  })

  // Keep last 100 entries
  if (history.length > 100) history.splice(0, history.length - 100)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history))

  // Check and unlock achievements
  checkAchievements(stats, history)
}

export function recordDailyComplete({ time, hints }) {
  const stats = getStats()

  stats.dailiesCompleted = (stats.dailiesCompleted || 0) + 1

  // Streak tracking
  const today = new Date().toISOString().split('T')[0]
  const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0]

  if (stats.lastDailyDate === yesterday) {
    stats.currentStreak = (stats.currentStreak || 0) + 1
  } else if (stats.lastDailyDate !== today) {
    stats.currentStreak = 1
  }
  stats.lastDailyDate = today

  if (!stats.bestStreak || stats.currentStreak > stats.bestStreak) {
    stats.bestStreak = stats.currentStreak
  }

  localStorage.setItem(STATS_KEY, JSON.stringify(stats))

  // Also record as a solve
  recordSolve({ time, hints, difficulty: 'daily', type: 'daily' })
}

export function getStatsForAchievements() {
  const stats = getStats()
  try {
    const tutorials = JSON.parse(localStorage.getItem('sudokuCompletedTutorials') || '[]')
    stats.tutorialsCompleted = Array.isArray(tutorials) ? tutorials.length : (new Set(tutorials)).size
  } catch (e) {
    stats.tutorialsCompleted = 0
  }
  return stats
}

function checkAchievements(stats) {
  const ACH_KEY = 'sudoku-dojo-achievements'
  let dates = {}
  try { dates = JSON.parse(localStorage.getItem(ACH_KEY) || '{}') } catch (e) {}

  const checks = [
    { id: 'first-solve', cond: stats.totalSolved >= 1 },
    { id: 'five-solves', cond: stats.totalSolved >= 5 },
    { id: 'ten-solves', cond: stats.totalSolved >= 10 },
    { id: 'fifty-solves', cond: stats.totalSolved >= 50 },
    { id: 'first-daily', cond: (stats.dailiesCompleted || 0) >= 1 },
    { id: 'week-streak', cond: (stats.currentStreak || 0) >= 7 },
    { id: 'month-streak', cond: (stats.currentStreak || 0) >= 30 },
    { id: 'speed-demon', cond: stats.bestTime > 0 && stats.bestTime < 120000 },
    { id: 'speed-king', cond: stats.bestTime > 0 && stats.bestTime < 60000 },
    { id: 'no-hints', cond: (stats.perfectSolves || 0) >= 1 },
    { id: 'five-perfect', cond: (stats.perfectSolves || 0) >= 5 },
    { id: 'night-owl', cond: !!stats.nightSolve },
    { id: 'early-bird', cond: !!stats.earlySolve },
  ]

  let changed = false
  for (const { id, cond } of checks) {
    if (cond && !dates[id]) {
      dates[id] = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
      changed = true
    }
  }

  if (changed) {
    localStorage.setItem(ACH_KEY, JSON.stringify(dates))
  }
}

function formatTimeMs(ms) {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? `${m}m ${sec}s` : `${sec}s`
}
