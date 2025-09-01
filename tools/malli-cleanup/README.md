# Malli Cleanup Tool

A tool to clean up and standardize Malli metadata in Clojure code.

## Features

1. **Metadata Positioning**: Moves inline Malli metadata to the correct position (after docstring)
2. **Lambda Transformation**: Converts simple lambda functions to more idiomatic forms:
   - `(fn* [x] (instance? Type x))` → `(partial instance? Type)`
   - `(fn* [x] (pred? x))` → `pred?`

## Usage

### Quick Start

```bash
# Run analysis to see what needs changing
./apply_migration.sh --analyze

# Dry run to preview changes
./apply_migration.sh

# Apply transformations
./apply_migration.sh --apply
```

### Direct CLI Usage

```bash
# Analyze codebase
clojure -M:run --analyze ../../src ../../shared/src

# Dry run (default)
clojure -M:run ../../src

# Transform files
clojure -M:run --transform -o migrated ../../src
```

## Transformations

### Lambda Simplification

Before:
```clojure
{:malli/schema [:=> [:cat [:fn (fn* [p1__1234#] (instance? File p1__1234#))]] :boolean]}
```

After:
```clojure
{:malli/schema [:=> [:cat [:fn (partial instance? File)]] :boolean]}
```

### Predicate Simplification

Before:
```clojure
{:malli/schema [:=> [:cat [:fn (fn* [p1__1234#] (string? p1__1234#))]] :keyword]}
```

After:
```clojure
{:malli/schema [:=> [:cat [:fn string?]] :keyword]}
```

## Files

- `src/malli_cleanup/analyzer.clj` - Analyzes codebase for Malli patterns
- `src/malli_cleanup/simple_transformer.clj` - Performs transformations
- `src/malli_cleanup/core.clj` - Main CLI interface
- `apply_migration.sh` - Convenience script for migration

## Testing

Test files are provided in the `test/` directory:
- `sample_input.clj` - Input with various patterns
- `expected_output.clj` - Expected transformation results

Run tests:
```bash
clojure -M:test
```

## Safety

The tool:
1. Creates backups before applying changes
2. Provides dry-run mode by default
3. Generates detailed reports of changes
4. Outputs to separate directory for review