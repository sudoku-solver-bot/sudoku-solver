<template>
  <div class="help-page" :class="{ dark: isDark }">
    <div class="help-header">
      <button class="back-btn" @click="$emit('exit')">← Back</button>
      <h2>Help</h2>
    </div>

    <div class="help-content">
      <!-- Getting Started -->
      <section class="help-section">
        <h3 class="section-title">🚀 Getting Started</h3>
        <div class="help-cards">
          <div class="help-card">
            <div class="card-icon">🧩</div>
            <h4>Free Play</h4>
            <p>Generate puzzles at any difficulty level. Tap <strong>Free Play</strong> on the home screen, choose a difficulty, and start solving.</p>
          </div>
          <div class="help-card">
            <div class="card-icon">📅</div>
            <h4>Daily Challenge</h4>
            <p>A new puzzle every day! Build your streak by solving consecutive days. Same puzzle for everyone worldwide.</p>
          </div>
          <div class="help-card">
            <div class="card-icon">📚</div>
            <h4>Learn Techniques</h4>
            <p>Step-by-step tutorials teach you solving techniques from basic to advanced. Progress through belt levels like martial arts!</p>
          </div>
        </div>
      </section>

      <!-- Controls -->
      <section class="help-section">
        <h3 class="section-title">🎮 Controls</h3>
        <div class="control-list">
          <div class="control-item">
            <div class="control-key">Tap a cell</div>
            <span>Select it. Tap again to deselect.</span>
          </div>
          <div class="control-item">
            <div class="control-key">Number buttons</div>
            <span>Place a number in the selected cell.</span>
          </div>
          <div class="control-item">
            <div class="control-key">✏️ Pencil mode</div>
            <span>Toggle pencil marks to note possible candidates in a cell.</span>
          </div>
          <div class="control-item">
            <div class="control-key">✕ Clear</div>
            <span>Remove a number or pencil mark from the selected cell.</span>
          </div>
          <div class="control-item">
            <div class="control-key">💡 Hint</div>
            <span>Get a hint for the selected cell. Uses the solver to suggest a value.</span>
          </div>
          <div class="control-item">
            <div class="control-key">↩ Undo / ↪ Redo</div>
            <span>Undo or redo your last move.</span>
          </div>
        </div>
      </section>

      <!-- Highlighting -->
      <section class="help-section">
        <h3 class="section-title">👁️ Highlighting</h3>
        <div class="control-list">
          <div class="control-item">
            <div class="control-key">Tap a filled cell</div>
            <span>Highlights all other cells with the <strong>same number</strong> so you can quickly spot duplicates or gaps.</span>
          </div>
          <div class="control-item">
            <div class="control-key">Tap an empty cell</div>
            <span>Highlights the entire <strong>row, column, and 3×3 box</strong> to help you see what numbers are already placed.</span>
          </div>
        </div>
      </section>

      <!-- Pencil Marks -->
      <section class="help-section">
        <h3 class="section-title">✏️ Pencil Marks</h3>
        <p class="help-text">Pencil marks (also called candidates) are small numbers you write in cells to track which values are still possible.</p>
        <div class="tip-box">
          <strong>💡 Tip:</strong> Enable <strong>Show Candidates</strong> in settings to auto-compute pencil marks. The app calculates which numbers are possible based on the current board state.
        </div>
      </section>

      <!-- Solving Techniques Overview -->
      <section class="help-section">
        <h3 class="section-title">🥋 Techniques Overview</h3>
        <p class="help-text">Sudoku Dojo teaches techniques from beginner to expert. Here's a quick overview:</p>
        <div class="technique-list">
          <div class="technique-item" v-for="t in techniques" :key="t.name">
            <span class="belt-dot" :style="{ background: t.color }"></span>
            <div>
              <strong>{{ t.name }}</strong>
              <p>{{ t.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Tips -->
      <section class="help-section">
        <h3 class="section-title">💡 Tips & Tricks</h3>
        <div class="tips-list">
          <div class="tip-item">
            <span class="tip-num">1</span>
            <p><strong>Start with easy puzzles</strong> to build confidence. Work your way up as you learn techniques.</p>
          </div>
          <div class="tip-item">
            <span class="tip-num">2</span>
            <p><strong>Use pencil marks</strong> liberally. They help you track possibilities and spot patterns.</p>
          </div>
          <div class="tip-item">
            <span class="tip-num">3</span>
            <p><strong>Scan systematically</strong> — check each row, column, and box for missing numbers.</p>
          </div>
          <div class="tip-item">
            <span class="tip-num">4</span>
            <p><strong>Don't guess!</strong> Every number can be logically deduced. If you're stuck, use a hint or review a technique tutorial.</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isDark: { type: Boolean, default: false }
})
defineEmits(['exit'])

