---
name: sudoku-deployer
description: Deploy sudoku-solver from the main branch to the local machine. Monitors the GitHub repo for new merges, pulls, builds, and restarts the local systemd service. Use when asked to deploy, redeploy, check deployment status, or sync local with remote.
---

# Sudoku Deployer

Deploys the sudoku-solver project to the local machine.

## Target

- **Repo:** `/home/claw1/repos/sudoku-solver`
- **Remotes:** `origin` = `sudoku-solver-bot/sudoku-solver`, `fork` = `novaclawhk/sudoku-solver`
- **Branch:** `master`
- **Systemd service:** `sudoku-solver.service` (PORT=25321)
- **Service binary:** `/tmp/sudoku-debug/web/build/install/web/bin/web`
- **Local URL:** `http://localhost:25321`

## Golden Rule: Always Deploy Master

- **Always deploy from `origin/master`** — never from local branches, feature branches, or commits ahead of master
- Before any deploy operation, ensure the local repo is on the `master` branch and in sync with `origin/master`
- If working on a feature branch, stash or discard changes and switch to `master` first
- The deployer's only job is to keep the local service in sync with `origin/master`

## Deploy Procedure

### Commit Tracking

The deployer tracks the last deployed commit in a `.deploy-commit` file at the repo root:

```bash
DEPLOY_FILE="/home/claw1/repos/sudoku-solver/.deploy-commit"

# Read last deployed commit
if [ -f "$DEPLOY_FILE" ]; then
  DEPLOYED=$(cat "$DEPLOY_FILE")
else
  DEPLOYED=""
fi
```

**Do not rebuild if the deployed commit already matches `origin/master`.** Only rebuild when master has new commits.

### Full Deploy (pull + build + restart)

```bash
set -e

cd /home/claw1/repos/sudoku-solver

# ALWAYS switch to master first
git checkout master
git fetch origin

LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/master)

# Check .deploy-commit for last deployed commit
DEPLOY_FILE=".deploy-commit"
if [ -f "$DEPLOY_FILE" ]; then
  DEPLOYED=$(cat "$DEPLOY_FILE")
  if [ "$DEPLOYED" = "$REMOTE" ]; then
    echo "Already deployed at $DEPLOYED — nothing to do"
    exit 0
  fi
fi

# Only deploy if remote master is ahead of what's currently served
if [ "$LOCAL" != "$REMOTE" ]; then
  echo "Updating local: $LOCAL -> $REMOTE"
  git log --oneline $LOCAL..$REMOTE
  git pull --ff-only origin master
fi

# Build frontend
cd web-ui && npm install && npm run build && cd ..

# Build backend
./gradlew :web:installDist --no-daemon

# Copy to deploy directory
rm -rf /tmp/sudoku-debug
mkdir -p /tmp/sudoku-debug
cp -r web/build /tmp/sudoku-debug/web

# Restart service
sudo systemctl restart sudoku-solver
sleep 3
sudo systemctl status sudoku-solver --no-pager

# Record deployed commit
echo "$REMOTE" > "$DEPLOY_FILE"
echo "Deployed: $REMOTE"
```

### Quick Status Check

```bash
# Service status
sudo systemctl status sudoku-solver --no-pager

# Health endpoint
curl -sf http://localhost:25321/api/health | python3 -m json.tool

# Recent logs (last 50 lines)
sudo journalctl -u sudoku-solver -n 50 --no-pager
```

### View Logs

```bash
# Follow logs
sudo journalctl -u sudoku-solver -f

# Recent errors
sudo journalctl -u sudoku-solver -p err --no-pager -n 30
```

## Rules

1. **Always deploy from `origin/master`** — never from local branches or ahead-of-master commits
2. **Check `.deploy-commit` first** — skip rebuild if the deployed commit already matches `origin/master`
3. **Always switch to master** before any deploy operation
4. Never push to master — this agent only pulls and deploys
5. Always `--ff-only` to avoid merge commits
6. Verify health after restart — if health check fails, roll back:
   ```bash
   # Rollback: checkout previous commit and rebuild
   git checkout $LOCAL
   ./gradlew :web:installDist --no-daemon
   rm -rf /tmp/sudoku-debug && mkdir -p /tmp/sudoku-debug && cp -r web/build /tmp/sudoku-debug/web
   sudo systemctl restart sudoku-solver
   ```
4. Report deployment result (commit hash, status, health)
