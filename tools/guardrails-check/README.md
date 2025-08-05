# Guardrails Check Tool

A Babashka-based tool for finding Clojure functions that use raw `defn`/`defn-` instead of Guardrails' `>defn`/`>defn-`.

## Overview

This tool helps maintain consistent validation across a Clojure codebase by identifying functions that are not using [Guardrails](https://github.com/fulcrologic/guardrails), a runtime validation library that provides better error messages and zero overhead in production.

## Features

- Fast analysis using Babashka (no JVM startup overhead)
- Multiple output formats (EDN, Markdown)
- Pattern-based function search
- Statistics and namespace listing
- Configurable source directory

## Installation

This tool requires [Babashka](https://github.com/babashka/babashka) to be installed on your system.

```bash
# Install Babashka (if not already installed)
curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install
chmod +x install
./install
```

## Usage

### Using Babashka directly

```bash
# Check for unspecced functions (EDN output)
bb check [src-dir]

# Generate markdown report
bb report [src-dir]

# Show statistics
bb stats [src-dir]

# List affected namespaces
bb list [src-dir]

# Find specific functions
bb find <pattern> [src-dir]

# Show help
bb help
```

### Using Make

```bash
# Check default src/potatoclient
make check

# Generate report for custom directory
make report SRC_DIR=../src

# Find functions with 'process' in name
make find PATTERN=process

# Save report to file
make save-report

# Show all available commands
make help
```

## Examples

### Check a project

```bash
$ bb check src/myproject
{:summary
 {:total-unspecced-functions 15,
  :total-namespaces-with-issues 3,
  :namespaces [:myproject.core :myproject.utils :myproject.handlers]},
 :details
 {:myproject.core [init shutdown process-request],
  :myproject.utils [parse-config validate-input],
  :myproject.handlers [handle-get handle-post handle-delete]}}
```

### Generate a report

```bash
$ bb report src/myproject
# Guardrails Check Report

## Summary
- Total unspecced functions: 15
- Namespaces with issues: 3

## Functions Without Guardrails

### myproject.core
- init
- process-request
- shutdown

### myproject.handlers
- handle-delete
- handle-get
- handle-post

### myproject.utils
- parse-config
- validate-input
```

### Find specific functions

```bash
$ bb find handle src/myproject
{:pattern "handle",
 :matches
 [{:namespace :myproject.handlers, :function handle-get}
  {:namespace :myproject.handlers, :function handle-post}
  {:namespace :myproject.handlers, :function handle-delete}],
 :count 3}
```

## Output Formats

- **EDN**: Machine-readable format for integration with other tools
- **Markdown**: Human-readable reports suitable for documentation
- **Statistics**: Quick overview of Guardrails adoption

## Integration with PotatoClient

This tool was extracted from the PotatoClient project to be standalone. In the main project, you can run:

```bash
# From project root
make report-unspecced

# Or directly use the tool
cd tools/guardrails-check
make report SRC_DIR=../../src/potatoclient
```

## How it Works

The tool:
1. Recursively finds all Clojure source files (`.clj`, `.cljs`, `.cljc`)
2. Parses each file looking for function definitions
3. Identifies functions using `defn`/`defn-` vs `>defn`/`>defn-`
4. Generates reports grouped by namespace

## Limitations

- Simple regex-based parsing (doesn't understand commented code)
- Only checks function definitions, not other Guardrails features
- Doesn't check if Guardrails is actually imported in the namespace

## Contributing

To improve this tool:
1. Modify the core logic in `src/guardrails_check/core.clj`
2. Update CLI commands in `src/guardrails_check/cli.clj`
3. Add new tasks to `bb.edn`
4. Update documentation

## License

Same as PotatoClient project.