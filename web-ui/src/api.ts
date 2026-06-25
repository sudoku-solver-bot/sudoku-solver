const API_BASE = '/api'

async function apiPost(url: string, body: Record<string, unknown>): Promise<any> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  return response.json()
}

async function apiGet(url: string): Promise<any> {
  const response = await fetch(url)
  return response.json()
}

// ---------------------------------------------------------------------------
// Client-side solver helpers
// ---------------------------------------------------------------------------

/** Performance timer for client solver metrics. */
let _clientSolver: any = null

async function loadClientSolver(): Promise<any> {
  if (_clientSolver) return _clientSolver
  try {
    _clientSolver = await import('@sudoku-dojo/solver')
    return _clientSolver
  } catch {
    return null
  }
}

// ---------------------------------------------------------------------------
// Solve — client-first, server fallback
// ---------------------------------------------------------------------------

export async function solvePuzzle(puzzle: string, includeMetrics = false): Promise<unknown> {
  // Try client-side solver first
  const clientSolver = await loadClientSolver()
  if (clientSolver) {
    const startTime = performance.now()
    const solution = clientSolver.solve(puzzle)
    const elapsed = performance.now() - startTime

    if (solution) {
      return {
        solved: true,
        solution,
        clientSolver: true,
        metrics: {
          solveTimeMs: Math.round(elapsed * 100) / 100,
          difficulty: '? (client)',
          techniquesUsed: ['Simple Elimination', 'Naked Subset', 'Hidden Single']
        }
      }
    }
    return { solved: false, error: 'No solution found', clientSolver: true }
  }

  // Fall back to server API
  return apiPost(`${API_BASE}/solve`, { puzzle, includeMetrics })
}

// ---------------------------------------------------------------------------
// Solve with steps — client-first, server fallback
// ---------------------------------------------------------------------------

export async function solveWithSteps(puzzle: string): Promise<unknown> {
  const clientSolver = await loadClientSolver()
  if (clientSolver) {
    try {
      const startTime = performance.now()
      const board = clientSolver.BoardReader.fromString(puzzle.replace(/\./g, '0'), clientSolver.Board)
      const wrapper = new clientSolver.SolverWithSteps()
      const [solution, progress] = wrapper.solveWithSteps(board)
      const elapsed = performance.now() - startTime

      if (solution) {
        // Convert solution to string
        let solutionStr = ''
        for (let r = 0; r < 9; r++) {
          for (let c = 0; c < 9; c++) {
            const coord = clientSolver.Coord.all[r * 9 + c]
            solutionStr += solution.isConfirmed(coord) ? solution.value(coord) : '.'
          }
        }

        // Convert progress steps to plain objects
        const steps = progress.steps.map((s: any) => ({
          stepNumber: s.stepNumber,
          stepType: s.stepType,
          explanation: s.explanation,
          affectedCells: s.affectedCells?.map((c: any) => ({ row: c.row, col: c.col })) ?? [],
          values: [...(s.values ?? [])]
        }))

        return {
          solved: true,
          solution: solutionStr,
          steps,
          stepCount: steps.length,
          techniquesUsed: [...new Set(steps.map((s: any) => s.stepType))],
          solveTimeMs: Math.round(elapsed * 100) / 100,
          clientSolver: true
        }
      }
    } catch {
      // Client failed — fall through to server
    }
  }

  // Fall back to server API
  return apiPost(`${API_BASE}/solve-steps`, { puzzle })
}

// ---------------------------------------------------------------------------
// Generate — client-first, server fallback
// ---------------------------------------------------------------------------

