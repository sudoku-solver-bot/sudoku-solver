import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import HelpPage from '@/components/HelpPage.vue'

describe('HelpPage', () => {
  it('mounts without errors', () => {
    const wrapper = mount(HelpPage)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders Getting Started section', () => {
    const wrapper = mount(HelpPage)
    expect(wrapper.text()).toContain('Getting Started')
    expect(wrapper.text()).toContain('Free Play')
    expect(wrapper.text()).toContain('Daily Challenge')
    expect(wrapper.text()).toContain('Learn Techniques')
  })

  it('renders Controls section', () => {
    const wrapper = mount(HelpPage)
    expect(wrapper.text()).toContain('Controls')
    expect(wrapper.text()).toContain('Pencil mode')
  })

  it('emits exit when back button is clicked', async () => {
    const wrapper = mount(HelpPage)
    const backBtn = wrapper.find('.back-btn')
    await backBtn.trigger('click')
    expect(wrapper.emitted('exit')).toBeTruthy()
  })

  it('applies dark mode class', () => {
    const wrapper = mount(HelpPage, { props: { isDark: true } })
    expect(wrapper.find('.help-page').classes()).toContain('dark')
  })
})
