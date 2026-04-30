# Sudoku Dojo — Agent System

Five specialized agents manage the Sudoku Dojo project autonomously.

## Agents

| Agent | Role | Schedule | Skill |
|---|---|---|---|
| 🚀 **Deployer** | Deploy from master to local server | Every 15 min | [skill](skills/sudoku-deployer.md) · [profile](profiles/sudoku-deployer.md) |
| 📋 **Planner** | Read code/logs, create implementation plans | Daily 9 AM HKT | [skill](skills/sudoku-planner.md) · [profile](profiles/sudoku-planner.md) |
| 💻 **Coder** | Implement plans via git flow | Every 1 hour | [skill](skills/sudoku-coder.md) · [profile](profiles/sudoku-coder.md) |
| 👀 **Reviewer** | Review PRs, resolve conflicts, merge | Every 3 hours | [skill](skills/sudoku-reviewer.md) · [profile](profiles/sudoku-reviewer.md) |
| 🧪 **Tester** | Active QA: test all endpoints, tutorials, edge cases | Every 6 hours | [skill](skills/sudoku-tester.md) · [profile](profiles/sudoku-tester.md) |

## Workflow

```
Tester (finds bugs) → Planner (creates fix plans) → Coder (implements) → Reviewer (merges) → Deployer (deploys)
```

## Current Priority

**Bug Squash Phase** — no new features or i18n languages. Focus on finding and fixing bugs only.

## Files

- `skills/` — Detailed instructions each agent follows (SKILL.md)
- `profiles/` — Agent persona and scope (AGENTS.md)
