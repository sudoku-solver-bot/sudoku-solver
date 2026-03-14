# Development Workflow

## Rules

1. **NEVER push directly to master** - Always use branches and PRs
2. **Create an issue first** - Document what the change is about
3. **Create a feature branch** - Branch from master with descriptive name
4. **Create a PR** - Request review before merging
5. **Wait for approval** - At least one approval required before merge

## Workflow

```
1. Create Issue → 2. Create Branch → 3. Make Changes → 4. Create PR → 5. Review → 6. Merge
```

### Branch Naming

- `feature/<description>` - New features
- `fix/<description>` - Bug fixes
- `test/<description>` - Test additions
- `docs/<description>` - Documentation updates
- `refactor/<description>` - Code refactoring

### Example

```bash
# 1. Create issue on GitHub
gh issue create --title "test: Add unit tests for eliminators" --body "Add comprehensive tests..."

# 2. Create branch
git checkout -b test/eliminator-unit-tests

# 3. Make changes and commit
git add .
git commit -m "test: Add unit tests for eliminators"

# 4. Push branch and create PR
git push -u origin test/eliminator-unit-tests
gh pr create --title "test: Add unit tests for eliminators" --body "Closes #X"

# 5. Request review
gh pr edit <number> --add-reviewer William1104

# 6. After approval, merge
gh pr merge <number> --squash
```

## Exceptions

- Emergency hotfixes may skip some steps (but should still have a PR)
- Trivial typo fixes in documentation can be direct pushed

## Enforcement

- CI runs on all PRs
- Branch protection rules should prevent direct pushes to master
