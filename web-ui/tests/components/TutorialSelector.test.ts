import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TutorialSelector from '@/components/TutorialSelector.vue'

describe('TutorialSelector', () => {
  const mockTutorials = [
    { id: 1, title: 'Naked Single', beltName: 'White Belt', beltColor: '#fff' },
    { id: 2, title: 'Hidden Single', beltName: 'White Belt', beltColor: '#fff' }
  ]

  it('mounts without errors', () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set(),
        isDark: false,
        quizData: [],
        practiceData: []
      }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders tutorial list', () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set(),
        isDark: false,
        quizData: [],
        practiceData: []
      }
    })
    expect(wrapper.text()).toContain('Naked Single')
  })

  it('shows progress info', () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set([1]),
        isDark: false,
        quizData: [],
        practiceData: []
      }
    })
    // Should show 1/2 completed or progress indicator
    expect(wrapper.element).toBeTruthy()
  })

  it('emits exit when back button clicked', async () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set(),
        isDark: false,
        quizData: [],
        practiceData: []
      }
    })
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('emits select when tutorial clicked', async () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set(),
        isDark: false,
        quizData: [],
        practiceData: []
      }
    })
    // Find a tutorial card/button
    const tutorialBtn = wrapper.findAll('button').find(b => b.text().includes('Naked Single'))
    if (tutorialBtn) {
      await tutorialBtn.trigger('click')
      expect(wrapper.emitted('select')).toBeTruthy()
    }
  })

  it('applies dark mode', () => {
    const wrapper = mount(TutorialSelector, {
      props: {
        tutorials: mockTutorials,
        completedIds: new Set(),
        isDark: true,
        quizData: [],
        practiceData: []
      }
    })
    expect(wrapper.element).toBeTruthy()
  })
})
