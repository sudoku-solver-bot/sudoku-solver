<template>
  <transition name="slide-down">
    <div
      v-if="!online"
      class="offline-banner"
    >
      <span>📡 You're offline — puzzle still works!</span>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const online = ref(navigator.onLine)
    const onOnline = () => { online.value = true }
    const onOffline = () => { online.value = false }
    onMounted(() => {
      window.addEventListener('online', onOnline)
      window.addEventListener('offline', onOffline)
    })
    onUnmounted(() => {
      window.removeEventListener('online', onOnline)
      window.removeEventListener('offline', onOffline)
    })
</script>

<style scoped>
.offline-banner {
  position: fixed; top: 0; left: 0; right: 0;
  background: #f57c00; color: white;
  text-align: center; padding: 8px; font-size: 13px;
  font-weight: 600; z-index: 9999;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.slide-down-enter-active, .slide-down-leave-active {
  transition: transform 0.3s ease;
}
.slide-down-enter-from, .slide-down-leave-to {
  transform: translateY(-100%);
}
</style>

