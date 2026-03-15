const API_BASE = '/api'

export async function solvePuzzle(puzzle, includeMetrics = false) {
  const response = await fetch(`${API_BASE}/solve`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle, includeMetrics })
  })
  return response.json()
}

export async function generatePuzzle(difficulty = 'MEDIUM') {
  const response = await fetch(`${API_BASE}/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ difficulty })
  })
  return response.json()
}

export async function getHintForPuzzle(puzzle) {
  const response = await fetch(`${API_BASE}/hint`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle })
  })
  return response.json()
}

export async function validatePuzzle(puzzle, checkUniqueness = true) {
  const response = await fetch(`${API_BASE}/validate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle, checkUniqueness })
  })
  return response.json()
}
