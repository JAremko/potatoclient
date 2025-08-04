# Guardrails Check Tool

A fast Babashka-based tool for finding functions that don't use Guardrails (`>defn`/`>defn-`) in the PotatoClient codebase.

## Overview

The Guardrails Check tool helps maintain code quality by:
- Finding functions using raw `defn` instead of `>defn`
- Generating reports in multiple formats
- Providing namespace-level statistics
- Supporting pattern-based searches
- Running without JVM startup overhead

## Installation

The tool is included in the PotatoClient repository:

```bash
cd tools/guardrails-check

# Verify Babashka is installed
bb --version
```

## Usage

### Basic Commands

```bash
# Check for unspecced functions (EDN output)
bb check ../../src/potatoclient

# Generate markdown report
bb report ../../src/potatoclient

# Find functions by pattern
bb find process ../../src/potatoclient

# Show statistics only
bb stats ../../src/potatoclient
```

### From Project Root

The main project Makefile provides a convenient wrapper:

```bash
# Generate report from project root
make report-unspecced

# Output saved to: ./reports/unspecced-functions.md
```

## Report Format

### Markdown Report

The markdown report includes:
- Summary statistics
- Functions grouped by namespace
- Direct file links for easy navigation

Example output:

```markdown
# Unspecced Functions Report

Generated: 2024-08-04 14:30:45

## Summary
- Total namespaces analyzed: 25
- Namespaces with unspecced functions: 5
- Total unspecced functions: 23
- Coverage: 89.5%

## Functions by Namespace

### potatoclient.transit.core (8 functions)

- `encode-transit` - [Line 25](../../src/potatoclient/transit/core.clj#L25)
- `decode-transit` - [Line 31](../../src/potatoclient/transit/core.clj#L31)
...
```

### EDN Output

For programmatic use:

```clojure
{:timestamp "2024-08-04T14:30:45"
 :summary {:total-namespaces 25
           :affected-namespaces 5
           :total-functions 23}
 :namespaces {"potatoclient.transit.core"
              [{:name "encode-transit"
                :line 25
                :type :defn
                :file "src/potatoclient/transit/core.clj"}
               {:name "decode-transit"
                :line 31
                :type :defn-
                :file "src/potatoclient/transit/core.clj"}]}}
```

## Pattern Matching

### Find Specific Functions

```bash
# Find all functions with "process" in the name
bb find process src/

# Case-insensitive search
bb find HANDLE src/

# Partial matches work
bb find enc src/  # Finds "encode-transit"
```

### Filter by Namespace

```bash
# Check specific namespace
bb check src/potatoclient/transit/

# Multiple paths
bb check src/potatoclient/core.clj src/potatoclient/state.clj
```

## Understanding Results

### Why Functions Might Not Use Guardrails

Common valid reasons:

1. **Performance Critical**
   ```clojure
   ;; Transit serialization hot path
   (defn encode-transit [data]  ; Raw defn for performance
     ...)
   ```

2. **Meta-programming**
   ```clojure
   ;; Macro-generated functions
   (defn generated-handler-1 []
     ...)
   ```

3. **Java Interop**
   ```clojure
   ;; Proxy/reify implementations
   (defn -main [& args]  ; Java method
     ...)
   ```

4. **Development Tools**
   ```clojure
   ;; REPL helpers, not production code
   (defn debug-state []
     ...)
   ```

### Interpreting Statistics

Coverage percentages:
- **>95%**: Excellent Guardrails adoption
- **90-95%**: Good, with justified exceptions
- **<90%**: Review needed, possible oversight

## Integration with Development

### Pre-commit Hook

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
cd tools/guardrails-check
output=$(bb check ../../src/potatoclient)
if [ -n "$output" ]; then
  echo "New unspecced functions found:"
  echo "$output"
  echo "Add Guardrails or document why not needed"
  exit 1
fi
```

### CI Integration

```yaml
# In GitHub Actions
- name: Check Guardrails Coverage
  run: |
    cd tools/guardrails-check
    bb report ../../src/potatoclient
    coverage=$(bb stats ../../src/potatoclient | grep "Coverage" | cut -d: -f2)
    if [ "$coverage" -lt "90" ]; then
      echo "Guardrails coverage below 90%"
      exit 1
    fi
```

## Advanced Usage

### Custom Output Formats

```bash
# JSON output (via jq)
bb check src/ | bb -e "(json/generate-string *input*)"

# CSV for spreadsheets
bb check src/ | bb -e '(doseq [[ns fns] *input*]
                         (doseq [f fns]
                           (println (str ns "," (:name f) "," (:line f)))))'
```

### Exclude Patterns

```clojure
;; Create custom wrapper
(require '[babashka.fs :as fs])

(defn check-filtered [path]
  (let [results (check-files path)]
    (remove #(re-find #"generated|test" (:file %)) results)))
```

## How It Works

The tool uses regex-based parsing to find function definitions:

1. **Scans** Clojure source files
2. **Identifies** `defn` and `defn-` usage
3. **Ignores** `>defn` and `>defn-`
4. **Extracts** function name and line number
5. **Groups** by namespace
6. **Formats** output

## Limitations

- Doesn't understand macro-generated functions
- Can't detect runtime-created functions
- May miss functions in eval'd code
- Regex-based, not full parser

## Best Practices

1. **Regular Checks**: Run weekly or before releases
2. **Document Exceptions**: Comment why raw defn is used
3. **Review Changes**: Check report diffs in PRs
4. **Set Goals**: Aim for >90% coverage
5. **Automate**: Add to CI pipeline

## Troubleshooting

### No Output

```bash
# Verify path exists
ls -la ../../src/potatoclient

# Check Babashka version
bb --version  # Need 1.0+

# Try absolute path
bb check /absolute/path/to/src
```

### Incorrect Results

- Check for syntax errors in source files
- Ensure files have .clj or .cljc extension
- Verify file encoding (UTF-8)

## See Also

- [Code Standards](../development/code-standards.md)
- [Guardrails Documentation](https://github.com/fulcrologic/guardrails)
- [Makefile Targets](../reference/build-targets.md)