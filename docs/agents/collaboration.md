# Collaboration Skill — Agent Team

This file defines the inter-agent workflow. The primary project is **sudoku-solver** (`sudoku-solver-bot/sudoku-solver`). There is also a **video-pipeline** project (`novaclawhk/video-pipeline`) that follows the same workflow patterns. **Every agent must read this on session startup.**

## Session Rules

### Startup Repo Guard (MANDATORY)
- **Every agent MUST verify their working directory** at session startup before doing any work
- Expected repo paths: `~/repos/sudoku-solver/` (primary) or `~/repos/video-pipeline/` (secondary)
- If `cwd` does not match the expected repo, **abort immediately** and notify the sender/Planner
- This prevents accidental work in the wrong repository
- If an agent is explicitly redirected by William to another repo, this guard is waived for that session

### Dedicated Repo Clones (MANDATORY)
- **Every agent has its own dedicated `git clone`** — never use another agent's repo
- The video-pipeline project has 3 separate clones, one per agent context:
  | Agent | Clone Location | Purpose |
  |-------|---------------|---------|
  | **deployer** / **main (Nova)** | `~/.openclaw/workspace-deployer/video-pipeline/` | Production-facing — pull, deploy, inspect |
  | **coder** | `~/.openclaw/workspace-coder/repos/video-pipeline/` | Development — write, commit, PR |
  | **user repo** | `/home/claw1/repos/video-pipeline/` | User-facing — venv/systemd |
- **Do NOT** `git pull`, `git push`, or modify another agent's clone
- **Do NOT** change the working directory to another agent's repo path

### Named Persistent Sessions
- **Every agent MUST use their named persistent session key** for inter-agent communication
- **Every agent MUST know all other agents' session keys**

| Agent | Session Key | Telegram Bot |
|-------|-------------|--------------|
| ✨ Main (William) | `agent:main:telegram:direct:6437304507` | `@claw1_tele_bot` |
| 💻 Coder | `agent:coder:telegram:direct:6437304507` | `@nova_sudoku_coder_bot` |
| 🔍 Reviewer | `agent:reviewer:telegram:direct:6437304507` | `@nova_sudoku_reviewer_bot` |
| 🧪 Tester | `agent:tester:telegram:direct:6437304507` | `@nova_sudoku_tester_bot` |
| 📋 Planner | `agent:planner:telegram:direct:6437304507` | `@nova_sudoku_planner_bot` |
| 🏗️ Architect | `agent:architect:telegram:direct:6437304507` | `@nova_sudoku_architect_bot` |
| 🚀 Deployer | `agent:deployer:telegram:direct:6437304507` | `@nova_sudoku_deployer_bot` |

### Session Restart Notifications
- **After every session restart**, notify all agents you might interact with as soon as you come online, using their canonical session keys from the table above
- Send: `sessions_send(sessionKey="canonical:sessionKey", message="🔄 [<Agent>] Session restarted. New session is active.")`
- **Also append a session key refresh note** — other agents should run `sessions_list` to find your current live session key before calling `sessions_send` back
- This prevents missed messages due to stale session keys after gateway restarts

### Notify William
  ```
  message(action="send", channel="telegram", to="telegram:6437304507", accountId="<your-id>", message="<emoji> [<Agent>] <summary>")
  ```
- **Use the `message` tool**, NOT `sessions_send`, for Telegram notifications

---

## Workflow Pipeline

The canonical issue flow (for architecture/feature work):

```
Architect →Reviewer → Planner → Coder → Reviewer → Deployer → Tester
                            ↑                            │
                            └──── bug/suggestion ────────┘
```

### Quick Reference: Who Handles What

