<template>
  <div class="daily-challenge" :class="{ dark: isDark }">
    <div class="daily-header">
      <button class="back-btn" @click="$emit('exit')">← Back</button>
      <div class="daily-title">
        <span class="calendar-icon">📅</span>
        <h2>Daily Challenge</h2>
      </div>
      <div class="streak-badge" v-if="streak > 0">
        🔥 {{ streak }}
      </div>
    </div>

    <!-- Puzzle info -->
    <div class="puzzle-info">
      <div class="info-badge" :style="{ background: challenge.beltColor }">
        <span class="belt-emoji">{{ challenge.beltEmoji }}</span>
        <span class="belt-name">{{ challenge.beltName }}</span>
      </div>
      <div class="info-date">{{ formattedDate }}</div>
    </div>

    <!-- Timer -->
    <div class="timer">
      <span class="timer-icon">⏱️</span>
      <span class="timer-value">{{ formatTime(elapsedTime) }}</span>
    </div>

    <!-- Grid -->
    <SudokuGrid
      :puzzle="puzzle"
      :given-cells="givenCells"
      :solved-cells="solvedCells"
      :selected-cell="selectedCell"
      :is-dark="isDark"
      :candidates="candidates"
      :show-candidates="true"
      @update="onCellUpdate"
      @select="onCellSelect"
    />

    <!-- Number pad -->
    <div class="number-pad">
      <button
        v-for="n in 9"
        :key="n"
        class="num-btn"
        @click="inputNumber(n)"
      >{{ n }}</button>
      <button class="num-btn erase" @click="eraseCell">⌫</button>
    </div>

    <!-- Completion overlay -->
    <div v-if="completed" class="completion-overlay" @click="$emit('exit')">
      <div class="completion-content">
        <div class="completion-emoji">🏆</div>
        <h2>Daily Complete!</h2>
        <div class="completion-stats">
          <div class="stat">
            <span class="stat-value">{{ formatTime(elapsedTime) }}</span>
            <span class="stat-label">Time</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ hintsUsed }}</span>
            <span class="stat-label">Hints</span>
          </div>
        </div>
        <p class="completion-msg">See you tomorrow! 🌟</p>
        <div class="completion-actions">
          <button class="share-btn" @click="shareResult">📤 Share</button>
          <button class="done-btn" @click="$emit('exit')">🏠 Home</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import SudokuGrid from './SudokuGrid.vue'
import { fetchDailyChallenge, solvePuzzle } from '../api'

