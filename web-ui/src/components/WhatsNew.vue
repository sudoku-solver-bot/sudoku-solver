<template>
  <div class="whats-new-overlay" @click.self="$emit('close')">
    <div class="whats-new-card" :class="{ dark: isDark }">
      <div class="whats-new-header">
        <h3>✨ What's New</h3>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="version-badge">v2.0 — April 2026</div>

      <div class="feature-list">
        <div class="feature" v-for="f in features" :key="f.icon">
          <span class="feature-icon">{{ f.icon }}</span>
          <div>
            <strong>{{ f.title }}</strong>
            <p>{{ f.desc }}</p>
          </div>
        </div>
      </div>

      <div class="stats-row">
        <span>📊 {{ totalPRs }} features shipped</span>
        <span>📚 15+ tutorials</span>
        <span>🏆 7 belt levels</span>
      </div>

      <button class="got-it-btn" @click="$emit('close')">Let's play! 🎮</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WhatsNew',
  props: { isDark: Boolean },
  emits: ['close'],
  setup() {
    const totalPRs = 136
    const features = [
      { icon: '🎨', title: 'Board Themes', desc: 'Default, Wood, Neon, Minimal — switch in Settings' },
      { icon: '🎉', title: 'Confetti Celebration', desc: 'Puzzle completion triggers a particle show!' },
      { icon: '🔊', title: 'Sound Effects', desc: 'Audio feedback for every action (toggle in Settings)' },
      { icon: '⚠️', title: 'Conflict Highlighting', desc: 'Duplicate values in red with shake animation' },
      { icon: '🌐', title: '繁體中文', desc: 'Full Traditional Chinese support — switch in Settings' },
      { icon: '💾', title: 'Save & Resume', desc: 'Auto-saves your puzzle. Resume anytime.' },
      { icon: '🔗', title: 'Share Puzzles', desc: 'Generate a link and challenge friends' },
      { icon: '📥', title: 'Import Puzzles', desc: 'Paste any Sudoku and start solving' },
      { icon: '🖨️', title: 'Print', desc: 'Clean printable grid for paper solving' },
      { icon: '⌨️', title: 'Keyboard Shortcuts', desc: 'Press ? to see all shortcuts' },
      { icon: '📱', title: 'PWA Install', desc: 'Add to home screen for app-like experience' },
    ]
    return { features, totalPRs }
  }
}
</script>

<style scoped>
.whats-new-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6); display: flex; align-items: center;
  justify-content: center; z-index: 1000;
}
.whats-new-card {
  background: white; border-radius: 20px; padding: 24px;
  max-width: 400px; width: 90%; max-height: 85vh; overflow-y: auto;
}
.whats-new-card.dark { background: #2d2d2d; color: #e0e0e0; }
.whats-new-header { display: flex; justify-content: space-between; align-items: center; }
.whats-new-header h3 { margin: 0; font-size: 20px; }
.close-btn { background: none; border: none; font-size: 20px; cursor: pointer; color: #999; }
.version-badge {
  display: inline-block; background: #e8f0fe; color: #4285f4;
  padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;
  margin: 12px 0;
}
.whats-new-card.dark .version-badge { background: #1a237e; color: #82b1ff; }
.feature-list { margin: 12px 0; }
.feature {
  display: flex; gap: 12px; padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.whats-new-card.dark .feature { border-color: #444; }
.feature-icon { font-size: 24px; flex-shrink: 0; }
.feature strong { font-size: 14px; display: block; }
.feature p { font-size: 12px; color: #888; margin: 2px 0 0; }
.whats-new-card.dark .feature p { color: #aaa; }
.stats-row {
  display: flex; justify-content: space-around; padding: 12px 0;
  font-size: 12px; color: #888; border-top: 1px solid #f0f0f0;
}
.whats-new-card.dark .stats-row { border-color: #444; color: #aaa; }
.got-it-btn {
  width: 100%; padding: 12px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #4285f4, #34a853); color: white;
  font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 8px;
}
</style>
