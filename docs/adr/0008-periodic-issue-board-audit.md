# ADR-0008: Periodic Issue Board Audit

**Date:** 2026-06-22
**Status:** proposed
**Author:** Architect agent 🏗️

## Context

The June 22, 2026 audit revealed that **12 out of 13 plan issues** on the board were stale — the work had already been implemented and merged, but the issues were never closed. Only 1 issue (#689 HintModal.vue) represented actual remaining work.

This drift occurs because:
- The Coder implements work but doesn't always close the issue
- The Reviewer merges PRs but doesn't verify issue closure
- The Planner creates issues but has no mechanism to detect staleness
- There is no scheduled "truth check" between the issue board and the codebase

Without periodic audits, the board becomes an unreliable source of truth, wasting time during triage and making it hard to assess actual pipeline health.

## Decision

We will implement a **weekly issue board audit** as a lightweight process check:

1. **Frequency:** Every 7 days, triggered by the Planner agent
2. **Scope:** All open issues labeled `plan`, `bug`, or `suggestion`
3. **Method:** For each open issue, check if the referenced work exists in the codebase (file presence, API endpoint, test file). If confirmed implemented, close the issue with a comment noting the verification.
4. **Output:** A brief audit summary sent to William and the Architect
5. **Edge case:** If work is partially implemented, add a comment with specifics and keep the issue open

### What this is NOT
- NOT a full code review of every issue
- NOT blocking any agent's workflow
- NOT a replacement for proper issue lifecycle management at implementation time

## Consequences

### Positive
- Issue board always reflects reality within a 7-day window
- Eliminates wasted triage time on stale issues
- Provides a weekly health pulse of the pipeline
- Catches orphaned work early

### Negative
- Adds ~5-10 minutes of Planner work per week
- Risks closing issues prematurely if verification is too shallow
- Doesn't fix root cause (agents not closing issues when work is done)

### Neutral
- Audit results may surface that some agents need clearer issue-closure discipline
- The audit itself may evolve into automated checks over time

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Enforce issue closure at merge time (PR template, branch protection) | Requires tooling changes; doesn't catch issues that were never linked to PRs |
| Automated CI check that scans open issues vs code | Complex to implement reliably; false positives likely with partial implementations |
| Do nothing / accept drift | Already proven costly — 92% staleness rate found in this audit |
| Architect does audits | Architect role is oversight, not ops; Planner owns issue lifecycle |
