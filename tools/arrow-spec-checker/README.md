# Arrow Spec Checker

A fast tool to detect Clojure functions without arrow (`=>`) spec definitions.

## Purpose

This tool finds all `defn` and `defn-` functions in your codebase that don't have corresponding arrow specs (e.g., `m/=>` or any `=>` form). It helps ensure all functions have proper Malli instrumentation specs defined.

## Usage

### From the tool directory:
```bash
cd tools/arrow-spec-checker
./check.sh                  # Check ../../src by default
./check.sh /path/to/dir     # Check specific directory
```

### Direct with Clojure:
```bash
clojure -M:run /path/to/source
```

## How it Works

1. **Parallel Processing**: Uses `pmap` to analyze files concurrently for speed
2. **AST Analysis**: Uses `rewrite-clj` to parse Clojure code properly
3. **Set Difference**: Builds sets of function definitions and arrow specs, then finds the difference
4. **Clean Output**: Groups results by file with line numbers

## Output

The tool provides:
- Clean list of files and line numbers where functions lack arrow specs
- Exit code 0 if all functions have specs
- Exit code 1 if missing specs found (useful for CI)

## Example Output

```
⚠ Found 3 functions without arrow specs:

/src/myapp/core.clj
   42: process-data
   78: validate-input

/src/myapp/utils.clj
  156: format-output
```

## Implementation Details

- Only counts explicit arrow specs (`=>` forms)
- Ignores metadata schemas (`:malli/schema`)
- Handles any namespace for `=>` (e.g., `m/=>`, `malli/=>`, etc.)
- Robust error handling for malformed code