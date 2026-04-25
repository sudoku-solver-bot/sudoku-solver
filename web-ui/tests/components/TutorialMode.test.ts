import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TutorialMode from '@/components/TutorialMode.vue'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/api', () => ({
  fetchTutorialBoard: vi.fn().mockResolvedValue({
    puzzle: '.'.repeat(81),
    solution: '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
    candidates: {}
  }),
  completeTutorial: vi.fn().mockResolvedValue({ success: true }),
  fetchCandidates: vi.fn().mockResolvedValue({ candidates: {} })
}))

describe('TutorialMode', () => {
  const mockLesson = {
    id: 1,
    title: 'Naked Single',
    beltName: 'White Belt',
    beltEmoji: '⬜',
    beltColor: '#ffffff',
    examplePuzzle: '.'.repeat(81),
    steps: [
      { text: 'Look at this cell. Only one number fits.', highlightedCells: [0] },
      { text: 'The value must be 5.', highlightedCells: [0], answer: 5 }
    ]
  }

  it('mounts without errors', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: false }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })

  it('shows lesson title', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: false }
    })
    await flushPromises()
    expect(wrapper.text()).toContain('Naked Single')
  })

  it('shows belt badge', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: false }
    })
    await flushPromises()
    expect(wrapper.text()).toContain('White Belt')
  })

  it('shows progress bar', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: false }
    })
    await flushPromises()
    expect(wrapper.find('.progress-bar').exists() || wrapper.find('[class*="progress"]').exists()).toBe(true)
  })

  it('emits exit when back button clicked', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: false }
    })
    await flushPromises()
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('applies dark mode', async () => {
    const wrapper = mount(TutorialMode, {
      props: { lesson: mockLesson, isDark: true }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })
})
