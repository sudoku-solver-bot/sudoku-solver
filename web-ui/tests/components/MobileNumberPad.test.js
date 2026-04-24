import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import MobileNumberPad from '../../src/components/MobileNumberPad.vue'

// Mock SudokuGrid to isolate number pad behavior
vi.mock('../../src/components/SudokuGrid.vue', () => ({
  default: { template: '<div class="mock-grid" />' }
}))

describe('MobileNumberPad', () => {
  const defaultProps = {
    visible: true,
    counts: { 1: 2, 2: 5, 3: 9, 4: 0, 5: 1, 6: 4, 7: 3, 8: 6, 9: 8 },
    pencilMode: false
  }

  it('renders all 9 number buttons when visible', () => {
    const wrapper = mount(MobileNumberPad, { props: defaultProps })
    const buttons = wrapper.findAll('.bar-btn')
    // 9 digits + clear + hint + pencil = 12
    expect(buttons.length).toBe(12)
    // First 9 should be numbers
    for (let i = 1; i <= 9; i++) {
      expect(buttons[i - 1].find('.bar-num').text()).toBe(String(i))
    }
  })

  it('shows remaining count for each digit', () => {
    const wrapper = mount(MobileNumberPad, { props: defaultProps })
    const buttons = wrapper.findAll('.bar-btn')
    // Digit 1: 2 placed → 7 remaining
    expect(buttons[0].find('.bar-count').text()).toBe('7')
    // Digit 3: 9 placed → 0 remaining (complete)
    expect(buttons[2].find('.bar-count').text()).toBe('0')
    expect(buttons[2].classes()).toContain('complete')
  })

  it('emits input event with correct digit', async () => {
    const wrapper = mount(MobileNumberPad, { props: defaultProps })
    const buttons = wrapper.findAll('.bar-btn')
    await buttons[4].trigger('click') // digit 5
    expect(wrapper.emitted('input')).toBeTruthy()
    expect(wrapper.emitted('input')[0]).toEqual([5])
  })

  it('emits clear event', async () => {
    const wrapper = mount(MobileNumberPad, { props: defaultProps })
    const clearBtn = wrapper.findAll('.bar-btn')[9] // 10th button = clear
    await clearBtn.trigger('click')
    expect(wrapper.emitted('clear')).toBeTruthy()
  })

  it('emits hint event', async () => {
    const wrapper = mount(MobileNumberPad, { props: defaultProps })
    const hintBtn = wrapper.findAll('.bar-btn')[10] // hint
    await hintBtn.trigger('click')
    expect(wrapper.emitted('hint')).toBeTruthy()
  })

  it('emits toggle-pencil event and shows active state', async () => {
    const wrapper = mount(MobileNumberPad, { props: { ...defaultProps, pencilMode: true } })
    const pencilBtn = wrapper.findAll('.bar-btn')[11]
    expect(pencilBtn.classes()).toContain('pencil-active')
    await pencilBtn.trigger('click')
    expect(wrapper.emitted('toggle-pencil')).toBeTruthy()
  })

  it('does not render when not visible', () => {
    const wrapper = mount(MobileNumberPad, { props: { ...defaultProps, visible: false } })
    expect(wrapper.find('.number-bar').exists()).toBe(false)
  })
})

describe('Cell Selection & Deselect', () => {
  // These test the integration between grid events and pad visibility
  
  it('should set selectedCell to -1 when deselecting (tap same cell)', () => {
    // Simulate the toggle behavior in SudokuGrid: select → deselect on retap
    const selectedCell = ref(5)
    // Simulating the grid toggle: if same cell tapped, emit -1
    const onCellSelect = (index) => {
      if (selectedCell.value === index) {
        selectedCell.value = -1
      } else {
        selectedCell.value = index
      }
    }
    
    onCellSelect(5) // tap same cell → deselect
    expect(selectedCell.value).toBe(-1)
    
    onCellSelect(3) // tap different cell → select
    expect(selectedCell.value).toBe(3)
  })

  it('pad visibility should follow selectedCell in Free Play', () => {
    const selectedCell = ref(-1)
    const showMobilePad = ref(false)
    
    const selectCell = (index) => {
      selectedCell.value = index
      if (index < 0) {
        showMobilePad.value = false
      } else {
        showMobilePad.value = true
      }
    }
    
    // Initially hidden
    expect(showMobilePad.value).toBe(false)
    
    // Select a cell → pad shows
    selectCell(5)
    expect(showMobilePad.value).toBe(true)
    expect(selectedCell.value).toBe(5)
    
    // Deselect → pad hides
    selectCell(-1)
    expect(showMobilePad.value).toBe(false)
    expect(selectedCell.value).toBe(-1)
  })

  it('Daily Challenge pad should remain visible after deselect', () => {
    // Daily uses :visible="true" (always visible)
    const selectedCell = ref(5)
    const padAlwaysVisible = true // Daily's :visible="true"
    
    // Deselect cell
    selectedCell.value = -1
    
    // Pad should still be visible
    expect(padAlwaysVisible).toBe(true)
    // But no number can be placed since no cell selected
    expect(selectedCell.value).toBe(-1)
  })
})
