<template>
  <transition name="slide-up">
    <div v-if="visible" class="install-banner" :class="{ dark: isDark }">
      <span class="install-icon">📱</span>
      <div class="install-text">
        <strong>Add to Home Screen</strong>
        <span>Install Sudoku Dojo for quick access</span>
      </div>
      <button class="install-btn" @click="install">Install</button>
      <button class="dismiss-btn" @click="dismiss">✕</button>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const DISMISS_KEY = 'sudoku-install-dismissed'

const visible = ref(false)
    let deferredPrompt = null

    onMounted(() => {
      if (localStorage.getItem(DISMISS_KEY)) return

      window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault()
        deferredPrompt = e
        // Show after 30 seconds
        setTimeout(() => { visible.value = true }, 30000)
      })
    })

    const install = async () => {
      if (deferredPrompt) {
        deferredPrompt.prompt()
        await deferredPrompt.userChoice
        deferredPrompt = null
      }
      visible.value = false
    }

    const dismiss = () => {
      visible.value = false
      localStorage.setItem(DISMISS_KEY, 'true')
    }
</script>

<style scoped>
.install-banner {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background: white;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.15);
  z-index: 500;
}
.install-banner.dark { background: #333; color: #eee; }
.install-icon { font-size: 24px; }
.install-text { flex: 1; display: flex; flex-direction: column; }
.install-text strong { font-size: 14px; }
.install-text span { font-size: 12px; color: #888; }
.install-banner.dark .install-text span { color: #aaa; }
.install-btn {
  background: #4285f4; color: white; border: none; padding: 8px 16px;
  border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer;
}
.dismiss-btn { background: none; border: none; font-size: 16px; color: #999; cursor: pointer; }

.slide-up-enter-active, .slide-up-leave-active { transition: transform 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100%); }
</style>

