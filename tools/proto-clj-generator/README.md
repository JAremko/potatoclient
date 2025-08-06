# Proto-CLJ Generator

A custom code generator that creates Clojure functions for bidirectional protobuf conversion with runtime validation using Guardrails and Malli specs.

## Overview

This tool generates Clojure code that can:
- Convert Clojure maps to protobuf messages (builder functions)
- Convert protobuf messages to Clojure maps (parser functions)
- Handle all protobuf types including oneofs, enums, and nested messages
- Generate runtime validation using Guardrails (`>defn`/`>defn-`)
- Generate Malli specs alongside protobuf bindings
- Support namespace separation (each protobuf package gets its own namespace)
- Generate enum↔keyword conversion maps
- Handle cross-namespace type references

## Usage

### Using Makefile (Recommended)

```bash
# Generate all protobuf bindings with guardrails
make generate

# Run tests
make test

# Check guardrails coverage
make guardrails-check

# Run REPL for development
make nrepl
```

### Using deps.edn alias

```bash
# From the proto-clj-generator directory
clojure -X:gen

# With custom paths
clojure -X:gen :input-dir '"../custom/descriptors"' :output-dir '"./out"' :namespace-prefix '"my.proto"'

# Without guardrails (for production builds)
clojure -X:gen :guardrails? false
```

### Command line

```bash
clojure -M -m generator.main <input-dir> <output-dir> <namespace-prefix> [guardrails?]

# Example with guardrails
clojure -M -m generator.main ../../tools/proto-explorer/output/json-descriptors /home/jare/git/potatoclient/shared potatoclient.proto true

# Example without guardrails
clojure -M -m generator.main ../../tools/proto-explorer/output/json-descriptors generated potatoclient.proto false
```

### From REPL

```clojure
(require '[generator.main :as main])

;; With guardrails (default)
(main/generate
  {:input-dir "../../tools/proto-explorer/output/json-descriptors"
   :output-dir "/home/jare/git/potatoclient/shared"
   :namespace-prefix "potatoclient.proto"
   :guardrails? true})
```

## Generated Code Structure

The generator uses namespace separation - each protobuf package generates to its own namespace:

### Root Namespaces
- `<namespace-prefix>.cmd` - Root command namespace with re-exports
- `<namespace-prefix>.ser` - Root state/serialization namespace with re-exports

### Package Namespaces
Each protobuf package generates to:
- `<namespace-prefix>.<package>.<subpackage>` - Contains all types from that package

Example structure:
```
potatoclient/proto/
├── cmd.clj                    # Root command namespace
├── cmd/
│   ├── daycamera.clj         # cmd.DayCamera messages
│   ├── heatcamera.clj        # cmd.HeatCamera messages
│   ├── rotaryplatform.clj    # cmd.RotaryPlatform messages
│   └── ...
└── ser.clj                    # Root state namespace
```

## Generated Code Features

### 1. Guardrails Runtime Validation

All generated functions use `>defn` with runtime specs:

```clojure
(>defn build-root
  "Build a Root protobuf message from a map."
  [m]
  [root-spec => any?]  ; Runtime validation
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; ... builder code
    (.build builder)))
```

### 2. Malli Specs

Specs are generated in the same file as bindings:

```clojure
(def root-spec
  "Malli spec for root message"
  [:map
   [:protocol-version [:maybe :int]]
   [:session-id [:maybe :int]]
   [:payload [:altn {:day-camera [:map [:day-camera :cmd.day-camera/root]]
                     :heat-camera [:map [:heat-camera :cmd.heat-camera/root]]
                     ;; ... other variants
                     }]]])
```

### 3. Enum Conversion Maps

Generated in the same file as enum usage:

```clojure
;; Enum to keyword map
(def jon-gui-data-client-type-keywords
  {ser.JonGUIDataClientType/UNKNOWN :unknown
   ser.JonGUIDataClientType/SMARTPHONE :smartphone
   ser.JonGUIDataClientType/SMARTWATCH :smartwatch})

;; Keyword to enum map
(def jon-gui-data-client-type-values
  {:unknown ser.JonGUIDataClientType/UNKNOWN
   :smartphone ser.JonGUIDataClientType/SMARTPHONE
   :smartwatch ser.JonGUIDataClientType/SMARTWATCH})
```

