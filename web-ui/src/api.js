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

/**
 * Encoding/decoding utilities for puzzle sharing.
 */

/**
 * Encodes an 81-character puzzle string to base64url format.
 * Empty cells (.) are replaced with 0 for encoding.
 * @param {string} puzzle - The puzzle string (81 chars, 1-9 for values, . for empty)
 * @returns {string} URL-safe base64 encoded string
 */
export function encodePuzzle(puzzle) {
  if (puzzle.length !== 81) {
    throw new Error(`Puzzle must be 81 characters, got ${puzzle.length}`)
  }

  // Replace '.' with '0' for encoding
  const normalized = puzzle.replace(/\./g, '0')
  const bytes = new TextEncoder().encode(normalized)

  // Use base64url encoding (replace + and / with - and _, remove padding)
  let base64 = btoa(String.fromCharCode(...bytes))
  base64 = base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')

  return base64
}

/**
 * Decodes a base64url encoded puzzle string back to puzzle format.
 * @param {string} encoded - The base64url encoded string
 * @returns {string} The 81-character puzzle string (1-9 for values, . for empty)
 */
export function decodePuzzle(encoded) {
  try {
    // Convert base64url to standard base64
    let base64 = encoded.replace(/-/g, '+').replace(/_/g, '/')

    // Add padding if needed
    while (base64.length % 4) {
      base64 += '='
    }

    // Decode base64
    const binaryStr = atob(base64)
    const bytes = new Uint8Array(binaryStr.length)
    for (let i = 0; i < binaryStr.length; i++) {
      bytes[i] = binaryStr.charCodeAt(i)
    }

    const decoded = new TextDecoder().decode(bytes)

    if (decoded.length !== 81) {
      throw new Error(`Decoded puzzle must be 81 characters, got ${decoded.length}`)
    }

    // Validate characters
    if (!/^[0-9]+$/.test(decoded)) {
      throw new Error('Decoded puzzle contains invalid characters')
    }

    // Convert '0' back to '.'
    return decoded.replace(/0/g, '.')
  } catch (e) {
    throw new Error(`Invalid encoded puzzle: ${e.message}`)
  }
}

/**
 * Generates a shareable URL for the current puzzle.
 * @param {string} puzzle - The puzzle string (81 chars, 1-9 for values, . for empty)
 * @returns {string} The full shareable URL
 */
export function generateShareUrl(puzzle) {
  const encoded = encodePuzzle(puzzle)
  const url = new URL(window.location.href)
  url.searchParams.set('p', encoded)
  return url.toString()
}

/**
 * Loads a puzzle from URL parameters.
 * Checks for the 'p' parameter in the current URL.
 * @returns {string|null} The decoded puzzle string, or null if not found
 */
export function loadPuzzleFromUrl() {
  const urlParams = new URLSearchParams(window.location.search)
  const encoded = urlParams.get('p')

  if (!encoded) {
    return null
  }

  try {
    return decodePuzzle(encoded)
  } catch (e) {
    console.error('Failed to decode puzzle from URL:', e)
    return null
  }
}
