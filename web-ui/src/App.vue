<template>
  <div class="app">
    <div class="container" :class="{ loading }">
      <h1>🧩 Sudoku Solver</h1>

      <ResultDisplay
        :message="resultMessage"
        :type="resultType"
        :visible="resultVisible"
        :difficulty="resultDifficulty"
        :techniques="resultTechniques"
      />

      <SudokuGrid
        :puzzle="puzzle"
        :given-cells="givenCells"
        :solved-cells="solvedCells"
        @update="onCellUpdate"
      />

      <ControlPanel
        @solve="solve"
        @clear="clearGrid"
        @generate="generate"
        @hint="getHint"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import SudokuGrid from './components/SudokuGrid.vue'
import ControlPanel from './components/ControlPanel.vue'
import ResultDisplay from './components/ResultDisplay.vue'
import { solvePuzzle, generatePuzzle, getHintForPuzzle } from './api'

export default {
  name: 'App',
  components: {
    SudokuGrid,
    ControlPanel,
    ResultDisplay
  },
  setup() {
    // Puzzle state (81 characters, '.' for empty)
    const puzzle = ref('.'.repeat(81))
    const givenCells = ref(new Set())
    const solvedCells = ref(new Set())

    // UI state
    const loading = ref(false)
    const resultMessage = ref('')
    const resultType = ref('info')
    const resultVisible = ref(false)
    const resultDifficulty = ref('')
    const resultTechniques = ref([])

    // Update a single cell
    const onCellUpdate = (index, value) => {
      const chars = puzzle.value.split('')
      chars[index] = value || '.'
      puzzle.value = chars.join('')
    }

    // Set puzzle from string
    const setPuzzle = (str, isGiven = true) => {
      puzzle.value = str
      givenCells.value = new Set()
      solvedCells.value = new Set()

      for (let i = 0; i < 81; i++) {
        if (str[i] !== '.') {
          if (isGiven) {
            givenCells.value.add(i)
          } else {
            solvedCells.value.add(i)
          }
        }
      }
    }

    // Show result
    const showResult = (message, type = 'info', difficulty = '', techniques = []) => {
      resultMessage.value = message
      resultType.value = type
      resultDifficulty.value = difficulty
      resultTechniques.value = techniques
      resultVisible.value = true

      setTimeout(() => {
        resultVisible.value = false
      }, type === 'success' ? 8000 : 5000)
    }

    // Solve the puzzle
    const solve = async () => {
      loading.value = true
      try {
        const data = await solvePuzzle(puzzle.value, true)
        if (data.solved) {
          setPuzzle(data.solution, false)
          showResult(
            `Solved in ${data.metrics.solveTimeMs.toFixed(2)}ms`,
            'success',
            data.metrics.difficulty,
            data.metrics.techniquesUsed
          )
        } else {
          showResult(data.error || 'No solution found', 'error')
        }
      } catch (e) {
        showResult('Failed to connect to server: ' + e.message, 'error')
      } finally {
        loading.value = false
      }
    }

    // Generate a new puzzle
    const generate = async (difficulty) => {
      loading.value = true
      try {
        const data = await generatePuzzle(difficulty)
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          showResult(`Generated ${data.difficulty} puzzle`, 'success')
        } else {
          showResult(data.error || 'Failed to generate puzzle', 'error')
        }
      } catch (e) {
        showResult('Failed to connect to server: ' + e.message, 'error')
      } finally {
        loading.value = false
      }
    }

    // Get a hint
    const getHint = async () => {
      loading.value = true
      try {
        const data = await getHintForPuzzle(puzzle.value)
        if (data.hasHint) {
          const hint = data.hint
          showResult(
            `Cell (${hint.row + 1}, ${hint.col + 1}) = ${hint.value} | ${hint.technique}`,
            'info'
          )
        } else {
          showResult(data.error || 'No hint available', 'error')
        }
      } catch (e) {
        showResult('Failed to connect to server: ' + e.message, 'error')
      } finally {
        loading.value = false
      }
    }

    // Clear the grid
    const clearGrid = () => {
      puzzle.value = '.'.repeat(81)
      givenCells.value = new Set()
      solvedCells.value = new Set()
      resultVisible.value = false
    }

    return {
      puzzle,
      givenCells,
      solvedCells,
      loading,
      resultMessage,
      resultType,
      resultVisible,
      resultDifficulty,
      resultTechniques,
      onCellUpdate,
      solve,
      generate,
      getHint,
      clearGrid
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.app {
  width: 100%;
  display: flex;
  justify-content: center;
}

.container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 30px;
  max-width: 500px;
  width: 100%;
}

.container.loading {
  opacity: 0.6;
  pointer-events: none;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
  font-size: 28px;
}

/* Mobile responsive */
@media (max-width: 500px) {
  body {
    padding: 10px;
  }

  .container {
    padding: 15px;
    border-radius: 12px;
  }

  h1 {
    font-size: 22px;
    margin-bottom: 15px;
  }
}

/* iPhone specific fixes */
@media (max-width: 400px) {
  .container {
    padding: 12px;
  }

  h1 {
    font-size: 20px;
  }
}
</style>
