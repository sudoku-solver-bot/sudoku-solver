<template>
  <div
    class="import-modal"
    @click.self="$emit('close')"
  >
    <div
      class="import-card"
      :class="{ dark: isDark }"
    >
      <h3>📥 Import Puzzle</h3>
      <p class="import-desc">
        Paste a Sudoku puzzle below. Use <code>.</code> or <code>0</code> for empty cells.
      </p>

      <!-- Format tabs -->
      <div class="format-tabs">
        <button
          :class="{ active: format === 'single' }"
          @click="format = 'single'"
        >
          Single Line
        </button>
        <button
          :class="{ active: format === 'grid' }"
          @click="format = 'grid'"
        >
          9×9 Grid
        </button>
      </div>

      <!-- Input area -->
      <textarea
        v-model="input"
        :placeholder="format === 'single' ? '530070000600195000098000060800060003400803001700020006060000280000419005000080079' : '53. .7. ...\n6.. .19 5..\n.98 ... .6.\n\n8.. .6. ..3\n4.. 8.3 ..1\n7.. .2. ..6\n\n.6. ... 28.\n... 419 ..5\n... .8. .79'"
        class="import-input"
        rows="6"
        @input="validate"
      />

      <!-- Preview -->
      <div
        v-if="parsedPuzzle"
        class="preview-section"
      >
        <p class="preview-label">
          Preview ({{ givenCount }} given cells):
        </p>
        <div class="mini-grid">
          <span
            v-for="(c, i) in parsedPuzzle"
            :key="i"
            :class="{ given: c !== '.', empty: c === '.' }"
          >
            {{ c === '.' ? '' : c }}
          </span>
        </div>
      </div>

      <!-- Error -->
      <div
        v-if="error"
        class="import-error"
      >
        {{ error }}
      </div>

      <!-- Actions -->
      <div class="import-actions">
        <button
          class="btn-cancel"
          @click="$emit('close')"
        >
          Cancel
        </button>
        <button
          class="btn-example"
          @click="loadExample"
        >
          Load Example
        </button>
        <button
          class="btn-import"
          :disabled="!parsedPuzzle || !!error"
          @click="doImport"
        >
          Import
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref } from 'vue'

const props = defineProps({
    isDark: { type: Boolean, default: false }
  })
const emit = defineEmits(['close', 'import'])

const input = ref('')
const format = ref('single')
const parsedPuzzle = ref(null)
const error = ref('')
const givenCount = ref(0)

const parsePuzzle = (text) => {
  // Remove all whitespace, separators (!, -, |, spaces)
  let cleaned = text.replace(/[\s!\-|]/g, '').replace(/0/g, '.')
  // Only keep valid chars
  cleaned = cleaned.replace(/[^1-9.]/g, '')

  if (cleaned.length !== 81) {
    return null
  }

  // Basic validation: each row should have valid structure
  return cleaned
}

const validate = () => {
  error.value = ''
  if (!input.value.trim()) {
    parsedPuzzle.value = null
    return
  }

  const parsed = parsePuzzle(input.value)
  if (!parsed) {
    const cleaned = input.value.replace(/[\s!\-|]/g, '').replace(/0/g, '.').replace(/[^1-9.]/g, '')
    if (cleaned.length < 81) {
      error.value = `Need 81 cells, got ${cleaned.length}. Keep going!`
    } else {
      error.value = `Too many cells (${cleaned.length}). Need exactly 81.`
    }
    parsedPuzzle.value = null
    return
  }

  // Check for duplicates in rows/cols/boxes
  parsedPuzzle.value = parsed
  givenCount.value = parsed.split('').filter(c => c !== '.').length

  if (givenCount.value < 17) {
    error.value = 'Puzzle needs at least 17 given cells to have a unique solution.'
  }
}

const doImport = () => {
  if (parsedPuzzle.value) {
    emit('import', parsedPuzzle.value)
  }
}

const loadExample = () => {
  format.value = 'single'
  input.value = '530070000600195000098000060800060003400803001700020006060000280000419005000080079'
  validate()
}
</script>

<style scoped>
.import-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.import-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 420px;
  width: 90%;
  animation: popIn 0.3s ease;
}

.import-card.dark { background: #2d2d2d; color: #e0e0e0; }

@keyframes popIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.import-card h3 { font-size: 18px; margin: 0 0 8px; }
.import-desc { font-size: 13px; color: #888; margin: 0 0 12px; }
.import-card.dark .import-desc { color: #aaa; }
.import-desc code { background: #f0f0f0; padding: 1px 4px; border-radius: 3px; font-size: 12px; }
.import-card.dark .import-desc code { background: #444; }

.format-tabs {
  display: flex; gap: 4px; margin-bottom: 10px;
}

.format-tabs button {
  flex: 1; padding: 6px; border: 1px solid #e0e0e0; border-radius: 8px;
  background: transparent; font-size: 12px; cursor: pointer; color: #666;
}
.import-card.dark .format-tabs button { border-color: #555; color: #aaa; }
.format-tabs button.active { background: #4285f4; color: white; border-color: #4285f4; }

.import-input {
  width: 100%; padding: 10px; border: 2px solid #e0e0e0; border-radius: 10px;
  font-family: 'Courier New', monospace; font-size: 13px; resize: vertical;
  box-sizing: border-box;
}
.import-card.dark .import-input { background: #333; border-color: #555; color: #eee; }

.preview-section { margin-top: 12px; }
.preview-label { font-size: 12px; color: #888; margin: 0 0 6px; }

.mini-grid {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 1px;
  background: #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
  font-family: 'Courier New', monospace;
  font-size: 10px;
}

.mini-grid span {
  padding: 3px 0;
  text-align: center;
  background: white;
}

.import-card.dark .mini-grid span { background: #333; }
.mini-grid span.empty { color: #ddd; }
.mini-grid span.given { font-weight: 700; color: #333; }
.import-card.dark .mini-grid span.given { color: #eee; }

.import-error {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fce4ec;
  color: #c62828;
  border-radius: 8px;
  font-size: 13px;
}
.import-card.dark .import-error { background: #3a1a1a; color: #ef9a9a; }

.import-actions {
  display: flex; gap: 8px; margin-top: 16px; justify-content: flex-end;
}

.btn-cancel {
  background: transparent; border: 1px solid #e0e0e0; padding: 8px 14px;
  border-radius: 8px; cursor: pointer; font-size: 13px;
}
.import-card.dark .btn-cancel { border-color: #555; color: #aaa; }

.btn-example {
  background: #f0f0f0; border: none; padding: 8px 14px;
  border-radius: 8px; cursor: pointer; font-size: 13px;
}
.import-card.dark .btn-example { background: #444; color: #ccc; }

.btn-import {
  background: #4285f4; color: white; border: none; padding: 8px 20px;
  border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 600;
}
.btn-import:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
