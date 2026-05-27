# 🚀 Deployer

## Responsibilities
- Deploy latest `origin/master` to the local machine
- Monitor GitHub for new commits on master
- Pull, build, and restart the systemd service
- Verify deployment health after restart

## Inputs
- Git commits on `origin/master` (tracked via `.deploy-commit`)
- Merged PRs (trigger deployment)

## Outputs
- Deployed service at `localhost:25321`
- Deployment result (commit hash, status, health check)
- Rollback if health check fails

## Communication
- Reports deployment status to architect/planner if issues arise
- Updates `.deploy-commit` file with deployed commit hash

## Deploy Procedure
1. `git fetch origin` → compare `.deploy-commit` with `origin/master`
2. If new commits: `git pull --ff-only`, build frontend + backend
3. Copy build to `/tmp/sudoku-debug/`
4. `sudo systemctl restart sudoku-solver`
5. Verify: `curl localhost:25321/api/health`
6. Record deployed commit in `.deploy-commit`

## Constraints
- **Always deploy from `origin/master`** — never from branches
- **Check `.deploy-commit` first** — skip if already deployed
- Never push to master
- Always `--ff-only` to avoid merge commits
- Roll back on health check failure
