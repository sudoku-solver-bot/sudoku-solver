import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AboutPage from '@/components/AboutPage.vue'

describe('AboutPage', () => {
  it('mounts without errors', () => {
    const wrapper = mount(AboutPage)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders the app title', () => {
    const wrapper = mount(AboutPage)
    expect(wrapper.text()).toContain('Sudoku Dojo')
  })

  it('renders feature items', () => {
    const wrapper = mount(AboutPage)
    expect(wrapper.text()).toContain('Daily Challenge')
    expect(wrapper.text()).toContain('Learn Techniques')
    expect(wrapper.text()).toContain('Smart Hints')
    expect(wrapper.text()).toContain('Pencil Marks')
  })

  it('emits exit when back button is clicked', async () => {
    const wrapper = mount(AboutPage)
    const backBtn = wrapper.find('.back-btn')
    await backBtn.trigger('click')
    expect(wrapper.emitted('exit')).toBeTruthy()
  })

  it('applies dark mode class', () => {
    const wrapper = mount(AboutPage, { props: { isDark: true } })
    expect(wrapper.find('.about-page').classes()).toContain('dark')
  })

  it('renders credits section', () => {
    const wrapper = mount(AboutPage)
    expect(wrapper.text()).toContain('Built with')
    expect(wrapper.text()).toContain('v1.0.0')
  })

  it('renders QR code section', () => {
    const wrapper = mount(AboutPage)
    expect(wrapper.find('.qr-section').exists()).toBe(true)
    expect(wrapper.text()).toContain('Scan to share')
  })
})
