<template>
  <div class="user-testing-survey">
    <div class="survey-container">
      <!-- Survey Header -->
      <div class="survey-header">
        <div class="survey-progress">
          <span class="step-indicator">Question {{ currentQuestionIndex + 1 }} of {{ totalQuestions }}</span>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: progressPercentage + '%' }"
            />
          </div>
        </div>
        <h2>{{ survey.name }}</h2>
        <p class="survey-description">
          {{ survey.description }}
        </p>
      </div>

      <!-- Question Content -->
      <div
        v-if="currentQuestion"
        class="question-content"
      >
        <div class="question-card">
          <h3>{{ currentQuestion.questionText }}</h3>
          
          <!-- Rating Questions -->
          <div
            v-if="currentQuestion.responseType === 'RATING_1_5'"
            class="rating-question"
          >
            <div class="rating-stars">
              <button 
                v-for="star in 5" 
                :key="star"
                :class="['star', { 'filled': rating >= star }]"
                :disabled="isSubmitting"
                @click="selectRating(star)"
              >
                ⭐
              </button>
            </div>
            <p class="rating-label">
              {{ rating ? `${rating}/5` : 'Click a star to rate' }}
            </p>
          </div>

          <!-- Emoji Rating -->
          <div
            v-if="currentQuestion.responseType === 'EMOJI_RATING'"
            class="emoji-question"
          >
            <div class="emoji-options">
              <button 
                v-for="emoji in emojiOptions" 
                :key="emoji.value"
                :class="['emoji-option', { 'selected': selectedEmoji === emoji.value }]"
                :disabled="isSubmitting"
                @click="selectEmoji(emoji.value)"
              >
                <span class="emoji">{{ emoji.emoji }}</span>
                <span class="label">{{ emoji.label }}</span>
              </button>
            </div>
          </div>

          <!-- Yes/No Questions -->
          <div
            v-if="currentQuestion.responseType === 'YES_NO'"
            class="yesno-question"
          >
            <div class="yesno-options">
              <button 
                :class="['yesno-btn', 'yes-btn', { 'selected': selectedBoolean === true }]"
                :disabled="isSubmitting"
                @click="selectBoolean(true)"
              >
                ✅ Yes
              </button>
              <button 
                :class="['yesno-btn', 'no-btn', { 'selected': selectedBoolean === false }]"
                :disabled="isSubmitting"
                @click="selectBoolean(false)"
              >
                ❌ No
              </button>
            </div>
          </div>

          <!-- Multiple Choice Questions -->
          <div
            v-if="currentQuestion.responseType === 'MULTIPLE_CHOICE'"
            class="multiple-choice-question"
          >
            <div class="choice-options">
              <button 
                v-for="choice in multipleChoiceOptions" 
                :key="choice"
                :class="['choice-btn', { 'selected': selectedMultipleChoice === choice }]"
                :disabled="isSubmitting"
                @click="selectMultipleChoice(choice)"
              >
                {{ choice }}
              </button>
            </div>
          </div>

          <!-- Text Questions -->
          <div
            v-if="currentQuestion.responseType === 'TEXT'"
            class="text-question"
          >
            <textarea 
              v-model="textResponse"
              :placeholder="getPlaceholderText()"
              class="text-input"
              rows="4"
              :disabled="isSubmitting"
            />
          </div>

          <!-- Rating 1-10 -->
          <div
            v-if="currentQuestion.responseType === 'RATING_1_10'"
            class="rating-10-question"
          >
            <div class="rating-scale">
              <div
                v-for="num in 10"
                :key="num"
                class="rating-option"
              >
                <input 
                  :id="`rating-${num}`" 
                  v-model="rating10" 
                  type="radio" 
                  :value="num"
                  :disabled="isSubmitting"
                >
                <label :for="`rating-${num}`">{{ num }}</label>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation Buttons -->
      <div class="navigation">
        <button 
          v-if="currentQuestionIndex > 0"
          :disabled="isSubmitting"
          class="nav-btn previous-btn"
          @click="previousQuestion"
        >
          ← Previous
        </button>

        <button 
          :disabled="!canProceed || isSubmitting"
          class="nav-btn continue-btn"
          @click="currentQuestionIndex < totalQuestions - 1 ? nextQuestion() : submitSurvey()"
        >
          {{ currentQuestionIndex < totalQuestions - 1 ? 'Next →' : 'Submit Survey' }}
        </button>
      </div>

      <!-- Loading State -->
      <div
        v-if="isSubmitting"
        class="submitting"
      >
        <div class="spinner" />
        <p>Saving your answers...</p>
      </div>

      <!-- Success Message -->
      <div
        v-if="showSuccess"
        class="success-message"
      >
        <div class="success-content">
          <div class="success-icon">
            🎉
          </div>
          <h3>Thank You!</h3>
          <p>Your feedback helps us make Sudoku even more fun!</p>
          <button
            class="close-btn"
            @click="closeSurvey"
          >
            Continue Playing
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Question {
  questionId: string
  questionText: string
  responseType: string
  category: string
}