| Issue Type | Who Handles It | Notes |
|------------|---------------|-------|
| Code fix / feature | **Coder** | From Planner breakdown or bug report |
| Architecture decision (ADR) | **Reviewer** first, then **Planner** for breakdown | Architect creates, Reviewer approves, Planner breaks down |
| PR review / review feedback | **Reviewer** | All PRs need reviewer approval |
| Deployment / ops | **Deployer** | After PR merge |
| Testing concern | **Tester** | Integration tests, bug filing |
| Over budget (>25 min) | **Planner** | For breakdown into smaller sub-issues |
| Unclear / role mismatch | **Planner** | Planner reassigns to the correct agent |

### 1. ADR Flow (Architect → Reviewer → Planner → Coder)
- **Architect** creates ADRs and files as GitHub issues with `architecture` label
- **Architect** assigns the issue to **Reviewer** for architecture review
- **Reviewer** reviews the ADR:
  - If **approved** → **Reviewer reassigns the issue to Planner** for breakdown
  - If **changes requested** → **Reviewer notifies Architect** with feedback, repeat
- **Planner** reviews the approved ADR, sets effort estimation and priority, checks alignment with existing ADRs
- If over budget, **Planner** breaks it down into sub-issues and assigns to **Coder**
- **Coder** implements the sub-issues

### 2. Issue Pickup (by Coder)
- **Coder** picks up `plan` and `bug` issues from their queue
- Issues come from: Planner (broken-down ADRs) or Tester (bug reports)
- If a picked-up issue is over budget (>25 min), coder must flag it (see over-budget handling below)

### 3. Over-Budget Handling (Coder → Planner)
- If **Coder** estimates an issue is >25 minutes, it must:
  1. Add an estimation comment on the GitHub issue
  2. Notify **Planner** for breakdown: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="coder", message="💻 [Coder] Issue #N over budget: ~<X> min. Recommending planner breakdown.")`
- **Coder** does NOT work on over-budget issues

### 4. PR Review (Reviewer)
- After **Coder** creates a PR, **Reviewer** reviews it
- **If changes requested:**
  1. **Reviewer** leaves a review comment with specific feedback
  2. **Reviewer** notifies **Coder**: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="reviewer", message="🔍 [Reviewer] PR #N changes requested: <summary>. Coder please fix.")`
  3. **Coder** fixes and re-pushes
- **If approved and merged:**
  1. **Reviewer** notifies **Deployer**: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="reviewer", message="🔍 [Reviewer] PR #N merged. Commit: <hash>. Please deploy.")`

### 4b. PR Comments — Tags & Notifications
- **Anyone** (William, any agent) may leave a comment on a PR — always **tag the responsible agent** with `@<agent-github-username>`:
  - Code issue → tag **Coder** (nova-sudoku-coder)
  - Architecture concern → tag **Architect** (nova-sudoku-architect)
  - Review feedback → tag **Reviewer** (nova-sudoku-reviewer)
  - Deployment/ops → tag **Deployer** (nova-sudoku-deployer)
  - Testing concern → tag **Tester** (nova-sudoku-tester)
  - Wrong role / unclear → tag **Planner** (nova-sudoku-planner)
- **If you tagged an agent**, notify them via `sessions_send` with the PR number, link, and a summary of the comment
- **All agents**: on every session startup, check your GitHub mentions / tags on open PRs and respond

### 5. Deployment (Deployer)
- **Deployer** receives merge notification from **Reviewer**
- **Deployer** deploys the latest version
- **Deployer** notifies **Tester**: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="deployer", message="🚀 [Deployer] PR #N deployed. Commit: <hash>. Health: OK. Please start testing.")`

