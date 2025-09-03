# Kondo Config Generator

A tool to generate clj-kondo configurations from Malli function schemas in the PotatoClient codebase.

## Purpose

This tool bridges the gap between runtime validation (Malli) and static analysis (clj-kondo) by:
- Collecting all `:malli/schema` metadata from functions
- Generating clj-kondo type configurations for static checking
- Enabling IDE support for function signatures
- Providing compile-time validation alongside runtime instrumentation

## Features

- **Automatic Schema Collection**: Scans all namespaces for Malli function schemas
- **Config Generation**: Creates clj-kondo configurations for static analysis
- **Verification**: Validates generated configs are well-formed
- **Summary Reports**: Generates metadata about collected schemas

## Usage

### Generate Configs

From this directory:
```bash
make run
```

From project root:
```bash
make kondo-configs
```

### Verify Configs

```bash
make verify
```

### Run Tests

```bash
make test
```

## Output

The tool generates files in `.clj-kondo/`:
- `config.edn` - Main clj-kondo configuration with type information
- `malli-schemas.edn` - Summary of collected schemas

## How It Works

1. **Initialize Registry**: Sets up the global Malli schema registry
2. **Load Namespaces**: Requires all namespaces containing schemas
3. **Collect Schemas**: Uses `malli.instrument/collect!` to gather function schemas
4. **Generate Configs**: Uses `malli.clj-kondo/emit!` to create configurations
5. **Verify Output**: Validates the generated EDN is well-formed

## Integration with Development

When combined with the instrumentation system:
- **Runtime**: Malli instrumentation validates at runtime
- **Static**: Clj-kondo checks at compile/edit time
- **IDE**: Provides inline warnings and suggestions

## Troubleshooting

### No schemas found
- Ensure namespaces are properly required
- Check that functions have `:malli/schema` metadata
- Verify registry initialization succeeded

### Config file invalid
- Check for syntax errors in function schemas
- Ensure all referenced schemas are registered
- Look for incompatible schema types

## Development

### Adding New Namespaces

Edit `src/kondo_gen/core.clj` and add to `namespaces-with-schemas`:
```clojure
(def ^:private namespaces-with-schemas
  ['your.new.namespace
   ...])
```

### Running Tests

```bash
make test
```

Tests verify:
- Schema collection works correctly
- Config generation produces valid EDN
- Generated configs are usable by clj-kondo