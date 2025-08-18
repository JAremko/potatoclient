# PotatoClient Shared Module

## Overview

The `shared` module is the foundational library for the PotatoClient project, providing Protocol Buffer definitions, Malli specifications, and serialization utilities that ensure type safety and protocol compliance across all subsystems.

This module serves as the single source of truth for:
- Protocol Buffer message definitions (commands and state)
- Malli runtime validation specifications
- Serialization/deserialization between EDN and protobuf binary formats
- Shared data structures and enums

## Architecture

### Core Components

1. **Protocol Buffers**: Define the wire protocol for all system communication
2. **Malli Specifications**: Runtime validation mirroring protobuf constraints
3. **Serialization Layer**: Bidirectional EDN ↔ Protobuf conversion
4. **Validation Pipeline**: Two-tier validation (Malli + buf.validate)

### Design Principles

- **No Backward Compatibility**: Pre-alpha stage, breaking changes allowed
- **Strict Validation**: All map specs use `{:closed true}` to reject extra keys
- **Comprehensive Testing**: Every spec and serialization path is tested
- **Type Safety**: Protobuf + Malli ensures compile-time and runtime safety

## Directory Structure

```
shared/
├── Makefile                    # Build commands for proto generation
├── deps.edn                    # Clojure dependencies
├── build.clj                   # Build configuration
├── scripts/
│   ├── generate-protos.sh     # Proto generation script
│   └── fix-proto-compatibility.sh  # Java compatibility fixes
├── src/
│   ├── java/                   # Generated Java classes from protobuf
│   │   ├── cmd/               # Command message classes
│   │   │   ├── JonSharedCmd.java
│   │   │   ├── JonSharedCmdCompass.java
│   │   │   ├── JonSharedCmdGps.java
│   │   │   ├── JonSharedCmdLrf.java
│   │   │   ├── JonSharedCmdRotary.java
│   │   │   └── JonSharedCmdTracker.java
│   │   └── ser/               # State/data message classes
│   │       ├── JonSharedData.java
│   │       ├── JonSharedDataCamera.java
│   │       ├── JonSharedDataGps.java
│   │       ├── JonSharedDataLrf.java
│   │       ├── JonSharedDataRotary.java
│   │       ├── JonSharedDataSystem.java
│   │       └── JonSharedDataTracker.java
│   └── potatoclient/
│       ├── proto/
│       │   ├── serialize.clj   # EDN → Protobuf conversion
│       │   └── deserialize.clj # Protobuf → EDN conversion
│       ├── specs/
│       │   ├── cmd/            # Command message specs
│       │   │   ├── root.clj
│       │   │   ├── compass.clj
│       │   │   ├── gps.clj
│       │   │   ├── lrf.clj
│       │   │   ├── rotary.clj
│       │   │   └── tracker.clj
│       │   ├── state/          # State message specs
│       │   │   ├── root.clj
│       │   │   ├── camera.clj
│       │   │   ├── gps.clj
│       │   │   ├── lrf.clj
│       │   │   ├── rotary.clj
│       │   │   ├── system.clj
│       │   │   └── tracker.clj
│       │   └── common.clj      # Shared specs (enums, types)
│       └── malli/
│           ├── oneof.clj       # Custom oneof schema for protobuf
│           └── registry.clj    # Centralized spec registry
├── test/
│   └── potatoclient/
│       ├── test_harness.clj   # Test initialization
│       ├── proto/              # Serialization tests
│       └── specs/              # Spec validation tests
└── target/                     # Build output (generated)
    └── classes/                # Compiled Java classes
```

## API Reference

### Proto Serialization API

```clojure
;; Serialize EDN to protobuf binary
(require '[potatoclient.proto.serialize :as serialize])

;; Create a GPS command
(serialize/edn->proto 
  {:cmd/root 
    {:gps {:set-position {:latitude 40.7128 
                          :longitude -74.0060}}}}
  :cmd/root)
;; => byte array

;; Deserialize protobuf binary to EDN
(require '[potatoclient.proto.deserialize :as deserialize])

(deserialize/proto->edn proto-bytes :cmd/root)
;; => {:cmd/root {:gps {:set-position {...}}}}
```

### Malli Spec API

```clojure
;; Access specs from registry
(require '[potatoclient.specs.cmd.root :as cmd-spec])
(require '[malli.core :as m])

;; Validate command
(m/validate cmd-spec/root-spec command-data)

;; Generate test data
(require '[malli.generator :as mg])
(mg/generate cmd-spec/root-spec)
```

### Available Message Types

#### Commands (cmd namespace)
- **Root**: `cmd/root` - Command wrapper with oneof selection
- **Compass**: `cmd/compass` - Compass control commands
- **GPS**: `cmd/gps` - GPS positioning commands
- **LRF**: `cmd/lrf` - Laser rangefinder commands
- **Rotary**: `cmd/rotary` - Rotary platform control
- **Tracker**: `cmd/tracker` - Target tracking commands

