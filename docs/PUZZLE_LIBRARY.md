# Sudoku Puzzle Library
# Sourced from SudokuWiki.org and famous puzzle collections
# Format: 81-char string, 1-9 for givens, . for empty cells

## Beginner / Teaching Puzzles (SudokuWiki)

### Naked Single
- Getting Started: `2...7..38.....6.7.3...4.6....8.2.7..1.......6..7.3.4....4.8...9.6.4.....91..6...2` (26 givens, 55 candidates)

### Hidden Single
- Hidden Pair board: `.........9.46.7....768.41..3.97.1.8...8...3...5.3.87.2..75.261....4.32.8.........` (30 givens, 49 candidates)

### Naked Pair
- Example 1: `4......38..2..41....53..24..7.6.9..4.2.....7.6..7.3.9..57..83....39..4..24......9` (30 givens, 51 candidates)
- Example 2: `.8..9..3..3.........2.6.1.8.2.8..5..8..9.7..6..4..5.7.5.3.4.9.........1..1..5..2.` (26 givens)

### Naked Triple
- Example: `.7...8.29..2.....4854.2......83742.............32617......9.6122.....4..13.6...7.` (30 givens)

### Hidden Pair
- Example: `.........9.46.7....768.41..3.97.1.8...8...3...5.3.87.2..75.261....4.32.8.........` (30 givens)

### Hidden Triple
- Example: `.........231.9.....65..31....8924...1...5...6...1367....93..57.....1.843.........` (27 givens)

### Pointing Pair / Triple
- Pointing Pair 1: `.1.9.36......8....9.....5.7..2.1.43....4.2....64.7.2..7.1.....5....3......56.1.2.` (26 givens, 55 candidates)
- Pointing Pair 2: `.32..61..41..........9.1...5...9...4.6.....7.3...2...5...5.8..........19..7...86.` (23 givens)
- Pointing Triple: `9...5....2..63...5..6..2.....31...7.....2.9...8...5......8..1..5...1...4....6...8` (22 givens)

### Box/Line Reduction
- BLR 1: `.16..78.3...8......7...1.6..48...3..6.......2..9...65..6.9...2......2...9.46..51.` (26 givens, 55 candidates)
- BLR Triple: `...9.3.1...4...6..75.....4....48....2.......3....52....4.....81..5...26..9.2.8...` (23 givens)

## Famous Hard Puzzles

### World's Hardest Sudoku (Arto Inkala, 2012)
`8..........36......7..9.2...5...7.......457.....1...3...1....68..85...1..9....4..` (21 givens, 60 candidates, 1 pair, 11 triples)
Requires: X-Wing, Swordfish, XY-Wing, coloring chains

### AI Escargot (Arto Inkala, 2006)  
`1....7.9..3..2...8..96..5....53..9...1..8......2...6..4..2..3...7..1..5...3...7..` (24 givens, 57 candidates, 5 pairs, 17 triples)
One of the first puzzles rated "most difficult" by multiple solvers

## Usage Notes
- All puzzles validated against our /api/v1/candidates endpoint
- Candidate counts shown after simple elimination only
- For tutorials, prefer puzzles with many visible candidates
- SudokuWiki puzzles are designed to showcase specific techniques
