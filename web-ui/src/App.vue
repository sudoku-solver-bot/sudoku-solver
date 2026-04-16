<template>
  <div class="app" :class="{ dark: isDark }">
    <div class="container" :class="{ loading }">
      <!-- Header with dark mode toggle -->
      <div class="header">
        <h1>🧩 Sudoku Solver</h1>
        <button class="dark-toggle" @click="toggleDarkMode" :title="isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'">
          {{ isDark ? '☀️' : '🌙' }}
        </button>
      </div>

      <!-- Toast notification -->
      <ToastNotification
        :visible="toast.visible"
        :type="toast.type"
        :title="toast.title"
        :message="toast.message"
        :show-retry="toast.showRetry"
        @close="hideToast"
        @retry="toast.onRetry"
      />

      <!-- Progress indicator -->
      <ProgressIndicator
        :puzzle="puzzle"
        :given-cells="givenCells"
        :mistakes="mistakes"
        :hints-used="hintsUsed"
        :elapsed-time="elapsedTime"
      />

      <!-- Result display -->
      <ResultDisplay
        :message="resultMessage"
        :type="resultType"
        :visible="resultVisible"
        :difficulty="resultDifficulty"
        :techniques="resultTechniques"
      />

      <!-- Loading overlay -->
      <transition name="fade">
        <div v-if="loading" class="loading-overlay">
          <div class="spinner"></div>
          <p class="loading-text">{{ loadingMessage }}</p>
        </div>
      </transition>

      <!-- Sudoku grid -->
      <SudokuGrid
        :puzzle="puzzle"
        :given-cells="givenCells"
        :solved-cells="solvedCells"
        :selected-cell="selectedCell"
        :is-dark="isDark"
        :candidates="candidates"
        :show-candidates="showCandidates"
        @update="onCellUpdate"
        @select="selectCell"
        @navigate="navigateToCell"
        @undo="undo"
        @redo="redo"
      />

      <!-- Control panel -->
      <ControlPanel
        :loading="loading"
        :can-undo="canUndo"
        :can-redo="canRedo"
        :undo-count="undoCount"
        :redo-count="redoCount"
        :show-candidates="showCandidates"
        @solve="solve"
        @clear="clearGrid"
        @generate="generate"
        @hint="getHint"
        @undo="undo"
        @redo="redo"
        @toggle-candidates="showCandidates = !showCandidates"
      />

      <!-- Mobile number pad -->
      <MobileNumberPad
        :visible="showMobilePad"
        @input="onNumberPadInput"
        @clear="clearSelectedCell"
        @hint="getHint"
      />

      <!-- Hint modal -->
      <HintModal
        :visible="hintModalVisible"
        :hint="currentHint"
        :total-hints="hintsUsed"
        @close="closeHintModal"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import SudokuGrid from './components/SudokuGrid.vue'
import ControlPanel from './components/ControlPanel.vue'
import ResultDisplay from './components/ResultDisplay.vue'
import ProgressIndicator from './components/ProgressIndicator.vue'
import ToastNotification from './components/ToastNotification.vue'
import MobileNumberPad from './components/MobileNumberPad.vue'
import HintModal from './components/HintModal.vue'
import {
  solvePuzzle,
  generatePuzzle,
  getHintForPuzzle,
  saveState,
  undo,
  redo,
  getHistory,
  fetchCandidates
} from './api'

