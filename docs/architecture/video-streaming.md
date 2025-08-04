# Video Streaming Architecture

The video streaming subsystem handles dual H.264 streams with hardware acceleration, gesture recognition, and command generation. This document covers the architecture after the August 2025 refactoring.

## Architecture Overview

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

### VideoStreamManager (Kotlin)

Main coordinator for each video stream subprocess:
- Manages WebSocket connection for H.264 stream
- Coordinates GStreamer pipeline
- Handles frame timing and metadata
- Routes commands and events via Transit

### MouseEventHandler (Kotlin)

Processes raw mouse events into high-level commands:
- Gesture recognition (tap, double-tap, pan, swipe)
- NDC coordinate conversion
- Zoom-based speed calculation
- Command generation via CommandBuilder

### GStreamerPipeline (Kotlin)

Hardware-accelerated video decoding:
- Automatic decoder selection (NVIDIA > Intel QSV > VA-API > Software)
- Zero-allocation buffer management
- Direct rendering to Swing component
- Frame timing extraction

### CommandBuilder (Kotlin)

Creates properly formatted Transit commands:
```kotlin
object CommandBuilder {
    fun rotaryGotoNDC(streamType: StreamType, ndcX: Double, ndcY: Double)
    fun cvStartTrackNDC(streamType: StreamType, ndcX: Double, ndcY: Double, frameTime: Long?)
    fun rotarySetVelocity(azSpeed: Double, elSpeed: Double, azDir: RotaryDirection, elDir: RotaryDirection)
    fun rotaryHalt()
    // Camera zoom commands per stream type
}
```

## Gesture System

### Supported Gestures

| Gesture | Detection | Command Generated |
|---------|-----------|------------------|
| **Tap** | Single click < 200ms | `rotary-goto-ndc` |
| **Double-tap** | Two clicks < 300ms apart | `cv-start-track-ndc` |
| **Pan** | Click + drag > 5px | `rotary-set-velocity` |
| **Pan stop** | Release after pan | `rotary-halt` |
| **Wheel up** | Mouse wheel up | Stream-specific zoom in |
| **Wheel down** | Mouse wheel down | Stream-specific zoom out |

### Gesture Configuration

Configured in `resources/config/gestures.edn`:
```clojure
{:tap-duration-ms 200
 :double-tap-interval-ms 300
 :movement-threshold 5
 :zoom-speed-config {...}}
```

### NDC Coordinate System

Normalized Device Coordinates (NDC) provide resolution-independent positioning:
- Range: [-1, 1] for both axes
- Center: (0, 0)
- Conversion: `ndcX = (2 * pixelX / width) - 1`
- Y-axis inverted: `ndcY = -((2 * pixelY / height) - 1)`

### Zoom-Based Speed Control

Pan gesture speed varies by zoom level:
```clojure
{:zoom-speed-config
 {:heat {:zoom-table-index {0 {:max-rotation-speed 1.0}
                           1 {:max-rotation-speed 0.7}
                           2 {:max-rotation-speed 0.5}
                           3 {:max-rotation-speed 0.3}
                           4 {:max-rotation-speed 0.1}}}}}
```

## Command Flow

### From Gesture to Server

1. **Mouse Event** → VideoComponent
2. **Gesture Recognition** → MouseEventHandler detects gesture type
3. **Command Generation** → CommandBuilder creates Transit command
4. **Transit Message** → VideoStreamManager sends via Transit protocol
5. **Main Process Routing** → Validates and forwards to Command subprocess
6. **Protobuf Conversion** → Generated handlers convert to protobuf
7. **WebSocket Send** → Command subprocess sends to server

### Example Command

```clojure
;; User double-taps at screen center on heat stream
{:msg-type :command
 :msg-id "550e8400-e29b-41d4-a716-446655440000"
 :timestamp 1627849200000
 :payload {:cv {:start-track-ndc {:channel :heat
                                  :x 0.0
                                  :y 0.0
                                  :frame-time 167234500}}}}
```

## Performance Optimizations

### Zero-Allocation Streaming
- Pre-allocated ByteBuffers for video frames
- Buffer pool with size limits
- Lock-free buffer recycling
- Direct memory for native interop

### Hardware Acceleration
- Automatic decoder selection based on availability
- Direct pipeline without color conversion
- Hardware-specific optimizations (NVDEC, QSV, VA-API)

### Frame Data Access
- Atomic variables for current frame metadata
- Non-blocking access from gesture handlers
- Synchronized with video presentation

## Testing Infrastructure

### Mock Video Stream Tool

Located in `tools/mock-video-stream/`:
- Simulates video stream subprocess behavior
- Generates test scenarios
- Validates command structure
- Contract-first testing approach

### Test Coverage

- Unit tests for gesture recognition
- Integration tests for command flow
- Mock tool for subprocess testing
- Scenario-based validation

## Configuration

### Stream Types

| Stream | Resolution | Use Case |
|--------|------------|----------|
| Heat | 900×720 | Thermal imaging |
| Day | 1920×1080 | Visible spectrum |

### Video Pipeline

Default GStreamer pipeline:
```
appsrc → h264parse → decoder → videoconvert → autovideosink
```

Decoder selection priority:
1. NVIDIA (nvh264dec)
2. Intel QSV (qsvh264dec)
3. VA-API (vaapih264dec)
4. Software (avdec_h264)

## Development Guidelines

### Adding New Gestures

1. Update `GestureRecognizer.kt` with detection logic
2. Add gesture type to `GestureType` enum
3. Create command in `CommandBuilder`
4. Add handler in Clojure
5. Update gesture configuration
6. Add tests

### Adding New Commands

1. Define command structure in `CommandBuilder.kt`
2. Add Malli spec in `video.stream` namespace
3. Create test scenario in mock tool
4. Update documentation

### Debugging Video Streams

- Enable GStreamer debug: `GST_DEBUG=3`
- Check individual subprocess logs in `./logs/`
- Use mock video stream tool for isolation
- Monitor frame timing with metrics

## See Also

- [Transit Protocol](./transit-protocol.md) - Message specifications
- [Command System](./command-system.md) - Command routing
- [Mock Video Stream Tool](../tools/mock-video-stream.md) - Testing tool