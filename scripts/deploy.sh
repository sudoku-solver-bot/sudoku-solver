#!/bin/bash
# Sudoku Dojo — Deploy Script
# Builds, deploys with atomic swap, smoke tests, and rolls back on failure.
#
# Usage:
#   ./scripts/deploy.sh              # Full deploy
#   ./scripts/deploy.sh --dry-run    # Show planned actions without executing
#   ./scripts/deploy.sh --rollback   # Restore previous backup
#
# Environment:
#   SUDOKU_DEPLOY_DIR   — Target directory (default: /opt/sudoku-solver)
#   SUDOKU_URL          — Smoke test URL (default: http://localhost:25321)

set -euo pipefail

# --- Configuration ---
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"
DEPLOY_DIR="${SUDOKU_DEPLOY_DIR:-/opt/sudoku-solver}"
BACKUP_DIR="${DEPLOY_DIR}.backup"
BUILD_DIR="$REPO_DIR/web/build/install/web"
SMOKE_TEST="$SCRIPT_DIR/smoke-test.sh"
SERVICE_NAME="sudoku-solver"
SMOKE_URL="${SUDOKU_URL:-http://localhost:25321}"
DRY_RUN=false
ROLLBACK=false
LOG_FILE=""

# --- Argument parsing ---
for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=true ;;
        --rollback) ROLLBACK=true ;;
        *) echo "Unknown argument: $arg"; exit 1 ;;
    esac
done

# --- Logging ---
init_log() {
    LOG_FILE="/tmp/sudoku-deploy-$(date +%Y%m%d-%H%M%S).log"
    exec 2> >(tee -a "$LOG_FILE" >&2)
    echo "=== Deploy started at $(date -u +%Y-%m-%dT%H:%M:%SZ) ===" | tee -a "$LOG_FILE"
    echo "Deploy dir: $DEPLOY_DIR" | tee -a "$LOG_FILE"
    echo "Log file: $LOG_FILE"
    if $DRY_RUN; then
        echo "** DRY RUN — no changes will be made **" | tee -a "$LOG_FILE"
    fi
    echo ""
}

log() {
    local ts
    ts="[$(date -u +%Y-%m-%dT%H:%M:%SZ)]"
    echo "$ts $*" | tee -a "${LOG_FILE:-/dev/null}"
}

die() {
    log "FATAL: $*"
    exit 1
}

# --- Dry-run wrapper ---
run() {
    if $DRY_RUN; then
        log "[DRY-RUN] Would run: $*"
    else
        log "Running: $*"
        "$@"
    fi
}

run_capture() {
    if $DRY_RUN; then
        log "[DRY-RUN] Would run: $*"
        echo "(dry-run)"
    else
        log "Running: $*"
        "$@"
    fi
}

# --- Rollback ---
do_rollback() {
    log "=== ROLLBACK ==="
    if [ ! -d "$BACKUP_DIR" ]; then
        die "No backup found at $BACKUP_DIR — nothing to roll back to"
    fi

    log "Stopping service..."
    run systemctl stop "$SERVICE_NAME" || true

    log "Restoring backup from $BACKUP_DIR..."
    if [ -d "$DEPLOY_DIR" ]; then
        run rm -rf "$DEPLOY_DIR"
    fi
    run mv "$BACKUP_DIR" "$DEPLOY_DIR"

    log "Starting service..."
    run systemctl start "$SERVICE_NAME"

    sleep 3
    log "Running smoke test on restored build..."
    if run_capture env SUDOKU_URL="$SMOKE_URL" bash "$SMOKE_TEST"; then
        log "Rollback successful — service restored and healthy"
    else
        log "WARNING: Smoke test failed after rollback. Service may be unstable."
    fi
}

# --- Main deploy ---
do_deploy() {
    log "=== BUILD ==="
    cd "$REPO_DIR"

    log "Building distribution..."
    run ./gradlew :web:installDist --no-daemon -q
    log "Build complete."

    if [ ! -d "$BUILD_DIR" ]; then
        die "Build output not found at $BUILD_DIR"
    fi

    log "=== ATOMIC SWAP ==="

    # If a previous backup exists, clean it up
    if [ -d "$BACKUP_DIR" ]; then
        log "Removing old backup..."
        run rm -rf "$BACKUP_DIR"
    fi

    # Move current live to backup
    if [ -d "$DEPLOY_DIR" ]; then
        log "Backing up current deployment..."
        run mv "$DEPLOY_DIR" "$BACKUP_DIR"
    fi

    # Atomically move new build into place
    log "Moving new build into place..."
    run mv "$BUILD_DIR" "$DEPLOY_DIR"

    log "=== RESTART SERVICE ==="
    run systemctl restart "$SERVICE_NAME"
    log "Waiting for service to stabilize..."
    sleep 3

    log "=== SMOKE TEST ==="
    if run_capture env SUDOKU_URL="$SMOKE_URL" bash "$SMOKE_TEST"; then
        log "✅ Smoke test passed — deploy successful"

        # Clean up backup on success (keep only the new version)
        if [ -d "$BACKUP_DIR" ] && ! $DRY_RUN; then
            log "Cleaning up backup..."
            rm -rf "$BACKUP_DIR"
        fi
    else
        log "❌ Smoke test FAILED — rolling back..."
        do_rollback
        exit 1
    fi

    log "=== Deploy complete at $(date -u +%Y-%m-%dT%H:%M:%SZ) ==="
    log "Log saved to: $LOG_FILE"
}

# --- Entry point ---
init_log

if $ROLLBACK; then
    do_rollback
else
    do_deploy
fi
