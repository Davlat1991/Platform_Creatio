#!/usr/bin/env bash
# ============================================================
#  Allure: отчёт с накоплением истории и трендов (Linux/Git Bash, для будущего Jenkins)
#  Использование:
#    ./report.sh                          - отчёт по последним результатам
#    ./report.sh test                     - прогнать тесты, затем отчёт
#    ./report.sh test -P regression -Denv=preprod
# ============================================================
set -u

RESULTS=target/allure-results
REPORT=target/allure-report
HISTORY=allure-history

if [ "${1:-}" = "test" ]; then
  shift
  echo "[1/5] Running tests..."
  mvn clean test "$@" || true     # отчёт нужен даже при упавших тестах
fi

if [ ! -d "$RESULTS" ]; then
  echo "ERROR: $RESULTS не найден. Сначала прогони тесты:  ./report.sh test"
  exit 1
fi

if [ -d "$HISTORY" ]; then
  echo "[2/5] Seeding history..."
  mkdir -p "$RESULTS/history"
  cp -r "$HISTORY/." "$RESULTS/history/"
fi

echo "[3/5] Stamping build..."
BUILD="run-$(date +%Y%m%d-%H%M)"
cat > "$RESULTS/executor.json" <<JSON
{ "name": "Local", "type": "manual", "buildName": "$BUILD" }
JSON

echo "[4/5] Generating report..."
allure generate "$RESULTS" --clean -o "$REPORT"

if [ -d "$REPORT/history" ]; then
  echo "[5/5] Saving history..."
  mkdir -p "$HISTORY"
  cp -r "$REPORT/history/." "$HISTORY/"
fi

echo "Done. Opening report..."
allure open "$REPORT"