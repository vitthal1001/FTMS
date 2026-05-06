#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${1:-$ROOT_DIR/.logs}"
DEBUG_DIR="$ROOT_DIR/brain/debugging"
THRESHOLD=3

mkdir -p "$DEBUG_DIR"

if [[ ! -d "$LOG_DIR" ]]; then
  echo "watchdog: log directory not found: $LOG_DIR"
  exit 0
fi

fingerprints="$(
  find "$LOG_DIR" -type f \( -name "*.log" -o -name "*.txt" \) -print0 |
    xargs -0 awk '
      /Exception|ERROR|FAILED|BUILD FAILED|There were failing tests|Stacktrace|Caused by:/ {
        gsub(/[0-9a-fA-F-]{8,}/, "<id>");
        gsub(/[0-9]+/, "<n>");
        print;
      }
    ' |
    sort |
    uniq -c |
    awk -v threshold="$THRESHOLD" '$1 >= threshold {print}'
)"

if [[ -z "$fingerprints" ]]; then
  echo "watchdog: no repeated failure fingerprint reached threshold $THRESHOLD"
  exit 0
fi

timestamp="$(date -u +"%Y%m%dT%H%M%SZ")"
report="$DEBUG_DIR/repeated-failure-$timestamp.md"

{
  echo "# Repeated Failure Analysis - $timestamp"
  echo
  echo "The watchdog detected repeated failure fingerprints at least $THRESHOLD times."
  echo
  echo "## Fingerprints"
  echo
  echo '```text'
  echo "$fingerprints"
  echo '```'
  echo
  echo "## Required Action"
  echo
  echo "- Stop regenerating the same fix."
  echo "- Identify the first failing boundary."
  echo "- Record root cause, attempted fixes, and next diagnostic step before editing again."
} > "$report"

echo "watchdog: repeated failure threshold reached; wrote $report"
exit 2

