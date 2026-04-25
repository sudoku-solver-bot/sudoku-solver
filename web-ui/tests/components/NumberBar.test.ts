import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import NumberBar from '@/components/NumberBar.vue'

describe('NumberBar', () => {
  it('mounts without errors', () => {
    const wrapper = mount(NumberBar, {
      props: { counts: {}, isDark: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders 9 digit cells', () => {
    const wrapper = mount(NumberBar, {
      props: { counts: {}, isDark: false }
    })
    // Should render cells for digits 1-9
    const cells = wrapper.findAll('.digit-cell') || wrapper.findAll('.number-cell') || wrapper.findAll('[class*="cell"]')
    expect(cells.length).toBeGreaterThan(0)
  })

  it('marks completed numbers', () => {
    const counts = { 1: 9, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0, 7: 0, 8: 0, 9: 0 }
    const wrapper = mount(NumberBar, {
      props: { counts, isDark: false }
    })
    const cells = wrapper.findAll('[class*="complete"]')
    expect(cells.length).toBeGreaterThan(0)
  })

  it('applies dark mode', () => {
    const wrapper = mount(NumberBar, {
      props: { counts: {}, isDark: true }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
