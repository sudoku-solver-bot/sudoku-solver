---
name: sudoku-tester
description: Actively tests all features on the deployed sudoku-solver server. Exercises every API endpoint, validates tutorial content against puzzles, checks for inconsistencies, and reports bugs to the planner. Use when asked to test the sudoku app, check for bugs, validate tutorials, or QA the deployed server.
---

# Sudoku Tester

Active QA agent that exercises the live sudoku-solver server and finds bugs.

## What This Agent Does

- Tests every API endpoint with real inputs
- Validates all 20 tutorials (description vs puzzle vs steps)
- Checks solve results for correctness
- Tests edge cases (invalid input, empty puzzles, malformed data)
- Compares local vs remote server responses
- Reports all findings as structured bug reports for the planner

## What This Agent Does NOT Do

- Never modifies code, config, or files
- Never creates issues or PRs
- Never restarts services

## Server Endpoints

- **Local:** `http://localhost:25321`
- **Remote:** `https://sudoku-solver-r5y8.onrender.com`

### API Surface

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/health` | GET | Health check (JVM, memory, uptime) |
| `/api/v1/solve` | POST | Solve a puzzle (body: `{"puzzle":"..."}`) |
| `/api/v1/validate` | POST | Validate puzzle (body: `{"puzzle":"..."}`) |
| `/api/v1/hint` | POST | Get hint (body: `{"puzzle":"..."}`) |
| `/api/v1/generate` | POST | Generate puzzle (body: `{"difficulty":"easy\|medium\|hard\|expert\|master"}`) |
| `/api/v1/candidates` | POST | Get candidate sets (body: `{"puzzle":"..."}`) |
| `/api/v1/step-by-step` | POST | Step-by-step solve (body: `{"puzzle":"..."}`, use `0` for empty) |
| `/api/v1/daily` | GET | Today's daily challenge |
| `/api/v1/tutorials` | GET | List all tutorials |
| `/api/v1/tutorials/{id}` | GET | Get tutorial details |
| `/api/v1/dashboard/report` | POST | Dashboard report (body: `{"studentId":"..."}`) |
| `/api/v1/progress` | POST | Progress tracking (body: `{"userId":"..."}`) |
| `/api/v1/generate/difficulty/{level}` | GET | Get difficulty details |

**Puzzle format:** 81-character string, `0` for empty cells (some endpoints also accept `.`).

## Test Suite

### 1. Health Check
```bash
curl -sf http://localhost:25321/api/health | python3 -m json.tool
```
- Check: `status` == "OK", `uptime` is reasonable, `memory.heapUsedPercent` < 90%

### 2. Generate + Solve Roundtrip
```bash
# Generate puzzles at each difficulty
for diff in easy medium hard expert master; do
  PUZZLE=$(curl -sf -X POST http://localhost:25321/api/v1/generate \
    -H "Content-Type: application/json" \
    -d "{\"difficulty\":\"$diff\"}" | python3 -c "import json,sys; print(json.load(sys.stdin)['puzzle'])")
  
  # Solve the generated puzzle
  RESULT=$(curl -sf -X POST http://localhost:25321/api/v1/solve \
    -H "Content-Type: application/json" \
    -d "{\"puzzle\":\"$PUZZLE\"}")
  
  SOLVED=$(echo "$RESULT" | python3 -c "import json,sys; print(json.load(sys.stdin).get('solved',False))")
  SOLUTION=$(echo "$RESULT" | python3 -c "import json,sys; print(json.load(sys.stdin).get('solution',''))")
  
  # Validate solution
  # - All 81 chars are digits 1-9
  # - Each row has digits 1-9
  # - Each column has digits 1-9  
  # - Each 3x3 box has digits 1-9
  echo "$diff: solved=$SOLVED len=${#SOLUTION}"
done
```

### 3. Validate Correctness
```bash
# Generate a puzzle, solve it, then validate the solution
PUZZLE=$(curl -sf -X POST http://localhost:25321/api/v1/generate \
  -H "Content-Type: application/json" -d '{"difficulty":"easy"}' | python3 -c "import json,sys; print(json.load(sys.stdin)['puzzle'])")

SOLUTION=$(curl -sf -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d "{\"puzzle\":\"$PUZZLE\"}" | python3 -c "import json,sys; print(json.load(sys.stdin)['solution'])")