export async function generatePuzzle(difficulty = 'MEDIUM', seed?: number): Promise<unknown> {
  // Try client-side generator first
  const clientSolver = await loadClientSolver()
  if (clientSolver) {
    try {
      const startTime = performance.now()
      const levelMap: Record<string, number> = {
        EASY: 1, MEDIUM: 2, HARD: 3, EXPERT: 4, VERY_HARD: 5, MASTER: 6
      }
      const level = levelMap[difficulty.toUpperCase()] ?? 2
      const puzzle = clientSolver.generatePuzzle(level, seed)
      const elapsed = performance.now() - startTime

      // Convert board to string
      let puzzleStr = ''
      for (let r = 0; r < 9; r++) {
        for (let c = 0; c < 9; c++) {
          const coord = clientSolver.Coord.all[r * 9 + c]
          puzzleStr += puzzle.isConfirmed(coord) ? puzzle.value(coord) : '.'
        }
      }

      return {
        puzzle: puzzleStr,
        difficulty,
        generateTimeMs: Math.round(elapsed * 100) / 100,
        clientSolver: true
      }
    } catch {
      // Client failed — fall through to server
    }
  }

  // Fall back to server API
  return apiPost(`${API_BASE}/generate`, { difficulty })
}

// ---------------------------------------------------------------------------
// Hint — client-first, server fallback
// ---------------------------------------------------------------------------

let _clientHintGenerator: any = null

async function loadClientHintGenerator(): Promise<any> {
  if (_clientHintGenerator) return _clientHintGenerator
  try {
    const solver = await import('@sudoku-dojo/solver')
    if (solver.generateHint) {
      _clientHintGenerator = solver
      return _clientHintGenerator
    }
    return null
  } catch {
    return null
  }
}

export async function getHintForPuzzle(puzzle: string, technique?: string): Promise<unknown> {
  // Try client-side hint generator first
  const clientHG = await loadClientHintGenerator()
  if (clientHG) {
    try {
      const board = clientHG.BoardReader.fromString(puzzle.replace(/\./g, '0'), clientHG.Board)
      const options: Record<string, unknown> = {}
      if (technique) {
        // Map technique string to Technique enum value
        const techEnum = clientHG.Technique[technique.toUpperCase().replace(/ /g, '_')]
        if (techEnum) options.targetTechnique = techEnum
      }
      const hint = clientHG.generateHint(board, options)
      if (hint) {
        return {
          hasHint: true,
          hint: {
            row: hint.coord.row,
            col: hint.coord.col,
            value: hint.value,
            technique: hint.technique
          },
          explanation: hint.explanation,
          clientSolver: true
        }
      }
      // No client hint found — fall through to server
    } catch {
      // Client failed — fall through to server
    }
  }

  // Fall back to server API
  const normalized = puzzle.replace(/\./g, '0')
  const body: Record<string, string> = { puzzle: normalized }
  if (technique) body.technique = technique
  const serverData = await apiPost(`${API_BASE}/hint`, body)
  
  // Normalize server response to match client shape
  if (serverData && (serverData as any).cell) {
    const d = serverData as any
    return {
      hasHint: true,
      hint: {
        row: d.cell.row,
        col: d.cell.col,
        value: d.value,
        technique: d.technique
      },
      explanation: d.explanation,
      clientSolver: false
    }
  }
  return { hasHint: false, error: 'No hint found' }
}

// ---------------------------------------------------------------------------
// Validate — client-first, server fallback
// ---------------------------------------------------------------------------

export async function validatePuzzle(puzzle: string, checkUniqueness = true): Promise<unknown> {
  const clientSolver = await loadClientSolver()
  if (clientSolver) {
    const result = clientSolver.validate(puzzle)
    if (result.valid || result.error) {
      return { ...result, clientSolver: true }
    }
  }
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

// ---------------------------------------------------------------------------
// Candidates — client-first, server fallback
// ---------------------------------------------------------------------------

export async function fetchCandidates(puzzle: string): Promise<unknown> {
  const clientSolver = await loadClientSolver()
  if (clientSolver) {
    const board = clientSolver.BoardReader.fromString(puzzle, clientSolver.Board)
    const solver = new clientSolver.Solver()
    solver.solve(board)
    const result: Record<string, number[]> = {}
    for (let i = 0; i < 81; i++) {
      const coord = clientSolver.Coord.all[i]
      if (!board.isConfirmed(coord)) {
        result[String(i)] = board.candidateValues(coord)
      }
    }
    return { candidates: result, clientSolver: true }
  }
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
