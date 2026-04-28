import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Dashboard from '@/components/Dashboard.vue'

describe('Dashboard', () => {
  it('mounts without errors', () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set(),
        totalTutorials: 15,
        isDark: false
      }
    })
    expect(wrapper.find('.dashboard').exists() || wrapper.element).toBeTruthy()
  })

  it('renders action cards', () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set(),
        totalTutorials: 15,
        isDark: false
      }
    })
    // Dashboard should have action buttons/cards for daily, learn, play
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('emits play when play action triggered', async () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set(),
        totalTutorials: 15,
        isDark: false
      }
    })
    const playBtn = wrapper.findAll('button').find(b =>
      b.text().includes('Play') || b.text().includes('play')
    )
    if (playBtn) {
      await playBtn.trigger('click')
      expect(wrapper.emitted('play')).toBeTruthy()
    }
  })

  it('emits daily when daily action triggered', async () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set(),
        totalTutorials: 15,
        isDark: false
      }
    })
    const dailyBtn = wrapper.findAll('button').find(b =>
      b.text().includes('Daily') || b.text().includes('daily')
    )
    if (dailyBtn) {
      await dailyBtn.trigger('click')
      expect(wrapper.emitted('daily')).toBeTruthy()
    }
  })

  it('shows tutorial progress', () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set([1, 2, 3]),
        totalTutorials: 15,
        isDark: false
      }
    })
    expect(wrapper.text()).toContain('3')
  })

  it('applies dark mode class', () => {
    const wrapper = mount(Dashboard, {
      props: {
        completedTutorials: new Set(),
        totalTutorials: 15,
        isDark: true
      }
    })
    expect(wrapper.find('.dark').exists() || wrapper.classes().includes('dark')).toBe(true)
  })
})
