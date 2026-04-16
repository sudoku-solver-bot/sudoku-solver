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

// Undo/Redo API endpoints
export async function saveState(puzzle) {
  const response = await fetch(`${API_BASE}/v1/undo-redo/save`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle })
  })
  return response.json()
}

export async function undo() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/undo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  return response.json()
}

export async function redo() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/redo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  return response.json()
}

export async function getHistory() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/history`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  return response.json()
}

export async function fetchCandidates(puzzle) {
  const response = await fetch(`${API_BASE}/v1/candidates`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle })
  })
  return response.json()
}

// Tutorial API
export async function fetchTutorials() {
  const response = await fetch(`${API_BASE}/v1/tutorials`)
  return response.json()
}

export async function fetchTutorial(id) {
  const response = await fetch(`${API_BASE}/v1/tutorials/${id}`)
  return response.json()
}

export async function fetchTutorialBoard(id) {
  const response = await fetch(`${API_BASE}/v1/tutorials/${id}/board`)
  return response.json()
}

export async function completeTutorial(id) {
  const response = await fetch(`${API_BASE}/v1/tutorials/${id}/complete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  return response.json()
}

export async function fetchTutorialProgress() {
  const response = await fetch(`${API_BASE}/v1/tutorials/progress`)
  return response.json()
}
