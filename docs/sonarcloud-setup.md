# SonarCloud Setup

This project uses [SonarCloud](https://sonarcloud.io) for continuous code quality analysis.

## Features

- ✅ **Code Smell Detection** - Identifies maintainability issues
- ✅ **Security Vulnerabilities** - Spots potential security problems
- ✅ **Code Coverage** - Tracks test coverage via JaCoCo
- ✅ **PR Analysis** - Analyzes pull requests automatically
- ✅ **Quality Gates** - Ensures code meets quality standards
- ✅ **Technical Debt Tracking** - Monitors code health over time

## Dashboard

📊 **SonarCloud Dashboard**: https://sonarcloud.io/project/overview?id=sudoku-solver-bot_sudoku-solver

## Setup (One-time)

### 1. Create SonarCloud Account

1. Go to https://sonarcloud.io
2. Sign up with your GitHub account
3. Authorize SonarCloud to access your GitHub repositories

### 2. Create Project

1. Go to https://sonarcloud.io/projects/create
2. Select "sudoku-solver-bot/sudoku-solver" repository
3. Click "Set Up"

### 3. Generate Token

1. Go to https://sonarcloud.io/account/security
2. Generate a new token named "GitHub Actions"
3. Copy the token (you won't see it again!)

### 4. Add GitHub Secret

1. Go to https://github.com/sudoku-solver-bot/sudoku-solver/settings/secrets/actions
2. Click "New repository secret"
3. Name: `SONAR_TOKEN`
4. Value: Paste the token from step 3
5. Click "Add secret"

### 5. Trigger Analysis

Push to `master` or create a PR - GitHub Actions will automatically run SonarCloud analysis!

## Local Analysis

To run SonarCloud analysis locally:

```bash
# Set your SonarCloud token
export SONAR_TOKEN=your_token_here

# Run analysis
./gradlew sonarqube
```

## Configuration Files

- **Root**: `build.gradle.kts` - SonarCloud plugin configuration
- **Properties**: `sonar-project.properties` - Project settings
- **Workflow**: `.github/workflows/gradle.yml` - CI/CD integration
- **Coverage**: `kotlin/build.gradle.kts` - JaCoCo configuration

## Quality Gate

The project uses SonarCloud's default Quality Gate:

- ✅ **Coverage**: > 80% on new code
- ✅ **Duplications**: < 3% on new code
- ✅ **Maintainability**: A rating on new code
- ✅ **Reliability**: A rating on new code
- ✅ **Security**: A rating on new code

## Interpreting Results

### Ratings

- **A** ✅ - Excellent
- **B** 🟡 - Good
- **C** 🟠 - Fair
- **D** 🔴 - Poor
- **E** ❌ - Critical issues

### Issue Types

- **🐛 Bug** - Something that will break code
- **🧠 Code Smell** - Maintainability issue
- **🔓 Vulnerability** - Security risk
- **🔴 Security Hotspot** - Needs security review

## Troubleshooting

### Analysis Fails

1. Check `SONAR_TOKEN` is set in GitHub Secrets
2. Verify project key matches: `sudoku-solver-bot_sudoku-solver`
3. Check SonarCloud organization: `sudoku-solver-bot`

### Low Coverage

1. Run tests locally: `./gradlew test`
2. Generate coverage: `./gradlew jacocoTestReport`
3. Check report: `kotlin/build/reports/jacoco/test/html/index.html`

### PR Analysis Missing

1. Ensure PR is from a branch (not fork)
2. Check GitHub Actions workflow completed
3. Verify `SONAR_TOKEN` has access to PR

## Resources

- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [SonarScanner for Gradle](https://docs.sonarcloud.io/advanced-setup/ci-based-analysis/sonarscanner-for-gradle/)
- [Kotlin Quality Profiles](https://rules.sonarsource.com/kotlin/)
