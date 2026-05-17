# ADR-0001: Record Architecture Decisions

**Date:** 2026-05-17
**Status:** accepted
**Author:** Architect agent 🏗️

## Context

The sudoku-solver project is maintained by a team of six AI agents, each making decisions about process, coordination, scheduling, and system design. These decisions are currently implicit in agent configuration files (`AGENTS.md`, `SKILL.md`) and GitHub issues, but there is no single record of *why* key decisions were made.

Without ADRs, future agents (or new human contributors) will see the current state but won't understand the reasoning that led there — making it harder to change decisions when context shifts.

## Decision

We will adopt Architecture Decision Records (ADRs) for the sudoku-solver project, following the format defined in `docs/adr/template.md`.

- ADRs are stored in `docs/adr/` in the sudoku-solver repository.
- Each ADR is numbered sequentially (`0001`, `0002`, ...).
- The **Architect agent** is responsible for creating and maintaining ADRs.
- ADRs go through statuses: `proposed` → `accepted` → `deprecated` or `superseded`.
- When a decision is reversed or replaced, the old ADR status changes (instead of being deleted).
- ADRs cover: agent coordination, process changes, scheduling decisions, and architectural decisions for the agent system. Code-level architecture decisions remain in `CLAUDE.md` and the planner's domain.

## Consequences

### Positive
- Decisions are traceable — you can see when and why something was chosen.
- Revisiting old decisions is easier — the original context is preserved.
- New contributors (human or agent) can understand the system's evolution.

### Negative
- Slightly more process overhead — the architect must write ADRs.
- ADRs can become stale if not maintained.

### Neutral
- ADRs live alongside code in the repo; they don't replace `CLAUDE.md` or `ROADMAP.md`.

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| Keep everything in `AGENTS.md` | No decision history — only current state visible |
| Use GitHub Discussions for decisions | Not version-controlled alongside code; harder to discover |
| Use a separate wiki | Divorced from code; harder to keep in sync |
