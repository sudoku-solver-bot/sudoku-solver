// Generate a shareable image of the puzzle
const COLORS = {
  EASY: { bg: '#e8f5e9', accent: '#34a853', header: '#1b5e20' },
  MEDIUM: { bg: '#fff8e1', accent: '#f9a825', header: '#f57f17' },
  HARD: { bg: '#fbe9e7', accent: '#ea4335', header: '#b71c1c' },
  EXPERT: { bg: '#f3e5f5', accent: '#ab47bc', header: '#4a148c' },
  MASTER: { bg: '#e8eaf6', accent: '#3f51b5', header: '#1a237e' }
}

export function generatePuzzleImage(puzzle, difficulty = 'EASY', timeStr = '') {
  const canvas = document.createElement('canvas')
  const size = 450
  const cellSize = 44
  const offset = 27
  canvas.width = size
  canvas.height = size + 80
  const ctx = canvas.getContext('2d')
  const colors = COLORS[difficulty] || COLORS.EASY

  // Background
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  // Header bar
  ctx.fillStyle = colors.header
  ctx.fillRect(0, 0, canvas.width, 50)
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold 18px -apple-system, sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('🧩 Sudoku Dojo', canvas.width / 2, 32)

  // Subtitle
  ctx.fillStyle = '#888'
  ctx.font = '12px -apple-system, sans-serif'
  ctx.fillText(`${difficulty} ${timeStr ? '· ' + timeStr : ''}`, canvas.width / 2, 66)

  // Grid background
  const gridTop = 78
  ctx.fillStyle = colors.bg
  ctx.fillRect(offset, gridTop, cellSize * 9, cellSize * 9)

  // Grid lines
  for (let i = 0; i <= 9; i++) {
    const isBox = i % 3 === 0
    ctx.strokeStyle = isBox ? '#333' : '#ccc'
    ctx.lineWidth = isBox ? 2 : 1
    // Horizontal
    ctx.beginPath()
    ctx.moveTo(offset, gridTop + i * cellSize)
    ctx.lineTo(offset + 9 * cellSize, gridTop + i * cellSize)
    ctx.stroke()
    // Vertical
    ctx.beginPath()
    ctx.moveTo(offset + i * cellSize, gridTop)
    ctx.lineTo(offset + i * cellSize, gridTop + 9 * cellSize)
    ctx.stroke()
  }

  // Numbers
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  for (let i = 0; i < 81; i++) {
    const val = puzzle[i]
    if (val === '.') continue
    const row = Math.floor(i / 9)
    const col = i % 9
    const x = offset + col * cellSize + cellSize / 2
    const y = gridTop + row * cellSize + cellSize / 2
    ctx.fillStyle = '#333'
    ctx.font = 'bold 20px -apple-system, sans-serif'
    ctx.fillText(val, x, y)
  }

  // Footer
  ctx.fillStyle = '#aaa'
  ctx.font = '10px -apple-system, sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('sudoku-solver-r5y8.onrender.com', canvas.width / 2, canvas.height - 12)

  return canvas.toDataURL('image/png')
}

export function downloadImage(dataUrl, filename = 'sudoku.png') {
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = filename
  a.click()
}
