import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BeltCertificate from '@/components/BeltCertificate.vue'

vi.mock('@/certificate-image', () => ({
  generateCertificateImage: vi.fn().mockReturnValue(null),
  downloadCertificateImage: vi.fn()
}))

describe('BeltCertificate', () => {
  const defaultProps = {
    technique: 'Naked Single',
    beltName: 'White Belt',
    beltEmoji: '⬜',
    beltColor: '#ffffff'
  }

  it('mounts without errors', () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    expect(wrapper.find('.certificate-overlay').exists()).toBe(true)
  })

  it('displays technique name', () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    expect(wrapper.text()).toContain('Naked Single')
  })

  it('displays belt name', () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    expect(wrapper.text()).toContain('White Belt')
  })

  it('shows done button', () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    const closeBtn = wrapper.findAll('button').find(b => b.text().includes('Done'))
    expect(closeBtn).toBeTruthy()
  })

  it('emits close when Done button clicked', async () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    const closeBtn = wrapper.findAll('button').find(b => b.text().includes('Done'))
    await closeBtn.trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('emits close when overlay clicked', async () => {
    const wrapper = mount(BeltCertificate, { props: defaultProps })
    await wrapper.find('.certificate-overlay').trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
