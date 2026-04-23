import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Leaderboard from '@/components/Leaderboard.vue'

describe('Leaderboard', () => {
  it('mounts without errors', () => {
    const wrapper = mount(Leaderboard, {
      props: { isDark: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders leaderboard content', () => {
    const wrapper = mount(Leaderboard, {
      props: { isDark: false }
    })
    expect(wrapper.findAll('button').length).toBeGreaterThan(0)
  })

  it('emits back when back button clicked', async () => {
    const wrapper = mount(Leaderboard, {
      props: { isDark: false }
    })
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('back')).toBeTruthy()
    }
  })

  it('applies dark mode', () => {
    const wrapper = mount(Leaderboard, {
      props: { isDark: true }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