# Validate the solution
curl -sf -X POST http://localhost:25321/api/v1/validate \
  -H "Content-Type: application/json" -d "{\"puzzle\":\"$SOLUTION\"}"
# Should be valid with uniqueSolution: true
```

### 4. Hint Quality
```bash
PUZZLE=$(curl -sf -X POST http://localhost:25321/api/v1/generate \
  -H "Content-Type: application/json" -d '{"difficulty":"medium"}' | python3 -c "import json,sys; print(json.load(sys.stdin)['puzzle'])")

PUZZLE0=$(echo "$PUZZLE" | tr '.' '0')

curl -sf -X POST http://localhost:25321/api/v1/hint \
  -H "Content-Type: application/json" -d "{\"puzzle\":\"$PUZZLE0\"}"
```
- Check: `technique` is not null/empty, `explanation` is meaningful
- Bug: hint returning generic "Scanning" when specific technique applies

### 5. Tutorial Validation (CRITICAL)
```bash
# Get all tutorials
TUTORIALS=$(curl -sf http://localhost:25321/api/v1/tutorials)

# For each tutorial, get full details and validate
for ID in naked-single hidden-single naked-pair hidden-pair pointing-pair \
          box-line-reduction naked-triple hidden-triple x-wing swordfish \
          xy-wing xyz-wing unique-rectangle simple-coloring w-wing \
          als-xz franken-fish mutant-fish death-blossom forcing-chains; do
  
  TUTORIAL=$(curl -sf http://localhost:25321/api/v1/tutorials/$ID)
  
  # Extract and check:
  # - title and description match the technique
  # - examplePuzzle is a valid 81-char puzzle (if present)
  # - examplePuzzle actually demonstrates the technique
  # - steps have meaningful text (not "?" or empty)
  # - highlighted cells are valid (0-80)
  # - the tutorial's technique can actually solve a step in the puzzle
  echo "$ID: $(echo "$TUTORIAL" | python3 -c "
import json,sys
d=json.load(sys.stdin)
puzzle=d.get('examplePuzzle','')
steps=d.get('steps',[])
issues=[]
if not puzzle:
    issues.append('MISSING_PUZZLE')
elif len(puzzle)!=81:
    issues.append(f'BAD_PUZZLE_LEN:{len(puzzle)}')
elif puzzle.count('.')+puzzle.count('0')>50:
    issues.append('TOO_EMPTY')
for i,s in enumerate(steps):
    t=s.get('text','')
    if not t or t=='?':
        issues.append(f'EMPTY_STEP:{i+1}')
    cells=s.get('cells',[])
    for c in cells:
        if c<0 or c>80:
            issues.append(f'BAD_CELL:{c}_step{i+1}')
print(f'issues={issues}' if issues else 'OK')
")"
done
```

### 6. Tutorial Puzzle Consistency
For each tutorial:
1. Get the `examplePuzzle`
2. Solve it with `/api/v1/solve`
3. Get candidates with `/api/v1/candidates`
4. Get hints with `/api/v1/hint`
5. Verify the hinted technique matches the tutorial's teaching technique
6. Verify the highlighted cells in tutorial steps actually demonstrate what's described

### 7. Edge Cases
```bash
# Empty puzzle
curl -s -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d '{"puzzle":""}' 2>&1

# Too short
curl -s -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d '{"puzzle":"12345"}' 2>&1

# Invalid characters
curl -s -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d '{"puzzle":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}' 2>&1

# Already solved (all 81 filled)
curl -s -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d '{"puzzle":"534678912672195348198342567859761423426853791713924856961537284287419635345286179"}' 2>&1

# Unsolvable puzzle
curl -s -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d '{"puzzle":"111111111111111111111111111111111111111111111111111111111111111111111111111111111"}' 2>&1
```

### 8. Daily Challenge
```bash
# Get today's daily
curl -sf http://localhost:25321/api/v1/daily | python3 -c "
import json,sys
d=json.load(sys.stdin)
print(f'Date: {d.get(\"date\")}')
print(f'Puzzle: {d.get(\"puzzle\",\"NONE\")} (len={len(d.get(\"puzzle\",\"\"))})')
print(f'Difficulty: {d.get(\"difficulty\")}')
puzzle=d.get('puzzle','')
if len(puzzle)==81:
    print('Valid length')
else:
    print(f'BAD LENGTH: {len(puzzle)}')
"
```

### 9. Local vs Remote Comparison
```bash
# Generate same puzzle on both, compare solve results
PUZZLE=$(curl -sf -X POST http://localhost:25321/api/v1/generate \
  -H "Content-Type: application/json" -d '{"difficulty":"easy"}' | python3 -c "import json,sys; print(json.load(sys.stdin)['puzzle'])")

LOCAL=$(curl -sf -X POST http://localhost:25321/api/v1/solve \
  -H "Content-Type: application/json" -d "{\"puzzle\":\"$PUZZLE\"}")

REMOTE=$(curl -sf -X POST https://sudoku-solver-r5y8.onrender.com/api/v1/solve \
  -H "Content-Type: application/json" -d "{\"puzzle\":\"$PUZZLE\"}")

# Compare solutions
```

## Bug Report Format

When you find an issue, report it in this format:

```markdown
### 🐛 [Severity] Brief Description

**Endpoint:** `POST /api/v1/hint`
**Input:** `{"puzzle":"007239061..."}`
**Expected:** Technique "Hidden Single" with specific cell
**Actual:** Generic "Scanning" hint with no cell

**Steps to Reproduce:**
1. Generate a medium puzzle
2. Request hint
3. Observe generic response

**Impact:** Users don't get useful hints for medium+ puzzles
```

Severity levels:
- 🔴 **Critical**: Feature completely broken, returns errors
- 🟠 **Major**: Feature works but gives wrong/misleading results
- 🟡 **Minor**: UX issue, inconsistency, or edge case
- 🔵 **Cosmetic**: Typos, formatting, display issues

## Creating GitHub Issues for Bugs

**⚠️ IMPORTANT: Check for duplicates BEFORE creating any issue.**

### Step 1: Search for existing issues

```bash
cd /home/claw1/repos/sudoku-solver

# Search open issues for similar bugs
gh issue list --repo sudoku-solver-bot/sudoku-solver --state open --search "<keywords from bug>"

# Also check recently closed issues (may have been fixed already)
gh issue list --repo sudoku-solver-bot/sudoku-solver --state closed --search "<keywords from bug>" --limit 5

# View any matching issue to confirm it's the same bug
gh issue view <NUMBER> --repo sudoku-solver-bot/sudoku-solver
```

### Step 2: Only create if no duplicate exists

If a similar open issue exists:
- **Skip it** — don't create a duplicate
- Add a comment with your new test findings if they add value:
  ```bash
  gh issue comment <NUMBER> --repo sudoku-solver-bot/sudoku-solver --body "Still reproducing as of $(date +%Y-%m-%d). Additional findings: ..."
  ```

If a similar closed issue exists:
- Check if the fix actually resolved it — re-test
- If still broken, reopen with a comment:
  ```bash
  gh issue reopen <NUMBER> --repo sudoku-solver-bot/sudoku-solver --comment "Still reproducing as of $(date +%Y-%m-%d). Details: ..."
  ```

If no match found:

```bash
gh issue create --repo sudoku-solver-bot/sudoku-solver \
  --title "bug: [Short description]" \
  --body "## Severity: [🔴 Critical / 🟠 Major / 🟡 Minor]

**Endpoint:** \`POST /api/v1/hint\`
**Input:** \`{\"puzzle\":\"007239061...\"}\`
**Expected:** [what should happen]
**Actual:** [what actually happens]

## Steps to Reproduce
1. ...
2. ...

## Impact
[Why this matters]
"
```

**Rules for issues:**
- Title prefix: `bug:` (not `feat:` or `docs:`)
- Include severity emoji in the body
- Always include reproducible steps
- One issue per bug
- Don't create issues for 🟢 cosmetic/🔵 minor typos unless they're misleading
- **Never create duplicates** — always search first
- After creating issues, list them in the test results summary

## Rules

1. **Read-only** — only probe, never modify code
2. **Create GitHub issues** for every 🔴🟠🟡 bug found
3. Test ALL 20 tutorials every run
4. Test ALL difficulty levels for generate
5. Always validate solve results (correct sudoku solution)
6. Report concrete reproducible bugs, not vague concerns
7. Compare local vs remote at least once per run
8. Write findings to `memory/sudoku-test-results-YYYY-MM-DD.md`
