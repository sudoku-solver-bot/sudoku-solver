export function printPuzzle(puzzle, difficulty) {
  const diff = difficulty || 'Sudoku'
  let cells = ''
  for (let i = 0; i < 81; i++) {
    const v = puzzle[i]
    const br = (i % 9 === 2 || i % 9 === 5) ? ' br' : ''
    const bb = (Math.floor(i / 9) === 2 || Math.floor(i / 9) === 5) ? ' bb' : ''
    const cls = v === '.' ? 'empty' : 'given'
    const content = v === '.' ? '&nbsp;' : v
    cells += '<div class="cell ' + cls + br + bb + '">' + content + '</div>'
  }
  const html = [
    '<!DOCTYPE html><html><head><title>' + diff + ' Sudoku</title>',
    '<style>',
    'body{font-family:Arial,sans-serif;text-align:center;padding:40px}',
    'h1{font-size:24px;margin-bottom:4px}',
    '.grid{display:inline-grid;grid-template-columns:repeat(9,48px);border:3px solid #333;margin:20px auto}',
    '.cell{width:48px;height:48px;border:1px solid #ccc;display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:600}',
    '.cell.br{border-right:2px solid #333}.cell.bb{border-bottom:2px solid #333}',
    '.given{color:#000}.empty{color:#ccc}',
    '@media print{body{padding:20px}}',
    '</style></head><body>',
    '<h1>Sudoku Puzzle</h1>',
    '<p style="color:#999">' + diff + '</p>',
    '<div class="grid">' + cells + '</div>',
    '<p style="color:#aaa;margin-top:30px">Sudoku Dojo</p>',
    '</body></html>'
  ].join('')
  const w = window.open('', '_blank')
  w.document.write(html)
  w.document.close()
  setTimeout(() => w.print(), 500)
}
