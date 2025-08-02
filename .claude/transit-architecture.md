# Transit Architecture Documentation

## Overview

PotatoClient uses a Transit-based architecture to completely isolate protobuf handling from the Clojure codebase. This design provides clean separation of concerns, better testability, and eliminates protobuf dependencies in the main process.

**For the complete message protocol specification with emphasis on our keyword-based data model, see [transit-protocol.md](./transit-protocol.md).**

## Architecture Principles

1. **Complete Protobuf Isolation**: All protobuf code is confined to Kotlin subprocesses
2. **Transit for IPC**: All inter-process communication uses Transit with MessagePack format
3. **Single App State**: Following re-frame pattern with a single app-db atom
4. **Subprocess Management**: Clean lifecycle management for all subprocesses
5. **Keyword-Based Maps**: All Transit messages use keyword keys for consistency and performance
6. **Clean Architecture**: Single approach using Transit handlers - no manual serialization

## Components

### Clojure Components

#### `potatoclient.transit.core`
Core Transit functionality:
- `make-reader` / `make-writer` - Create Transit readers/writers
- `read-message` / `write-message!` - Read/write Transit messages
- `create-message` - Create message envelopes with metadata
- `validate-message-envelope` - Validate message structure

#### `potatoclient.transit.app-db`
Single source of truth for application state:
- Centralized atom following re-frame pattern
- Typed accessors with Guardrails validation
- State update functions for all subsystems
- Watch support for reactive updates

#### `potatoclient.transit.commands`
Command API that creates Transit messages:
- All commands return plain maps (no side effects)
- Commands include: ping, set-localization, rotary-goto, camera controls, etc.
- Guardrails validation on all parameters

#### `potatoclient.transit.subprocess-launcher`
Process lifecycle management:
- `start-command-subprocess` - Launch command processor
- `start-state-subprocess` - Launch state processor
- `send-message!` - Send Transit messages to subprocesses
- `stop-subprocess!` - Graceful shutdown with cleanup

### Kotlin Components

#### `CommandSubprocess`
Main class for command processing:
- Receives Transit commands via stdin (with keyword keys)
- Uses Transit keys for message access (e.g., `msg[TransitKeys.MSG_TYPE]`)
- Converts to protobuf using `SimpleCommandHandlers` with fallback to `SimpleCommandBuilder`
- Sends protobuf commands via WebSocket to server
- Handles reconnection and error recovery
- Sends WebSocket errors via Transit protocol (not stderr)

#### `StateSubprocess`
Main class for state updates:
- Receives protobuf state from WebSocket
- Uses Transit handlers for automatic serialization via `SimpleProtobufHandlers`
- Sends protobuf objects directly - handlers do the conversion
- Token bucket rate limiting (configurable via control messages)
- Sends Transit messages to main process via stdout
- All state data automatically converted to keywords

#### `TransitCommunicator`
Handles Transit communication over stdin/stdout:
- Message framing with length prefix
- Thread-safe read/write operations
- Preserves Transit keywords in both directions
- Configurable to use custom handlers

#### `TransitKeys`
Pre-created Transit keyword constants:
- Avoids repeated keyword instantiation
- Central location for all Transit keys
- Performance optimized with static instances

#### `TransitExtensions`
Kotlin extension properties for Transit maps:
- Clean property-style access to keyword-based maps
- Type-safe getters with nullable returns
- Helper functions for nested access and type checking
- Proper type handling for Transit writer
- Error handling and logging

#### `SimpleProtobufHandlers`
Transit handlers for automatic protobuf serialization:
- WriteHandlers for all protobuf message types:
  - Main state (`JonGUIState`) tagged as "jon-gui-state"
  - System data tagged as "system-data"
  - Rotary, GPS, compass, LRF, camera data with appropriate tags
- Automatic enum to Transit keyword conversion
- All fields included (no "has" checks - protobuf v3 style)
- Clean separation between data and serialization
- No manual conversion needed in StateSubprocess

