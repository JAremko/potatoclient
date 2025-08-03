# Transit Message Protocol Specification

## Core Philosophy: Keywords Everywhere

**In Clojure, all data is keywords and numbers.** The only exceptions are actual text content:
- Log message text (`:message` field in log messages)
- Error descriptions (`:error` and `:message` fields in error messages)

This principle is fundamental to our architecture and enables:
- Type safety through compile-time enum checking
- Performance through keyword interning
- Clean code without string/keyword conversions
- Automatic serialization via Transit handlers

## Message Envelope Structure

All messages follow a standard envelope format with **keyword keys**:

```clojure
{:msg-type keyword    ; Message type (from MessageType enum)
 :msg-id   string     ; UUID for message tracking
 :timestamp long      ; Unix timestamp in milliseconds
 :payload  map}       ; Message-specific payload with keyword keys
```

### Message Types (from MessageType enum)

All message types are automatically converted to keywords:
- `COMMAND` → `:command`
- `STATE_UPDATE` → `:state-update`
- `REQUEST` → `:request`
- `RESPONSE` → `:response`
- `LOG` → `:log`
- `ERROR` → `:error`
- `STATUS` → `:status`
- `METRIC` → `:metric`
- `EVENT` → `:event`

## Keyword Conversion Rules

### Automatic Conversions

1. **Enums → Keywords**: All protobuf enums automatically become keywords
   ```kotlin
   // Kotlin sends
   MessageType.COMMAND
   
   // Clojure receives
   :command
   ```

2. **String Fields → Keywords**: Enum-like string fields become keywords
   ```kotlin
   // Kotlin sends
   "gesture-type" -> "tap"
   
   // Clojure receives
   :gesture-type :tap
   ```

3. **Numbers Stay Numbers**: All numeric types remain as-is
   ```kotlin
   // Kotlin sends
   "x" -> 0.5f
   
   // Clojure receives
   :x 0.5
   ```

### Text Content Exceptions

Only these fields remain as strings:
- Log messages: `{:level :info :message "User clicked button"}`
- Error messages: `{:error "NullPointerException" :message "Cannot read property"}`
- Stack traces: `{:stack-trace "at line 42..."}`

## Message Type Specifications

### Command Messages

Commands use nested keyword structure matching protobuf hierarchy:

```clojure
{:msg-type :command
 :msg-id "uuid-here"
 :timestamp 1234567890
 :payload {:rotary {:goto-ndc {:channel "heat"  ; String for channel
                               :x 0.5           ; Number
                               :y -0.5}}}}      ; Number
```

### State Update Messages

State updates with automatic enum conversion:

```clojure
{:msg-type :state-update
 :msg-id "uuid-here"
 :timestamp 1234567890
 :payload {:system {:localization :ukrainian     ; Enum → keyword
                   :cpu-temperature 45.2         ; Number
                   :rec-enabled true}            ; Boolean
           :rotary {:mode :stabilized           ; Enum → keyword
                   :azimuth 123.45               ; Number
                   :is-moving true}}}            ; Boolean
```

### Event Messages

Events with typed payloads:

```clojure
{:msg-type :event
 :msg-id "uuid-here"
 :timestamp 1234567890
 :payload {:type :gesture                        ; EventType enum → keyword
           :gesture-type :tap                    ; GestureType → keyword
           :stream-type :heat                    ; Channel → keyword
           :x 0.5                               ; Number
           :y 0.5}}                             ; Number
```

### Log Messages

Log messages preserve text content:

```clojure
{:msg-type :log
 :msg-id "uuid-here"
 :timestamp 1234567890
 :payload {:level :debug                         ; LogLevel → keyword
           :message "Connection established"      ; String (actual text)
           :logger "CommandSubprocess"            ; String (logger name)
           :process :command}}                    ; ProcessType → keyword
```

### Error Messages

Errors preserve text for debugging:

```clojure
{:msg-type :error
 :msg-id "uuid-here"
 :timestamp 1234567890
 :payload {:error "IOException"                   ; String (exception type)
           :message "Connection refused"          ; String (error text)
           :stack-trace "at line 42..."           ; String (stack trace)
           :context {:process :state              ; Keyword context
                    :retry-count 3}}}             ; Number
```

## Transit Handler Integration

### Kotlin Side

All message types use Transit handlers for automatic serialization:

```kotlin
// Register handlers
val handlers = ProtobufTransitHandlers.createWriteHandlers()
val writer = TransitFactory.writer(
    TransitFactory.Format.MSGPACK,
    outputStream,
    TransitFactory.writeHandlerMap(handlers)
)

// Automatic serialization with proper tagging
writer.write(protoState)      // Tagged as "jon-state"
writer.write(gestureEvent)    // Tagged as "gesture-event"
writer.write(logMessage)      // Tagged as "log-message"
```

