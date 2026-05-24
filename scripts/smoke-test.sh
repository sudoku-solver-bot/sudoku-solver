#!/bin/bash
# Sudoku Dojo — Deploy Smoke Test
# Verifies the deployment is healthy after a deploy.
# Exit code: 0 = success, 1 = failure

set -euo pipefail

BASE_URL="${SUDOKU_URL:-http://localhost:25321}"
FAIL=0

echo "🔍 Smoke testing $BASE_URL ..."

# 1. Health endpoint returns OK
echo -n "  Health check... "
HEALTH=$(curl -sf "$BASE_URL/api/v1/health" 2>/dev/null)
if echo "$HEALTH" | grep -q '"status" *: *"OK"'; then
    echo "✅"
else
    echo "❌ (status not OK)"
    FAIL=1
fi

# 2. Git commit matches expected (if set)
if [ -n "${EXPECTED_COMMIT:-}" ]; then
    echo -n "  Commit match... "
    ACTUAL=$(echo "$HEALTH" | grep -o '"gitCommit" *: *"[^"]*"' | head -1 | cut -d'"' -f4)
    if [ "$ACTUAL" = "$EXPECTED_COMMIT" ]; then
        echo "✅ ($ACTUAL)"
    else
        echo "❌ (expected $EXPECTED_COMMIT, got $ACTUAL)"
        FAIL=1
    fi
fi

# 3. Solve endpoint works with a known puzzle
echo -n "  Solve endpoint... "
SOLVE_RESULT=$(curl -sf -X POST "$BASE_URL/api/v1/solve" \
    -H "Content-Type: application/json" \
    -d '{"puzzle":"000000030002090500080706004900054006030000070600380009300601020007020600060000000"}' 2>/dev/null)
if echo "$SOLVE_RESULT" | grep -q '"solved" *: *true'; then
    echo "✅"
else
    echo "❌ (solve failed)"
    FAIL=1
fi

if [ $FAIL -eq 0 ]; then
    echo "✅ All checks passed"
else
    echo "❌ Some checks failed"
fi

exit $FAIL