#### State (state namespace)
- **Root**: `state/root` - System state wrapper (JonGUIState)
- **Camera**: `state/camera` - Camera subsystem state
- **GPS**: `state/gps` - GPS position and status
- **LRF**: `state/lrf` - Laser rangefinder readings
- **Rotary**: `state/rotary` - Platform position state
- **System**: `state/system` - Overall system status
- **Tracker**: `state/tracker` - Tracking subsystem state

### Common Enums and Types

Located in `potatoclient.specs.common`:
- **Angles**: `angle-deg`, `angle-rad`, `angle-mrad`
- **Positions**: `latitude`, `longitude`, `altitude`
- **Enums**: Camera types, tracking modes, system states
- **Validation**: Range constraints, precision limits

## Build Commands

### Development Workflow

```bash
# Daily development - compile existing proto sources
make compile

# Run all tests (auto-compiles if needed)
make test

# Full proto regeneration (when proto files change)
make proto

# Clean all generated artifacts
make proto-clean
```

### Proto Generation Process

1. **Fetch Proto Files**: Retrieved from external `protogen` repository
2. **Generate Java**: Docker-based `jettison-proto-generator` creates Java classes
3. **Fix Compatibility**: Script patches Java 8 compatibility issues
4. **Compile Classes**: Java compilation to target/classes
5. **Generate Pronto**: Clojure-Java bridge code generation

## Testing

### Test Categories

- **Unit Tests**: Individual spec validation
- **Integration Tests**: Full serialization round-trips
- **Property Tests**: Generative testing with test.check
- **Validation Tests**: buf.validate constraint verification

### Running Tests

```bash
# Run all tests
lein test

# Or using Makefile
make test

# Run specific namespace
lein test potatoclient.proto.serialize-test
```

## Integration Guide

### For Other Modules

1. **Add Dependency** in your `deps.edn`:
```clojure
{:deps {potatoclient/shared {:local/root "../shared"}}}
```

2. **Import Specs**:
```clojure
(require '[potatoclient.specs.cmd.root :as cmd])
(require '[potatoclient.specs.state.root :as state])
```

3. **Use Serialization**:
```clojure
(require '[potatoclient.proto.serialize :as ser])
(require '[potatoclient.proto.deserialize :as deser])
```

### Proto Class Access

Java classes are available after compilation:
- Commands: `cmd.JonSharedCmd$*`
- State: `ser.JonSharedData$*`

Example:
```java
import cmd.JonSharedCmd.Root;
import ser.JonSharedData.JonGUIState;
```

## Validation Pipeline

### Two-Tier Validation

1. **Malli Validation** (Clojure):
   - Structure validation
   - Type checking
   - Range constraints
   - Custom business rules

2. **buf.validate** (Protobuf):
   - Wire format compliance
   - Field constraints
   - Enum validation
   - Required field checks

### Validation Flow

```
EDN Data → Malli Validation → Proto Construction → buf.validate → Binary
                     ↓ fail                              ↓ fail
                  Exception                          Exception
```

## Development Guidelines

### Adding New Message Types

1. **Define Proto** in protogen repository
2. **Run Generation**: `make proto`
3. **Create Malli Spec** in `src/potatoclient/specs/`
4. **Add Tests** in `test/potatoclient/specs/`
5. **Update Registry** if needed

### Spec Requirements

- All maps must use `{:closed true}`
- Mirror buf.validate constraints exactly
- Include comprehensive docstrings
- Provide generators for testing

### Testing Philosophy

- Never disable failing tests
- Fix the code, not the test
- Ensure 100% spec coverage
- Test all serialization paths

## Dependencies

### Core Dependencies
- **Clojure**: 1.12.1
- **Malli**: 0.19.1 (runtime validation)
- **Guardrails**: 1.2.9 (function specs)
- **Protobuf Java**: 4.31.1
- **buf.validate**: 0.3.2

### Development Dependencies
- **test.check**: 1.1.1 (property testing)
- **cognitect test-runner**: 0.5.1

## Troubleshooting

### Common Issues

1. **Proto Compilation Fails**
   - Ensure Docker is running
   - Check network access to protogen repo
   - Run `make proto-clean` then `make proto`

2. **Class Not Found**
   - Run `make compile` to ensure Java classes are built
   - Check target/classes directory exists

3. **Validation Mismatch**
   - Ensure specs match latest proto definitions
   - Check buf.validate constraints in proto files
   - Verify Malli spec has `{:closed true}`

## License

[Project License Information]

## Contributing

See main project CONTRIBUTING.md for guidelines.

### Contact

[Project Contact Information]