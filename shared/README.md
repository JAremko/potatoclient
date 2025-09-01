# Shared Module

The foundational library for the PotatoClient system, providing protocol definitions, validation, serialization utilities, and inter-process communication (IPC) infrastructure that ensures type safety and protocol consistency across all subsystems.

## Overview

The shared module serves as the **single source of truth** for:
- **Protocol Buffer definitions** for all system communication (commands and state)
- **Malli specifications** that mirror protobuf constraints with runtime validation
- **Command construction** with automatic field population and validation
- **Serialization/deserialization** utilities for EDN ↔ Protobuf binary conversion
- **Inter-Process Communication (IPC)** using Unix Domain Sockets with Transit serialization
- **Comprehensive validation** at multiple layers (Malli, buf.validate, roundtrip testing)

## Architecture

```
shared/
├── src/
│   ├── java/                          # Compiled Java protobuf classes
│   │   ├── cmd/                       # Command proto classes
│   │   │   ├── JonSharedCmd.java     # Root command with oneof payload
│   │   │   ├── Compass/              # Compass subsystem commands
│   │   │   ├── DayCamera/            # Day camera commands
│   │   │   ├── HeatCamera/           # Heat camera commands
│   │   │   ├── GPS/                  # GPS commands
│   │   │   ├── Lrf/                  # Laser range finder commands
│   │   │   ├── RotaryPlatform/       # Platform control commands
│   │   │   └── System/               # System-wide commands
│   │   ├── ser/                       # State proto classes
│   │   │   ├── JonSharedData.java    # Root state (JonGUIState)
│   │   │   └── JonSharedData*.java   # Subsystem state classes
│   │   └── potatoclient/java/ipc/    # IPC infrastructure
│   │       ├── UnixSocketCommunicator.java  # Unix socket implementation
│   │       └── SocketFactory.java           # Socket lifecycle management
│   └── potatoclient/
│       ├── cmd/                       # Command construction and sending
│       │   ├── core.clj              # Queue management and dispatch
│       │   ├── builder.clj           # Efficient proto-map building
│       │   ├── validation.clj        # Roundtrip validation with deep-diff
│       │   ├── root.clj              # Root commands (ping, noop, frozen)
│       │   └── *.clj                 # Subsystem command implementations
│       ├── proto/                     # Protobuf serialization
│       │   ├── serialize.clj         # EDN → Binary with validation
│       │   └── deserialize.clj       # Binary → EDN with validation
│       ├── specs/                     # Malli specifications
│       │   ├── common.clj            # Reusable specs (angles, coordinates)
│       │   ├── cmd/                  # Command message specs
│       │   └── state/                # State message specs
│       ├── malli/                     # Malli infrastructure
│       │   ├── registry.clj          # Global spec registry
│       │   └── oneof.clj             # Custom oneof schema for proto oneofs
│       └── ipc/                       # Inter-Process Communication
│           ├── core.clj              # IPC server with Unix sockets
│           └── transit.clj           # Transit msgpack serialization
├── target/classes/                    # Compiled Java protobuf classes
├── test/                              # Comprehensive test suite
│   ├── potatoclient/                 # Unit and integration tests
│   └── test_suites/                  # Organized test suites
│       ├── cmd_suite.clj             # Command tests
│       ├── ipc_suite.clj             # IPC tests
│       ├── malli_suite.clj           # Malli spec tests
│       ├── oneof_suite.clj           # Oneof validation tests
│       └── serialization_suite.clj   # Serialization tests
└── Makefile                           # Build and test automation
```

## Key Features

### 🚀 Command System

The command system provides type-safe, validated command construction with automatic protocol field population:

