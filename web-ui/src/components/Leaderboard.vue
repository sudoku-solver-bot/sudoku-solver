<template>
  <div class="leaderboard" :class="{ dark: isDark }">
    <!-- Header -->
    <div class="lb-header">
      <button class="back-btn" @click="$emit('back')">← Back</button>
      <h2>🏆 Leaderboard</h2>
      <span class="lb-badge">OPT-IN</span>
    </div>

    <!-- Opt-in prompt -->
    <div v-if="!optedIn" class="opt-in-card">
      <div class="opt-in-icon">🏆</div>
      <h3>Join the Leaderboard?</h3>
      <p>Share your daily challenge times and see how you rank against other solvers. Completely optional!</p>
      <div class="name-input">
        <label>Display Name</label>
        <input v-model="playerName" placeholder="Your name" maxlength="20" />
      </div>
      <button class="join-btn" :disabled="!playerName.trim()" @click="joinLeaderboard">
        Join Leaderboard
      </button>
    </div>

    <!-- Leaderboard content -->
    <div v-else class="lb-content">
      <!-- Your stats -->
      <div class="your-stats">
        <div class="player-card">
          <span class="player-rank">#{{ yourRank || '—' }}</span>
          <span class="player-name">{{ playerName }}</span>
          <span class="player-streak">🔥 {{ yourStreak }}</span>
        </div>
      </div>

      <!-- Tabs -->
      <div class="lb-tabs">
        <button :class="{ active: tab === 'daily' }" @click="tab = 'daily'">Today</button>
        <button :class="{ active: tab === 'weekly' }" @click="tab = 'weekly'">This Week</button>
        <button :class="{ active: tab === 'alltime' }" @click="tab = 'alltime'">All Time</button>
      </div>

      <!-- Rankings -->
      <div class="rankings">
        <div v-if="rankings.length === 0" class="empty-state">
          <span>🎯</span>
          <p>No entries yet. Complete today's daily challenge!</p>
        </div>
        <div
          v-for="(entry, i) in rankings"
          :key="entry.id"
          class="rank-row"
          :class="{ 'is-you': entry.isYou, 'top-3': i < 3 }"
        >
          <span class="rank-num">{{ getMedal(i) }}</span>
          <span class="rank-name">{{ entry.name }}</span>
          <span class="rank-time">{{ formatTime(entry.time) }}</span>
          <span class="rank-hints">{{ entry.hints === 0 ? '🧠' : entry.hints + '💡' }}</span>
        </div>
      </div>

      <!-- Actions -->
      <div class="lb-actions">
        <button class="share-score-btn" @click="shareScore">📤 Share Score</button>
        <button class="leave-btn" @click="leaveLeaderboard">Leave Leaderboard</button>
      </div>
    </div>
  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'

const STORAGE_KEY = 'sudoku-dojo-leaderboard'
const OPT_IN_KEY = 'sudoku-dojo-lb-optin'
const NAME_KEY = 'sudoku-dojo-lb-name'

const props = defineProps({
    isDark: { type: Boolean, default: false }
  })
const emit = defineEmits(['back'])

const optedIn = ref(false)
const playerName = ref('')
const tab = ref('daily')

// Simulated leaderboard data (localStorage-based for MVP)
// In a real app, this would be a backend API
const rankings = computed(() => {
  const data = getLeaderboardData()
  const today = new Date().toISOString().split('T')[0]

  let entries = data.filter(e => {
    if (tab.value === 'daily') return e.date === today
    if (tab.value === 'weekly') {
      const weekAgo = new Date(Date.now() - 7 * 86400000).toISOString().split('T')[0]
      return e.date >= weekAgo
    }
    return true
  })

  // Best time per player
  const best = new Map()
  for (const e of entries) {
    const existing = best.get(e.name)
    if (!existing || e.time < existing.time) {
      best.set(e.name, { ...e, isYou: e.name === playerName.value })
    }
  }

  return [...best.values()]
    .sort((a, b) => a.time - b.time)
    .map(e => ({ ...e, isYou: e.name === playerName.value }))
})

const yourRank = computed(() => {
  const idx = rankings.value.findIndex(e => e.isYou)
  return idx >= 0 ? idx + 1 : null
})

const yourStreak = computed(() => {
  try {
    const saved = localStorage.getItem('sudokuDailyStreak')
    if (saved) return JSON.parse(saved).count || 0
  } catch (e) {}
  return 0
})

const getLeaderboardData = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch (e) { return [] }
}

const saveLeaderboardData = (data) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

const joinLeaderboard = () => {
  if (!playerName.value.trim()) return
  optedIn.value = true
  localStorage.setItem(OPT_IN_KEY, 'true')
  localStorage.setItem(NAME_KEY, playerName.value.trim())
  playerName.value = playerName.value.trim()
}

const leaveLeaderboard = () => {
  optedIn.value = false
  localStorage.removeItem(OPT_IN_KEY)
  localStorage.removeItem(NAME_KEY)
}

const getMedal = (i) => {
  if (i === 0) return '🥇'
  if (i === 1) return '🥈'
  if (i === 2) return '🥉'
  return `#${i + 1}`
}

