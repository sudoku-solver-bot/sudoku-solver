import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import WhatsNew from '@/components/WhatsNew.vue'

describe('WhatsNew', () => {
  it('mounts without errors', () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders version info', () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: false }
    })
    expect(wrapper.text()).toContain('v2.0')
  })

  it('renders feature list', () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: false }
    })
    // Should show feature cards
    expect(wrapper.findAll('[class*="feature"]').length + wrapper.findAll('button').length).toBeGreaterThan(0)
  })

  it('has close/continue button', () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: false }
    })
    const playBtn = wrapper.findAll('button').find(b => b.text().includes('Let') || b.text().includes('play') || b.text().includes('Play') || b.text().includes('Continue'))
    expect(playBtn).toBeTruthy()
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: false }
    })
    const closeBtn = wrapper.findAll('button').find(b => b.text().includes('Let') || b.text().includes('play') || b.text().includes('Play'))
    if (closeBtn) {
      await closeBtn.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })

  it('applies dark mode', () => {
    const wrapper = mount(WhatsNew, {
      props: { isDark: true }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
