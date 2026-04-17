<template>
  <div class="grid" :class="{ dark: isDark, colorblind: colorBlind, 'high-contrast': highContrast, ['theme-' + theme]: theme !== 'default' }" role="grid" aria-label="Sudoku puzzle grid">
    <div
      v-for="(cell, index) in 81"
      :key="index"
      class="cell"
      :class="getCellClasses(index)"
      @click="selectCell(index)"
      role="gridcell"
      :aria-label="getCellLabel(index)"
      :aria-selected="selectedCell === index"
    >
      <input
        v-if="puzzle[index] !== '.' || !showCandidates || givenCells.has(index)"
        ref="inputs"
        type="text"
        inputmode="numeric"
        pattern="[1-9]"
        maxlength="1"
        :value="puzzle[index] === '.' ? '' : puzzle[index]"
        :readonly="givenCells.has(index)"
        :data-index="index"
        @input="onInput(index, $event)"
        @focus="selectCell(index)"
        @keydown="onKeyDown($event, index)"
      />
      <div
        v-else
        ref="candidateGrids"
        class="candidates-grid"
        :data-index="index"
        tabindex="0"
        @focus="selectCell(index)"
        @keydown="onKeyDown($event, index)"
      >
        <span
          v-for="n in 9"
          :key="n"
          class="candidate"
          :class="{ visible: cellCandidates(index, n) }"
        >{{ cellCandidates(index, n) ? n : '' }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, nextTick } from 'vue'

