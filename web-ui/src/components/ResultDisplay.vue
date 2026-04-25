<template>
  <div
    v-if="visible"
    class="result"
    :class="[type, { visible }]"
  >
    <span
      v-if="difficulty"
      :class="['difficulty', difficultyClass]"
    >
      {{ difficulty }}
    </span>
    <span class="message">{{ message }}</span>
    <div
      v-if="techniques.length > 0"
      class="techniques"
    >
      <small>Techniques used:</small>
      <div class="technique-tags">
        <span
          v-for="t in techniques"
          :key="t"
          class="technique-tag"
        >{{ t }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  message?: string
  type?: string
  visible?: boolean
  difficulty?: string
  techniques?: string[]
}
const props = withDefaults(defineProps<Props>(), {
  message: '',
  type: 'info',
  visible: false,
  difficulty: '',
  techniques: () => []
})

const difficultyClass = computed(() => {
  return props.difficulty.toLowerCase()
})
</script>

<style scoped>
.result {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  line-height: 1.5;
}

.result.success {
  background: #e8f5e9;
  border: 1px solid #4caf50;
  color: #2e7d32;
}

.result.error {
  background: #ffebee;
  border: 1px solid #f44336;
  color: #c62828;
}

.result.info {
  background: #e3f2fd;
  border: 1px solid #2196f3;
  color: #1565c0;
}

.message {
  display: inline;
}

.techniques {
  margin-top: 8px;
  opacity: 0.8;
}

.technique-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.technique-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  background: #e8f0fe;
  color: #4285f4;
}

.difficulty {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-right: 10px;
}

.difficulty.easy {
  background: #4caf50;
  color: white;
}

.difficulty.medium {
  background: #ff9800;
  color: white;
}

.difficulty.hard {
  background: #f44336;
  color: white;
}

.difficulty.expert {
  background: #9c27b0;
  color: white;
}

.difficulty.master {
  background: #e91e63;
  color: white;
}

/* Mobile */
@media (max-width: 500px) {
  .result {
    padding: 12px;
    font-size: 14px;
  }

  .difficulty {
    font-size: 12px;
    padding: 3px 10px;
  }
}
</style>