interface Survey {
  surveyId: string
  name: string
  description: string
  questions: Question[]
}

interface RatingResponse {
  type: 'RatingResponse'
  value: number
}

interface TextResponse {
  type: 'TextResponse'
  text: string | boolean | null
}

type SurveyResponse = RatingResponse | TextResponse

const props = defineProps<{
  survey: Survey
  sessionId: string
  participantId: string
}>()

const emit = defineEmits<{
  'survey-submitted': [payload: { submissionId: string; surveyId: string }]
  'survey-completed': []
}>()

const currentQuestionIndex = ref(0)
const responses = ref<Record<string, SurveyResponse>>({})
const rating = ref(0)
const selectedEmoji = ref<string | null>(null)
const selectedBoolean = ref<boolean | null>(null)
const selectedMultipleChoice = ref<string | null>(null)
const textResponse = ref('')
const rating10 = ref<number | null>(null)
const isSubmitting = ref(false)
const showSuccess = ref(false)

const emojiOptions = [
  { value: 'very_fun', emoji: '😄', label: 'So Fun!' },
  { value: 'fun', emoji: '🙂', label: 'Fun' },
  { value: 'okay', emoji: '😐', label: 'Okay' },
  { value: 'boring', emoji: '😕', label: 'Boring' },
  { value: 'hard', emoji: '😩', label: 'Too Hard' }
]

const multipleChoiceOptions = [
  'The first puzzle',
  'The hint system',
  'The celebration animations',
  'The progress bar',
  'The tutorial mode',
  'The visual feedback'
]

const currentQuestion = computed(() => props.survey.questions[currentQuestionIndex.value])
const totalQuestions = computed(() => props.survey.questions.length)
const progressPercentage = computed(() => ((currentQuestionIndex.value + 1) / totalQuestions.value) * 100)

const canProceed = computed(() => {
  const question = currentQuestion.value
  switch (question.responseType) {
    case 'RATING_1_5': return rating.value > 0
    case 'EMOJI_RATING': return selectedEmoji.value !== null
    case 'YES_NO': return selectedBoolean.value !== null
    case 'MULTIPLE_CHOICE': return selectedMultipleChoice.value !== null
    case 'TEXT': return true
    case 'RATING_1_10': return rating10.value !== null
    default: return true
  }
})

function selectRating(value: number): void {
  rating.value = value
  saveCurrentAnswer()
}

function selectEmoji(value: string): void {
  selectedEmoji.value = value
  saveCurrentAnswer()
}

function selectBoolean(value: boolean): void {
  selectedBoolean.value = value
  saveCurrentAnswer()
}

function selectMultipleChoice(value: string): void {
  selectedMultipleChoice.value = value
  saveCurrentAnswer()
}

