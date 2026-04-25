import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ToastNotification from '@/components/ToastNotification.vue'

describe('ToastNotification', () => {
  it('mounts without errors', () => {
    const wrapper = mount(ToastNotification, {
      props: { visible: true, type: 'info', title: 'Test', message: 'Hello', duration: 0 }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('shows title and message when visible', () => {
    const wrapper = mount(ToastNotification, {
      props: { visible: true, type: 'success', title: 'Success!', message: 'Puzzle solved', duration: 0 }
    })
    expect(wrapper.text()).toContain('Success!')
    expect(wrapper.text()).toContain('Puzzle solved')
  })

  it('renders different types', () => {
    const types = ['success', 'error', 'warning', 'info']
    for (const type of types) {
      const wrapper = mount(ToastNotification, {
        props: { visible: true, type, title: 'Test', message: 'msg', duration: 0 }
      })
      expect(wrapper.element).toBeTruthy()
    }
  })

  it('emits close when close button clicked', async () => {
    const wrapper = mount(ToastNotification, {
      props: { visible: true, type: 'info', title: 'Test', message: 'msg', duration: 0 }
    })
    const closeBtn = wrapper.find('.toast-close')
    await closeBtn.trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('shows retry button when showRetry is true', () => {
    const wrapper = mount(ToastNotification, {
      props: { visible: true, type: 'error', title: 'Error', message: 'Failed', showRetry: true, duration: 0 }
    })
    const retryBtn = wrapper.find('.toast-retry')
    expect(retryBtn.exists()).toBe(true)
    expect(retryBtn.text()).toContain('Try Again')
  })

  it('emits retry when retry button clicked', async () => {
    const wrapper = mount(ToastNotification, {
      props: { visible: true, type: 'error', title: 'Error', message: 'Failed', showRetry: true, duration: 0 }
    })
    const retryBtn = wrapper.find('.toast-retry')
    await retryBtn.trigger('click')
    expect(wrapper.emitted('retry')).toBeTruthy()
  })
})
