<template>
  <div
    class="app"
    :class="{ dark: isDark }"
    @click="handleAppClick"
  >
    <div
      class="container"
      :class="{ loading }"
    >
      <!-- Header -->
      <div class="header">
        <h1>🧩 Sudoku Solver</h1>
        <div class="header-actions">
          <!-- Primary actions always visible -->
          <button
            v-if="playMode"
            class="header-btn home-btn"
            title="Home"
            @click="playMode = false"
          >
            🏠
          </button>
          <button
            class="header-btn daily-btn"
            :title="dailyMode ? 'Exit Daily' : 'Daily Challenge'"
            @click="dailyMode = !dailyMode"
          >
            📅
          </button>
          <button
            class="header-btn dark-toggle"
            :title="isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'"
            @click="toggleDarkMode"
          >
            {{ isDark ? '☀️' : '🌙' }}
          </button>

          <!-- More menu -->
          <div class="header-more">
            <button
              class="header-btn more-btn"
              title="More"
              @click.stop="moreMenuOpen = !moreMenuOpen"
            >
              ⋯
            </button>
            <div
              v-if="moreMenuOpen"
              class="more-dropdown"
              :class="{ dark: isDark }"
              @click.stop
            >
              <button
                class="menu-item"
                @click="toggleTutorialMode(); moreMenuOpen = false"
              >
                <span class="menu-icon">📚</span>
                <span class="menu-label">Learn Techniques</span>
              </button>
              <button
                class="menu-item"
                @click="leaderboardOpen = !leaderboardOpen; moreMenuOpen = false"
              >
                <span class="menu-icon">🏆</span>
                <span class="menu-label">Leaderboard</span>
              </button>
              <button
                class="menu-item"
                @click="savesOpen = !savesOpen; moreMenuOpen = false"
              >
                <span class="menu-icon">💾</span>
                <span class="menu-label">Saved Puzzles</span>
              </button>
              <button
                class="menu-item"
                @click="openAchievements(); moreMenuOpen = false"
              >
                <span class="menu-icon">🏅</span>
                <span class="menu-label">Achievements</span>
              </button>
              <button
                class="menu-item"
                @click="openStats(); moreMenuOpen = false"
              >
                <span class="menu-icon">📊</span>
                <span class="menu-label">Statistics</span>
              </button>
              <button
                class="menu-item"
                @click="settingsOpen = !settingsOpen; moreMenuOpen = false"
              >
                <span class="menu-icon">⚙️</span>
                <span class="menu-label">Settings</span>
              </button>
              <button
                class="menu-item"
                @click="helpOpen = true; moreMenuOpen = false"
              >
                <span class="menu-icon">❓</span>
                <span class="menu-label">Help</span>
              </button>
              <button
                class="menu-item"
                @click="aboutOpen = true; moreMenuOpen = false"
              >
                <span class="menu-icon">ℹ️</span>
                <span class="menu-label">About</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Dashboard (home) -->
      <Dashboard
        v-if="!tutorialMode && !tutorialSelectorOpen && !dailyMode && !playMode && !settingsOpen && !aboutOpen && !helpOpen && !quizMode && !practiceMode && !leaderboardOpen && !achievementsOpen && !statsOpen && !savesOpen"
        :completed-tutorials="completedTutorials"
        :total-tutorials="tutorialList.length || 15"
        :is-dark="isDark"
        @daily="dailyMode = true"
        @learn="toggleTutorialMode"
        @play="playMode = true"
      />

      <!-- Settings -->
      <Settings
        v-if="settingsOpen && !tutorialMode && !dailyMode"
        :is-dark="isDark"
        :color-blind="colorBlindMode"
        :high-contrast="highContrastMode"
        :theme="boardTheme"
        :challenge-mode="challengeMode"
        @exit="settingsOpen = false"
        @toggle-dark="toggleDarkMode"
        @toggle-colorblind="toggleColorBlind"
        @toggle-highcontrast="toggleHighContrast"
        @toggle-challenge="toggleChallenge"
        @change-theme="boardTheme = $event"
      />

      <!-- About -->
      <AboutPage
        v-if="aboutOpen && !tutorialMode && !dailyMode"
        :is-dark="isDark"
        @exit="aboutOpen = false"
      />

      <!-- Help Page -->
      <HelpPage
        v-if="helpOpen && !tutorialMode && !dailyMode"
        :is-dark="isDark"
        @exit="helpOpen = false"
      />

      <!-- Leaderboard -->
      <Leaderboard
        v-if="leaderboardOpen && !tutorialMode && !dailyMode && !quizMode && !practiceMode && !achievementsOpen && !statsOpen && !savesOpen && !whatsNewOpen"
        :is-dark="isDark"
        @back="leaderboardOpen = false"
      />

      <!-- What's New -->
      <WhatsNew
        v-if="whatsNewOpen"
        :is-dark="isDark"
        @close="whatsNewOpen = false"
      />

      <!-- Saved Puzzles -->
      <SavedPuzzles
        v-if="savesOpen && !tutorialMode && !dailyMode && !quizMode && !practiceMode && !achievementsOpen && !statsOpen && !leaderboardOpen"
        :is-dark="isDark"
        :current-puzzle="puzzle"
        :current-difficulty="puzzleDifficulty"
        @close="savesOpen = false"
        @load="onLoadSave"
      />

      <!-- Achievements -->
      <Achievements
        v-if="achievementsOpen && !tutorialMode && !dailyMode && !quizMode && !practiceMode && !leaderboardOpen && !statsOpen"
        :is-dark="isDark"
        :stats="achievementStats"
        @back="achievementsOpen = false"
      />

      <!-- Stats -->
      <StatsPage
        v-if="statsOpen && !tutorialMode && !dailyMode && !quizMode && !practiceMode && !leaderboardOpen && !achievementsOpen"
        :is-dark="isDark"
        @back="statsOpen = false"
        @reset-stats="statsOpen = false"
      />

      <!-- Daily Challenge -->
      <DailyChallenge
        v-if="dailyMode && !tutorialMode && !tutorialSelectorOpen && !quizMode && !practiceMode"
        :is-dark="isDark"
        @exit="dailyMode = false"
      />

      <!-- Tutorial Selector -->
      <TutorialSelector
        v-if="tutorialSelectorOpen && !tutorialMode && !quizMode && !practiceMode"
        :tutorials="tutorialList"
        :completed-ids="completedTutorials"
        :is-dark="isDark"
        :quiz-data="quizList"
        :practice-data="practiceList"
        @exit="tutorialSelectorOpen = false"
        @select="onTutorialSelected"
        @quiz="onQuizSelected"
        @practice="onPracticeSelected"
      />

      <!-- Tutorial Mode -->
      <TutorialMode
        v-if="tutorialMode && currentTutorialLesson"
        :lesson="currentTutorialLesson"
        :is-dark="isDark"
        @exit="exitTutorialMode"
        @completed="onTutorialCompleted"
      />

      <!-- Quiz Mode -->
      <QuizMode
        v-if="quizMode && currentQuiz"
        :quiz="currentQuiz"
        :is-dark="isDark"
        @exit="exitQuizMode"
        @completed="onQuizCompleted"
      />

      <!-- Practice Mode -->
      <PracticeMode
        v-if="practiceMode && currentPracticeSet"
        :practice-set="currentPracticeSet"
        :is-dark="isDark"
        @exit="exitPracticeMode"
        @completed="onPracticeCompleted"
      />

      <!-- Normal mode (hidden in tutorial/daily/dashboard) -->
      <template v-if="playMode && !tutorialMode && !tutorialSelectorOpen && !dailyMode">
        <!-- Toast notification -->
        <ToastNotification
          :visible="toast.visible"
          :type="toast.type"
          :title="toast.title"
          :message="toast.message"
          :show-retry="toast.showRetry"
          @close="hideToast"
          @retry="toast.onRetry"
        />

        <!-- Progress indicator -->
        <ProgressIndicator
          :puzzle="puzzle"
          :given-cells="givenCells"
          :mistakes="mistakes"
          :hints-used="hintsUsed"
          :elapsed-time="elapsedTime"
          :difficulty="puzzleDifficulty"
        />

        <!-- Result display -->
        <ResultDisplay
          :message="resultMessage"
          :type="resultType"
          :visible="resultVisible"
          :difficulty="resultDifficulty"
          :techniques="resultTechniques"
        />

        <!-- Loading overlay -->
        <transition name="fade">
          <div
            v-if="loading"
            class="loading-overlay"
          >
            <div class="spinner" />
            <p class="loading-text">
              {{ loadingMessage }}
            </p>
          </div>
        </transition>

        <!-- Sudoku grid -->
        <SudokuGrid
          :puzzle="puzzle"
          :given-cells="givenCells"
          :solved-cells="solvedCells"
          :selected-cell="selectedCell"
          :is-dark="isDark"
          :candidates="candidates"
          :show-candidates="showCandidates"
          :color-blind="colorBlindMode"
          :high-contrast="highContrastMode"
          :challenge-mode="challengeMode"

          :theme="boardTheme"
          @update="onCellUpdate"
          @select="selectCell"
          @navigate="navigateToCell"
          @undo="undo"
          @redo="redo"
        />

        <!-- Mobile number pad (right below grid) -->
        <MobileNumberPad
          :visible="showMobilePad"
          :counts="digitCounts"
          :pencil-mode="pencilMode"

          @input="onNumberPadInput"
          @clear="clearSelectedCell"
          @hint="getHint"
          @toggle-pencil="pencilMode = !pencilMode"
        />

        <!-- Control panel -->
        <ControlPanel
          :loading="loading"
          :can-undo="canUndo"
          :can-redo="canRedo"
          :undo-count="undoCount"
          :redo-count="redoCount"
          :show-candidates="showCandidates"
          @solve="solve"
          @clear="clearGrid"
          @generate="generate"
          @import="importModalOpen = true"
          @share="sharePuzzle"
          @print="handlePrint"
          @share-image="handleShareImage"
          @hint="getHint"
          @undo="undo"
          @redo="redo"
          @toggle-candidates="showCandidates = !showCandidates"
        />

        <!-- Hint modal -->
        <HintModal
          :visible="hintModalVisible"
          :hint="currentHint"
          :total-hints="hintsUsed"
          @close="closeHintModal"
        />

        <!-- Import puzzle modal -->
        <ImportPuzzle
          v-if="importModalOpen"
          :is-dark="isDark"
          @close="importModalOpen = false"
          @import="onImportPuzzle"
        />

        <!-- Keyboard help -->
        <KeyboardHelp
          v-if="keyboardHelpOpen"
          :is-dark="isDark"
          @close="keyboardHelpOpen = false"
        />

        <!-- Confetti celebration -->
        <ConfettiCelebration
          :visible="confettiVisible"
          :time="formatTime(elapsedTime)"
          :mistakes="mistakes"
          :hints="hintsUsed"
          @done="confettiVisible = false"
        />
      </template>

      <!-- PWA install prompt -->
      <InstallPrompt :is-dark="isDark" />
      <OfflineIndicator />

      <!-- First-time onboarding -->
      <OnboardingTour
        :visible="onboardingOpen"
        :is-dark="isDark"
        @close="onboardingOpen = false"
        @done="onboardingOpen = false; localStorage.setItem('sudoku-seen-onboarding', 'true')"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch, computed } from 'vue'
