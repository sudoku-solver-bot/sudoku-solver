<template>
  <div class="buttons">
    <button class="btn-primary" @click="$emit('solve')">Solve</button>
    <button class="btn-warning" @click="$emit('clear')">Clear</button>
    <button class="btn-secondary" @click="$emit('generate', 'EASY')">Easy</button>
    <button class="btn-secondary" @click="$emit('generate', 'MEDIUM')">Medium</button>
    <button class="btn-secondary" @click="$emit('generate', 'HARD')">Hard</button>
    <button class="btn-info" @click="$emit('generate', 'EXPERT')">Expert</button>
    <button class="btn-info btn-full" @click="$emit('hint')">Get Hint</button>
  </div>
</template>

<script>
export default {
  name: 'ControlPanel',
  emits: ['solve', 'clear', 'generate', 'hint']
}
</script>

<style scoped>
.buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 10px;
}

button {
  padding: 14px 20px;
  border: none;
  border-radius: 8px;
  font-size: clamp(14px, 3.5vw, 16px);
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.1s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

button:active {
  transform: scale(0.98);
}

@media (hover: hover) {
  button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
}

.btn-primary {
  background: #4285f4;
  color: white;
}

.btn-secondary {
  background: #34a853;
  color: white;
}

.btn-warning {
  background: #fbbc05;
  color: white;
}

.btn-info {
  background: #9c27b0;
  color: white;
}

.btn-full {
  grid-column: span 2;
}

/* Mobile touch targets - minimum 44px height for iOS */
@media (max-width: 500px) {
  button {
    padding: 12px 16px;
    min-height: 44px;
  }

  .buttons {
    gap: 8px;
  }
}

/* iPhone SE */
@media (max-width: 380px) {
  button {
    padding: 10px 12px;
    font-size: 13px;
  }
}
</style>
