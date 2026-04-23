import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SudokuGrid from '@/components/SudokuGrid.vue'

describe('SudokuGrid', () => {
  const defaultProps = {
    puzzle: '.'.repeat(81),
    givenCells: new Set(),
    solvedCells: new Set(),
    selectedCell: -1,
    isDark: false,
    candidates: {},
    showCandidates: false
  }

  it('renders 81 cells', () => {
    const wrapper = mount(SudokuGrid, { props: defaultProps })
    expect(wrapper.findAll('.cell')).toHaveLength(81)
  })

  it('emits update event on valid input', async () => {
    const wrapper = mount(SudokuGrid, { props: defaultProps })
    const input = wrapper.findAll('input')[0]
    await input.setValue('5')
    expect(wrapper.emitted('update')).toBeTruthy()
    expect(wrapper.emitted('update')[0]).toEqual([0, '5'])
  })

  it('rejects invalid input (non-1-9)', async () => {
    const wrapper = mount(SudokuGrid, { props: defaultProps })
    const input = wrapper.findAll('input')[0]
    await input.setValue('0')
    expect(wrapper.emitted('update')).toBeTruthy()
    expect(wrapper.emitted('update')[0]).toEqual([0, ''])
  })

  it('displays given cells with "given" class', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const givenCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: { ...defaultProps, puzzle, givenCells }
    })
    const firstCell = wrapper.findAll('.cell')[0]
    expect(firstCell.classes()).toContain('given')
  })

  it('displays solved cells with "solved" class', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const solvedCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: { ...defaultProps, puzzle, solvedCells }
    })
    const firstCell = wrapper.findAll('.cell')[0]
    expect(firstCell.classes()).toContain('solved')
  })

  it('marks correct cells with border-right class', () => {
    const wrapper = mount(SudokuGrid, { props: defaultProps })
    const cells = wrapper.findAll('.cell')
    expect(cells[2].classes()).toContain('border-right')
    expect(cells[5].classes()).toContain('border-right')
    expect(cells[8].classes()).not.toContain('border-right')
  })

  it('marks correct cells with border-bottom class', () => {
    const wrapper = mount(SudokuGrid, { props: defaultProps })
    const cells = wrapper.findAll('.cell')
    expect(cells[18].classes()).toContain('border-bottom')
    expect(cells[45].classes()).toContain('border-bottom')
    expect(cells[0].classes()).not.toContain('border-bottom')
  })

  it('has readonly attribute for given cells', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const givenCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: { ...defaultProps, puzzle, givenCells }
    })
    const firstInput = wrapper.findAll('input')[0]
    expect(firstInput.attributes('readonly')).toBeDefined()
  })
})
