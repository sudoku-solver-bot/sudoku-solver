<template>
  <div
    class="stats-page"
    :class="{ dark: isDark }"
  >
    <!-- Header -->
    <div class="stats-header">
      <button
        class="back-btn"
        @click="$emit('back')"
      >
        ← Back
      </button>
      <h2>📊 Statistics</h2>
    </div>

    <!-- Overview cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <span class="stat-icon">🧩</span>
        <span class="stat-value">{{ totalSolved }}</span>
        <span class="stat-label">Puzzles Solved</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">⚡</span>
        <span class="stat-value">{{ formatTime(bestTime) }}</span>
        <span class="stat-label">Best Time</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">⏱️</span>
        <span class="stat-value">{{ formatTime(avgTime) }}</span>
        <span class="stat-label">Average Time</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🔥</span>
        <span class="stat-value">{{ currentStreak }}</span>
        <span class="stat-label">Current Streak</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🏆</span>
        <span class="stat-value">{{ bestStreak }}</span>
        <span class="stat-label">Best Streak</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🧠</span>
        <span class="stat-value">{{ perfectSolves }}</span>
        <span class="stat-label">No-Hint Solves</span>
      </div>
    </div>

    <!-- Difficulty breakdown -->
    <div class="section">
      <h3>By Difficulty</h3>
      <div class="difficulty-bars">
        <div
          v-for="d in difficultyStats"
          :key="d.name"
          class="diff-row"
        >
          <span class="diff-label">{{ d.icon }} {{ d.name }}</span>
          <div class="diff-bar">
            <div
              class="diff-fill"
              :style="{ width: d.percent + '%', background: d.color }"
            />
          </div>
          <span class="diff-count">{{ d.count }}</span>
          <span
            v-if="d.best"
            class="diff-best"
          >⏱ {{ formatTime(d.best) }}</span>
        </div>
      </div>
    </div>

    <!-- Time distribution -->
    <div class="section">
      <h3>Time Distribution</h3>
      <div class="time-bars">
        <div
          v-for="t in timeDistribution"
          :key="t.label"
          class="time-row"
        >
          <span class="time-label">{{ t.label }}</span>
          <div class="time-bar-track">
            <div
              class="time-bar-fill"
              :style="{ width: t.percent + '%' }"
            />
          </div>
          <span class="time-count">{{ t.count }}</span>
        </div>
      </div>
    </div>

    <!-- Recent activity -->
    <div class="section">
      <h3>Recent Activity</h3>
      <div
        v-if="recentActivity.length === 0"
        class="empty"
      >
        No activity yet. Start solving!
      </div>
      <div
        v-for="a in recentActivity"
        :key="a.id"
        class="activity-row"
      >
        <span class="activity-icon">{{ a.icon }}</span>
        <span class="activity-text">{{ a.text }}</span>
        <span class="activity-time">{{ a.timeAgo }}</span>
      </div>
    </div>

    <!-- Reset -->
    <div class="section reset-section">
      <button
        class="reset-btn"
        @click="confirmReset"
      >
        🗑️ Reset All Statistics
      </button>
      <button
        class="export-btn"
        @click="exportCSV"
      >
        📥 Export as CSV
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">

import { computed, onMounted, ref } from 'vue'

const STATS_KEY = 'sudoku-dojo-stats'
const HISTORY_KEY = 'sudoku-dojo-history'

const props = defineProps({
    isDark: { type: Boolean, default: false }
  })
const emit = defineEmits(['back', 'reset-stats'])

const stats = ref({})
const history = ref([])

onMounted(() => {
  try {
    const s = localStorage.getItem(STATS_KEY)
    if (s) stats.value = JSON.parse(s)
  } catch (e) {}
  try {
    const h = localStorage.getItem(HISTORY_KEY)
    if (h) history.value = JSON.parse(h)
  } catch (e) {}
})

const totalSolved = computed(() => stats.value.totalSolved || 0)
const bestTime = computed(() => stats.value.bestTime || 0)
const avgTime = computed(() => {
  const t = stats.value.totalTime || 0
  const n = stats.value.totalSolved || 0
  return n > 0 ? Math.round(t / n) : 0
})
const currentStreak = computed(() => stats.value.currentStreak || 0)
const bestStreak = computed(() => stats.value.bestStreak || 0)
const perfectSolves = computed(() => stats.value.perfectSolves || 0)

const difficultyStats = computed(() => {
  const byDiff = stats.value.byDifficulty || {}
  const bestByDiff = stats.value.bestTimeByDifficulty || {}
  const max = Math.max(1, ...Object.values(byDiff))
  return [
    { name: 'Easy', icon: '🟢', color: '#34a853', count: byDiff.easy || 0, percent: ((byDiff.easy || 0) / max) * 100, best: bestByDiff.easy },
    { name: 'Medium', icon: '🟡', color: '#fbbc04', count: byDiff.medium || 0, percent: ((byDiff.medium || 0) / max) * 100, best: bestByDiff.medium },
    { name: 'Hard', icon: '🟠', color: '#ff6d01', count: byDiff.hard || 0, percent: ((byDiff.hard || 0) / max) * 100, best: bestByDiff.hard },
    { name: 'Expert', icon: '🔴', color: '#ea4335', count: byDiff.expert || 0, percent: ((byDiff.expert || 0) / max) * 100, best: bestByDiff.expert },
  ]
})