#### `SimpleCommandHandlers`
Simple command builder from Transit data:
- Converts Transit command messages to protobuf commands
- Uses Transit keywords for parameter access
- Builds `JonSharedCmd.Root` from action and params
- Supports all command types: rotary, system, GPS, compass, CV, cameras, LRF, glass heater
- Type-safe construction with proper error handling
- Cleaner alternative to `SimpleCommandBuilder`
- Note: OSD commands removed (not found in protobuf)

## Keyword Creation Best Practices

### Creating Keywords in Java/Kotlin
Always use the Transit public API for creating keywords:
```java
// CORRECT - Using Transit public API
import com.cognitect.transit.TransitFactory;
Keyword myKeyword = TransitFactory.keyword("my-key");

// WRONG - Using internal implementation
import com.cognitect.transit.impl.KeywordImpl;
Keyword myKeyword = new KeywordImpl("my-key");  // Don't do this!
```

### Pre-created Keywords
For performance, create keywords once and reuse them:
- Java enums (`MessageType`, `EventType`) create keywords in constructors
- Kotlin `TransitKeys` object holds pre-created keyword constants
- This avoids repeated keyword instantiation during message processing

### Automatic Conversion
Transit automatically converts between string keys and keywords:
- Kotlin sends maps with string keys
- Clojure receives maps with keyword keys
- No manual conversion needed

## Message Flow

### Command Flow (UI → Server)
```
1. User action in UI
2. Clojure calls command function (e.g., `commands/rotary-goto`)
3. Command returns Transit map: `{:action "rotary-goto" :params {:channel "heat" :x 0.5 :y -0.3}}`
4. Subprocess launcher sends map to CommandSubprocess via Transit
5. CommandSubprocess tries SimpleCommandHandlers first, falls back to SimpleCommandBuilder
6. Protobuf sent via WebSocket to server
```

### State Flow (Server → UI)
```
1. Server sends protobuf state via WebSocket
2. StateSubprocess receives binary protobuf data
3. Parses to `JonGUIState` protobuf object
4. Sends protobuf object directly via Transit (handlers serialize automatically)
5. Transit WriteHandlers convert all enums to keywords
6. Main process receives Transit message with keyword-based state
7. State updates app-db atom
```
2. StateSubprocess receives and parses protobuf
3. Debouncing: Compare with last sent state, skip if identical
4. Rate limiting: Check token bucket, skip if rate exceeded
5. Transit automatically serializes protobuf using registered handlers
6. Send Transit message to main process via stdout
7. Main process updates app-db with new state
8. UI components react to state changes
```

## Keyword Usage Examples

### Clojure Side
```clojure
;; Creating messages with keyword keys
(transit-core/create-message :command
  {:action "rotary-goto-ndc"
   :channel :heat
   :x 0.5
   :y -0.5})
;; Result: {:msg-type :command
;;          :msg-id "uuid..."
;;          :timestamp 1234567890
;;          :payload {:action "rotary-goto-ndc" ...}}

;; Handling messages - keywords arrive naturally
(defmethod handle-message :state
  [_ _ msg]
  (let [battery-level (get-in msg [:payload :system :battery-level])]
    (when (< battery-level 20)
      (log/warn "Low battery" battery-level))))
```

### Kotlin Side
```kotlin
// Clean access with extension properties
val msgType = msg.msgType  // Type-safe!
val payload = msg.payload   // Clean!

// Accessing nested values
val batteryLevel = stateMsg.payload?.system?.batteryLevel
val action = commandMsg.payload?.action ?: "ping"

