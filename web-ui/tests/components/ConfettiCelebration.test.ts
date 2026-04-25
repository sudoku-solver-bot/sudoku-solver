import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfettiCelebration from '@/components/ConfettiCelebration.vue'

describe('ConfettiCelebration', () => {
  it('mounts without errors when visible', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.find('.confetti-container').exists()).toBe(true)
  })

  it('does not render when not visible', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: false, time: '1:30' }
    })
    expect(wrapper.find('.confetti-container').exists()).toBe(false)
  })

  it('displays celebration text', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.text()).toContain('Puzzle Complete!')
  })

  it('shows time when provided', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '2:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.text()).toContain('2:30')
  })

  it('shows perfect message when no mistakes', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.text()).toContain('Perfect!')
  })

  it('shows mistake count when mistakes > 0', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 2, hints: 0 }
    })
    expect(wrapper.text()).toContain('2 mistakes')
  })

  it('shows no hints message when hints is 0', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.text()).toContain('No hints')
  })

  it('shows hint count when hints > 0', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 3 }
    })
    expect(wrapper.text()).toContain('3 hints')
  })

  it('has continue button', () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    expect(wrapper.find('.celebration-btn').exists()).toBe(true)
  })

  it('emits done when continue button clicked', async () => {
    const wrapper = mount(ConfettiCelebration, {
      props: { visible: true, time: '1:30', mistakes: 0, hints: 0 }
    })
    await wrapper.find('.celebration-btn').trigger('click')
    expect(wrapper.emitted('done')).toBeTruthy()
  })
})
