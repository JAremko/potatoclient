# Validate

A Clojure-based tool for validating protobuf binary payloads using `buf.validate` annotations. This tool can validate binary files against their protobuf schemas with validation constraints.

## Features

- **Binary Validation**: Validates protobuf binary files using buf.validate
- **Dual Mode Support**: Validates both command (`cmd.JonSharedCmd`) and state (`ser.JonSharedData`) messages
- **Auto-Detection**: Automatically detects message type when not specified
- **Multiple Output Formats**: Supports text, JSON, and EDN output formats
- **Comprehensive Testing**: Includes unit tests with in-memory validation and end-to-end tests using NIO2 virtual filesystem
- **Detailed Error Reporting**: Shows field-level validation violations with constraint details

## Prerequisites

- Java 11 or higher
- Clojure CLI tools
- Docker (for protobuf generation)
- Git LFS (optional, for faster protobuf generation)

## Installation

### 1. Generate Protobuf Classes

First time setup requires generating the protobuf Java classes:

```bash
make build
```

This will:
1. Clone the protogen repository
2. Build the Docker image for protobuf generation
3. Generate Java classes with buf.validate support
4. Compile all Java sources

### 2. Download Dependencies

```bash
make deps
```

## Usage

### Command Line Interface

```bash
# Validate a binary file (auto-detect type)
make validate FILE=path/to/file.bin

# Validate as state message explicitly
make validate-state FILE=output/state_20241208.bin

# Validate as command message explicitly
make validate-cmd FILE=commands.bin

# With verbose output
make validate FILE=data.bin VERBOSE=true

# With JSON output
make validate FILE=data.bin OUTPUT=json
```

### Direct Clojure Usage

```bash
# Show help
clojure -M:run -h

# Validate a file
clojure -M:run -f output/state_20241208.bin

# Validate with specific type
clojure -M:run -f commands.bin -t cmd

# JSON output with verbose mode
clojure -M:run -f data.bin -o json -v
```

### Options

- `-f, --file FILE` - Path to binary file to validate (required)
- `-t, --type TYPE` - Message type: `state`, `cmd`, or `auto` (default: auto)
- `-o, --output FORMAT` - Output format: `text`, `json`, or `edn` (default: text)
- `-v, --verbose` - Enable verbose output
- `-h, --help` - Show help message

## Testing

### Run All Tests

```bash
make test
```

### Test Structure

The tool includes comprehensive testing:

1. **Test Harness** (`test_harness.clj`):
   - Ensures protobuf classes are compiled
   - Generates test binary messages
   - Creates invalid/corrupted test data

2. **Unit Tests** (`validator_test.clj`):
   - In-memory validation tests
   - Parser and auto-detection tests
   - Error handling tests

3. **End-to-End Tests** (`e2e_test.clj`):
   - Uses Jimfs (Google's in-memory NIO2 filesystem)
   - Tests file I/O without actual disk access
   - Concurrent file validation tests
   - Large file handling tests

## Development

### REPL Development

```bash
make repl
```

### Project Structure

```
validate/
├── src/
│   ├── validate/
│   │   ├── core.clj         # CLI entry point
│   │   ├── validator.clj    # Core validation logic
│   │   └── test_harness.clj # Test utilities
│   └── java/                # Generated protobuf classes
├── test/
│   └── validate/
│       ├── validator_test.clj # Unit tests
│       └── e2e_test.clj      # End-to-end tests
├── deps.edn                  # Dependencies
├── build.clj                 # Build configuration
├── Makefile                  # Build automation
└── scripts/
    └── generate-protos.sh    # Protobuf generation script
```

## Validation Process

1. **Binary Parsing**: Attempts to parse the binary data as protobuf
2. **Type Detection**: If type is auto, tries both state and cmd formats
3. **Constraint Validation**: Uses buf.validate to check field constraints
4. **Result Formatting**: Outputs validation results in requested format

## Example Output

### Successful Validation

```
Validation Result:
  Message Type: state
  Message Size: 1024 bytes
  Valid: true
```

### Failed Validation

```
Validation Result:
  Message Type: cmd
  Message Size: 256 bytes
  Valid: false
  Error: Validation failed
  Violations:
    - Field: system.reboot.delayMs
      Constraint: int32.gte
      Message: value must be greater than or equal to 0
```

## Troubleshooting

### Proto Classes Not Found

If you see errors about missing proto classes:

```bash
make clean-build
make build
```

### Docker Issues

Ensure Docker is running:

```bash
docker info
```

### Dependency Issues

Check for outdated dependencies:

```bash
make deps-outdated
```

Upgrade dependencies interactively:

```bash
make deps-upgrade
```

## Integration with Other Tools

This tool is designed to work with binary protobuf files for validation purposes.

## License

See the main project LICENSE file.