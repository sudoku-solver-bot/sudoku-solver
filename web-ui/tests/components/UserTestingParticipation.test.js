import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserTestingParticipation from '@/components/UserTestingParticipation.vue'

describe('UserTestingParticipation', () => {
  it('mounts without errors', () => {
    const wrapper = mount(UserTestingParticipation)
    expect(wrapper.element).toBeTruthy()
  })

  it('renders participation form content', () => {
    const wrapper = mount(UserTestingParticipation)
    expect(wrapper.findAll('button').length + wrapper.findAll('input').length).toBeGreaterThan(0)
  })
})
