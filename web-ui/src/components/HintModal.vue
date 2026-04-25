<template>
  <transition name="modal">
    <div
      v-if="visible"
      class="modal-overlay"
      @click="$emit('close')"
    >
      <div
        class="modal-content"
        @click.stop
      >
        <button
          class="modal-close"
          @click="$emit('close')"
        >
          &times;
        </button>

        <div class="hint-header">
          <span class="hint-icon">💡</span>
          <h2>Here's a Hint!</h2>
        </div>

        <div
          v-if="hint"
          class="hint-body"
        >
          <div class="hint-cell-highlight">
            <div class="hint-position">
              Look at cell
              <span class="coord">Row {{ hint.row + 1 }}</span>
              <span class="coord">Column {{ hint.col + 1 }}</span>
            </div>
            <div class="hint-value">
              The answer is: <span class="value">{{ hint.value }}</span>
            </div>
          </div>

          <div class="hint-technique">
            <span class="technique-label">Technique:</span>
            <span class="technique-name">{{ formatTechnique(hint.technique) }}</span>
          </div>

          <div class="hint-explanation">
            <button
              class="explanation-toggle"
              @click="showExplanation = !showExplanation"
            >
              {{ showExplanation ? '▼' : '▶' }} Why this hint?
            </button>
            <transition name="expand">
              <div
                v-if="showExplanation"
                class="explanation-content"
              >
                <p>{{ getTechniqueExplanation(hint.technique) }}</p>
                <p class="kid-friendly">
                  {{ getKidFriendlyExplanation(hint.technique) }}
                </p>
              </div>
            </transition>
          </div>

          <div class="hint-stats">
            <span>Hints used this puzzle: {{ totalHints }}</span>
          </div>
        </div>

        <div
          v-else
          class="hint-error"
        >
          <p>No hint available right now.</p>
          <p class="error-detail">
            Try solving some more cells first!
          </p>
        </div>

        <button
          class="hint-button"
          @click="$emit('close')"
        >
          Got it, thanks!
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>

import { ref } from 'vue'

const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    hint: {
      type: Object,
      default: null
    },
    totalHints: {
      type: Number,
      default: 0
    }
  })
const emit = defineEmits(['close'])

const showExplanation = ref(false)

const formatTechnique = (technique) => {
  return technique
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim()
}

const getTechniqueExplanation = (technique) => {
  const explanations = {
    'SINGLE_CANDIDATE': 'This cell has only one possible value that doesn\'t conflict with its row, column, or 3x3 box.',
    'HIDDEN_SINGLE': 'While this cell may have multiple candidates, one value can only go in this specific cell within its row, column, or box.',
    'NAKED_PAIR': 'Two cells in the same group have only the same two candidates. These values can be removed from other cells in the group.',
    'NAKED_TRIPLE': 'Three cells in the same group share only three candidates between them. These values can be removed from other cells.',
    'POINTING_PAIR': 'A candidate in a 3x3 box is restricted to a single row or column, allowing us to eliminate it from the rest of that row/column outside the box.',
    'BOX_LINE_REDUCTION': 'When a candidate in a row or column is restricted to one 3x3 box, it can be eliminated from other cells in that box.'
  }
  return explanations[technique] || 'This is a valid logical deduction for solving the puzzle.'
}

const getKidFriendlyExplanation = (technique) => {
  const kidFriendly = {
    'SINGLE_CANDIDATE': '🎯 Think of it like a game of "what\'s left"? Only one number fits here!',
    'HIDDEN_SINGLE': '🔍 This number has nowhere else to go in this row, column, or box. It\'s like musical chairs!',
    'NAKED_PAIR': '👯 Two best friends that always hang out together - they only go in these two spots!',
    'NAKED_TRIPLE': '👯👯 Three numbers that are best buddies and only hang out in these three spots!',
    'POINTING_PAIR': '👆 These two spots point the way - we know where a number can\'t go!',
    'BOX_LINE_REDUCTION': '📦 In this square box, a number is stuck in one line - so it can\'t be anywhere else in the box!'
  }
  return kidFriendly[technique] || '🧩 Keep practicing and you\'ll spot these patterns!'
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 20px;
  padding: 24px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #f5f5f5;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background: #e0e0e0;
  color: #333;
}

.hint-header {
  text-align: center;
  margin-bottom: 24px;
}

.hint-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.hint-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.hint-body {
  margin-bottom: 20px;
}

.hint-cell-highlight {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  text-align: center;
}

.hint-position {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.coord {
  display: inline-block;
  background: white;
  padding: 4px 8px;
  border-radius: 6px;
  margin: 0 4px;
  font-weight: 600;
  color: #4285f4;
}

.hint-value {
  font-size: 16px;
  color: #333;
}

.value {
  display: inline-block;
  background: #4285f4;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 28px;
  font-weight: 700;
  margin-left: 8px;
}

.hint-technique {
  background: #fff8e1;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.technique-label {
  font-size: 13px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.technique-name {
  font-size: 14px;
  font-weight: 600;
  color: #f57f17;
}

.hint-explanation {
  margin-bottom: 12px;
}

.explanation-toggle {
  width: 100%;
  padding: 12px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s;
}

.explanation-toggle:hover {
  background: #e8f0fe;
}

.explanation-content {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.explanation-content p {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin-bottom: 8px;
}

.explanation-content p:last-child {
  margin-bottom: 0;
}

.kid-friendly {
  font-weight: 600;
  color: #4285f4;
  background: #e8f0fe;
  padding: 8px;
  border-radius: 6px;
}

.hint-stats {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
}

.hint-error {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error-detail {
  font-size: 14px;
  color: #888;
  margin-top: 8px;
}

.hint-button {
  width: 100%;
  padding: 14px;
  background: #4285f4;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.hint-button:hover {
  background: #3367d6;
}

/* Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

/* Mobile */
@media (max-width: 500px) {
  .modal-content {
    padding: 20px;
    border-radius: 16px 16px 0 0;
    position: fixed;
    bottom: 0;
    top: auto;
    max-height: 80vh;
    border-radius: 20px 20px 0 0;
  }

  .modal-close {
    top: 12px;
    right: 12px;
  }

  .hint-icon {
    font-size: 40px;
  }

  .hint-header h2 {
    font-size: 20px;
  }

  .value {
    font-size: 24px;
    padding: 6px 12px;
  }
}
</style>
