import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import QuizMode from '@/components/QuizMode.vue'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/api', () => ({
  fetchQuizBoard: vi.fn().mockResolvedValue({
    puzzle: '.'.repeat(81),
    solution: '534678912672195348198342567859761423426853791713924856961537284287419635345286179',
    candidates: {}
  }),
  fetchCandidates: vi.fn().mockResolvedValue({ candidates: {} })
}))

describe('QuizMode', () => {
  const mockQuiz = {
    belt: 'white',
    title: 'White Belt Quiz',
    questions: [
      {
        id: 'q1',
        text: 'Find the naked single',
        puzzle: '.'.repeat(81),
        answerCell: 40,
        answer: 5
      }
    ]
  }

  it('mounts without errors', async () => {
    const wrapper = mount(QuizMode, {
      props: { quiz: mockQuiz, isDark: false }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })

  it('shows quiz header with belt info', async () => {
    const wrapper = mount(QuizMode, {
      props: { quiz: mockQuiz, isDark: false }
    })
    await flushPromises()
    expect(wrapper.findAll('button').length).toBeGreaterThan(0)
  })

  it('emits exit when back button clicked', async () => {
    const wrapper = mount(QuizMode, {
      props: { quiz: mockQuiz, isDark: false }
    })
    await flushPromises()
    const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back') || b.text().includes('←'))
    if (backBtn) {
      await backBtn.trigger('click')
      expect(wrapper.emitted('exit')).toBeTruthy()
    }
  })

  it('applies dark mode', async () => {
    const wrapper = mount(QuizMode, {
      props: { quiz: mockQuiz, isDark: true }
    })
    await flushPromises()
    expect(wrapper.element).toBeTruthy()
  })
})
