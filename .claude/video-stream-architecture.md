# Video Stream Architecture

## Overview

This document describes the video stream architecture after the August 2025 refactoring. The system now uses a clean, keyword-based command format that matches the protobuf structure exactly.

## Architecture Diagram

```
┌─────────────────────┐     ┌─────────────────────┐     ┌──────────────────┐
│  Video Component    │     │   Clojure Main      │     │ Command Subprocess│
│  (Swing/GStreamer)  │     │   (Router)          │     │    (Kotlin)      │
└──────────┬──────────┘     └──────────┬──────────┘     └────────┬─────────┘
           │                           │                          │
     Mouse Events                      │                          │
           │                           │                          │
           ▼                           │                          │
┌─────────────────────┐                │                          │
│  MouseEventHandler  │                │                          │
│  (Kotlin)           │                │                          │
├─────────────────────┤                │                          │
│ • Gesture detection │                │                          │
│ • NDC conversion    │                │                          │
│ • CommandBuilder    │                │                          │
└──────────┬──────────┘                │                          │
           │                           │                          │
      Commands                         │                          │
           │                           │                          │
           ▼                           ▼                          │
┌─────────────────────┐     ┌─────────────────────┐              │
│ VideoStreamManager  │────▶│  Transit Protocol   │              │
│                     │     │  Message Routing    │              │
└─────────────────────┘     └──────────┬──────────┘              │
                                       │                          │
                              Validated Commands                  │
                                       │                          │
                                       ▼                          ▼
                            ┌─────────────────────┐     ┌─────────────────────┐
                            │   Malli Validation  │────▶│ GeneratedHandlers   │
                            │   & Routing         │     │ (Protobuf)          │
                            └─────────────────────┘     └─────────────────────┘
```

## Key Components

### 1. MouseEventHandler (Kotlin)

Processes mouse events and generates high-level commands:

```kotlin
// Gesture detection → Command generation
class MouseEventHandler(
    videoComponent: Component,
    callback: EventCallback,
    streamType: StreamType,
    frameDataProvider: FrameDataProvider? = null
) {
    // Sends commands like:
    // {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.3}}}
}
```

### 2. CommandBuilder (Kotlin)

Creates properly formatted commands with keywords:

```kotlin
object CommandBuilder {
    fun rotaryGotoNDC(streamType: StreamType, ndcX: Double, ndcY: Double): Map<String, Any>
    fun cvStartTrackNDC(streamType: StreamType, ndcX: Double, ndcY: Double, frameTimestamp: Long?)
    fun rotarySetVelocity(azSpeed: Double, elSpeed: Double, azDir: RotaryDirection, elDir: RotaryDirection)
    fun rotaryHalt()
    fun heatCameraNextZoom()
    fun dayCameraNextZoom()
    // etc.
}
```

### 3. Transit Protocol

All communication uses Transit with MessagePack:

```clojure
;; Command from video stream
{:msg-type :command
 :msg-id "uuid"
 :timestamp 1234567890
 :payload {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.3}}}}

;; Event from video stream
{:msg-type :event
 :msg-id "uuid"
 :timestamp 1234567890
 :payload {:type :gesture
           :gesture-type :tap
           :x 400 :y 300
           :ndc-x 0.0 :ndc-y 0.0
           :stream-type :heat}}
```

### 4. Shared Components

#### NDCConverter (Java)
```java
public class NDCConverter {
    public static NDCPoint pixelToNDC(int x, int y, int width, int height)
    public static PixelPoint ndcToPixel(double ndcX, double ndcY, int width, int height)
}
```

#### Video Stream Specs (Malli)
```clojure
(ns potatoclient.specs.video.stream)
;; Defines all valid command structures
;; Used by mock tool and main app
```

## Command Reference

### Gesture → Command Mapping

| Gesture | Command | Example |
|---------|---------|---------|
| Tap | rotary-goto-ndc | `{:rotary {:goto-ndc {:channel :heat :x 0.0 :y 0.0}}}` |
| Double-tap | cv-start-track-ndc | `{:cv {:start-track-ndc {:channel :heat :x 0.5 :y -0.3 :frame-time 12345}}}` |
| Pan | rotary-set-velocity | `{:rotary {:set-velocity {:azimuth-speed 0.1 :elevation-speed 0.05 :azimuth-direction :clockwise :elevation-direction :counter-clockwise}}}` |
| Pan stop | rotary-halt | `{:rotary {:halt {}}}` |
| Wheel up | zoom in | `{:heat-camera {:next-zoom-table-pos {}}}` |
| Wheel down | zoom out | `{:day-camera {:prev-zoom-table-pos {}}}` |

### Keyword Conversions

All enum values are converted to keywords:

| Enum | Keyword |
|------|---------|
| StreamType.HEAT | `:heat` |
| StreamType.DAY | `:day` |
| RotaryDirection.CLOCKWISE | `:clockwise` |
| RotaryDirection.COUNTER_CLOCKWISE | `:counter-clockwise` |
| EventType.GESTURE | `:gesture` |
| EventType.WINDOW | `:window` |
| EventType.CLOSE | `:close` |

## Testing Infrastructure

### Mock Video Stream Tool

Located in `tools/mock-video-stream/`, provides:
- Subprocess mode for integration testing
- Test scenario generation
- Command validation
- Transit protocol compliance testing

Usage:
```bash
# Run as subprocess
make process STREAM_TYPE=heat

# Generate test data
make generate

# Validate scenarios
make validate
```

### Test Files

- `CommandBuilderTest.kt` - Unit tests for command structure
- `CommandTransitFormatTest.kt` - Transit serialization tests
- `VideoStreamCommandIntegrationTest.kt` - End-to-end testing
- `mock-video-stream/*_test.clj` - Mock tool tests

## Development Guidelines

### Adding New Commands

1. Add to `CommandBuilder.kt`:
```kotlin
fun newCommand(param: Type): Map<String, Any> {
    return mapOf(
        "subsystem" to mapOf(
            "command-name" to mapOf(
                "param" to param.toKeyword()  // Always use keywords!
            )
        )
    )
}
```

2. Add to video stream specs:
```clojure
(def new-command
  [:map
   [:subsystem [:map
                [:command-name [:map
                               [:param keyword?]]]]]])
```

3. Add test scenario to mock tool
4. Add unit test to `CommandBuilderTest.kt`

### Common Pitfalls

1. **Always use keywords** - Never send string enum values
2. **Use nested maps** - Commands must match protobuf structure
3. **Include frame time** - CV commands need synchronized timestamps
4. **Test with mock tool** - Validate protocol compliance

## Performance Considerations

- Commands are generated synchronously in gesture callbacks
- Frame data is retrieved from atomic variables (non-blocking)
- Transit serialization is fast (~1ms per command)
- No reflection used (static code generation)

## Future Enhancements

1. **Swipe Gestures** - Infrastructure ready, not implemented
2. **Multi-touch** - Currently single-touch only
3. **Gesture Customization** - Per-user gesture preferences
4. **Command Batching** - For high-frequency pan updates
5. **Predictive Commands** - Anticipate user intent

## Related Documentation

- [Transit Protocol](.claude/transit-protocol.md)
- [Kotlin Subprocess](.claude/kotlin-subprocess.md)
- [Mock Video Stream Tool](../tools/mock-video-stream/README.md)
- [Protobuf Command System](.claude/protobuf-command-system.md)