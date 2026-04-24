<template>
  <div class="control-panel">
    <!-- Undo/Redo row -->
    <div class="undo-redo-row">
      <button
        class="btn-undo"
        :disabled="!canUndo"
        @click="$emit('undo')"
      >
        ↶ Undo
        <span v-if="undoCount > 0" class="count">({{ undoCount }})</span>
      </button>
      <button
        class="btn-redo"
        :disabled="!canRedo"
        @click="$emit('redo')"
      >
        Redo ↷
        <span v-if="redoCount > 0" class="count">({{ redoCount }})</span>
      </button>
    </div>

    <!-- Main action buttons -->
    <div class="main-buttons">
      <button class="btn-primary" :disabled="loading" @click="$emit('solve')">
        <span class="btn-icon">🧩</span>
        Solve
      </button>
      <button class="btn-secondary" :disabled="loading" @click="$emit('clear')">
        <span class="btn-icon">🗑️</span>
        Clear
      </button>
      <button class="btn-secondary" @click="$emit('import')">
        <span class="btn-icon">📥</span>
        Import
      </button>
    </div>

    <!-- Difficulty buttons -->
    <div class="difficulty-section">
      <p class="section-label">New Puzzle:</p>
      <div class="difficulty-buttons">
        <button
          class="btn-difficulty easy"
          :disabled="loading"
          @click="$emit('generate', 'EASY')"
        >
          ⭐ Easy
        </button>
        <button
          class="btn-difficulty medium"
          :disabled="loading"
          @click="$emit('generate', 'MEDIUM')"
        >
          ⭐⭐ Medium
        </button>
        <button
          class="btn-difficulty hard"
          :disabled="loading"
          @click="$emit('generate', 'HARD')"
        >
          ⭐⭐⭐ Hard
        </button>
        <button
          class="btn-difficulty expert"
          :disabled="loading"
          @click="$emit('generate', 'EXPERT')"
        >
          ⭐⭐⭐⭐ Expert
        </button>
      </div>
    </div>

    <!-- Hint button -->
    <button class="btn-hint" :disabled="loading" @click="$emit('hint')">
      <span class="btn-icon">💡</span>
      Get a Hint!
    </button>

    <button class="btn-share" @click="$emit('share')">
      <span class="btn-icon">🔗</span>
      Share Puzzle
    </button>

    <button class="btn-print" @click="$emit('print')">
      <span class="btn-icon">🖨️</span>
      Print
    </button>
    <button class="btn-image" @click="$emit('share-image')">
      <span class="btn-icon">📸</span>
      Image
    </button>

    <!-- Pencil marks toggle -->
    <button
      class="btn-toggle"
      :class="{ active: showCandidates }"
      @click="$emit('toggle-candidates')"
    >
      <span class="btn-icon">{{ showCandidates ? '✏️' : '✏️' }}</span>
      {{ showCandidates ? 'Hide Pencil Marks' : 'Show Pencil Marks' }}
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
    loading: {
      type: Boolean,
      default: false
    },
    canUndo: {
      type: Boolean,
      default: false
    },
    canRedo: {
      type: Boolean,
      default: false
    },
    undoCount: {
      type: Number,
      default: 0
    },
    redoCount: {
      type: Number,
      default: 0
    },
    showCandidates: {
      type: Boolean,
      default: true
    }
  })

const emit = defineEmits(['solve', 'clear', 'generate', 'hint', 'undo', 'redo', 'toggle-candidates', 'import', 'share', 'print', 'share-image'])
</script>

<style scoped>
.control-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.undo-redo-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.btn-undo,
.btn-redo {
  padding: 10px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.btn-undo:not(:disabled):hover,
.btn-redo:not(:disabled):hover {
  border-color: #4285f4;
  color: #4285f4;
}

.btn-undo:disabled,
.btn-redo:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.count {
  font-size: 11px;
  opacity: 0.6;
}

.main-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.difficulty-section {
  margin-top: 2px;
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 6px 0;
}

.difficulty-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

/* Unified button base */
button {
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #555;
  font-size: clamp(13px, 3vw, 15px);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

button:active:not(:disabled) {
  transform: scale(0.97);
}

@media (hover: hover) {
  button:hover:not(:disabled) {
    border-color: #4285f4;
    color: #4285f4;
  }
}

.btn-icon {
  font-size: 16px;
}

/* Primary action — Solve */
.btn-primary {
  background: #4285f4;
  color: white;
  border-color: #4285f4;
}

/* Secondary action — Clear */
.btn-secondary {
  background: #f5f5f5;
  color: #555;
  border-color: #ddd;
}

/* Difficulty buttons — subtle left border accent, same base style */
.btn-difficulty {
  font-size: clamp(12px, 2.8vw, 14px);
  padding: 8px 12px;
  border-left: 3px solid;
}

.btn-difficulty.easy {
  border-left-color: #4caf50;
}

.btn-difficulty.medium {
  border-left-color: #ff9800;
}

.btn-difficulty.hard {
  border-left-color: #f44336;
}

.btn-difficulty.expert {
  border-left-color: #9c27b0;
}

/* Hint — primary blue to match Solve */
.btn-hint {
  background: #4285f4;
  color: white;
  border-color: #4285f4;
  padding: 12px;
  font-size: 15px;
}

/* Utility buttons — all same neutral style */
.btn-share,
.btn-print,
.btn-image {
  background: white;
  color: #555;
  border-color: #ddd;
  padding: 10px 14px;
}

/* Toggle — outline style */
.btn-toggle {
  background: white;
  color: #555;
  padding: 10px 14px;
  font-size: clamp(12px, 2.8vw, 14px);
  border: 1px solid #ddd;
}

.btn-toggle.active {
  border-color: #4285f4;
  color: #4285f4;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .btn-undo,
  .btn-redo,
  button {
    background: #2a2a2a;
    border-color: #444;
    color: #ccc;
  }
  .btn-primary,
  .btn-hint {
    background: #4285f4;
    color: white;
    border-color: #4285f4;
  }
  .btn-secondary {
    background: #333;
    color: #ccc;
  }
  .btn-toggle.active {
    background: #1a2a3a;
  }
}

/* Mobile responsive */
@media (max-width: 500px) {
  .control-panel {
    gap: 8px;
  }

  button {
    padding: 8px 10px;
    min-height: 42px;
  }

  .difficulty-buttons {
    gap: 6px;
  }

  .btn-difficulty {
    padding: 6px 10px;
    font-size: 12px;
  }
}

/* iPhone SE */
@media (max-width: 380px) {
  .undo-redo-row {
    gap: 6px;
  }

  button {
    padding: 6px 8px;
    font-size: 12px;
  }
}

/* Extra small screens */
@media (max-width: 320px) {
  .difficulty-buttons {
    grid-template-columns: 1fr;
  }

  .btn-difficulty {
    padding: 8px;
  }
}
</style>
