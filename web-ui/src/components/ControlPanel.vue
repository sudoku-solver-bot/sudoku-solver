<template>
  <div class="control-panel kid-friendly">
    <div class="header">
      <h1>🧩 Sudoku Fun!</h1>
      <p class="subtitle">Learn & Play</p>
    </div>

    <!-- Difficulty Selection -->
    <div class="difficulty-section">
      <h3>Choose Your Level:</h3>
      <div class="difficulty-buttons">
        <button 
          v-for="level in difficultyLevels" 
          :key="level.name"
          @click="selectDifficulty(level)"
          :class="['difficulty-btn', { active: selectedDifficulty === level.name }]"
        >
          <span class="emoji">{{ level.emoji }}</span>
          <span class="name">{{ level.displayName }}</span>
          <span class="age">{{ level.targetAge }}</span>
        </button>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="actions">
      <button @click="generatePuzzle" class="btn primary">
        🎲 New Puzzle
      </button>
      <button @click="getHint" class="btn secondary" :disabled="!currentPuzzle">
        💡 Get Hint
      </button>
      <button @click="checkProgress" class="btn secondary">
        🏆 My Progress
      </button>
    </div>

    <!-- Progress Display -->
    <div v-if="progress" class="progress-display">
      <div class="level-badge">
        <span class="level-icon">⭐</span>
        <span class="level-number">Level {{ progress.level }}</span>
      </div>
      <div class="xp-bar">
        <div class="xp-fill" :style="{ width: progress.levelProgress * 100 + '%' }"></div>
        <span class="xp-text">{{ progress.experiencePoints }} XP</span>
      </div>
      <div class="stats">
        <span>🎯 {{ progress.stats.puzzlesSolved }} puzzles</span>
        <span>🔥 {{ progress.stats.currentStreak }} day streak</span>
      </div>
    </div>

    <!-- Achievement Unlocked Modal -->
    <div v-if="showAchievement" class="achievement-modal">
      <div class="achievement-content">
        <div class="achievement-icon">{{ showAchievement.icon }}</div>
        <h2>{{ showAchievement.name }}</h2>
        <p>{{ showAchievement.description }}</p>
        <button @click="showAchievement = null" class="btn">Awesome! 🎉</button>
      </div>
    </div>

    <!-- Celebration Overlay -->
    <div v-if="celebration" class="celebration-overlay">
      <div class="confetti-container">
        <!-- Confetti particles would be rendered here -->
      </div>
      <div class="celebration-message">
        <h2>{{ celebration.message }}</h2>
        <div class="emoji-burst">{{ celebration.emoji.join(' ') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const difficultyLevels = [
  { 
    name: 'EASY', 
    displayName: 'Easy', 
    emoji: '🌱',
    targetAge: 'Ages 8-9',
    description: 'Perfect for beginners!'
  },
  { 
    name: 'MEDIUM', 
    displayName: 'Medium', 
    emoji: '🌿',
    targetAge: 'Ages 10-11',
    description: 'Ready for a challenge?'
  },
  { 
    name: 'HARD', 
    displayName: 'Hard', 
    emoji: '🌳',
    targetAge: 'Ages 12-13',
    description: 'Test your skills!'
  },
  { 
    name: 'EXPERT', 
    displayName: 'Expert', 
    emoji: '🏆',
    targetAge: 'Ages 14+',
    description: 'For puzzle masters!'
  }
]

const selectedDifficulty = ref('EASY')
const currentPuzzle = ref(null)
const progress = ref(null)
const showAchievement = ref(null)
const celebration = ref(null)

const emit = defineEmits(['generate', 'hint', 'progress'])

function selectDifficulty(level) {
  selectedDifficulty.value = level.name
}

async function generatePuzzle() {
  try {
    const response = await fetch('/api/v1/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ difficulty: selectedDifficulty.value })
    })
    
    const data = await response.json()
    currentPuzzle.value = data.puzzle
    emit('generate', data)
    
    // Show encouraging message
    showEncouragement()
  } catch (error) {
    console.error('Failed to generate puzzle:', error)
  }
}

async function getHint() {
  if (!currentPuzzle.value) return
  
  try {
    const response = await fetch('/api/v1/hint', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        puzzle: currentPuzzle.value,
        targetDifficulty: selectedDifficulty.value
      })
    })
    
    const hint = await response.json()
    emit('hint', hint)
  } catch (error) {
    console.error('Failed to get hint:', error)
  }
}

async function checkProgress() {
  try {
    // This would normally use stored user stats
    const response = await fetch('/api/v1/progress', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: 'demo-user',
        puzzlesSolved: 15,
        tutorialsCompleted: 2,
        currentStreak: 3
      })
    })
    
    progress.value = await response.json()
    emit('progress', progress.value)
  } catch (error) {
    console.error('Failed to check progress:', error)
  }
}

function showEncouragement() {
  const messages = [
    "You've got this! 💪",
    "Take your time! ⏰",
    "Think carefully! 🧠",
    "Believe in yourself! ✨"
  ]
  // Could show as toast notification
}

function triggerCelebration(condition) {
  fetch('/api/v1/celebration', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(condition)
  })
  .then(res => res.json())
  .then(data => {
    celebration.value = data
    setTimeout(() => {
      celebration.value = null
    }, data.duration)
  })
}
</script>

<style scoped>
.kid-friendly {
  font-family: 'Comic Sans MS', 'Chalkboard', cursive;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-radius: 20px;
  color: white;
}

.header h1 {
  font-size: 2.5em;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.subtitle {
  font-size: 1.2em;
  opacity: 0.9;
}

.difficulty-section {
  margin: 20px 0;
}

.difficulty-section h3 {
  margin-bottom: 10px;
  font-size: 1.3em;
}

.difficulty-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.difficulty-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.difficulty-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.difficulty-btn.active {
  background: rgba(255, 255, 255, 0.4);
  border-color: #ffd700;
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
}

.difficulty-btn .emoji {
  font-size: 2em;
}

.difficulty-btn .name {
  font-size: 1.2em;
  font-weight: bold;
  margin: 5px 0;
}

.difficulty-btn .age {
  font-size: 0.9em;
  opacity: 0.8;
}

.actions {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  font-size: 1.1em;
  cursor: pointer;
  transition: all 0.3s;
  font-family: inherit;
}

.btn.primary {
  background: #ffd700;
  color: #333;
}

.btn.secondary {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.progress-display {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 15px;
  padding: 15px;
  margin-top: 20px;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.level-icon {
  font-size: 2em;
}

.level-number {
  font-size: 1.3em;
  font-weight: bold;
}

.xp-bar {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  height: 25px;
  position: relative;
  overflow: hidden;
}

.xp-fill {
  background: linear-gradient(90deg, #ffd700, #ffed4e);
  height: 100%;
  transition: width 0.5s;
}

.xp-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: bold;
  color: #333;
}

.stats {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
  font-size: 1.1em;
}

.achievement-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.achievement-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
  border-radius: 20px;
  text-align: center;
  max-width: 400px;
}

.achievement-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.celebration-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.celebration-message {
  text-align: center;
  font-size: 2em;
  animation: bounce 0.5s ease-in-out;
}

.emoji-burst {
  font-size: 2em;
  margin-top: 20px;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}
</style>
