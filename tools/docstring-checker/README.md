# Docstring Checker

A tool to detect Clojure definitions without documentation strings.

## Purpose

This tool finds all `def`, `defn`, `defn-`, and `defonce` forms in your codebase that lack docstrings. It helps ensure all functions and important definitions are properly documented.

## Features

- Detects both string docstrings and `^{:doc "..."}` metadata documentation
- Covers all definition types: `def`, `defn`, `defn-`, `defonce`
- Provides coverage statistics and breakdown by type
- Groups results by file with line numbers
- Parallel processing for fast analysis
- CI-friendly exit codes

## Usage

### From the tool directory:
```bash
cd tools/docstring-checker
./check.sh                  # Check ../../src by default
./check.sh /path/to/dir     # Check specific directory
./check.sh test             # Check test directory
```

### Direct with Clojure:
```bash
clojure -M:run /path/to/source
```

## Docstring Formats

The tool recognizes multiple docstring formats:

```clojure
;; String docstring for functions
(defn my-function
  "This function does something"
  [x] ...)

;; String docstring for definitions
(def my-constant
  "This is an important constant"
  42)

;; Metadata docstring
(def ^{:doc "Metadata documentation"} my-var value)
(defonce ^{:doc "Singleton with doc"} my-atom (atom nil))

;; Complex metadata
(defn ^{:private true :doc "Private helper"} helper [x] ...)
```

## Output

The tool provides:
- Overall documentation coverage percentage
- Breakdown by definition type
- List of files with missing documentation
- Line numbers for quick navigation
- Exit code 0 if fully documented
- Exit code 1 if documentation missing (useful for CI)

## Example Output

```
📊 Docstring Coverage: 82% (746/906)

📈 Missing by type:
  defn: 45
  defn-: 23
  def: 92
  defonce: 3

⚠️ Found 163 definitions without docstrings:

/src/myapp/core.clj
   42: defn process-data
   78: defn- validate-input
  156: def config-map

/src/myapp/utils.clj
   23: defonce cache-atom
   67: def default-timeout
```

## Implementation Details

- Uses `rewrite-clj` for accurate AST parsing
- Handles metadata nodes correctly
- Recognizes empty strings as missing documentation
- Robust error handling for malformed code
- Parallel file processing with `pmap`