// Using with when expressions
when (msg.msgType) {
    MessageType.COMMAND.keyword -> handleCommand(msg)
    MessageType.EVENT.keyword -> handleEvent(msg)
    else -> log.warn("Unknown message type: ${msg.msgType}")
}
```

## Key Design Decisions

### Why Transit?
- Human-readable in development (can inspect messages)
- Efficient binary format (MessagePack) in production
- Excellent Clojure/Java interop
- Preserves rich data types (keywords, sets, etc.)

### Why Keywords Everywhere?
- **Performance**: Keywords are interned and compared by reference
- **Consistency**: One pattern throughout the codebase
- **Type Safety**: Can't accidentally use wrong string keys
- **Transit Native**: Transit handles keywords efficiently
- **Clean Code**: Extension properties make Kotlin side readable

### Why Subprocess Isolation?
- Protobuf version conflicts avoided
- Clean separation of concerns
- Easier testing (can mock subprocesses)
- Fault isolation (subprocess crash doesn't kill UI)

### Debouncing Strategy
- Uses protobuf's built-in `equals()` method
- Prevents sending duplicate states
- Reduces IPC overhead
- Configurable via system property

### Rate Limiting
- Token bucket algorithm
- Default: 30 updates/second
- Configurable via Transit control messages
- Separate limit for each state type

## Testing

### Unit Tests
- `transit_core_test.clj` - Core Transit functionality
- `transit_minimal_test.clj` - Basic integration test
- `transit_integration_test.clj` - Full integration scenarios

### Test Strategy
- Mock subprocesses for unit tests
- Test Transit serialization/deserialization
- Verify message envelope structure
- Test rate limiting and debouncing logic

## Configuration

### Environment Variables
- `TRANSIT_DEBUG=true` - Enable Transit debug logging
- `DEBOUNCE_ENABLED=false` - Disable debouncing
- `MAX_RATE_HZ=60` - Set max update rate

### Runtime Configuration
- Rate limits configurable via Transit messages
- Debouncing can be toggled at runtime
- Logging levels adjustable

## Error Handling

### Subprocess Crashes
- Automatic restart with exponential backoff
- State preserved in app-db
- Error logged with full context

### WebSocket Failures
- Reconnection with exponential backoff
- Command queuing during disconnection
- State updates resume on reconnection

### Transit Errors
- Malformed messages logged and skipped
- Type conversion errors handled gracefully
- IPC channel errors trigger subprocess restart

## Performance Considerations

### Debouncing
- Eliminates ~40-60% of redundant updates
- Uses efficient protobuf comparison
- Minimal CPU overhead

### Rate Limiting
- Token bucket refills at configured rate
- O(1) performance for rate checks
- No memory allocation in hot path

### Message Framing
- 4-byte length prefix for efficient parsing
- Direct ByteArray operations
- No intermediate string conversions

## Message Conversion

### Command Generation
- Commands start as Transit maps in Clojure
- CommandSubprocess converts Transit to protobuf
- SimpleCommandBuilder handles the conversion logic (to be replaced with ReadHandlers)
- Supports common commands: ping, rotary-halt, rotary-goto-ndc, etc.

### State Updates with Transit Handlers
- Server sends protobuf state messages
- StateSubprocess uses Transit handlers for automatic serialization
- ProtobufTransitHandlers provides WriteHandlers for all message types
- Enums automatically converted to Transit keywords
- No manual conversion needed - Transit handles everything

```kotlin
// Register handlers with Transit
val writeHandlers = ProtobufTransitHandlers.createWriteHandlers()
val writer = TransitFactory.writer(
    TransitFactory.Format.MSGPACK, 
    outputStream, 
    TransitFactory.writeHandlerMap(writeHandlers)
)

