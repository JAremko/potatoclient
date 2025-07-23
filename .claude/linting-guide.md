# PotatoClient Linting and Code Quality Guide

This guide covers all aspects of code quality, linting, and static analysis for the PotatoClient project.

## Code Quality and Linting

PotatoClient includes comprehensive linting for both Clojure and Kotlin code to maintain code quality and consistency.

### Linting Tools

**Clojure**
- **clj-kondo** (v2025.06.05) - Fast static analyzer with custom configuration
  - Configured for Guardrails, Seesaw, and Telemere macros
  - Custom hooks for `>defn`, `>defn-`, and `>def` macros
  - Enhanced lint-as mappings for Telemere functions (`handler:console`, `format-signal-fn`, etc.)
  - Namespace configurations for common libraries to reduce false positives
  - Reduced false positives through precise lint-as mappings

**Kotlin**
- **ktlint** (v1.5.0) - Code style checker following IntelliJ IDEA conventions
  - Enforces consistent formatting and style
  - Auto-downloads on first use
  - Configuration in `.ktlint/editorconfig`
  
- **detekt** (v1.23.7) - Advanced static analysis tool
  - Identifies code smells, complexity issues, and potential bugs
  - Comprehensive rule set in `detekt.yml`
  - Provides technical debt estimates

### Running Linters

Run `make help` to see all available linting commands. The project uses:
- **clj-kondo** for Clojure static analysis
- **ktlint** for Kotlin style checking
- **detekt** for advanced Kotlin analysis

Key commands:
- `make lint` - Run all code quality checks
- `make lint-report` - Generate comprehensive lint report
- `make lint-report-filtered` - Generate report with false positives filtered out (recommended)

### Lint Report Generation

The project includes a Babashka script that aggregates all linting results into a unified Markdown report. Use `make lint-report-filtered` for best results as it filters out known false positives.

For custom report options, run:
```bash
bb scripts/lint-report.bb --help
```

### clj-kondo Configuration

Custom configuration in `.clj-kondo/config.edn`:
- **Guardrails support**: `>defn`, `>defn-`, `>def` properly recognized
- **Seesaw macros**: UI construction functions linted correctly
- **Telemere logging**: Log macros understood without false positives
- **Custom hooks**: For complex macro transformations
- **Namespace grouping**: Organized imports validation

Common issues detected:
- Unresolved symbols and namespaces
- Unused imports and bindings
- Missing docstrings
- Unsorted namespaces
- Type mismatches (with Malli integration)

### Kotlin Linting Configuration

**ktlint** (`.ktlint/editorconfig`):
- IntelliJ IDEA code style
- 120 character line limit
- 4-space indentation
- Import ordering enforcement
- Trailing comma rules

**detekt** (`detekt.yml`):
- Complexity thresholds (cognitive and cyclomatic)
- Exception handling patterns
- Naming conventions
- Performance anti-patterns
- Code smell detection

### False Positive Handling

The project includes advanced false positive detection for common Clojure patterns that confuse static analyzers:

**Filtered Report**: Use `make lint-report-filtered` to get a report that automatically filters out known false positives:
- **Seesaw UI patterns**: Keyword arguments in widget constructors (e.g., `:text`, `:items`, `:border`)
- **Telemere logging**: Functions with colons like `handler:console`
- **Guardrails symbols**: Spec symbols referred but used in specs (>, |, ?, =>)
- **Standard library**: False warnings about `clojure.string` and `clojure.java.io`

**Statistics**: Typically filters ~56% of reported issues as false positives, focusing attention on real problems.

**Regenerating Reports**: When you fix issues or add new code, regenerate the filtered report to see current real issues:
```bash
make lint-report-filtered
```

### Integration with Development Workflow

1. **During Development**: Run `make lint` regularly to catch issues early
2. **Before Commits**: Use `make lint-report-filtered` to see only real issues
3. **CI/CD Integration**: All linters can be integrated into GitHub Actions
4. **IDE Integration**: IntelliJ IDEA with Cursive automatically uses clj-kondo
5. **Periodic Build Verification**: Run `make dev` or `make run` periodically to ensure the project builds successfully and hasn't been broken by recent changes

### Fixing Common Issues

**Clojure**:
- Missing docstrings: Add docstrings to public functions
- Unused imports: Remove or use `:refer :all` sparingly
- Unresolved symbols: Often due to macros - check lint-as configuration

**Kotlin**:
- Trailing spaces: Configure your editor to trim on save
- Import ordering: Use IntelliJ's "Optimize Imports" (Ctrl+Alt+O)
- Complexity warnings: Refactor large functions into smaller ones

## Related Build Commands

From the Makefile (run `make help` for full details):

- `make lint` - Run all code quality checks (clj-kondo + ktlint + detekt)
- `make clj-kondo` - Run Clojure linter only
- `make ktlint` - Run Kotlin style checker only
- `make detekt` - Run Kotlin static analyzer only
- `make lint-report` - Generate comprehensive lint report (includes false positives)
- `make lint-report-filtered` - Generate filtered lint report (recommended - removes ~56% false positives)
- `make report-unspecced` - Generate report of functions without Malli specs

## Checking for Unspecced Functions

### Guardrails Check

To find functions still using raw `defn`/`defn-`:

```clojure
(require '[potatoclient.guardrails.check :as check])

;; Print report
(check/print-report)

;; Get data structure
(check/report-unspecced-functions)
```

### Malli Instrumentation Check

To ensure all functions have Malli specs:

Use `make report-unspecced` to generate a report of functions without specs. The report will be saved to `./reports/unspecced-functions.md`.

This report will:
- List all functions that lack Malli instrumentation
- Group them by namespace
- Provide statistics on coverage
- Only include actual functions (not schema definitions)

## Best Practices

### Development Workflow

1. **Fix Guardrails errors immediately** - They indicate real bugs in your code
2. **Write precise specs** - Use domain-specific types from `potatoclient.specs`
3. **Check reflection warnings** - They indicate performance issues
4. **Run linters regularly** - Catch issues early in development
5. **Use filtered reports** - Focus on real issues, not false positives
6. **Build periodically** - Run `make dev` or `make run` to ensure nothing is broken

### Function Development

1. **Use `>defn` or `>defn-` for all functions** - Never use raw `defn`
2. **Return `nil` for side effects** - Not `true` or other values
3. **Add specs immediately** - Don't defer this
4. **Run `make report-unspecced`** - Regularly check for missing specs

## Important Notes

- **Development Mode Validation**: When using `make dev`, you get additional runtime validation through Guardrails that goes beyond static analysis
- **False Positives**: Always use the filtered report (`make lint-report-filtered`) to avoid wasting time on false positives
- **Build Verification**: The project must build successfully - linting alone doesn't guarantee the code works
- **Continuous Improvement**: As you fix issues, regenerate reports to track progress
- **IDE Integration**: Your IDE may provide additional linting - use both IDE and command-line tools for best coverage