import SudokuGrid from './components/SudokuGrid.vue'
import ControlPanel from './components/ControlPanel.vue'
import ResultDisplay from './components/ResultDisplay.vue'
import ProgressIndicator from './components/ProgressIndicator.vue'
import ToastNotification from './components/ToastNotification.vue'
import MobileNumberPad from './components/MobileNumberPad.vue'
import HintModal from './components/HintModal.vue'
import TutorialMode from './components/TutorialMode.vue'
import TutorialSelector from './components/TutorialSelector.vue'
import QuizMode from './components/QuizMode.vue'
import PracticeMode from './components/PracticeMode.vue'
import DailyChallenge from './components/DailyChallenge.vue'
import Dashboard from './components/Dashboard.vue'
import ImportPuzzle from './components/ImportPuzzle.vue'
import Leaderboard from './components/Leaderboard.vue'
import Achievements from './components/Achievements.vue'
import StatsPage from './components/StatsPage.vue'
import { getStatsForAchievements } from './stats-tracker'
import { playSound } from './sounds'
import { updateFavicon } from './favicon'
import { printPuzzle } from './print'
import { generatePuzzleImage, downloadImage } from './share-image'
import ConfettiCelebration from './components/ConfettiCelebration.vue'
import SavedPuzzles from './components/SavedPuzzles.vue'
import InstallPrompt from './components/InstallPrompt.vue'

