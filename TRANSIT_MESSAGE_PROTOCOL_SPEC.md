# Transit Message Protocol Specification

## Overview

This document specifies the Transit message protocol used for inter-process communication in PotatoClient. All messages use Transit's MessagePack format with keyword keys.

## Core Principles

1. **All keys are Transit keywords** - No string keys in messages
2. **Consistent envelope structure** - Every message has the same envelope
3. **Type safety** - Message types defined in Java enums
4. **Bidirectional** - Same protocol for both directions

## Message Envelope

Every Transit message follows this structure:

```clojure
{:msg-type keyword    ; Message type (from MessageType enum)
 :msg-id   string     ; UUID for message tracking
 :timestamp long      ; Unix timestamp in milliseconds
 :payload  map}       ; Message-specific payload
```

## Message Types

Defined in `potatoclient.transit.MessageType`:

- `:command` - Commands to be executed
- `:response` - Response to a command
- `:request` - Request from subprocess
- `:log` - Log messages
- `:error` - Error messages
- `:status` - Status updates
- `:metric` - Performance metrics
- `:event` - Application events
- `:state` - State updates

## Payload Structures

### Command Message
```clojure
{:msg-type :command
 :msg-id "550e8400-e29b-41d4-a716-446655440000"
 :timestamp 1234567890123
 :payload {:action "rotary-goto-ndc"
           :params {:channel :heat
                    :x 0.5
                    :y -0.5}}}
```

### State Update Message
```clojure
{:msg-type :state
 :msg-id "550e8400-e29b-41d4-a716-446655440001"
 :timestamp 1234567890456
 :payload {:timestamp 1234567890456
           :system {:battery-level 85
                    :has-data true}
           :proto-received true}}
```

### Event Message
```clojure
{:msg-type :event
 :msg-id "550e8400-e29b-41d4-a716-446655440002"
 :timestamp 1234567890789
 :payload {:type :gesture
           :gesture-type :tap
           :x 400
           :y 300
           :ndc-x 0.5
           :ndc-y -0.25
           :canvas-width 800
           :canvas-height 600
           :stream-type :heat}}
```

### Log Message
```clojure
{:msg-type :log
 :msg-id "550e8400-e29b-41d4-a716-446655440003"
 :timestamp 1234567891012
 :payload {:level "INFO"
           :message "WebSocket connected"
           :process "command"}}
```

### Error Message
```clojure
{:msg-type :error
 :msg-id "550e8400-e29b-41d4-a716-446655440004"
 :timestamp 1234567891345
 :payload {:error "Connection failed"
           :stack-trace "..."
           :process "state"}}
```

## Kotlin Implementation

### Reading Messages
```kotlin
// Using extension properties for clean access
val msg = transitComm.readMessage()
when (msg.msgType) {
    MessageType.COMMAND.keyword -> {
        val action = msg.payload?.action ?: "ping"
        val params = msg.payload?.params
        processCommand(action, params)
    }
    MessageType.RESPONSE.keyword -> {
        val status = msg.payload?.status
        handleResponse(status)
    }
}
```

### Writing Messages
```kotlin
// Send response with string keys (Transit converts to keywords)
transitComm.sendMessage(
    mapOf(
        "msg-type" to "response",
        "msg-id" to UUID.randomUUID().toString(),
        "timestamp" to System.currentTimeMillis(),
        "payload" to mapOf(
            "status" to "success",
            "result" to processResult
        )
    )
)
```

## Clojure Implementation

### Creating Messages
```clojure
;; Use create-message for consistent envelopes
(transit-core/create-message :command
  {:action "ping"})

;; Or manually create with keywords
{:msg-type :command
 :msg-id (str (java.util.UUID/randomUUID))
 :timestamp (System/currentTimeMillis)
 :payload {:action "ping"}}
```

### Handling Messages
```clojure
(defmethod handle-message :command
  [_ _ msg]
  (let [action (get-in msg [:payload :action])
        params (get-in msg [:payload :params])]
    (case action
      "ping" (handle-ping)
      "rotary-goto-ndc" (handle-rotary-goto params)
      (log/warn "Unknown command:" action))))
```

## Transport Layer

### Framing Protocol
Messages are framed with a 4-byte length prefix (big-endian):

```
[4 bytes length][N bytes Transit MessagePack data]
```

### stdin/stdout Communication
- Subprocesses read from stdin and write to stdout
- Main process manages bidirectional streams
- Framing ensures message boundaries

### Error Handling
- Malformed messages are logged and skipped
- Stream errors trigger subprocess restart
- Connection errors use exponential backoff

## Best Practices

1. **Always use keywords** - Never use string keys in Transit messages
2. **Use extension properties** - In Kotlin, use the provided extensions
3. **Validate messages** - Use Malli schemas to validate structure
4. **Include timestamps** - Every message should have a timestamp
5. **Use UUIDs** - Message IDs should be unique UUIDs
6. **Handle unknown types** - Log and skip unknown message types

## Performance Considerations

1. **Keyword interning** - Keywords are interned for fast comparison
2. **Pre-created constants** - Use `TransitKeys` to avoid allocation
3. **Extension properties** - Zero-cost abstractions in Kotlin
4. **Message pooling** - Reuse message maps where possible
5. **Batch operations** - Send multiple updates in one message when feasible

## Version Compatibility

- Protocol version: 1.0
- No breaking changes allowed
- New message types can be added
- New payload fields can be added
- Receivers must ignore unknown fields