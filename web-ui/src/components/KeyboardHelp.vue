<template>
  <div
    class="help-overlay"
    @click.self="$emit('close')"
  >
    <div
      class="help-card"
      :class="{ dark: isDark }"
    >
      <div class="help-header">
        <h3>⌨️ Keyboard Shortcuts</h3>
        <button
          class="close-btn"
          @click="$emit('close')"
        >
          ✕
        </button>
      </div>
      <div class="shortcuts">
        <div
          v-for="s in shortcuts"
          :key="s.key"
          class="shortcut-row"
        >
          <kbd>{{ s.key }}</kbd>
          <span>{{ s.desc }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

const emit = defineEmits(['close'])

const shortcuts = [
      { key: '↑ ↓ ← →', desc: 'Navigate cells' },
      { key: '1-9', desc: 'Enter number' },
      { key: 'Delete / ⌫', desc: 'Clear cell' },
      { key: 'Ctrl+Z', desc: 'Undo' },
      { key: 'Ctrl+Y', desc: 'Redo' },
      { key: '?', desc: 'Show this help' },
      { key: 'Esc', desc: 'Deselect cell' },
    ]
</script>

<style scoped>
.help-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center;
  justify-content: center; z-index: 1000;
}
.help-card {
  background: white; border-radius: 16px; padding: 24px;
  max-width: 340px; width: 90%;
}
.help-card.dark { background: #2d2d2d; color: #e0e0e0; }
.help-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.help-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #999; }
.shortcuts { display: flex; flex-direction: column; gap: 10px; }
.shortcut-row { display: flex; align-items: center; gap: 12px; }
kbd {
  background: #f5f5f5; border: 1px solid #ddd; border-radius: 6px;
  padding: 4px 10px; font-size: 13px; font-family: monospace;
  min-width: 90px; text-align: center; font-weight: 600;
}
.help-card.dark kbd { background: #3a3a3a; border-color: #555; }
</style>

