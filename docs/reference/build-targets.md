# Build Targets Reference

Complete reference for all Makefile targets in PotatoClient.

## Primary Development Commands

### `make dev`
Runs in development mode with full validation.
- Guardrails validation enabled
- All logging levels (DEBUG, INFO, WARN, ERROR)
- Individual subprocess log files
- Window title shows `[DEVELOPMENT]`

### `make dev-clean`
Development with forced clean rebuild.
- Regenerates all proto files
- Recompiles all code
- Ensures fresh start
- Then runs `make dev`

### `make ensure-compiled`
Smart compilation check.
- Compiles only if sources changed
- Checks for missing proto files
- Minimal rebuild when possible
- Used by `make dev` and `make test`

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

### `make compile-java-proto`
Compiles Java protobuf sources.
- Builds generated proto classes
- Required after `make proto`

### `make compile-java-enums`
Compiles Java enum sources.
- Builds enum classes for keywords
- Part of build process

### `make generate-keyword-trees`
Generates keyword mappings.
- Creates EDN to protobuf mappings
- Runs both cmd and state generation
- Output: `shared/specs/protobuf/proto_keyword_tree_*.clj`

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
Generates comprehensive test coverage report.
- Clojure coverage via Cloverage
- Java/Kotlin coverage via JaCoCo
- HTML reports in `target/coverage/`
- Shows line and branch coverage

### `make coverage-clojure`
Quick Clojure-only coverage.
- Faster than full coverage
- Shows Clojure code coverage only
- Good for quick checks

### `make coverage-analyze`
Analyzes existing coverage reports.
- Lists uncovered functions
- Generates uncovered code report
- Helps identify testing gaps

### `make coverage`
Alias for `coverage-clojure`.
- Quick coverage check
- Same as `make coverage-clojure`

## Code Quality Commands

### `make fmt`
Formats all code automatically.
- cljfmt for Clojure formatting
- ktlint for Kotlin formatting
- Fixes style issues in-place

### `make fmt-check`
Checks code formatting without changing files.
- Reports formatting issues
- Returns error if changes needed
- Use in CI/CD pipelines

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

### `make validate-actions`
Validates Action Registry.
- Compares registered actions with proto commands
- Identifies missing or extra actions
- Checks parameter consistency

## Platform Builds

### `make build-macos-dev`
Creates unsigned macOS development bundle.
- `.app` format
- Uses system Java
- For development testing only
- No code signing

Note: Linux and Windows platform builds are not currently implemented in the Makefile.

## Utility Commands

### `make clean`
Removes all build artifacts.
- Cleans `target/` directory
- Removes compiled classes
- Preserves source code

### `make clean-proto`
Clean generated proto files.
- Removes `src/potatoclient/java/` proto packages
- Use before regenerating protos

### `make clean-cache`
Clean Clojure compilation cache.
- Removes `.cpcache/` directory
- Ensures fresh compilation

### `make clean-app`
Clean application classes only.
- Preserves proto classes
- Removes Clojure and Kotlin classes

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

### `make mcp-server`
Starts MCP server for Claude integration.
- Runs on port 7888
- Allows Claude to interact with project
- Keep terminal open while using

### `make mcp-configure`
Adds potatoclient to Claude configuration.
- Configures MCP server in Claude
- One-time setup command
- Uses `claude` CLI tool

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