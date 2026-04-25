// Stats tracking utility for Sudoku Dojo

interface SolveStats {
  totalSolved?: number
  totalTime?: number
  bestTime?: number
  perfectSolves?: number
  byDifficulty?: Record<string, number>
  bestTimeByDifficulty?: Record<string, number>
  nightSolve?: boolean
  earlySolve?: boolean
  dailiesCompleted?: number
  currentStreak?: number
  bestStreak?: number
  lastDailyDate?: string
  tutorialsCompleted?: number
}

interface HistoryEntry {
  id: string
  timestamp: number
  type: string
  time: number
  hints: number
  difficulty: string
  text: string
}

const STATS_KEY = 'sudoku-dojo-stats'
const HISTORY_KEY = 'sudoku-dojo-history'

export function getStats(): SolveStats {
  try {
    return JSON.parse(localStorage.getItem(STATS_KEY) || '{}') as SolveStats
  } catch (_) { return {} }
}

export function getHistory(): HistoryEntry[] {
  try {
    return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]') as HistoryEntry[]
  } catch (_) { return [] }
}

interface RecordSolveOptions {
  time: number
  hints: number
  difficulty?: string
  type?: string
}

export function recordSolve({ time, hints, difficulty, type = 'free' }: RecordSolveOptions): void {
  const stats = getStats()
  const history = getHistory()

  stats.totalSolved = (stats.totalSolved || 0) + 1
  stats.totalTime = (stats.totalTime || 0) + time

  if (!stats.bestTime || time < stats.bestTime) {
    stats.bestTime = time
  }

  if (hints === 0) {
    stats.perfectSolves = (stats.perfectSolves || 0) + 1
  }

  if (!stats.byDifficulty) stats.byDifficulty = {}
  const diffKey = (difficulty || 'medium').toLowerCase()
  stats.byDifficulty[diffKey] = (stats.byDifficulty[diffKey] || 0) + 1

  if (!stats.bestTimeByDifficulty) stats.bestTimeByDifficulty = {}
  if (!stats.bestTimeByDifficulty[diffKey] || time < stats.bestTimeByDifficulty[diffKey]) {
    stats.bestTimeByDifficulty[diffKey] = time
  }

  const hour = new Date().getHours()
  if (hour >= 0 && hour < 5) stats.nightSolve = true
  if (hour >= 5 && hour < 7) stats.earlySolve = true

  localStorage.setItem(STATS_KEY, JSON.stringify(stats))

  history.push({
    id: Date.now().toString(36),
    timestamp: Date.now(),
    type,
    time,
    hints,
    difficulty: diffKey,
    text: `${diffKey} puzzle solved in ${formatTimeMs(time)}${hints > 0 ? ` (${hints} hints)` : ''}`
  })

  if (history.length > 100) history.splice(0, history.length - 100)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history))

  checkAchievements(stats)
}

interface RecordDailyOptions {
  time: number
  hints: number
}

export function recordDailyComplete({ time, hints }: RecordDailyOptions): void {
  const stats = getStats()

  stats.dailiesCompleted = (stats.dailiesCompleted || 0) + 1

  const today = new Date().toISOString().split('T')[0]
  const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0]

  if (stats.lastDailyDate === yesterday) {
    stats.currentStreak = (stats.currentStreak || 0) + 1
  } else if (stats.lastDailyDate !== today) {
    stats.currentStreak = 1
  }
  stats.lastDailyDate = today

  if (!stats.bestStreak || (stats.currentStreak || 0) > (stats.bestStreak || 0)) {
    stats.bestStreak = stats.currentStreak
  }

  localStorage.setItem(STATS_KEY, JSON.stringify(stats))

  recordSolve({ time, hints, difficulty: 'daily', type: 'daily' })
}

export function getStatsForAchievements(): SolveStats & { tutorialsCompleted: number } {
  const stats = getStats() as SolveStats & { tutorialsCompleted: number }
  try {
    const tutorials = JSON.parse(localStorage.getItem('sudokuCompletedTutorials') || '[]') as unknown[]
    stats.tutorialsCompleted = Array.isArray(tutorials) ? tutorials.length : (new Set(tutorials)).size
  } catch (_) {
    stats.tutorialsCompleted = 0
  }
  return stats
}

function checkAchievements(stats: SolveStats): void {
  const ACH_KEY = 'sudoku-dojo-achievements'
  let dates: Record<string, string> = {}
  try { dates = JSON.parse(localStorage.getItem(ACH_KEY) || '{}') as Record<string, string> } catch (_) {}

  const checks: Array<{ id: string; cond: boolean }> = [
    { id: 'first-solve', cond: (stats.totalSolved || 0) >= 1 },
    { id: 'five-solves', cond: (stats.totalSolved || 0) >= 5 },
    { id: 'ten-solves', cond: (stats.totalSolved || 0) >= 10 },
    { id: 'fifty-solves', cond: (stats.totalSolved || 0) >= 50 },
    { id: 'first-daily', cond: (stats.dailiesCompleted || 0) >= 1 },
    { id: 'week-streak', cond: (stats.currentStreak || 0) >= 7 },
    { id: 'month-streak', cond: (stats.currentStreak || 0) >= 30 },
    { id: 'speed-demon', cond: (stats.bestTime ?? 0) > 0 && (stats.bestTime ?? 0) < 120000 },
    { id: 'speed-king', cond: (stats.bestTime ?? 0) > 0 && (stats.bestTime ?? 0) < 60000 },
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

function formatTimeMs(ms: number): string {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? `${m}m ${sec}s` : `${sec}s`
}
