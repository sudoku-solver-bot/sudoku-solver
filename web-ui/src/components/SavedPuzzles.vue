<template>
  <div
    class="saves-modal"
    @click.self="$emit('close')"
  >
    <div
      class="saves-card"
      :class="{ dark: isDark }"
    >
      <div class="saves-header">
        <h3>💾 Saved Puzzles</h3>
        <button
          class="close-btn"
          @click="$emit('close')"
        >
          ✕
        </button>
      </div>

      <div
        v-if="saves.length === 0"
        class="empty-state"
      >
        <p>No saved puzzles yet!</p>
        <p class="hint">
          Click "Save Puzzle" while playing to save your progress.
        </p>
      </div>

      <div
        v-else
        class="saves-list"
      >
        <div
          v-for="(save, i) in saves"
          :key="i"
          class="save-item"
          :class="{ dark: isDark }"
        >
          <div class="save-info">
            <div class="save-name">
              {{ save.name || `Slot ${i + 1}` }}
            </div>
            <div class="save-meta">
              <span v-if="save.difficulty">{{ save.difficulty }}</span>
              <span>{{ save.progress }}/81 filled</span>
              <span>{{ save.date }}</span>
            </div>
          </div>
          <div class="save-actions">
            <button
              class="btn-load"
              @click="$emit('load', save)"
            >
              ▶ Load
            </button>
            <button
              class="btn-delete"
              @click="deleteSave(i)"
            >
              🗑️
            </button>
          </div>
        </div>
      </div>

      <div class="saves-footer">
        <button
          class="btn-save-current"
          @click="saveCurrent"
        >
          💾 Save Current Puzzle
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, onMounted } from 'vue'

const SAVES_KEY = 'sudoku-dojo-saves'

interface SaveData {
    puzzle: string; difficulty: string; progress: number; date: string; name: string
  }

  interface Props {
    isDark?: boolean
    currentPuzzle?: string
    currentDifficulty?: string
  }
  const props = withDefaults(defineProps<Props>(), {
    isDark: false,
    currentPuzzle: '',
    currentDifficulty: ''
  })
const emit = defineEmits<{ close: []; load: [save: SaveData] }>()

const saves = ref<SaveData[]>([])

const loadSaves = (): void => {
  try {
    saves.value = JSON.parse(localStorage.getItem(SAVES_KEY) || '[]')
  } catch { saves.value = [] }
}

onMounted(loadSaves)

const saveCurrent = (): void => {
  if (!props.currentPuzzle || props.currentPuzzle === '.'.repeat(81)) return
  const filled = props.currentPuzzle.split('').filter(c => c !== '.').length
  const save = {
    puzzle: props.currentPuzzle,
    difficulty: props.currentDifficulty,
    progress: filled,
    date: new Date().toLocaleDateString(),
    name: `Puzzle ${saves.value.length + 1}`
  }
  saves.value.unshift(save)
  if (saves.value.length > 10) saves.value = saves.value.slice(0, 10) // max 10
  localStorage.setItem(SAVES_KEY, JSON.stringify(saves.value))
}

const deleteSave = (index: number): void => {
  saves.value.splice(index, 1)
  localStorage.setItem(SAVES_KEY, JSON.stringify(saves.value))
}
</script>

<style scoped>
.saves-modal {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.saves-card {
  background: white; border-radius: 16px; padding: 20px;
  max-width: 400px; width: 90%; max-height: 80vh; overflow-y: auto;
}
.saves-card.dark { background: #2d2d2d; color: #e0e0e0; }
.saves-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.saves-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #999; }
.empty-state { text-align: center; color: #999; padding: 24px 0; }
.hint { font-size: 13px; }
.save-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px; border: 1px solid #eee; border-radius: 10px; margin-bottom: 8px;
}
.save-item.dark { border-color: #444; }
.save-name { font-weight: 600; font-size: 14px; }
.save-meta { display: flex; gap: 8px; font-size: 11px; color: #999; margin-top: 4px; }
.save-actions { display: flex; gap: 6px; }
.btn-load { background: #4285f4; color: white; border: none; padding: 6px 12px; border-radius: 6px; font-size: 12px; cursor: pointer; }
.btn-delete { background: transparent; border: 1px solid #eee; padding: 6px 8px; border-radius: 6px; cursor: pointer; }
.save-item.dark .btn-delete { border-color: #555; }
.saves-footer { margin-top: 16px; text-align: center; }
.btn-save-current {
  background: #34a853; color: white; border: none; padding: 10px 20px;
  border-radius: 10px; font-size: 14px; cursor: pointer; width: 100%;
}
</style>