const timeDistribution = computed(() => {
  const h = history.value
  const buckets = { '< 1 min': 0, '1-3 min': 0, '3-5 min': 0, '5-10 min': 0, '> 10 min': 0 }
  for (const entry of h) {
    const t = entry.time || 0
    if (t < 60000) buckets['< 1 min']++
    else if (t < 180000) buckets['1-3 min']++
    else if (t < 300000) buckets['3-5 min']++
    else if (t < 600000) buckets['5-10 min']++
    else buckets['> 10 min']++
  }
  const max = Math.max(1, ...Object.values(buckets))
  return Object.entries(buckets).map(([label, count]) => ({
    label, count, percent: (count / max) * 100
  }))
})

const recentActivity = computed(() => {
  return history.value.slice(-20).reverse().map(entry => ({
    id: entry.id || entry.timestamp,
    icon: entry.type === 'daily' ? '📅' : entry.type === 'tutorial' ? '📚' : '🧩',
    text: entry.text || `${entry.difficulty || ''} puzzle solved`,
    timeAgo: formatTimeAgo(entry.timestamp)
  }))
})

const formatTime = (ms) => {
  if (!ms) return '—'
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? `${m}m ${sec}s` : `${sec}s`
}

const formatTimeAgo = (ts) => {
  if (!ts) return ''
  const diff = Date.now() - ts
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  return `${days}d ago`
}

const confirmReset = () => {
  if (confirm('Reset all statistics? This cannot be undone.')) {
    localStorage.removeItem(STATS_KEY)
    localStorage.removeItem(HISTORY_KEY)
    stats.value = {}
    history.value = []
    emit('reset-stats')
  }
}

const exportCSV = () => {
  const s = stats.value
  const h = history.value
  let csv = 'Sudoku Dojo Statistics Export\n'
  csv += `Generated,${new Date().toISOString()}\n\n`
  csv += 'Summary\n'
  csv += `Total Solved,${s.totalSolved || 0}\n`
  csv += `Best Time (ms),${s.bestTime || 0}\n`
  csv += `Average Time (ms),${s.totalSolved ? Math.round((s.totalTime || 0) / s.totalSolved) : 0}\n`
  csv += `Current Streak,${s.currentStreak || 0}\n`
  csv += `Best Streak,${s.bestStreak || 0}\n`
  csv += `Perfect Solves,${s.perfectSolves || 0}\n`
  csv += `Dailies Completed,${s.dailiesCompleted || 0}\n\n`

  csv += 'Difficulty Breakdown\n'
  csv += 'Difficulty,Count\n'
  const byDiff = s.byDifficulty || {}
  for (const [k, v] of Object.entries(byDiff)) {
    csv += `${k},${v}\n`
  }
  csv += '\n'

  csv += 'History\n'
  csv += 'Timestamp,Date,Type,Time (ms),Hints,Difficulty\n'
  for (const entry of h) {
    const d = new Date(entry.timestamp).toISOString()
    csv += `${entry.timestamp},${d},${entry.type || 'free'},${entry.time || 0},${entry.hints || 0},${entry.difficulty || ''}\n`
  }

  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `sudoku-dojo-stats-${new Date().toISOString().split('T')[0]}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.stats-page {
  max-width: 560px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.stats-page.dark .stats-header { border-bottom-color: #444; }
.stats-header h2 { font-size: 20px; margin: 0; }
.stats-page.dark .stats-header h2 { color: #e0e0e0; }

.back-btn {
  background: #f0f0f0; border: none; padding: 8px 14px;
  border-radius: 8px; font-size: 14px; cursor: pointer;
}
.stats-page.dark .back-btn { background: #333; color: #ccc; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px 8px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stats-page.dark .stat-card { background: #2a2a2a; }

.stat-icon { font-size: 20px; }
.stat-value { font-size: 18px; font-weight: 700; color: #333; }
.stats-page.dark .stat-value { color: #e0e0e0; }
.stat-label { font-size: 10px; color: #888; text-transform: uppercase; }

.section {
  margin-bottom: 20px;
}
.section h3 {
  font-size: 16px;
  margin: 0 0 10px;
  color: #555;
}
.stats-page.dark .section h3 { color: #aaa; }

.difficulty-bars, .time-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diff-row, .time-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.diff-label, .time-label {
  min-width: 80px;
  font-size: 13px;
  color: #666;
}
.stats-page.dark .diff-label, .stats-page.dark .time-label { color: #aaa; }

.diff-bar, .time-bar-track {
  flex: 1;
  height: 16px;
  background: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}
.stats-page.dark .diff-bar, .stats-page.dark .time-bar-track { background: #333; }

.diff-fill, .time-bar-fill {
  height: 100%;
  border-radius: 8px;
  transition: width 0.5s ease;
  background: #4285f4;
}

.diff-count, .time-count {
  min-width: 24px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  text-align: right;
}

.diff-best {
  font-size: 11px;
  color: #4285f4;
  min-width: 60px;
  text-align: right;
}

.stats-page.dark .diff-count, .stats-page.dark .time-count { color: #ddd; }

.activity-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;
}
.stats-page.dark .activity-row { border-bottom-color: #333; }

.activity-icon { font-size: 16px; }
.activity-text { flex: 1; font-size: 13px; color: #555; }
.stats-page.dark .activity-text { color: #bbb; }
.activity-time { font-size: 11px; color: #aaa; }

.empty {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.reset-section {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #e0e0e0;
}

.reset-btn {
  background: transparent;
  border: 1px solid #e0e0e0;
  color: #999;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
}
.reset-btn:hover { border-color: #ea4335; color: #ea4335; }

.export-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
}

@media (max-width: 500px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