### 6. Testing (Tester)
- **Tester** receives deploy notification from **Deployer**
- **Tester** runs integration tests against the live site
- **If bugs found:**
  1. **Tester** creates a bug issue on GitHub (label: `bug`)
  2. **Tester** notifies **Coder**: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="tester", message="🧪 [Tester] Bug found: <title>. Issue #N created. Please fix.")`
- **If testable code found:**
  1. **Tester** checks if testing task can be converted to unit test
  2. If yes, **Tester** creates a suggestion issue (label: `suggestion`)
  3. **Tester** notifies **Coder**: `message(action="send", channel="telegram", to="telegram:6437304507", accountId="tester", message="🧪 [Tester] Suggestion: <title>. Issue #N created. Consider adding unit test.")`

### 7. ADR & Discussions
- **ADRs** are formal architecture decision records (GitHub issues with `architecture` label)
- **GitHub Discussions** is the informal forum for team conversations (enable it in repo settings)
- Anyone can comment on ADRs and Discussions (agents or William)
- **Tester** reviews ADRs to generate/update test plans
- If tester finds a missed testing area, it proposes a new ADR
- All agents should read ADRs to stay aligned on architecture decisions
- Use Discussions for: brainstorming, questions, ideas, informal debates
- Use ADRs for: formal decisions, architecture proposals, design records

---

## Service Disruption Protocol

Any agent that stops, restarts, or modifies a **production** (`sudoku-solver.service`) or **staging** (`sudoku-solver-staging.service`) systemd service must follow this protocol to prevent extended outages.

### 1. Notify Deployer
- Via `sessions_send` to `agent:deployer:telegram:direct:6437304507` **before** stopping/restarting/modifying either service
- For **emergency stops** (no time to notify beforehand): send the notification **immediately after** with the reason
- Include: which service, what action, and the reason

### 2. Document the Reason
- Comment on the relevant GitHub issue, session log, or memory entry
- Include: what was done, why, and any observed effects

### 3. Restore
- Restore the service to its original state (enabled + started) when investigation or maintenance is complete:
  ```bash
  sudo systemctl enable sudoku-solver.service  # or sudoku-solver-staging.service
  sudo systemctl start sudoku-solver.service
  ```
- Verify with: `sudo systemctl status sudoku-solver.service`

### 4. Confirm with Deployer
- Notify Deployer after restoration via `sessions_send`
- Deployer confirms both services are healthy before the task is considered complete

---

## GitHub Issue Assignment Protocol

When a GitHub issue is **assigned to an agent**, that agent must:

1. **Read the issue** — understand what's being asked
2. **Assess** — does the work match their role and budget?
3. **Act**:
   - ✅ **Fits their role**: Work on it
   - ❌ **Wrong role**: Reassign to the correct agent with a clear reason
   - ❌ **Over budget**: Reassign to Planner, specifying the estimated effort

### Reassignment Rules

| Issue Type | Assigned To → Reassign To | Reason |
|------------|--------------------------|--------|
| Too large (>25 min) | ANY → **Planner** | "Over budget (~X min), needs planner breakdown" |
| Bug report | Tester → **Coder** | "Bug report, needs coder fix" |
| Bug report | Reviewer → **Coder** | "Bug report, needs coder fix" |
| Architecture decision | Coder/Planner/Reviewer → **Architect** | "Architecture decision, needs architect" |
| ADR review | Architect → **Reviewer** | "ADR review, needs reviewer" |
| Implementation (general) | Planner → **Coder** | "Implementation, needs coder" |
| Implementation | Planner → **Coder** | "Implementation, needs coder" |
| Testing | Coder/Reviewer → **Tester** | "Testing, needs tester" |
| Deployment/Infra | ANY → **Deployer** | "Deployment/infra, needs deployer" |
| PR Review | ANY → **Reviewer** | "PR review, needs reviewer" |
| Planning/Breakdown | ANY → **Planner** | "Needs planner breakdown" |

### After Reassigning
1. **Comment on the issue** explaining why you're reassigning
2. **Send a message** via `sessions_send` to the target agent with: issue number, title, reason
3. **Notify William** via `message` tool with a brief summary

---

## Issue Labels

| Label | Meaning |
|-------|---------|
| `plan` | Planner-owned, awaiting breakdown |
| `bug` | Bug found by tester |
| `suggestion` | Tester suggestion for unit test conversion |
| `architecture` | Architect-owned, ADR-related |
| `priority: high` | High priority |
| `priority: medium` | Medium priority |
| `priority: low` | Low priority |

---

## Communication Protocol

### Message Format
Always use the `message` tool for Telegram notifications:
```
message(action="send", channel="telegram", to="telegram:6437304507", accountId="<your-agent-id>", message="<emoji> [<Agent Name>] <message>")
```

### What to Include
- PR number and commit hash (for merges/deploys)
- Issue number and title (for new issues/bugs)
- Specific action required (fix, deploy, test, review, breakdown)
- Brief summary of the situation

### What NOT to Do
- Do NOT use `sessions_send` for Telegram notifications
- Do NOT skip notifying William after receiving inter-agent messages
- Do NOT merge your own PRs (branch protection requires different account)
- Do NOT close issues you didn't create

### Where to Discuss
- **GitHub Issues** — formal work items, bugs, tasks
- **GitHub Discussions** — informal team conversations, brainstorming, questions
  - All agents have write access (can create posts and comment)
  - Use `gh api graphql` to create and interact with Discussions
  - Categories: Announcements, General, Ideas, Q&A, Show and Tell
- **ADRs (issues)** — architecture decisions, design proposals
- **Telegram** — urgent notifications, deployment alerts

---

## Agent-Specific Responsibilities

### 🏗️ Architect
- Create ADRs for architecture decisions
- Convert ADRs into GitHub issues with `architecture` label
- **Assign ADR issues to Reviewer** for architecture review — do NOT assign directly to Coder
- Notify Reviewer of new ADRs for review
- If Reviewer approves the ADR, reassign to Planner for breakdown
- If Reviewer requests changes, update the ADR and re-submit to Reviewer
- Review pipeline for coordination improvements

### 📋 Planner
- Review all new issues (from Architect and others)
- Set effort estimation and priority labels
- Check alignment with ADRs
- Break down over-budget issues (>25 min) into sub-issues
- Send work orders to Coder
- **Staleness monitor**: every session, scan ALL open issues and PRs — if any agent missed an issue assignment or was tagged in a PR comment without responding, message that agent to take action
- If an issue is stuck (no activity >1 session), nudge the responsible agent
- If a PR has review comments with no coder response >30 min, nudge the coder

### 💻 Coder
- Pick up issues by priority
- Create branches and PRs
- Fix review feedback
- If over budget: comment estimation, notify Planner
- Do NOT merge own PRs
- If assigned an issue that doesn't match your role, **reassign** with a reason
- **Do NOT package or deploy** — stop at merged PR; packaging + deployment is the deployer's job

### 🔍 Reviewer
- Review all PRs for correctness, quality, coverage
- Request changes or approve
- Merge approved PRs
- **After merging a PR: reassign the issue to Deployer** and send a message like: `🔍 [Reviewer] PR #N merged. Issue reassigned to you — please deploy. Commit: <hash>.`
- **Review ADRs from Architect** — check architecture soundness
  - If approved → reassign the issue to **Planner** for breakdown
  - If changes needed → notify **Architect** with feedback
- **Do NOT assign ADR issues directly to Coder** — Planner must break them down into sub-issues first

### 🚀 Deployer
- **Package** the installable unit from source (build jar, build pip wheel, etc.)
- **Deploy / upgrade** the packaged unit to the local machine
- **Verify** health checks after deployment
- **After successful deployment: reassign the issue to Tester** and send a message like: `🚀 [Deployer] Deployed PR #N (commit <hash>). Issue reassigned to you — please test.`
- **If deployment fails**: reassign the issue back to **Coder** with error details

### 🧪 Tester
- Test deployed versions
- **When notified by Deployer about a deployed issue**:
  - Run tests on the deployed app
  - **If all checks pass**: close the issue
  - **If any check fails**: add test results + analysis to the issue, **reassign it back to Coder**, and send a message to Coder with the failure details and analysis
- Create bug issues for failures
- Create suggestion issues for testable code
- Notify Coder of bugs/suggestions
- **Generate and update test plans based on ADRs**
- If any important testing area is missed, **propose a new ADR**
- ADRs are discussion forums — anyone (agents or William) can comment and contribute
