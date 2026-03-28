<template>
  <transition name="toast">
    <div v-if="visible" :class="['toast', type]" @click="close">
      <div class="toast-content">
        <span class="toast-icon">{{ icon }}</span>
        <div class="toast-message">
          <span class="toast-title">{{ title }}</span>
          <span v-if="message" class="toast-text">{{ message }}</span>
        </div>
        <button class="toast-close" @click.stop="close">&times;</button>
      </div>
      <button v-if="showRetry" class="toast-retry" @click.stop="$emit('retry')">
        Try Again
      </button>
    </div>
  </transition>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ToastNotification',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
    },
    title: {
      type: String,
      default: ''
    },
    message: {
      type: String,
      default: ''
    },
    duration: {
      type: Number,
      default: 4000
    },
    showRetry: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'retry'],
  setup(props, { emit }) {
    const icon = computed(() => {
      const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
      }
      return icons[props.type] || icons.info
    })

    let timeout = null

    const startTimer = () => {
      if (timeout) clearTimeout(timeout)
      if (props.duration > 0) {
        timeout = setTimeout(() => {
          close()
        }, props.duration)
      }
    }

    const close = () => {
      if (timeout) clearTimeout(timeout)
      emit('close')
    }

    return {
      icon,
      close,
      startTimer
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.startTimer()
      }
    }
  }
}
</script>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 300px;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  padding: 16px;
  z-index: 1000;
  cursor: pointer;
}

.toast-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.toast-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.toast.success .toast-icon {
  background: #4caf50;
  color: white;
}

.toast.error .toast-icon {
  background: #f44336;
  color: white;
}

.toast.warning .toast-icon {
  background: #ff9800;
  color: white;
}

.toast.info .toast-icon {
  background: #2196f3;
  color: white;
}

.toast-message {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toast-title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.toast-text {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.2s;
}

.toast-close:hover {
  background: #f5f5f5;
  color: #333;
}

.toast-retry {
  margin-top: 12px;
  width: 100%;
  padding: 10px;
  background: #4285f4;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.toast-retry:hover {
  background: #3367d6;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* Mobile */
@media (max-width: 500px) {
  .toast {
    top: 10px;
    right: 10px;
    left: 10px;
    min-width: auto;
    max-width: none;
  }
}
</style>
