<template>
  <div class="settings" :class="{ dark: isDark }">
    <div class="settings-header">
      <button class="back-btn" @click="$emit('exit')">← Back</button>
      <h2>⚙️ Settings</h2>
    </div>

    <div class="settings-list">
      <!-- Accessibility section -->
      <div class="settings-section">
        <h3>Accessibility</h3>

        <label class="setting-row">
          <div class="setting-info">
            <span class="setting-name">Color-blind friendly</span>
            <span class="setting-desc">Uses patterns + distinct colors for highlights</span>
          </div>
          <input type="checkbox" :checked="colorBlind" @change="$emit('toggle-colorblind')" class="toggle">
        </label>

        <label class="setting-row">
          <div class="setting-info">
            <span class="setting-name">High contrast</span>
            <span class="setting-desc">Stronger borders and text for visibility</span>
          </div>
          <input type="checkbox" :checked="highContrast" @change="$emit('toggle-highcontrast')" class="toggle">
        </label>

        <label class="setting-row">
          <div class="setting-info">
            <span class="setting-name">Dark mode</span>
            <span class="setting-desc">Easier on the eyes at night</span>
          </div>
          <input type="checkbox" :checked="isDark" @change="$emit('toggle-dark')" class="toggle">
        </label>

        <label class="setting-row">
          <div class="setting-info">
            <span class="setting-name">Sound effects</span>
            <span class="setting-desc">Audio feedback for actions</span>
          </div>
          <input type="checkbox" :checked="soundEnabled" @change="toggleSound" class="toggle">
        </label>
      </div>

      <!-- Data section -->
      <div class="settings-section">
        <h3>Data</h3>

        <button class="setting-row clickable" @click="resetProgress">
          <div class="setting-info">
            <span class="setting-name">Reset progress</span>
            <span class="setting-desc">Clear all saved lesson progress and streaks</span>
          </div>
          <span class="setting-action">⚠️ Reset</span>
        </button>
      </div>

      <!-- About section -->
      <div class="settings-section">
        <h3>About</h3>
        <div class="about-text">
          <p><strong>Sudoku Dojo</strong> — Learn Sudoku step by step</p>
          <p>15 techniques across 7 belt levels</p>
          <p class="version">v1.0.0 · Built with ❤️ by Nova</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { isSoundEnabled, setSoundEnabled, playSound } from '../sounds'

export default {
  name: 'Settings',
  props: {
    isDark: { type: Boolean, default: false },
    colorBlind: { type: Boolean, default: false },
    highContrast: { type: Boolean, default: false }
  },
  emits: ['exit', 'toggle-dark', 'toggle-colorblind', 'toggle-highcontrast'],
  setup(props, { emit }) {
    const soundEnabled = ref(isSoundEnabled())

    const toggleSound = () => {
      soundEnabled.value = !soundEnabled.value
      setSoundEnabled(soundEnabled.value)
      if (soundEnabled.value) playSound('click')
    }

    const resetProgress = () => {
      if (confirm('Reset all progress? This cannot be undone.')) {
        localStorage.removeItem('sudokuCompletedTutorials')
        localStorage.removeItem('sudokuDailyStreak')
        localStorage.removeItem('sudokuDailyCompleted')
        emit('exit')
      }
    }
    return { resetProgress, soundEnabled, toggleSound }
  }
}
</script>

<style scoped>
.settings {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.settings-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.settings.dark .settings-header h2 {
  color: #e0e0e0;
}

.back-btn {
  background: #f0f0f0;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.settings.dark .back-btn {
  background: #333;
  color: #ccc;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-section h3 {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #888;
  margin: 0 0 8px;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 6px;
  cursor: pointer;
}

.settings.dark .setting-row {
  background: #2a2a2a;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.setting-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.settings.dark .setting-name {
  color: #ddd;
}

.setting-desc {
  font-size: 12px;
  color: #888;
}

.toggle {
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.setting-action {
  font-size: 13px;
  color: #ea4335;
  font-weight: 600;
}

.about-text {
  padding: 12px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.settings.dark .about-text {
  color: #aaa;
}

.version {
  font-size: 11px;
  color: #aaa;
  margin-top: 8px;
}
</style>
