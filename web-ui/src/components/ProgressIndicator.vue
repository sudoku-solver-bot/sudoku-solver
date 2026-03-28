<template>
  <div class="progress">
    <div class="progress-header">
      <span class="progress-label">Progress</span>
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
        <span class="stat-value">{{ formattedTime }}</span>
        <span class="stat-label">Time</span>
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
    }
  },
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

    return {
      filledCells,
      percentage,
      formattedTime
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