export default {
  name: 'App',
  components: {
    SudokuGrid,
    ControlPanel,
    ResultDisplay,
    ProgressIndicator,
    ToastNotification,
    MobileNumberPad,
    HintModal
  },
  setup() {
    // Puzzle state
    const puzzle = ref('.'.repeat(81))
    const givenCells = ref(new Set())
    const solvedCells = ref(new Set())

    // UI state
    const loading = ref(false)
    const loadingMessage = ref('')
    const selectedCell = ref(-1)
    const isDark = ref(false)
    const showMobilePad = ref(false)
    const isMobile = ref(false)

    // Timer state
    const elapsedTime = ref(0)
    let timerInterval = null

    // Progress tracking
    const mistakes = ref(0)
    const hintsUsed = ref(0)

    // Undo/Redo state
    const canUndo = ref(false)
    const canRedo = ref(false)
    const undoCount = ref(0)
    const redoCount = ref(0)
    let lastSavedState = ''

    // Result display state
    const resultMessage = ref('')
    const resultType = ref('info')
    const resultVisible = ref(false)
    const resultDifficulty = ref('')
    const resultTechniques = ref([])

    // Toast notification state
    const toast = reactive({
      visible: false,
      type: 'info',
      title: '',
      message: '',
      showRetry: false,
      onRetry: null
    })

    // Hint modal state
    const hintModalVisible = ref(false)
    const currentHint = ref(null)

    // Candidates (pencil marks) state
    const candidates = ref({})
    const showCandidates = ref(true)

    // Initialize dark mode from localStorage or system preference
    onMounted(() => {
      const savedDarkMode = localStorage.getItem('sudokuDarkMode')
      if (savedDarkMode !== null) {
        isDark.value = savedDarkMode === 'true'
      } else {
        isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
      }

      // Check if mobile device
      checkMobile()
      window.addEventListener('resize', checkMobile)

      // Start timer on first puzzle load
      startTimer()

      // Load initial undo/redo state
      loadHistoryState()
    })

    onUnmounted(() => {
      window.removeEventListener('resize', checkMobile)
      stopTimer()
    })

    const checkMobile = () => {
      isMobile.value = window.innerWidth < 768
    }

    const toggleDarkMode = () => {
      isDark.value = !isDark.value
      localStorage.setItem('sudokuDarkMode', isDark.value.toString())
    }

    const startTimer = () => {
      stopTimer()
      elapsedTime.value = 0
      timerInterval = setInterval(() => {
        elapsedTime.value += 1000
      }, 1000)
    }

    const stopTimer = () => {
      if (timerInterval) {
        clearInterval(timerInterval)
        timerInterval = null
      }
    }

    // Update a single cell
    const onCellUpdate = async (index, value) => {
      const chars = puzzle.value.split('')
      const oldValue = chars[index]
      chars[index] = value || '.'
      puzzle.value = chars.join('')

      // Show mobile pad on mobile if value was cleared
      if (isMobile.value && value === '') {
        showMobilePad.value = true
      }

      // Auto-save state for undo/redo
      await autoSaveState()

      // Refresh candidates after cell update
      await refreshCandidates()

      // Track mistakes if changing a solved cell to wrong value
      if (solvedCells.value.has(index) && value !== '.' && value !== oldValue) {
        // Could add validation here
      }
    }

    // Number pad input
    const onNumberPadInput = (num) => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, num.toString())
      }
    }

    const clearSelectedCell = () => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, '')
      }
    }

    // Cell selection
    const selectCell = (index) => {
      selectedCell.value = index
      // Show mobile pad when cell is selected on mobile
      if (isMobile.value && index >= 0 && !givenCells.value.has(index)) {
        showMobilePad.value = true
      }
    }

    const navigateToCell = (index) => {
      selectedCell.value = index
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

      // Reset progress tracking
      mistakes.value = 0
      hintsUsed.value = 0
      startTimer()

      // Fetch candidates for the new puzzle
      refreshCandidates()
    }

    // Refresh candidates from the backend
    const refreshCandidates = async () => {
      try {
        const data = await fetchCandidates(puzzle.value)
        if (data.candidates) {
          candidates.value = data.candidates
        }
      } catch (e) {
        // Silently fail - candidates are optional
        console.error('Failed to fetch candidates:', e)
      }
    }

    // Show toast notification
    const showToast = (title, message, type = 'info', showRetry = false, onRetry = null) => {
      toast.title = title
      toast.message = message
      toast.type = type
      toast.showRetry = showRetry
      toast.onRetry = onRetry
      toast.visible = true
    }

    const hideToast = () => {
      toast.visible = false
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

    // Undo/Redo functions
    const autoSaveState = async () => {
      const currentState = puzzle.value
      if (currentState !== lastSavedState) {
        try {
          await saveState(currentState)
          lastSavedState = currentState
          await loadHistoryState()
        } catch (e) {
          console.error('Failed to save state:', e)
        }
      }
    }

    const loadHistoryState = async () => {
      try {
        const data = await getHistory()
        canUndo.value = data.canUndo || false
        canRedo.value = data.canRedo || false
        undoCount.value = data.undoCount || 0
        redoCount.value = data.redoCount || 0
      } catch (e) {
        console.error('Failed to load history state:', e)
      }
    }

    const undoAction = async () => {
      try {
        const data = await undo()
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          await loadHistoryState()
          showResult('Undone!', 'info')
        } else {
          showToast('Undo Failed', data.error || 'Nothing to undo', 'error')
        }
      } catch (e) {
        showToast('Error', 'Failed to undo: ' + e.message, 'error', true, undoAction)
      }
    }

    const redoAction = async () => {
      try {
        const data = await redo()
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          await loadHistoryState()
          showResult('Redone!', 'info')
        } else {
          showToast('Redo Failed', data.error || 'Nothing to redo', 'error')
        }
      } catch (e) {
        showToast('Error', 'Failed to redo: ' + e.message, 'error', true, redoAction)
      }
    }

    const undo = () => {
      undoAction()
    }

    const redo = () => {
      redoAction()
    }

    // Solve the puzzle
    const solve = async () => {
      loading.value = true
      loadingMessage.value = 'Solving puzzle...'
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
          stopTimer()
        } else {
          showResult(data.error || 'No solution found', 'error')
        }
      } catch (e) {
        showToast(
          'Connection Error',
          'Failed to connect to server. Please check your connection.',
          'error',
          true,
          solve
        )
      } finally {
        loading.value = false
      }
    }

    // Generate a new puzzle
    const generate = async (difficulty) => {
      loading.value = true
      loadingMessage.value = `Generating ${difficulty.toLowerCase()} puzzle...`
      try {
        const data = await generatePuzzle(difficulty)
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          showResult(`Generated ${data.difficulty} puzzle!`, 'success')
          selectedCell.value = -1
          showMobilePad.value = false
          lastSavedState = data.puzzle
          await saveState(data.puzzle)
          await loadHistoryState()
        } else {
          showResult(data.error || 'Failed to generate puzzle', 'error')
        }
      } catch (e) {
        showToast(
          'Error',
          'Failed to generate puzzle: ' + e.message,
          'error',
          true,
          () => generate(difficulty)
        )
      } finally {
        loading.value = false
      }
    }

    // Get a hint
    const getHint = async () => {
      loading.value = true
      loadingMessage.value = 'Finding hint...'
      try {
        const data = await getHintForPuzzle(puzzle.value)
        if (data.hasHint) {
          currentHint.value = data.hint
          hintsUsed.value++
          hintModalVisible.value = true
          showMobilePad.value = false
        } else {
          showToast(
            'No Hint Available',
            data.error || 'Try solving some more cells first!',
            'warning'
          )
        }
      } catch (e) {
        showToast(
          'Error',
          'Failed to get hint: ' + e.message,
          'error',
          true,
          getHint
        )
      } finally {
        loading.value = false
      }
    }

    const closeHintModal = () => {
      hintModalVisible.value = false
    }

    // Clear the grid
    const clearGrid = () => {
      puzzle.value = '.'.repeat(81)
      givenCells.value = new Set()
      solvedCells.value = new Set()
      resultVisible.value = false
      selectedCell.value = -1
      showMobilePad.value = false
      mistakes.value = 0
      hintsUsed.value = 0
      candidates.value = {}
      startTimer()
      lastSavedState = puzzle.value
    }

    return {
      puzzle,
      givenCells,
      solvedCells,
      loading,
      loadingMessage,
      selectedCell,
      isDark,
      showMobilePad,
      elapsedTime,
      mistakes,
      hintsUsed,
      canUndo,
      canRedo,
      undoCount,
      redoCount,
      resultMessage,
      resultType,
      resultVisible,
      resultDifficulty,
      resultTechniques,
      toast,
      hintModalVisible,
      currentHint,
      candidates,
      showCandidates,
      toggleDarkMode,
      onCellUpdate,
      onNumberPadInput,
      clearSelectedCell,
      selectCell,
      navigateToCell,
      solve,
      generate,
      getHint,
      undo,
      redo,
      clearGrid,
      hideToast,
      closeHintModal
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
  min-height: 100vh;
  transition: background 0.3s ease;
}

