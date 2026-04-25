const API_BASE = '/api'

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function apiPost(url: string, body: Record<string, unknown>): Promise<any> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  return response.json()
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function apiGet(url: string): Promise<any> {
  const response = await fetch(url)
  return response.json()
}

// Solve
export async function solvePuzzle(puzzle: string, includeMetrics = false): Promise<unknown> {
  return apiPost(`${API_BASE}/solve`, { puzzle, includeMetrics })
}

// Generate
export async function generatePuzzle(difficulty = 'MEDIUM'): Promise<unknown> {
  return apiPost(`${API_BASE}/generate`, { difficulty })
}

// Hint
export async function getHintForPuzzle(puzzle: string): Promise<unknown> {
  const normalized = puzzle.replace(/\./g, '0')
  return apiPost(`${API_BASE}/hint`, { puzzle: normalized })
}

// Validate
export async function validatePuzzle(puzzle: string, checkUniqueness = true): Promise<unknown> {
  return apiPost(`${API_BASE}/validate`, { puzzle, checkUniqueness })
}

// Undo/Redo
export async function saveState(puzzle: string): Promise<unknown> {
  const response = await fetch(`${API_BASE}/v1/undo-redo/save`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ puzzle })
  })
  if (!response.ok) return { error: 'Undo/redo not available' }
  return response.json().catch(() => ({}))
}

export async function undo(): Promise<unknown> {
  const response = await fetch(`${API_BASE}/v1/undo-redo/undo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { error: 'Undo not available' }
  return response.json().catch(() => ({}))
}

export async function redo(): Promise<unknown> {
  const response = await fetch(`${API_BASE}/v1/undo-redo/redo`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { error: 'Redo not available' }
  return response.json().catch(() => ({}))
}

interface HistoryResult {
  canUndo: boolean
  canRedo: boolean
  undoCount: number
  redoCount: number
}

export async function getHistory(): Promise<HistoryResult> {
  const response = await fetch(`${API_BASE}/v1/undo-redo/history`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  if (!response.ok) return { canUndo: false, canRedo: false, undoCount: 0, redoCount: 0 }
  return response.json().catch(() => ({ canUndo: false, canRedo: false, undoCount: 0, redoCount: 0 }))
}

// Candidates
export async function fetchCandidates(puzzle: string): Promise<unknown> {
  return apiPost(`${API_BASE}/v1/candidates`, { puzzle })
}

// Tutorials
export async function fetchTutorials(): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials`)
}

export async function fetchTutorial(id: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/${id}`)
}

export async function fetchTutorialBoard(id: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/${id}/board`)
}

export async function completeTutorial(id: string): Promise<unknown> {
  return apiPost(`${API_BASE}/v1/tutorials/${id}/complete`, {})
}

export async function fetchTutorialProgress(): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/progress`)
}

// Daily Challenge
export async function fetchDailyChallenge(date: string | null = null): Promise<unknown> {
  const url = date ? `${API_BASE}/v1/daily/${date}` : `${API_BASE}/v1/daily`
  return apiGet(url)
}

// Quiz
export async function fetchQuizzes(): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/quizzes`)
}

export async function fetchQuizByBelt(belt: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/quizzes/${belt}`)
}

export async function fetchQuizBoard(belt: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/quizzes/${belt}/board`)
}

// Practice
export async function fetchAllPracticeSets(): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/practice`)
}

export async function fetchPracticePuzzles(tutorialId: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/practice/${tutorialId}`)
}

export async function fetchPracticeBoard(tutorialId: string, puzzleId: string): Promise<unknown> {
  return apiGet(`${API_BASE}/v1/tutorials/practice/${tutorialId}/${puzzleId}/board`)
}
