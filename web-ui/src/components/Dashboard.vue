<template>
  <div
    class="dashboard"
    :class="{ dark: isDark }"
  >
    <!-- Welcome -->
    <div class="welcome">
      <h2>Welcome back! 👋</h2>
      <p class="subtitle">
        What would you like to do today?
      </p>
    </div>

    <!-- Quick stats -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-icon">📚</span>
        <span class="stat-number">{{ completedTutorials.size }}/{{ totalTutorials }}</span>
        <span class="stat-label">Lessons</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🔥</span>
        <span class="stat-number">{{ streak }}</span>
        <span class="stat-label">Day Streak</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🏆</span>
        <span class="stat-number">{{ currentBelt }}</span>
        <span class="stat-label">Rank</span>
      </div>
    </div>

    <!-- Action cards -->
    <div class="actions">
      <!-- Daily Challenge -->
      <button
        class="action-card daily"
        @click="$emit('daily')"
      >
        <div class="action-icon">
          📅
        </div>
        <div class="action-content">
          <h3>Daily Challenge</h3>
          <p>{{ dailyInfo }}</p>
        </div>
        <div class="action-arrow">
          →
        </div>
      </button>

      <!-- Continue Learning -->
      <button
        class="action-card learn"
        @click="$emit('learn')"
      >
        <div class="action-icon">
          📚
        </div>
        <div class="action-content">
          <h3>Continue Learning</h3>
          <p>{{ learnInfo }}</p>
        </div>
        <div class="action-arrow">
          →
        </div>
      </button>

      <!-- Free Play -->
      <button
        class="action-card play"
        @click="$emit('play')"
      >
        <div class="action-icon">
          🧩
        </div>
        <div class="action-content">
          <h3>Free Play</h3>
          <p>Generate or enter a puzzle</p>
        </div>
        <div class="action-arrow">
          →
        </div>
      </button>
    </div>

    <!-- Recent belt progress -->
    <div class="belt-progress">
      <h3>Belt Progress</h3>
      <div class="belt-track">
        <div
          v-for="belt in belts"
          :key="belt.name"
          class="belt-node"
          :class="{ earned: belt.earned, current: belt.current }"
          :style="{ '--color': belt.color }"
          :title="belt.name"
          @click="belt.earned && openCert(belt)"
        >
          <span class="belt-emoji">{{ belt.emoji }}</span>
          <span class="belt-label">{{ belt.shortName }}</span>
        </div>
      </div>
      <p
        v-if="earnedBelts.length"
        class="cert-hint"
      >
        Click an earned belt to view certificate 🏆
      </p>
    </div>

    <!-- Certificate modal -->
    <BeltCertificate
      v-if="showCert"
      :technique="certBelt.technique"
      :belt-name="certBelt.name"
      :belt-emoji="certBelt.emoji"
      :belt-color="certBelt.color"
      :tutorials-completed="completedTutorials.size"
      :total-tutorials="totalTutorials"
      @close="showCert = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import BeltCertificate from './BeltCertificate.vue'

const props = defineProps({
    completedTutorials: { type: Set, default: () => new Set() },
    totalTutorials: { type: Number, default: 15 },
    isDark: { type: Boolean, default: false }
  })

const emit = defineEmits(['daily', 'learn', 'play'])

const showCert = ref(false)
const certBelt = ref({})

const openCert = (belt) => {
  certBelt.value = belt
  showCert.value = true
}
const streak = computed(() => {
  try {
    const saved = localStorage.getItem('sudokuDailyStreak')
    if (saved) {
      const data = JSON.parse(saved)
      const today = new Date().toISOString().split('T')[0]
      const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0]
      if (data.lastDate === today || data.lastDate === yesterday) return data.count
    }
  } catch (e) {}
  return 0
})

