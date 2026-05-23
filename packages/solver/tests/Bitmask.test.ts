import { describe, it, expect } from 'vitest'
import {
  SIZE,
  REGION_SIZE,
  CELL_COUNT,
  SYMBOLS,
  MASKS,
  WILDCARD_PATTERN,
  bitCount,
  hasValue,
  lowestValue,
  maskToValues,
  valueToMask
} from '../src/Bitmask'

describe('Bitmask constants', () => {
  it('SIZE is 9', () => expect(SIZE).toBe(9))
  it('REGION_SIZE is 3', () => expect(REGION_SIZE).toBe(3))
  it('CELL_COUNT is 81', () => expect(CELL_COUNT).toBe(81))

  it('SYMBOLS has 10 entries', () => {
    expect(SYMBOLS).toHaveLength(10)
    expect(SYMBOLS[0]).toBe('.')
    expect(SYMBOLS[1]).toBe('1')
    expect(SYMBOLS[9]).toBe('9')
  })

  it('MASKS has 9 entries with correct bit positions', () => {
    expect(MASKS).toHaveLength(9)
    expect(MASKS[0]).toBe(1)           // 2^0
    expect(MASKS[1]).toBe(2)           // 2^1
    expect(MASKS[2]).toBe(4)           // 2^2
    expect(MASKS[8]).toBe(256)         // 2^8
  })

  it('WILDCARD_PATTERN is 0b111111111 (511)', () => {
    expect(WILDCARD_PATTERN).toBe(511)
    expect(bitCount(WILDCARD_PATTERN)).toBe(9)
  })
})

describe('bitCount', () => {
  it('counts 0 as 0', () => expect(bitCount(0)).toBe(0))
  it('counts single bit', () => expect(bitCount(1)).toBe(1))
  it('counts 7-bits (127)', () => expect(bitCount(127)).toBe(7))
  it('counts all 9 bits (511)', () => expect(bitCount(511)).toBe(9))
  it('counts 3 bits (21 = 10101)', () => expect(bitCount(21)).toBe(3))
  it('counts 5 bits (341 = 101010101)', () => expect(bitCount(341)).toBe(5))
})

describe('hasValue', () => {
  it('finds value 1 in mask 1', () => expect(hasValue(1, 1)).toBe(true))
  it('does not find value 2 in mask 1', () => expect(hasValue(1, 2)).toBe(false))
  it('finds value 9 in wildcard mask', () => expect(hasValue(511, 9)).toBe(true))
  it('finds all values 1-9 in wildcard', () => {
    for (let v = 1; v <= 9; v++) {
      expect(hasValue(511, v)).toBe(true)
    }
  })
  it('finds no values in empty mask', () => {
    for (let v = 1; v <= 9; v++) {
      expect(hasValue(0, v)).toBe(false)
    }
  })
})

describe('lowestValue', () => {
  it('returns 0 for empty mask', () => expect(lowestValue(0)).toBe(0))
  it('returns 1 for mask 1', () => expect(lowestValue(1)).toBe(1))
  it('returns 2 for mask 2', () => expect(lowestValue(2)).toBe(2))
  it('returns 1 for mask 3 (1|2)', () => expect(lowestValue(3)).toBe(1))
  it('returns 3 for mask 12 (4|8)', () => expect(lowestValue(12)).toBe(3))
  it('returns 9 for mask 256', () => expect(lowestValue(256)).toBe(9))
  it('returns 1 for wildcard (511)', () => expect(lowestValue(511)).toBe(1))
})

describe('maskToValues', () => {
  it('returns empty array for empty mask', () => {
    expect(maskToValues(0)).toEqual([])
  })
  it('returns [1] for mask 1', () => {
    expect(maskToValues(1)).toEqual([1])
  })
  it('returns [1, 2] for mask 3', () => {
    expect(maskToValues(3)).toEqual([1, 2])
  })
  it('returns [1, 3, 5, 7, 9] for mask 341', () => {
    expect(maskToValues(341)).toEqual([1, 3, 5, 7, 9])
  })
  it('returns all 1-9 for wildcard', () => {
    expect(maskToValues(511)).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9])
  })
  it('returns [9] for mask 256', () => {
    expect(maskToValues(256)).toEqual([9])
  })
})

describe('valueToMask', () => {
  it('returns 0 for value 0', () => expect(valueToMask(0)).toBe(0))
  it('returns 1 for value 1', () => expect(valueToMask(1)).toBe(1))
  it('returns 256 for value 9', () => expect(valueToMask(9)).toBe(256))
  it('round-trips: maskToValues(valueToMask(v)) === [v]', () => {
    for (let v = 1; v <= 9; v++) {
      expect(maskToValues(valueToMask(v))).toEqual([v])
    }
  })
  it('hasValue(valueToMask(v), v) is true', () => {
    for (let v = 1; v <= 9; v++) {
      expect(hasValue(valueToMask(v), v)).toBe(true)
    }
  })
})
