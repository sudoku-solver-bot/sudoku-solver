#!/bin/bash
# Branch Protection Setup Script for sudoku-solver
# This script configures branch protection rules for the master branch
# Requires: GitHub CLI (gh) with admin permissions on the repository

set -e

REPO="sudoku-solver-bot/sudoku-solver"
BRANCH="master"

echo "Setting up branch protection rules for ${REPO}/${BRANCH}..."

# Configure branch protection rules
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  repos/${REPO}/branches/${BRANCH}/protection \
  -f required_pull_request_reviews='{
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true,
    "require_code_owner_review": false,
    "require_last_push_approval": false
  }' \
  -f enforce_admins=true \
  -f required_status_checks='{
    "strict": true,
    "contexts": [],
    "checks": [
      {"context": "Java CI"}
    ]
  }' \
  -f restrictions=null \
  -f allow_force_pushes=false \
  -f allow_deletions=false \
  -f required_linear_history=true

echo "✓ Branch protection rules configured successfully!"
echo ""
echo "Summary of rules applied:"
echo "  • Pull request reviews required: 1 approval"
echo "  • Dismiss stale reviews: enabled"
echo "  • Require status checks: Java CI"
echo "  • Strict status checks: enabled (must be up to date)"
echo "  • Enforce on admins: enabled"
echo "  • Force pushes: disabled"
echo "  • Branch deletions: disabled"
echo "  • Linear history: required"
echo ""
