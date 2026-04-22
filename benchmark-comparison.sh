#!/usr/bin/env bash
# Sudoku Solver - Benchmark Comparison Suite
# Runs JMH benchmarks and generates a performance comparison report.
#
# Usage:
#   ./benchmark-comparison.sh [options]
#   ./benchmark-comparison.sh --quick          # Quick run (fewer iterations)
#   ./benchmark-comparison.sh --eliminators    # Only basic eliminators
#   ./benchmark-comparison.sh --advanced       # Only advanced eliminators
#   ./benchmark-comparison.sh --full           # Full pipeline + solver

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_DIR="${SCRIPT_DIR}/build/reports/jmh"
RESULTS_FILE="${REPORT_DIR}/results.json"

mkdir -p "${REPORT_DIR}"

QUICK_MODE=false
BENCHMARK_FILTER=".*"

while [[ $# -gt 0 ]]; do
  case $1 in
    --quick)
      QUICK_MODE=true
      shift
      ;;
    --eliminators)
      BENCHMARK_FILTER=".*EliminatorBenchmark.*"
      shift
      ;;
    --advanced)
      BENCHMARK_FILTER=".*AdvancedEliminatorBenchmark.*"
      shift
      ;;
    --full)
      BENCHMARK_FILTER=".*"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--quick] [--eliminators] [--advanced] [--full]"
      exit 1
      ;;
  esac
done

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     🧩 Sudoku Solver — Benchmark Comparison Suite       ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "Filter: ${BENCHMARK_FILTER}"
echo "Quick mode: ${QUICK_MODE}"
echo "Report dir: ${REPORT_DIR}"
echo ""

# Build JMH arguments
JMH_ARGS=(
  "-Djmh.include=${BENCHMARK_FILTER}"
  "-Djmh.json=${RESULTS_FILE}"
)

if [[ "${QUICK_MODE}" == "true" ]]; then
  JMH_ARGS+=(
    "-Djmh.warmup.iterations=1"
    "-Djmh.warmup.time=1s"
    "-Djmh.measurement.iterations=1"
    "-Djmh.measurement.time=1s"
    "-Djmh.fork=1"
  )
  echo "⚡ Quick mode: 1 warmup + 1 measurement iteration"
else
  JMH_ARGS+=(
    "-Djmh.warmup.iterations=3"
    "-Djmh.warmup.time=2s"
    "-Djmh.measurement.iterations=5"
    "-Djmh.measurement.time=3s"
    "-Djmh.fork=2"
  )
  echo "🔬 Standard mode: 3 warmup + 5 measurement iterations, 2 forks"
fi

echo ""
echo "Running benchmarks..."
echo "─────────────────────────────────────────"

cd "${SCRIPT_DIR}"
./gradlew jmh --no-daemon "${JMH_ARGS[@]}" 2>&1 | tail -5

if [[ -f "${RESULTS_FILE}" ]]; then
  echo ""
  echo "✅ Benchmark results saved to: ${RESULTS_FILE}"
  echo ""
  echo "📊 Generating summary report..."
  echo "═══════════════════════════════════════════════════════════"

  # Parse and display results
  if command -v python3 &>/dev/null; then
    python3 - <<'PYTHON_SCRIPT'
import json
import sys

try:
    with open(sys.argv[1] if len(sys.argv) > 1 else "build/reports/jmh/results.json") as f:
        results = json.load(f)

    if not results:
        print("No benchmark results found.")
        sys.exit(0)

    # Group by benchmark class
    by_class = {}
    for r in results:
        benchmark = r.get("benchmark", "unknown")
        cls = benchmark.split(".")[-2] if "." in benchmark else "unknown"
        method = benchmark.split(".")[-1] if "." in benchmark else benchmark
        params = r.get("params", {})
        board = params.get("boardName", "?")

        key = f"{cls}.{method}"
        if key not in by_class:
            by_class[key] = []
        by_class[key].append({
            "board": board,
            "score": r.get("primaryMetric", {}).get("score", 0),
            "unit": r.get("primaryMetric", {}).get("scoreUnit", "?"),
            "error": r.get("primaryMetric", {}).get("scoreError", 0),
        })

    for bench, runs in sorted(by_class.items()):
        print(f"\n🔍 {bench}")
        print(f"   {'Board':<10} {'Score':>12} {'± Error':>12} {'Unit':<6}")
        print(f"   {'─'*10} {'─'*12} {'─'*12} {'─'*6}")
        for run in sorted(runs, key=lambda x: x["board"]):
            print(f"   {run['board']:<10} {run['score']:>12.3f} {run['error']:>12.3f} {run['unit']:<6}")

    # Summary
    print(f"\n📈 Total benchmarks: {len(results)}")
    print(f"📈 Unique tests: {len(by_class)}")

except Exception as e:
    print(f"Could not parse results: {e}")
PYTHON_SCRIPT
  fi

  echo ""
  echo "═══════════════════════════════════════════════════════════"
  echo "✅ Benchmark comparison complete!"
  echo "   Full results: ${RESULTS_FILE}"
  echo "═══════════════════════════════════════════════════════════"
else
  echo ""
  echo "❌ No results file found. Benchmarks may have failed."
  exit 1
fi
