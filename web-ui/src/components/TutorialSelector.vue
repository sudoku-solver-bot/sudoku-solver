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

    <!-- Search bar -->
    <div class="search-bar">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="🔍 Search techniques..."
        class="search-input"
      />
      <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">✕</button>
    </div>

    <!-- Belt groups -->
    <div class="belt-groups" v-if="!searchQuery">
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
            <div class="lesson-actions">
              <button
                v-if="lesson.completed && getPracticeSet(lesson.id)"
                class="mini-btn practice-btn"
                title="Practice puzzles"
                @click.stop="$emit('practice', lesson)"
              >🎯</button>
              <div v-if="!lesson.locked" class="lesson-arrow">→</div>
            </div>
          </button>
          <!-- Quiz button for belt group -->
          <button
            v-if="group.allComplete && group.quizData"
            class="quiz-card"
            @click="$emit('quiz', group.quizData)"
          >
            <div class="lesson-icon">🧠</div>
            <div class="lesson-info">
              <div class="lesson-title">Quiz: {{ group.quizData.technique }}</div>
              <div class="lesson-desc">Test your pattern recognition!</div>
            </div>
            <div class="lesson-arrow">→</div>
          </button>
        </div>
      </div>
    </div>

    <!-- Search results -->
    <div v-if="searchQuery" class="search-results">
      <div v-if="filteredLessons.length === 0" class="no-results">
        No techniques found for "{{ searchQuery }}"
      </div>
      <button
        v-for="lesson in filteredLessons"
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
          <div class="lesson-title" v-html="highlightMatch(lesson.title)"></div>
          <div class="lesson-desc" v-html="highlightMatch(lesson.description)"></div>
        </div>
        <div class="belt-badge" :style="{ background: lesson.beltColor }">
          {{ lesson.beltEmoji }}
        </div>
        <div v-if="!lesson.locked" class="lesson-arrow">→</div>
      </button>
    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'

export default {
  name: 'TutorialSelector',
  props: {
    tutorials: { type: Array, required: true },
    completedIds: { type: Set, default: () => new Set() },
    isDark: { type: Boolean, default: false },
    quizData: { type: Array, default: () => [] },
    practiceData: { type: Array, default: () => [] }
  },
  emits: ['exit', 'select', 'quiz', 'practice'],
  setup(props, { emit }) {
    const searchQuery = ref('')
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
            completed: 0,
            total: 0,
            allComplete: false,
            quizData: null
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
        groups[t.belt].total++
        if (completed) groups[t.belt].completed++
      }

      // Check which belt groups are fully complete and attach quiz data
      for (const group of Object.values(groups)) {
        group.allComplete = group.completed === group.total
        if (group.allComplete) {
          group.quizData = props.quizData.find(q => q.belt === group.belt) || null
        }
      }

      return Object.values(groups)
    })

    const getPracticeSet = (lessonId) => {
      return props.practiceData.find(p => p.tutorialId === lessonId)
    }

    const selectLesson = (lesson) => {
      if (!lesson.locked) {
        emit('select', lesson)
      }
    }

    const allLessons = computed(() => {
      const maxCompletedOrder = Math.max(
        ...props.tutorials
          .filter(tt => props.completedIds.has(tt.id))
          .map(tt => tt.order),
        0
      )
      return props.tutorials.map(t => ({
        ...t,
        completed: props.completedIds.has(t.id),
        locked: !props.completedIds.has(t.id) && t.order > maxCompletedOrder + 1
      }))
    })

    const filteredLessons = computed(() => {
      const q = searchQuery.value.toLowerCase().trim()
      if (!q) return []
      return allLessons.value.filter(l =>
        l.title.toLowerCase().includes(q) ||
        l.description.toLowerCase().includes(q) ||
        (l.beltName && l.beltName.toLowerCase().includes(q))
      )
    })

    const highlightMatch = (text) => {
      const q = searchQuery.value.trim()
      if (!q) return text
      const regex = new RegExp(`(${q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
      return text.replace(regex, '<mark>$1</mark>')
    }

    return { searchQuery, completedCount, overallProgress, beltGroups, getPracticeSet, selectLesson, filteredLessons, highlightMatch }
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

.lesson-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: all 0.2s;
}

.tutorial-selector.dark .mini-btn {
  background: #333;
  border-color: #555;
}

.mini-btn:hover {
  transform: scale(1.1);
  border-color: #4285f4;
}

.quiz-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #e8f0fe, #f3e5f5);
  border: 2px solid #9c27b0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  width: 100%;
}

.tutorial-selector.dark .quiz-card {
  background: linear-gradient(135deg, #1a1a3c, #2a1a2a);
  border-color: #9c27b0;
}

.quiz-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.2);
}

.search-bar {
  position: relative;
  margin-bottom: 12px;
}

.search-input {
  width: 100%;
  padding: 10px 36px 10px 14px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  color: #333;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #4285f4;
}

.tutorial-selector.dark .search-input {
  background: #2d2d2d;
  border-color: #444;
  color: #ddd;
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  padding: 4px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.no-results {
  text-align: center;
  padding: 24px;
  color: #888;
  font-size: 14px;
}

.belt-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

mark {
  background: #fff3cd;
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}

.tutorial-selector.dark mark {
  background: #5c4a00;
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
