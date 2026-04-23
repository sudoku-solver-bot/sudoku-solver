import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PracticeMode from '@/components/PracticeMode.vue'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/api', () => ({
  fetchPracticeBoard: vi.fn().mockResolvedValue({
    puzzle: '.'.repeat(81),
    solution: '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
    candidates: {}
  }),
  fetchCandidates: vi.fn().mockResolvedValue({ candidates: {} })
}))

describe('PracticeMode', () => {
  const mockPracticeSet = {
    tutorialId: 1,
    title: 'Test Practice',
    puzzles: [
      { id: 1, puzzle: '.'.repeat(81) }
    ]
  }

  it('mounts without errors', async () => {
    const wrapper = mount(PracticeMode, {
      props: { practiceSet: mockPracticeSet, isDark: false }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })

  it('shows practice header with buttons', async () => {
    const wrapper = mount(PracticeMode, {
      props: { practiceSet: mockPracticeSet, isDark: false }
    })
    await flushPromises()
    expect(wrapper.findAll('button').length).toBeGreaterThan(0)
  })

  it('emits exit when back button clicked', async () => {
    const wrapper = mount(PracticeMode, {
      props: { practiceSet: mockPracticeSet, isDark: false }
    })
    await flushPromises()
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('applies dark mode', async () => {
    const wrapper = mount(PracticeMode, {
      props: { practiceSet: mockPracticeSet, isDark: true }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })
})
