<template>
  <div class="number-bar" :class="{ dark: isDark }">
    <div v-for="n in 9" :key="n" class="digit-cell" :class="{ complete: counts[n] >= 9 }">
      <span class="digit-num">{{ n }}</span>
      <div class="digit-progress">
        <div class="digit-fill" :style="{ width: (counts[n] / 9 * 100) + '%' }"></div>
      </div>
      <span class="digit-remaining">{{ 9 - counts[n] }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NumberBar',
  props: {
    counts: { type: Object, default: () => ({}) },
    isDark: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.number-bar {
  display: flex;
  gap: 2px;
  padding: 8px 0;
  margin-bottom: 4px;
}
.digit-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 4px 2px;
  border-radius: 6px;
  background: #f8f9fa;
  transition: all 0.3s;
}
.number-bar.dark .digit-cell { background: #333; }
.digit-cell.complete { background: #e8f5e9; opacity: 0.5; }
.number-bar.dark .digit-cell.complete { background: #1b5e20; }
.digit-num { font-size: 14px; font-weight: 700; color: #333; }
.number-bar.dark .digit-num { color: #ddd; }
.digit-cell.complete .digit-num { color: #999; }
.digit-progress {
  width: 100%;
  height: 3px;
  background: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}
.number-bar.dark .digit-progress { background: #555; }
.digit-fill {
  height: 100%;
  background: linear-gradient(90deg, #4285f4, #34a853);
  border-radius: 2px;
  transition: width 0.3s ease;
}
.digit-cell.complete .digit-fill { background: #999; }
.digit-remaining {
  font-size: 10px;
  color: #888;
  font-weight: 600;
}
.digit-cell.complete .digit-remaining { color: #bbb; }
</style>
