import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserTestingSurvey from '@/components/UserTestingSurvey.vue'

describe('UserTestingSurvey', () => {
  const mockSurvey = {
    name: 'Test Survey',
    description: 'Please answer these questions',
    questions: [
      { id: 1, questionText: 'How fun was the puzzle?', responseType: 'RATING_1_5' },
      { id: 2, questionText: 'Would you play again?', responseType: 'YES_NO' },
      { id: 3, questionText: 'Any feedback?', responseType: 'TEXT' }
    ]
  }

  it('mounts without errors', () => {
    const wrapper = mount(UserTestingSurvey, {
      props: {
        survey: mockSurvey,
        sessionId: 'test-session',
        participantId: 'test-participant'
      }
    })
    expect(wrapper.element).toBeTruthy()
  })

  it('renders survey name', () => {
    const wrapper = mount(UserTestingSurvey, {
      props: {
        survey: mockSurvey,
        sessionId: 'test-session',
        participantId: 'test-participant'
      }
    })
    expect(wrapper.text()).toContain('Test Survey')
  })

  it('renders question text', () => {
    const wrapper = mount(UserTestingSurvey, {
      props: {
        survey: mockSurvey,
        sessionId: 'test-session',
        participantId: 'test-participant'
      }
    })
    expect(wrapper.text()).toContain('How fun was the puzzle')
  })

  it('shows navigation buttons', () => {
    const wrapper = mount(UserTestingSurvey, {
      props: {
        survey: mockSurvey,
        sessionId: 'test-session',
        participantId: 'test-participant'
      }
    })
    expect(wrapper.findAll('button').length).toBeGreaterThan(0)
  })
})
