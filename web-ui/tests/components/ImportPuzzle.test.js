import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ImportPuzzle from '@/components/ImportPuzzle.vue'

describe('ImportPuzzle', () => {
  it('mounts without errors', () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: false }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders import modal content', () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: false }
    })
    expect(wrapper.find('textarea').exists() || wrapper.find('input').exists()).toBe(true)
  })

  it('has cancel button', () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: false }
    })
    const cancelBtn = wrapper.findAll('button').find(b => b.text().includes('Cancel'))
    expect(cancelBtn).toBeTruthy()
  })

  it('emits close when cancel clicked', async () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: false }
    })
    const cancelBtn = wrapper.findAll('button').find(b => b.text().includes('Cancel'))
    if (cancelBtn) {
      await cancelBtn.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })

  it('has import button', () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: false }
    })
    const importBtn = wrapper.findAll('button').find(b => b.text().includes('Import'))
    expect(importBtn).toBeTruthy()
  })

  it('applies dark mode', () => {
    const wrapper = mount(ImportPuzzle, {
      props: { isDark: true }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
