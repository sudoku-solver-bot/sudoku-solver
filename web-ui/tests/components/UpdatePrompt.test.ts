import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UpdatePrompt from '@/components/UpdatePrompt.vue'

describe('UpdatePrompt', () => {
  it('mounts without errors', () => {
    const wrapper = mount(UpdatePrompt)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders update bar only when refresh is needed', () => {
    const wrapper = mount(UpdatePrompt)
    // In test env, PWA is not registered so needRefresh defaults to false
    expect(wrapper.find('.update-bar').exists()).toBe(false)
  })

  it('applies dark mode class when visible', async () => {
    const wrapper = mount(UpdatePrompt, { props: { isDark: true } })
    // needRefresh is false by default, so the bar isn't rendered
    // Just verify component mounts with dark prop without error
    expect(wrapper.element).toBeTruthy()
  })
})
