import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import HintModal from '@/components/HintModal.vue'

describe('HintModal', () => {
  it('mounts without errors when visible with hint', () => {
    const wrapper = mount(HintModal, {
      props: {
        visible: true,
        hint: { row: 0, col: 0, value: 5, technique: 'NakedSingle' },
        totalHints: 3
      }
    })
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
  })

  it('does not render when not visible', () => {
    const wrapper = mount(HintModal, {
      props: { visible: false, hint: null, totalHints: 0 }
    })
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
  })

  it('shows hint value when hint provided', () => {
    const wrapper = mount(HintModal, {
      props: {
        visible: true,
        hint: { row: 0, col: 0, value: 5, technique: 'NakedSingle' },
        totalHints: 1
      }
    })
    expect(wrapper.text()).toContain('5')
  })

  it('shows hint count', () => {
    const wrapper = mount(HintModal, {
      props: {
        visible: true,
        hint: { row: 0, col: 0, value: 5, technique: 'NakedSingle' },
        totalHints: 3
      }
    })
    expect(wrapper.text()).toContain('3')
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(HintModal, {
      props: {
        visible: true,
        hint: { row: 0, col: 0, value: 5, technique: 'NakedSingle' },
        totalHints: 1
      }
    })
    const closeBtn = wrapper.find('.hint-button')
    await closeBtn.trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('shows no hint message when hint is null', () => {
    const wrapper = mount(HintModal, {
      props: {
        visible: true,
        hint: null,
        totalHints: 0
      }
    })
    expect(wrapper.text()).toContain('No hint available')
  })
})