```clojure
(require '[potatoclient.cmd.root :as root]
         '[potatoclient.cmd.system :as sys]
         '[potatoclient.cmd.compass :as compass]
         '[potatoclient.cmd.core :as cmd-core])

;; Simple commands - all fields automatically populated
(root/ping)
;; => {:protocol_version 1, 
;;     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK,
;;     :session_id 0, 
;;     :important false, 
;;     :from_cv_subsystem false,
;;     :ping {}}

;; Commands with parameters
(sys/set-localization :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA)
(compass/calibrate-start)
(compass/set-magnetic-declination 15.5)

;; All command functions return full cmd roots
;; To actually send them, use send-command!
(let [cmd (root/ping)]
  (cmd-core/send-command! cmd))  ; Returns nil after enqueueing
```

**Available Command Subsystems:**
- **Root** - System-level commands (ping, noop, frozen states)
- **System** - Power, reboot, localization, recording, transport mode
- **Compass** - Calibration, magnetic declination, operational modes
- **GPS** - Position control, meteorological data, fix management
- **Day/Heat Camera** - Focus, zoom, iris, filters, digital effects
- **LRF** - Laser measurements, scanning, target designation
- **Rotary Platform** - Azimuth/elevation control, scanning patterns
- **OSD** - On-screen display management
- **CV** - Computer vision subsystem control

### 📨 Command Queue

Commands are queued using a non-blocking `LinkedBlockingQueue`:
- **Producer side**: Never blocks, queue grows as needed
- **Consumer side**: Blocks for 1 second, then sends ping to maintain connection
- **Test mode**: Automatic roundtrip validation without queuing

```clojure
;; Commands must be explicitly sent to queue
(let [cmd (root/ping)]
  (cmd-core/send-command! cmd))  ; Adds to queue

;; Consumer thread (WebSocket sender)
(cmd-core/consume-commands 
  (fn [binary-data] 
    (websocket/send! binary-data)))
```

### 🔌 Inter-Process Communication (IPC)

High-performance IPC using Unix Domain Sockets with Transit serialization:

```clojure
(require '[potatoclient.ipc.core :as ipc]
         '[potatoclient.ipc.transit :as transit])

;; Create IPC server for a stream
(def server (ipc/create-server :heat
              :on-message (fn [msg]
                           (println "Received:" msg))))

;; Send messages
(ipc/send-message server {:type :event
                          :data {:temperature 32.5}})

;; Receive messages (blocking)
(let [msg (ipc/receive-message server :timeout-ms 1000)]
  (process-message msg))

;; Stop server
(ipc/stop-server server)
```

**IPC Features:**
- **Unix Domain Sockets** - Fast, reliable local communication
- **Transit msgpack** - Efficient binary serialization
- **Framed messages** - Length-prefixed packets for reliable delivery
- **Async processing** - Separate reader/processor threads
- **Message queue** - Buffered message handling
- **Server pool** - Manage multiple stream servers

**Transit Keywords Architecture:**
- **Kotlin/Java side**: Use `com.cognitect.transit.Keyword` via `TransitFactory.keyword("name")`
- **Clojure side**: Automatically converts Transit Keywords to Clojure keywords
- **Never use strings** for message types - always use Transit Keywords for type safety

### ✅ Multi-Layer Validation

The module provides comprehensive validation at multiple levels:

1. **Malli specs** - Structure and constraint validation
2. **buf.validate** - Protocol buffer constraint validation at binary level
3. **Roundtrip testing** - Automatic EDN→Binary→EDN verification

```clojure
(require '[potatoclient.cmd.validation :as v])

;; Validate any command
(let [cmd (sys/reboot)
      result (v/validate-roundtrip-with-report cmd)]
  (if (:valid? result)
    (println "Command valid!")
    (println (:pretty-diff result))))  ; Deep-diff output
```

### 🔄 Serialization/Deserialization

Efficient bidirectional conversion between EDN and Protobuf binary:

