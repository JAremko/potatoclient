# Video Stream Command Migration Guide

## Overview

In August 2025, the video stream command format was completely redesigned to match the protobuf structure and use keywords consistently. This guide helps developers understand the changes.

## What Changed

### ❌ Old Format (Deprecated)
```clojure
;; Commands used action/params structure
{:action "rotary-goto-ndc"
 :params {:channel "HEAT"
          :x 0.5
          :y -0.3}}

;; Enum values were strings
{:action "rotary-set-velocity"
 :params {:azimuth-direction "CLOCKWISE"
          :elevation-direction "COUNTER_CLOCKWISE"}}
```

### ✅ New Format
```clojure
;; Commands use nested structure matching protobuf
{:rotary {:goto-ndc {:channel :heat  ; keyword!
                     :x 0.5
                     :y -0.3}}}

;; All enum values are keywords
{:rotary {:set-velocity {:azimuth-direction :clockwise      ; keyword!
                        :elevation-direction :counter-clockwise}}} ; keyword!
```

## Migration Checklist

### For Kotlin Developers

1. **Use CommandBuilder** instead of manual map construction:
```kotlin
// ❌ Old way
val command = mapOf(
    "action" to "rotary-goto-ndc",
    "params" to mapOf("channel" to "HEAT", "x" to 0.5, "y" to -0.3)
)

// ✅ New way
val command = CommandBuilder.rotaryGotoNDC(StreamType.HEAT, 0.5, -0.3)
```

2. **Convert enums to keywords** using extension functions:
```kotlin
// Extension functions handle conversion
fun StreamType.toKeyword(): String = when (this) {
    StreamType.HEAT -> "heat"    // Note: lowercase
    StreamType.DAY -> "day"
}
```

3. **Use sendCommand()** not sendRequest():
```kotlin
// ❌ Old
messageProtocol.sendRequest(action, params)

// ✅ New
messageProtocol.sendCommand(command)
```

### For Clojure Developers

1. **Remove old command builders** - Video streams now send complete commands
2. **Update message handlers** to expect new format:
```clojure
;; Old handler expected action/params
(defmethod handle-message :request [{:keys [action params]}]
  ...)

;; New handler receives complete commands
(defmethod handle-message :command [{:keys [payload]}]
  ;; payload is like {:rotary {:goto-ndc {...}}}
  ...)
```

3. **All values are keywords** in message handling:
```clojure
;; Everything arrives as keywords
(case (:stream-type event)  ; :heat or :day, not "HEAT" or "DAY"
  :heat (handle-heat-camera)
  :day (handle-day-camera))
```

## Command Format Reference

### Rotary Commands
```clojure
;; Goto position
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.3}}}

;; Set velocity
{:rotary {:set-velocity {:azimuth-speed 0.1
                        :elevation-speed 0.05
                        :azimuth-direction :clockwise
                        :elevation-direction :counter-clockwise}}}

;; Halt
{:rotary {:halt {}}}
```

### CV Commands
```clojure
;; Start tracking (with optional frame time)
{:cv {:start-track-ndc {:channel :heat
                       :x 0.0
                       :y 0.0
                       :frame-time 12345}}}
```

### Camera Commands
```clojure
;; Heat camera zoom
{:heat-camera {:next-zoom-table-pos {}}}
{:heat-camera {:prev-zoom-table-pos {}}}

;; Day camera zoom
{:day-camera {:next-zoom-table-pos {}}}
{:day-camera {:prev-zoom-table-pos {}}}
```

## Testing Your Migration

### 1. Use the Mock Video Stream Tool

```bash
cd tools/mock-video-stream

# Validate command format
make validate

# Generate test scenarios
make generate

# Run as mock subprocess
make process STREAM_TYPE=heat
```

### 2. Run Integration Tests

```kotlin
// Kotlin tests
./gradlew test --tests "*CommandBuilderTest"
./gradlew test --tests "*VideoStreamCommandIntegrationTest"
```

```clojure
;; Clojure tests
(require '[potatoclient.test.video-stream-test :as vst])
(vst/run-all-tests)
```

### 3. Check for Common Issues

| Issue | Solution |
|-------|----------|
| "Unknown command format" | Use CommandBuilder, not manual maps |
| "Invalid enum value" | Convert to keyword with .toKeyword() |
| "Command not received" | Use sendCommand() not sendRequest() |
| Window events have strings | Update to use EventType.keyword |

## Rollback Plan

**There is no rollback** - this is a breaking change with no backward compatibility. All components must be updated together:

1. Video stream subprocesses (Kotlin)
2. Main process message handlers (Clojure)
3. Command subprocess handlers (Kotlin)

## Benefits of New Format

1. **Type Safety** - CommandBuilder ensures correct structure
2. **Consistency** - Matches protobuf exactly
3. **Performance** - No string parsing or conversion
4. **Debugging** - Clear command structure in logs
5. **Testing** - Mock tool validates all commands

## Support

- Architecture docs: `.claude/video-stream-architecture.md`
- Mock tool: `tools/mock-video-stream/README.md`
- Example code: See test files
- Original TODO: `TODO_EVENT_ISOLATION_AND_TESTING.md`