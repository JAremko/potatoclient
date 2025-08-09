# Validate Tool

A high-performance Clojure tool for validating Protocol Buffer binary payloads using `buf.validate` constraints. This tool validates binary protobuf messages against their schemas with comprehensive constraint checking.

## 🚀 Features

- **Binary Validation**: Validates protobuf binary files using buf.validate constraints
- **Dual Message Support**: Handles both command (`cmd.JonSharedCmd$Root`) and state (`ser.JonSharedData$JonGUIState`) messages
- **Auto-Detection**: Intelligently detects message type when not specified
- **Performance Optimized**: Cached validator with < 20ms per validation
- **Multiple Output Formats**: Text, JSON, and EDN output formats
- **Comprehensive Validation**: Field-level constraint validation including:
  - GPS coordinate ranges (latitude: -90 to 90, longitude: -180 to 180)
  - Protocol version validation (must be > 0)
  - Enum constraints (client type cannot be UNSPECIFIED)
  - Nested message requirements
- **Idiomatic Pronto Usage**: Utilizes the Pronto library for efficient protobuf handling
- **Extensive Test Coverage**: 32 tests with 91 assertions, 100% passing

## 📋 Prerequisites

- Java 11 or higher
- Clojure CLI tools
- Docker (for protobuf generation)
- Make (for build automation)

## 🔧 Installation

### 1. Generate Protobuf Classes

First-time setup requires generating the protobuf Java classes:

```bash
# Full build with proto generation and compilation
make build
```

This will:
1. Generate protobuf Java classes with buf.validate support
2. Compile all Java sources
3. Compile Pronto performance optimizations

### 2. Quick Start

```bash
# Download dependencies
make deps

# Run tests to verify installation
make test
```

## 💻 Usage

### Command Line Interface

```bash
# Validate a binary file (auto-detect type)
make validate FILE=path/to/file.bin

# Validate as state message explicitly
make validate-state FILE=output/state.bin

# Validate as command message explicitly  
make validate-cmd FILE=commands.bin

# With JSON output
make validate FILE=data.bin OUTPUT=json

# With verbose output
make validate FILE=data.bin VERBOSE=true
```

### Direct Clojure Usage

```bash
# Show help
clojure -M:run -h

# Validate a file with auto-detection
clojure -M:run -f output/state.bin

# Validate with specific type
clojure -M:run -f commands.bin -t cmd

# JSON output with verbose mode
clojure -M:run -f data.bin -o json -v
```

### Command Line Options

| Option | Description | Default |
|--------|------------|---------|
| `-f, --file FILE` | Path to binary file to validate | Required |
| `-t, --type TYPE` | Message type: `state`, `cmd`, or `auto` | `auto` |
| `-o, --output FORMAT` | Output format: `text`, `json`, or `edn` | `text` |
| `-v, --verbose` | Enable verbose output | `false` |
| `-h, --help` | Show help message | - |

### Programmatic Usage

```clojure
(require '[validate.validator :as v])

;; Validate binary data with auto-detection
(v/validate-binary data)

;; Validate with specific type
(v/validate-binary data :type :state)

;; Validate a file
(v/validate-file "path/to/file.bin")

;; Validate from input stream
(v/validate-stream input-stream :type :cmd)
```

## 🧪 Testing

### Run All Tests

```bash
# Run complete test suite
make test

# Start REPL for interactive development
make repl
```

### Test Structure

The tool includes comprehensive testing with idiomatic Pronto patterns:

- **`test_harness.clj`**: Real-world test data generation using Pronto
- **`validator_test.clj`**: Core validation logic tests
- **`validation_test.clj`**: Validation behavior tests
- **`pronto_test.clj`**: Pronto performance and immutability tests
- **`harness_test.clj`**: Test data generation verification

## 📁 Project Structure

```
validate/
├── src/
│   └── validate/
│       ├── core.clj         # CLI entry point
│       └── validator.clj    # Core validation with caching
├── test/
│   └── validate/
│       ├── test_harness.clj    # Idiomatic Pronto test data
│       ├── validator_test.clj  # Core validator tests
│       ├── validation_test.clj # Behavior tests
│       ├── pronto_test.clj     # Pronto-specific tests
│       └── harness_test.clj    # Harness verification
├── deps.edn                     # Dependencies (includes Pronto fork)
├── build.clj                    # Build configuration
├── Makefile                     # Build automation
├── TODO.md                      # Development roadmap
└── scripts/
    └── generate-protos.sh       # Protobuf generation with buf
```

## ⚡ Performance

The validator uses a cached instance for optimal performance:

- **First validation**: ~160ms (includes validator initialization)
- **Subsequent validations**: < 20ms per validation
- **Batch processing**: 100 validations in < 2 seconds

## 🔍 Validation Process

1. **Binary Parsing**: Parses binary data as protobuf message
2. **Type Detection**: Auto-detects between state and cmd if not specified
3. **Constraint Validation**: Validates using buf.validate rules
4. **Result Formatting**: Outputs in requested format (text/json/edn)

## 📊 Example Output

### Successful Validation

```
✓ Validation successful
  Type: state
  Size: 1458 bytes
```

### Failed Validation

```
✗ Validation failed
  Type: state
  Size: 1458 bytes
  
Violations:
  • gps.latitude: value must be between -90 and 90 (was 200.0)
  • gps.longitude: value must be between -180 and 180 (was -400.0)
  • protocol_version: value must be greater than 0 (was 0)
```

### JSON Output

```json
{
  "valid": false,
  "message": "Validation failed",
  "type": "state",
  "size": 1458,
  "violations": [
    {
      "field": "gps.latitude",
      "constraint": "double.gte_lte",
      "message": "value must be between -90 and 90"
    }
  ]
}
```

## 🛠️ Development

### REPL Development

```bash
# Start REPL with test paths
make repl

# In REPL
(require '[validate.validator :as v])
(require '[validate.test-harness :as h] :reload)
```

### Building from Source

```bash
# Clean build artifacts
make clean

# Full rebuild
make clean-build
make build

# Run tests
make test
```

## 🐛 Troubleshooting

### Proto Classes Not Found

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

```bash
# Check for outdated dependencies
clojure -M:outdated

# Update dependencies
make deps
```

## 🎯 Validation Constraints

The tool enforces various buf.validate constraints:

| Field | Constraint | Valid Range |
|-------|-----------|-------------|
| GPS Latitude | `double.gte_lte` | -90 to 90 |
| GPS Longitude | `double.gte_lte` | -180 to 180 |
| GPS Altitude | `double.gte_lte` | -433 to 8848.86 |
| Protocol Version | `uint32.gt` | > 0 |
| Client Type | `enum.defined_only` | Not UNSPECIFIED |
| Rotary Speed | `double.gt_lte` | > 0 and ≤ 1 |

## 🚦 Current Status

✅ **Production Ready**
- All core features implemented
- Performance optimized with validator caching
- Comprehensive test coverage
- Field naming standardized (underscores)
- Enum values standardized (uppercase)
- Rotary command support added

## 📝 Future Enhancements

See [TODO.md](TODO.md) for planned enhancements including:
- Batch validation support
- Directory scanning
- Colored terminal output
- HTML report generation
- Custom validation rules

## 📄 License

See the main project LICENSE file.

## 🤝 Contributing

This tool is part of the PotatoClient project. For contribution guidelines, see the main project documentation.

---

*Built with Clojure, Pronto, and buf.validate for high-performance protobuf validation*