```clojure
(require '[potatoclient.proto.serialize :as serialize]
         '[potatoclient.proto.deserialize :as deserialize])

;; Fast serialization (no validation)
(let [cmd (root/ping)
      binary (serialize/serialize-cmd-payload* cmd)]
  (send-over-network binary))

;; Full serialization with validation
(let [cmd (root/ping)
      binary (serialize/serialize-cmd-payload cmd)]  ; Validates with Malli + buf.validate
  (send-over-network binary))

;; Deserialization
(let [binary (receive-from-network)
      cmd (deserialize/deserialize-cmd-payload binary)]  ; Full validation
  (process-command cmd))
```

### 🏗️ Efficient Proto Building

Following Pronto performance guidelines for optimal proto-map creation:

```clojure
(require '[potatoclient.cmd.builder :as builder])

;; Populate missing fields efficiently
(builder/populate-cmd-fields {:ping {}})
;; => {:protocol_version 1, :client_type ..., :session_id 0, :ping {}}

;; Custom overrides
(builder/create-full-cmd 
  {:system {:reboot {}}}
  {:session_id 12345})  ; Custom session ID
```

### 📋 Malli Schema Registry

Centralized registry for all data specifications:

```clojure
(require '[potatoclient.malli.registry :as registry])

;; Register custom specs
(registry/register-spec! :my-app/temperature
  [:double {:min -273.15 :max 1000}])

;; Setup global registry with custom schemas
(registry/setup-global-registry! my-custom-schemas)

;; Access registered specs
(m/schema :cmd/root)  ; Get command root schema
```

## Protocol Buffer Architecture

The system uses a comprehensive protobuf architecture for hardware control:

### Command Messages (`cmd` package)
- **Root**: `JonSharedCmd$Root` - Main command envelope with oneof payload
- **Optical Systems**: Day/heat cameras, glass heater control
- **Navigation**: GPS, compass, rotary platform positioning
- **Sensors**: Laser range finder, alignment systems
- **System**: Power, localization, recording, operational modes

### State Messages (`ser` package)
- **Root**: `JonGUIState` - Complete system state aggregation
- **Hardware State**: Camera settings, GPS position, compass data
- **System State**: CPU/GPU temps, disk space, recording status
- **Specialized State**: OSD overlays, actual space-time positioning

### Key Protocol Features
- **Comprehensive Validation**: buf.validate constraints on all fields
- **Type Safety**: Enums for all operational modes and states
- **Physical Constraints**: GPS coordinates, angles, temperatures enforced at protocol level
- **Client Types**: Support for internal CV, network, certificate-protected, LIRA clients

## Usage

### Prerequisites

1. **Compile proto classes** (required once or when .proto files change):
```bash
cd shared
make proto
```

2. **Run tests** to verify everything works:
```bash
make test
```

### Test Suites

The module includes organized test suites for focused testing:

```bash
make test              # Run all tests
make test-cmd         # Command construction/validation tests  
make test-ipc         # IPC communication tests
make test-malli       # Malli spec validation tests
make test-oneof       # Oneof custom schema tests
make test-serialization  # Serialization/deserialization tests
```

### Adding New Commands

1. Create the command function in the appropriate namespace:
```clojure
(ns potatoclient.cmd.my-subsystem
  (:require [potatoclient.cmd.core :as core]))

(defn my-command
  "Description of what this command does.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat :string] :cmd/root]}
  [param]
  (core/create-command 
    {:my_subsystem {:my_command {:field param}}}))
```

2. Add specs in `specs/cmd/my_subsystem.clj` if needed

3. Write tests with roundtrip validation

### Integration with Other Modules

```clojure
;; In your module's deps.edn
{:deps {potatoclient/shared {:local/root "../shared"}}}

;; In your code
(require '[potatoclient.cmd.root :as cmd-root]
         '[potatoclient.cmd.core :as cmd-core]
         '[potatoclient.proto.serialize :as serialize]
         '[potatoclient.ipc.core :as ipc])

;; Use commands
(let [cmd (cmd-root/ping)
      binary (serialize/serialize-cmd-payload cmd)]
  (websocket/send! binary))

;; Use IPC for local communication
(def ipc-server (ipc/create-server :heat
                  :on-message handle-ipc-message))
```

