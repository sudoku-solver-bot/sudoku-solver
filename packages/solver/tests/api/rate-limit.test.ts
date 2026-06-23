/**
 * Rate limiting test.
 *
 * Hits the live API server with rapid requests to verify rate limiting works.
 *
 * ## Prerequisites
 * - The sudoku-solver server must be running locally on port 8080
 *   (or set SUDOKU_API_URL env var)
 *
 * ## CI Skipping
 * This test is excluded from CI by default (it hits a live server).
 * Set `SUDOKU_API_URL` to enable it, e.g.:
 *   SUDOKU_API_URL=http://localhost:8080 npx vitest run tests/api/rate-limit.test.ts
 *
 * Without SUDOKU_API_URL, all tests are skipped.
 */

import { describe, it, expect } from 'vitest'

const API_URL = process.env.SUDOKU_API_URL

const describeIf = API_URL ? describe : describe.skip

describeIf('API rate limiting', () => {
  /** Lightweight endpoint that returns a small response. */
  const HEALTH_URL = `${API_URL}/api/v1/health`

  /**
   * Sends `count` concurrent requests to the given URL.
   * Returns an array of { status, headers } for each response.
   */
  async function sendBurst(url: string, count: number): Promise<Array<{ status: number; retryAfter: string | null }>> {
    const promises = Array.from({ length: count }, () =>
      fetch(url).then(async (res) => {
        const retryAfter = res.headers.get('Retry-After') ?? null
        return { status: res.status, retryAfter }
      }).catch(() => ({
        status: -1,
        retryAfter: null
      }))
    )
    return Promise.all(promises)
  }

  it('responds 200 for single health check', async () => {
    const res = await fetch(HEALTH_URL)
    expect(res.status).toBe(200)
  })

  it('returns 429 after exceeding rate limit', async () => {
    // Ktor RateLimit: 100 req/min per IP. Send 150 to ensure we hit the limit.
    const responses = await sendBurst(HEALTH_URL, 150)

    const statuses = responses.map(r => r.status)
    const tooMany = statuses.filter(s => s === 429)

    // Some requests may fail with connection errors if the server throttles aggressively,
    // but we should see at least one 429 response.
    expect(tooMany.length).toBeGreaterThan(0)
  })

  it('rate-limited responses include Retry-After header', async () => {
    // Send a burst that's guaranteed to hit the limit
    const responses = await sendBurst(HEALTH_URL, 150)

    const rateLimited = responses.filter(r => r.status === 429)
    // At least some rate-limited responses should carry a Retry-After header
    const withRetryAfter = rateLimited.filter(r => r.retryAfter !== null)
    expect(withRetryAfter.length).toBeGreaterThan(0)
  })
})