function saveCurrentAnswer(): void {
  if (!currentQuestion.value) return
  const q = currentQuestion.value
  let response: SurveyResponse
  switch (q.responseType) {
    case 'RATING_1_5':
      response = { type: 'RatingResponse', value: rating.value }
      break
    case 'RATING_1_10':
      response = { type: 'RatingResponse', value: rating10.value! }
      break
    case 'EMOJI_RATING':
      response = { type: 'TextResponse', text: selectedEmoji.value }
      break
    case 'YES_NO':
      response = { type: 'TextResponse', text: selectedBoolean.value ? 'true' : 'false' }
      break
    case 'MULTIPLE_CHOICE':
      response = { type: 'TextResponse', text: selectedMultipleChoice.value }
      break
    case 'TEXT':
      response = { type: 'TextResponse', text: textResponse.value }
      break
    default:
      response = { type: 'TextResponse', text: 'Answered' }
  }
  responses.value[q.questionId] = response
}

function getPlaceholderText(): string {
  const cat = currentQuestion.value?.category
  if (cat === 'ENGAGEMENT') return 'Tell us what you enjoyed most...'
  if (cat === 'LEARNING_EFFECTIVENESS') return 'What did you learn today?'
  if (cat === 'SATISFACTION') return 'Share your thoughts about the experience...'
  return 'Type your answer here...'
}

function previousQuestion(): void {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    loadCurrentAnswer()
  }
}

function nextQuestion(): void {
  if (canProceed.value && currentQuestionIndex.value < totalQuestions.value - 1) {
    saveCurrentAnswer()
    currentQuestionIndex.value++
    clearSelection()
  }
}

function loadCurrentAnswer(): void {
  const q = currentQuestion.value
  if (!q) return
  const saved = responses.value[q.questionId]
  if (!saved) return
  switch (q.responseType) {
    case 'RATING_1_5':
      if (saved.type === 'RatingResponse') rating.value = saved.value
      break
    case 'RATING_1_10':
      if (saved.type === 'RatingResponse') rating10.value = saved.value
      break
    case 'YES_NO':
      if (saved.type === 'TextResponse') selectedBoolean.value = saved.text === 'true'
      break
    case 'EMOJI_RATING':
      if (saved.type === 'TextResponse') selectedEmoji.value = saved.text as string
      break
    case 'MULTIPLE_CHOICE':
      if (saved.type === 'TextResponse') selectedMultipleChoice.value = saved.text as string
      break
    case 'TEXT':
      if (saved.type === 'TextResponse') textResponse.value = saved.text as string
      break
  }
}

function clearSelection(): void {
  rating.value = 0
  selectedEmoji.value = null
  selectedBoolean.value = null
  selectedMultipleChoice.value = null
  textResponse.value = ''
  rating10.value = null
}

function calculateOverallRating(): number | null {
  const ratingResponses = Object.values(responses.value).filter(
    (r): r is RatingResponse => r.type === 'RatingResponse'
  )
  if (ratingResponses.length === 0) return null
  const sum = ratingResponses.reduce((acc, r) => acc + r.value, 0)
  return Math.round(sum / ratingResponses.length)
}

async function submitSurvey(): Promise<void> {
  saveCurrentAnswer()
  isSubmitting.value = true
  try {
    const surveyData = {
      surveyId: props.survey.surveyId,
      participantId: props.participantId,
      sessionId: props.sessionId,
      responses: responses.value,
      overallRating: calculateOverallRating()
    }
    const response = await fetch('/api/v1/user-testing/survey/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(surveyData)
    })
    const result = await response.json()
    if (result.success) {
      showSuccess.value = true
      emit('survey-submitted', { submissionId: result.submissionId, surveyId: props.survey.surveyId })
    } else {
      alert('Error submitting survey. Please try again.')
    }
  } catch (error) {
    console.error('Error submitting survey:', error)
    alert('Network error. Please try again.')
  } finally {
    isSubmitting.value = false
  }
}

