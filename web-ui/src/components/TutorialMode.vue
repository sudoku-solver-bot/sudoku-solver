<template>
  <div
    class="tutorial-mode"
    :class="{ dark: isDark }"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
  >
    <!-- Header -->
    <div class="tutorial-header">
      <button
        class="back-btn"
        @click="$emit('exit')"
      >
        ← Back
      </button>
      <div
        class="belt-badge"
        :style="{ background: lesson.beltColor }"
      >
        <span class="belt-emoji">{{ lesson.beltEmoji }}</span>
      </div>
      <div class="lesson-info">
        <h2>{{ lesson.title }}</h2>
        <span class="belt-label">{{ lesson.beltName }}</span>
      </div>
    </div>

    <!-- Split layout -->
    <div class="tutorial-body">
      <!-- Board side -->
      <div class="board-side">
        <SudokuGrid
          :puzzle="boardPuzzle"
          :given-cells="givenCells"
          :solved-cells="solvedCells"
          :selected-cell="selectedCell"
          :is-dark="isDark"
          :candidates="boardCandidates"
          :show-candidates="true"
          :highlighted-cells="currentHighlight"
          @select="onCellSelect"
        />
      </div>

      <!-- Lesson panel -->
      <div class="lesson-panel">
        <!-- Progress -->
        <div class="step-progress">
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: progressPercent + '%' }"
            />
          </div>
          <span class="progress-text">Step {{ currentStepIndex + 1 }} of {{ lesson.steps.length }}</span>
        </div>

        <!-- Step content -->
        <div class="step-content">
          <p class="step-text">
            {{ currentStep.text }}
          </p>
        </div>

        <!-- Navigation -->
        <div class="step-nav">
          <button
            v-if="currentStepIndex > 0"
            class="nav-btn secondary"
            @click="prevStep"
          >
            ← Back
          </button>

          <button
            v-if="currentStep.type === 'question'"
            class="nav-btn hint"
            @click="showAnswer"
          >
            💡 Show Me
          </button>

          <button
            v-if="currentStepIndex < lesson.steps.length - 1"
            class="nav-btn primary"
            @click="nextStep"
          >
            Next →
          </button>

          <button
            v-if="currentStepIndex === lesson.steps.length - 1"
            class="nav-btn celebrate"
            @click="completeLesson"
          >
            🎉 Complete!
          </button>
        </div>

        <!-- Feedback -->
        <div
          v-if="feedback"
          class="feedback"
          :class="feedbackType"
        >
          {{ feedback }}
        </div>
      </div>
    </div>

    <!-- Celebration overlay -->
    <div
      v-if="celebrating"
      class="celebration-overlay"
      @click="celebrating = false"
    >
      <div class="celebration-content">
        <div class="confetti">
          🎉🎊⭐🏆🎯
        </div>
        <h2>Lesson Complete!</h2>
        <p>You've mastered <strong>{{ lesson.title }}</strong>!</p>
        <p class="belt-earned">
          {{ lesson.beltEmoji }} {{ lesson.beltName }} earned!
        </p>
        <button
          class="nav-btn primary"
          @click="$emit('exit')"
        >
          Continue
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, computed, onMounted, watch } from 'vue'
import SudokuGrid from './SudokuGrid.vue'
import { fetchTutorialBoard, completeTutorial } from '../api'

const props = defineProps({
    lesson: { type: Object, required: true },
    isDark: { type: Boolean, default: false }
  })
const emit = defineEmits(['exit', 'completed'])

const boardPuzzle = ref(props.lesson.examplePuzzle)
const boardCandidates = ref({})
const givenCells = ref(new Set())
const solvedCells = ref(new Set())
const selectedCell = ref(-1)
const currentStepIndex = ref(0)
const feedback = ref('')
const feedbackType = ref('info')
const celebrating = ref(false)

const currentStep = computed(() => props.lesson.steps[currentStepIndex.value] || props.lesson.steps[0])

const currentHighlight = computed(() => {
  const step = currentStep.value
  if (step.cells && step.cells.length > 0) {
    return [{ cells: step.cells, color: step.highlightColor }]
  }
  return []
})

const progressPercent = computed(() => {
  return ((currentStepIndex.value + 1) / props.lesson.steps.length) * 100
})

// Initialize puzzle
const initPuzzle = () => {
  const puzzle = props.lesson.examplePuzzle
  boardPuzzle.value = puzzle
  givenCells.value = new Set()
  solvedCells.value = new Set()
  for (let i = 0; i < 81; i++) {
    if (puzzle[i] !== '.') {
      givenCells.value.add(i)
    }
  }
  loadBoardCandidates()
}

const loadBoardCandidates = async () => {
  try {
    const data = await fetchTutorialBoard(props.lesson.id)
    if (data.candidates) {
      boardCandidates.value = data.candidates
    }
  } catch (e) {
    console.error('Failed to load tutorial board:', e)
  }
}

const nextStep = () => {
  if (currentStepIndex.value < props.lesson.steps.length - 1) {
    currentStepIndex.value++
    feedback.value = ''
  }
}

const prevStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value--
    feedback.value = ''
  }
}

const showAnswer = () => {
  const step = currentStep.value
  if (step.type === 'question') {
    feedback.value = `The answer: ${step.answerValue} goes in the highlighted cell!`
    feedbackType.value = 'hint'
  }
}

