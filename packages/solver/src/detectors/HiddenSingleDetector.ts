import type { Board } from '../Board'
import { Coord } from '../Coord'
import { CoordGroup } from '../CoordGroup'
import type { Hint, TechniqueDetector } from '../HintTypes'
import { Technique } from '../HintTypes'

/**
 * Detects hidden singles — values that appear only once in a row, column, or region.
 */
export class HiddenSingleDetector implements TechniqueDetector {
  readonly technique = Technique.HIDDEN_SINGLE

  detect(board: Board): Hint | null {
    for (const coordGroup of CoordGroup.all) {
      const knownValues = new Set(coordGroup.coords.map(c => board.value(c)))

      // Count occurrences of each candidate in the group
      const candidateCounts = new Map<number, Coord[]>()

      for (const coord of coordGroup.coords) {
        if (board.isConfirmed(coord)) continue
        for (const candidate of board.candidateValues(coord)) {
          if (!candidateCounts.has(candidate)) {
            candidateCounts.set(candidate, [])
          }
          candidateCounts.get(candidate)!.push(coord)
        }
      }

      // Find candidates that appear only once
      for (const [value, coords] of candidateCounts) {
        if (coords.length === 1 && !knownValues.has(value)) {
          const coord = coords[0]
          // Determine group name
          const firstCoord = coordGroup.coords[0]
          const lastCoord = coordGroup.coords[coordGroup.coords.length - 1]
          let groupName: string
          if (firstCoord.row === lastCoord.row) {
            groupName = `row ${firstCoord.row + 1}`
          } else if (firstCoord.col === lastCoord.col) {
            groupName = `column ${firstCoord.col + 1}`
          } else {
            const regionRow = Math.floor(coord.row / 3) + 1
            const regionCol = Math.floor(coord.col / 3) + 1
            groupName = `region (${regionRow}, ${regionCol})`
          }

          return {
            coord,
            value,
            technique: Technique.HIDDEN_SINGLE,
            explanation: `Value ${value} appears only once in ${groupName}. Fill it in at (${coord.row + 1}, ${coord.col + 1})!`
          }
        }
      }
    }

    return null
  }
}