import WhatsNew from './components/WhatsNew.vue'
import OfflineIndicator from './components/OfflineIndicator.vue'
import KeyboardHelp from './components/KeyboardHelp.vue'
import Settings from './components/Settings.vue'
import AboutPage from './components/AboutPage.vue'
import HelpPage from './components/HelpPage.vue'
import OnboardingTour from './components/OnboardingTour.vue'
import {
  solvePuzzle,
  generatePuzzle,
  getHintForPuzzle,
  saveState,
  undo,
  redo,
  getHistory,
  fetchCandidates,
  fetchTutorials,
  fetchTutorial,
  fetchQuizzes,
  fetchAllPracticeSets
} from './api'

export default {
  name: 'App',
  components: {
    SudokuGrid,
    ControlPanel,
    ResultDisplay,
    ProgressIndicator,
    ToastNotification,
    MobileNumberPad,
    HintModal,
    TutorialMode,
    TutorialSelector,
    QuizMode,
    PracticeMode,
    DailyChallenge,
    ImportPuzzle,
    Leaderboard,
    Dashboard,
    Achievements,
    StatsPage,
    ConfettiCelebration,
    SavedPuzzles,
    InstallPrompt,

    WhatsNew,
    OfflineIndicator,
    KeyboardHelp,
    Settings,
    AboutPage,
    HelpPage,
    OnboardingTour
  },
  setup() {
    // Puzzle state
    const puzzle = ref('.'.repeat(81))
    const givenCells = ref(new Set())
    const solvedCells = ref(new Set())

    // Digit counts for numpad
    const digitCounts = computed(() => {
      const counts = {1:0, 2:0, 3:0, 4:0, 5:0, 6:0, 7:0, 8:0, 9:0}
      for (const c of puzzle.value) {
        if (c >= '1' && c <= '9') counts[c]++
      }
      return counts
    })

    // UI state
    const loading = ref(false)
    const loadingMessage = ref('')
    const selectedCell = ref(-1)
    const isDark = ref(false)
    const boardTheme = ref(localStorage.getItem('sudoku-theme') || 'default')
    const showMobilePad = ref(false)
    const isMobile = ref(false)

    // Timer state
    const elapsedTime = ref(0)
    let timerInterval = null

    // Progress tracking
    const mistakes = ref(0)
    const hintsUsed = ref(0)
    const puzzleDifficulty = ref('')
    const puzzleSolution = ref('')

    // Undo/Redo state
    const canUndo = ref(false)
    const canRedo = ref(false)
    const undoCount = ref(0)
    const redoCount = ref(0)
    let lastSavedState = ''

    // Result display state
    const resultMessage = ref('')
    const resultType = ref('info')
    const resultVisible = ref(false)
    const resultDifficulty = ref('')
    const resultTechniques = ref([])

    // Toast notification state
    const toast = reactive({
      visible: false,
      type: 'info',
      title: '',
      message: '',
      showRetry: false,
      onRetry: null
    })

    // Hint modal state
    const hintModalVisible = ref(false)
    const currentHint = ref(null)

    // Candidates (pencil marks) state
    const candidates = ref({})
    const showCandidates = ref(true)
    const pencilMode = ref(false)

    // Tutorial state
    const tutorialMode = ref(false)
    const tutorialList = ref([])
    const currentTutorialLesson = ref(null)
    const tutorialSelectorOpen = ref(false)
    const dailyMode = ref(false)
    const playMode = ref(false)
    const settingsOpen = ref(false)
    const aboutOpen = ref(false)
    const helpOpen = ref(false)
    const onboardingOpen = ref(false)
    const hasSeenOnboarding = ref(localStorage.getItem('sudoku-seen-onboarding') === 'true')
    const moreMenuOpen = ref(false)
    const colorBlindMode = ref(false)
    const highContrastMode = ref(false)
    const challengeMode = ref(localStorage.getItem('sudoku-challenge') === 'true')
    const completedTutorials = ref(new Set())

    // Quiz & Practice state
    const quizMode = ref(false)
    const currentQuiz = ref(null)
    const quizList = ref([])
    const practiceMode = ref(false)
    const currentPracticeSet = ref(null)
    const practiceList = ref([])
    const leaderboardOpen = ref(false)
    const whatsNewOpen = ref(false)
    const savesOpen = ref(false)
    const confettiVisible = ref(false)
    const importModalOpen = ref(false)
    const keyboardHelpOpen = ref(false)
    const achievementsOpen = ref(false)
    const statsOpen = ref(false)
    const achievementStats = ref({})

    // Load completed tutorials from localStorage
    try {
      const saved = localStorage.getItem('sudokuCompletedTutorials')
      if (saved) completedTutorials.value = new Set(JSON.parse(saved))
    } catch (e) {}
    try {
      colorBlindMode.value = localStorage.getItem('sudokuColorBlind') === 'true'
      highContrastMode.value = localStorage.getItem('sudokuHighContrast') === 'true'
    } catch (e) {}

    // Initialize dark mode from localStorage or system preference
    const handleKeyDown = (e) => {
      // Don't capture when typing in inputs
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return

      const cell = selectedCell.value

      // Arrow key navigation
      if (e.key === 'ArrowUp' && cell >= 9) { e.preventDefault(); navigateToCell(cell - 9); return }
      if (e.key === 'ArrowDown' && cell < 72) { e.preventDefault(); navigateToCell(cell + 9); return }
      if (e.key === 'ArrowLeft' && cell % 9 > 0) { e.preventDefault(); navigateToCell(cell - 1); return }
      if (e.key === 'ArrowRight' && cell % 9 < 8) { e.preventDefault(); navigateToCell(cell + 1); return }

      // Number keys 1-9 to enter values
      if (/^[1-9]$/.test(e.key) && cell >= 0 && !givenCells.value.has(cell)) {
        e.preventDefault()
        onCellUpdate(cell, e.key)
        return
      }

      // Delete/Backspace to clear cell
      if ((e.key === 'Delete' || e.key === 'Backspace') && cell >= 0 && !givenCells.value.has(cell)) {
        e.preventDefault()
        onCellUpdate(cell, '')
        return
      }

      // Escape to deselect
      if (e.key === 'Escape') {
        selectedCell.value = -1
        keyboardHelpOpen.value = false
        return
      }

      // ? for keyboard help
      if (e.key === '?' || (e.key === '/' && e.shiftKey)) {
        keyboardHelpOpen.value = !keyboardHelpOpen.value
        return
      }

      // Ctrl+Z / Cmd+Z for undo
      if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
        e.preventDefault()
        undo()
        return
      }

      // Ctrl+Y / Cmd+Shift+Z for redo
      if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
        e.preventDefault()
        redo()
        return
      }

      // H for hint
      if (e.key === 'h' || e.key === 'H') {
        e.preventDefault()
        getHint()
        return
      }
    }

    onMounted(() => {
      const savedDarkMode = localStorage.getItem('sudokuDarkMode')
      if (savedDarkMode !== null) {
        isDark.value = savedDarkMode === 'true'
      } else {
        isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
      }

      // Check if mobile device
      checkMobile()

      // Show What's New on version change (not for brand new users who get onboarding)
      const seenVersion = localStorage.getItem('sudoku-version')
      const isNewUser = !seenVersion && !localStorage.getItem('sudoku-seen-onboarding')
      if (seenVersion && seenVersion !== '2.0') {
        whatsNewOpen.value = true
        localStorage.setItem('sudoku-version', '2.0')
      } else if (!seenVersion) {
        // First visit ever — set version and show onboarding
        localStorage.setItem('sudoku-version', '2.0')
        if (!hasSeenOnboarding.value && !navigator.webdriver) {
          onboardingOpen.value = true
        }
      }
      window.addEventListener('resize', checkMobile)

      // Start timer on first puzzle load
      startTimer()

      // Load initial undo/redo state
      loadHistoryState()

      // Check for shared puzzle in URL (must be before savedGame check)
      const params = new URLSearchParams(window.location.search)
      const sharedPuzzle = params.get('p')

      // Restore saved game state
      const savedGame = localStorage.getItem('sudoku-current-game')
      if (savedGame && !sharedPuzzle) {
        try {
          const game = JSON.parse(savedGame)
          if (game.puzzle && game.puzzle.length === 81 && game.puzzle !== '.'.repeat(81)) {
            setPuzzle(game.puzzle, true)
            if (game.playMode) playMode.value = true
            if (game.difficulty) puzzleDifficulty.value = game.difficulty
            showToast('Game Resumed', 'Picked up where you left off!', 'info')
          }
        } catch (e) {}
      }

      // Handle shared puzzle
      if (sharedPuzzle) {
        try {
          const decoded = atob(sharedPuzzle).replace(/0/g, '.')
          if (decoded.length === 81) {
            setPuzzle(decoded, true)
            playMode.value = true
            showResult('Shared puzzle loaded! Solve it yourself or tap Solve.', 'info')
          }
        } catch (e) {}
        // Clean URL
        window.history.replaceState({}, '', window.location.pathname)
      }

      // Keyboard navigation
      window.addEventListener('keydown', handleKeyDown)

      // Auto-save game state on puzzle changes
      watch(puzzle, (val) => {
        if (val && val !== '.'.repeat(81)) {
          const filled = val.split('').filter(c => c !== '.').length
          updateFavicon(puzzleDifficulty.value, filled)
          localStorage.setItem('sudoku-current-game', JSON.stringify({
            puzzle: val,
            playMode: playMode.value,
            difficulty: puzzleDifficulty.value,
            ts: Date.now()
          }))
        }
      })
    })

    onUnmounted(() => {
      window.removeEventListener('resize', checkMobile)
      window.removeEventListener('keydown', handleKeyDown)
      stopTimer()
    })

    const checkMobile = () => {
      isMobile.value = window.innerWidth < 768
    }

    const formatTime = (ms) => {
      const seconds = Math.floor(ms / 1000)
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      if (minutes > 0) return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
      return `${seconds}s`
    }

    const toggleDarkMode = () => {
      isDark.value = !isDark.value
      localStorage.setItem('sudokuDarkMode', isDark.value.toString())
    }

    const closeMoreMenu = () => {
      moreMenuOpen.value = false
    }

    const handleAppClick = (e) => {
      moreMenuOpen.value = false
      // Close mobile pad when clicking outside grid/pad area
      if (showMobilePad.value) {
        const grid = e.target.closest('.grid, .number-bar, .pad-btn, .bar-btn')
        if (!grid) {
          showMobilePad.value = false
          selectedCell.value = -1
        }
      }
    }

    const startTimer = () => {
      stopTimer()
      elapsedTime.value = 0
      timerInterval = setInterval(() => {
        elapsedTime.value += 1000
      }, 1000)
    }

    const stopTimer = () => {
      if (timerInterval) {
        clearInterval(timerInterval)
        timerInterval = null
      }
    }

    // Update a single cell
    const onCellUpdate = async (index, value) => {
      const chars = puzzle.value.split('')
      const oldValue = chars[index]
      chars[index] = value || '.'
      puzzle.value = chars.join('')

      // Sound feedback + auto-remove pencil marks
      if (value && value !== '.') {
        playSound('place')
        autoRemovePencilMarks(index, value)
        // Challenge mode: detect mistakes
        if (challengeMode.value && puzzleSolution.value && puzzleSolution.value[index] !== value) {
          mistakes.value++
          if (mistakes.value >= 3) {
            stopTimer()
            showResult('💀 Game Over! 3 mistakes reached.', 'error')
          }
        }
      }

      // Show mobile pad on mobile if value was cleared
      if (isMobile.value && value === '') {
        showMobilePad.value = true
      }

      // Auto-save state for undo/redo
      await autoSaveState()

      // Refresh candidates after cell update
      await refreshCandidates()

      // Auto-remove pencil marks for this number in same row/col/box
      if (value && value !== '.') {
        const row = Math.floor(index / 9)
        const col = index % 9
        const region = Math.floor(row / 3) * 3 + Math.floor(col / 3)
        const newCandidates = { ...candidates.value }
        let changed = false
        for (let i = 0; i < 81; i++) {
          const iRow = Math.floor(i / 9)
          const iCol = i % 9
          const iRegion = Math.floor(iRow / 3) * 3 + Math.floor(iCol / 3)
          if (iRow === row || iCol === col || iRegion === region) {
            const key = String(i)
            if (newCandidates[key]) {
              const filtered = newCandidates[key].filter(n => n !== parseInt(value))
              if (filtered.length !== newCandidates[key].length) {
                newCandidates[key] = filtered
                changed = true
              }
            }
          }
        }
        if (changed) {
          candidates.value = newCandidates
        }
      }

      // Track mistakes if changing a solved cell to wrong value
      if (solvedCells.value.has(index) && value !== '.' && value !== oldValue) {
        // Could add validation here
      }

      // Check for puzzle completion in play mode
      if (playMode.value && !puzzle.value.includes('.')) {
        // All cells filled — check if correct
        const solved = await solvePuzzle(puzzle.value, false).catch(() => null)
        if (solved && solved.solved) {
          stopTimer()
          playSound('solved')
          confettiVisible.value = true
        }
      }
    }

    // Number pad input
    const onNumberPadInput = (num) => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, num.toString())
      }
    }

    const clearSelectedCell = () => {
      if (selectedCell.value >= 0 && !givenCells.value.has(selectedCell.value)) {
        onCellUpdate(selectedCell.value, '')
      }
    }

    // Cell selection
    const selectCell = (index) => {
      selectedCell.value = index
      if (index < 0) {
        showMobilePad.value = false
      } else {
        showMobilePad.value = true
      }
    }

    const navigateToCell = (index) => {
      selectedCell.value = index
    }

    // Set puzzle from string
    const setPuzzle = (str, isGiven = true) => {
      puzzle.value = str
      givenCells.value = new Set()
      solvedCells.value = new Set()

      for (let i = 0; i < 81; i++) {
        if (str[i] !== '.') {
          if (isGiven) {
            givenCells.value.add(i)
          } else {
            solvedCells.value.add(i)
          }
        }
      }

      // Reset progress tracking
      mistakes.value = 0
      hintsUsed.value = 0
      startTimer()

      // Fetch candidates for the new puzzle
      refreshCandidates()
    }

    // Refresh candidates from the backend
    const refreshCandidates = async () => {
      try {
        const data = await fetchCandidates(puzzle.value)
        if (data.candidates) {
          candidates.value = data.candidates
        }
      } catch (e) {
        // Silently fail - candidates are optional
        console.error('Failed to fetch candidates:', e)
      }
    }

    // Auto-remove pencil marks when a number is placed
    const autoRemovePencilMarks = (index, value) => {
      if (value === '.' || !showCandidates.value) return
      const row = Math.floor(index / 9)
      const col = index % 9
      const region = Math.floor(row / 3) * 3 + Math.floor(col / 3)
      const num = parseInt(value)

      const updated = { ...candidates.value }
      for (let i = 0; i < 81; i++) {
        const r = Math.floor(i / 9)
        const c = i % 9
        const reg = Math.floor(r / 3) * 3 + Math.floor(c / 3)
        if (r === row || c === col || reg === region) {
          if (updated[i]) {
            const marks = updated[i].filter(n => n !== num)
            if (marks.length > 0) updated[i] = marks
            else delete updated[i]
          }
        }
      }
      candidates.value = updated
    }

    // Show toast notification
    const showToast = (title, message, type = 'info', showRetry = false, onRetry = null) => {
      toast.title = title
      toast.message = message
      toast.type = type
      toast.showRetry = showRetry
      toast.onRetry = onRetry
      toast.visible = true
    }

    const hideToast = () => {
      toast.visible = false
    }

    // Show result
    const showResult = (message, type = 'info', difficulty = '', techniques = []) => {
      resultMessage.value = message
      resultType.value = type
      resultDifficulty.value = difficulty
      resultTechniques.value = techniques
      resultVisible.value = true

      setTimeout(() => {
        resultVisible.value = false
      }, type === 'success' ? 8000 : 5000)
    }

    // Undo/Redo functions
    const autoSaveState = async () => {
      const currentState = puzzle.value
      if (currentState !== lastSavedState) {
        try {
          await saveState(currentState)
          lastSavedState = currentState
          await loadHistoryState()
        } catch (e) {
          console.error('Failed to save state:', e)
        }
      }
    }

    const loadHistoryState = async () => {
      try {
        const data = await getHistory()
        canUndo.value = data.canUndo || false
        canRedo.value = data.canRedo || false
        undoCount.value = data.undoCount || 0
        redoCount.value = data.redoCount || 0
      } catch (e) {
        console.error('Failed to load history state:', e)
      }
    }

    const undoAction = async () => {
      try {
        const data = await undo()
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          await loadHistoryState()
          showResult('Undone!', 'info')
        } else {
          showToast('Undo Failed', data.error || 'Nothing to undo', 'error')
        }
      } catch (e) {
        showToast('Error', 'Failed to undo: ' + e.message, 'error', true, undoAction)
      }
    }

    const redoAction = async () => {
      try {
        const data = await redo()
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          await loadHistoryState()
          showResult('Redone!', 'info')
        } else {
          showToast('Redo Failed', data.error || 'Nothing to redo', 'error')
        }
      } catch (e) {
        showToast('Error', 'Failed to redo: ' + e.message, 'error', true, redoAction)
      }
    }

    const undo = () => {
      undoAction()
    }

    const redo = () => {
      redoAction()
    }

    // Solve the puzzle
    const solve = async () => {
      loading.value = true
      loadingMessage.value = 'Solving puzzle...'
      try {
        const data = await solvePuzzle(puzzle.value, true)
        if (data.solved) {
          playSound('solved')
          setPuzzle(data.solution, false)
          showResult(
            `Solved in ${data.metrics.solveTimeMs.toFixed(2)}ms`,
            'success',
            data.metrics.difficulty,
            data.metrics.techniquesUsed
          )
          stopTimer()
        } else {
          showResult(data.error || 'No solution found', 'error')
        }
      } catch (e) {
        showToast(
          'Connection Error',
          'Failed to connect to server. Please check your connection.',
          'error',
          true,
          solve
        )
      } finally {
        loading.value = false
      }
    }

    // Generate a new puzzle
    const generate = async (difficulty) => {
      loading.value = true
      loadingMessage.value = `Generating ${difficulty.toLowerCase()} puzzle...`
      try {
        const data = await generatePuzzle(difficulty)
        if (data.puzzle) {
          setPuzzle(data.puzzle, true)
          showResult(`Generated ${data.difficulty} puzzle!`, 'success')
          puzzleDifficulty.value = data.difficulty || difficulty
          // Get solution for challenge mode
          try {
            const sol = await solvePuzzle(data.puzzle, false)
            if (sol && sol.solved) puzzleSolution.value = sol.solution
          } catch(e) {}
          selectedCell.value = -1
          showMobilePad.value = false
          lastSavedState = data.puzzle
          await saveState(data.puzzle)
          await loadHistoryState()
        } else {
          showResult(data.error || 'Failed to generate puzzle', 'error')
        }
      } catch (e) {
        const msg = e.message?.includes('fetch') || e.message?.includes('network') || e.message?.includes('Failed to fetch')
          ? 'Server is sleeping. Trying again in 30s...'
          : 'Failed to generate puzzle: ' + e.message
        showToast(
          'Oops!',
          msg,
          'error',
          true,
          () => generate(difficulty)
        )
      } finally {
        loading.value = false
      }
    }

    const onImportPuzzle = (puzzleStr) => {
      setPuzzle(puzzleStr, true)
      selectedCell.value = -1
      importModalOpen.value = false
      playMode.value = true
      showResult('Puzzle imported! Tap Solve or solve it yourself.', 'success')
    }

    const onLoadSave = (save) => {
      setPuzzle(save.puzzle, true)
      selectedCell.value = -1
      savesOpen.value = false
      playMode.value = true
      if (save.difficulty) puzzleDifficulty.value = save.difficulty
      showResult('Puzzle loaded! Continue where you left off.', 'info')
    }

    const sharePuzzle = async () => {
      // Encode puzzle as base64 URL parameter
      const puzzleData = puzzle.value.replace(/\./g, '0')
      const encoded = btoa(puzzleData)
      const url = `${window.location.origin}?p=${encoded}`

      if (navigator.share) {
        try {
          await navigator.share({
            title: 'Sudoku Dojo Puzzle',
            text: 'Can you solve this Sudoku puzzle?',
            url
          })
        } catch (e) { /* cancelled */ }
      } else {
        await navigator.clipboard.writeText(url)
        showToast('Link Copied!', 'Share this link to challenge someone!', 'success')
      }
      playSound('click')
    }

    const handlePrint = () => {
      printPuzzle(puzzle.value, puzzleDifficulty.value)
      playSound('click')
    }

    const handleShareImage = () => {
      const img = generatePuzzleImage(puzzle.value, puzzleDifficulty.value)
      downloadImage(img)
      playSound('click')
      showToast('Image Saved!', 'Share it with friends!', 'success')
    }

    // Get a hint
    const getHint = async () => {
      loading.value = true
      loadingMessage.value = 'Finding hint...'
      try {
        const data = await getHintForPuzzle(puzzle.value)
        if (data.hasHint) {
          currentHint.value = data.hint
          hintsUsed.value++
          hintModalVisible.value = true
          playSound('hint')
          showMobilePad.value = false
        } else {
          showToast(
            'No Hint Available',
            data.error || 'Try solving some more cells first!',
            'warning'
          )
        }
      } catch (e) {
        showToast(
          'Error',
          'Failed to get hint: ' + e.message,
          'error',
          true,
          getHint
        )
      } finally {
        loading.value = false
      }
    }

    const closeHintModal = () => {
      hintModalVisible.value = false
    }

    // Clear the grid
    const clearGrid = () => {
      puzzle.value = '.'.repeat(81)
      givenCells.value = new Set()
      solvedCells.value = new Set()
      resultVisible.value = false
      selectedCell.value = -1
      showMobilePad.value = false
      mistakes.value = 0
      hintsUsed.value = 0
      candidates.value = {}
      startTimer()
      lastSavedState = puzzle.value
    }

    // Tutorial methods
    const toggleColorBlind = () => {
      colorBlindMode.value = !colorBlindMode.value
      localStorage.setItem('sudokuColorBlind', colorBlindMode.value.toString())
    }

    const toggleHighContrast = () => {
      highContrastMode.value = !highContrastMode.value
      localStorage.setItem('sudokuHighContrast', highContrastMode.value.toString())
    }

    const toggleChallenge = () => {
      challengeMode.value = !challengeMode.value
      localStorage.setItem('sudoku-challenge', challengeMode.value.toString())
    }

    const toggleTutorialMode = async () => {
      if (tutorialMode.value || quizMode.value || practiceMode.value) {
        tutorialMode.value = false
        currentTutorialLesson.value = null
        quizMode.value = false
        currentQuiz.value = null
        practiceMode.value = false
        currentPracticeSet.value = null
        return
      }
      try {
        const tutorials = await fetchTutorials()
        tutorialList.value = tutorials
        // Load quiz and practice data in parallel
        const [quizzes, practices] = await Promise.all([
          fetchQuizzes().catch(() => []),
          fetchAllPracticeSets().catch(() => [])
        ])
        quizList.value = quizzes
        practiceList.value = practices
        // Show the selector instead of auto-loading
        tutorialSelectorOpen.value = true
      } catch (e) {
        console.error('Failed to load tutorials:', e)
      }
    }

    const onTutorialSelected = async (lesson) => {
      try {
        const full = await fetchTutorial(lesson.id)
        currentTutorialLesson.value = full
        tutorialSelectorOpen.value = false
        tutorialMode.value = true
      } catch (e) {
        console.error('Failed to load tutorial:', e)
      }
    }

    const exitTutorialMode = () => {
      tutorialMode.value = false
      currentTutorialLesson.value = null
      // Go back to selector
      if (tutorialList.value.length > 0) {
        tutorialSelectorOpen.value = true
      }
    }

    const onTutorialCompleted = (lessonId) => {
      completedTutorials.value.add(lessonId)
      localStorage.setItem(
        'sudokuCompletedTutorials',
        JSON.stringify([...completedTutorials.value])
      )
    }

    // Quiz methods
    const onQuizSelected = (quiz) => {
      currentQuiz.value = quiz
      tutorialSelectorOpen.value = false
      quizMode.value = true
    }

    const exitQuizMode = () => {
      quizMode.value = false
      currentQuiz.value = null
      if (tutorialList.value.length > 0) {
        tutorialSelectorOpen.value = true
      }
    }

    const onQuizCompleted = (result) => {
      // Score is saved in QuizMode via localStorage
    }

    // Practice methods
    const onPracticeSelected = async (lesson) => {
      const set = practiceList.value.find(p => p.tutorialId === lesson.id)
      if (set) {
        currentPracticeSet.value = set
        tutorialSelectorOpen.value = false
        practiceMode.value = true
      }
    }

    const exitPracticeMode = () => {
      practiceMode.value = false
      currentPracticeSet.value = null
      if (tutorialList.value.length > 0) {
        tutorialSelectorOpen.value = true
      }
    }

    const openAchievements = () => {
      achievementStats.value = getStatsForAchievements()
      leaderboardOpen.value = false
      statsOpen.value = false
      achievementsOpen.value = true
    }

    const openStats = () => {
      achievementsOpen.value = false
      leaderboardOpen.value = false
      statsOpen.value = true
    }

    const onPracticeCompleted = (result) => {
      // Progress is saved in PracticeMode via localStorage
    }

    return {
      puzzle,
      givenCells,
      solvedCells,
      digitCounts,
      loading,
      loadingMessage,
      selectedCell,
      isDark,
      showMobilePad,
      elapsedTime,
      mistakes,
      hintsUsed,
      puzzleDifficulty,
      canUndo,
      canRedo,
      undoCount,
      redoCount,
      resultMessage,
      resultType,
      resultVisible,
      resultDifficulty,
      resultTechniques,
      toast,
      hintModalVisible,
      currentHint,
      candidates,
      showCandidates,
      pencilMode,
      moreMenuOpen,
      closeMoreMenu,
      handleAppClick,
      tutorialMode,
      tutorialList,
      currentTutorialLesson,
      tutorialSelectorOpen,
      dailyMode,
      playMode,
      settingsOpen,
      aboutOpen,
      helpOpen,
      onboardingOpen,
      hasSeenOnboarding,
      colorBlindMode,
      highContrastMode,
      boardTheme,
      completedTutorials,
      toggleDarkMode,
      toggleColorBlind,
      toggleHighContrast,
      toggleChallenge,
      challengeMode,
      toggleTutorialMode,
      onTutorialSelected,
      exitTutorialMode,
      onTutorialCompleted,
      quizMode,
      currentQuiz,
      quizList,
      practiceMode,
      currentPracticeSet,
      practiceList,
      onQuizSelected,
      exitQuizMode,
      onQuizCompleted,
      onPracticeSelected,
      exitPracticeMode,
      onPracticeCompleted,
      leaderboardOpen,
      whatsNewOpen,
      savesOpen,
      onLoadSave,
      achievementsOpen,
      statsOpen,
      achievementStats,
      openAchievements,
      openStats,
      onCellUpdate,
      onNumberPadInput,
      clearSelectedCell,
      selectCell,
      navigateToCell,
      solve,
      generate,
      getHint,
      undo,
      redo,
      clearGrid,
      hideToast,
      closeHintModal,
      importModalOpen,
      keyboardHelpOpen,
      onImportPuzzle,
      sharePuzzle,
      handlePrint,
      handleShareImage,
      handleKeyDown,
      confettiVisible,
      formatTime
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  min-height: 100vh;
  transition: background 0.3s ease;
}

