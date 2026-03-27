import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SudokuGrid from '@/components/SudokuGrid.vue'

describe('SudokuGrid', () => {
  it('renders 81 cells', () => {
    const puzzle = '.'.repeat(81)
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells: new Set()
      }
    })

    expect(wrapper.findAll('.cell')).toHaveLength(81)
    expect(wrapper.findAll('input')).toHaveLength(81)
  })

  it('emits update event on valid input', async () => {
    const puzzle = '.'.repeat(81)
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells: new Set()
      }
    })

    const input = wrapper.findAll('input')[0]
    await input.setValue('5')

    expect(wrapper.emitted('update')).toBeTruthy()
    expect(wrapper.emitted('update')[0]).toEqual([0, '5'])
  })

  it('rejects invalid input (non-1-9)', async () => {
    const puzzle = '.'.repeat(81)
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells: new Set()
      }
    })

    const input = wrapper.findAll('input')[0]
    await input.setValue('0')

    expect(wrapper.emitted('update')).toBeTruthy()
    expect(wrapper.emitted('update')[0]).toEqual([0, ''])
  })

  it('displays given cells with "given" class', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const givenCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells,
        solvedCells: new Set()
      }
    })

    const firstCell = wrapper.findAll('.cell')[0]
    expect(firstCell.classes()).toContain('given')
  })

  it('displays solved cells with "solved" class', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const solvedCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells
      }
    })

    const firstCell = wrapper.findAll('.cell')[0]
    expect(firstCell.classes()).toContain('solved')
  })

  it('marks correct cells with border-right class', () => {
    const puzzle = '.'.repeat(81)
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells: new Set()
      }
    })

    const cells = wrapper.findAll('.cell')
    // Columns 3, 6 (0-indexed: 2, 5) should have border-right, but not column 9 (8)
    expect(cells[2].classes()).toContain('border-right')
    expect(cells[5].classes()).toContain('border-right')
    expect(cells[8].classes()).not.toContain('border-right')
  })

  it('marks correct cells with border-bottom class', () => {
    const puzzle = '.'.repeat(81)
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells: new Set(),
        solvedCells: new Set()
      }
    })

    const cells = wrapper.findAll('.cell')
    // Rows 3, 6 (0-indexed: 2, 5) should have border-bottom
    // Cell at row 3, col 1 = index 18 (3 * 9 + 0)
    // Cell at row 6, col 1 = index 45 (6 * 9 + 0)
    expect(cells[18].classes()).toContain('border-bottom')
    expect(cells[45].classes()).toContain('border-bottom')
    expect(cells[0].classes()).not.toContain('border-bottom')
  })

  it('has readonly attribute for given cells', () => {
    const puzzle = '5.......' + '.'.repeat(73)
    const givenCells = new Set([0])
    const wrapper = mount(SudokuGrid, {
      props: {
        puzzle,
        givenCells,
        solvedCells: new Set()
      }
    })

    const firstInput = wrapper.findAll('input')[0]
    expect(firstInput.attributes('readonly')).toBeDefined()
  })
})
