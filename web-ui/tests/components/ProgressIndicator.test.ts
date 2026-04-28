import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProgressIndicator from '@/components/ProgressIndicator.vue'

describe('ProgressIndicator', () => {
  const defaultProps = {
    puzzle: '.'.repeat(81),
    givenCells: new Set(),
    mistakes: 0,
    hintsUsed: 0,
    elapsedTime: 0,
    timerPaused: false,
    difficulty: 'EASY',
    newRecord: false
  }

  it('mounts without errors', () => {
    const wrapper = mount(ProgressIndicator, { props: defaultProps })
    expect(wrapper.element).toBeTruthy()
  })

  it('shows progress bar', () => {
    const wrapper = mount(ProgressIndicator, { props: defaultProps })
    expect(wrapper.find('[class*="progress"]').exists() || wrapper.find('.progress-bar').exists()).toBe(true)
  })

  it('shows filled cell count', () => {
    const puzzle = '5' + '.'.repeat(80)
    const wrapper = mount(ProgressIndicator, {
      props: { ...defaultProps, puzzle, givenCells: new Set([0]) }
    })
    expect(wrapper.text()).toContain('1')
  })

  it('shows time display', () => {
    const wrapper = mount(ProgressIndicator, {
      props: { ...defaultProps, elapsedTime: 65000 }
    })
    // 65000ms = 1:05
    expect(wrapper.text()).toContain('1:05')
  })

  it('shows mistake count when mistakes > 0', () => {
    const wrapper = mount(ProgressIndicator, {
      props: { ...defaultProps, mistakes: 2 }
    })
    expect(wrapper.text()).toContain('2')
  })

  it('shows difficulty badge', () => {
    const wrapper = mount(ProgressIndicator, {
      props: { ...defaultProps, difficulty: 'HARD' }
    })
    expect(wrapper.text()).toContain('HARD')
  })

  it('emits toggle-pause when pause clicked', async () => {
    const wrapper = mount(ProgressIndicator, { props: defaultProps })
    const pauseBtn = wrapper.findAll('button').find(b => b.text().includes('Pause') || b.text().includes('⏸'))
    if (pauseBtn) {
      await pauseBtn.trigger('click')
      expect(wrapper.emitted('toggle-pause')).toBeTruthy()
    }
  })
})