## Testing

The module includes comprehensive testing with 700+ test cases:

- **Unit tests** for each command function
- **Roundtrip validation** for all commands
- **Generative testing** with 500+ samples per function using `malli.instrument/check`
- **Negative tests** to ensure error handling
- **Integration tests** for end-to-end flow
- **IPC tests** for communication infrastructure

### Automatic Generative Testing with mi/check

```clojure
(require '[malli.instrument :as mi])

;; Collect schemas from functions with :malli/schema metadata
(mi/collect! {:ns ['potatoclient.cmd.compass]})

;; Run generative testing on all collected functions
(mi/check)  ; Returns nil if all pass, or a map of failures
```

This automatically:
- Generates test inputs based on function schemas
- Validates outputs against return schemas
- Finds edge cases and constraint violations
- Reports smallest failing inputs through shrinking

## Build Tools

The Makefile provides convenient build automation:

```bash
make help              # Show all available commands
make proto            # Regenerate and compile proto classes
make compile          # Compile existing proto sources
make test             # Run all tests
make clean            # Clean build artifacts (preserves proto)
make clean-all        # Clean everything including proto
make kondo-configs    # Generate clj-kondo type configs from Malli specs
make deps-outdated    # Check for outdated dependencies
make deps-upgrade     # Interactively upgrade dependencies
```

## Performance

The command system is optimized for performance:
- **Pronto hints** for direct Java method dispatch
- **Single proto-map creation** with all fields at once
- **Efficient queue operations** with Java concurrent collections
- **Minimal allocations** through careful data flow
- **Direct ByteBuffer usage** in IPC for zero-copy operations
- **Framed messaging** for efficient packet handling

## Development Guidelines

### Mandatory Requirements

1. **Use Malli schemas ONLY** - Clojure Spec is forbidden
   - Use Malli schemas: `:int`, `:double`, `:boolean`, `:map`, etc.
   - NOT predicates: `int?`, `double?`, `boolean?` (these are spec-style)

2. **All functions must have :malli/schema metadata** for type checking
   ```clojure
   (defn my-function
     {:malli/schema [:=> [:cat :double] :cmd/root]}
     [arg]
     ...)
   ```

3. **All proto maps must use closed specs** to catch typos
   ```clojure
   [:map {:closed true} ...]
   ```

4. **All commands must pass roundtrip validation**
   - Protobuf deserialization adds `nil` for all oneof fields - this is valid
   - Our validation removes nils for comparison in tests only

5. **Never modify tests that fail** - fix the code instead

6. **Use Transit Keywords for IPC** - never use strings for message types

### Important Discoveries

- **Numeric values in tests**: Always use doubles (e.g., `15.5`, not `15`) when testing functions that expect `:double`
- **Oneof fields with nils**: Valid in our schemas, handled by `remove-nil-values` in validation
- **Custom schemas**: Define in `specs/common.clj` and register them (e.g., `:nat-int`)
- **Malli metadata**: Functions have `:malli/schema` metadata for type information
- **Generative testing with mi/check**: Works automatically with Malli-annotated functions!
- **IPC message flow**: Transit automatically converts between Clojure and Transit keywords

## Protocol Consistency

All data structures are defined in `.proto` files and compiled to:
1. **Java classes** for JVM interop
2. **Malli specs** for Clojure validation
3. **EDN schemas** for runtime checking

This ensures:
- **Type safety** across language boundaries
- **Protocol compliance** with buf.validate constraints
- **Runtime validation** with helpful error messages
- **Seamless IPC** between Clojure and Kotlin/Java processes

## Dependencies

Key dependencies include:
- **Pronto** - Clojure wrapper for Protocol Buffers (custom fork)
- **Malli** - Data and function schema validation
- **buf.validate** - Protocol buffer constraint validation
- **Transit** - Efficient msgpack serialization for IPC
- **Telemere** - Structured logging for IPC events
- **test.check** - Generative testing support

## License

[Your License Here]