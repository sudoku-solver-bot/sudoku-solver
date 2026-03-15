<template>
  <div class="grid">
    <div
      v-for="(cell, index) in 81"
      :key="index"
      class="cell"
      :class="{
        given: givenCells.has(index),
        solved: solvedCells.has(index),
        'border-right': (index + 1) % 3 === 0 && (index + 1) % 9 !== 0,
        'border-bottom': isBottomBorder(index)
      }"
    >
      <input
        type="text"
        inputmode="numeric"
        pattern="[1-9]"
        maxlength="1"
        :value="puzzle[index] === '.' ? '' : puzzle[index]"
        :readonly="givenCells.has(index)"
        @input="onInput(index, $event)"
      />
    </div>
  </div>
</template>

<script>
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
    }
  },
  emits: ['update'],
  methods: {
    onInput(index, event) {
      const value = event.target.value
      // Only allow 1-9
      if (/^[1-9]$/.test(value) || value === '') {
        this.$emit('update', index, value)
      } else {
        event.target.value = ''
        this.$emit('update', index, '')
      }
    },
    isBottomBorder(index) {
      const row = Math.floor(index / 9)
      return (row === 2 || row === 5)
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

.cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  font-size: clamp(14px, 4vw, 20px);
  font-weight: 600;
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
}

.cell input:focus {
  background: #e8f0fe;
}

.cell.given input {
  color: #333;
  background: #f0f0f0;
}

.cell.solved input {
  color: #4285f4;
}

.cell.border-right {
  border-right: 2px solid #333;
}

.cell.border-bottom {
  border-bottom: 2px solid #333;
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
</style>