// All types serialize automatically with proper tagging
writer.write(protoState)      // Tagged as "jon-state"
writer.write(gestureEvent)    // Tagged as "gesture-event"
writer.write(controlMessage)  // Tagged as "ctl-message"
```

## Automatic Keyword Type System

### Overview
The codebase uses an automatic keyword-based type system that leverages Transit's capabilities to eliminate manual string/keyword conversions. This system is now fully implemented and operational.

### Core Insight
Our system only uses:
1. **Enums** (from protobuf) → Automatically become keywords
2. **Numbers** (int/float/double) → Stay as numbers  
3. **Special case**: Log messages with `:text` key → Stay as strings

### Implementation Strategy

#### Transit Handlers
Custom Transit handlers automatically convert strings to keywords based on patterns:

```clojure
;; In transit/keyword_handlers.clj
(defn should-keywordize? [value]
  (and (string? value)
       (re-matches #"^[a-z][a-z0-9-]*$" value)  ; Enum pattern
       (not (uuid-string? value))))              ; Preserve UUIDs

;; Text preservation for log messages
(def text-preserving-message-types
  #{:log :error :debug :info :warn :trace})
```

#### Enum Integration
Java enums provide type safety and automatic conversion:

```java
// MessageType.java
public enum MessageType {
    COMMAND("command"),
    STATE_UPDATE("state-update");
    
    private final Keyword keyword;
    
    MessageType(String key) {
        this.key = key;
        this.keyword = TransitFactory.keyword(key);
    }
}
```

#### Benefits
1. **No Manual Conversion**: Enums automatically become keywords
2. **Type Safety**: Compile-time checking with enums
3. **Performance**: Keywords are interned, faster comparisons
4. **Clean Code**: No more `(keyword ...)` or `.toString()` calls

### Implementation Status
1. **Phase 1**: ✅ Transit keyword system implemented
2. **Phase 2**: ✅ All manual keyword conversions removed
3. **Phase 3**: ✅ Full Transit handler architecture completed
   - WriteHandlers for all message types (state, events, control, errors)
   - ReadHandlers for command building
   - Both subprocesses updated to use handlers
4. **Phase 4**: ⏳ Remove all legacy code and manual serialization

### Transit Handler Architecture
The system uses Transit handlers for all message serialization:

```kotlin
// In any subprocess
val handlers = ProtobufTransitHandlers.createWriteHandlers()
val writer = TransitFactory.writer(
    TransitFactory.Format.MSGPACK,
    outputStream,
    TransitFactory.writeHandlerMap(handlers)
)

// Write any supported type - Transit handles serialization
writer.write(protoState)     // Tagged as "jon-state"
writer.write(gestureEvent)   // Tagged as "gesture-event"
writer.write(errorMessage)   // Tagged as "error-message"
```

**No Manual Serialization**: All message types use handlers for consistent, type-safe serialization.

### Examples

```kotlin
// Kotlin - sending
messageProtocol.sendEvent(
    EventType.WINDOW,  // enum
    mapOf("type" to WindowEventType.CLOSE.name.lowercase())
)

// Transit wire format (automatic conversion)
{:msg-type :event
 :payload {:type :close}}

// Clojure - receiving
(case (:type payload)
  :close (handle-close))  ; Direct keyword comparison!
```

## Implementation Files

### Clojure Side
- `src/potatoclient/transit/core.clj` - Transit reader/writer creation
- `src/potatoclient/transit/app_db.clj` - App-db atom and state management  
- `src/potatoclient/transit/commands.clj` - Command API functions
- `src/potatoclient/transit/subprocess_launcher.clj` - Process lifecycle
- `src/potatoclient/transit/handlers.clj` - Message handlers
- `src/potatoclient/transit/validation.clj` - Message validation
- `src/potatoclient/transit/keyword_handlers.clj` - Automatic keyword conversion

### Kotlin Side
- `TransitCommunicator.kt` - Low-level Transit I/O with handler support
- `TransitMessageProtocol.kt` - Message protocol implementation
- `CommandSubprocess.kt` - Command handling subprocess
- `StateSubprocess.kt` - State update subprocess  
- `SimpleCommandBuilder.kt` - Transit to protobuf conversion (kept for fallback)
- `ProtobufCommandHandlers.kt` - Transit ReadHandlers for command building
- `ProtobufTransitHandlers.kt` - Transit handlers for all message types

## Future Improvements

1. ~~**Replace SimpleCommandBuilder**: Use Transit ReadHandlers for command building~~ ✅ COMPLETED
2. ~~**Malli Spec Validation**: Create specs matching protobuf validation constraints~~ ✅ COMPLETED
3. **Message Compression**: Add optional gzip for large states
4. **Batching**: Combine multiple commands in single message
5. **Metrics**: Add performance metrics collection
6. **Schema Evolution**: Version message formats
7. **Binary Diffing**: Send only changed fields for states
8. **Complete Command Support**: Add remaining command types (LIRA, LRF_align)
9. **Remove Legacy Code**: Clean up SimpleCommandBuilder and SimpleStateConverter once handlers are proven stable
10. **WebSocket Error Handling**: Send errors via Transit protocol instead of stderr