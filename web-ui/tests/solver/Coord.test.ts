import { describe, it, expect } from 'vitest'
import { Coord } from '@/solver/Coord'

describe('Coord', () => {
  describe('static all', () => {
    it('has exactly 81 entries', () => {
      expect(Coord.all).toHaveLength(81)
    })

    it('is frozen (immutable)', () => {
      expect(Object.isFrozen(Coord.all)).toBe(true)
    })
  })

  describe('index mapping', () => {
    it('row 0, col 0 maps to index 0', () => {
      expect(Coord.all[0].row).toBe(0)
      expect(Coord.all[0].col).toBe(0)
      expect(Coord.all[0].index).toBe(0)
    })

    it('row 0, col 8 maps to index 8', () => {
      expect(Coord.all[8].row).toBe(0)
      expect(Coord.all[8].col).toBe(8)
      expect(Coord.all[8].index).toBe(8)
    })

    it('row 1, col 0 maps to index 9', () => {
      expect(Coord.all[9].row).toBe(1)
      expect(Coord.all[9].col).toBe(0)
      expect(Coord.all[9].index).toBe(9)
    })

    it('row 8, col 8 maps to index 80', () => {
      expect(Coord.all[80].row).toBe(8)
      expect(Coord.all[80].col).toBe(8)
      expect(Coord.all[80].index).toBe(80)
    })

    it('each coord index matches its position in the array', () => {
      for (let i = 0; i < 81; i++) {
        expect(Coord.all[i].index).toBe(i)
      }
    })
  })

  describe('region mapping', () => {
    it('top-left cells (rows 0-2, cols 0-2) are region 0', () => {
      expect(Coord.all[0].region).toBe(0)
      expect(Coord.all[1].region).toBe(0)
      expect(Coord.all[10].region).toBe(0)
      expect(Coord.all[20].region).toBe(0)
    })

    it('top-middle cells (rows 0-2, cols 3-5) are region 1', () => {
      expect(Coord.all[3].region).toBe(1)
      expect(Coord.all[13].region).toBe(1)
    })

    it('top-right cells (rows 0-2, cols 6-8) are region 2', () => {
      expect(Coord.all[6].region).toBe(2)
      expect(Coord.all[17].region).toBe(2)
    })

    it('middle-left cells (rows 3-5, cols 0-2) are region 3', () => {
      expect(Coord.all[27].region).toBe(3)
      expect(Coord.all[36].region).toBe(3)
    })

    it('center cells (rows 3-5, cols 3-5) are region 4', () => {
      expect(Coord.all[30].region).toBe(4)
      expect(Coord.all[40].region).toBe(4)
    })

    it('middle-right cells (rows 3-5, cols 6-8) are region 5', () => {
      expect(Coord.all[33].region).toBe(5)
      expect(Coord.all[44].region).toBe(5)
    })

    it('bottom-left cells (rows 6-8, cols 0-2) are region 6', () => {
      expect(Coord.all[54].region).toBe(6)
      expect(Coord.all[63].region).toBe(6)
    })

    it('bottom-middle cells (rows 6-8, cols 3-5) are region 7', () => {
      expect(Coord.all[57].region).toBe(7)
      expect(Coord.all[67].region).toBe(7)
    })

    it('bottom-right cells (rows 6-8, cols 6-8) are region 8', () => {
      expect(Coord.all[60].region).toBe(8)
      expect(Coord.all[80].region).toBe(8)
    })

    it('all 9 cells in region 0 have region === 0', () => {
      const region0Indices = [0, 1, 2, 9, 10, 11, 18, 19, 20]
      for (const i of region0Indices) {
        expect(Coord.all[i].region).toBe(0)
      }
    })
  })

  describe('singleton', () => {
    it('Coord.all returns the same array on repeat calls', () => {
      const a = Coord.all
      const b = Coord.all
      expect(a).toBe(b) // same reference, cached
    })
  })
})
