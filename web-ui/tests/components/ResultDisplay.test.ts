import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ResultDisplay from '@/components/ResultDisplay.vue'

describe('ResultDisplay', () => {
  it('displays success message with correct class', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Puzzle solved!',
        type: 'success',
        visible: true
      }
    })

    expect(wrapper.find('.result').classes()).toContain('success')
    expect(wrapper.text()).toContain('Puzzle solved!')
  })

  it('displays error message with correct class', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'No solution found',
        type: 'error',
        visible: true
      }
    })

    expect(wrapper.find('.result').classes()).toContain('error')
    expect(wrapper.text()).toContain('No solution found')
  })

  it('displays info message with correct class', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Loading...',
        type: 'info',
        visible: true
      }
    })

    expect(wrapper.find('.result').classes()).toContain('info')
    expect(wrapper.text()).toContain('Loading...')
  })

  it('shows difficulty badge with correct class for EASY', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Generated puzzle',
        type: 'success',
        visible: true,
        difficulty: 'EASY'
      }
    })

    expect(wrapper.find('.difficulty').exists()).toBe(true)
    expect(wrapper.find('.difficulty').classes()).toContain('easy')
    expect(wrapper.find('.difficulty').text()).toBe('EASY')
  })

  it('shows difficulty badge with correct class for MEDIUM', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Generated puzzle',
        type: 'success',
        visible: true,
        difficulty: 'MEDIUM'
      }
    })

    expect(wrapper.find('.difficulty').classes()).toContain('medium')
  })

  it('shows difficulty badge with correct class for HARD', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Generated puzzle',
        type: 'success',
        visible: true,
        difficulty: 'HARD'
      }
    })

    expect(wrapper.find('.difficulty').classes()).toContain('hard')
  })

  it('shows difficulty badge with correct class for EXPERT', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Generated puzzle',
        type: 'success',
        visible: true,
        difficulty: 'EXPERT'
      }
    })

    expect(wrapper.find('.difficulty').classes()).toContain('expert')
  })

  it('shows difficulty badge with correct class for MASTER', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Generated puzzle',
        type: 'success',
        visible: true,
        difficulty: 'MASTER'
      }
    })

    expect(wrapper.find('.difficulty').classes()).toContain('master')
  })

  it('displays techniques list when provided', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Puzzle solved!',
        type: 'success',
        visible: true,
        techniques: ['Naked Singles', 'Hidden Singles']
      }
    })

    expect(wrapper.find('.techniques').exists()).toBe(true)
    expect(wrapper.text()).toContain('Techniques used:')
    expect(wrapper.text()).toContain('Naked Singles')
    expect(wrapper.text()).toContain('Hidden Singles')
  })

  it('does not display techniques list when empty', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Puzzle solved!',
        type: 'success',
        visible: true,
        techniques: []
      }
    })

    expect(wrapper.find('.techniques').exists()).toBe(false)
  })

  it('does not render when visible is false', () => {
    const wrapper = mount(ResultDisplay, {
      props: {
        message: 'Should not see this',
        type: 'info',
        visible: false
      }
    })

    expect(wrapper.find('.result').exists()).toBe(false)
  })
})
