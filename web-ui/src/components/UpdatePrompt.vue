<template>
  <transition name="slide-up">
    <div v-if="needRefresh" class="update-bar" :class="{ dark: isDark }">
      <span class="update-text">🔄 New version available</span>
      <button class="update-btn" @click="updateServiceWorker">Reload</button>
      <button class="dismiss-btn" @click="close">✕</button>
    </div>
  </transition>
</template>

<script setup>
import { ref } from 'vue'
import { useRegisterSW } from 'virtual:pwa-register/vue'

const props = defineProps({
  isDark: { type: Boolean, default: false }
})

const needRefresh = ref(false)
let updateSW = () => {}

try {
  const registration = useRegisterSW({
    onRegisteredSW(_swUrl, r) {
      if (r) {
        // Check for updates every 30 minutes
        setInterval(() => r.update(), 30 * 60 * 1000)
      }
    },
    onNeedRefresh() {
      needRefresh.value = true
    }
  })
  updateSW = registration.updateSW
} catch (_e) {
  // PWA not available (dev mode)
}

const close = () => {
  needRefresh.value = false
}
</script>

<style scoped>
.update-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 10px 16px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 300;
  border-top: 1px solid #e0e0e0;
}

.update-bar.dark {
  background: #2d2d2d;
  border-top-color: #444;
}

.update-text {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.update-bar.dark .update-text {
  color: #ddd;
}

.update-btn {
  background: #4285f4;
  color: white;
  border: 1px solid #4285f4;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.update-btn:active {
  transform: scale(0.97);
}

.dismiss-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}
</style>
