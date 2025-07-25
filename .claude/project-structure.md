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
- **Location**: `./test/potatoclient/cmd/`
- **Purpose**: Comprehensive test coverage for command system
- **Key files**:
  - `comprehensive_command_test.clj` - Tests all 200+ commands
  - `generator_test.clj` - Property-based testing
  - `validation_safety_test.clj` - Boundary validation tests
  - `test_helpers.clj` - Enum conversion helpers

### Documentation
- **Main docs**:
  - `CLAUDE.md` - Primary developer guide
  - `README.md` - Project overview
  - `docs/TESTING_AND_VALIDATION.md` - Testing guide
- **Technical docs** (`.claude/` directory):
  - `protobuf-command-system.md` - Protobuf implementation details
  - `kotlin-subprocess.md` - Kotlin video streaming architecture
  - `linting-guide.md` - Code quality tools

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