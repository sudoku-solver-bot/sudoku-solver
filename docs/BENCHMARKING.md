# Sudoku Solver Benchmarks

This project uses JMH (Java Microbenchmark Harness) for performance testing.

## Running Benchmarks

### Run all benchmarks
```bash
./gradlew :kotlin:jmh
```

### Run specific benchmark class
```bash
./gradlew :kotlin:jmh -PjmhInclude=SolverBenchmark
./gradlew :kotlin:jmh -PjmhInclude=EliminatorBenchmark
```

### Run specific benchmark method
```bash
./gradlew :kotlin:jmh -PjmhInclude=SolverBenchmark.solvePuzzle
```

### Run with specific parameters
```bash
# Test only g1 and g2 puzzles
./gradlew :kotlin:jmh -PjmhInclude=SolverBenchmark -PjmhParams=boardName=g1,boardName=g2
```

## Available Benchmarks

### SolverBenchmark
Tests full puzzle solving performance.

| Benchmark | Description |
|-----------|-------------|
| `solvePuzzle` | Solves a complete puzzle with all eliminators |

**Parameters:**
- `boardName`: Puzzle to solve (`g1`, `g2`, `g3`, `g4`)
- `groupExecutionEliminatorThreshold`: Threshold for exclusion eliminator (`0`, `3`, `6`, `9`)

### EliminatorBenchmark
Tests individual eliminator performance.

| Benchmark | Description |
|-----------|-------------|
| `simpleEliminator` | Simple candidate elimination |
| `groupCandidateEliminator` | Naked pairs/triples detection |
| `exclusionCandidateEliminator` | Hidden singles detection |
| `hiddenSubsetEliminator` | Hidden pairs/triples detection |
| `xWingEliminator` | X-Wing pattern detection |
| `swordfishEliminator` | Swordfish pattern detection |
| `xyWingEliminator` | XY-Wing chain detection |
| `allEliminatorsCombined` | All eliminators in sequence |

**Parameters:**
- `boardName`: Puzzle to test (`g1`, `g2`, `g3`, `g4`)

## Output Format

JMH outputs results in a table format:

```
Benchmark                               Mode  Cnt    Score    Error  Units
SolverBenchmark.solvePuzzle             avgt   10  123.456 ± 12.34  us/op
EliminatorBenchmark.simpleEliminator    avgt   10   45.678 ±  5.67  ns/op
```

- **Mode**: Average time (avgt)
- **Cnt**: Number of iterations
- **Score**: Average time per operation
- **Error**: Confidence interval
- **Units**: Microseconds (us) or nanoseconds (ns)

## CI/CD Integration

Benchmarks run via manual GitHub Actions workflow dispatch:
1. Go to Actions → JMH Benchmark
2. Click "Run workflow"
3. Select branch and run

**Note:** Benchmarks are not run on every PR to avoid long CI times.

## Adding New Benchmarks

1. Create a new Java class in `kotlin/src/jmh/java/will/sudoku/solver/`
2. Annotate with `@Benchmark` methods
3. Use `@State` for shared state
4. Run `./gradlew :kotlin:jmh` to verify

## Performance Tips

- Run on a quiet machine (minimize background processes)
- Run multiple iterations for stable results
- Compare results across commits to detect regressions
- Use the same JVM version for consistent comparisons
