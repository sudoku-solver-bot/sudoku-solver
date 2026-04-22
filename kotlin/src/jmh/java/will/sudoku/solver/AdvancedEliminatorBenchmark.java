package will.sudoku.solver;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for advanced eliminator performance (Tier 1-3).
 * Tests each advanced eliminator in isolation to measure efficiency
 * on various puzzle difficulties.
 */
public class AdvancedEliminatorBenchmark {

    // --- Tier 1: Advanced Eliminators ---

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void xyWingEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new XYWingCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void wWingEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new WWingCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void simpleColoringEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new SimpleColoringCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void xyzWingEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new XYZWingCandidateEliminator();
        eliminator.eliminate(board);
    }

    // --- Tier 2: Expert Eliminators ---

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void forcingChainsEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new ForcingChainsCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void uniqueRectanglesEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new UniqueRectanglesCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void alsxzEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new ALSXZCandidateEliminator();
        eliminator.eliminate(board);
    }

    // --- Tier 3: Master Eliminators ---

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void frankenFishEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new FrankenFishCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void mutantFishEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new MutantFishCandidateEliminator();
        eliminator.eliminate(board);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void deathBlossomEliminator(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new DeathBlossomCandidateEliminator();
        eliminator.eliminate(board);
    }

    // --- Full Pipeline Benchmark ---

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void fullEliminationPipeline(BenchmarkState state) {
        Board board = state.originalBoard.copy();
        List<CandidateEliminator> eliminators = Settings.INSTANCE.getEliminators();
        for (CandidateEliminator eliminator : eliminators) {
            eliminator.eliminate(board);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void solveFullPuzzle(BenchmarkState state) {
        Solver solver = new Solver();
        solver.solve(state.originalBoard.copy());
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"g1", "g2", "g3", "g4"})
        public String boardName;

        public Board originalBoard;

        @Setup(Level.Trial)
        public void setUp() {
            String resourcePath = "/solver/www.sudokuweb.org/" + boardName + ".question";
            InputStream stream = SolverTest.class.getResourceAsStream(resourcePath);
            originalBoard = BoardReader.readBoard(stream);
        }
    }
}
