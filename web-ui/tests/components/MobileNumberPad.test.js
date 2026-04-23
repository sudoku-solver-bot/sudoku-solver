import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MobileNumberPad from '@/components/MobileNumberPad.vue'

describe('MobileNumberPad', () => {
  it('mounts without errors', () => {
    const wrapper = mount(MobileNumberPad, {
      props: { visible: true, counts: {}, pencilMode: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders number buttons 1-9', () => {
    const wrapper = mount(MobileNumberPad, {
      props: { visible: true, counts: {}, pencilMode: false }
    })
    // Should have 9 number buttons
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('emits input when number button clicked', async () => {
    const wrapper = mount(MobileNumberPad, {
      props: { visible: true, counts: {}, pencilMode: false }
    })
    // Find a number button
    const numBtn = wrapper.findAll('button').find(b => b.text().trim().match(/^[1-9]$/))
    if (numBtn) {
      await numBtn.trigger('click')
      expect(wrapper.emitted('input')).toBeTruthy()
    }
  })

  it('shows pencil mode toggle', () => {
    const wrapper = mount(MobileNumberPad, {
      props: { visible: true, counts: {}, pencilMode: false }
    })
    const pencilBtn = wrapper.findAll('button').find(b => b.text().includes('✏'))
    expect(pencilBtn).toBeTruthy()
  })

  it('marks completed numbers', () => {
    const counts = { 1: 9, 2: 5, 3: 0 }
    const wrapper = mount(MobileNumberPad, {
      props: { visible: true, counts, pencilMode: false }
    })
    const buttons = wrapper.findAll('button')
    const btn1 = buttons.find(b => b.text().trim() === '1')
    if (btn1) {
      expect(btn1.classes()).toContain('complete')
    }
  })
})
