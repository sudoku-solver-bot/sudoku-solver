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
  // Backend expects digits only: 1-9 for filled, 0 for empty
  const normalized = puzzle.replace(/\./g, '0')
  const response = await fetch(`${API_BASE}/hint`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle: normalized })
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
  if (!response.ok) return { error: 'Undo/redo not available' }
  return response.json().catch(() => ({}))
}

export async function undo() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/undo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { error: 'Undo not available' }
  return response.json().catch(() => ({}))
}

export async function redo() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/redo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { error: 'Redo not available' }
  return response.json().catch(() => ({}))
}

export async function getHistory() {
  const response = await fetch(`${API_BASE}/v1/undo-redo/history`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { canUndo: false, canRedo: false, undoCount: 0, redoCount: 0 }
  return response.json().catch(() => ({ canUndo: false, canRedo: false, undoCount: 0, redoCount: 0 }))
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

// Daily Challenge API
export async function fetchDailyChallenge(date = null) {
  const url = date ? `${API_BASE}/v1/daily/${date}` : `${API_BASE}/v1/daily`
  const response = await fetch(url)
  return response.json()
}

// Quiz API
export async function fetchQuizzes() {
  const response = await fetch(`${API_BASE}/v1/tutorials/quizzes`)
  return response.json()
}

export async function fetchQuizByBelt(belt) {
  const response = await fetch(`${API_BASE}/v1/tutorials/quizzes/${belt}`)
  return response.json()
}

export async function fetchQuizBoard(belt) {
  const response = await fetch(`${API_BASE}/v1/tutorials/quizzes/${belt}/board`)
  return response.json()
}

// Practice API
export async function fetchAllPracticeSets() {
  const response = await fetch(`${API_BASE}/v1/tutorials/practice`)
  return response.json()
}

export async function fetchPracticePuzzles(tutorialId) {
  const response = await fetch(`${API_BASE}/v1/tutorials/practice/${tutorialId}`)
  return response.json()
}

export async function fetchPracticeBoard(tutorialId, puzzleId) {
  const response = await fetch(`${API_BASE}/v1/tutorials/practice/${tutorialId}/${puzzleId}/board`)
  return response.json()
}