const formatTime = (ms) => {
  const s = Math.floor(ms / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}

const shareScore = async () => {
  const rank = yourRank.value
  const text = rank
    ? `🏆 Sudoku Dojo Leaderboard\n\nI'm #${rank} today! Can you beat me?\n\nPlay: https://sudoku-solver-r5y8.onrender.com`
    : `🏆 Sudoku Dojo\n\nCome solve daily puzzles!\n\nPlay: https://sudoku-solver-r5y8.onrender.com`

  if (navigator.share) {
    try { await navigator.share({ title: 'Sudoku Dojo', text }) } catch (e) {}
  } else {
    await navigator.clipboard.writeText(text)
    alert('Score copied to clipboard!')
  }
}

// Submit a daily challenge score (called externally)
const submitScore = (timeMs, hints) => {
  if (!optedIn.value) return
  const data = getLeaderboardData()
  data.push({
    id: Date.now().toString(36),
    name: playerName.value,
    date: new Date().toISOString().split('T')[0],
    time: timeMs,
    hints: hints,
    timestamp: Date.now()
  })
  saveLeaderboardData(data)
}

onMounted(() => {
  const opted = localStorage.getItem(OPT_IN_KEY) === 'true'
  const name = localStorage.getItem(NAME_KEY) || ''
  optedIn.value = opted
  playerName.value = name

  // Auto-submit today's score if opted in and daily is complete
  if (opted && name) {
    try {
      const today = new Date().toISOString().split('T')[0]
      const completed = localStorage.getItem('sudokuDailyCompleted')
      const timeData = localStorage.getItem('sudokuDailyTime')
      if (completed === today && timeData) {
        const { time, hints } = JSON.parse(timeData)
        const data = getLeaderboardData()
        const alreadySubmitted = data.some(e => e.name === name && e.date === today)
        if (!alreadySubmitted) {
          submitScore(time, hints || 0)
        }
      }
    } catch (e) {}
  }
})
</script>

<style scoped>
.leaderboard {
  max-width: 480px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.lb-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.leaderboard.dark .lb-header { border-bottom-color: #444; }

.lb-header h2 { font-size: 20px; margin: 0; flex: 1; }
.leaderboard.dark .lb-header h2 { color: #e0e0e0; }

.lb-badge {
  font-size: 10px;
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.back-btn {
  background: #f0f0f0;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.leaderboard.dark .back-btn { background: #333; color: #ccc; }

/* Opt-in */
.opt-in-card {
  text-align: center;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 16px;
  margin: 16px 0;
}
.leaderboard.dark .opt-in-card { background: #2a2a2a; }

.opt-in-icon { font-size: 48px; margin-bottom: 12px; }
.opt-in-card h3 { font-size: 20px; margin: 0 0 8px; }
.leaderboard.dark .opt-in-card h3 { color: #e0e0e0; }
.opt-in-card p { font-size: 14px; color: #666; margin: 0 0 16px; }
.leaderboard.dark .opt-in-card p { color: #aaa; }

.name-input {
  margin-bottom: 16px;
}
.name-input label {
  display: block;
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
  text-align: left;
}
.name-input input {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 16px;
  box-sizing: border-box;
}
.leaderboard.dark .name-input input { background: #333; border-color: #555; color: #eee; }

.join-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}
.join-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Your stats */
.your-stats {
  margin-bottom: 16px;
}

.player-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #667eea22, #764ba222);
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid #667eea33;
}

.player-rank {
  font-size: 20px;
  font-weight: 700;
  color: #4285f4;
  min-width: 40px;
}

.player-name {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
}

.player-streak {
  font-size: 14px;
}

/* Tabs */
.lb-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  background: #f0f0f0;
  padding: 4px;
  border-radius: 10px;
}
.leaderboard.dark .lb-tabs { background: #333; }

.lb-tabs button {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  background: transparent;
  color: #666;
}
.leaderboard.dark .lb-tabs button { color: #aaa; }
.lb-tabs button.active { background: white; color: #333; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.leaderboard.dark .lb-tabs button.active { background: #444; color: #eee; }

/* Rankings */
.rankings {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 16px;
  color: #999;
}
.empty-state span { font-size: 36px; display: block; margin-bottom: 8px; }

.rank-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.rank-row.is-you {
  background: #e8f0fe;
  border: 1px solid #4285f433;
}
.leaderboard.dark .rank-row.is-you { background: #1a3a5c; }

.rank-row.top-3 { font-weight: 600; }

.rank-num { min-width: 36px; font-size: 16px; }
.rank-name { flex: 1; font-size: 14px; }
.rank-time { font-size: 14px; font-weight: 600; color: #4285f4; }
.rank-hints { font-size: 12px; min-width: 40px; text-align: right; }

/* Actions */
.lb-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  justify-content: center;
}

.share-score-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
}

.leave-btn {
  background: transparent;
  color: #999;
  border: 1px solid #e0e0e0;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
}

@media (max-width: 500px) {
  .lb-header h2 { font-size: 18px; }
  .rank-row { padding: 8px; }
}
</style>