const onCellSelect = (index) => {
  selectedCell.value = index
  const step = currentStep.value
  if (step.type === 'question' && step.answer !== undefined) {
    // Check if they clicked the right cell
    // answer field is 1-based index in the highlighted cells array
    const targetCell = step.cells[step.answer]
    if (index === targetCell) {
      feedback.value = `Correct! 🎯 The answer is ${step.answerValue}!`
      feedbackType.value = 'success'
    } else {
      feedback.value = 'Not quite — try another cell!'
      feedbackType.value = 'error'
    }
  }
}

// Swipe gesture support
let touchStartX = ref(0)
let touchStartY = ref(0)

const onTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
}

const onTouchEnd = (e) => {
  const dx = e.changedTouches[0].clientX - touchStartX.value
  const dy = e.changedTouches[0].clientY - touchStartY.value
  // Only trigger if horizontal swipe is dominant and long enough
  if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) {
    if (dx < 0) nextStep()  // Swipe left → next
    else prevStep()          // Swipe right → prev
  }
}

const completeLesson = async () => {
  try {
    await completeTutorial(props.lesson.id)
    celebrating.value = true
    emit('completed', props.lesson.id)
  } catch (e) {
    console.error('Failed to complete tutorial:', e)
    celebrating.value = true // Still celebrate
    emit('completed', props.lesson.id)
  }
}

// Watch for lesson changes
watch(() => props.lesson.id, () => {
  currentStepIndex.value = 0
  feedback.value = ''
  celebrating.value = false
  initPuzzle()
})

onMounted(() => {
  initPuzzle()
})
</script>

<style scoped>
.tutorial-mode {
  width: 100%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.tutorial-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.tutorial-mode.dark .tutorial-header {
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

.tutorial-mode.dark .back-btn {
  background: #333;
  color: #ccc;
}

.back-btn:hover {
  background: #e0e0e0;
}

.belt-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border: 2px solid #ccc;
  flex-shrink: 0;
}

.lesson-info {
  flex: 1;
}

.lesson-info h2 {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.tutorial-mode.dark .lesson-info h2 {
  color: #e0e0e0;
}

.belt-label {
  font-size: 12px;
  color: #666;
}

.tutorial-mode.dark .belt-label {
  color: #aaa;
}

.tutorial-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.board-side {
  flex: 0 0 auto;
  max-width: 400px;
}

.lesson-panel {
  flex: 1;
  min-width: 200px;
}

.step-progress {
  margin-bottom: 16px;
}

.progress-bar {
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 4px;
}

.tutorial-mode.dark .progress-bar {
  background: #444;
}

.progress-fill {
  height: 100%;
  background: #4285f4;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #888;
}

.step-content {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  min-height: 120px;
  max-height: 120px;
  overflow-y: auto;
}

.tutorial-mode.dark .step-content {
  background: #2a2a2a;
}

.step-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin: 0;
}

.tutorial-mode.dark .step-text {
  color: #ddd;
}

.step-nav {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn.primary {
  background: #4285f4;
  color: white;
}

.nav-btn.primary:hover {
  background: #3367d6;
}

.nav-btn.secondary {
  background: #f0f0f0;
  color: #666;
}

.nav-btn.hint {
  background: #fff8e1;
  color: #f9a825;
  border: 1px solid #fdd835;
}

.nav-btn.celebrate {
  background: #4285f4;
  color: white;
  border-color: #4285f4;
}

.nav-btn.celebrate:hover {
  transform: scale(1.05);
}

.feedback {
  margin-top: 12px;
  padding: 10px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.feedback.success {
  background: #e8f5e9;
  color: #2e7d32;
}

.feedback.error {
  background: #fce4ec;
  color: #c62828;
}

.feedback.hint {
  background: #fff8e1;
  color: #f57f17;
}

.feedback.info {
  background: #e3f2fd;
  color: #1565c0;
}

.celebration-overlay {
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

.celebration-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 360px;
  animation: popIn 0.5s ease;
}

.tutorial-mode.dark .celebration-content {
  background: #2d2d2d;
  color: #e0e0e0;
}

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.confetti {
  font-size: 40px;
  margin-bottom: 16px;
}

.celebration-content h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.tutorial-mode.dark .celebration-content h2 {
  color: #e0e0e0;
}

.belt-earned {
  font-size: 18px;
  margin: 12px 0 20px;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .tutorial-body {
    flex-direction: column;
  }

  .board-side {
    max-width: 100%;
  }

  .lesson-panel {
    min-width: auto;
  }

  .step-content {
    padding: 12px;
  }

  .step-text {
    font-size: 14px;
  }
}

@media (max-width: 500px) {
  .tutorial-header {
    gap: 8px;
    flex-wrap: wrap;
  }

  .belt-badge {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .lesson-info h2 {
    font-size: 16px;
  }

  .nav-btn {
    padding: 8px 14px;
    font-size: 13px;
  }

  .step-content {
    padding: 10px;
    border-radius: 10px;
  }

  .step-text {
    font-size: 14px;
    line-height: 1.5;
  }

  .celebration-content {
    padding: 24px;
    margin: 0 16px;
  }
}
</style>
