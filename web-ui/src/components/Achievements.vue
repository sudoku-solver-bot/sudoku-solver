<template>
  <div
    class="achievements"
    :class="{ dark: isDark }"
  >
    <!-- Header -->
    <div class="ach-header">
      <button
        class="back-btn"
        @click="$emit('back')"
      >
        ← Back
      </button>
      <h2>🏅 Achievements</h2>
      <span class="ach-count">{{ earned }}/{{ total }}</span>
    </div>

    <!-- Progress bar -->
    <div class="ach-progress">
      <div class="ach-bar">
        <div
          class="ach-fill"
          :style="{ width: progressPercent + '%' }"
        />
      </div>
      <span class="ach-pct">{{ Math.round(progressPercent) }}%</span>
    </div>

    <!-- Achievement grid -->
    <div class="ach-grid">
      <div
        v-for="badge in badges"
        :key="badge.id"
        class="ach-card"
        :class="{ earned: badge.earned, locked: !badge.earned }"
      >
        <div class="ach-icon">
          {{ badge.earned ? badge.icon : '🔒' }}
        </div>
        <div class="ach-info">
          <span class="ach-name">{{ badge.name }}</span>
          <span class="ach-desc">{{ badge.earned ? badge.description : '???' }}</span>
        </div>
        <div
          v-if="badge.earned"
          class="ach-date"
        >
          {{ badge.earnedDate }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { computed, onMounted, ref } from 'vue'

const ACH_KEY = 'sudoku-dojo-achievements'

const BADGE_DEFINITIONS = [
  { id: 'first-solve', name: 'First Steps', icon: '🌟', description: 'Solve your first puzzle', check: (s: any): boolean => s.totalSolved >= 1 },
  { id: 'five-solves', name: 'Getting Started', icon: '📖', description: 'Solve 5 puzzles', check: (s: any): boolean => s.totalSolved >= 5 },
  { id: 'ten-solves', name: 'Dedicated Solver', icon: '📝', description: 'Solve 10 puzzles', check: (s: any): boolean => s.totalSolved >= 10 },
  { id: 'fifty-solves', name: 'Puzzle Master', icon: '🧩', description: 'Solve 50 puzzles', check: (s: any): boolean => s.totalSolved >= 50 },
  { id: 'first-daily', name: 'Daily Devotee', icon: '📅', description: 'Complete your first daily challenge', check: (s: any): boolean => s.dailiesCompleted >= 1 },
  { id: 'week-streak', name: 'On Fire', icon: '🔥', description: '7-day daily challenge streak', check: (s: any): boolean => s.currentStreak >= 7 },
  { id: 'month-streak', name: 'Unstoppable', icon: '💪', description: '30-day daily challenge streak', check: (s: any): boolean => s.currentStreak >= 30 },
  { id: 'speed-demon', name: 'Speed Demon', icon: '⚡', description: 'Solve a puzzle in under 2 minutes', check: (s: any): boolean => s.bestTime > 0 && s.bestTime < 120000 },
  { id: 'speed-king', name: 'Lightning Fast', icon: '🏎️', description: 'Solve a puzzle in under 1 minute', check: (s: any): boolean => s.bestTime > 0 && s.bestTime < 60000 },
  { id: 'no-hints', name: 'Pure Logic', icon: '🧠', description: 'Solve a puzzle without using hints', check: (s: any): boolean => s.perfectSolves >= 1 },
  { id: 'five-perfect', name: 'Flawless Five', icon: '💎', description: 'Solve 5 puzzles without hints', check: (s: any): boolean => s.perfectSolves >= 5 },
  { id: 'first-tutorial', name: 'Student', icon: '📚', description: 'Complete your first tutorial', check: (s: any): boolean => s.tutorialsCompleted >= 1 },
  { id: 'half-tutorials', name: 'Scholar', icon: '🎓', description: 'Complete 10 tutorials', check: (s: any): boolean => s.tutorialsCompleted >= 10 },
  { id: 'all-tutorials', name: 'Grand Master', icon: '🏆', description: 'Complete all tutorials', check: (s: any): boolean => s.tutorialsCompleted >= 20 },
  { id: 'white-belt', name: 'White Belt', icon: '⬜', description: 'Earn your White Belt', check: (s: any): boolean => s.tutorialsCompleted >= 1 },
  { id: 'black-belt', name: 'Black Belt', icon: '⬛', description: 'Earn your Black Belt', check: (s: any): boolean => s.tutorialsCompleted >= 15 },
  { id: 'night-owl', name: 'Night Owl', icon: '🦉', description: 'Solve a puzzle after midnight', check: (s: any): boolean => s.nightSolve },
  { id: 'early-bird', name: 'Early Bird', icon: '🐦', description: 'Solve a puzzle before 7am', check: (s: any): boolean => s.earlySolve },
]

const props = defineProps({
    isDark: { type: Boolean, default: false },
    stats: { type: Object, default: () => ({}) }
  })
const emit = defineEmits(['back'])

const earnedDates = ref({})

const stats = computed(() => ({
  totalSolved: props.stats.totalSolved || 0,
  dailiesCompleted: props.stats.dailiesCompleted || 0,
  currentStreak: props.stats.currentStreak || 0,
  bestTime: props.stats.bestTime || 0,
  perfectSolves: props.stats.perfectSolves || 0,
  tutorialsCompleted: props.stats.tutorialsCompleted || 0,
  nightSolve: props.stats.nightSolve || false,
  earlySolve: props.stats.earlySolve || false,
}))

const badges = computed(() => {
  return BADGE_DEFINITIONS.map(def => {
    const earned = def.check(stats.value)
  })
})

const earned = computed(() => badges.value.filter(b => b.earned).length)
const total = computed(() => badges.value.length)
const progressPercent = computed(() => total.value ? (earned.value / total.value) * 100 : 0)

// Load saved achievement dates
onMounted(() => {
  try {
    const saved = localStorage.getItem(ACH_KEY)
    if (saved) earnedDates.value = JSON.parse(saved)
  } catch (e) {}

  // Check for newly earned badges and save dates
  let changed = false
  for (const badge of badges.value) {
    if (badge.earned && !earnedDates.value[badge.id]) {
      earnedDates.value[badge.id] = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
      changed = true
    }
  }
  if (changed) {
    localStorage.setItem(ACH_KEY, JSON.stringify(earnedDates.value))
  }
})
</script>

<style scoped>
.achievements {
  max-width: 560px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.ach-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.achievements.dark .ach-header { border-bottom-color: #444; }

.ach-header h2 { font-size: 20px; margin: 0; flex: 1; }
.achievements.dark .ach-header h2 { color: #e0e0e0; }

.ach-count {
  font-size: 14px;
  color: #4285f4;
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
.achievements.dark .back-btn { background: #333; color: #ccc; }

.ach-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.ach-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}
.achievements.dark .ach-bar { background: #444; }

.ach-fill {
  height: 100%;
  background: #4285f4;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.ach-pct {
  font-size: 14px;
  font-weight: 600;
  color: #4285f4;
  min-width: 36px;
}

.ach-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ach-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  transition: all 0.2s;
}

.ach-card.earned {
  background: #f8f9fa;
  border: 1px solid #ffd54f44;
}
.achievements.dark .ach-card.earned { background: #2a2510; border-color: #ffd54f33; }

.ach-card.locked {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
}
.achievements.dark .ach-card.locked { background: #222; border-color: #333; }

.ach-icon {
  font-size: 28px;
  min-width: 36px;
  text-align: center;
}

.ach-card.locked .ach-icon { opacity: 0.4; }

.ach-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ach-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.achievements.dark .ach-name { color: #e0e0e0; }

.ach-card.locked .ach-name { color: #999; }

.ach-desc {
  font-size: 12px;
  color: #666;
}
.achievements.dark .ach-desc { color: #aaa; }
.ach-card.locked .ach-desc { color: #bbb; }

.ach-date {
  font-size: 11px;
  color: #f9a825;
  font-weight: 600;
}

@media (max-width: 500px) {
  .ach-card { padding: 10px 12px; }
  .ach-icon { font-size: 24px; }
}
</style>
