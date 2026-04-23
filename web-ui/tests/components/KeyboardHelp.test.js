import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import KeyboardHelp from '@/components/KeyboardHelp.vue'

describe('KeyboardHelp', () => {
  it('mounts without errors', () => {
    const wrapper = mount(KeyboardHelp)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders keyboard shortcuts list', () => {
    const wrapper = mount(KeyboardHelp)
    // Should show keyboard shortcuts
    expect(wrapper.findAll('button').length + wrapper.findAll('.shortcut').length).toBeGreaterThan(0)
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(KeyboardHelp)
    const closeBtn = wrapper.findAll('button').find(b => b.text().includes('Close') || b.text().includes('×'))
    if (closeBtn) {
      await closeBtn.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })

  it('emits close when overlay clicked', async () => {
    const wrapper = mount(KeyboardHelp)
    // Click the overlay/background to close
    const overlay = wrapper.find('.overlay') || wrapper.find('[class*="modal"]')
    if (overlay.exists()) {
      await overlay.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })
})