### 4. Cross-Namespace References

Oneof fields correctly reference builders in other namespaces:

```clojure
(>defn- build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :day-camera (.setDayCamera builder (daycamera/build-root value))
    :heat-camera (.setHeatCamera builder (heatcamera/build-root value))
    ;; ... other cases
    ))
```

## Example Usage

```clojure
(require '[potatoclient.proto.cmd :as cmd])

;; Build a command with validation
(def command-bytes
  (-> {:protocol-version 1
       :session-id 42
       :payload {:rotary {:goto-ndc {:x 0.5 :y -0.5 :channel :heat}}}}
      cmd/build-root  ; Runtime validation happens here
      .toByteArray))

;; Parse a command
(def parsed-command
  (-> command-bytes
      (cmd.JonSharedCmd$Root/parseFrom)
      cmd/parse-root))  ; Returns validated Clojure map
```

## Guardrails Integration

The generated code integrates with the project's guardrails setup:

1. **Runtime Validation**: All generated functions validate inputs/outputs
2. **clj-kondo Support**: `.clj-kondo/config.edn` is configured for guardrails
3. **Coverage Checking**: Use `make guardrails-check` to verify all functions are spec'd
4. **Development vs Production**: Can disable guardrails for production builds

### Checking Guardrails Coverage

```bash
# Check which functions lack specs
make guardrails-check

# Or directly
clojure -X:guardrails-check :namespaces '[potatoclient.proto.cmd]'
```

## Custom Malli Specs

You can add custom specs that compose with generated specs:

```clojure
;; In shared/specs/custom/my_specs.clj
(ns potatoclient.specs.custom.my-specs
  (:require [potatoclient.proto.cmd :as cmd]))

;; Extend generated spec with additional constraints
(def strict-command-spec
  [:and
   cmd/root-spec
   [:fn {:error/message "Protocol version must be > 0"}
    #(pos? (:protocol-version %))]])
```

## Dependencies

- JSON descriptors from proto-explorer (must run proto-explorer first)
- Custom oneof schema from shared/specs
- Protobuf Java classes (must compile protos first)
- Guardrails for runtime validation
- Malli for spec definitions

## Development

### Run tests:
```bash
# All tests
make test

# Specific test namespace
clojure -X:test :nses '[generator.simple-roundtrip-test]'
```

### Check generated code:
```bash
# Generate code
make generate

# Inspect generated files
cat /home/jare/git/potatoclient/shared/potatoclient/proto/cmd.clj
cat /home/jare/git/potatoclient/shared/potatoclient/proto/cmd/rotaryplatform.clj
```

### Debug generation:
```bash
# Check intermediate EDN representation
cat /home/jare/git/potatoclient/shared/debug/command-edn.edn
cat /home/jare/git/potatoclient/shared/debug/type-lookup.edn
```

## Performance Considerations

1. **Guardrails Overhead**: Runtime validation adds overhead. For production:
   ```bash
   # Generate without guardrails
   clojure -X:gen :guardrails? false
   ```

2. **Namespace Loading**: With namespace separation, only load what you need:
   ```clojure
   ;; Load only specific command namespace
   (require '[potatoclient.proto.cmd.rotaryplatform :as rotary])
   ```

## Integration with WebSocket Refactoring

This tool is part of the WebSocket refactoring effort to replace Kotlin subprocesses with pure Clojure implementations. The generated code provides:

1. **Type Safety**: Runtime validation ensures messages conform to protobuf schema
2. **Better Errors**: Malli specs provide detailed validation errors
3. **REPL-Friendly**: All generated code is pure Clojure, easy to test at REPL
4. **Performance**: No reflection, direct Java interop with type hints

## Troubleshooting

### "Cannot find proto class" errors
Ensure protobuf Java classes are compiled:
```bash
cd ../.. && make proto
```

### Validation failures
Check the Malli spec error - it shows exactly which field failed:
```clojure
;; Example error
{:type :malli.core/invalid-input
 :data {:protocol-version -1}  ; negative not allowed
 :schema [:map [:protocol-version [:maybe :int]]]}