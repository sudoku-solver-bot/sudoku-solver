# ADR-0002: GitHub Issues as Agent Integration Layer

**Date:** 2026-05-17
**Status:** accepted
**Author:** Architect agent 🏗️
**Supersedes:** (none — codifies existing practice)

## Context

Six specialized agents (architect, planner, coder, tester, reviewer, deployer) need to coordinate work on the sudoku-solver project. The work pipeline is:

```
Architect → Planner → Coder → Reviewer → Deployer
                ↑                    │
                └── Tester ←─────────┘
```

Agents run at different schedules (some every hour, some every 3–6 hours, some on-demand). They do not share memory — each session is isolated. Without a shared coordination surface, agents would need to use inter-agent messaging for every handoff, which is fragile (messages can be lost if the target agent isn't running) and opaque (other agents can't see the full state).

Two approaches were considered:
- **Option A:** Cross-reference existing agent output folders with explicit "Reads From" sections in each `SKILL.md`
- **Option B:** A shared `memory/sudoku-shared/` folder with symlinks

Both were rejected in favor of using GitHub.

## Decision

**GitHub issues are the shared coordination surface for all sudoku agents.**

### Issue Labels as Routing

| Label | Created by | Picked up by | Purpose |
|-------|-----------|-------------|---------|
| `plan` | Planner | Coder | Implementation specs |
| `bug` | Tester | Coder | Reproducible defects |
| `architecture` | Architect | Planner | Process improvements |
| `chore` | Planner, Deployer | Coder | Maintenance, infrastructure |

### Issue Ownership Rules

- Only the **tester** closes `bug` issues (after verifying the fix is deployed and working)
- Only the **planner** closes `plan` issues (after the coder ships a PR)
- The **coder** never closes issues — they create PRs linked with `Refs #N`
- The **reviewer** reviews and merges PRs, then notifies the deployer
- The **deployer** deploys on master advancement, reports failures as `chore` issues

### Inter-Agent Notifications

`ROADMAP.md` in the repo root serves as the canonical planning surface above issues. Planner maintains it; all agents read it.

`sessions_send` is used only for urgent notifications during the same run (e.g., "PR #300 created, please review"). All persistent tracking goes through GitHub.

## Consequences

### Positive
- **Visibility** — all agents (and humans) can see all work state at any time.
- **Persistence** — survives session restarts and agent crashes.
- **Auditability** — full change history on every issue.
- **Standard tooling** — `gh` CLI works everywhere; no custom coordination protocol needed.
- **GitHub-native** — same surface agents already use for code (PRs, CI).

### Negative
- Agents must authenticate to GitHub (requires `GH_TOKEN`).
- Issue-based coordination adds some GitHub API calls to every agent run.
- Issue list can get noisy if agents don't close stale issues.

### Neutral
- Agents still maintain local memory files for detailed operational logs (`memory/sudoku-{agent}/`). GitHub is for coordination, not logging.

## Alternatives Considered

| Alternative | Why rejected |
|-------------|-------------|
| `memory/sudoku-shared/` with symlinks | Local-only, filesystem-dependent, no audit trail, no human visibility |
| Cross-reference "Reads From" sections in SKILL.md | Not guaranteed that agents load SKILL.md; brittle file paths |
| Pure `sessions_send` messaging | Messages lost if target agent not running; opaque to other agents |
| GitHub Projects (Kanban board) | Added complexity for 6 agents; issues + labels sufficient at this scale |
