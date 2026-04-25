<template>
  <div
    class="practice-mode"
    :class="{ dark: isDark }"
  >
    <!-- Header -->
    <div class="practice-header">
      <button
        class="back-btn"
        @click="$emit('exit')"
      >
        ← Back
      </button>
      <div class="practice-info">
        <h2>Practice: {{ practiceSet.technique }}</h2>
        <span class="puzzle-count">Puzzle {{ currentPuzzleIndex + 1 }} of {{ practiceSet.puzzles.length }}</span>
      </div>
    </div>

    <!-- Puzzle selector -->
    <div class="puzzle-tabs">
      <button
        v-for="(p, idx) in practiceSet.puzzles"
        :key="p.id"
        class="puzzle-tab"
        :class="{ active: idx === currentPuzzleIndex, completed: completedPuzzles.has(p.id) }"
        @click="selectPuzzle(idx)"
      >
        {{ idx + 1 }}
      </button>
    </div>

    <!-- Board -->
    <div class="board-side">
      <SudokuGrid
        :puzzle="boardPuzzle"
        :given-cells="givenCells"
        :solved-cells="solvedCells"
        :selected-cell="selectedCell"
        :is-dark="isDark"
        :candidates="boardCandidates"
        :show-candidates="true"
        @select="onCellSelect"
        @update="onCellUpdate"
      />
    </div>

    <!-- Actions -->
    <div class="practice-actions">
      <button
        class="action-btn hint-btn"
        @click="getHint"
      >
        💡 Hint
      </button>
      <button
        class="action-btn check-btn"
        @click="checkSolution"
      >
        ✓ Check
      </button>
      <button
        class="action-btn reset-btn"
        @click="resetPuzzle"
      >
        ↺ Reset
      </button>
    </div>

    <!-- Hint display -->
    <div
      v-if="hintText"
      class="hint-box"
    >
      {{ hintText }}
    </div>

    <!-- Feedback -->
    <div
      v-if="feedback"
      class="feedback"
      :class="feedbackType"
    >
      {{ feedback }}
    </div>

    <!-- Completion overlay -->
    <div
      v-if="showComplete"
      class="complete-overlay"
      @click="showComplete = false"
    >
      <div
        class="complete-content"
        @click.stop
      >
        <div class="complete-emoji">
          🎉
        </div>
        <h2>Puzzle Solved!</h2>
        <p>Great work practicing {{ practiceSet.technique }}!</p>
        <div class="complete-btns">
          <button
            v-if="currentPuzzleIndex < practiceSet.puzzles.length - 1"
            class="action-btn next-btn"
            @click="nextPuzzle"
          >
            Next Puzzle →
          </button>
          <button
            class="action-btn done-btn"
            @click="$emit('exit')"
          >
            Back to Tutorials
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, onMounted, watch } from 'vue'
import SudokuGrid from './SudokuGrid.vue'
import { fetchPracticeBoard, fetchCandidates } from '../api'

interface Props {
  practiceSet: Record<string, any>
  isDark?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  isDark: false
})
const emit = defineEmits<{ exit: []; completed: [payload: { puzzleId: string; tutorialId: string }] }>()

const currentPuzzleIndex = ref(0)
const boardPuzzle = ref('')
const originalPuzzle = ref('')
const boardCandidates = ref({})
const givenCells = ref(new Set())
const solvedCells = ref(new Set())
const selectedCell = ref(-1)
const hintText = ref('')
const feedback = ref('')
const feedbackType = ref('info')
const showComplete = ref(false)
const completedPuzzles = ref(new Set())

const loadPuzzle = async () => {
  const puzzle = props.practiceSet.puzzles[currentPuzzleIndex.value]
  if (!puzzle) return

  boardPuzzle.value = puzzle.puzzle
  originalPuzzle.value = puzzle.puzzle
  givenCells.value = new Set()
  solvedCells.value = new Set()
  selectedCell.value = -1
  hintText.value = ''
  feedback.value = ''

  for (let i = 0; i < 81; i++) {
    if (puzzle.puzzle[i] !== '.') givenCells.value.add(i)
  }

  try {
    const data = await fetchPracticeBoard(props.practiceSet.tutorialId, puzzle.id)
    if (data.candidates) boardCandidates.value = data.candidates
  } catch (e) {
    console.error('Failed to load practice board:', e)
  }
}

