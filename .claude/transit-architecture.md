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
- Commands use nested format matching protobuf structure
- All parameters use keywords consistently (`:heat`, `:day`, `:en`, etc.)
- Commands include: ping, start-recording, rotary-goto, camera controls, etc.
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
- Converts to protobuf using `GeneratedCommandHandlers` (static code generation)
- Sends protobuf commands via WebSocket to server
- Handles reconnection and error recovery
- Sends WebSocket errors via Transit protocol (not stderr)

#### `StateSubprocess`
Main class for state updates:
- Receives protobuf state from WebSocket
- Uses `GeneratedStateHandlers` for automatic Transit conversion
- All protobuf fields automatically converted to keyword-based maps
- Token bucket rate limiting (configurable via control messages)
- Sends Transit messages to main process via stdout
- Zero manual conversion needed

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

#### `GeneratedCommandHandlers` & `GeneratedStateHandlers`
Auto-generated Transit handlers from protobuf definitions:
- Static code generation from keyword trees (`proto_keyword_tree_cmd.clj`, `proto_keyword_tree_state.clj`)
- Type-safe conversion between Transit maps and protobuf objects
- Automatic handling of nested messages and enums
- Natural disambiguation of common commands (`:start`, `:stop`) by parent context
- Zero reflection, compile-time checked
- Regenerated automatically when protos change

#### Static Code Generation Architecture
The system uses generated handlers created from protobuf definitions:
- Keyword trees map EDN keywords to Java protobuf classes
- Generated at build time by Proto Explorer tool
- Supports all 15 command types and 13 state types
- Handles complex nested structures and oneofs
- Common commands disambiguated by parent context:
  - `{:gps {:start {}}}` → `JonSharedCmdGps.Start`
  - `{:lrf {:start {}}}` → `JonSharedCmdLrf.Start`
  - `{:rotary {:start {}}}` → `JonSharedCmdRotary.Start`

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
2. Clojure creates nested command map: `{:rotary {:goto {:azimuth 180.0}}}`
3. Subprocess launcher sends map to CommandSubprocess via Transit
4. GeneratedCommandHandlers converts Transit map to protobuf:
   - Finds `:rotary` in root → calls `buildRotary()`
   - Finds `:goto` in rotary → calls `buildRotaryGoto()`
   - Sets field values and builds protobuf message
5. Protobuf sent via WebSocket to server
```

### State Flow (Server → UI)
```
1. Server sends protobuf state via WebSocket
2. StateSubprocess receives and parses to `JonGUIState` protobuf
3. GeneratedStateHandlers extracts all fields to Transit maps:
   - Recursively converts nested messages
   - Enums become keywords (e.g., `LOCKED` → `:locked`)
   - All field names kebab-cased
4. Debouncing: Compare with last sent state, skip if identical
5. Rate limiting: Check token bucket, skip if rate exceeded
6. Send Transit message to main process via stdout
7. Main process updates app-db with keyword-based state
8. UI components react to state changes
```

### State Integration with App-DB

The state handling system is fully integrated with the re-frame-style app-db:

#### Message Handler (`app_db.clj`)
```clojure
(>defn handle-state-update
  "Handle state update messages from state subprocess"
  [msg]
  [map? => nil?]
  ;; Extract state from payload (Transit uses string keys)
  (when-let [state-data (get-in msg ["payload" "state"])]
    ;; Map protobuf state keys to app-db keys
    (let [state-update 
          (cond-> {}
            ;; System state
            (contains? state-data "system")
            (assoc :system (map-system-state (get state-data "system")))
            ;; GPS state
            (contains? state-data "gps")
            (assoc :gps (map-gps-state (get state-data "gps")))
            ;; ... other subsystems
            )]
      ;; Update server state in app-db
      (update-server-state! state-update))))
```

#### Key Mapping Considerations
- **Transit String Keys**: Messages from Kotlin use string keys, not keywords
- **Protobuf → App-DB Mapping**: State fields are mapped to match existing app-db structure
- **Type Conversions**: Booleans, enums, and numbers are properly converted
- **Defaults**: Missing fields get sensible defaults to prevent nil errors

#### Testing
Comprehensive tests ensure state integration works correctly:
- `app_db_state_test.clj` - Tests all subsystem state updates
- `state_roundtrip_test.clj` - Tests protobuf → Transit conversion
- `state_e2e_test.clj` - End-to-end subprocess communication tests

## Keyword Usage Examples

### Clojure Side
```clojure
;; Creating messages with nested structure
(transit-core/create-message :command
  {:rotary {:goto-ndc {:channel "heat" :x 0.5 :y -0.5}}})
;; Result: {:msg-type :command
;;          :msg-id "uuid..."
;;          :timestamp 1234567890
;;          :payload {:rotary {:goto-ndc {:channel "heat" ...}}}}

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

// Accessing nested command values
val commandData = msg.payload as? Map<String, Any>
val rotaryCmd = commandData?.get("rotary") as? Map<String, Any>
val gotoParams = rotaryCmd?.get("goto") as? Map<String, Any>

// Or use generated handlers directly
val protobuf = GeneratedCommandHandlers.buildCommand(commandData)

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

### Malli Generator Validation Tests (NEW)
Comprehensive validation testing with generated data:
- `malli_generation_test.clj` - Tests Malli generates valid command parameters
- `simple_malli_validation_test.clj` - Verifies generated data creates valid commands
- `sanity_check_validation_test.clj` - Ensures each validation stage signals failures
- `kotlin_malli_integration_test.clj` - Full Kotlin validation integration
- `ValidatorSanityTest.kt` - Kotlin-side validation verification

Each validation stage is verified to work:
1. **Guardrails** - Catches invalid function arguments
2. **Transit** - Detects corrupted data
3. **Kotlin handlers** - Reject invalid structures
4. **Protobuf** - Enforces required fields
5. **buf.validate** - Validates constraints
6. **Binary roundtrip** - Preserves data integrity
7. **Java equals** - Detects differences

### Test Strategy
- Mock subprocesses for unit tests
- Test Transit serialization/deserialization
- Verify message envelope structure
- Test rate limiting and debouncing logic
- Generate edge cases with Malli for comprehensive coverage

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
- GeneratedCommandHandlers handles all conversion automatically
- No manual mapping needed - static code generation from protobuf

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
- `GeneratedCommandHandlers.kt` - Auto-generated command builders
- `GeneratedStateHandlers.kt` - Auto-generated state extractors
- `TransitKeys.kt` - Pre-created keyword constants
- `TransitExtensions.kt` - Extension properties for clean access

## Future Improvements

1. ~~**Replace Manual Builders**: Use static code generation~~ ✅ COMPLETED
2. ~~**Malli Spec Validation**: Create specs matching protobuf constraints~~ ✅ COMPLETED
3. ~~**Remove Legacy Code**: Clean up manual builders~~ ✅ COMPLETED
4. **Message Compression**: Add optional gzip for large states
5. **Batching**: Combine multiple commands in single message
6. **Metrics**: Add performance metrics collection
7. **Schema Evolution**: Version message formats
8. **Binary Diffing**: Send only changed fields for states
9. **Complete Clojure Integration**: Update all command senders to use new format
10. **WebSocket Error Handling**: Improve error propagation via Transit