export default {
  name: 'SudokuGrid',
  props: {
    puzzle: {
      type: String,
      required: true
    },
    givenCells: {
      type: Set,
      required: true
    },
    solvedCells: {
      type: Set,
      required: true
    },
    selectedCell: {
      type: Number,
      default: -1
    },
    isDark: {
      type: Boolean,
      default: false
    },
    candidates: {
      type: Object,
      default: () => ({})
    },
    showCandidates: {
      type: Boolean,
      default: true
    },
    highlightedCells: {
      type: Array,
      default: () => []
    },
    showConflicts: {
      type: Boolean,
      default: true
    },
    colorBlind: {
      type: Boolean,
      default: false
    },
    highContrast: {
      type: Boolean,
      default: false
    },
    theme: {
      type: String,
      default: 'default' // default, wood, neon, minimal
    }
  },
  emits: ['update', 'select', 'navigate', 'undo', 'redo'],
  setup(props, { emit }) {
    const inputs = ref([])
    const candidateGrids = ref([])

    const cellCandidates = (index, n) => {
      const key = String(index)
      return props.candidates[key]?.includes(n) ?? false
    }

    const getRow = (index) => Math.floor(index / 9)
    const getCol = (index) => index % 9
    const getRegion = (index) => {
      const row = getRow(index)
      const col = getCol(index)
      return Math.floor(row / 3) * 3 + Math.floor(col / 3)
    }

    // Detect conflicts (duplicate values in row/col/box)
    const conflicts = computed(() => {
      if (!props.showConflicts) return new Set()
      const set = new Set()
      for (let i = 0; i < 81; i++) {
        if (props.puzzle[i] === '.') continue
        const val = props.puzzle[i]
        const row = getRow(i), col = getCol(i), region = getRegion(i)
        for (let j = 0; j < 81; j++) {
          if (i === j || props.puzzle[j] !== val) continue
          if (getRow(j) === row || getCol(j) === col || getRegion(j) === region) {
            set.add(i)
            set.add(j)
          }
        }
      }
      return set
    })

    const getCellClasses = (index) => {
      const classes = {
        given: props.givenCells.has(index),
        solved: props.solvedCells.has(index),
        selected: props.selectedCell === index,
        'border-right': (index + 1) % 3 === 0 && (index + 1) % 9 !== 0,
        'border-bottom': isBottomBorder(index)
      }

      // Highlight related cells when one is selected
      if (props.selectedCell >= 0) {
        const selectedRow = getRow(props.selectedCell)
        const selectedCol = getCol(props.selectedCell)
        const selectedRegion = getRegion(props.selectedCell)

        classes['related-row'] = getRow(index) === selectedRow && index !== props.selectedCell
        classes['related-col'] = getCol(index) === selectedCol && index !== props.selectedCell
        classes['related-region'] = getRegion(index) === selectedRegion && index !== props.selectedCell
        classes['same-value'] =
          props.puzzle[index] !== '.' &&
          props.puzzle[index] === props.puzzle[props.selectedCell] &&
          index !== props.selectedCell
      }

      // Tutorial highlighting (always active)
      classes['highlight-blue'] = isHighlighted(index, 'blue')
      classes['highlight-green'] = isHighlighted(index, 'green')
      classes['highlight-red'] = isHighlighted(index, 'red')
      classes['highlight-yellow'] = isHighlighted(index, 'yellow')

      // Conflict highlighting
      classes['conflict'] = conflicts.value.has(index)

      return classes
    }

    const isHighlighted = (index, color) => {
      return props.highlightedCells.some(h =>
        h.cells.includes(index) && h.color === color
      )
    }

    const isBottomBorder = (index) => {
      const row = Math.floor(index / 9)
      return row === 2 || row === 5
    }

    const getCellLabel = (index) => {
      const row = Math.floor(index / 9) + 1
      const col = (index % 9) + 1
      const value = props.puzzle[index]
      if (value !== '.') {
        return `Row ${row}, Column ${col}: ${value}`
      }
      const key = String(index)
      const cands = props.candidates[key] || []
      return `Row ${row}, Column ${col}: empty, candidates ${cands.join(', ') || 'none'}`
    }

    const selectCell = (index) => {
      emit('select', index)
      focusCell(index)
    }

    const focusCell = async (index) => {
      await nextTick()
      const input = inputs.value[index]
      if (input) {
        input.focus()
      } else if (candidateGrids.value) {
        // Find the candidate grid element with matching data-index
        const grids = Array.isArray(candidateGrids.value) ? candidateGrids.value : [candidateGrids.value]
        const grid = grids.find(el => el?.dataset?.index === String(index))
        if (grid) {
          grid.focus()
        }
      }
    }

    const onInput = (index, event) => {
      const value = event.target.value
      if (/^[1-9]$/.test(value) || value === '') {
        emit('update', index, value)
      } else {
        event.target.value = ''
        emit('update', index, '')
      }
    }

    const onKeyDown = (event, index) => {
      const row = getRow(index)
      const col = getCol(index)

      // Number keys
      if (/^[1-9]$/.test(event.key)) {
        event.preventDefault()
        if (!props.givenCells.has(index)) {
          emit('update', index, event.key)
        }
        return
      }

      // Navigation
      switch (event.key) {
        case 'ArrowUp':
          event.preventDefault()
          if (row > 0) emit('navigate', index - 9)
          break
        case 'ArrowDown':
          event.preventDefault()
          if (row < 8) emit('navigate', index + 9)
          break
        case 'ArrowLeft':
          event.preventDefault()
          if (col > 0) emit('navigate', index - 1)
          break
        case 'ArrowRight':
          event.preventDefault()
          if (col < 8) emit('navigate', index + 1)
          break
        case 'Tab':
          event.preventDefault()
          if (event.shiftKey) {
            // Previous cell
            if (index > 0) emit('navigate', index - 1)
          } else {
            // Next cell
            if (index < 80) emit('navigate', index + 1)
          }
          break
        case 'Backspace':
        case 'Delete':
          event.preventDefault()
          if (!props.givenCells.has(index)) {
            emit('update', index, '')
          }
          break
        case 'Enter':
          event.preventDefault()
          // Move to next cell or stay if at end
          if (index < 80) {
            emit('navigate', index + 1)
          }
          break
        case 'Escape':
          event.preventDefault()
          emit('select', -1)
          break
        case 'z':
        case 'Z':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault()
            if (event.shiftKey) {
              emit('redo')
            } else {
              emit('undo')
            }
          }
          break
        case 'y':
        case 'Y':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault()
            emit('redo')
          }
          break
      }
    }

    return {
      inputs,
      candidateGrids,
      cellCandidates,
      getCellClasses,
      isBottomBorder,
      selectCell,
      focusCell,
      onInput,
      onKeyDown
    }
  }
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 1px;
  background: #333;
  padding: 2px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.grid.dark {
  background: #555;
}

.cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  font-size: clamp(14px, 4vw, 20px);
  font-weight: 600;
  transition: background-color 0.15s ease;
  position: relative;
  overflow: hidden;
}

