<template>
  <div
    class="quiz-mode"
    :class="{ dark: isDark }"
  >
    <!-- Header -->
    <div class="quiz-header">
      <button
        class="back-btn"
        @click="$emit('exit')"
      >
        ← Back
      </button>
      <div
        class="belt-badge"
        :style="{ background: quiz.beltColor }"
      >
        <span class="belt-emoji">{{ quiz.beltEmoji }}</span>
      </div>
      <div class="quiz-info">
        <h2>Quiz: {{ quiz.technique }}</h2>
        <span class="belt-label">{{ quiz.beltName }}</span>
      </div>
    </div>

    <!-- Score -->
    <div class="score-bar">
      <span class="score-text">Score: {{ score }} / {{ totalAnswered }}</span>
      <span
        v-if="totalAnswered > 0"
        class="score-pct"
      >{{ scorePercent }}%</span>
    </div>

    <!-- Question -->
    <div
      v-if="currentQuestion"
      class="quiz-body"
    >
      <div class="question-text">
        <span class="q-number">Q{{ currentQuestionIndex + 1 }}/{{ quiz.questions.length }}</span>
        {{ currentQuestion.question }}
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
          :highlighted-cells="currentHighlight"
          @select="onCellSelect"
        />
      </div>

      <!-- Hint -->
      <div
        v-if="showHint && !answered"
        class="hint-box"
      >
        {{ currentQuestion.hint }}
      </div>

      <!-- Feedback -->
      <transition name="flash">
        <div
          v-if="answered"
          class="answer-feedback"
          :class="isCorrect ? 'correct' : 'wrong'"
        >
          <span
            v-if="isCorrect"
            class="feedback-icon"
          >✓</span>
          <span
            v-else
            class="feedback-icon"
          >✗</span>
          <span>{{ isCorrect ? 'Correct! Great job!' : 'Not quite — the answer was row ' + answerRow + ', col ' + answerCol }}</span>
        </div>
      </transition>

      <!-- Actions -->
      <div class="quiz-actions">
        <button
          v-if="!answered"
          class="action-btn hint-btn"
          @click="showHint = true"
        >
          Show Hint
        </button>

        <button
          v-if="answered && currentQuestionIndex < quiz.questions.length - 1"
          class="action-btn next-btn"
          @click="nextQuestion"
        >
          Next Question →
        </button>

        <button
          v-if="answered && currentQuestionIndex === quiz.questions.length - 1"
          class="action-btn done-btn"
          @click="finishQuiz"
        >
          See Results 🎉
        </button>
      </div>
    </div>

    <!-- Results overlay -->
    <div
      v-if="showResults"
      class="results-overlay"
      @click="showResults = false"
    >
      <div
        class="results-content"
        @click.stop
      >
        <div class="results-emoji">
          🏆
        </div>
        <h2>Quiz Complete!</h2>
        <div class="results-score">
          <span class="big-score">{{ score }} / {{ quiz.questions.length }}</span>
          <span class="score-label">Correct Answers</span>
        </div>
        <p
          v-if="score === quiz.questions.length"
          class="results-msg perfect"
        >
          Perfect score! You've mastered {{ quiz.technique }}!
        </p>
        <p
          v-else-if="score >= quiz.questions.length / 2"
          class="results-msg good"
        >
          Good job! Keep practicing {{ quiz.technique }}.
        </p>
        <p
          v-else
          class="results-msg try-again"
        >
          Keep practicing — you'll get it next time!
        </p>
        <div class="results-btns">
          <button
            class="action-btn next-btn"
            @click="$emit('exit')"
          >
            Back to Tutorials
          </button>
          <button
            class="action-btn hint-btn"
            @click="retryQuiz"
          >
            Try Again
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

import { ref, computed, onMounted, watch } from 'vue'
import SudokuGrid from './SudokuGrid.vue'
import { fetchQuizBoard } from '../api'

const props = defineProps({
    quiz: { type: Object, required: true },
    isDark: { type: Boolean, default: false }
  })
const emit = defineEmits(['exit', 'completed'])

const currentQuestionIndex = ref(0)
const boardPuzzle = ref('')
const boardCandidates = ref({})
const givenCells = ref(new Set())
const solvedCells = ref(new Set())
const selectedCell = ref(-1)
const answered = ref(false)
const isCorrect = ref(false)
const showHint = ref(false)
const score = ref(0)
const totalAnswered = ref(0)
const showResults = ref(false)

const currentQuestion = computed(() => props.quiz.questions[currentQuestionIndex.value])

const currentHighlight = computed(() => {
  const q = currentQuestion.value
  if (q && q.highlightCells && q.highlightCells.length > 0 && !answered.value) {
    return [{ cells: q.highlightCells, color: q.highlightColor }]
  }
  if (answered.value && q) {
    return [{ cells: [q.answerCell], color: isCorrect.value ? 'green' : 'red' }]
  }
  return []
})

const scorePercent = computed(() => {
  if (totalAnswered.value === 0) return 0
  return Math.round((score.value / totalAnswered.value) * 100)
})

const answerRow = computed(() => {
  const q = currentQuestion.value
  return q ? Math.floor(q.answerCell / 9) + 1 : 0
})

const answerCol = computed(() => {
  const q = currentQuestion.value
  return q ? (q.answerCell % 9) + 1 : 0
})

const loadQuestion = async () => {
  const q = currentQuestion.value
  if (!q) return

  boardPuzzle.value = q.puzzle
  givenCells.value = new Set()
  solvedCells.value = new Set()
  selectedCell.value = -1
  answered.value = false
  isCorrect.value = false
  showHint.value = false

  for (let i = 0; i < 81; i++) {
    if (q.puzzle[i] !== '.') givenCells.value.add(i)
  }

  try {
    const data = await fetchQuizBoard(props.quiz.belt)
    if (data.candidates) boardCandidates.value = data.candidates
  } catch (e) {
    console.error('Failed to load quiz board:', e)
  }
}

