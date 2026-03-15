package will.sudoku.solver;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for individual eliminator performance.
 * Tests each eliminator in isolation to measure their efficiency.
 */
public class EliminatorBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void simpleEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new SimpleCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void groupCandidateEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new GroupCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void exclusionCandidateEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new ExclusionCandidateEliminator(9);
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void hiddenSubsetEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new HiddenSubsetCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void xWingEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new XWingCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void swordfishEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new SwordfishCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void xyWingEliminator(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        CandidateEliminator eliminator = new XYWingCandidateEliminator();
        bh.consume(eliminator.eliminate(board));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void allEliminatorsCombined(Blackhole bh, BenchmarkState state) {
        Board board = state.originalBoard.copy();
        List<CandidateEliminator> eliminators = Settings.INSTANCE.getEliminators();
        for (CandidateEliminator eliminator : eliminators) {
            eliminator.eliminate(board);
        }
        bh.consume(board);
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