.cell:focus-within {
  outline: 2px solid #4285f4;
  outline-offset: -2px;
  z-index: 1;
}

.grid.dark .cell {
  background: #2d2d2d;
}

.candidates-grid {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  outline: none;
}

.candidate {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(7px, 1.8vw, 11px);
  font-weight: 400;
  color: transparent;
  user-select: none;
  line-height: 1;
}

.candidate.visible {
  color: #999;
}

.grid.dark .candidate.visible {
  color: #888;
}

.cell input {
  width: 100%;
  height: 100%;
  border: none;
  text-align: center;
  font-size: clamp(14px, 4vw, 20px);
  font-weight: 600;
  background: transparent;
  outline: none;
  caret-color: #4285f4;
  color: inherit;
}

.grid.dark .cell input {
  caret-color: #81c995;
}

.cell input:focus {
  outline: none;
}

/* Selected cell */
.cell.selected {
  background: #4285f4 !important;
}

.cell.selected input {
  color: white;
}

.grid.dark .cell.selected {
  background: #81c995 !important;
}

/* Related cells highlighting */
.cell.related-row,
.cell.related-col,
.cell.related-region {
  background: #e8f0fe;
}

.grid.dark .cell.related-row,
.grid.dark .cell.related-col,
.grid.dark .cell.related-region {
  background: #3d3d3d;
}

.cell.same-value {
  background: #fff8e1;
}

.cell.conflict {
  background: #ffebee !important;
  color: #c62828;
  animation: shake 0.3s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
}

.grid.dark .cell.same-value {
  background: #4d4d3d;
}

.grid.dark .cell.conflict {
  background: #4d1a1a !important;
  color: #ef9a9a;
}

/* Given and solved cells */
.cell.given input {
  color: #333;
  background: #f0f0f0;
}

.cell.given.selected input {
  color: white;
  background: transparent;
}

.grid.dark .cell.given input {
  color: #e0e0e0;
  background: #3d3d3d;
}

.cell.solved input {
  color: #4285f4;
}

.cell.solved.selected input {
  color: white;
}

.grid.dark .cell.solved input {
  color: #81c995;
}

/* Tutorial highlighting */
.cell.highlight-blue {
  background: #e3f2fd !important;
  box-shadow: inset 0 0 0 2px #4285f4;
  animation: pulse 1.5s ease infinite;
}

.cell.highlight-green {
  background: #e8f5e9 !important;
  box-shadow: inset 0 0 0 2px #34a853;
  animation: pulse 1.5s ease infinite;
}

.cell.highlight-red {
  background: #fce4ec !important;
  box-shadow: inset 0 0 0 2px #ea4335;
  animation: pulse 1.5s ease infinite;
}

.cell.highlight-yellow {
  background: #fff8e1 !important;
  box-shadow: inset 0 0 0 2px #fbbc05;
  animation: pulse 1.5s ease infinite;
}

/* Color-blind friendly: use patterns + shapes instead of just color */
.grid.colorblind .cell.highlight-blue {
  background: #e3f2fd !important;
  box-shadow: inset 0 0 0 3px #1565c0;
}

.grid.colorblind .cell.highlight-green {
  background: #f3e5f5 !important;
  box-shadow: inset 0 0 0 3px #7b1fa2;
}

.grid.colorblind .cell.highlight-red {
  background: #fff3e0 !important;
  box-shadow: inset 0 0 0 3px #e65100;
}

.grid.colorblind .cell.highlight-yellow {
  background: #e0f7fa !important;
  box-shadow: inset 0 0 0 3px #006064;
}

/* High contrast mode */
.grid.high-contrast .cell {
  border: 1px solid #000;
}

.grid.high-contrast .cell.given input {
  color: #000;
  font-weight: 800;
}

.grid.high-contrast .cell.solved input {
  color: #1a73e8;
  font-weight: 800;
}

.grid.high-contrast .cell.selected {
  outline: 3px solid #000;
  outline-offset: -3px;
}

.grid.high-contrast .candidates-grid .candidate.visible {
  color: #333;
  font-weight: 500;
}

.cell.highlight-blue.selected,
.cell.highlight-green.selected,
.cell.highlight-red.selected,
.cell.highlight-yellow.selected {
  animation: none;
}

