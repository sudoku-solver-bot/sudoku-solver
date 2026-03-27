import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ControlPanel from '@/components/ControlPanel.vue'

describe('ControlPanel', () => {
  it('emits solve event when Solve button is clicked', async () => {
    const wrapper = mount(ControlPanel)

    const solveButton = wrapper.find('.btn-primary')
    await solveButton.trigger('click')

    expect(wrapper.emitted('solve')).toBeTruthy()
    expect(wrapper.emitted('solve')).toHaveLength(1)
  })

  it('emits clear event when Clear button is clicked', async () => {
    const wrapper = mount(ControlPanel)

    const clearButton = wrapper.find('.btn-warning')
    await clearButton.trigger('click')

    expect(wrapper.emitted('clear')).toBeTruthy()
    expect(wrapper.emitted('clear')).toHaveLength(1)
  })

  it('emits generate event with EASY difficulty', async () => {
    const wrapper = mount(ControlPanel)

    const buttons = wrapper.findAll('.btn-secondary')
    await buttons[0].trigger('click')

    expect(wrapper.emitted('generate')).toBeTruthy()
    expect(wrapper.emitted('generate')[0]).toEqual(['EASY'])
  })

  it('emits generate event with MEDIUM difficulty', async () => {
    const wrapper = mount(ControlPanel)

    const buttons = wrapper.findAll('.btn-secondary')
    await buttons[1].trigger('click')

    expect(wrapper.emitted('generate')).toBeTruthy()
    expect(wrapper.emitted('generate')[0]).toEqual(['MEDIUM'])
  })

  it('emits generate event with HARD difficulty', async () => {
    const wrapper = mount(ControlPanel)

    const buttons = wrapper.findAll('.btn-secondary')
    await buttons[2].trigger('click')

    expect(wrapper.emitted('generate')).toBeTruthy()
    expect(wrapper.emitted('generate')[0]).toEqual(['HARD'])
  })

  it('emits generate event with EXPERT difficulty', async () => {
    const wrapper = mount(ControlPanel)

    const buttons = wrapper.findAll('.btn-info')
    await buttons[0].trigger('click')

    expect(wrapper.emitted('generate')).toBeTruthy()
    expect(wrapper.emitted('generate')[0]).toEqual(['EXPERT'])
  })

  it('emits hint event when Get Hint button is clicked', async () => {
    const wrapper = mount(ControlPanel)

    const hintButton = wrapper.find('.btn-full')
    await hintButton.trigger('click')

    expect(wrapper.emitted('hint')).toBeTruthy()
    expect(wrapper.emitted('hint')).toHaveLength(1)
  })

  it('renders all 7 buttons', () => {
    const wrapper = mount(ControlPanel)

    expect(wrapper.findAll('button')).toHaveLength(7)
  })
})
