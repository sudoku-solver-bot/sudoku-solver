// Generate a downloadable certificate image using Canvas
// Follows the same pattern as share-image.js

export function generateCertificateImage({
  technique,
  beltName,
  beltEmoji,
  beltColor,
  tutorialsCompleted,
  totalTutorials
}) {
  const canvas = document.createElement('canvas')
  const W = 800
  const H = 560
  canvas.width = W
  canvas.height = H
  const ctx = canvas.getContext('2d')

  // --- Background ---
  ctx.fillStyle = '#fffef8'
  ctx.fillRect(0, 0, W, H)

  // --- Gold border ---
  const borderPad = 16
  const innerPad = 8
  // Outer border
  ctx.strokeStyle = '#c9a94e'
  ctx.lineWidth = 4
  roundRect(ctx, borderPad, borderPad, W - borderPad * 2, H - borderPad * 2, 20)
  ctx.stroke()
  // Inner border
  ctx.strokeStyle = '#e8d5a0'
  ctx.lineWidth = 2
  roundRect(ctx, borderPad + innerPad, borderPad + innerPad, W - (borderPad + innerPad) * 2, H - (borderPad + innerPad) * 2, 16)
  ctx.stroke()

  // --- Corner ornaments ---
  const cornerSize = 24
  ctx.fillStyle = '#c9a94e'
  // Top-left
  drawCornerOrnament(ctx, borderPad + innerPad + 12, borderPad + innerPad + 12, cornerSize)
  // Top-right
  drawCornerOrnament(ctx, W - borderPad - innerPad - 12 - cornerSize, borderPad + innerPad + 12, cornerSize)
  // Bottom-left
  drawCornerOrnament(ctx, borderPad + innerPad + 12, H - borderPad - innerPad - 12 - cornerSize, cornerSize)
  // Bottom-right
  drawCornerOrnament(ctx, W - borderPad - innerPad - 12 - cornerSize, H - borderPad - innerPad - 12 - cornerSize, cornerSize)

  // --- Top ornament emojis ---
  ctx.textAlign = 'center'
  ctx.font = '36px sans-serif'
  ctx.fillText('✨🏆✨', W / 2, 80)

  // --- Title ---
  ctx.fillStyle = '#2c3e50'
  ctx.font = 'bold 36px Georgia, "Times New Roman", serif'
  ctx.fillText('Certificate of Achievement', W / 2, 130)

  // --- Divider ---
  drawDivider(ctx, W / 2, 152, 240)

  // --- Subtitle ---
  ctx.fillStyle = '#7f8c8d'
  ctx.font = '18px -apple-system, sans-serif'
  ctx.fillText('This certifies mastery of', W / 2, 190)

  // --- Technique name ---
  ctx.fillStyle = '#c9a94e'
  ctx.font = 'bold 32px Georgia, serif'
  ctx.fillText(technique, W / 2, 240)

  // --- Belt badge circle ---
  const badgeCX = W / 2
  const badgeCY = 310
  const badgeR = 32
  // Shadow
  ctx.beginPath()
  ctx.arc(badgeCX, badgeCY + 2, badgeR, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(0,0,0,0.1)'
  ctx.fill()
  // Circle
  ctx.beginPath()
  ctx.arc(badgeCX, badgeCY, badgeR, 0, Math.PI * 2)
  ctx.fillStyle = beltColor || '#FFD700'
  ctx.fill()
  ctx.strokeStyle = 'rgba(0,0,0,0.15)'
  ctx.lineWidth = 2
  ctx.stroke()
  // Emoji
  ctx.font = '28px sans-serif'
  ctx.textBaseline = 'middle'
  ctx.fillText(beltEmoji || '🟡', badgeCX, badgeCY)
  ctx.textBaseline = 'alphabetic'

  // --- Belt name ---
  ctx.fillStyle = '#555'
  ctx.font = 'bold 22px -apple-system, sans-serif'
  ctx.fillText(beltName, W / 2, 370)

  // --- Divider ---
  drawDivider(ctx, W / 2, 400, 200)

  // --- Dojo name ---
  ctx.fillStyle = '#999'
  ctx.font = '14px -apple-system, sans-serif'
  ctx.letterSpacing = '2px'
  ctx.fillText('SUDOKU DOJO', W / 2, 435)

  // --- Date ---
  const dateStr = new Date().toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
  ctx.fillStyle = '#aaa'
  ctx.font = '14px -apple-system, sans-serif'
  ctx.fillText(dateStr, W / 2, 460)

  // --- Lessons progress ---
  ctx.fillStyle = '#bbb'
  ctx.font = '13px -apple-system, sans-serif'
  ctx.fillText(`${tutorialsCompleted} / ${totalTutorials} lessons completed`, W / 2, 490)

  // --- Footer ---
  ctx.fillStyle = '#ccc'
  ctx.font = '11px -apple-system, sans-serif'
  ctx.fillText('sudoku-solver-r5y8.onrender.com', W / 2, H - 30)

  return canvas.toDataURL('image/png')
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.lineTo(x + w - r, y)
  ctx.quadraticCurveTo(x + w, y, x + w, y + r)
  ctx.lineTo(x + w, y + h - r)
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
  ctx.lineTo(x + r, y + h)
  ctx.quadraticCurveTo(x, y + h, x, y + h - r)
  ctx.lineTo(x, y + r)
  ctx.quadraticCurveTo(x, y, x + r, y)
  ctx.closePath()
}

function drawDivider(ctx, cx, cy, width) {
  const grad = ctx.createLinearGradient(cx - width / 2, cy, cx + width / 2, cy)
  grad.addColorStop(0, 'rgba(201,169,78,0)')
  grad.addColorStop(0.3, 'rgba(201,169,78,0.7)')
  grad.addColorStop(0.5, 'rgba(201,169,78,1)')
  grad.addColorStop(0.7, 'rgba(201,169,78,0.7)')
  grad.addColorStop(1, 'rgba(201,169,78,0)')
  ctx.strokeStyle = grad
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(cx - width / 2, cy)
  ctx.lineTo(cx + width / 2, cy)
  ctx.stroke()
}

function drawCornerOrnament(ctx, x, y, size) {
  ctx.save()
  ctx.beginPath()
  // Simple diamond ornament
  ctx.moveTo(x + size / 2, y)
  ctx.lineTo(x + size, y + size / 2)
  ctx.lineTo(x + size / 2, y + size)
  ctx.lineTo(x, y + size / 2)
  ctx.closePath()
  ctx.globalAlpha = 0.3
  ctx.fill()
  ctx.globalAlpha = 1
  ctx.restore()
}

export function downloadCertificateImage(dataUrl, beltName) {
  const safeName = beltName.toLowerCase().replace(/\s+/g, '-')
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = `sudoku-dojo-${safeName}-certificate.png`
  a.click()
}