.grid.dark .cell.highlight-blue { background: #1a3a5c !important; }
.grid.dark .cell.highlight-green { background: #1a3c1a !important; }
.grid.dark .cell.highlight-red { background: #3c1a1a !important; }
.grid.dark .cell.highlight-yellow { background: #3c3c1a !important; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.75; }
}

/* Borders for 3x3 box edges — using box-shadow to avoid layout shifts */
.cell.border-right::after {
  content: '';
  position: absolute;
  right: -1px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #333;
  pointer-events: none;
}

.grid.dark .cell.border-right::after {
  background: #555;
}

.cell.border-bottom::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: #333;
  pointer-events: none;
}

.grid.dark .cell.border-bottom::after {
  background: #555;
}

/* Mobile touch targets */
@media (max-width: 500px) {
  .grid {
    gap: 1px;
    padding: 2px;
  }

  .cell input {
    font-size: clamp(12px, 3.5vw, 16px);
    min-height: 32px;
  }
}

/* iPhone SE and smaller */
@media (max-width: 380px) {
  .cell input {
    font-size: 14px;
    min-height: 28px;
  }
}

/* Small mobile - ensure minimum touch target size */
@media (max-width: 320px) {
  .grid {
    gap: 0;
    padding: 1px;
  }

  .cell input {
    font-size: 12px;
    min-height: 26px;
  }
}

/* Board Themes */

/* Wood theme */
.grid.theme-wood {
  background: #deb887;
  border: 3px solid #8b4513;
}
.grid.theme-wood .cell {
  background: #f5deb3;
  border-color: #d2b48c;
  color: #3e2723;
}
.grid.theme-wood .cell.given {
  color: #1a0e0a;
  font-weight: 700;
}
.grid.theme-wood .cell.selected {
  background: #ffe0b2;
  box-shadow: inset 0 0 0 2px #ff8f00;
}
.grid.theme-wood .cell.related-row,
.grid.theme-wood .cell.related-col,
.grid.theme-wood .cell.related-region {
  background: #efe0c8;
}
.grid.theme-wood .cell.border-right { border-right: 2px solid #8b4513; }
.grid.theme-wood .cell.border-bottom { border-bottom: 2px solid #8b4513; }
.grid.theme-wood.dark { background: #3e2723; border-color: #5d4037; }
.grid.theme-wood.dark .cell { background: #4e342e; color: #d7ccc8; border-color: #5d4037; }

/* Neon theme */
.grid.theme-neon {
  background: #0a0a2e;
  border: 2px solid #00ffff;
  box-shadow: 0 0 20px rgba(0,255,255,0.3);
}
.grid.theme-neon .cell {
  background: #0f0f3d;
  border-color: #1a1a5e;
  color: #00ffff;
}
.grid.theme-neon .cell.given {
  color: #ff00ff;
  text-shadow: 0 0 8px rgba(255,0,255,0.5);
}
.grid.theme-neon .cell.selected {
  background: #1a1a5e;
  box-shadow: inset 0 0 0 2px #00ffff, 0 0 10px rgba(0,255,255,0.4);
}
.grid.theme-neon .cell.related-row,
.grid.theme-neon .cell.related-col,
.grid.theme-neon .cell.related-region {
  background: #12124a;
}
.grid.theme-neon .cell.same-value { background: #1a0a3e; }
.grid.theme-neon .cell.border-right { border-right: 2px solid #00ffff; }
.grid.theme-neon .cell.border-bottom { border-bottom: 2px solid #00ffff; }

/* Minimal theme */
.grid.theme-minimal .cell {
  background: white;
  border-color: #eee;
  color: #333;
  font-weight: 300;
}
.grid.theme-minimal .cell.given {
  font-weight: 600;
  color: #111;
}
.grid.theme-minimal .cell.selected {
  background: #f5f5f5;
  box-shadow: inset 0 0 0 1px #999;
}
.grid.theme-minimal .cell.related-row,
.grid.theme-minimal .cell.related-col,
.grid.theme-minimal .cell.related-region {
  background: #fafafa;
}
.grid.theme-minimal .cell.border-right { border-right: 1px solid #ccc; }
.grid.theme-minimal .cell.border-bottom { border-bottom: 1px solid #ccc; }
.grid.theme-minimal.dark .cell { background: #222; color: #ddd; border-color: #333; }
</style>
