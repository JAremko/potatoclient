# PotatoClient Project Structure Quick Reference

## Key Directories

### Protobuf Definitions
- **Location**: `./proto/`
- **Purpose**: Contains all `.proto` files with `buf.validate` annotations
- **Key files**:
  - `jon_shared_cmd.proto` - Root command structure
  - `jon_shared_cmd_*.proto` - Platform-specific commands
  - `jon_shared_data_types.proto` - Shared enum definitions

### TypeScript Reference Implementation
- **Location**: `./examples/web/frontend/ts/`
- **Purpose**: Original TypeScript implementation that Clojure is based on
- **Key directories**:
  - `cmd/` - Command implementations
  - `statePub/` - State management patterns

### Test Files

#### Transit and Command Tests
- **Location**: `./test/potatoclient/transit/`
- **Purpose**: Transit-based command and validation testing
- **Key files**:
  - `command_roundtrip_test.clj` - Tests all 29 command types
  - `malli_generation_test.clj` - Malli generator validation
  - `simple_malli_validation_test.clj` - Basic command generation
  - `sanity_check_validation_test.clj` - Pipeline sanity checks
  - `generator_roundtrip_test.clj` - Property-based testing
  - `kotlin_generator_validation_test.clj` - Full Kotlin integration
  - `VALIDATION_SANITY_CHECKS.md` - Validation documentation

#### Kotlin Tests
- **Location**: `./test/kotlin/potatoclient/kotlin/transit/`
- **Purpose**: Kotlin-side validation and integration
- **Key files**:
  - `TestCommandProcessor.kt` - Enhanced command processor for testing
  - `ValidatorSanityTest.kt` - buf.validate constraint tests
  - `MalliPayloadValidator.kt` - Standalone validation tool
  - `BufValidateTest.kt` - Protobuf validation tests
  - `GeneratedHandlersRoundtripTest.kt` - Handler roundtrip tests

### Documentation
- **Main docs**:
  - `CLAUDE.md` - Primary developer guide
  - `README.md` - Project overview
  - `docs/TESTING_AND_VALIDATION.md` - Testing guide
- **Technical docs** (`.claude/` directory):
  - `protobuf-command-system.md` - Protobuf implementation details
  - `kotlin-subprocess.md` - Kotlin video streaming architecture
  - `linting-guide.md` - Code quality tools
  - `malli-validation-testing.md` - Comprehensive validation testing guide
  - `transit-architecture.md` - Transit-based IPC architecture
  - `transit-protocol.md` - Message protocol specification

## Important Files for Command System

### Clojure Implementation
- `src/potatoclient/cmd/` - All command namespaces
- `src/potatoclient/specs.clj` - Malli validation schemas
- `src/potatoclient/proto.clj` - Protobuf serialization

### Generated Code
- `src/potatoclient/java/` - Generated Java protobuf classes
- `target/classes/` - Compiled protobuf classes

## Build and Test Commands

```bash
# Protobuf generation
make proto

# Run tests
make test

# Run specific command tests
clojure -M:test -n potatoclient.cmd.comprehensive-command-test

# Development with validation
make dev

# Check code quality
make lint
```