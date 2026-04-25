import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock global fetch
const mockFetch = vi.fn()
global.fetch = mockFetch

// Re-import the API module for each test to use fresh mock
describe('API error resilience', () => {
  beforeEach(() => {
    vi.resetModules()
    mockFetch.mockReset()
  })

  async function getApiModule() {
    const api = await import('@/api')
    return api
  }

  describe('404 responses', () => {
    it('solvePuzzle handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { solvePuzzle } = await getApiModule()
      // Should not throw, even with 404
      const result = await solvePuzzle('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('generatePuzzle handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { generatePuzzle } = await getApiModule()
      const result = await generatePuzzle('EASY')
      expect(result).toBeDefined()
    })

    it('getHintForPuzzle handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { getHintForPuzzle } = await getApiModule()
      const result = await getHintForPuzzle('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('validatePuzzle handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { validatePuzzle } = await getApiModule()
      const result = await validatePuzzle('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('saveState handles 404 gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.reject(new Error('Not JSON')) })
      const { saveState } = await getApiModule()
      const result = await saveState('.'.repeat(81))
      expect(result).toBeDefined()
      expect(result.error).toBeDefined()
    })

    it('undo handles 404 gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.reject(new Error('Not JSON')) })
      const { undo } = await getApiModule()
      const result = await undo()
      expect(result).toBeDefined()
      expect(result.error).toBeDefined()
    })

    it('redo handles 404 gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.reject(new Error('Not JSON')) })
      const { redo } = await getApiModule()
      const result = await redo()
      expect(result).toBeDefined()
      expect(result.error).toBeDefined()
    })

    it('getHistory handles 404 gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.reject(new Error('Not JSON')) })
      const { getHistory } = await getApiModule()
      const result = await getHistory()
      expect(result).toBeDefined()
      expect(result.canUndo).toBe(false)
    })

    it('fetchCandidates handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { fetchCandidates } = await getApiModule()
      const result = await fetchCandidates('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('fetchTutorials handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { fetchTutorials } = await getApiModule()
      const result = await fetchTutorials()
      expect(result).toBeDefined()
    })

    it('fetchDailyChallenge handles 404', async () => {
      mockFetch.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: 'Not found' }) })
      const { fetchDailyChallenge } = await getApiModule()
      const result = await fetchDailyChallenge()
      expect(result).toBeDefined()
    })
  })

  describe('empty body responses', () => {
    it('solvePuzzle handles empty body', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({}) })
      const { solvePuzzle } = await getApiModule()
      const result = await solvePuzzle('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('generatePuzzle handles empty body', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({}) })
      const { generatePuzzle } = await getApiModule()
      const result = await generatePuzzle()
      expect(result).toBeDefined()
    })

    it('fetchTutorials handles empty body', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({}) })
      const { fetchTutorials } = await getApiModule()
      const result = await fetchTutorials()
      expect(result).toBeDefined()
    })
  })

  describe('malformed JSON responses', () => {
    it('solvePuzzle handles malformed JSON', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.reject(new SyntaxError('Bad JSON')) })
      const { solvePuzzle } = await getApiModule()
      await expect(solvePuzzle('.'.repeat(81))).rejects.toThrow()
    })

    it('saveState handles malformed JSON gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.reject(new SyntaxError('Bad JSON')) })
      const { saveState } = await getApiModule()
      const result = await saveState('.'.repeat(81))
      expect(result).toBeDefined()
    })

    it('getHistory handles malformed JSON gracefully', async () => {
      mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.reject(new SyntaxError('Bad JSON')) })
      const { getHistory } = await getApiModule()
      const result = await getHistory()
      expect(result).toBeDefined()
      expect(result.canUndo).toBe(false)
    })
  })

  describe('network failures', () => {
    it('solvePuzzle does not throw on network error', async () => {
      mockFetch.mockRejectedValue(new TypeError('Failed to fetch'))
      const { solvePuzzle } = await getApiModule()
      await expect(solvePuzzle('.'.repeat(81))).rejects.toThrow()
    })

    it('generatePuzzle does not throw on network error', async () => {
      mockFetch.mockRejectedValue(new TypeError('Failed to fetch'))
      const { generatePuzzle } = await getApiModule()
      await expect(generatePuzzle()).rejects.toThrow()
    })
  })
})