const beltOrder = [
  { name: 'White Belt', shortName: 'Wht', emoji: '⬜', color: '#E0E0E0', lessons: 1 },
  { name: 'Yellow Belt', shortName: 'Yel', emoji: '🟡', color: '#FFD700', lessons: 2 },
  { name: 'Orange Belt', shortName: 'Org', emoji: '🟠', color: '#FF8C00', lessons: 4 },
  { name: 'Green Belt', shortName: 'Grn', emoji: '🟢', color: '#34A853', lessons: 6 },
  { name: 'Blue Belt', shortName: 'Blu', emoji: '🔵', color: '#4285F4', lessons: 8 },
  { name: 'Purple Belt', shortName: 'Pur', emoji: '🟣', color: '#9C27B0', lessons: 10 },
  { name: 'Brown Belt', shortName: 'Brn', emoji: '🟤', color: '#795548', lessons: 12 },
  { name: 'Black Belt', shortName: 'Blk', emoji: '⬛', color: '#333333', lessons: 15 },
]

const currentBelt = computed(() => {
  const count = props.completedTutorials.size
  for (let i = beltOrder.length - 1; i >= 0; i--) {
    if (count >= beltOrder[i].lessons) return beltOrder[i].emoji
  }
  return '⬜'
})

const belts = computed(() => {
  const count = props.completedTutorials.size
  return beltOrder.map(b => ({
    ...b,
    earned: count >= b.lessons,
    current: count < b.lessons && (beltOrder.indexOf(b) === 0 || count >= beltOrder[beltOrder.indexOf(b) - 1].lessons)
  }))
})

const dailyInfo = computed(() => {
  const now = new Date()
  const today = now.toISOString().split('T')[0]
  try {
    const saved = localStorage.getItem('sudokuDailyCompleted')
    if (saved === today) return '✅ Completed today!'
  } catch (e) {}
  const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
  return `${dayNames[now.getDay()]}'s puzzle is waiting`
})

const learnInfo = computed(() => {
  const count = props.completedTutorials.size
  if (count === 0) return 'Start with Naked Single ⬜'
  if (count >= props.totalTutorials) return 'All lessons complete! 🎓'
  return `${props.totalTutorials - count} lessons remaining`
})

const earnedBelts = computed(() => belts.value.filter(b => b.earned))
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.3s ease;
  padding: 8px 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome {
  text-align: center;
  margin-bottom: 20px;
}

.welcome h2 {
  font-size: 22px;
  color: #333;
  margin: 0 0 4px;
}

.dashboard.dark .welcome h2 {
  color: #e0e0e0;
}

.subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

/* Stats row */
.stats-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px 8px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dashboard.dark .stat-card {
  background: #2a2a2a;
}

.stat-icon {
  font-size: 20px;
}

.stat-number {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.dashboard.dark .stat-number {
  color: #e0e0e0;
}

.stat-label {
  font-size: 11px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Action cards */
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  width: 100%;
}

.dashboard.dark .action-card {
  background: #2d2d2d;
  border-color: #444;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.action-card.daily {
  border-color: #4285f4;
}

.dashboard.dark .action-card.daily {
  background: #1f1f2d;
}

.action-card.learn {
  border-color: #4285f4;
}

.dashboard.dark .action-card.learn {
  background: #1f1f2d;
}

.action-card.play {
  border-color: #4285f4;
}

.action-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
}

.action-content h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 2px;
}

.dashboard.dark .action-content h3 {
  color: #e0e0e0;
}

.action-content p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.dashboard.dark .action-content p {
  color: #aaa;
}

.action-arrow {
  font-size: 18px;
  color: #4285f4;
  font-weight: bold;
}

/* Belt progress */
.belt-progress h3 {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dashboard.dark .belt-progress h3 {
  color: #aaa;
}

.belt-track {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}

.belt-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  opacity: 0.3;
  transition: all 0.3s;
}

.belt-node.earned {
  opacity: 1;
  cursor: pointer;
}

.belt-node.earned:hover {
  transform: scale(1.1);
}

.belt-node.current {
  opacity: 1;
  transform: scale(1.15);
}

.belt-node.current .belt-emoji {
  animation: pulse 2s ease infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.belt-emoji {
  font-size: 20px;
}

.belt-label {
  font-size: 9px;
  color: #888;
  text-transform: uppercase;
}

.cert-hint {
  text-align: center;
  font-size: 12px;
  color: #aaa;
  margin-top: 8px;
}

@media (max-width: 380px) {
  .stats-row {
    gap: 6px;
  }
  .stat-card {
    padding: 8px 4px;
  }
  .stat-number {
    font-size: 15px;
  }
  .action-card {
    padding: 12px;
    gap: 10px;
  }
}
</style>
