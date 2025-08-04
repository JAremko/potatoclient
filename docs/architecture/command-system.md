# Command System Architecture

The command system provides structured communication between the UI and server, with complete protobuf isolation in Kotlin subprocesses.

## Overview

Commands flow from user actions through multiple layers:

```
User Action → Clojure UI → Transit Command → Command Subprocess → Protobuf → Server
```

Key principles:
- **Protobuf Isolation**: Clojure never touches protobuf
- **Transit Protocol**: All IPC uses Transit/MessagePack
- **Static Generation**: Zero manual code for new commands
- **Keywords Everywhere**: All parameters use keywords

## Architecture

### Command Flow

1. **UI Layer** (Clojure)
   - User triggers action (button click, gesture, etc.)
   - Creates command map with nested keywords
   - Sends via Transit to Command subprocess

2. **Command Subprocess** (Kotlin)
   - Receives Transit command
   - Uses generated handlers to convert to protobuf
   - Sends protobuf via WebSocket to server

3. **State Subprocess** (Kotlin)
   - Receives protobuf state from server
   - Uses generated handlers to convert to Transit
   - Sends Transit state to main process

4. **Main Process** (Clojure)
   - Updates app-db with new state
   - UI reacts to state changes

### Command Structure

Commands use nested maps matching protobuf hierarchy:

```clojure
;; Simple command
{:ping {}}

;; Platform control
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}

;; System commands
{:system {:start-rec {}}}
{:system {:stop-rec {}}}

;; Camera control
{:heat-camera {:next-zoom-table-pos {}}}
{:day-camera {:set-focus-mode {:mode :auto}}}
```

## Static Code Generation

### Generated Handlers

The system uses static code generation to eliminate manual protobuf mapping:

```kotlin
// GeneratedCommandHandlers.kt
object GeneratedCommandHandlers {
    fun buildCommand(transitMap: Map<String, Any>): Any {
        // Auto-generated code maps Transit → Protobuf
        return when (val key = transitMap.keys.first()) {
            "ping" -> buildPing()
            "rotary" -> buildRotary(transitMap["rotary"])
            "system" -> buildSystem(transitMap["system"])
            // ... all commands
        }
    }
}
```

### Benefits
- **Zero Configuration**: New commands work automatically
- **Type Safety**: Compile-time validation
- **Performance**: No reflection or runtime overhead
- **Maintainability**: Regenerate when protos change

## Command Categories

### Core Commands
- `ping` - Keepalive/connection test
- `get-product-info` - System information

### Platform Control (Rotary)
- `goto-ndc` - Move to NDC position
- `set-velocity` - Continuous movement
- `halt` - Stop all movement
- `start/stop` - Platform power

### Camera Control
- **Heat Camera**: Zoom, focus, palette
- **Day Camera**: Zoom, focus modes
- **Recording**: Start/stop recording

### Computer Vision
- `start-track-ndc` - Start tracking at position
- `stop-track` - Stop tracking

### System Control
- `start-rec` / `stop-rec` - Recording
- `set-localization` - UI language
- `power-state` - System power

## Keyword Conversions

All enum-like values use keywords:

### Stream Types
- `HEAT` → `:heat`
- `DAY` → `:day`

### Languages
- `ENGLISH` → `:en`
- `UKRAINIAN` → `:uk`

### Focus Modes
- `AUTO` → `:auto`
- `MANUAL` → `:manual`
- `INFINITY` → `:infinity`

### Directions
- `CLOCKWISE` → `:clockwise`
- `COUNTER_CLOCKWISE` → `:counter-clockwise`

## Command Validation

### Malli Schemas

Commands are validated before sending:

```clojure
(def rotary-goto-ndc
  [:map
   [:channel [:enum :heat :day]]
   [:x [:and number? [:>= -1.0] [:<= 1.0]]]
   [:y [:and number? [:>= -1.0] [:<= 1.0]]]])

(def command-envelope
  [:map
   [:msg-type [:= :command]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])
```

### Validation Flow
1. UI creates command
2. Malli validates structure
3. Transit serializes
4. Kotlin validates again
5. Protobuf provides final validation

## Adding New Commands

### 1. Define in Protobuf
```protobuf
message NewCommand {
    string parameter = 1;
    int32 value = 2;
}
```

### 2. Regenerate Code
```bash
make proto                    # Generate Java classes
make generate-kotlin-handlers # Generate handlers
```

### 3. Use in Clojure
```clojure
(cmd/send-command! 
  {:subsystem {:new-command {:parameter :value
                            :value 42}}})
```

The command automatically works through the entire stack!

## Testing Commands

### Mock Command Subprocess
```clojure
(with-mock-command-subprocess
  (cmd/send-command! {:ping {}})
  (is (= 1 (count @sent-commands))))
```

### Integration Testing
```clojure
(deftest command-roundtrip
  (with-test-system
    (cmd/send-command! {:rotary {:halt {}}})
    (wait-for-state [:rotary :is-moving] false)))
```

## Performance Considerations

- **Pre-allocated Buffers**: Reuse Transit buffers
- **Keyword Interning**: Keywords compare by reference
- **Static Dispatch**: No reflection in hot paths
- **Batch Commands**: Group related commands

## Debugging

### Enable Debug Logging
```clojure
(log/set-level! :potatoclient.transit :debug)
```

### Inspect Transit Messages
```clojure
(add-watch cmd/command-chan :debug
  (fn [_ _ _ msg]
    (println "Command:" msg)))
```

### Monitor WebSocket
```kotlin
// In CommandSubprocess
LoggingUtils.logDebug("Sending: ${command.toJson()}")
```

## See Also

- [Transit Protocol](./transit-protocol.md) - Message format details
- [System Overview](./system-overview.md) - Overall architecture
- [Proto Explorer](../tools/proto-explorer.md) - Schema generation