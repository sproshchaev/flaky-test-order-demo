#!/usr/bin/env bash
# Runs the test suite with random class order for the first N seeds from
# seeds.txt and compares the summary with expected.txt.
# Exit code 1 if the result differs.
#
#   ./run.sh          # all 300 seeds
#   ./run.sh 90       # first 90 seeds, no comparison
set -euo pipefail
cd "$(dirname "$0")"

SEEDS="${1:-300}"
OUT=target/results.txt
LOG=target/run.log
TARGET=com.shop.order.FirstOrderDiscountIT
NEIGHBOUR=com.shop.loyalty.LoyaltyPointsIT

mvn -q -B test-compile
mkdir -p target
: > "$LOG"

# class that ran right before FirstOrderDiscountIT in a surefire log
previous_class() {
  grep -E '^\[INFO\] Running ' "$1" | sed 's/.*Running //' \
    | grep -B1 -x "$TARGET" | head -1
}

run_seed() {
  mvn -B -Pci surefire:test -Dsurefire.runOrder.random.seed="$1" > "$2" 2>&1
}

failed=()
fail_after_neighbour=0
pass_after_neighbour=0
for seed in $(head -n "$SEEDS" seeds.txt); do
  run_log=target/seed.log
  if run_seed "$seed" "$run_log"; then
    status=pass
    if [[ "$(previous_class "$run_log")" == "$NEIGHBOUR" ]]; then
      pass_after_neighbour=$((pass_after_neighbour + 1))
    fi
  else
    status=fail
    failed+=("$seed")
    if [[ "$(previous_class "$run_log")" == "$NEIGHBOUR" ]]; then
      fail_after_neighbour=$((fail_after_neighbour + 1))
    fi
  fi
  echo "seed=$seed $status" >> "$LOG"
done

# the first failed seed must fail again every time
reproduced=0
if (( ${#failed[@]} > 0 )); then
  for _ in 1 2 3; do
    if ! run_seed "${failed[0]}" target/repro.log; then
      reproduced=$((reproduced + 1))
    fi
  done
fi

# The pair alone, without random order: works the same on any OS.
# Alphabetical order puts com.shop.loyalty before com.shop.order.
pair_result() {
  if mvn -B test -Dtest='LoyaltyPointsIT,FirstOrderDiscountIT' \
      -Dsurefire.runOrder="$1" > target/pair.log 2>&1; then
    echo pass
  else
    echo fail
  fi
}
pair_forward=$(pair_result alphabetical)
pair_reverse=$(pair_result reversealphabetical)

# Which seeds fail depends on the OS: Surefire shuffles the classes in the
# order it found them on disk. Printed for information, not compared.
echo "os: $(uname -s)"
echo "failed runs: ${#failed[@]} of $SEEDS"
echo "failed seeds: ${failed[*]:-none}"

# What does not depend on the OS goes to the comparison.
# 2..20 failures of 300 cover 99.8% of outcomes for a 1-in-30 rate.
in_range=no
if (( ${#failed[@]} >= 2 && ${#failed[@]} <= 20 )); then
  in_range=yes
fi
all_after_neighbour=no
if (( ${#failed[@]} > 0 && fail_after_neighbour == ${#failed[@]} )); then
  all_after_neighbour=yes
fi
{
  echo "seeds: first $SEEDS from seeds.txt"
  echo "test classes: $(find src/test/java -name '*IT.java' ! -name 'AbstractIT.java' | wc -l | tr -d ' ')"
  echo "failed runs within 2..20 (1 in 30 expected): $in_range"
  echo "every failed run had LoyaltyPointsIT right before: $all_after_neighbour"
  echo "passed runs with LoyaltyPointsIT right before: $pass_after_neighbour"
  echo "first failed seed reproduced: $reproduced of 3"
  echo "pair LoyaltyPointsIT then FirstOrderDiscountIT: $pair_forward"
  echo "pair FirstOrderDiscountIT then LoyaltyPointsIT: $pair_reverse"
  java tools/OrderOdds.java
} > "$OUT"

cat "$OUT"

if [[ "$SEEDS" != 300 ]]; then
  echo "custom seed count, no comparison with expected.txt"
elif diff -u expected.txt "$OUT"; then
  echo "OK: matches expected.txt"
else
  echo "MISMATCH: result differs from expected.txt" >&2
  exit 1
fi
