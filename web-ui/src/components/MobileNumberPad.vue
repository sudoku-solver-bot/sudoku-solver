<template>
  <transition name="slide-up">
    <div v-if="visible" class="number-pad">
      <div class="pad-grid">
        <button
          v-for="num in [1, 2, 3, 4, 5, 6, 7, 8, 9]"
          :key="num"
          class="pad-btn"
          :class="{ complete: counts[num] >= 9, suggested: num === suggested }"
          @click="$emit('input', num)"
        >
          {{ num }}
          <span v-if="counts[num] !== undefined" class="pad-count" :class="{ done: counts[num] >= 9 }">{{ 9 - counts[num] }}</span>
        </button>
        <button class="pad-btn pad-clear" @click="$emit('clear')">
          ✕
        </button>
        <button class="pad-btn pad-hint" @click="$emit('hint')">
          💡
        </button>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'MobileNumberPad',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    counts: {
      type: Object,
      default: () => ({})
    },
    suggested: {
      type: Number,
      default: null
    }
  },
  emits: ['input', 'clear', 'hint']
}
</script>

<style scoped>
.number-pad {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 16px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 16px 16px 0 0;
  z-index: 100;
}

.pad-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  max-width: 400px;
  margin: 0 auto;
}

.pad-btn {
  aspect-ratio: 1;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-tap-highlight-color: transparent;
}

.pad-btn:active {
  transform: scale(0.95);
  background: #f0f0f0;
}

.pad-count {
  position: absolute;
  top: 2px; right: 4px;
  font-size: 10px;
  font-weight: 700;
  color: #999;
}
.pad-count.done { color: #34a853; }

.pad-btn.complete {
  opacity: 0.4;
  background: #e8f5e9;
  border-color: #c8e6c9;
}

.pad-btn.suggested {
  border-color: #4285f4;
  box-shadow: 0 0 0 3px rgba(66,133,244,0.3);
  animation: pulse-suggest 1.5s ease-in-out infinite;
}

@keyframes pulse-suggest {
  0%, 100% { box-shadow: 0 0 0 3px rgba(66,133,244,0.3); }
  50% { box-shadow: 0 0 0 5px rgba(66,133,244,0.15); }
}

.pad-btn { position: relative; }

@media (hover: hover) {
  .pad-btn:hover {
    border-color: #4285f4;
    background: #e8f0fe;
  }
}

.pad-clear {
  background: #ffebee;
  border-color: #ffcdd2;
  color: #c62828;
  font-size: 20px;
}

.pad-hint {
  background: #fff8e1;
  border-color: #ffecb3;
  color: #f57f17;
  font-size: 20px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

/* Mobile specific */
@media (max-width: 500px) {
  .number-pad {
    padding: 12px;
  }

  .pad-grid {
    gap: 6px;
  }

  .pad-btn {
    font-size: 20px;
    border-radius: 10px;
  }
}

/* Hide on desktop */
@media (min-width: 768px) {
  .number-pad {
    display: none;
  }
}
</style>
