package hin.sudoku.solver;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import will.sudoku.solver.*;

import java.util.concurrent.TimeUnit;

public class SolverBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void solvePuzzle(Blackhole bh, BenchmarkState state) {
        Settings.INSTANCE.getExclusionCandidateEliminator().setShortCircuitThreshold(state.groupExecutionEliminatorThreshold);
        Solver solver = new Solver();
        bh.consume(solver.solve(state.board));
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"g1", "g2", "g3", "g4"})
        public String boardName;

        @Param({"0", "3", "6", "9"})
        public int groupExecutionEliminatorThreshold;

        public Board board;

        @Setup(Level.Trial)
        public void setUp() {
            board = BoardReader.readBoard(SolverTest.class.getResourceAsStream("/solver/www.sudokuweb.org/" + boardName + ".question"));
        }
    }
}
