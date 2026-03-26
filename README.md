# Sudoku Solver

A high-performance Sudoku solver implemented in Kotlin, featuring multiple constraint propagation strategies and backtracking with JMH benchmarking capabilities.

## Overview

This project implements a fast Sudoku solver that combines:
- **Bitmask-based candidate representation** for efficient state management
- **Three elimination strategies** for constraint propagation
- **Backtracking search** with minimum remaining values heuristic
- **JMH benchmarks** for performance validation

## Features

- **Efficient Candidate Management**: Uses bitmask patterns (9-bit integers) to represent possible values
- **Three Elimination Techniques**:
  - *Simple Eliminator*: Removes confirmed values from peer cells
  - *Group Eliminator*: Detects naked pairs/triples (naked subsets)
  - *Exclusion Eliminator*: Detects hidden singles
- **Configurable Thresholds**: The exclusion eliminator can skip groups with too many known values
- **Benchmarking**: JMH microbenchmarks for performance analysis

## Project Structure

```
sudoku-solver/
├── kotlin/                    # Kotlin implementation
│   ├── src/main/
│   │   └── java/will/sudoku/solver/
│   │       ├── Board.kt              # Board state & candidate patterns
│   │       ├── Solver.kt             # Main solver with backtracking
│   │       ├── Settings.kt            # Configuration constants
│   │       ├── Coord.kt               # Cell coordinates (row, col)
│   │       ├── CoordGroup.kt          # Row/column/region groups
│   │       ├── CandidateEliminator.kt # Interface for eliminators
│   │       ├── SimpleCandidateEliminator.kt
│   │       ├── GroupCandidateEliminator.kt
│   │       ├── ExclusionCandidateEliminator.kt
│   │       └── BoardReader.kt         # Parse boards from files/strings
│   ├── src/test/             # JUnit 5 tests
│   └── src/jmh/              # JMH benchmarks
```

## Building

### Prerequisites

- JDK 21 or higher
- Gradle 7.x+ (or use the included Gradle wrapper)

### Build Commands

```bash
# Build the project
./gradlew build

# Clean build artifacts
./gradlew clean
```

## Running Tests

```bash
# Run all tests
./gradlew test
```

Tests use parameterized board files in `kotlin/src/test/resources/solver/`. Boards are read in pairs:
- `<name>.question` - The unsolved puzzle
- `<name>.solution` - The expected solution

## Running Benchmarks

```bash
# Run JMH benchmarks
./gradlew :kotlin:jmh
```

The benchmarks test solving performance with different `shortCircuitThreshold` values (0, 3, 6, 9) across multiple puzzle files (g1-g4).

## Board Format

Boards are represented as text with the following format:

- `.` or `0` - Empty/unknown cell
- `1-9` - Confirmed value
- `!` - Column separator (every 3 columns, for 3x3 regions)
- `-` - Row separator (every 3 rows, for 3x3 regions)

Example:
```
.4.!3.8!1..
21.!.65!...
6..!...!.7.
---!---!---
9.3!.46!781
...
```

## Usage Example (Kotlin)

```kotlin
import will.sudoku.solver.*

// Parse a board from string
val board = BoardReader.readBoard("""
    .4.!3.8!1..
    21.!.65!...
    6..!...!.7.
    ---!---!---
    9.3!.46!781
    1.4!829!5.6
    8.5!...!.2.
    ---!---!---
    4..!...!6.3
    ...!6.2!.47
    .8.!.3.!...
""".trimIndent())

// Solve the puzzle
val solved = Solver().solve(board)

if (solved != null) {
    println(solved)
} else {
    println("No solution found")
}
```

## Architecture

### Candidate Pattern Representation

The solver uses bitmask patterns to efficiently track possible values for each cell:

- Each cell has a 9-bit integer representing candidates
- Bit `i` (0-indexed) corresponds to value `i+1`
- Example: `0b000001010` = values 2 and 4 are candidates
- Confirmed cell = only one bit set (single value)

```kotlin
// Value 5 is confirmed: 0b000010000 (bit 4 set)
// All candidates: 0b111111111 (all 9 bits)
```

### Solver Algorithm

1. **Constraint Propagation**: Apply all eliminators until no more changes
   - `SimpleCandidateEliminator`: Remove known values from peers
   - `GroupCandidateEliminator`: Find naked pairs/triples in groups
   - `ExclusionCandidateEliminator`: Find hidden singles in groups

2. **Select Cell**: Choose the cell with minimum remaining candidates (MRV heuristic)

3. **Backtrack**: Try each candidate value recursively
   - After placing a value, apply eliminators again
   - If conflict occurs, backtrack and try next candidate

### Coordinate System

- `Coord(row: Int, col: Int)` - Zero-indexed (0-8)
- `CoordGroup` - Represents rows, columns, and 3x3 regions
- Each cell belongs to 3 groups: its row, column, and region

## Configuration

The `Settings` object contains global configuration:

