# Protobuf and Command System Documentation

This document contains detailed information about PotatoClient's Protobuf implementation and command system architecture.

## Table of Contents

- [Protobuf Implementation Details](#protobuf-implementation-details)
- [Command System Architecture](#command-system-architecture)
- [Package Structure](#package-structure)
- [Command Validation and Specs](#command-validation-and-specs)
- [Command Infrastructure](#command-infrastructure)
- [Command Flow](#command-flow)
- [JSON Output Format](#json-output-format)
- [Testing Commands](#testing-commands)
- [Development Mode Debugging](#development-mode-debugging)
- [Debug Utilities](#debug-utilities)
- [Build System Integration](#build-system-integration)
- [Troubleshooting](#troubleshooting)

## Protobuf Implementation Details

PotatoClient uses a **direct Protobuf implementation** (migrated from Pronto wrapper library):

**Key Features**:
- Custom kebab-case conversion for Clojure idioms
- Direct manipulation of Protobuf builders and messages
- No external wrapper dependencies
- Protobuf version 4.29.5 with protoc 29.5
- Google's JsonFormat for protobuf to JSON conversion (debugging)
- **protobuf-java-util** dependency required for JsonFormat functionality

**Package Structure for Generated Classes**:
- Proto files generate classes in simple package names (not `com.potatocode.jon`)
- Command messages: `cmd` package (e.g., `cmd.JonSharedCmd$Root`)
- Platform-specific commands: Sub-packages like `cmd.RotaryPlatform`
- Data types: `ser` package (e.g., `ser.JonSharedDataTypes`)

**Important Package Names**:
- Data types are in the `ser` package (e.g., `ser.JonSharedDataTypes`)
- Command types are in the `cmd` package and sub-packages
- The preprocessing script maintains these package names during proto compilation

**Serialization** (Clojure map → Protobuf bytes):
```clojure
;; Example: Serialize a command
(proto/encode-command {:action :connect :url "wss://example.com"})
```

**Deserialization** (Protobuf bytes → Clojure map):
```clojure
;; Example: Deserialize GUI state
(proto/decode-gui-state proto-bytes)
;; Returns: {:connected true :stream-type :heat ...}
```

**Protobuf to JSON Debugging**:
```clojure
;; Using Google's JsonFormat for debugging
(-> (JsonFormat/printer)
    (.includingDefaultValueFields)
    (.print protobuf-message))
```

**Case Conversion**:
- Protobuf fields use `camelCase` (Java convention)
- Clojure maps use `:kebab-case` keywords
- Automatic bidirectional conversion handled by `proto.clj`

**Adding New Message Types**:
1. Define in `.proto` files using camelCase
2. Run `make proto` to regenerate Java classes
3. Check generated package structure in `src/potatoclient/java/`
4. Use existing `encode-*` / `decode-*` patterns in `proto.clj`
5. Keys automatically converted to kebab-case in Clojure

## Command System Architecture

PotatoClient includes a command system based on the TypeScript web frontend architecture, enabling control message sending via Protobuf. The implementation is modeled after the example frontend documented in `examples/web/COMMAND_STATE_ARCHITECTURE_REPORT.md`.

### Architecture Overview

The command system uses:
- **Core.async channels** for message routing (similar to BroadcastChannels in TypeScript)
- **Protobuf encoding** for wire format compatibility
- **JSON output** with Base64-encoded payloads
- **Read-only mode** for restricted operation
- **Development mode debugging** with automatic protobuf to JSON decoding

The Clojure implementation follows the same patterns as the TypeScript version in the example web frontend, providing equivalent functionality for command creation, encoding, and dispatching.

## Package Structure

**Important**: The protobuf Java classes are generated in the `cmd` and `data` packages, not `com.potatocode.jon`:
- Command classes: `cmd` package (e.g., `cmd.JonSharedCmd$Root`)
- Rotary platform: `cmd.RotaryPlatform` package (e.g., `cmd.RotaryPlatform.JonSharedCmdRotary$Root`)
- Day camera: `cmd.DayCamera` package (e.g., `cmd.DayCamera.JonSharedCmdDayCamera$Root`)
- Data types: `ser` package (e.g., `ser.JonSharedDataTypes$JonGuiDataRotaryDirection`)

**Critical Class Names**:
- The data types class is `JonSharedDataTypes`, not `JonGuiDataTypes`
- All enums are nested classes within `JonSharedDataTypes`
- Example: `ser.JonSharedDataTypes$JonGuiDataClientType`

## Command Validation and Specs

**IMPORTANT**: All command functions must use domain-specific specs that match the protobuf validation constraints. The `.proto` files in `./proto` directory define exact valid ranges using `buf.validate` annotations.

**Available Domain Specs** (defined in `potatoclient.specs`):
- `::azimuth-degrees` - [0, 360) degrees
- `::elevation-degrees` - [-90, 90] degrees
- `::rotation-speed` - [0, 1] normalized
- `::gps-latitude` - [-90, 90] degrees
- `::gps-longitude` - [-180, 180) degrees
- `::gps-altitude` - [-433, 8848.86] meters (Dead Sea to Everest)
- `::zoom-level` - [0, 1] normalized
- `::focus-value` - [0, 1] normalized
- `::ndc-x`, `::ndc-y` - [-1, 1] normalized device coordinates
- All enum types (e.g., `::rotary-direction`, `::day-camera-palette`)

### Using Protobuf Validation Specs

**For Command and State Functions**: When adding functions that handle commands or state derived from protobuf messages, **ALWAYS check the `.proto` files in the `./proto` directory** for validation constraints. The protobuf files contain `buf.validate` specifications that define the exact valid ranges and constraints for all fields.

**How to Use Protobuf Validation Specs**:

1. **Check the proto files first**:
   ```bash
   # Find validation constraints for a specific field
   grep -n "validate" ./proto/*.proto
   
   # Example: Check azimuth constraints
   grep -A2 -B2 "azimuth" ./proto/jon_shared_cmd_rotary.proto
   ```

2. **Common protobuf validation patterns**:
   - `[(buf.validate.field).float = {gte: 0.0, lte: 1.0}]` - Range [0.0, 1.0]
   - `[(buf.validate.field).double = {gte: -90.0, lte: 90.0}]` - Range [-90, 90]
   - `[(buf.validate.field).int32 = {gte: 0}]` - Non-negative integer
   - `[(buf.validate.field).enum = {defined_only: true}]` - Valid enum value

3. **Create corresponding Malli specs in `potatoclient.specs`**:
   ```clojure
   ;; Example: From proto constraint [(buf.validate.field).float = {gte: 0.0, lt: 360.0}]
   (def azimuth-degrees
     "Azimuth angle in degrees [0, 360)"
     [:double {:min 0.0 :max 360.0}])
   
   ;; Example: From proto constraint [(buf.validate.field).float = {gte: -1.0, lte: 1.0}]
   (def ndc-x
     "Normalized device coordinate X [-1.0 to 1.0]"
     [:and number? [:>= -1.0] [:<= 1.0]])
   ```

4. **Use these specs in Guardrails functions**:
   ```clojure
   (>defn set-platform-azimuth
     "Set platform azimuth to specific value"
     [value]
     [:potatoclient.specs/azimuth-degrees => nil?]  ; Use domain spec, not number?
     ...)
   ```

## Command Infrastructure

### Core (`potatoclient.cmd.core`)
```clojure
;; Initialize the command system
(cmd/init!)

;; Send basic commands
(cmd/send-cmd-ping)
(cmd/send-cmd-frozen)

;; Enable read-only mode (only ping/frozen allowed)
(cmd/set-read-only-mode! true)
```

### Rotary Platform (`potatoclient.cmd.rotary`)
```clojure
;; Basic control
(rotary/rotary-start)
(rotary/rotary-stop)
(rotary/rotary-halt)

;; Position control
(rotary/set-platform-azimuth 45.0)
(rotary/set-platform-elevation 30.0)

;; Rotation control
(rotary/rotary-azimuth-rotate 10.0 
  JonGuiDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)

;; Combined operations
(rotary/rotate-both-to 90.0 5.0 clockwise-dir 45.0 3.0)

;; Scanning
(rotary/scan-start)
(rotary/scan-stop)
```

### Day Camera (`potatoclient.cmd.day-camera`)
```clojure
;; Power and zoom
(camera/power-on)
(camera/zoom-in)
(camera/zoom-direct-value 2.5)

;; Focus control
(camera/focus-auto)
(camera/focus-manual)
(camera/focus-direct-value 1.8)

;; Image settings
(camera/set-stabilization true)
(camera/change-palette (camera/string->palette "bw"))

;; Camera parameters
(camera/set-agc-mode (camera/string->agc-mode "auto"))
(camera/set-exposure-mode (camera/string->exposure-mode "manual"))
(camera/set-shutter-speed 60.0)
```

## Command Flow

1. UI/API calls command function
2. Function creates Protobuf message via Java builders
3. Message encoded to bytes
4. Command posted to core.async channel with metadata
5. Reader loop outputs JSON with Base64 payload
6. (Future: WebSocket/WebTransport sends to server)

## JSON Output Format

Commands are output as JSON for easy integration:
```json
{
  "payload": "CgEBGhIKEAoOCAESCgoICgYSBAgBEAE=",
  "shouldBuffer": true,
  "size": 42
}
```

## Testing Commands

```clojure
;; Run comprehensive test suite
(require '[potatoclient.cmd.test :as cmd-test])
(cmd-test/run-all-tests)
```

This will demonstrate all command types and verify JSON output.

## Development Mode Debugging

In development mode, the command system automatically decodes and logs protobuf messages for easier debugging:

1. **Automatic Logging**: When running with `make dev`, all commands are decoded to JSON and logged at INFO level
2. **Structured Output**: Log entries include the JSON structure, command type, and payload size
3. **No Production Impact**: This feature only runs when `runtime/release-build?` is false

Example log output:
```
INFO [potatoclient.cmd.core] - Command protobuf structure
  {:type "command"
   :json "{
     \"protocolVersion\": 1,
     \"clientType\": \"JON_GUI_DATA_CLIENT_TYPE_WEB\",
     \"ping\": {}
   }"
   :size 8}
```

## Debug Utilities

The `potatoclient.cmd.debug` namespace provides additional tools for inspecting protobuf messages:

```clojure
(require '[potatoclient.cmd.debug :as debug])

;; Decode a Base64 payload to see its structure
(debug/decode-base64-command "CAEaAggBGgI=")

;; Inspect a specific command (shows both Base64 and JSON)
(debug/inspect-command 
  #(rotary/set-platform-azimuth 45.0) 
  "Set Azimuth Command")

;; Run all demos to see various command structures
(debug/run-all-demos)

;; Compare two commands side by side
(debug/compare-commands
  #(camera/zoom-in) "Zoom In"
  #(camera/zoom-out) "Zoom Out")
```

The debug utilities use Google's `JsonFormat` (from protobuf-java-util) to convert between protobuf binary format and human-readable JSON, making it easy to verify command construction and troubleshoot issues.

## Build System Integration

### Dynamic Classpath Configuration

The build system (`build.clj`) uses a dynamic basis function to handle compiled protobuf classes:
- Development builds can use a static basis
- CI builds need to include `target/classes` after protobuf compilation
- The `get-basis` function checks if `target/classes` exists and adds it to the classpath

### Important Build Sequence

1. Clean previous artifacts
2. Generate protobuf source files
3. Compile Java protobuf classes to `target/classes`
4. Compile Kotlin subprocesses (see [kotlin-subprocess.md](./kotlin-subprocess.md#build-integration))
5. Run Clojure compilation (which needs the protobuf classes)
6. Create JAR with all components

### Common Build Issues

- `ClassNotFoundException` for protobuf classes: Ensure `target/classes` is on classpath during Clojure compilation
- Package name mismatches: Check that preprocessing script is converting `ser` → `data` correctly
- Missing dependencies: Ensure `protobuf-java-util` is included for JsonFormat support
- Wrong class names: Remember `JonSharedDataTypes` not `JonGuiDataTypes`
- Case-sensitive builder methods: Java protobuf builders use camelCase (e.g., `setUseRotaryAsCompass` not `SetUseRotaryAsCompass`)

## Troubleshooting

### When encountering protobuf-related errors:

1. **Check the package name**:
   ```bash
   # Verify generated Java files have correct package
   grep -n "^package" target/classes/java/cmd/*.java
   grep -n "^package" target/classes/java/data/*.java
   ```

2. **Verify package names**:
   ```bash
   # Proto files and generated Java should have 'ser' package for data types
   grep "^package" proto/jon_shared_data*.proto
   grep "^package" src/potatoclient/java/ser/*.java
   ```

3. **Check class name references**:
   ```bash
   # Find references to old class names
   grep -r "JonGuiDataTypes" src/
   # Should use JonSharedDataTypes instead
   ```

4. **Ensure dependencies match**:
   ```clojure
   ;; In deps.edn, must have both:
   com.google.protobuf/protobuf-java {:mvn/version "4.29.5"}
   com.google.protobuf/protobuf-java-util {:mvn/version "4.29.5"}
   ```

### Update Protocol Workflow

1. Edit `.proto` files
2. Run `make proto`
3. Update `potatoclient.proto` accessors

### Add Event Type

1. Define in `potatoclient.events.stream`
2. Handle in Kotlin subprocess (see [kotlin-subprocess.md](./kotlin-subprocess.md#event-system-integration))
3. Add to `ipc/message-handlers` dispatch table

### Modify Pipeline

For GStreamer pipeline modifications, see [kotlin-subprocess.md](./kotlin-subprocess.md#pipeline-modification)