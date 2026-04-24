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

  describe('cell selection with mixed input/candidate elements', () => {
    // Build a puzzle where some cells have values and others have candidates.
    // This tests the fix for the bug where focusCell used array position
    // instead of data-index to find elements, causing wrong cell selection.
    const mixedPuzzle =
      '5' + '.'.repeat(8) +   // row 0: cell 0 has value, 1-8 empty
      '.'.repeat(9) +         // row 1: all empty
      '.'.repeat(9) +         // row 2: all empty
      '.'.repeat(9) +         // row 3: all empty
      '.'.repeat(9) +         // row 4: all empty
      '.'.repeat(8) + '3' +   // row 5: cells 45-53 empty, cell 54 has value
      '.'.repeat(9) +         // row 6: all empty
      '.'.repeat(9) +         // row 7: all empty
      '.'.repeat(9)           // row 8: all empty

    const mixedGivenCells = new Set([0, 54])
    const mixedCandidates = {
      '1': [2, 5], '2': [1, 3], '3': [2, 7],
      '4': [5, 8], '5': [3, 6], '6': [4, 9],
      '7': [1, 8], '8': [2, 4], '9': [3, 7],
      '10': [5, 9], '11': [1, 6], '12': [3, 8],
      '45': [1, 9], '46': [2, 8], '47': [3, 7],
      '48': [4, 6], '49': [5, 7], '50': [1, 3],
      '51': [6, 9], '52': [2, 5], '53': [4, 8]
    }

    const propsWithCandidates = {
      ...defaultProps,
      puzzle: mixedPuzzle,
      givenCells: mixedGivenCells,
      candidates: mixedCandidates,
      showCandidates: true
    }

    it('emits select with correct index when clicking an empty cell', async () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })
      const cells = wrapper.findAll('.cell')

      // Click cell 1 (empty with candidates, after given cell 0)
      await cells[1].trigger('click')
      const selectEvents = wrapper.emitted('select')
      expect(selectEvents).toBeTruthy()
      const lastSelect = selectEvents[selectEvents.length - 1]
      expect(lastSelect).toEqual([1])
    })

    it('emits select with correct index when clicking a cell in later rows', async () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })
      const cells = wrapper.findAll('.cell')

      // Click cell 45 (row 5, col 0) - empty cell with candidates
      await cells[45].trigger('click')
      const selectEvents = wrapper.emitted('select')
      expect(selectEvents).toBeTruthy()
      const lastSelect = selectEvents[selectEvents.length - 1]
      expect(lastSelect).toEqual([45])
    })

    it('emits select with correct index when clicking a given cell', async () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })
      const cells = wrapper.findAll('.cell')

      // Click cell 54 (given value '3' at row 6, col 0)
      await cells[54].trigger('click')
      const selectEvents = wrapper.emitted('select')
      expect(selectEvents).toBeTruthy()
      const lastSelect = selectEvents[selectEvents.length - 1]
      expect(lastSelect).toEqual([54])
    })

    it('emits correct index for sequential clicks across mixed cells', async () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })
      const cells = wrapper.findAll('.cell')

      // Click sequence: given(0) -> empty(1) -> empty(2) -> given(54)
      await cells[0].trigger('click')
      await cells[1].trigger('click')
      await cells[2].trigger('click')
      await cells[54].trigger('click')

      const selectEvents = wrapper.emitted('select')
      expect(selectEvents).toHaveLength(4)
      expect(selectEvents[0]).toEqual([0])
      expect(selectEvents[1]).toEqual([1])
      expect(selectEvents[2]).toEqual([2])
      expect(selectEvents[3]).toEqual([54])
    })

    it('renders candidates div for empty cells and input for filled cells', () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })
      const cells = wrapper.findAll('.cell')

      // Cell 0 has value '5' -> should have input
      expect(cells[0].find('input').exists()).toBe(true)
      expect(cells[0].find('.candidates-grid').exists()).toBe(false)

      // Cell 1 is empty with candidates -> should have candidates-grid
      expect(cells[1].find('.candidates-grid').exists()).toBe(true)

      // Cell 54 has value '3' -> should have input
      expect(cells[54].find('input').exists()).toBe(true)
    })

    it('assigns correct data-index to all elements', () => {
      const wrapper = mount(SudokuGrid, { props: propsWithCandidates })

      // Check that inputs have correct data-index
      const inputs = wrapper.findAll('input')
      for (const input of inputs) {
        const idx = input.attributes('data-index')
        expect(idx).toBeDefined()
        expect(Number(idx)).toBeGreaterThanOrEqual(0)
        expect(Number(idx)).toBeLessThan(81)
        // The input value should match the puzzle at that index
        expect(input.attributes('value')).toBe(
          mixedPuzzle[Number(idx)] === '.' ? '' : mixedPuzzle[Number(idx)]
        )
      }
    })
  })

  describe('cell deselect', () => {
    it('deselects cell when tapping the already-selected cell', async () => {
      // Simulate: cell 5 is already selected by the parent
      const wrapper = mount(SudokuGrid, { props: { ...defaultProps, selectedCell: 5 } })
      const cells = wrapper.findAll('.cell')

      // Click same cell again — should deselect (emit -1)
      await cells[5].trigger('click')
      expect(wrapper.emitted('select')).toBeTruthy()
      expect(wrapper.emitted('select').at(-1)).toEqual([-1])
    })

    it('does not deselect when clicking a different cell', async () => {
      const wrapper = mount(SudokuGrid, { props: { ...defaultProps, selectedCell: 5 } })
      const cells = wrapper.findAll('.cell')

      // Click different cell
      await cells[10].trigger('click')
      expect(wrapper.emitted('select').at(-1)).toEqual([10])
    })
  })
})
