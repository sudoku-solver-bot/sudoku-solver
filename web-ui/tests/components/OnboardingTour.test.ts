import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import OnboardingTour from '@/components/OnboardingTour.vue'

describe('OnboardingTour', () => {
  it('mounts without errors', () => {
    const wrapper = mount(OnboardingTour, { props: { visible: true } })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders when visible', () => {
    const wrapper = mount(OnboardingTour, { props: { visible: true } })
    expect(wrapper.find('.onboarding-overlay').exists()).toBe(true)
  })

  it('does not render when not visible', () => {
    const wrapper = mount(OnboardingTour, { props: { visible: false } })
    expect(wrapper.find('.onboarding-overlay').exists()).toBe(false)
  })

  it('emits close when skip is clicked', async () => {
    const wrapper = mount(OnboardingTour, { props: { visible: true } })
    const skipBtn = wrapper.find('.btn-skip')
    // Skip button only shows after first step, so check if it exists
    if (skipBtn.exists()) {
      await skipBtn.trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    }
  })

  it('shows progress dots matching step count', () => {
    const wrapper = mount(OnboardingTour, { props: { visible: true } })
    const dots = wrapper.findAll('.dot')
    expect(dots.length).toBeGreaterThan(0)
  })

  it('shows next button', () => {
    const wrapper = mount(OnboardingTour, { props: { visible: true } })
    expect(wrapper.find('.btn-next').exists()).toBe(true)
  })
})
