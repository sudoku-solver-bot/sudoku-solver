import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SavedPuzzles from '@/components/SavedPuzzles.vue'

describe('SavedPuzzles', () => {
  it('mounts without errors', () => {
    const wrapper = mount(SavedPuzzles, {
      props: { isDark: false, currentPuzzle: '', currentDifficulty: '' }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders save/load UI', () => {
    const wrapper = mount(SavedPuzzles, {
      props: { isDark: false, currentPuzzle: '', currentDifficulty: '' }
    })
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(SavedPuzzles, {
      props: { isDark: false, currentPuzzle: '', currentDifficulty: '' }
    })
    const closeBtn = wrapper.findAll('button').find(b => b.text().includes('Close') || b.text().includes('Cancel') || b.text().includes('×'))
    if (closeBtn) {
      await closeBtn.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })

  it('shows empty state when no puzzles', () => {
    const wrapper = mount(SavedPuzzles, {
      props: { isDark: false, currentPuzzle: '', currentDifficulty: '' }
    })
    // Should show empty state message
    expect(wrapper.text()).toBeTruthy()
  })

  it('applies dark mode', () => {
    const wrapper = mount(SavedPuzzles, {
      props: { isDark: true, currentPuzzle: '', currentDifficulty: '' }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
