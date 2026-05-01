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

## Deploy Procedure

### Full Deploy (pull + build + restart)

```bash
set -e

cd /home/claw1/repos/sudoku-solver
git fetch origin
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/master)

if [ "$LOCAL" = "$REMOTE" ]; then
  echo "Already up to date: $LOCAL"
  exit 0
fi

echo "Updating: $LOCAL -> $REMOTE"
git log --oneline $LOCAL..$REMOTE

# Pull latest
git checkout master
git pull --ff-only origin master

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

1. Never push to master — this agent only pulls and deploys
2. Always `--ff-only` to avoid merge commits
3. Verify health after restart — if health check fails, roll back:
   ```bash
   # Rollback: checkout previous commit and rebuild
   git checkout $LOCAL
   ./gradlew :web:installDist --no-daemon
   rm -rf /tmp/sudoku-debug && mkdir -p /tmp/sudoku-debug && cp -r web/build /tmp/sudoku-debug/web
   sudo systemctl restart sudoku-solver
   ```
4. Report deployment result (commit hash, status, health)
