import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '@/App.vue'
import { flushPromises } from '@vue/test-utils'

// Mock the API module
vi.mock('@/api', () => ({
  solvePuzzle: vi.fn(),
  generatePuzzle: vi.fn(),
  getHintForPuzzle: vi.fn()
}))

import { solvePuzzle, generatePuzzle, getHintForPuzzle } from '@/api'

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders all child components', () => {
    const wrapper = mount(App)

    expect(wrapper.findComponent({ name: 'SudokuGrid' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ControlPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ResultDisplay' }).exists()).toBe(true)
  })

  it('clear button resets the grid', async () => {
    const wrapper = mount(App)

    // Set initial puzzle state by simulating cell input
    const grid = wrapper.findComponent({ name: 'SudokuGrid' })
    await grid.vm.$emit('update', 0, '5')

    // Click clear button
    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })
    await controlPanel.vm.$emit('clear')

    expect(wrapper.vm.puzzle).toBe('.'.repeat(81))
    expect(wrapper.vm.givenCells.size).toBe(0)
    expect(wrapper.vm.solvedCells.size).toBe(0)
  })

  it('generate button creates new puzzle', async () => {
    const mockPuzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
    generatePuzzle.mockResolvedValue({
      puzzle: mockPuzzle,
      difficulty: 'EASY'
    })

    const wrapper = mount(App)
    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })

    await controlPanel.vm.$emit('generate', 'EASY')
    await flushPromises()

    expect(generatePuzzle).toHaveBeenCalledWith('EASY')
    expect(wrapper.vm.puzzle).toBe(mockPuzzle)
    expect(wrapper.vm.givenCells.size).toBeGreaterThan(0)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('solve button calls API and updates grid', async () => {
    const initialPuzzle = '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79'
    const mockSolution = '534678912672195348198342567859761423426853791713924856961537284287419635345286179'
    solvePuzzle.mockResolvedValue({
      solved: true,
      solution: mockSolution,
      metrics: {
        solveTimeMs: 42.5,
        difficulty: 'MEDIUM',
        techniquesUsed: ['Naked Singles', 'Hidden Singles']
      }
    })

    const wrapper = mount(App)

    // Set up initial puzzle by generating it
    generatePuzzle.mockResolvedValue({
      puzzle: initialPuzzle,
      difficulty: 'MEDIUM'
    })

    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })
    await controlPanel.vm.$emit('generate', 'MEDIUM')
    await flushPromises()

    // Now solve it
    solvePuzzle.mockClear()
    await controlPanel.vm.$emit('solve')
    await flushPromises()

    expect(solvePuzzle).toHaveBeenCalledWith(initialPuzzle, true)
    expect(wrapper.vm.puzzle).toBe(mockSolution)
    expect(wrapper.vm.solvedCells.size).toBeGreaterThan(0)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('hint button calls API', async () => {
    getHintForPuzzle.mockResolvedValue({
      hasHint: true,
      hint: {
        row: 0,
        col: 0,
        value: 5,
        technique: 'Naked Single'
      }
    })

    const wrapper = mount(App)
    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })

    await controlPanel.vm.$emit('hint')
    await flushPromises()

    expect(getHintForPuzzle).toHaveBeenCalled()
    expect(wrapper.vm.loading).toBe(false)
  })

  it('updates cell on input', async () => {
    const wrapper = mount(App)
    const grid = wrapper.findComponent({ name: 'SudokuGrid' })

    await grid.vm.$emit('update', 0, '5')

    expect(wrapper.vm.puzzle[0]).toBe('5')
  })

  it('sets loading state during solve', async () => {
    let resolvePromise
    solvePuzzle.mockImplementation(() => new Promise(resolve => {
      resolvePromise = resolve
    }))

    const wrapper = mount(App)
    const controlPanel = wrapper.findComponent({ name: 'ControlPanel' })

    // Trigger solve
    controlPanel.vm.$emit('solve')

    // Loading should be true immediately
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.loading).toBe(true)

    // Resolve the API call
    resolvePromise({
      solved: true,
      solution: '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
      metrics: {
        solveTimeMs: 42.5,
        difficulty: 'MEDIUM',
        techniquesUsed: ['Naked Singles']
      }
    })

    await flushPromises()

    expect(wrapper.vm.loading).toBe(false)
  })
})
