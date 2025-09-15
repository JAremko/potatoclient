# Docstring Checker

A comprehensive documentation coverage tool that ensures all Clojure definitions have proper docstrings, maintaining code quality and readability.

## Purpose

Documentation is critical for maintainable code. This tool:
- Finds all Clojure definition forms lacking docstrings
- Covers functions (`defn`, `defn-`, `defmacro`), variables (`def`, `defonce`), multimethods (`defmulti`), and types (`defprotocol`, `defrecord`, `deftype`)
- Ensures comprehensive documentation coverage across the codebase
- Provides actionable reports with file paths and line numbers
- Helps maintain professional code quality standards
- Enforces the principle that **ALL definitions MUST have docstrings**

## Why Documentation Matters

**Well-documented code is professional code**. Docstrings provide:
- **Intent clarity** - Explain why, not just what
- **API contracts** - Document parameters and return values
- **Usage examples** - Help developers use your code correctly
- **Maintenance hints** - Guide future modifications
- **Knowledge transfer** - Reduce onboarding time for new developers

## Usage

### Quick Start
```bash
# From project root
cd tools/docstring-checker
./check.sh                  # Check ../../src by default
./check.sh /path/to/dir     # Check specific directory
./check.sh test             # Check test directory
```

### Direct Clojure Execution
```bash
clojure -M:run /path/to/source
```

### Integration with Development Workflow
```bash
# After adding new functions
./check.sh src/

# During code review
./check.sh && echo "Documentation complete!"

# As part of CI pipeline
./check.sh || exit 1

# Check test coverage too
./check.sh test/
```

## How It Works

1. **Parallel Processing**: Uses `pmap` for concurrent file analysis
2. **AST-Based Analysis**: Leverages `rewrite-clj` for accurate parsing
3. **Comprehensive Detection**:
   - Functions: `defn`, `defn-`, `defmacro`
   - Variables: `def`, `defonce`
   - Multimethods: `defmulti`
   - Types: `defprotocol`, `defrecord`, `deftype`
   - Detects string docstrings and metadata docs
   - Identifies empty strings as missing documentation
4. **Grouped Reporting**: Organizes results by file and definition type

## Docstring Formats Recognized

```clojure
;; ✅ GOOD - String docstring for functions
(defn process-command
  "Process a command and return a status map.
   
   Args:
     cmd - Command map with :type and :data keys
   
   Returns:
     Status map with :success and :result keys"
  [cmd]
  ...)

;; ✅ GOOD - Macro with docstring
(defmacro with-timing
  "Macro to measure execution time of body expressions.
   Returns a map with :result and :elapsed-ms keys."
  [& body]
  ...)

;; ✅ GOOD - Multimethod with docstring
(defmulti handle-event
  "Dispatches events based on their :type field.
   Each handler should return a response map."
  :type)

;; ✅ GOOD - Protocol with docstring
(defprotocol Lifecycle
  "Protocol for components with start/stop lifecycle"
  (start [this] "Initialize the component")
  (stop [this] "Shutdown the component"))

;; ✅ GOOD - Record with docstring
(defrecord User
  "Represents a user in the system with authentication details"
  [id username email])

;; ✅ GOOD - Type with docstring
(deftype Cache
  "Thread-safe cache implementation with TTL support"
  [^:volatile-mutable data])

;; ✅ GOOD - Metadata docstring
(def ^{:doc "Global application state atom. Contains :ui, :server, and :session keys."} 
  app-state 
  (atom {}))

;; ❌ BAD - Missing docstring
(defn undocumented-function [x] ...)
(defmacro no-doc-macro [& args] ...)
(defmulti no-doc-multi :type)

;; ❌ BAD - Empty docstring
(def my-var
  ""  ; Empty strings don't count!
  value)
```

## Output Format

### Success Case
```
✅ DOCUMENTATION COVERAGE: 100% (523/523)
=====================================
All definitions have docstrings!

Breakdown by type:
  defn:    312 ✓
  defn-:    89 ✓
  def:     117 ✓
  defonce:   5 ✓
```

