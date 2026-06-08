# ADR-005: Drop Daily Challenge Feature

**Date:** 2026-06-08
**Status:** Accepted
**Author:** Architect Agent 🏗️
**Decided by:** William

---

## Context

The daily challenge feature was removed during Phase 1 scope reduction (ADR-002) along with several other non-essential features. The backend route `GET /api/v1/daily` still exists in the codebase but returns 404 because the underlying implementation was stripped.

The tester flagged this as a bug (missing endpoint), but it is **intentionally absent**. The daily challenge requires:

- Real backend persistence (user progress, streak tracking, daily puzzle selection)
- A puzzle rotation/selection system
- Time-zone-aware daily resets
- User account integration for streak tracking

None of these existed — the original implementation was a hardcoded stub. Removing it avoids shipping a broken experience.

## Decision

**We permanently drop the daily challenge feature.** It will not be re-added in Phase 1 or Phase 2.

Rationale:
1. **Low learning value** — daily challenges test speed, not technique comprehension. The tutorial and quiz modes already assess learning.
2. **High implementation cost** — requires persistence, scheduling, user accounts — none of which exist yet.
3. **Dilutes focus** — the core value is teaching sudoku techniques, not competitive daily play.
4. **Honest 404** — a clean 404 is better than a stub that pretends to work.

If a daily challenge is ever desired in the future, it should be a **separate project** with its own persistence layer, not bolted onto the tutorial-focused sudoku solver.

## Consequences

### Positive
- No dead code or misleading stubs in the API
- Tester不会再误报为 bug
- Clearer product scope: tutorial + quiz + practice

### Negative
- Users who expect a daily challenge won't find one
- The 404 will persist until the route is removed from the codebase (cleanup task)

### Neutral
- The `/api/v1/daily` route remains as a 404 until cleaned up — harmless but noisy

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Keep as stub returning random puzzle | Misleading — implies feature works; adds maintenance burden |
| Implement full daily challenge | Out of scope for Phase 1; requires persistence layer that doesn't exist |
| Remove route entirely now | Cleanup task; low priority vs. active bugs |

## Related

- ADR-002: Feature Scope Reduction for Phase 1
- Issue #664: Quiz multi-solution bugs (tester filed alongside daily challenge finding)
