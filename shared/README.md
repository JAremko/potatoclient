# Shared Module

The foundational library for the PotatoClient system, providing protocol definitions, validation, and serialization utilities that ensure type safety and protocol consistency across all subsystems.

## Overview

The shared module serves as the **single source of truth** for:
- **Protocol Buffer definitions** for all system communication
- **Malli specifications** that mirror protobuf constraints with runtime validation
- **Command construction** with automatic field population and validation
- **Serialization/deserialization** utilities for EDN ↔ Protobuf binary conversion

## Architecture

```
shared/
├── src/potatoclient/
│   ├── cmd/                    # Command construction and sending
│   │   ├── core.clj            # Queue management and command dispatch
│   │   ├── builder.clj         # Efficient proto-map building
│   │   ├── validation.clj      # Roundtrip validation with deep-diff
│   │   ├── root.clj            # Root-level commands (ping, noop, frozen)
│   │   └── *.clj               # Command implementations by subsystem
│   ├── proto/                  # Protobuf serialization
│   │   ├── serialize.clj       # EDN → Binary with validation
│   │   └── deserialize.clj     # Binary → EDN with validation
│   ├── specs/                  # Malli specifications
│   │   ├── common.clj          # Reusable specs (angles, coordinates, etc.)
│   │   ├── cmd/                # Command message specs
│   │   └── state/              # State message specs
│   └── malli/                  # Malli infrastructure
│       ├── registry.clj        # Global spec registry
│       └── oneof.clj           # Custom oneof schema for proto oneofs
├── target/classes/             # Compiled Java protobuf classes
└── test/                       # Comprehensive test suite

```

## Key Features

### 🚀 Command System

The command system provides type-safe, validated command construction with automatic protocol field population:

```clojure
(require '[potatoclient.cmd.root :as root]
         '[potatoclient.cmd.system :as sys]
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
;; => {:protocol_version 1, ...
;;     :system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA}}}

;; All command functions return full cmd roots
;; To actually send them, use send-command!
(let [cmd (root/ping)]
  (cmd-core/send-command! cmd))  ; Returns nil after enqueueing
```

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

### ✅ Validation

Multi-layer validation ensures protocol compliance:

1. **Malli specs** - Structure and constraint validation
2. **buf.validate** - Protocol buffer constraint validation  
3. **Roundtrip testing** - Automatic in test mode

```clojure
(require '[potatoclient.cmd.validation :as v])

;; Validate any command
(let [cmd (sys/reboot)
      result (v/validate-roundtrip-with-report cmd)]
  (if (:valid? result)
    (println "Command valid!")
    (println (:pretty-diff result))))  ; Deep-diff output
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

## Usage

### Prerequisites

1. **Compile proto classes** (required once or when .proto files change):
```bash
cd shared
make proto
```

2. **Run tests** to verify everything works:
```bash
clojure -M:test
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
         '[potatoclient.proto.serialize :as serialize])

;; Use commands - they return full cmd roots
(let [cmd (cmd-root/ping)
      binary (serialize/serialize-cmd-payload cmd)]
  (websocket/send! binary))

;; Or use the queue system
(let [cmd (cmd-root/ping)]
  (cmd-core/send-command! cmd))
```

## Testing

The module includes comprehensive testing:

- **Unit tests** for each command function
- **Roundtrip validation** for all commands
- **Generative testing** with 500+ samples per function
- **Negative tests** to ensure error handling
- **Integration tests** for end-to-end flow

### Automatic Generative Testing with mi/check

You can use `malli.instrument/check` for automatic generative testing:

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

### Running Tests

Run all tests:
```bash
clojure -M:test
```

Run specific test namespace:
```bash
clojure -M:test -n potatoclient.cmd.integration-test
```


## Protocol Consistency

All data structures are defined in `.proto` files and compiled to:
1. **Java classes** for JVM interop
2. **Malli specs** for Clojure validation
3. **EDN schemas** for runtime checking

This ensures:
- **Type safety** across language boundaries
- **Protocol compliance** with buf.validate constraints
- **Runtime validation** with helpful error messages

## Performance

The command system is optimized for performance:
- **Pronto hints** for direct Java method dispatch
- **Single proto-map creation** with all fields at once
- **Efficient queue operations** with Java concurrent collections
- **Minimal allocations** through careful data flow

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

### Important Discoveries

- **Numeric values in tests**: Always use doubles (e.g., `15.5`, not `15`) when testing functions that expect `:double`
- **Oneof fields with nils**: Valid in our schemas, handled by `remove-nil-values` in validation
- **Custom schemas**: Define in `specs/common.clj` and register them (e.g., `:nat-int`)
- **Malli metadata**: Functions have `:malli/schema` metadata for type information
- **Generative testing with mi/check**: Works automatically with Malli-annotated functions!

## License

[Your License Here]