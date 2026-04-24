<template>
  <div v-if="visible" class="number-bar">
    <div class="bar-row">
      <button
        v-for="num in [1, 2, 3, 4, 5, 6, 7, 8, 9]"
        :key="num"
        class="bar-btn"
        :class="{ complete: counts[num] >= 9 }"
        @click="$emit('input', num)"
      >
        <span class="bar-num">{{ num }}</span>
        <span v-if="counts[num] !== undefined" class="bar-count" :class="{ done: counts[num] >= 9 }">{{ 9 - counts[num] }}</span>
      </button>
      <button class="bar-btn bar-clear" @click="$emit('clear')" title="Clear">✕</button>
      <button class="bar-btn bar-hint" @click="$emit('hint')" title="Hint">💡</button>
      <button class="bar-btn" :class="{ 'pencil-active': pencilMode }" @click="$emit('toggle-pencil')" title="Pencil marks">✏️</button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    counts: {
      type: Object,
      default: () => ({})
    },
    pencilMode: {
      type: Boolean,
      default: false
    }
  })

const emit = defineEmits(['input', 'clear', 'hint', 'toggle-pencil'])
</script>

<style scoped>
.number-bar {
  margin-top: 12px;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.bar-row {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: wrap;
}

.bar-btn {
  position: relative;
  width: 36px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  -webkit-tap-highlight-color: transparent;
  padding: 0;
}

.bar-btn:active {
  transform: scale(0.93);
  background: #f0f0f0;
}

.bar-num {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.bar-count {
  font-size: 8px;
  font-weight: 700;
  color: #aaa;
  line-height: 1;
  margin-top: 1px;
}

.bar-count.done { color: #34a853; }

.bar-btn.complete {
  opacity: 0.35;
  background: #e8f5e9;
  border-color: #c8e6c9;
}

.bar-clear {
  background: #fff5f5;
  border-color: #ffcdd2;
}

.bar-hint {
  background: #fffde7;
  border-color: #ffecb3;
}

.bar-btn.pencil-active {
  background: #e3f2fd;
  border-color: #4285f4;
  box-shadow: 0 0 0 1px rgba(66,133,244,0.3);
}

@media (hover: hover) {
  .bar-btn:hover {
    border-color: #4285f4;
    background: #e8f0fe;
  }
}

/* Dark mode handled by parent via :deep or .dark on ancestor */
</style>