### Missing Documentation Report
```
📊 DOCUMENTATION COVERAGE REPORT
================================
📈 Coverage: 87.3% (482/552)

⚠️ MISSING DOCUMENTATION BY TYPE
---------------------------------
defn:      23 functions need docs
defn-:     15 private functions need docs
defmacro:    5 macros need docs
def:       30 definitions need docs
defonce:    2 singletons need docs
defmulti:   3 multimethods need docs
defprotocol: 1 protocol needs docs
defrecord:  2 records need docs
deftype:    1 type needs docs

📁 FILES WITH MISSING DOCS (12 files)
--------------------------------------

src/potatoclient/streams/events.clj (8 missing)
  Line 42: defn handle-gesture-event
  Line 78: defn- normalize-message
  Line 156: def event-handlers
  Line 201: defn validate-gesture
  Line 234: defn- log-event
  Line 267: def default-config
  Line 289: defonce event-queue
  Line 312: defn process-queue

src/potatoclient/ui/components.clj (5 missing)
  Line 23: defn create-button
  Line 67: defn- update-internal
  Line 89: def ui-defaults
  Line 112: defn dispose-frame
  Line 145: def color-scheme

[... showing top 10 files, 2 more files omitted]

❌ 70 definitions need documentation
```

## Documentation Standards

### Required Documentation

1. **Functions (`defn`, `defn-`)** - MUST document:
   - Purpose and behavior
   - Parameter descriptions
   - Return value description
   - Side effects if any
   - Example usage for complex functions

2. **Macros (`defmacro`)** - MUST document:
   - Macro expansion behavior
   - Parameter structure
   - When to use vs regular functions
   - Example expansions

3. **Multimethods (`defmulti`)** - MUST document:
   - Dispatch function logic
   - Expected dispatch values
   - Overall purpose of the multimethod
   - Note: `defmethod` implementations typically don't need docstrings

4. **Protocols (`defprotocol`)** - MUST document:
   - Protocol purpose and contract
   - Each method's expected behavior
   - Implementation requirements

5. **Records and Types (`defrecord`, `deftype`)** - MUST document:
   - Purpose and use cases
   - Field descriptions
   - Thread safety characteristics
   - Protocol implementations if any

6. **Variables (`def`, `defonce`)** - MUST document:
   - Purpose and structure
   - Mutability characteristics
   - Thread safety considerations
   - Valid value ranges or constraints

### Documentation Quality Guidelines

```clojure
;; ❌ BAD - Too vague
(defn process
  "Process the data"
  [data] ...)

;; ✅ GOOD - Clear and specific
(defn process
  "Transform raw sensor data into normalized readings.
   Applies calibration coefficients and removes outliers.
   
   Args:
     data - Vector of raw sensor readings (mV)
   
   Returns:
     Vector of normalized readings (0.0-1.0 range)"
  [data] ...)
```

## Exit Codes

- **0**: All definitions have docstrings
- **1**: Missing documentation detected (useful for CI/CD pipelines)

## Best Practices

1. **Document immediately** - Write docstrings when creating definitions
2. **Be specific** - Explain intent, not just restate the function name
3. **Include examples** - For complex functions, show usage
4. **Document invariants** - Explain assumptions and constraints
5. **Update documentation** - Keep docs in sync with code changes

## Common Issues and Solutions

### Issue: Generated code lacks docstrings
**Solution**: Macros that generate definitions should include docstring parameters.

### Issue: Spec definitions flagged as undocumented
**Solution**: Even self-explanatory specs benefit from documentation explaining their purpose and usage.

### Issue: Test helpers flagged
**Solution**: Test utilities should be documented to help other developers write tests.

## Implementation Details

- **Language**: Clojure with rewrite-clj for AST parsing
- **Performance**: Parallel processing with `pmap`
- **Accuracy**: Handles all metadata formats correctly
- **Robustness**: Graceful handling of malformed code
- **Coverage**: Analyzes all Clojure definition forms