.app {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: background 0.3s ease;
}

.app.dark {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  padding: 20px;
  max-width: 500px;
  width: 100%;
  position: relative;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.app.dark .container {
  background: #1e1e1e;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.container.loading {
  opacity: 0.6;
  pointer-events: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
  position: relative;
}

/* Unified header button style */
.header-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: #e8f0fe;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-btn:hover {
  background: #d2e3fc;
  transform: scale(1.1);
}

.app.dark .header-btn {
  background: #333;
}

.app.dark .header-btn:hover {
  background: #444;
}

/* More menu dropdown */
.header-more {
  position: relative;
}

.more-btn {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
}

.more-dropdown {
  position: absolute;
  right: 0;
  top: 52px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 100;
  overflow: hidden;
  animation: fadeIn 0.15s ease;
}

.more-dropdown.dark {
  background: #2d2d2d;
  border-color: #444;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background 0.15s;
}

.menu-item:hover {
  background: #f0f0f0;
}

.more-dropdown.dark .menu-item {
  color: #e0e0e0;
}

.more-dropdown.dark .menu-item:hover {
  background: #383838;
}

.menu-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.menu-label {
  flex: 1;
  text-align: left;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

h1 {
  color: #333;
  font-size: 28px;
  transition: color 0.3s ease;
}

.app.dark h1 {
  color: #e0e0e0;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.app.dark .loading-overlay {
  background: rgba(30, 30, 30, 0.9);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e0e0e0;
  border-top-color: #4285f4;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
}

.app.dark .loading-text {
  color: #aaa;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Mobile responsive */
@media (max-width: 500px) {
  body {
    padding: 10px;
  }

  .app {
    padding: 10px;
    align-items: flex-start;
  }

  .container {
    padding: 15px;
    border-radius: 12px;
  }

  h1 {
    font-size: 22px;
  }

  .header-btn {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}

/* iPhone specific fixes */
@media (max-width: 400px) {
  .container {
    padding: 12px;
  }

  h1 {
    font-size: 20px;
  }
}

/* Extra small screens */
@media (max-width: 320px) {
  .app {
    padding: 5px;
  }

  .container {
    padding: 10px;
    border-radius: 8px;
  }

  h1 {
    font-size: 18px;
  }
}
</style>