const techniques = [
  { name: 'Naked Single', desc: 'Only one candidate left — that must be the answer.', color: '#E0E0E0' },
  { name: 'Hidden Single', desc: 'A number can only go in one place within a row, column, or box.', color: '#FFD700' },
  { name: 'Naked Pair', desc: 'Two cells share the same two candidates — eliminate those numbers elsewhere.', color: '#FF8C00' },
  { name: 'Pointing Pair', desc: 'Candidates in a box restricted to one row/column eliminate from that row/column.', color: '#34A853' },
  { name: 'Naked Triple', desc: 'Three cells sharing three candidates — same logic as pairs but wider.', color: '#4285F4' },
  { name: 'X-Wing', desc: 'Two rows and two columns forming a rectangle pattern.', color: '#9C27B0' },
  { name: 'XY-Wing', desc: 'Three-cell chain that eliminates a candidate from a related cell.', color: '#795548' },
  { name: 'Advanced', desc: 'Swordfish, Coloring, Unique Rectangles, and more expert techniques.', color: '#333' }
]
</script>

<style scoped>
.help-page {
  padding: 20px;
  max-width: 500px;
  margin: 0 auto;
  color: #333;
  min-height: 100vh;
}

.help-page.dark {
  color: #ddd;
}

.help-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.help-header h2 {
  font-size: 20px;
  margin: 0;
}

.back-btn {
  background: none;
  border: none;
  color: #4285f4;
  font-size: 15px;
  cursor: pointer;
  padding: 4px 8px;
}

.help-page.dark .back-btn {
  color: #8ab4f8;
}

.help-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.help-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  margin: 0;
  padding-bottom: 6px;
  border-bottom: 1px solid #eee;
}

.help-page.dark .section-title {
  border-bottom-color: #444;
}

/* Getting Started Cards */
.help-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.help-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: white;
}

.help-page.dark .help-card {
  background: #2d2d2d;
  border-color: #444;
}

.card-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.help-card h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
}

.help-card p {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.help-page.dark .help-card p {
  color: #aaa;
}

/* Controls list */
.control-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f8f9fa;
}

.help-page.dark .control-item {
  background: #2a2a2a;
}

.control-key {
  font-size: 13px;
  font-weight: 600;
  color: #4285f4;
  white-space: nowrap;
  min-width: 110px;
}

.help-page.dark .control-key {
  color: #8ab4f8;
}

.control-item span {
  font-size: 13px;
  color: #555;
  line-height: 1.4;
}

.help-page.dark .control-item span {
  color: #bbb;
}

/* Help text */
.help-text {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  margin: 0;
}

.help-page.dark .help-text {
  color: #bbb;
}

/* Tip box */
.tip-box {
  padding: 12px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  color: #555;
  background: #f8f9fa;
}

.help-page.dark .tip-box {
  background: #2a2a2a;
  border-color: #444;
  color: #bbb;
}

/* Technique list */
.technique-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.technique-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f8f9fa;
}

.help-page.dark .technique-item {
  background: #2a2a2a;
}

.belt-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  border: 1px solid #ccc;
}

.technique-item strong {
  font-size: 14px;
  display: block;
  margin-bottom: 2px;
}

.technique-item p {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.help-page.dark .technique-item p {
  color: #999;
}

/* Tips */
.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 12px;
}

.tip-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #4285f4;
  color: white;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-item p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: #555;
}

.help-page.dark .tip-item p {
  color: #bbb;
}

/* Mobile */
@media (max-width: 380px) {
  .help-page {
    padding: 14px;
  }
  .control-item {
    flex-direction: column;
    gap: 4px;
  }
  .control-key {
    min-width: auto;
  }
}
</style>