### Clojure Side

Keywords arrive naturally without conversion:

```clojure
;; Direct keyword access
(defmethod handle-message :state
  [_ _ msg]
  (let [mode (get-in msg [:payload :rotary :mode])]  ; Already a keyword!
    (case mode
      :stabilized (handle-stabilized)
      :platform (handle-platform)
      :scanning (handle-scanning))))

;; No string/keyword conversions needed
(defmethod handle-message :event
  [_ _ msg]
  (case (get-in msg [:payload :type])
    :gesture (handle-gesture msg)
    :navigation (handle-nav msg)
    :window (handle-window msg)))
```

## Enum Definitions

All enums in the system automatically convert to keywords:

### MessageType (Java)
```java
public enum MessageType {
    COMMAND("command"),
    STATE_UPDATE("state-update"),
    REQUEST("request"),
    RESPONSE("response"),
    LOG("log"),
    ERROR("error"),
    STATUS("status"),
    METRIC("metric"),
    EVENT("event");
    
    private final Keyword keyword;
    
    MessageType(String key) {
        this.keyword = TransitFactory.keyword(key);
    }
}
```

### EventType (Java)
```java
public enum EventType {
    NAVIGATION("navigation"),
    WINDOW("window"),
    FRAME("frame"),
    ERROR("error"),
    GESTURE("gesture");
}
```

### Protobuf Enums
All protobuf enums (localization, GPS fix type, rotary mode, etc.) automatically convert to lowercase keywords:
- `JON_GUI_DATA_SYSTEM_LOCALIZATIONS_UA` → `:ukrainian`
- `JON_GUI_DATA_GPS_FIX_TYPE_3D` → `:3d`
- `JON_GUI_DATA_ROTARY_MODE_STABILIZED` → `:stabilized`

## Validation

### Clojure Side

Use Malli schemas with keyword specs:

```clojure
(def command-payload
  ;; Nested structure matching protobuf hierarchy
  ;; Top-level keys are command categories
  [:map-of keyword? [:map-of keyword? any?]])

(def state-payload
  [:map
   [:system {:optional true} 
    [:map
     [:localization {:optional true} keyword?]
     [:cpu-temperature {:optional true} number?]]]
   [:rotary {:optional true}
    [:map
     [:mode {:optional true} keyword?]
     [:azimuth {:optional true} number?]]]])
```

### Kotlin Side

Extension properties provide type-safe access:

```kotlin
// Clean keyword access
val msgType = msg.msgType      // Returns keyword
val commandData = msg.payload as? Map<String, Any>
val rotaryCmd = commandData?.get("rotary") as? Map<String, Any>

// Type checking built-in
when (msg.msgType) {
    MessageType.COMMAND.keyword -> handleCommand(msg)
    MessageType.EVENT.keyword -> handleEvent(msg)
}
```

## Implementation Guidelines

### DO:
- Use keywords for all enum values
- Use keywords for all map keys
- Let Transit handlers do the conversion
- Trust the automatic serialization

### DON'T:
- Don't call `(keyword ...)` in Clojure
- Don't use string keys in Transit maps
- Don't manually convert enums to strings
- Don't bypass the Transit handlers

### REMEMBER:
- **Keywords are data** in our system
- **Strings are only for human-readable text**
- **Numbers are numbers**
- **Everything else is a keyword**

## Benefits of This Approach

1. **Type Safety**: Enums provide compile-time checking
2. **Performance**: Keywords are interned and compare by reference
3. **Clarity**: No ambiguity about data types
4. **Simplicity**: No manual conversions needed
5. **Consistency**: One pattern everywhere

## Migration Notes

This is a clean architecture with no backward compatibility:
- All new code uses keywords
- No legacy string keys supported
- No manual converters maintained
- Single source of truth: Transit handlers

## Command Format Evolution

The command format has evolved to directly mirror protobuf structure:

```clojure
;; Old format (action-based)
{:action "rotary-goto-ndc" :params {:channel "heat" :x 0.5 :y -0.5}}

;; New format (nested with consistent keywords)
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}
```

**Key improvements**:
- Direct mapping to protobuf message structure
- All parameters use keywords consistently (`:heat`, `:day`, `:en`, `:uk`)
- No more action strings or params wrapping
- Split complex commands (e.g., `set-recording` → `start-recording`/`stop-recording`)
- Focus modes use keywords: `:auto`, `:manual`, `:infinity`
- Palette names use keywords: `:white-hot`, `:black-hot`, etc.
