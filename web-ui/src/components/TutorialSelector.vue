<template>
  <div class="tutorial-selector" :class="{ dark: isDark }">
    <div class="selector-header">
      <button class="back-btn" @click="$emit('exit')">← Back</button>
      <h2>📚 Sudoku Dojo</h2>
      <div class="total-progress">
        {{ completedCount }}/{{ tutorials.length }}
      </div>
    </div>

    <!-- Overall progress bar -->
    <div class="overall-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: overallProgress + '%' }"></div>
      </div>
    </div>

    <!-- Belt groups -->
    <div class="belt-groups">
      <div
        v-for="group in beltGroups"
        :key="group.belt"
        class="belt-group"
      >
        <div class="belt-group-header" :style="{ borderLeftColor: group.color }">
          <span class="belt-emoji">{{ group.emoji }}</span>
          <span class="belt-name">{{ group.name }}</span>
          <span class="belt-count">{{ group.completed }}/{{ group.lessons.length }}</span>
        </div>
        <div class="belt-lessons">
          <button
            v-for="lesson in group.lessons"
            :key="lesson.id"
            class="lesson-card"
            :class="{ completed: lesson.completed, locked: lesson.locked }"
            @click="selectLesson(lesson)"
          >
            <div class="lesson-icon">
              <span v-if="lesson.completed">✅</span>
              <span v-else-if="lesson.locked">🔒</span>
              <span v-else>{{ lesson.beltEmoji }}</span>
            </div>
            <div class="lesson-info">
              <div class="lesson-title">{{ lesson.title }}</div>
              <div class="lesson-desc">{{ lesson.description }}</div>
            </div>
            <div v-if="!lesson.locked" class="lesson-arrow">→</div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'TutorialSelector',
  props: {
    tutorials: { type: Array, required: true },
    completedIds: { type: Set, default: () => new Set() },
    isDark: { type: Boolean, default: false }
  },
  emits: ['exit', 'select'],
  setup(props, { emit }) {
    const completedCount = computed(() => props.completedIds.size)

    const overallProgress = computed(() => {
      if (props.tutorials.length === 0) return 0
      return (completedCount.value / props.tutorials.length) * 100
    })

    const beltGroups = computed(() => {
      const groups = {}
      for (const t of props.tutorials) {
        if (!groups[t.belt]) {
          groups[t.belt] = {
            belt: t.belt,
            name: t.beltName,
            emoji: t.beltEmoji,
            color: t.beltColor,
            lessons: [],
            completed: 0
          }
        }
        const completed = props.completedIds.has(t.id)
        // Lock lessons that are 3+ orders ahead of the highest completed
        const maxCompletedOrder = Math.max(
          ...props.tutorials
            .filter(tt => props.completedIds.has(tt.id))
            .map(tt => tt.order),
          0
        )
        const locked = !completed && t.order > maxCompletedOrder + 1

        groups[t.belt].lessons.push({ ...t, completed, locked })
        if (completed) groups[t.belt].completed++
      }
      return Object.values(groups)
    })

    const selectLesson = (lesson) => {
      if (!lesson.locked) {
        emit('select', lesson)
      }
    }

    return { completedCount, overallProgress, beltGroups, selectLesson }
  }
}
</script>

<style scoped>
.tutorial-selector {
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.selector-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.selector-header h2 {
  flex: 1;
  font-size: 20px;
  color: #333;
  margin: 0;
}

.tutorial-selector.dark .selector-header h2 {
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

.tutorial-selector.dark .back-btn {
  background: #333;
  color: #ccc;
}

.total-progress {
  font-size: 14px;
  font-weight: 600;
  color: #4285f4;
  white-space: nowrap;
}

.overall-progress {
  margin-bottom: 16px;
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.tutorial-selector.dark .progress-bar {
  background: #444;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4285f4, #34a853, #fbbc05);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.belt-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.belt-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-left: 4px solid;
  background: #f8f9fa;
  border-radius: 0 8px 8px 0;
  font-weight: 600;
  font-size: 14px;
}

.tutorial-selector.dark .belt-group-header {
  background: #2a2a2a;
}

.belt-emoji {
  font-size: 18px;
}

.belt-name {
  flex: 1;
  color: #333;
}

.tutorial-selector.dark .belt-name {
  color: #ddd;
}

.belt-count {
  font-size: 12px;
  color: #888;
}

.belt-lessons {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-left: 16px;
  margin-top: 6px;
}

.lesson-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  width: 100%;
}

.tutorial-selector.dark .lesson-card {
  background: #2d2d2d;
  border-color: #444;
}

.lesson-card:hover:not(.locked) {
  border-color: #4285f4;
  box-shadow: 0 2px 8px rgba(66, 133, 244, 0.15);
  transform: translateY(-1px);
}

.lesson-card.completed {
  border-color: #34a853;
  background: #f0fdf0;
}

.tutorial-selector.dark .lesson-card.completed {
  background: #1a2e1a;
  border-color: #34a853;
}

.lesson-card.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.lesson-icon {
  font-size: 20px;
  flex-shrink: 0;
  width: 28px;
  text-align: center;
}

.lesson-info {
  flex: 1;
  min-width: 0;
}

.lesson-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.tutorial-selector.dark .lesson-title {
  color: #ddd;
}

.lesson-desc {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tutorial-selector.dark .lesson-desc {
  color: #999;
}

.lesson-arrow {
  color: #4285f4;
  font-size: 16px;
  font-weight: bold;
}

.lesson-card.completed .lesson-arrow {
  color: #34a853;
}

@media (max-width: 500px) {
  .selector-header h2 {
    font-size: 17px;
  }

  .lesson-card {
    padding: 8px 10px;
  }

  .lesson-title {
    font-size: 13px;
  }

  .lesson-desc {
    font-size: 11px;
  }
}
</style>
