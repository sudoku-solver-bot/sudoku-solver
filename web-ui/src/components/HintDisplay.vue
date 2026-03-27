<template>
  <div class="hint-display" v-if="hint">
    <div class="hint-card">
      <div class="technique-badge">{{ hint.technique }}</div>
      
      <h3>💡 Here's a tip:</h3>
      
      <p class="explanation">{{ hint.explanation }}</p>
      
      <div v-if="hint.highlight" class="highlight-section">
        <p>Look at this cell: Row {{ hint.cell.row + 1 }}, Column {{ hint.cell.col + 1 }}</p>
        <p class="value-hint">Try the number: <strong>{{ hint.value }}</strong></p>
      </div>
      
      <div class="teaching-points">
        <h4>📚 Remember:</h4>
        <ul>
          <li v-for="(point, index) in hint.teachingPoints" :key="index">
            {{ point }}
          </li>
        </ul>
      </div>
      
      <div class="confidence-meter">
        <span>Confidence: </span>
        <div class="meter">
          <div class="meter-fill" :style="{ width: (hint.confidence * 100) + '%' }"></div>
        </div>
        <span>{{ Math.round(hint.confidence * 100) }}%</span>
      </div>
      
      <button @click="$emit('close')" class="btn">Got it! 👍</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  hint: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])
</script>

<style scoped>
.hint-display {
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
  padding: 20px;
}

.hint-card {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  border-radius: 25px;
  padding: 30px;
  max-width: 500px;
  width: 100%;
  font-family: 'Comic Sans MS', 'Chalkboard', cursive;
  color: #333;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.technique-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.5);
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  margin-bottom: 15px;
  font-size: 0.9em;
}

.hint-card h3 {
  font-size: 1.5em;
  margin: 15px 0;
}

.explanation {
  font-size: 1.2em;
  line-height: 1.6;
  margin: 15px 0;
  background: rgba(255, 255, 255, 0.3);
  padding: 15px;
  border-radius: 15px;
}

.highlight-section {
  background: rgba(255, 255, 255, 0.4);
  padding: 15px;
  border-radius: 15px;
  margin: 15px 0;
}

.value-hint strong {
  color: #667eea;
  font-size: 1.3em;
}

.teaching-points {
  margin: 20px 0;
}

.teaching-points h4 {
  margin-bottom: 10px;
}

.teaching-points ul {
  list-style: none;
  padding: 0;
}

.teaching-points li {
  padding: 8px 0;
  padding-left: 25px;
  position: relative;
}

.teaching-points li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #4caf50;
  font-weight: bold;
}

.confidence-meter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 15px 0;
}

.meter {
  flex: 1;
  height: 10px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 5px;
  overflow: hidden;
}

.meter-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50, #8bc34a);
  transition: width 0.3s;
}

.btn {
  width: 100%;
  padding: 15px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 1.2em;
  cursor: pointer;
  font-family: inherit;
  margin-top: 15px;
  transition: all 0.3s;
}

.btn:hover {
  background: #5568d3;
  transform: translateY(-2px);
}
</style>
