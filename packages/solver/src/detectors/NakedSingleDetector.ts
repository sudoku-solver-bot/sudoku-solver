import type { Board } from '../Board'
import { Coord } from '../Coord'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects naked singles — cells with only one remaining candidate.
 */
export class NakedSingleDetector implements TechniqueDetector {
  readonly technique = Technique.NAKED_SINGLE

  detect(board: Board): Hint | null {
    for (const coord of Coord.all) {
      if (board.isConfirmed(coord)) continue
      const candidates = board.candidateValues(coord)
      if (candidates.length === 1) {
        const value = candidates[0]
        // Build explanation
        const seen = new Set<number>()
        for (let i = 0; i < 9; i++) {
          seen.add(board.value(Coord.all[coord.row * 9 + i]))
          seen.add(board.value(Coord.all[i * 9 + coord.col]))
        }
        const boxRow = Math.floor(coord.row / 3) * 3
        const boxCol = Math.floor(coord.col / 3) * 3
        for (let r = boxRow; r < boxRow + 3; r++) {
          for (let c = boxCol; c < boxCol + 3; c++) {
            seen.add(board.value(Coord.all[r * 9 + c]))
          }
        }
        seen.delete(0)
        const missing = [1, 2, 3, 4, 5, 6, 7, 8, 9].filter(v => v !== value && !seen.has(v))
        return {
          coord,
          value,
          technique: Technique.NAKED_SINGLE,
          explanation: `Cell (${coord.row + 1}, ${coord.col + 1}) can only be ${value}! All other numbers ${missing.join(', ')} are already present in the row, column, or box.`
        }
      }
    }
    return null
  }
}
