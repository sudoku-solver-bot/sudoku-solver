<template>
  <div class="dashboard" :class="{ dark: isDark }">
    <!-- Welcome -->
    <div class="welcome">
      <h2>{{ t('dashboardTitle') }} 👋</h2>
      <p class="subtitle">{{ t('dashboardSubtitle') }}</p>
    </div>

    <!-- Quick stats -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-icon">📚</span>
        <span class="stat-number">{{ completedTutorials.size }}/{{ totalTutorials }}</span>
        <span class="stat-label">Lessons</span>
      </div>
      <div class="stat-card streak-card" :class="{ 'on-fire': streak >= 3 }">
        <span class="stat-icon">{{ streak >= 7 ? '🔥' : streak >= 3 ? '⚡' : '📅' }}</span>
        <span class="stat-number">{{ streak }}</span>
        <span class="stat-label">Day Streak</span>
        <span v-if="streak >= 3" class="streak-msg">{{ streakMsg }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🏆</span>
        <span class="stat-number">{{ currentBelt }}</span>
        <span class="stat-label">Rank</span>
      </div>
    </div>

    <!-- Tip of the day -->
    <div class="tip-card" :class="{ dark: isDark }">
      <span class="tip-icon">💡</span>
      <div class="tip-content">
        <strong>Tip #{{ tipIndex + 1 }}</strong>
        <p>{{ tips[tipIndex] }}</p>
      </div>
      <button class="tip-next" @click="tipIndex = (tipIndex + 1) % tips.length">→</button>
    </div>

    <!-- Action cards -->
    <div class="actions">
      <!-- Daily Challenge -->
      <button class="action-card daily" @click="$emit('daily')">
        <div class="action-icon">📅</div>
        <div class="action-content">
          <h3>Daily Challenge</h3>
          <p>{{ dailyInfo }}</p>
        </div>
        <div class="action-arrow">→</div>
      </button>

      <!-- Continue Learning -->
      <button class="action-card learn" @click="$emit('learn')">
        <div class="action-icon">📚</div>
        <div class="action-content">
          <h3>Continue Learning</h3>
          <p>{{ learnInfo }}</p>
        </div>
        <div class="action-arrow">→</div>
      </button>

      <!-- Free Play -->
      <button class="action-card play" @click="$emit('play')">
        <div class="action-icon">🧩</div>
        <div class="action-content">
          <h3>Free Play</h3>
          <p>Generate or enter a puzzle</p>
        </div>
        <div class="action-arrow">→</div>
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
      <p v-if="earnedBelts.length" class="cert-hint">Click an earned belt to view certificate 🏆</p>
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

<script>
import { computed, ref } from 'vue'
import BeltCertificate from './BeltCertificate.vue'

import { useI18n } from '../i18n'

export default {
  name: 'Dashboard',
  props: {
    completedTutorials: { type: Set, default: () => new Set() },
    totalTutorials: { type: Number, default: 15 },
    isDark: { type: Boolean, default: false }
  },
  emits: ['daily', 'learn', 'play'],
  components: { BeltCertificate },
  setup(props) {
    const { t } = useI18n()
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

    const streakMsg = computed(() => {
      const s = streak.value
      if (s >= 30) return 'Legendary! 🌟'
      if (s >= 14) return 'Unstoppable! 💪'
      if (s >= 7) return 'On fire! 🔥'
      if (s >= 5) return 'Keep going! 🚀'
      if (s >= 3) return 'Nice streak! ✨'
      return ''
    })

    const tipIndex = ref(0)
    const tips = [
      'Start with rows/columns/boxes that have the most given numbers.',
      'Use pencil marks to track candidates in each cell.',
      'Look for "naked singles" — cells with only one possible value.',
      'Hidden singles: a number that can only go in one place in a row/col/box.',
      'Scan for pairs — two cells with the same two candidates eliminate them elsewhere.',
      'Press ? to see all keyboard shortcuts.',
      'Challenge mode: 3 mistakes = game over. Train your accuracy!',
      'Try cell colors to track patterns and groups.',
      'The daily challenge resets at midnight — keep your streak alive!',
      'Press Ctrl+Z to undo. You can undo multiple times.',
      'Print your puzzle for old-school paper solving.',
      'Share a puzzle link with friends to compete.',
    ]

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

    return { t, streak, streakMsg, currentBelt, belts, earnedBelts, dailyInfo, learnInfo, showCert, certBelt, openCert, tips, tipIndex }
  }
}
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

.streak-card.on-fire {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2) !important;
  animation: pulse-glow 2s ease-in-out infinite;
}

.streak-msg {
  font-size: 11px;
  color: #e65100;
  font-weight: 600;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,152,0,0.4); }
  50% { box-shadow: 0 0 12px 4px rgba(255,152,0,0.2); }
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

/* Tip card */
.tip-card {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; margin-bottom: 12px;
  background: #fffde7; border-radius: 12px;
  border: 1px solid #fff9c4;
}
.tip-card.dark { background: #33291a; border-color: #5d4627; }
.tip-icon { font-size: 24px; }
.tip-content { flex: 1; }
.tip-content strong { font-size: 12px; color: #f57f17; display: block; }
.tip-content p { font-size: 13px; margin: 2px 0 0; color: #555; }
.tip-card.dark .tip-content p { color: #ccc; }
.tip-next {
  background: #fff8e1; border: none; border-radius: 50%;
  width: 28px; height: 28px; cursor: pointer; font-size: 16px;
}
.tip-card.dark .tip-next { background: #5d4627; color: #ccc; }

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
  border-color: #ff6b6b;
  background: #fff5f5;
}

.dashboard.dark .action-card.daily {
  background: #2d1f1f;
}

.action-card.learn {
  border-color: #4285f4;
  background: #f0f4ff;
}

.dashboard.dark .action-card.learn {
  background: #1f1f2d;
}

.action-card.play {
  border-color: #34a853;
  background: #f0fff4;
}

.dashboard.dark .action-card.play {
  background: #1f2d1f;
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
