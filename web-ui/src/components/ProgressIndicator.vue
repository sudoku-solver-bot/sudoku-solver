<template>
  <div class="progress">
    <div class="progress-header">
      <span class="progress-label">Progress</span>
      <span v-if="difficulty" class="difficulty-badge" :class="difficulty.toLowerCase()">{{ difficultyStars }} {{ difficulty }}</span>
      <span class="progress-percentage">{{ percentage }}%</span>
    </div>
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: percentage + '%' }"></div>
    </div>
    <div class="progress-stats">
      <div class="stat">
        <span class="stat-icon">📝</span>
        <span class="stat-value">{{ filledCells }}/81</span>
        <span class="stat-label">Filled</span>
      </div>
      <div class="stat">
        <span class="stat-icon">⏱️</span>
        <span class="stat-value" :class="{ paused: timerPaused }">{{ formattedTime }}</span>
        <button v-if="elapsedTime > 0" class="pause-btn" @click="$emit('toggle-pause')">
          {{ timerPaused ? '▶' : '⏸' }}
        </button>
        <span class="stat-label">Time</span>
        <span v-if="newRecord" class="new-record-badge">🏆 New Record!</span>
      </div>
      <div v-if="mistakes > 0" class="stat">
        <span class="stat-icon">❌</span>
        <span class="stat-value">{{ mistakes }}</span>
        <span class="stat-label">Mistakes</span>
      </div>
      <div v-if="hintsUsed > 0" class="stat">
        <span class="stat-icon">💡</span>
        <span class="stat-value">{{ hintsUsed }}</span>
        <span class="stat-label">Hints</span>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ProgressIndicator',
  props: {
    puzzle: {
      type: String,
      required: true
    },
    givenCells: {
      type: Set,
      required: true
    },
    mistakes: {
      type: Number,
      default: 0
    },
    hintsUsed: {
      type: Number,
      default: 0
    },
    elapsedTime: {
      type: Number,
      default: 0
    },
    timerPaused: {
      type: Boolean,
      default: false
    },
    difficulty: {
      type: String,
      default: ''
    },
    newRecord: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle-pause'],
  setup(props) {
    const filledCells = computed(() => {
      let count = 0
      for (let i = 0; i < props.puzzle.length; i++) {
        if (props.puzzle[i] !== '.') {
          count++
        }
      }
      return count
    })

    const percentage = computed(() => {
      return Math.round((filledCells.value / 81) * 100)
    })

    const formattedTime = computed(() => {
      const seconds = Math.floor(props.elapsedTime / 1000)
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60

      if (minutes > 0) {
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
      }
      return `${seconds}s`
    })

    const difficultyStars = computed(() => {
      const map = { EASY: '⭐', MEDIUM: '⭐⭐', HARD: '⭐⭐⭐', EXPERT: '⭐⭐⭐⭐', MASTER: '⭐⭐⭐⭐⭐' }
      return map[props.difficulty.toUpperCase()] || '⭐'
    })

    return {
      filledCells,
      percentage,
      formattedTime,
      difficultyStars
    }
  }
}
</script>

<style scoped>
.progress {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.paused {
  color: #ea4335 !important;
  text-decoration: line-through;
}

.pause-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 2px 6px;
}

.new-record-badge {
  display: inline-block;
  background: linear-gradient(135deg, #fbbc04, #ea4335);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 8px;
  animation: record-pulse 1s ease-in-out infinite;
  margin-top: 2px;
}

@keyframes record-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
.pause-btn {
  background: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 1px 6px;
  font-size: 12px;
  cursor: pointer;
  margin: 2px 0;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

.difficulty-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  text-transform: capitalize;
}
.difficulty-badge.easy { background: #e8f5e9; color: #2e7d32; }
.difficulty-badge.medium { background: #fff3e0; color: #e65100; }
.difficulty-badge.hard { background: #fce4ec; color: #c62828; }
.difficulty-badge.expert { background: #f3e5f5; color: #6a1b9a; }
.difficulty-badge.master { background: #e0e0e0; color: #333; }

.progress-percentage {
  font-size: 18px;
  font-weight: 700;
  color: #4285f4;
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4285f4, #34a853);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 12px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 20px;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 11px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Mobile */
@media (max-width: 500px) {
  .progress {
    padding: 12px;
  }

  .progress-stats {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .stat-icon {
    font-size: 16px;
  }

  .stat-value {
    font-size: 14px;
  }

  .stat-label {
    font-size: 9px;
  }
}

/* Dark mode */
@media (prefers-color-scheme: dark) {
  .progress {
    background: #2d2d2d;
  }

  .progress-bar {
    background: #404040;
  }

  .stat-value {
    color: #e0e0e0;
  }

  .progress-label {
    color: #aaa;
  }
}
</style>
