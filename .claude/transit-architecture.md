# Transit Architecture Documentation

## Overview

PotatoClient uses a Transit-based architecture to completely isolate protobuf handling from the Clojure codebase. This design provides clean separation of concerns, better testability, and eliminates protobuf dependencies in the main process.

## Architecture Principles

1. **Complete Protobuf Isolation**: All protobuf code is confined to Kotlin subprocesses
2. **Transit for IPC**: All inter-process communication uses Transit with MessagePack format
3. **Single App State**: Following re-frame pattern with a single app-db atom
4. **Subprocess Management**: Clean lifecycle management for all subprocesses
5. **Keyword-Based Maps**: All Transit messages use keyword keys for consistency and performance

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
- Uses extension properties for clean message access
- Converts to protobuf using `SimpleCommandBuilder`
- Sends protobuf commands via WebSocket to server
- Handles reconnection and error recovery

#### `StateSubprocess`
Main class for state updates:
- Receives protobuf state from WebSocket
- Converts to Transit maps using `SimpleStateConverter`
- Implements debouncing (compares with last sent state)
- Token bucket rate limiting (configurable)
- Sends Transit messages to main process via stdout
- Uses extension properties for clean keyword access

#### `TransitCommunicator`
Handles Transit communication over stdin/stdout:
- Message framing with length prefix
- Thread-safe read/write operations
- Preserves Transit keywords in both directions

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
3. Command returns Transit map: `{:action "rotary-goto" :params {...}}`
4. Subprocess launcher sends map to CommandSubprocess via Transit
5. CommandSubprocess converts to protobuf using SimpleCommandBuilder
6. Protobuf sent via WebSocket to server
```

### State Flow (Server → UI)
```
1. Server sends protobuf state via WebSocket
2. StateSubprocess receives and parses protobuf
3. Debouncing: Compare with last sent state, skip if identical
4. Rate limiting: Check token bucket, skip if rate exceeded
5. Convert protobuf to Transit map using SimpleStateConverter
6. Send Transit map to main process via stdout
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
// OLD way with string keys (don't do this!)
val msgType = msg["msg-type"] as? String
val payload = msg["payload"] as? Map<*, *>

// NEW way with extension properties
val msgType = msg.msgType  // Clean!
val payload = msg.payload   // Type-safe!

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

## Automatic Keyword Type System (In Progress)

### Overview
The codebase is transitioning to an automatic keyword-based type system that leverages Transit's capabilities to eliminate manual string/keyword conversions.

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

### Migration Path
1. **Phase 1**: Add Transit handlers (backward compatible)
2. **Phase 2**: Remove manual conversions
3. **Phase 3**: Add validation and type registry

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

## Future Improvements

1. **Complete Keyword System**: Finish automatic keyword conversion implementation
2. **Message Compression**: Add optional gzip for large states
3. **Batching**: Combine multiple commands in single message
4. **Metrics**: Add performance metrics collection
5. **Schema Evolution**: Version message formats
6. **Binary Diffing**: Send only changed fields for states
