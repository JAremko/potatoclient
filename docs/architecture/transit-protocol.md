# Transit Protocol Specification

The Transit protocol is the foundation of all inter-process communication in PotatoClient. It provides type-safe, efficient message passing between Clojure and Kotlin processes.

## Core Philosophy: Keywords Everywhere

**In Clojure, all data is keywords and numbers.** The only exceptions are actual text content:
- Log message text (`:message` field in log messages)
- Error descriptions (`:error` and `:message` fields in error messages)

This principle enables:
- Type safety through compile-time enum checking
- Performance through keyword interning
- Clean code without string/keyword conversions
- Automatic serialization via Transit handlers

## Message Envelope

All messages follow a standard envelope format:

```clojure
{:msg-type keyword    ; Message type (e.g., :command, :event)
 :msg-id   string     ; UUID for message tracking
 :timestamp long      ; Unix timestamp in milliseconds
 :payload  map}       ; Message-specific payload
```

### Message Types

All message types from the `MessageType` enum automatically convert to keywords:

| Java Enum | Transit Keyword |
|-----------|----------------|
| `COMMAND` | `:command` |
| `STATE_UPDATE` | `:state-update` |
| `REQUEST` | `:request` |
| `RESPONSE` | `:response` |
| `LOG` | `:log` |
| `ERROR` | `:error` |
| `STATUS` | `:status` |
| `METRIC` | `:metric` |
| `EVENT` | `:event` |

## Message Specifications

### Command Messages

Commands use nested keyword structure matching protobuf hierarchy:

```clojure
{:msg-type :command
 :msg-id "550e8400-e29b-41d4-a716-446655440000"
 :timestamp 1627849200000
 :payload {:rotary {:goto-ndc {:channel :heat  ; Note: keyword!
                               :x 0.5
                               :y -0.5}}}}
```

### State Updates

State updates with automatic enum conversion:

```clojure
{:msg-type :state-update
 :msg-id "uuid"
 :timestamp 1627849200000
 :payload {:system {:localization :ukrainian    ; Enum → keyword
                   :cpu-temperature 45.2
                   :rec-enabled true}
           :rotary {:mode :stabilized          ; Enum → keyword
                   :azimuth 123.45
                   :is-moving true}}}
```

### Events

Events from video streams and UI:

```clojure
{:msg-type :event
 :msg-id "uuid"
 :timestamp 1627849200000
 :payload {:type :gesture              ; EventType → keyword
           :gesture-type :tap          ; GestureType → keyword
           :stream-type :heat          ; StreamType → keyword
           :x 0.5
           :y 0.5
           :ndc-x 0.0
           :ndc-y 0.0}}
```

### Log Messages

Log messages preserve text content:

```clojure
{:msg-type :log
 :msg-id "uuid"
 :timestamp 1627849200000
 :payload {:level :debug                        ; LogLevel → keyword
           :message "Connection established"     ; String (actual text)
           :process :command}}                   ; ProcessType → keyword
```

### Error Messages

Errors preserve text for debugging:

```clojure
{:msg-type :error
 :msg-id "uuid"
 :timestamp 1627849200000
 :payload {:error "IOException"                  ; String (exception type)
           :message "Connection refused"         ; String (error text)
           :stack-trace "at line 42..."          ; String (stack trace)
           :context {:process :state             ; Keyword context
                    :retry-count 3}}}            ; Number
```

## Keyword Conversion Rules

### Automatic Conversions

1. **Enums → Keywords**: All Java/Kotlin enums become keywords
2. **Map Keys → Keywords**: All Transit map keys are keywords
3. **Enum-like Strings → Keywords**: Known string values become keywords

### Protobuf Enum Examples

| Protobuf Enum | Transit Keyword |
|---------------|----------------|
| `JON_GUI_DATA_SYSTEM_LOCALIZATIONS_UA` | `:ukrainian` |
| `JON_GUI_DATA_GPS_FIX_TYPE_3D` | `:3d` |
| `JON_GUI_DATA_ROTARY_MODE_STABILIZED` | `:stabilized` |
| `HEAT` | `:heat` |
| `DAY` | `:day` |

## Implementation

### Kotlin Side

```kotlin
// Automatic serialization with Transit handlers
val handlers = ProtobufTransitHandlers.createWriteHandlers()
val writer = TransitFactory.writer(
    TransitFactory.Format.MSGPACK,
    outputStream,
    TransitFactory.writeHandlerMap(handlers)
)

// Enums automatically convert
writer.write(MessageType.COMMAND)  // → :command
writer.write(StreamType.HEAT)      // → :heat
```

### Clojure Side

```clojure
;; Keywords arrive naturally
(defmethod handle-message :state
  [_ _ msg]
  (let [mode (get-in msg [:payload :rotary :mode])]  ; Already :stabilized
    (case mode
      :stabilized (handle-stabilized)
      :platform (handle-platform)
      :scanning (handle-scanning))))

;; No conversions needed
(case (:stream-type event)
  :heat (handle-heat-stream)
  :day (handle-day-stream))
```

## Validation

### Malli Schemas

```clojure
(def message-envelope
  [:map
   [:msg-type [:enum :command :state-update :event :log :error
               :status :metric :request :response]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

(def gesture-event
  [:map
   [:type [:= :gesture]]
   [:gesture-type [:enum :tap :double-tap :pan :swipe]]
   [:stream-type [:enum :heat :day]]
   [:x number?]
   [:y number?]
   [:ndc-x [:and number? [:>= -1.0] [:<= 1.0]]]
   [:ndc-y [:and number? [:>= -1.0] [:<= 1.0]]]])
```

## Best Practices

### DO:
- Use keywords for all enum values
- Use keywords for all map keys
- Let Transit handlers do conversions
- Trust automatic serialization

### DON'T:
- Don't call `(keyword ...)` manually
- Don't use string keys in maps
- Don't convert enums to strings
- Don't bypass Transit handlers

### REMEMBER:
- **Keywords are data** in our system
- **Strings are only for human text**
- **Everything else is a keyword**

## Benefits

1. **Type Safety**: Compile-time enum checking
2. **Performance**: Keywords intern and compare by reference
3. **Clarity**: No type ambiguity
4. **Simplicity**: No manual conversions
5. **Consistency**: One pattern everywhere

## See Also

- [System Overview](./system-overview.md) - Overall architecture
- [Command System](./command-system.md) - Command routing details
- [Message Types Reference](../reference/message-types.md) - Complete message catalog