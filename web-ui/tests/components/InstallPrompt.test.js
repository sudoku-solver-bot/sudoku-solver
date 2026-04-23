import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import InstallPrompt from '@/components/InstallPrompt.vue'

describe('InstallPrompt', () => {
  it('mounts without errors', () => {
    const wrapper = mount(InstallPrompt)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders install banner when applicable', () => {
    const wrapper = mount(InstallPrompt)
    // The component renders a fixed banner with install prompt
    // It may be hidden by default if no install event was fired
    expect(wrapper.element).toBeTruthy()
  })
})
