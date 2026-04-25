<template>
  <div
    v-if="visible"
    class="onboarding-overlay"
  >
    <div
      class="onboarding-card"
      :class="{ dark: isDark }"
    >
      <!-- Progress dots -->
      <div class="progress-dots">
        <span
          v-for="i in steps.length"
          :key="i"
          class="dot"
          :class="{ active: i - 1 === currentStep }"
          :style="i - 1 === currentStep ? { background: '#4285f4' } : {}"
        />
      </div>

      <!-- Step content -->
      <div class="step-visual">
        <div class="visual-icon">
          {{ steps[currentStep].icon }}
        </div>
      </div>

      <h3 class="step-title">
        {{ steps[currentStep].title }}
      </h3>
      <p class="step-desc">
        {{ steps[currentStep].desc }}
      </p>

      <!-- Actions -->
      <div class="step-actions">
        <button
          v-if="currentStep > 0"
          class="btn-skip"
          @click="$emit('close')"
        >
          Skip
        </button>
        <span v-else />
        <button
          class="btn-next"
          @click="nextStep"
        >
          {{ currentStep === steps.length - 1 ? "Let's go! 🎉" : 'Next →' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  isDark: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'done'])

const currentStep = ref(0)

const steps = [
  {
    icon: '🧩',
    title: 'Welcome to Sudoku Dojo!',
    desc: 'Learn, practice, and master Sudoku — from your very first puzzle to expert-level techniques.'
  },
  {
    icon: '📅',
    title: 'Daily Challenge',
    desc: 'A new puzzle every day. Build your streak by solving consecutive days and compare your time.'
  },
  {
    icon: '📚',
    title: 'Learn Step by Step',
    desc: 'Interactive tutorials teach you solving techniques from basic elimination to advanced patterns, organized by belt levels.'
  },
  {
    icon: '✏️',
    title: 'Pencil Marks',
    desc: 'Toggle pencil mode to note possible numbers in cells. Enable "Show Candidates" in settings for auto-computed pencil marks.'
  },
  {
    icon: '💡',
    title: 'Hints & Help',
    desc: 'Stuck? Use the Hint button for a suggestion. Check the Help page anytime from the menu for controls and tips.'
  }
]

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  } else {
    currentStep.value = 0
    emit('done')
  }
}

// Expose reset for parent
defineExpose({ reset: () => { currentStep.value = 0 } })
</script>

<style scoped>
.onboarding-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.onboarding-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  max-width: 360px;
  width: 100%;
  text-align: center;
  animation: slideUp 0.3s ease;
}

.onboarding-card.dark {
  background: #2d2d2d;
  color: #ddd;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.progress-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  transition: background 0.2s;
}

.onboarding-card.dark .dot {
  background: #555;
}

.step-visual {
  margin-bottom: 16px;
}

.visual-icon {
  font-size: 56px;
  line-height: 1;
}

.step-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.step-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin: 0 0 24px 0;
}

.onboarding-card.dark .step-desc {
  color: #aaa;
}

.step-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-skip {
  background: none;
  border: none;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 12px;
}

.btn-next {
  background: #4285f4;
  color: white;
  border: 1px solid #4285f4;
  border-radius: 20px;
  padding: 10px 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-next:active {
  transform: scale(0.97);
}
</style>