```kotlin
object Settings {
    const val size: Int = 9                           // 9x9 board
    val regionSize: Int = 3                          // 3x3 regions
    val symbols: CharArray = charArrayOf('.', '1'..'9')  // Display symbols
}
```

## Contributing

### Development Workflow

1. Fork the repository and create a feature branch
2. Make your changes
3. Run tests: `./gradlew test`
4. Run benchmarks if performance changes: `./gradlew :kotlin:jmh`
5. Submit a pull request with clear description of changes

### Adding New Elimination Strategies

1. Implement the `CandidateEliminator` interface:
```kotlin
class MyEliminator : CandidateEliminator {
    override fun eliminate(board: Board): Boolean {
        // Apply elimination rules
        // Return true if any changes were made
    }
}
```

2. Add to eliminators list in `Settings.kt`:
```kotlin
val eliminators = listOf(
    simpleCandidateEliminator,
    groupCandidateEliminator,
    exclusionCandidateEliminator,
    myEliminator  // Add here
)
```

3. Add tests in `kotlin/src/test/resources/solver/` with `<name>.question` and `<name>.solution` pairs

### Code Style

- Follow Kotlin coding conventions
- Use the `Settings` object for configuration constants
- Prefer bitmask operations over loops for candidate checking
- Add unit tests for any new functionality

### Pre-commit Hooks

This project uses [pre-commit](https://pre-commit.com/) to automatically check code quality before commits.

#### Setup

1. Install pre-commit (one-time setup):
```bash
# Using pip
pip install pre-commit

# Or using Homebrew (macOS)
brew install pre-commit
```

2. Install the hooks:
```bash
pre-commit install
```

3. (Optional) Run against all files to verify setup:
```bash
pre-commit run --all-files
```

#### What Gets Checked

Pre-commit automatically runs these checks on every commit:

- **File validation**: YAML, JSON, XML syntax
- **Merge conflicts**: Detects leftover conflict markers
- **Security**: Detects private keys and AWS credentials
- **Formatting**: Trailing whitespace, end-of-file fixes, line endings
- **Linting**: 
  - Kotlin (ktlint)
  - JavaScript/Vue (ESLint)
  - Dockerfiles (hadolint)
- **Large files**: Prevents committing files > 1MB

#### Bypass Hooks (Emergency Only)

```bash
# Skip pre-commit hooks (not recommended)
git commit --no-verify
```

#### Update Hooks

Periodically update to latest hook versions:
```bash
pre-commit autoupdate
```

For more information, see the [pre-commit documentation](https://pre-commit.com/).

## Performance

Benchmark results test different `shortCircuitThreshold` values:
- `0`: Apply exclusion eliminator to all groups (most thorough, slower)
- `3-6`: Skip groups with many known values (balanced)
- `9`: Skip exclusion eliminator entirely (faster, but may miss constraints)

Higher thresholds improve speed on simple puzzles but may increase backtracking on complex ones.

Benchmarks test different `shortCircuitThreshold` values:
- `0`: Apply exclusion eliminator to all groups (most thorough, slower)
- `3-6`: Skip groups with many known values (balanced)
- `9`: Skip exclusion eliminator entirely (faster, but may miss constraints)

Higher thresholds improve speed on simple puzzles but may increase backtracking on complex ones.

### Performance

### Optimization Tips

- The MRV (minimum remaining values) heuristic significantly reduces search space
- Bitmask operations are ~10x faster than array-based candidate tracking
- Pre-computed `Coord.all` and `CoordGroup.all` avoid repeated allocations

## Troubleshooting

### Build Issues

**Problem**: "Could not resolve dependencies"
```
Solution: Clean and rebuild: ./gradlew clean build
```

**Problem**: JMH benchmarks fail to run
```
Solution: Ensure JDK 11 is installed and JAVA_HOME is set correctly
```

### Solver Issues

**Problem**: Solver returns null for valid puzzles
```
Solution: Check board format - ensure separators (! and -) are placed correctly
```

**Problem**: Solver is slow on certain puzzles
```
Solution: Adjust shortCircuitThreshold in Settings or add more eliminators
```

**Problem**: OutOfMemoryError on large batches
```
Solution: Increase JVM heap size: ./gradlew :kotlin:test -Dorg.gradle.jvmargs="-Xmx2g"
```

### Board Format Issues

**Problem**: "Invalid board format" error
```
Solution: Ensure exactly 9 rows and 9 columns (excluding separators)
```

**Problem**: Solution doesn't match expected
```
Solution: Verify the .solution file is valid - run BoardReader.readBoard() on it
```

## Board Difficulty Classification

Based on solver performance:

- **Easy (< 10ms)**: Solved by constraint propagation alone, no backtracking needed
- **Medium (10-100ms)**: Requires minimal backtracking (few hundred guesses)
- **Hard (100-1000ms)**: Extensive backtracking (thousands of guesses)
- **Expert (> 1000ms)**: Maximum difficulty, requires advanced techniques

To classify a puzzle, run: `./gradlew :kotlin:jmh` and observe solve times.

## License

This project is open source. See repository for license details.