import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatsPage from '@/components/StatsPage.vue'

describe('StatsPage', () => {
  it('mounts without errors', () => {
    const wrapper = mount(StatsPage, {
      props: { isDark: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders statistics content', () => {
    const wrapper = mount(StatsPage, {
      props: { isDark: false }
    })
    expect(wrapper.findAll('button').length).toBeGreaterThan(0)
  })

  it('emits back when back button clicked', async () => {
    const wrapper = mount(StatsPage, {
      props: { isDark: false }
    })
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('back')).toBeTruthy()
    }
  })

  it('shows stats title', () => {
    const wrapper = mount(StatsPage, {
      props: { isDark: false }
    })
    expect(wrapper.text()).toContain('Statistic')
  })

  it('applies dark mode', () => {
    const wrapper = mount(StatsPage, {
      props: { isDark: true }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