const onCellSelect = (index) => {
  selectedCell.value = index
}

const onCellUpdate = async (index, value) => {
  const chars = boardPuzzle.value.split('')
  chars[index] = value || '.'
  boardPuzzle.value = chars.join('')

  // Refresh candidates
  try {
    const data = await fetchCandidates(boardPuzzle.value)
    if (data.candidates) boardCandidates.value = data.candidates
  } catch (e) {
    // Silently fail
  }

  // Check if puzzle is complete
  if (!boardPuzzle.value.includes('.')) {
    checkSolution()
  }
}

const getHint = async () => {
  hintText.value = 'Loading hint...'
  try {
    const data = await fetchCandidates(boardPuzzle.value)
    if (data.candidates) {
      // Find a cell with only one candidate as a hint
      const entries = Object.entries(data.candidates)
      const singleCand = entries.find(([_, vals]) => vals.length === 1)
      if (singleCand) {
        const idx = parseInt(singleCand[0])
        const val = singleCand[1][0]
        const row = Math.floor(idx / 9) + 1
        const col = (idx % 9) + 1
        hintText.value = `Try cell at row ${row}, column ${col} — it has only one candidate: ${val}`
      } else {
        hintText.value = 'No obvious hint found. Try eliminating candidates using ' + props.practiceSet.technique + '.'
      }
    }
  } catch (e) {
    hintText.value = 'Could not load hint.'
  }
}

const checkSolution = () => {
  if (boardPuzzle.value.includes('.')) {
    feedback.value = 'Puzzle is not complete yet. Keep going!'
    feedbackType.value = 'info'
    return
  }

  // Basic validation: check rows, cols, boxes
  let valid = true
  for (let i = 0; i < 9; i++) {
    const row = new Set()
    const col = new Set()
    const box = new Set()
    for (let j = 0; j < 9; j++) {
      const rv = boardPuzzle.value[i * 9 + j]
      const cv = boardPuzzle.value[j * 9 + i]
      const br = Math.floor(i / 3) * 3 + Math.floor(j / 3)
      const bc = (i % 3) * 3 + (j % 3)
      const bv = boardPuzzle.value[br * 9 + bc]

      if (row.has(rv) || col.has(cv) || box.has(bv)) {
        valid = false
        break
      }
      row.add(rv)
      col.add(cv)
      box.add(bv)
    }
    if (!valid) break
  }

  if (valid) {
    feedback.value = ''
    showComplete.value = true
    const puzzle = props.practiceSet.puzzles[currentPuzzleIndex.value]
    completedPuzzles.value.add(puzzle.id)
    saveProgress(puzzle.id)
    emit('completed', { puzzleId: puzzle.id, tutorialId: props.practiceSet.tutorialId })
  } else {
    feedback.value = 'Something is wrong — check for duplicates!'
    feedbackType.value = 'error'
  }
}

const saveProgress = (puzzleId) => {
  try {
    const key = 'sudoku-dojo-practice-progress'
    const saved = JSON.parse(localStorage.getItem(key) || '{}')
    const setId = props.practiceSet.id
    if (!saved[setId]) saved[setId] = []
    if (!saved[setId].includes(puzzleId)) saved[setId].push(puzzleId)
    localStorage.setItem(key, JSON.stringify(saved))
  } catch (e) {
    console.error('Failed to save practice progress:', e)
  }
}

const loadProgress = () => {
  try {
    const key = 'sudoku-dojo-practice-progress'
    const saved = JSON.parse(localStorage.getItem(key) || '{}')
    const setId = props.practiceSet.id
    if (saved[setId]) {
      completedPuzzles.value = new Set(saved[setId])
    }
  } catch (e) {}
}

