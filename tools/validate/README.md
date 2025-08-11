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

# Run only property-based tests
clojure -M:test -n validate.specs.state-property-test
clojure -M:test -n validate.specs.cmd-property-test

# Start REPL for interactive development
make repl
```

### Property-Based Testing

The validate tool includes extensive property-based testing to ensure Malli specs are 100% compatible with buf.validate constraints:

```clojure
;; In REPL - test state specs with 1000 samples
(require '[validate.spec-validation-harness :as harness])
(harness/test-state-spec :n 1000 :verbose? true)

;; Test command specs
(harness/test-cmd-spec :n 500)

;; Analyze failures if any occur
(-> (harness/test-state-spec :n 1000)
    harness/analyze-violations)

;; Test specific sub-message
(harness/test-sub-message-spec 
  :state/gps 
  h/state-mapper 
  ser.JonSharedData$JonGUIState$GPS 
  :n 300)
```

### Hierarchical Message Validation

#### Important Discovery: Sub-Message Validation

The production validator (`validate.validator`) is designed to only validate root messages (JonGUIState and JonSharedCmd$Root). However, buf.validate constraints ARE present on all sub-messages and can be validated independently using a test-specific validator.

**Production Validator** (for actual use):
- Only validates complete root messages  
- Attempts to parse all binaries as either State or Cmd root
- Sub-messages fail validation when tested standalone

**Test Validator** (`validate.test-validator`):
- Can validate any protobuf message type independently
- Enables bottom-up testing strategy
- Validates sub-messages directly without requiring root context

#### Bottom-Up Testing Strategy

For comprehensive testing, we validate messages hierarchically:

1. **Level 1 (Leaf Messages)**: Validate GPS, System, Time, etc. independently
2. **Level 2 (Composite Messages)**: Validate messages with nested sub-messages  
3. **Level 3 (Root Messages)**: Validate complete State or Cmd with all sub-messages

Example test output demonstrating the difference:
```
=== GPS Sub-message Validation ===
Direct validation (test_validator):
  Valid?: true
  Violations: []

Binary validation (production validator):
  Detected as: :cmd
  Valid?: false
  Violations: (payload required, protocol_version > 0, client_type not UNSPECIFIED)
```

This hierarchical approach ensures:
- Sub-message specs are correct and generate valid data
- Parent messages properly compose sub-messages
- Complete root messages validate with all constraints

### Test Structure

The tool includes comprehensive testing with idiomatic Pronto patterns and property-based testing:

#### Core Test Files
- **`test_harness.clj`**: Real-world test data generation using Pronto
- **`validator_test.clj`**: Core validation logic tests
- **`validation_test.clj`**: Validation behavior tests
- **`pronto_test.clj`**: Pronto performance and immutability tests
- **`harness_test.clj`**: Test data generation verification

#### Property-Based Testing Suite
- **`spec_validation_harness.clj`**: Comprehensive test harness for round-trip validation
  - Spec → Proto-map → Binary → Proto-map → Spec validation pipeline
  - Validates Malli-generated data against buf.validate constraints
  - Ensures 100% compatibility between Malli specs and protobuf validation
- **`specs/state_property_test.clj`**: Property-based tests for State messages
  - Tests all 14 sub-messages with 300+ generated samples each
  - Manual edge case testing for boundary values
  - Round-trip validation for complete state messages
- **`specs/cmd_property_test.clj`**: Property-based tests for Command messages
  - Tests all 15 command types with generated samples
  - Validates oneof exclusivity constraints
  - Boundary value testing for protocol versions and rotary speeds

#### Property Testing Features
- **Round-trip Validation**: Each generated value goes through complete cycle:
  1. Generate data from Malli spec
  2. Convert to proto-map
  3. Validate with buf.validate
  4. Serialize to binary
  5. Deserialize back to proto-map
  6. Validate with buf.validate again
  7. Convert back to EDN
  8. Validate with Malli spec
  9. Check equality with original

- **Coverage Analysis**: Ensures specs generate all required fields and enum values
- **Violation Analysis**: Identifies patterns in validation failures to improve specs
- **Manual + Generated Testing**: Combines manual edge cases with 100-300 generated samples per message type

## 📁 Project Structure

```
validate/
├── src/
│   ├── java/                    # Generated protobuf Java classes (LOCAL)
│   │   ├── cmd/                 # Command message classes
│   │   └── ser/                 # State message classes
│   ├── potatoclient             # SYMLINK → shared/src/potatoclient
│   └── validate/                # Tool-specific code (LOCAL)
│       ├── core.clj             # CLI entry point
│       ├── examples.clj         # Usage examples
│       ├── specs/               # Spec directories
│       │   ├── cmd              # SYMLINK → shared specs/cmd
│       │   ├── shared           # SYMLINK → shared specs
│       │   └── state            # SYMLINK → shared specs/state
│       └── validator.clj        # Core validation with caching
├── test/                        # Test files (LOCAL)
│   └── validate/
│       ├── test_harness.clj    # Idiomatic Pronto test data
│       ├── validator_test.clj  # Core validator tests
│       ├── validation_test.clj # Behavior tests
│       ├── pronto_test.clj     # Pronto-specific tests
│       ├── harness_test.clj    # Harness verification
│       └── specs/
│           ├── oneof_edn_test.clj # Oneof EDN spec tests
│           └── state_test.clj     # State spec tests
├── deps.edn                     # Dependencies (includes Pronto fork)
├── build.clj                    # Build configuration
├── Makefile                     # Build automation
├── TODO.md                      # Development roadmap
└── scripts/
    └── generate-protos.sh       # Protobuf generation with buf
```

### Directory Types

**LOCAL (Tool-specific)**:
- `src/java/` - Generated protobuf Java classes
- `src/validate/` - Tool-specific validation code
- `test/` - All test files
- Configuration files (deps.edn, build.clj, etc.)

**SYMLINKS (Shared across project)**:
- `src/potatoclient` → `../../../shared/src/potatoclient` - Shared project code
- `src/validate/specs/shared` → `../../../../shared/src/potatoclient/specs` - All shared specs
- `src/validate/specs/cmd` → `../../../../../shared/src/potatoclient/specs/cmd` - Command message specs
- `src/validate/specs/state` → `../../../../../shared/src/potatoclient/specs/state` - State message specs

This structure allows the validate tool to:
1. Use shared Malli specs that are maintained centrally in `/shared/src/potatoclient/specs/`
2. Share specs with other tools and the main application
3. Maintain tool-specific code locally while leveraging shared components
4. Keep generators and spec dependencies in the shared location for consistency

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