function closeSurvey(): void {
  emit('survey-completed')
}

onMounted(() => {
  clearSelection()
})
</script>

<style scoped>
.user-testing-survey {
  max-width: 700px;
  margin: 0 auto;
  font-family: 'Inter', sans-serif;
}

.survey-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.survey-header {
  background: #4285f4;
  color: white;
  padding: 2rem;
  text-align: center;
}

.survey-header h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
}

.survey-description {
  margin: 0;
  opacity: 0.9;
  font-size: 1rem;
}

.survey-progress {
  margin-bottom: 1.5rem;
}

.step-indicator {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  opacity: 0.8;
}

.progress-bar {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  height: 6px;
  overflow: hidden;
}

.progress-fill {
  background: white;
  height: 100%;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.question-content {
  padding: 2rem;
}

.question-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 2rem;
  border: 2px solid #e2e8f0;
}

.question-card h3 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.25rem;
  line-height: 1.5;
}

/* Rating Styles */
.rating-question {
  text-align: center;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.star {
  font-size: 2rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 0.25rem;
}

.star:hover {
  transform: scale(1.1);
}

.star.filled {
  color: #fbbf24;
}

.star:not(.filled) {
  color: #d1d5db;
}

.rating-label {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Emoji Rating Styles */
.emoji-question {
  text-align: center;
}

.emoji-options {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1rem;
}

.emoji-option {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  min-width: 100px;
}

.emoji-option:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
}

.emoji-option.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.emoji {
  display: block;
  font-size: 2rem;
  margin-bottom: 0.25rem;
}

.emoji-option .label {
  display: block;
  font-size: 0.75rem;
  color: #4b5563;
}

/* Yes/No Styles */
.yesno-question {
  text-align: center;
}

.yesno-options {
  display: flex;
  gap: 2rem;
  justify-content: center;
  margin-top: 1rem;
}

.yesno-btn {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1rem 2rem;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
  font-weight: 600;
}

.yesno-btn:hover {
  transform: translateY(-2px);
}

.yesno-btn.selected {
  border-color: #10b981;
  background: #d1fae5;
}

.yes-btn.selected {
  color: #065f46;
}

.no-btn.selected {
  border-color: #ef4444;
  background: #fee2e2;
  color: #991b1b;
}

/* Multiple Choice Styles */
.multiple-choice-question {
  text-align: center;
}

.choice-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.choice-btn {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.choice-btn:hover {
  border-color: #3b82f6;
}

.choice-btn.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

/* Text Input Styles */
.text-question {
  text-align: left;
}

.text-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  font-family: 'Inter', sans-serif;
  resize: vertical;
}

.text-input:focus {
  outline: none;
  border-color: #3b82f6;
}

/* Rating 1-10 Styles */
.rating-10-question {
  text-align: center;
}

.rating-scale {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.rating-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.rating-option input[type="radio"] {
  margin-bottom: 0.25rem;
}

.rating-option label {
  font-size: 0.875rem;
  color: #4b5563;
  cursor: pointer;
}

/* Navigation Styles */
.navigation {
  padding: 0 2rem 2rem;
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.nav-btn {
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  transition: background 0.2s;
  font-weight: 600;
}

.nav-btn:hover:not(:disabled) {
  background: #4b5563;
}

.continue-btn {
  background: #3b82f6;
}

.continue-btn:hover:not(:disabled) {
  background: #2563eb;
}

.nav-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

/* Loading State */
.submitting {
  padding: 2rem;
  text-align: center;
  background: #f8fafc;
}

.spinner {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Success Message */
.success-message {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.success-content {
  background: white;
  border-radius: 16px;
  padding: 3rem;
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.success-content h3 {
  color: #1f2937;
  margin-bottom: 1rem;
}

.close-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 1rem;
}

.close-btn:hover {
  background: #059669;
}
</style>