const resetPuzzle = () => {
  boardPuzzle.value = originalPuzzle.value
  givenCells.value = new Set()
  solvedCells.value = new Set()
  for (let i = 0; i < 81; i++) {
    if (originalPuzzle.value[i] !== '.') givenCells.value.add(i)
  }
  hintText.value = ''
  feedback.value = ''
  loadPuzzle()
}

const selectPuzzle = (idx) => {
  currentPuzzleIndex.value = idx
  loadPuzzle()
}

const nextPuzzle = () => {
  if (currentPuzzleIndex.value < props.practiceSet.puzzles.length - 1) {
    currentPuzzleIndex.value++
    showComplete.value = false
    loadPuzzle()
  }
}

watch(() => props.practiceSet.id, () => {
  currentPuzzleIndex.value = 0
  loadProgress()
  loadPuzzle()
})

onMounted(() => {
  loadProgress()
  loadPuzzle()
})
</script>

<style scoped>
.practice-mode {
  width: 100%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.practice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.practice-mode.dark .practice-header {
  border-bottom-color: #444;
}

.back-btn {
  background: #f0f0f0;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.practice-mode.dark .back-btn {
  background: #333;
  color: #ccc;
}

.back-btn:hover {
  background: #e0e0e0;
}

.practice-info {
  flex: 1;
}

.practice-info h2 {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.practice-mode.dark .practice-info h2 {
  color: #e0e0e0;
}

.puzzle-count {
  font-size: 12px;
  color: #666;
}

.practice-mode.dark .puzzle-count {
  color: #aaa;
}

.puzzle-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.puzzle-tab {
  width: 36px;
  height: 36px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.practice-mode.dark .puzzle-tab {
  background: #2d2d2d;
  border-color: #444;
  color: #ddd;
}

.puzzle-tab.active {
  border-color: #4285f4;
  background: #e3f2fd;
  color: #4285f4;
}

.puzzle-tab.completed {
  border-color: #34a853;
  background: #e8f5e9;
  color: #34a853;
}

.practice-mode.dark .puzzle-tab.active {
  background: #1a3a5c;
}

.practice-mode.dark .puzzle-tab.completed {
  background: #1a3c1a;
}

.board-side {
  max-width: 400px;
  margin-bottom: 12px;
}

.practice-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.hint-btn {
  background: #fff8e1;
  color: #f9a825;
  border: 1px solid #fdd835;
}

.check-btn {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #34a853;
}

.check-btn:hover {
  background: #c8e6c9;
}

.reset-btn {
  background: #f0f0f0;
  color: #666;
}

.reset-btn:hover {
  background: #e0e0e0;
}

.next-btn {
  background: #4285f4;
  color: white;
}

.next-btn:hover {
  background: #3367d6;
}

.done-btn {
  background: #4285f4;
  color: white;
}

.done-btn:hover {
  transform: scale(1.05);
}

.hint-box {
  padding: 10px 14px;
  background: #fff8e1;
  border: 1px solid #fdd835;
  border-radius: 8px;
  font-size: 14px;
  color: #f57f17;
  margin-bottom: 8px;
}

.feedback {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.feedback.info {
  background: #e3f2fd;
  color: #1565c0;
}

.feedback.error {
  background: #fce4ec;
  color: #c62828;
}

/* Completion overlay */
.complete-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.complete-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 360px;
  animation: popIn 0.5s ease;
}

.practice-mode.dark .complete-content {
  background: #2d2d2d;
  color: #e0e0e0;
}

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.complete-emoji {
  font-size: 48px;
  margin-bottom: 12px;
}

.complete-content h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.practice-mode.dark .complete-content h2 {
  color: #e0e0e0;
}

.complete-content p {
  color: #666;
  margin-bottom: 20px;
}

.practice-mode.dark .complete-content p {
  color: #aaa;
}

.complete-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* Mobile */
@media (max-width: 500px) {
  .practice-header {
    gap: 8px;
  }

  .practice-info h2 {
    font-size: 16px;
  }

  .action-btn {
    padding: 8px 14px;
    font-size: 13px;
  }

  .complete-content {
    padding: 24px;
    margin: 0 16px;
  }
}
</style>
