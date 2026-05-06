# Engineering Watchdog

The watchdog script is located at `scripts/engineering-watchdog.sh`.

It scans `.logs` by default or a supplied log directory and fingerprints repeated build, test, stack trace, and error lines. If a fingerprint appears at least three times, it writes a report into `brain/debugging` and exits with code `2`.

Required behavior after a repeated failure report:

- Stop editing.
- Identify the first failing boundary.
- Document root cause and attempted fixes.
- Choose a new diagnostic step before implementing again.

