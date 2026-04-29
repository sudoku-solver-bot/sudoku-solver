<template>
  <div class="user-testing-participation">
    <div class="participation-header">
      <h2>Join the Sudoku Adventure! 🎮</h2>
      <p>Help us make Sudoku learning fun for kids like you!</p>
    </div>

    <!-- Step 1: Child Information -->
    <div
      v-if="currentStep === 1"
      class="step-content"
    >
      <div class="info-form">
        <h3>Tell us about yourself</h3>
        
        <div class="form-group">
          <label for="childName">What's your name?</label>
          <input 
            id="childName" 
            v-model="childName" 
            type="text" 
            placeholder="Enter your name"
            class="form-input"
          >
        </div>

        <div class="form-group">
          <label for="age">How old are you?</label>
          <select 
            id="age" 
            v-model="age" 
            class="form-input"
          >
            <option value="">
              Select your age
            </option>
            <option
              v-for="ageOption in ageOptions"
              :key="ageOption"
              :value="ageOption"
            >
              {{ ageOption }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="parentEmail">Your parent's email</label>
          <input 
            id="parentEmail" 
            v-model="parentEmail" 
            type="email" 
            placeholder="parent@email.com"
            class="form-input"
          >
          <small class="form-help">We'll send progress updates to your parent</small>
        </div>

        <div
          v-if="errors.length"
          class="error-messages"
        >
          <div
            v-for="error in errors"
            :key="error"
            class="error-message"
          >
            ⚠️ {{ error }}
          </div>
        </div>

        <button 
          :disabled="!canCreateParticipant" 
          class="continue-btn"
          @click="createParticipant"
        >
          Start My Adventure! 🚀
        </button>
      </div>
    </div>

    <!-- Step 2: Feature Assignment -->
    <div
      v-if="currentStep === 2"
      class="step-content"
    >
      <div class="feature-assignment">
        <h3>Getting Your Sudoku Powers Ready ⚡</h3>
        <p>We're setting up your special features for the adventure!</p>
        
        <div
          v-if="isLoading"
          class="loading-spinner"
        >
          <div class="spinner" />
          <p>Setting up your adventure...</p>
        </div>

        <div
          v-if="!isLoading && assignmentInfo"
          class="assignment-complete"
        >
          <div class="success-icon">
            ✅
          </div>
          <h4>Adventure Ready!</h4>
          <p>You've been assigned to the <strong>{{ assignmentInfo.variant }}</strong> group.</p>
          <p>You'll have these special features:</p>
          <ul class="feature-list">
            <li
              v-for="feature in assignmentInfo.features"
              :key="feature"
            >
              ✨ {{ feature }}
            </li>
          </ul>
          
          <button
            class="continue-btn"
            @click="startFirstSession"
          >
            Start My First Puzzle! 🧩
          </button>
        </div>
      </div>
    </div>

    <!-- Step 3: First Puzzle -->
    <div
      v-if="currentStep === 3"
      class="step-content"
    >
      <div class="first-puzzle">
        <h3>Welcome to Your First Puzzle! 🌟</h3>
        <p>You're all set! Let's start with a simple puzzle to learn the basics.</p>
        
        <div class="puzzle-info">
          <div class="info-card">
            <h4>🎯 Your Mission</h4>
            <p>Fill in the empty cells so that each row, column, and 3x3 box contains all numbers from 1 to 9.</p>
          </div>
          
          <div class="info-card">
            <h4>💡 Tips</h4>
            <ul>
              <li>Look for cells where only one number can fit</li>
              <li>Use the hints if you get stuck</li>
              <li>Have fun learning!</li>
            </ul>
          </div>
        </div>

        <div class="controls">
          <button
            class="continue-btn"
            @click="continueToPuzzle"
          >
            Let's Start! 🎮
          </button>
        </div>
      </div>
    </div>

    <!-- Success Animation -->
    <div
      v-if="showSuccess"
      class="success-overlay"
    >
      <div class="success-content">
        <div class="success-emoji">
          🎉
        </div>
        <h3>Welcome to the Adventure!</h3>
        <p>Your Sudoku learning journey starts now!</p>
        <button
          class="close-btn"
          @click="closeSuccess"
        >
          Awesome! Let's Go!
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface AssignmentInfo {
  variant: string
  features: string[]
}

const emit = defineEmits<{
  'start-puzzle': [payload: { sessionId: string; participantId: string; assignmentInfo: AssignmentInfo | null }]
}>()

const currentStep = ref<number>(1)
const childName = ref<string>('')
const age = ref<string>('')
const parentEmail = ref<string>('')
const errors = ref<string[]>([])
const isLoading = ref<boolean>(false)
const assignmentInfo = ref<AssignmentInfo | null>(null)
const showSuccess = ref<boolean>(false)
const ageOptions: number[] = Array.from({ length: 7 }, (_, i) => i + 8) // 8-14 years

let participantId = ''
let sessionId = ''

const validateEmail = (email: string): boolean => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

const canCreateParticipant = computed<boolean>(() => {
  return childName.value.trim() !== '' &&
    age.value !== '' &&
    parentEmail.value.trim() !== '' &&
    validateEmail(parentEmail.value)
})

const validateForm = (): boolean => {
  errors.value = []

  if (!childName.value.trim()) {
    errors.value.push('Please enter your name')
  }

  if (!age.value) {
    errors.value.push('Please select your age')
  }

  if (!parentEmail.value.trim()) {
    errors.value.push('Please enter your parent\'s email')
  } else if (!validateEmail(parentEmail.value)) {
    errors.value.push('Please enter a valid email address')
  }

  return errors.value.length === 0
}

const getRandomVariant = (): string => {
  const variants = ['CONTROL', 'VARIANT_A', 'VARIANT_B']
  return variants[Math.floor(Math.random() * variants.length)]
}

const createParticipant = async (): Promise<void> => {
  if (!validateForm()) return

  isLoading.value = true
  errors.value = []

  try {
    const participantData = {
      participantId: `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      age: parseInt(age.value),
      parentEmail: parentEmail.value,
      childName: childName.value,
      assignedVariant: getRandomVariant()
    }

    const response = await fetch('/api/v1/user-testing/participant', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(participantData)
    })

    const result = await response.json()

    if (result.success) {
      participantId = result.participantId
      currentStep.value = 2

      // Get feature assignment
      await getFeatureAssignment(result.assignedVariant)
    } else {
      errors.value.push(result.error || 'Failed to create participant')
    }
  } catch (error) {
    errors.value.push('Network error. Please try again.')
    console.error('Error creating participant:', error)
  } finally {
    isLoading.value = false
  }
}

const getFeatureAssignment = async (variant: string): Promise<void> => {
  try {
    const response = await fetch(`/api/v1/user-testing/features/${variant}`)
    const result = await response.json()

    if (result.success) {
      assignmentInfo.value = {
        variant,
        features: Object.values(result.features) as string[]
      }
    }
  } catch (error) {
    console.error('Error getting feature assignment:', error)
    // Fallback default features
    assignmentInfo.value = {
      variant,
      features: ['Visual Feedback', 'Progress Bar', 'Hints']
    }
  }
}

const startFirstSession = async (): Promise<void> => {
  isLoading.value = true

  try {
    const sessionData = {
      sessionId: `session_${Date.now()}`,
      participantId,
      phase: 'ONBOARDING'
    }

    const response = await fetch('/api/v1/user-testing/session', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sessionData)
    })

    const result = await response.json()

    if (result.success) {
      currentStep.value = 3
      sessionId = result.sessionId
    } else {
      errors.value.push('Failed to start session')
    }
  } catch (error) {
    errors.value.push('Network error. Please try again.')
    console.error('Error starting session:', error)
  } finally {
    isLoading.value = false
  }
}

const continueToPuzzle = (): void => {
  emit('start-puzzle', {
    sessionId,
    participantId,
    assignmentInfo: assignmentInfo.value
  })
}

const closeSuccess = (): void => {
  showSuccess.value = false
}
</script>

<style scoped>
.user-testing-participation {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Inter', sans-serif;
}

.participation-header {
  text-align: center;
  margin-bottom: 2rem;
}

.participation-header h2 {
  color: #3b82f6;
  margin-bottom: 0.5rem;
  font-size: 2rem;
}

.participation-header p {
  color: #6b7280;
  font-size: 1.1rem;
}

.step-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.info-form h3,
.feature-assignment h3,
.first-puzzle h3 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.form-help {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.error-messages {
  margin-bottom: 1.5rem;
}

.error-message {
  background: #fef2f2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.continue-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  width: 100%;
}

.continue-btn:hover:not(:disabled) {
  background: #2563eb;
}

.continue-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.loading-spinner {
  text-align: center;
  padding: 2rem;
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

.assignment-complete {
  text-align: center;
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.assignment-complete h4 {
  color: #1f2937;
  margin-bottom: 1rem;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 1.5rem 0;
}

.feature-list li {
  padding: 0.5rem 0;
  color: #4b5563;
}

.puzzle-info {
  display: grid;
  gap: 1rem;
  margin-bottom: 2rem;
}

.info-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 1.5rem;
}

.info-card h4 {
  color: #1f2937;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-card ul {
  margin: 0;
  padding-left: 1.5rem;
}

.info-card li {
  margin-bottom: 0.5rem;
  color: #4b5563;
}

.success-overlay {
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

.success-emoji {
  font-size: 5rem;
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