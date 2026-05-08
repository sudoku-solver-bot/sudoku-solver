import { describe, it, expect } from 'vitest'
import { CoordGroup } from '@/solver/CoordGroup'
import { Coord } from '@/solver/Coord'

describe('CoordGroup', () => {
  describe('vertical (columns)', () => {
    it('has 9 column groups', () => {
      expect(CoordGroup.vertical).toHaveLength(9)
    })

    it('each column group has 9 cells', () => {
      for (const group of CoordGroup.vertical) {
        expect(group.coords).toHaveLength(9)
      }
    })

    it('column 0 contains cells 0, 9, 18, ..., 72', () => {
      const col0 = CoordGroup.vertical[0].coords
      const expected = [0, 9, 18, 27, 36, 45, 54, 63, 72]
      expect(col0.map(c => c.index)).toEqual(expected)
    })

    it('all cells in column c have col === c', () => {
      for (let c = 0; c < 9; c++) {
        for (const coord of CoordGroup.vertical[c].coords) {
          expect(coord.col).toBe(c)
        }
      }
    })
  })

  describe('horizontal (rows)', () => {
    it('has 9 row groups', () => {
      expect(CoordGroup.horizontal).toHaveLength(9)
    })

    it('each row group has 9 cells', () => {
      for (const group of CoordGroup.horizontal) {
        expect(group.coords).toHaveLength(9)
      }
    })

    it('row 0 contains cells 0-8', () => {
      const row0 = CoordGroup.horizontal[0].coords
      const expected = [0, 1, 2, 3, 4, 5, 6, 7, 8]
      expect(row0.map(c => c.index)).toEqual(expected)
    })

    it('all cells in row r have row === r', () => {
      for (let r = 0; r < 9; r++) {
        for (const coord of CoordGroup.horizontal[r].coords) {
          expect(coord.row).toBe(r)
        }
      }
    })
  })

  describe('region (3×3 boxes)', () => {
    it('has 9 region groups', () => {
      expect(CoordGroup.region).toHaveLength(9)
    })

    it('each region group has 9 cells', () => {
      for (const group of CoordGroup.region) {
        expect(group.coords).toHaveLength(9)
      }
    })

    it('region 0 (top-left) contains cells 0,1,2,9,10,11,18,19,20', () => {
      const r0 = CoordGroup.region[0].coords
      const expected = [0, 1, 2, 9, 10, 11, 18, 19, 20]
      expect(r0.map(c => c.index)).toEqual(expected)
    })

    it('every cell in a region has the same region property', () => {
      for (let r = 0; r < 9; r++) {
        for (const coord of CoordGroup.region[r].coords) {
          expect(coord.region).toBe(r)
        }
      }
    })
  })

  describe('all', () => {
    it('has 27 groups (9 rows + 9 cols + 9 regions)', () => {
      expect(CoordGroup.all).toHaveLength(27)
    })

    it('first 9 are vertical (columns)', () => {
      for (let c = 0; c < 9; c++) {
        expect(CoordGroup.all[c]).toBe(CoordGroup.vertical[c])
      }
    })

    it('next 9 are horizontal (rows)', () => {
      for (let r = 0; r < 9; r++) {
        expect(CoordGroup.all[9 + r]).toBe(CoordGroup.horizontal[r])
      }
    })

    it('last 9 are regions', () => {
      for (let r = 0; r < 9; r++) {
        expect(CoordGroup.all[18 + r]).toBe(CoordGroup.region[r])
      }
    })
  })

  describe('no duplicate cells within groups', () => {
    it('no row group has duplicate cells', () => {
      for (const group of CoordGroup.horizontal) {
        const indices = group.coords.map(c => c.index)
        expect(new Set(indices).size).toBe(9)
      }
    })

    it('no column group has duplicate cells', () => {
      for (const group of CoordGroup.vertical) {
        const indices = group.coords.map(c => c.index)
        expect(new Set(indices).size).toBe(9)
      }
    })

    it('no region group has duplicate cells', () => {
      for (const group of CoordGroup.region) {
        const indices = group.coords.map(c => c.index)
        expect(new Set(indices).size).toBe(9)
      }
    })
  })
})
