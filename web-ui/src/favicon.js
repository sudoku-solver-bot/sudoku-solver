// Generate a dynamic favicon based on game state
const COLORS = {
  EASY: '#34a853',
  MEDIUM: '#fbbc04',
  HARD: '#ea4335',
  EXPERT: '#ab47bc',
  MASTER: '#1a237e'
}

export function updateFavicon(difficulty = 'EASY', filled = 0) {
  const canvas = document.createElement('canvas')
  canvas.width = 32
  canvas.height = 32
  const ctx = canvas.getContext('2d')

  // Background
  ctx.fillStyle = COLORS[difficulty] || COLORS.EASY
  ctx.beginPath()
  ctx.roundRect(0, 0, 32, 32, 6)
  ctx.fill()

  // Grid lines (simplified 3x3)
  ctx.strokeStyle = 'rgba(255,255,255,0.4)'
  ctx.lineWidth = 1
  for (let i = 1; i < 3; i++) {
    ctx.beginPath()
    ctx.moveTo(4, 4 + i * 8); ctx.lineTo(28, 4 + i * 8); ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(4 + i * 8, 4); ctx.lineTo(4 + i * 8, 28); ctx.stroke()
  }

  // Progress arc
  if (filled > 0) {
    const pct = filled / 81
    ctx.strokeStyle = 'rgba(255,255,255,0.8)'
    ctx.lineWidth = 3
    ctx.beginPath()
    ctx.arc(16, 16, 14, -Math.PI / 2, -Math.PI / 2 + pct * Math.PI * 2)
    ctx.stroke()
  }

  // Set favicon
  let link = document.querySelector("link[rel*='icon']")
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }
  link.href = canvas.toDataURL('image/png')
}
