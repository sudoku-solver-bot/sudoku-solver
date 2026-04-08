# Branch Protection Rules

## Overview

This directory contains scripts and documentation for setting up and maintaining branch protection rules for the sudoku-solver repository.

## Branch Protection Configuration

The following branch protection rules are configured for the `master` branch:

### Pull Request Requirements
- **Required approvals**: 1
- **Dismiss stale reviews**: Enabled (new commits require re-approval)
- **Code owner reviews**: Optional
- **Last push approval**: Not required

### Status Checks
- **Required checks**: `Java CI`
- **Strict mode**: Enabled (branch must be up to date with base branch)
- **Context verification**: All CI checks must pass before merging

### Branch Restrictions
- **Force pushes**: Disabled
- **Deletions**: Disabled
- **Linear history**: Required (no merge commits, use squash/rebase)
- **Admin enforcement**: Enabled (rules apply to admins too)

## Applying the Rules

### Prerequisites
- GitHub CLI (`gh`) installed
- Admin permissions on `sudoku-solver-bot/sudoku-solver`

### Setup Instructions

1. **Using the provided script**:
   ```bash
   cd .github/scripts
   ./setup-branch-protection.sh
   ```

2. **Manual configuration via GitHub UI**:
   - Navigate to Repository Settings
   - Click "Branches" in the left sidebar
   - Click "Add rule" or edit existing `master` rule
   - Configure settings as documented above
   - Click "Create" or "Save changes"

3. **Manual configuration via GitHub API**:
   ```bash
   # See setup-branch-protection.sh for the complete API call
   ```

## Security Rationale

### Why These Rules?

1. **Pull Request Reviews**: Ensures code changes are reviewed before merging, catching bugs and maintaining code quality.

2. **Status Checks**: Guarantees all tests pass before code enters the main branch, preventing broken builds.

3. **Strict Mode**: Requires feature branches to be up to date, integrating recent changes and reducing merge conflicts.

4. **Linear History**: Produces cleaner git history, making debugging and code archeology easier.

5. **No Force Pushes**: Prevents accidental or malicious history rewriting on the main branch.

6. **No Deletions**: Protects against accidental branch deletion of the main development line.

7. **Admin Enforcement**: Ensures even repository administrators follow the same processes.

### Security Benefits

- **Code Review**: Mandatory peer review reduces security vulnerabilities and bugs
- **CI Validation**: Automated tests must pass, preventing integration issues
- **Audit Trail**: Linear history provides clear traceability of changes
- **Change Management**: Structured workflow prevents unauthorized direct commits
- **Protection Against Accidents**: Prevents force pushes and deletions that could lose work

## Bypassing Protection Rules

In emergency situations, administrators can temporarily bypass protection rules:

1. Go to Repository Settings → Branches
2. Edit the master branch protection rule
3. Uncheck "Enforce on admins"
4. Make necessary changes
5. Re-enable admin enforcement immediately after

**Warning**: Only bypass in genuine emergencies. Document any bypass incidents.

## Monitoring and Compliance

### Checking Current Protection Status

```bash
gh api repos/sudoku-solver-bot/sudoku-solver/branches/master/protection
```

### Auditing Changes

Regular audits should verify:
- Protection rules remain active
- Required status checks match CI workflows
- Review count requirements are appropriate
- No unauthorized bypasses have occurred

## Related Documentation

- [GitHub Branch Protection Docs](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches)
- [CI/CD Workflows](../../.github/workflows/)
- [Contributing Guidelines](../../CONTRIBUTING.md)

## Troubleshooting

### "Insufficient permissions" error
- Ensure you have admin access to the repository
- Check that your GitHub token has the correct scopes

### Status checks not found
- Verify CI workflow names match those configured
- Check that workflows are active in Actions tab

### Cannot bypass when needed
- Contact repository owner or organization admin
- Document the emergency situation and reason for bypass

## Maintenance

Branch protection rules should be reviewed:
- After major CI/CD changes
- When team workflow evolves
- During periodic security audits (quarterly recommended)

## Version History

- **2026-03-30**: Initial branch protection rules created as part of security hardening (#24)
