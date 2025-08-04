# Build Targets Reference

Complete reference for all Makefile targets in PotatoClient.

## Primary Development Commands

### `make dev`
Runs in development mode with full validation.
- Guardrails validation enabled
- All logging levels (DEBUG, INFO, WARN, ERROR)
- Individual subprocess log files
- Window title shows `[DEVELOPMENT]`

### `make nrepl`
Starts REPL server on port 7888.
- Full Guardrails validation
- Same as dev mode but for REPL development
- Connect from editor (Emacs/CIDER, VS Code/Calva, IntelliJ/Cursive)

### `make run`
Runs the built JAR (production-like).
- No Guardrails overhead
- Standard logging only
- Must run `make build` first

## Build Commands

### `make build`
Builds the main JAR file.
- Compiles all code
- Creates uberjar with dependencies
- Output: `target/potatoclient.jar`

### `make release`
Creates optimized release build.
- AOT compilation
- Direct linking
- Stripped metadata
- No development overhead
- Output: `target/potatoclient-release.jar`

### `make proto`
Generates protobuf Java classes.
- Cleans old bindings first
- Uses protogen Docker tool
- Updates command/state classes

### `make compile-kotlin`
Compiles Kotlin sources.
- Downloads Kotlin 2.2.0 if needed
- Compiles all subprocess code
- Must run after `make proto`

## Testing Commands

### `make test`
Runs all tests with automatic logging.
- Saves to `logs/test-runs/TIMESTAMP/`
- Generates summary and failure reports
- Shows test results

### `make test-summary`
Views latest test run summary.
- Shows pass/fail counts
- Lists failed tests
- Quick overview of test health

### `make test-coverage`
Generates test coverage report.
- Uses Cloverage with JaCoCo
- HTML report in `target/coverage/`
- Shows line and branch coverage

## Code Quality Commands

### `make lint`
Runs all linters.
- clj-kondo for Clojure
- ktlint for Kotlin style
- detekt for Kotlin analysis

### `make lint-report`
Generates detailed lint report.
- Markdown format
- Saved to `reports/lint-report.md`

### `make lint-report-filtered`
Lint report with false positives removed.
- ~56% reduction in noise
- More actionable results
- Uses intelligent filtering

### `make report-unspecced`
Finds functions without Guardrails.
- Lists raw `defn` usage
- Groups by namespace
- Shows coverage statistics

## Platform Builds

### `make build-linux`
Creates Linux AppImage.
- Self-contained package
- Works on most distributions
- Includes all dependencies

### `make build-windows`
Creates Windows installer.
- `.exe` format
- Native Windows package
- Includes JRE

### `make build-macos`
Creates macOS bundle.
- `.dmg` disk image
- Code signing ready
- Universal binary support

## Utility Commands

### `make clean`
Removes all build artifacts.
- Cleans `target/` directory
- Removes compiled classes
- Preserves source code

### `make check-deps`
Verifies all dependencies.
- Checks Java version
- Verifies GStreamer
- Tests tool availability

### `make help`
Shows all available commands.
- Self-documenting Makefile
- Includes descriptions
- Shows common workflows

## Development Workflows

### Standard Development
```bash
make clean
make proto
make compile-kotlin
make dev
```

### Testing Workflow
```bash
make test
make test-summary
make lint-report-filtered
```

### Release Workflow
```bash
make clean
make proto
make compile-kotlin
make test
make release
make build-linux
```

## Environment Variables

### `JAVA_OPTS`
Additional JVM options.
```bash
JAVA_OPTS="-Xmx4g" make dev
```

### `GST_DEBUG`
GStreamer debug level.
```bash
GST_DEBUG=3 make dev
```

### `STREAM_TYPE`
For mock video stream tool.
```bash
cd tools/mock-video-stream
make process STREAM_TYPE=heat
```

## Tips

1. **Always run `make proto` after protobuf changes**
2. **Use `make help` for inline documentation**
3. **Check `make lint-report-filtered` before commits**
4. **Run `make test-summary` to quickly check test health**
5. **Use `make clean` when switching branches**