.app {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: background 0.3s ease;
}

.app.dark {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 30px;
  max-width: 500px;
  width: 100%;
  position: relative;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.app.dark .container {
  background: #1e1e1e;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.container.loading {
  opacity: 0.6;
  pointer-events: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h1 {
  color: #333;
  font-size: 28px;
  transition: color 0.3s ease;
}

.app.dark h1 {
  color: #e0e0e0;
}

.dark-toggle {
  width: 44px;
  height: 44px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app.dark .dark-toggle {
  background: #333;
}

.dark-toggle:hover {
  transform: scale(1.1);
}

.dark-toggle:active {
  transform: scale(0.95);
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.app.dark .loading-overlay {
  background: rgba(30, 30, 30, 0.9);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e0e0e0;
  border-top-color: #4285f4;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
}

.app.dark .loading-text {
  color: #aaa;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Mobile responsive */
@media (max-width: 500px) {
  body {
    padding: 10px;
  }

  .app {
    padding: 10px;
    align-items: flex-start;
  }

  .container {
    padding: 15px;
    border-radius: 12px;
  }

  h1 {
    font-size: 22px;
  }

  .dark-toggle {
    width: 40px;
    height: 40px;
    font-size: 18px;
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

/* Extra small screens */
@media (max-width: 320px) {
  .app {
    padding: 5px;
  }

  .container {
    padding: 10px;
    border-radius: 8px;
  }

  h1 {
    font-size: 18px;
  }
}
</style>
