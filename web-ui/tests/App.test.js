import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '@/App.vue'
import { flushPromises } from '@vue/test-utils'

// Mock all API functions
vi.mock('@/api', () => ({
  solvePuzzle: vi.fn().mockResolvedValue({ solved: false }),
  generatePuzzle: vi.fn().mockResolvedValue({ puzzle: '.'.repeat(81), difficulty: 'EASY' }),
  getHintForPuzzle: vi.fn().mockResolvedValue({ hasHint: false }),
  saveState: vi.fn().mockResolvedValue({}),
  undo: vi.fn().mockResolvedValue({}),
  redo: vi.fn().mockResolvedValue({}),
  getHistory: vi.fn().mockResolvedValue({ canUndo: false, canRedo: false, undoCount: 0, redoCount: 0 }),
  fetchCandidates: vi.fn().mockResolvedValue({ candidates: {} }),
  fetchTutorials: vi.fn().mockResolvedValue([]),
  fetchTutorial: vi.fn().mockResolvedValue({}),
  fetchQuizzes: vi.fn().mockResolvedValue([]),
  fetchAllPracticeSets: vi.fn().mockResolvedValue([])
}))

// Mock utility modules
vi.mock('@/stats-tracker', () => ({
  getStatsForAchievements: vi.fn().mockReturnValue({})
}))

vi.mock('@/sounds', () => ({
  playSound: vi.fn(),
  isSoundEnabled: vi.fn().mockReturnValue(false),
  setSoundEnabled: vi.fn()
}))

vi.mock('@/favicon', () => ({
  updateFavicon: vi.fn()
}))

vi.mock('@/print', () => ({
  printPuzzle: vi.fn()
}))

vi.mock('@/share-image', () => ({
  generatePuzzleImage: vi.fn().mockReturnValue(null),
  downloadImage: vi.fn()
}))

import { solvePuzzle, generatePuzzle, getHintForPuzzle } from '@/api'

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders header with title', () => {
    const wrapper = mount(App)
    expect(wrapper.find('h1').text()).toContain('Sudoku Solver')
  })

  it('renders Dashboard as default view', () => {
    const wrapper = mount(App)
    expect(wrapper.findComponent({ name: 'Dashboard' }).exists()).toBe(true)
  })

  it('navigates to Free Play mode when play is triggered', async () => {
    const wrapper = mount(App)

    const dashboard = wrapper.findComponent({ name: 'Dashboard' })
    await dashboard.vm.$emit('play')
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'SudokuGrid' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ControlPanel' }).exists()).toBe(true)
  })

  it('navigates to Settings panel', async () => {
    const wrapper = mount(App)
    const settingsBtn = wrapper.find('.settings-btn')
    await settingsBtn.trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'Settings' }).exists()).toBe(true)
  })

  it('navigates to Daily Challenge', async () => {
    const wrapper = mount(App)
    const dailyBtn = wrapper.find('.daily-btn')
    await dailyBtn.trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'DailyChallenge' }).exists()).toBe(true)
  })

  it('generates a puzzle and fills the grid', async () => {
    const mockPuzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
    generatePuzzle.mockResolvedValue({
      puzzle: mockPuzzle,
      difficulty: 'EASY'
    })

    const wrapper = mount(App)

    // Go to play mode first
    wrapper.vm.playMode = true
    await wrapper.vm.$nextTick()

    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })
    await controlPanel.vm.$emit('generate', 'EASY')
    await flushPromises()

    expect(generatePuzzle).toHaveBeenCalledWith('EASY')
    expect(wrapper.vm.puzzle).toBe(mockPuzzle)
  })

  it('handles API errors without crashing', async () => {
    solvePuzzle.mockRejectedValue(new Error('Network error'))
    generatePuzzle.mockRejectedValue(new Error('Network error'))

    const wrapper = mount(App)

    wrapper.vm.playMode = true
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'SudokuGrid' }).exists()).toBe(true)
  })

  it('clears grid when clear event is emitted', async () => {
    const wrapper = mount(App)

    wrapper.vm.playMode = true
    await wrapper.vm.$nextTick()

    const grid = wrapper.findComponent({ name: 'SudokuGrid' })
    await grid.vm.$emit('update', 0, '5')

    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })
    await controlPanel.vm.$emit('clear')

    expect(wrapper.vm.puzzle).toBe('.'.repeat(81))
  })

  it('renders InstallPrompt and OfflineIndicator', () => {
    const wrapper = mount(App)
    expect(wrapper.findComponent({ name: 'InstallPrompt' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'OfflineIndicator' }).exists()).toBe(true)
  })
})
