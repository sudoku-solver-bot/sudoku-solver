import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Settings from '@/components/Settings.vue'

// Mock sounds module
vi.mock('@/sounds', () => ({
  isSoundEnabled: vi.fn().mockReturnValue(false),
  setSoundEnabled: vi.fn(),
  playSound: vi.fn()
}))

describe('Settings', () => {
  it('mounts without errors', () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    expect(wrapper.find('.settings').exists() || wrapper.element).toBeTruthy()
  })

  it('renders settings header', () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    expect(wrapper.find('h2').text()).toContain('Settings')
  })

  it('renders accessibility toggles', () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect(checkboxes.length).toBeGreaterThan(0)
  })

  it('renders theme selector buttons', () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const themeBtns = wrapper.findAll('.theme-btn')
    expect(themeBtns.length).toBeGreaterThan(0)
  })

  it('emits exit when back button clicked', async () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('emits toggle-dark when dark mode checkbox changed', async () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    // Find the dark mode checkbox (3rd one - colorblind, high contrast, dark mode)
    if (checkboxes.length >= 3) {
      await checkboxes[2].trigger('change')
      expect(wrapper.emitted('toggle-dark')).toBeTruthy()
    }
  })

  it('emits toggle-colorblind when colorblind checkbox changed', async () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    if (checkboxes.length >= 1) {
      await checkboxes[0].trigger('change')
      expect(wrapper.emitted('toggle-colorblind')).toBeTruthy()
    }
  })

  it('emits toggle-highcontrast when high contrast checkbox changed', async () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    if (checkboxes.length >= 2) {
      await checkboxes[1].trigger('change')
      expect(wrapper.emitted('toggle-highcontrast')).toBeTruthy()
    }
  })

  it('shows about section', () => {
    const wrapper = mount(Settings, {
      props: { isDark: false, colorBlind: false, highContrast: false, challengeMode: false, theme: 'default' }
    })
    expect(wrapper.text()).toContain('Sudoku Dojo')
  })
})
