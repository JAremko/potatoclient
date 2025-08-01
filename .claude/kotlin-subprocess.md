# Kotlin Subprocess Architecture

This document contains detailed information about the Kotlin subprocess implementation in PotatoClient. The Kotlin subprocesses handle high-performance video streaming with hardware acceleration and zero-allocation design.

## Overview

PotatoClient uses Kotlin subprocesses to handle dual H.264 WebSocket streams (Heat: 900x720, Day: 1920x1080). Each stream runs in a separate process with optimized memory management and hardware acceleration.

## Key Components

### VideoStreamManager
The main coordinator for WebSocket and GStreamer pipeline integration. Handles:
- WebSocket connection lifecycle
- Stream data routing to GStreamer
- Event dispatch to main process via IPC
- Graceful shutdown and resource cleanup

### WebSocketClientBuiltIn
Leverages Java 17's built-in HttpClient with Kotlin coroutine optimizations:
- Coroutine-friendly async operations
- Automatic reconnection logic
- SSL certificate handling (trust-all for development)
- Efficient binary frame processing

### ByteBufferPool
High-performance lock-free buffer pool implementation:
- Cache-line padding to prevent false sharing
- Pre-allocated DirectByteBuffers
- Automatic buffer recycling
- Thread-safe without locks on hot path

### GStreamerPipeline
Zero-copy video pipeline with hardware acceleration:
- Dynamic decoder selection based on hardware availability
- Try-lock patterns for non-blocking operations
- Direct pipeline: `appsrc → h264parse → decoder → queue → videosink`
- No unnecessary color space conversions

## Performance Optimizations

### Zero-Allocation Streaming
- Dual buffer pools: WebSocket and GStreamer
- Pre-allocated objects on hot path
- Thread-local storage for frequently used objects
- Direct ByteBuffers for optimal native interop

### Lock-Free Design
- Lock-free buffer pool implementation
- Try-lock patterns in pipeline to avoid blocking
- Atomic operations for state management
- Minimal synchronization overhead

### Hardware Acceleration
Decoder priority (automatically selected):
1. **NVIDIA** (nvh264dec) - Highest performance on NVIDIA GPUs
2. **Direct3D 11** (d3d11h264dec) - Windows hardware acceleration
3. **Intel QSV** (msdkh264dec) - Intel Quick Sync Video
4. **VA-API/VideoToolbox** - Linux/macOS hardware decoders
5. **Software fallback** - CPU-based decoding when no hardware available

### Memory Management
- Automatic message buffer trimming prevents memory bloat
- Comprehensive performance metrics and pool statistics
- DirectByteBuffer usage for zero-copy native operations
- Careful lifecycle management of native resources

## Event System Integration

When adding new event types:
1. Define the event in `potatoclient.events.stream` (Clojure side)
2. Handle in `VideoStreamManager.kt`:
   ```kotlin
   when (eventType) {
       "connected" -> handleConnected(data)
       "disconnected" -> handleDisconnected(data)
       "error" -> handleError(data)
       // Add new event handling here
   }
   ```
3. Add to `ipc/message-handlers` dispatch table in Clojure

## Pipeline Modification

To modify the GStreamer pipeline:

1. **Edit Pipeline Structure**: Modify `GStreamerPipeline.kt`
   - Pipeline elements are created in the `init` block
   - Current pipeline: `appsrc → h264parse → decoder → queue → videosink`

2. **Decoder Priority**: Adjust in `GStreamerPipeline.kt` init block
   ```kotlin
   private val decoderPriority = listOf(
       "nvh264dec",        // NVIDIA
       "d3d11h264dec",     // Direct3D 11
       "msdkh264dec",      // Intel QSV
       // Add or reorder decoders here
   )
   ```

3. **Threading Model**: Uses try-lock patterns to avoid blocking
   ```kotlin
   if (pipelineLock.tryLock()) {
       try {
           // Pipeline operations
       } finally {
           pipelineLock.unlock()
       }
   } else {
       // Handle busy pipeline
   }
   ```

## Transit IPC Integration

### Message Protocol
All Kotlin subprocesses communicate with the main Clojure process using Transit/MessagePack:

```kotlin
// TransitMessageProtocol provides standardized message creation
val messageProtocol = TransitMessageProtocol("video-stream", transitComm)

// Send events with automatic keyword conversion
messageProtocol.sendEvent(
    EventType.WINDOW.key,
    mapOf(
        "type" to EventType.CLOSE.key,  // Becomes :close keyword
        "stream-id" to streamId
    )
)
```

### Keyword Type System
With the automatic keyword conversion system:
- All enum values automatically become keywords in Clojure
- No manual string/keyword conversion needed
- Type safety through Java enums

**Examples**:
```kotlin
// Kotlin sends:
mapOf("channel" to "heat", "action" to "rotary-goto-ndc")

// Clojure receives:
{:channel :heat, :action :rotary-goto-ndc}  // Automatic conversion!
```

### Transit Extensions
Kotlin code uses extension properties for clean keyword-based map access:

```kotlin
// Clean property access instead of map lookups
val msgType = msg.msgType          // Instead of msg["msg-type"]
val action = msg.payload?.action    // Instead of msg["payload"]?.get("action")

// Type-safe with nullability
when (msg.msgType) {
    MessageType.COMMAND.keyword -> handleCommand(msg)
    MessageType.REQUEST.keyword -> handleRequest(msg)
}
```

## Logging Integration

Kotlin subprocesses integrate with the main Clojure logging system:
- Log messages sent via Transit protocol to main process
- Main process controls logging level and destinations
- Consistent logging format across all processes
- Individual log files per subprocess in development mode
- Development mode: All log levels to console and file
- Production mode: Only WARN/ERROR levels

## Build Integration

The Kotlin subprocesses are compiled as part of the main build:

1. **Kotlin Version**: 2.2.0 (downloaded automatically during build)
2. **Compilation Step**: After protobuf generation, before Clojure compilation
3. **Output**: Compiled classes included in final JAR
4. **Dependencies**: Managed through Gradle build in `kotlin/` directory

## Development Guidelines

### Adding Features
1. Maintain zero-allocation principles on hot paths
2. Use coroutines for async operations
3. Prefer try-lock over blocking locks
4. Add performance metrics for new features
5. Test with different hardware decoder configurations

### Debugging
1. Enable GStreamer debug output: `GST_DEBUG=3`
2. Use performance metrics to identify bottlenecks
3. Check buffer pool statistics for leaks
4. Monitor native memory usage

### Testing Hardware Decoders
```bash
# List available decoders
gst-inspect-1.0 | grep h264

# Test specific decoder
GST_DEBUG=3 make dev
# Then connect to a stream and check logs for decoder selection
```

## Common Issues

### Decoder Selection
- If hardware decoder fails, pipeline automatically falls back to software
- Check system logs for decoder initialization errors
- Ensure GStreamer plugins are installed (gst-plugins-bad for hardware decoders)

### Memory Leaks
- Monitor buffer pool statistics in logs
- Check for unreleased DirectByteBuffers
- Verify proper cleanup in shutdown sequence

### Performance Issues
- Check if hardware acceleration is being used
- Monitor buffer pool wait times
- Verify no blocking operations on stream path
- Check for excessive GC pressure

## Future Improvements

1. **Dynamic Quality Adjustment**: Adapt stream quality based on performance
2. **Multi-GPU Support**: Better handling of systems with multiple GPUs
3. **Enhanced Metrics**: More detailed performance instrumentation
4. **Vulkan Video**: Support for newer Vulkan-based decoders