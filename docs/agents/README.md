# Sudoku Dojo — Agent System

Six specialized agents manage the Sudoku Dojo project autonomously.

## Agents

| Agent | Role | Schedule | Role File | Skill · Profile |
|---|---|---|---|---|
| 🏗️ **Architect** | Roadmap, ADRs, high-level direction | On request | [role](architect.md) | — |
| 📋 **Planner** | Read code/logs, create implementation plans | Daily 9 AM HKT | [role](planner.md) | [skill](skills/sudoku-planner.md) · [profile](profiles/sudoku-planner.md) |
| 💻 **Coder** | Implement plans via git flow | Every 1 hour | [role](coder.md) | [skill](skills/sudoku-coder.md) · [profile](profiles/sudoku-coder.md) |
| 👀 **Reviewer** | Review PRs, resolve conflicts, merge | Every 3 hours | [role](reviewer.md) | [skill](skills/sudoku-reviewer.md) · [profile](profiles/sudoku-reviewer.md) |
| 🧪 **Tester** | Active QA: test all endpoints, tutorials, edge cases | Every 6 hours | [role](tester.md) | [skill](skills/sudoku-tester.md) · [profile](profiles/sudoku-tester.md) |
| 🚀 **Deployer** | Deploy from master to local server | Every 15 min | [role](deployer.md) | [skill](skills/sudoku-deployer.md) · [profile](profiles/sudoku-deployer.md) |

## Workflow

```
Architect (defines) → Planner (decomposes) → Coder (implements) → Reviewer (merges) → Deployer (deploys)
                                                      ↑
                                                Tester (finds bugs)
```

## References

- [**UI/UX Guidelines**](../UI-GUIDELINES.md) — authoritative frontend UX reference for all agents.

## Current Priority

**Bug Squash Phase** — no new features or i18n languages. Focus on finding and fixing bugs only.

## Files

- `skills/` — Detailed instructions each agent follows (SKILL.md)
- `profiles/` — Agent persona and scope (AGENTS.md)
- Role files (`architect.md`, `coder.md`, `deployer.md`, `planner.md`, `reviewer.md`, `tester.md`) — concise role definitions