const onCellSelect = (index) => {
  if (answered.value) return
  selectedCell.value = index

  const q = currentQuestion.value
  if (index === q.answerCell) {
    isCorrect.value = true
    score.value++
  } else {
    isCorrect.value = false
  }
  answered.value = true
  totalAnswered.value++

  // Save score to localStorage
  saveScore()
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < props.quiz.questions.length - 1) {
    currentQuestionIndex.value++
    loadQuestion()
  }
}

const saveScore = () => {
  try {
    const key = 'sudoku-dojo-quiz-scores'
    const saved = JSON.parse(localStorage.getItem(key) || '{}')
    const qId = currentQuestion.value.id
    saved[qId] = {
      correct: isCorrect.value,
      attempts: (saved[qId]?.attempts || 0) + 1,
      lastAttempt: Date.now()
    }
    localStorage.setItem(key, JSON.stringify(saved))
  } catch (e) {
    console.error('Failed to save quiz score:', e)
  }
}

const finishQuiz = () => {
  showResults.value = true
  emit('completed', {
    quizId: props.quiz.id,
    score: score.value,
    total: props.quiz.questions.length
  })
}

const retryQuiz = () => {
  currentQuestionIndex.value = 0
  score.value = 0
  totalAnswered.value = 0
  showResults.value = false
  loadQuestion()
}

watch(() => props.quiz.id, () => {
  currentQuestionIndex.value = 0
  score.value = 0
  totalAnswered.value = 0
  showResults.value = false
  loadQuestion()
})

onMounted(() => {
  loadQuestion()
})
</script>

<style scoped>
.quiz-mode {
  width: 100%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes correctFlash {
  0% { background-color: #c8e6c9; }
  50% { background-color: #66bb6a; }
  100% { background-color: #c8e6c9; }
}

@keyframes wrongShake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-4px); }
  40%, 80% { transform: translateX(4px); }
}

.quiz-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

.quiz-mode.dark .quiz-header {
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

.quiz-mode.dark .back-btn {
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

.quiz-info {
  flex: 1;
}

.quiz-info h2 {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.quiz-mode.dark .quiz-info h2 {
  color: #e0e0e0;
}

.belt-label {
  font-size: 12px;
  color: #666;
}

.quiz-mode.dark .belt-label {
  color: #aaa;
}

.score-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.quiz-mode.dark .score-bar {
  background: #2a2a2a;
}

.score-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.quiz-mode.dark .score-text {
  color: #ddd;
}

.score-pct {
  font-size: 14px;
  font-weight: 700;
  color: #4285f4;
}

.quiz-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-text {
  font-size: 15px;
  line-height: 1.5;
  color: #333;
  padding: 12px;
  background: #f0f4ff;
  border-radius: 10px;
  border-left: 4px solid #4285f4;
}

.quiz-mode.dark .question-text {
  color: #ddd;
  background: #1f1f2d;
}

.q-number {
  font-weight: 700;
  color: #4285f4;
  margin-right: 6px;
}

.board-side {
  max-width: 400px;
}

.hint-box {
  padding: 10px 14px;
  background: #fff8e1;
  border: 1px solid #fdd835;
  border-radius: 8px;
  font-size: 14px;
  color: #f57f17;
}

.answer-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
}

.answer-feedback.correct {
  background: #e8f5e9;
  color: #2e7d32;
  animation: correctFlash 0.6s ease;
}

.quiz-mode.dark .answer-feedback.correct {
  background: #1a3c1a;
}

.answer-feedback.wrong {
  background: #fce4ec;
  color: #c62828;
  animation: wrongShake 0.4s ease;
}

.quiz-mode.dark .answer-feedback.wrong {
  background: #3c1a1a;
}

.feedback-icon {
  font-size: 20px;
  font-weight: 800;
}

.quiz-actions {
  display: flex;
  gap: 8px;
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

/* Results overlay */
.results-overlay {
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

.results-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  max-width: 360px;
  animation: popIn 0.5s ease;
}

.quiz-mode.dark .results-content {
  background: #2d2d2d;
  color: #e0e0e0;
}

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.results-emoji {
  font-size: 48px;
  margin-bottom: 12px;
}

.results-content h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
}

.quiz-mode.dark .results-content h2 {
  color: #e0e0e0;
}

.results-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
}

.big-score {
  font-size: 36px;
  font-weight: 800;
  color: #4285f4;
}

.score-label {
  font-size: 14px;
  color: #888;
  margin-top: 4px;
}

.results-msg {
  font-size: 16px;
  margin-bottom: 20px;
}

.results-msg.perfect {
  color: #2e7d32;
}

.results-msg.good {
  color: #1565c0;
}

.results-msg.try-again {
  color: #e65100;
}

.results-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* Flash transition */
.flash-enter-active {
  transition: all 0.3s ease;
}

.flash-leave-active {
  transition: all 0.2s ease;
}

.flash-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.flash-leave-to {
  opacity: 0;
}

/* Mobile responsive */
@media (max-width: 500px) {
  .quiz-header {
    gap: 8px;
    flex-wrap: wrap;
  }

  .belt-badge {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .quiz-info h2 {
    font-size: 16px;
  }

  .question-text {
    font-size: 14px;
    padding: 10px;
  }

  .action-btn {
    padding: 8px 14px;
    font-size: 13px;
  }

  .results-content {
    padding: 24px;
    margin: 0 16px;
  }
}
</style>