export default {
  name: 'DailyChallenge',
  components: { SudokuGrid },
  props: {
    isDark: { type: Boolean, default: false }
  },
  emits: ['exit'],
  setup(props, { emit }) {
    const challenge = ref({ beltEmoji: '⬜', beltName: 'Loading...', difficulty: 'easy', beltColor: '#e0e0e0' })
    const puzzle = ref('.'.repeat(81))
    const givenCells = ref(new Set())
    const solvedCells = ref(new Set())
    const candidates = ref({})
    const selectedCell = ref(-1)
    const elapsedTime = ref(0)
    const hintsUsed = ref(0)
    const completed = ref(false)
    const streak = ref(0)
    let timerInterval = null

    const formattedDate = ref('')

    const formatTime = (ms) => {
      const s = Math.floor(ms / 1000)
      const m = Math.floor(s / 60)
      return `${m}:${String(s % 60).padStart(2, '0')}`
    }

    const loadChallenge = async () => {
      try {
        const data = await fetchDailyChallenge()
        challenge.value = data
        puzzle.value = data.puzzle
        candidates.value = data.candidates || {}

        givenCells.value = new Set()
        for (let i = 0; i < 81; i++) {
          if (data.puzzle[i] !== '.') givenCells.value.add(i)
        }

        formattedDate.value = new Date().toLocaleDateString('en-US', {
          weekday: 'long', month: 'short', day: 'numeric'
        })

        // Load streak from localStorage
        const saved = localStorage.getItem('sudokuDailyStreak')
        if (saved) {
          const streakData = JSON.parse(saved)
          const lastDate = streakData.lastDate
          const today = new Date().toISOString().split('T')[0]
          if (lastDate === today) {
            streak.value = streakData.count
          } else {
            const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0]
            streak.value = lastDate === yesterday ? streakData.count : 0
          }
        }
      } catch (e) {
        console.error('Failed to load daily challenge:', e)
      }
    }

    const startTimer = () => {
      stopTimer()
      timerInterval = setInterval(() => {
        if (!completed.value) elapsedTime.value += 1000
      }, 1000)
    }

    const stopTimer = () => {
      if (timerInterval) {
        clearInterval(timerInterval)
        timerInterval = null
      }
    }

    const onCellUpdate = (index, value) => {
      const chars = puzzle.value.split('')
      chars[index] = value || '.'
      puzzle.value = chars.join('')

      // Check completion
      if (!puzzle.value.includes('.')) {
        checkCompletion()
      }
    }

    const onCellSelect = (index) => {
      selectedCell.value = index
    }

    const inputNumber = (n) => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, n.toString())
      }
    }

    const eraseCell = () => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, '')
      }
    }

    const checkCompletion = async () => {
      try {
        const data = await solvePuzzle(puzzle.value, false)
        if (data.solved) {
          completed.value = true
          stopTimer()

          // Update streak
          const today = new Date().toISOString().split('T')[0]
          streak.value++
          localStorage.setItem('sudokuDailyStreak', JSON.stringify({
            lastDate: today,
            count: streak.value
          }))
          localStorage.setItem('sudokuDailyCompleted', today)
        }
      } catch (e) {
        console.error('Validation failed:', e)
      }
    }

    onMounted(() => {
      loadChallenge()
      startTimer()
    })

    onUnmounted(() => {
      stopTimer()
    })

    const shareResult = () => {
      const date = new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
      const timeStr = formatTime(elapsedTime.value)
      const belt = challenge.value.beltEmoji
      const text = `🧩 Sudoku Dojo ${date}\n${belt} ${challenge.value.difficulty}\n⏱️ ${timeStr}\n💡 ${hintsUsed.value} hints\n🔥 ${streak.value} day streak\n\nPlay at sudoku-solver-r5y8.onrender.com`
      
      if (navigator.share) {
        navigator.share({ title: 'Sudoku Dojo', text })
      } else {
        navigator.clipboard.writeText(text).then(() => {
          alert('Copied to clipboard!')
        })
      }
    }

    return {
      challenge, puzzle, givenCells, solvedCells, candidates,
      selectedCell, elapsedTime, hintsUsed, completed, streak,
      formattedDate, formatTime,
      onCellUpdate, onCellSelect, inputNumber, eraseCell, shareResult
    }
  }
}
</script>

<style scoped>
.daily-challenge {
  width: 100%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.daily-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.back-btn {
  background: #f0f0f0;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.daily-challenge.dark .back-btn {
  background: #333;
  color: #ccc;
}

.daily-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.daily-title h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.daily-challenge.dark .daily-title h2 {
  color: #e0e0e0;
}

.calendar-icon {
  font-size: 22px;
}

.streak-badge {
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
}

.puzzle-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.info-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: white;
  border: 1px solid rgba(255,255,255,0.3);
}

.info-date {
  font-size: 13px;
  color: #888;
}

.timer {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}

.daily-challenge.dark .timer {
  color: #e0e0e0;
}

.timer-icon {
  margin-right: 8px;
}

.number-pad {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-top: 12px;
  flex-wrap: wrap;
}

.num-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  color: #333;
}

.daily-challenge.dark .num-btn {
  background: #333;
  border-color: #555;
  color: #e0e0e0;
}

.num-btn:hover {
  background: #e8f0fe;
  border-color: #4285f4;
}

.num-btn.erase {
  background: #f5f5f5;
  font-size: 16px;
}

.completion-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.completion-content {
  background: white;
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  max-width: 320px;
  animation: popIn 0.5s ease;
}

.daily-challenge.dark .completion-content {
  background: #2d2d2d;
  color: #e0e0e0;
}

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.completion-emoji {
  font-size: 48px;
  margin-bottom: 12px;
}

.completion-stats {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin: 16px 0;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #4285f4;
}

.stat-label {
  font-size: 12px;
  color: #888;
}

.completion-msg {
  font-size: 16px;
  margin: 12px 0;
}

.done-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 12px;
  background: #4285f4;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.completion-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.share-btn {
  padding: 12px 24px;
  border: 2px solid #4285f4;
  border-radius: 12px;
  background: white;
  color: #4285f4;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.daily-challenge.dark .share-btn {
  background: #2d2d2d;
  color: #81c995;
  border-color: #81c995;
}

@media (max-width: 400px) {
  .num-btn {
    width: 34px;
    height: 34px;
    font-size: 15px;
  }
}
</style>
