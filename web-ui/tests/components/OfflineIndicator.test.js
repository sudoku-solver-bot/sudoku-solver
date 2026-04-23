import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import OfflineIndicator from '@/components/OfflineIndicator.vue'

describe('OfflineIndicator', () => {
  it('mounts without errors', () => {
    const wrapper = mount(OfflineIndicator)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders without crashing', () => {
    // Component renders based on online/offline status
    // In test environment, navigator.onLine is typically true
    const wrapper = mount(OfflineIndicator)
    expect(wrapper.vm).toBeTruthy()
  })
})
