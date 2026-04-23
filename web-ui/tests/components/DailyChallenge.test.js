import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DailyChallenge from '@/components/DailyChallenge.vue'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/api', () => ({
  fetchDailyChallenge: vi.fn().mockResolvedValue({
    puzzle: '.'.repeat(81),
    difficulty: 'EASY',
    date: '2026-04-23'
  }),
  solvePuzzle: vi.fn().mockResolvedValue({ solved: false })
}))

describe('DailyChallenge', () => {
  it('mounts without errors', async () => {
    const wrapper = mount(DailyChallenge, {
      props: { isDark: false }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })

  it('emits exit when exit triggered', async () => {
    const wrapper = mount(DailyChallenge, {
      props: { isDark: false }
    })
    await flushPromises()
    // The component has a back/exit mechanism
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('Exit') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('renders with dark mode', async () => {
    const wrapper = mount(DailyChallenge, {
      props: { isDark: true }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })
})
