/**
 * Generator: Run with `npx tsx tests/genCorpus.ts` to produce the parity corpus.
 */
import { solve } from '../src/index'
import * as fs from 'fs'
import * as path from 'path'

interface PuzzleEntry {
  name: string
  puzzle: string
  solution: string | null
  difficulty: string
  source: string
}

const puzzles: Omit<PuzzleEntry, 'solution'>[] = [
  // === Existing test puzzles (solutions verified against Kotlin) ===
  { name: 'g1-easy', difficulty: 'easy', source: 'sudokuweb.org/g1',
    puzzle: '.4.3.81..21..65...6......7.9.3.467811.48295.68.5....2.4.....6.3...6.2.47.8..3....' },
  { name: 'g2-medium', difficulty: 'medium', source: 'sudokuweb.org/g2',
    puzzle: '1..496..23.6.1.7...8...31.6..5.6...8.63.85..9...3.45.16.2...9.48..6.9.5.5.982.6..' },
  { name: 'g3-medium', difficulty: 'medium', source: 'sudokuweb.org/g3',
    puzzle: '.6...5....7......1....634....3.8....21..9...54....78....16...84.......5.8...4.61.' },
  { name: 'g4-hard', difficulty: 'hard', source: 'sudokuweb.org/g4',
    puzzle: '7...4.2.....52...6......5...7....96..6.....8.425...........9.31..4..7...1..6.....' },
  { name: 'easy-classic', difficulty: 'easy', source: 'sudokuweb.org/classic',
    puzzle: '53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79' },

  // === Famous puzzles ===
  { name: 'inkala-2012', difficulty: 'extreme', source: 'Arto Inkala 2012',
    puzzle: '8..........36......7..9.2...5...7.......457.....1...3...1....68..85...1..9....4..' },
  { name: 'escargot', difficulty: 'extreme', source: 'Al Escargot',
    puzzle: '1....7.9..3..2...8..96..5....53..9...1..8...26....4...3......1..4......7..7...3..' },
  { name: 'norvig-hard', difficulty: 'hard', source: 'Peter Norvig',
    puzzle: '4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......' },

  // === Easy puzzles (from online sources) ===
  { name: 'easy-01', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '320000600006000008100050090800010000004000300000020001060070002900000400003000065' },
  { name: 'easy-02', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000010000080000070900400002050060040004000800030020050200003009070000020000040000' },
  { name: 'easy-03', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000400000003050200020008040400000009090010080600000004010200030008040900000005000' },
  { name: 'easy-04', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '006000800050030020800100003010000060002080500040000010700005002080060070003000900' },
  { name: 'easy-05', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '200080300060070084030500209000105008000000740750000000000000000570008900000036010' },
  { name: 'easy-06', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '083400000000705100000000070008079300200000008500030006010000020804050000000006800' },
  { name: 'easy-07', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000060000600000009030080104050402000001000300000708090806050030700000002000040000' },
  { name: 'easy-08', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '010040090002000600500307008009050300080000010007030800400902003006000100030010060' },
  { name: 'easy-09', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000900010023060400600003009800400060010000030050009007500200004004080700070005000' },
  { name: 'easy-10', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '029000080006000100500302007003010500080000060001070200700205003002000700060000910' },

  // === Medium puzzles ===
  { name: 'medium-01', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '300200000000107000706030500070009080900020004010800050009040301000702000000008006' },
  { name: 'medium-02', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000006000000100070030080010600020000005070800000040003090030040010008000000700000' },
  { name: 'medium-03', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '200000008030050010006100700040000060008070500090000030004002600060080090800000002' },
  { name: 'medium-04', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000000000007608400000705000000050090020301070010040000000106000004509800000000000' },
  { name: 'medium-05', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000042000800000003030100040600090007002000300100050004080007050900000001000830000' },
  { name: 'medium-06', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '010000050000802000800000003000070090030201080050030000700000006000506000040000010' },
  { name: 'medium-07', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '400000005050030020001200900030000060006010700070000080002009400060080050800000001' },
  { name: 'medium-08', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '020000060000128000008070200004005090100000006090600100002050800000789000060000050' },
  { name: 'medium-09', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000800007005090200020401000040903010907000806080106040000605070001040600400008000' },
  { name: 'medium-10', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000400010030502040100708009300000002040000070700000004800305007020106050090004000' },

  // === Hard puzzles ===
  { name: 'hard-01', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '020000000000600003074800000000003200508000104006900000000001430300008000000000060' },
  { name: 'hard-02', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '005300000800000020070010500400005300010070006003200080060500009004000030000009700' },
  { name: 'hard-03', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000000000000003085001020000000507000004000100090000000500000073002010000000040009' },
  { name: 'hard-04', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '100007000030000080005000400000084000060000050000530000002000300050000070000900001' },
  { name: 'hard-05', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000600000000010007100000005060500300008000200005004060200000008800090000000007000' },
  { name: 'hard-06', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '500000003000700400020030010007000009010080030400000100030060090009008000200000004' },
  { name: 'hard-07', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '800000001003050600060010050000500200400080009005003000010070040006020900200000007' },
  { name: 'hard-08', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000000000000000040007005600060080300300000002001030050005900200080000000000206000' },
  { name: 'hard-09', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '050083070700100600100070000005006008000050000400800900000040002003002005020610040' },
  { name: 'hard-10', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000300020108050060050080400800000010000209000040000003006040090090070304080005000' },

  // === More easy puzzles ===
  { name: 'easy-11', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '700043000020001040000700039060030100200000005005020060810007000030600020000810007' },
  { name: 'easy-12', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '003080060700040002060002100046000010200000007030000540007900050300020009050030200' },
  { name: 'easy-13', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000002000001000700020500160500037010070000050010290003098004030007000600000100000' },
  { name: 'easy-14', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '000020300070300080003005006500200009090000050100004007400600900050008040007010000' },
  { name: 'easy-15', difficulty: 'easy', source: 'sudokuweb.org',
    puzzle: '002005900500070004010000060000102000009000800000608000030000010900050006008300400' },

  // === More medium puzzles ===
  { name: 'medium-11', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '100005007060030080700600002003020500040010070006050400800001006090080050500400003' },
  { name: 'medium-12', difficulty: 'medium', source: 'sudokuweb.org',
    puzzle: '000500000050030020900800004003000900800070001006000300100003005040050060000002000' },

  // === More hard puzzles ===
  { name: 'hard-11', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000000013000700000800000500040000090700305008020000040006000004000009000580000000' },
  { name: 'hard-12', difficulty: 'hard', source: 'sudokuweb.org',
    puzzle: '000000000070000030200030006003000100080050020001000300500010008040000070000000000' },

  // === Boundary cases ===
  { name: 'multi-solution', difficulty: 'multi', source: 'regression-256',
    puzzle: '438.....9..16....3...73.........9.6.8..1..3...76.2....1...4279692...6.3.....17...' },
  { name: 'empty-grid', difficulty: 'empty', source: 'boundary',
    puzzle: '.................................................................................' },
  { name: 'contradictory', difficulty: 'invalid', source: 'boundary',
    puzzle: '11...............................................................................' },
]

const results: PuzzleEntry[] = []
let solved = 0, unsolved = 0
for (const { name, puzzle, difficulty, source } of puzzles) {
  let solution: string | null = null
  try {
    solution = solve(puzzle)
  } catch {
    solution = null
  }
  results.push({ name, puzzle, solution, difficulty, source })
  if (solution) {
    console.log(`  ✓ ${name}: ${difficulty}`)
    solved++
  } else {
    console.log(`  ✗ ${name}: ${difficulty} — no solution`)
    unsolved++
  }
}

const outPath = path.join(import.meta.dirname, 'parity', 'puzzles.json')
fs.mkdirSync(path.dirname(outPath), { recursive: true })
fs.writeFileSync(outPath, JSON.stringify({ puzzles: results }, null, 2) + '\n')
console.log(`\nTotal: ${results.length} entries (${solved} solved, ${unsolved} unsolved)→ ${outPath}`)
