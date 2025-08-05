# Proto-CLJ Generator

A custom code generator that creates Clojure functions for bidirectional protobuf conversion.

## Overview

This tool generates Clojure code that can:
- Convert Clojure maps to protobuf messages (builder functions)
- Convert protobuf messages to Clojure maps (parser functions)
- Handle all protobuf types including oneofs, enums, and nested messages
- Work with the existing protobuf-java 4.x without version conflicts

## Usage

### Using deps.edn alias

```bash
# From the proto-clj-generator directory
clojure -M:gen

# With custom paths
clojure -M:gen -Xinput-dir ../custom/descriptors -Xoutput-dir ./out -Xnamespace-prefix my.proto
```

### Command line

```bash
clojure -M -m generator.main <input-dir> <output-dir> <namespace-prefix>

# Example
clojure -M -m generator.main ../../tools/proto-explorer/output/json-descriptors generated potatoclient.proto
```

### From REPL

```clojure
(require '[generator.main :as main])

(main/generate
  {:input-dir "../../tools/proto-explorer/output/json-descriptors"
   :output-dir "generated"
   :namespace-prefix "potatoclient.proto"})
```

## Generated Code Structure

The generator creates two namespaces:

### `<namespace-prefix>.command`

Contains converters for command messages:
- `build-root` - Convert Clojure map to Root protobuf
- `parse-root` - Convert Root protobuf to Clojure map
- Individual builders/parsers for each command type

### `<namespace-prefix>.state`

Contains converters for state messages:
- `build-jon-gui-state` - Convert Clojure map to JonGUIState protobuf
- `parse-jon-gui-state` - Convert JonGUIState protobuf to Clojure map
- Individual builders/parsers for each state component

## Example Usage

```clojure
(require '[potatoclient.proto.command :as cmd])

;; Build a command
(def command-bytes
  (-> {:rotary {:goto-ndc {:x 0.5 :y -0.5 :channel :heat}}}
      cmd/build-root
      .toByteArray))

;; Parse a command
(def parsed-command
  (-> command-bytes
      (Root/parseFrom)
      cmd/parse-root))
```

## Features

- **Bidirectional conversion** - Both Clojure→Protobuf and Protobuf→Clojure
- **Oneof support** - Uses custom `:oneof` Malli schema for validation
- **Enum handling** - Automatic keyword↔enum conversion
- **Nested messages** - Recursive builder/parser generation
- **Repeated fields** - Handles lists/vectors properly
- **Forward declarations** - Handles circular dependencies

## Dependencies

- JSON descriptors from proto-explorer (must run proto-explorer first)
- Custom oneof schema from shared/specs
- Protobuf Java classes (must compile protos first)

## Development

Run tests:
```bash
clojure -M:test
```

Check generated code:
```bash
# Generate code
clojure -M:gen

# Inspect generated files
cat generated/potatoclient/proto/command.clj
cat generated/potatoclient/proto/state.clj
```

## Integration with WebSocket Refactoring

This tool is part of the WebSocket refactoring effort to replace Kotlin subprocesses with pure Clojure implementations. The generated code will be used by the new Hato WebSocket clients to convert between Clojure data structures and protobuf messages.