<template>
  <div class="grid" :class="{ dark: isDark }">
    <div
      v-for="(cell, index) in 81"
      :key="index"
      class="cell"
      :class="getCellClasses(index)"
      @click="selectCell(index)"
    >
      <input
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
    }
  },
  emits: ['update', 'select', 'navigate', 'undo', 'redo'],
  setup(props, { emit }) {
    const inputs = ref([])

    const getRow = (index) => Math.floor(index / 9)
    const getCol = (index) => index % 9
    const getRegion = (index) => {
      const row = getRow(index)
      const col = getCol(index)
      return Math.floor(row / 3) * 3 + Math.floor(col / 3)
    }

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

      return classes
    }

    const isBottomBorder = (index) => {
      const row = Math.floor(index / 9)
      return row === 2 || row === 5
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
}

.grid.dark .cell {
  background: #2d2d2d;
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

.grid.dark .cell.same-value {
  background: #4d4d3d;
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

/* Borders */
.cell.border-right {
  border-right: 2px solid #333;
}

.grid.dark .cell.border-right {
  border-right-color: #555;
}

.cell.border-bottom {
  border-bottom: 2px solid #333;
}

.grid.dark .cell.border-bottom {
  border-bottom-color: #555;
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
</style>
