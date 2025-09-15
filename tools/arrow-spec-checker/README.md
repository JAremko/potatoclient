# Arrow Spec Checker

A high-performance tool for identifying Clojure functions without arrow (`=>`) spec definitions, essential for `malli.dev/start!` instrumentation.

## Purpose

Arrow specs are critical for runtime validation in Clojure applications using Malli. This tool:
- Finds all `defn` and `defn-` functions lacking arrow specs
- Ensures comprehensive instrumentation coverage for `malli.dev/start!`
- Provides actionable reports with file paths and line numbers
- Helps maintain code quality through spec completeness

## Why Arrow Specs Matter

**Arrow specs (`m/=>`) are auto-discovered by `malli.dev/start!`**, while metadata schemas (`:malli/schema`) are not. This makes arrow specs essential for:
- Runtime validation during development
- Automatic instrumentation without manual registration
- Generative testing with proper function signatures
- Static analysis via CLJ-Kondo integration

## Usage

### Quick Start
```bash
# From project root
cd tools/arrow-spec-checker
./check.sh                  # Check ../../src by default
./check.sh /path/to/dir     # Check specific directory
```

### Direct Clojure Execution
```bash
clojure -M:run /path/to/source
```

### Integration with Development Workflow
```bash
# After adding new functions
./check.sh src/

# Before enabling instrumentation
./check.sh && make dev

# As part of CI pipeline
./check.sh || exit 1
```

## How It Works

1. **Parallel Processing**: Uses `pmap` for concurrent file analysis
2. **AST-Based Analysis**: Leverages `rewrite-clj` for accurate parsing
3. **Intelligent Detection**: 
   - Identifies all `defn` and `defn-` definitions
   - Finds all arrow spec forms (`=>` with any namespace)
   - Calculates the difference to identify missing specs
4. **Grouped Reporting**: Organizes results by file with line numbers

## Output Format

### Success Case
```
✅ All 127 functions have arrow specs defined!
Functions analyzed: 127
Arrow specs found: 127
Coverage: 100%
```

### Missing Specs Report
```
⚠️ FUNCTIONS WITHOUT ARROW SPECS (12)
==========================================

📁 src/potatoclient/streams/events.clj (5 missing)
  Line 42: handle-gesture-event
  Line 78: normalize-message
  Line 156: process-event-queue
  Line 201: validate-gesture
  Line 234: log-event

📁 src/potatoclient/ui/components.clj (4 missing)
  Line 23: create-button
  Line 67: update-label
  Line 89: refresh-panel
  Line 112: dispose-frame

📁 src/potatoclient/state/core.clj (3 missing)
  Line 15: initialize-state
  Line 48: reset-state
  Line 92: validate-state

❌ 12 functions need arrow specs for full instrumentation support
```

## Arrow Spec vs Metadata Schema

```clojure
;; ✅ GOOD - Arrow spec (auto-discovered by malli.dev/start!)
(defn process-command
  "Process a command and return result"
  [cmd]
  ...)
(m/=> process-command [:=> [:cat :cmd/root] [:map [:status :keyword]]])

;; ❌ BAD - Metadata schema (NOT auto-discovered)
(defn process-command
  "Process a command and return result"
  {:malli/schema [:=> [:cat :cmd/root] [:map [:status :keyword]]]}
  [cmd]
  ...)
```

## Exit Codes

- **0**: All functions have arrow specs
- **1**: Missing specs found (useful for CI/CD pipelines)

## Best Practices

1. **Run after adding new functions** to catch missing specs early
2. **Include in CI pipeline** to enforce spec coverage
3. **Focus on high-traffic modules** for maximum instrumentation benefit
4. **Don't skip private functions** - `defn-` also benefits from specs
5. **Consider migration** for legacy code using metadata schemas

## Implementation Details

- **Language**: Clojure with rewrite-clj for AST parsing
- **Performance**: Parallel processing with `pmap`
- **Accuracy**: Handles malformed code gracefully
- **Flexibility**: Accepts any namespace for `=>` (m/=>, malli/=>, etc.)
- **Scope**: Analyzes both public (`defn`) and private (`defn-`) functions

## Common Issues and Solutions

### Issue: Functions in init.clj can't use arrow specs
**Solution**: Due to registry initialization order, these functions may need metadata schemas or delayed spec registration.

### Issue: Large codebases take time to analyze
**Solution**: The tool uses parallel processing, but you can check specific directories to speed up analysis during development.

### Issue: False positives for macro-generated functions
**Solution**: The tool only analyzes literal `defn` forms in source code. Generated functions may need manual verification.
