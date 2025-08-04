# Kotlin Subprocess Architecture

Detailed architecture of PotatoClient's Kotlin subprocess system.

## Overview

PotatoClient uses separate Kotlin subprocesses to isolate protobuf handling, video processing, and maintain clean architecture boundaries. The main Clojure process never touches protobuf directly.

```
┌─────────────────────┐         Transit          ┌──────────────────────┐
│  Clojure Process    │ ◄──────────────────────► │  Kotlin Subprocesses │
│                     │      MessagePack          │                      │
│  - UI (Swing)       │                          │  - Command Router    │
│  - Business Logic   │                          │  - State Manager     │
│  - Transit Only     │                          │  - Video Streams(2)  │
│  - No Protobuf      │                          │  - Protobuf Handler  │
└─────────────────────┘                          └──────────────────────┘
```

## Subprocess Types

### 1. Command Subprocess

**Purpose**: Routes commands from UI to backend systems

**Responsibilities**:
- Receive Transit commands from Clojure
- Convert to protobuf messages
- Send to hardware/backend
- Return responses via Transit

**Key Classes**:
```kotlin
class CommandSubprocess : SubprocessBase() {
    private val commandHandler = GeneratedCommandHandlers()
    private val protoClient = ProtobufWebSocketClient()
    
    override fun handleMessage(message: TransitMessage) {
        when (message.msgType) {
            "command" -> routeCommand(message.data)
            "ping" -> respondPong(message.correlationId)
        }
    }
}
```

### 2. State Subprocess

**Purpose**: Manages system state updates

**Responsibilities**:
- Receive protobuf state from backend
- Convert to Transit format
- Stream to Clojure process
- Maintain state consistency

**Key Classes**:
```kotlin
class StateSubprocess : SubprocessBase() {
    private val stateHandler = GeneratedStateHandlers()
    private var lastState: SystemState? = null
    
    override fun handleStateUpdate(protoState: ProtoState) {
        val transitState = stateHandler.toTransit(protoState)
        if (hasChanged(transitState, lastState)) {
            sendToClojure(transitState)
            lastState = transitState
        }
    }
}
```

### 3. Video Stream Subprocesses (2)

**Purpose**: Handle H.264 video decoding and display

**Responsibilities**:
- Decode H.264 streams
- Manage GStreamer pipelines
- Handle gesture recognition
- Render to Swing components

**Key Classes**:
```kotlin
class VideoStreamManager(
    private val streamType: StreamType, // HEAT or DAY
    private val display: VideoDisplay
) {
    private val pipeline = GStreamerPipeline()
    private val gestureRecognizer = GestureRecognizer()
    private val panController = PanController()
    
    fun start(url: String) {
        pipeline.play(url, display.videoSink)
    }
}
```

## Communication Protocol

### Transit over Framed I/O

All subprocess communication uses Transit with MessagePack encoding over framed I/O:

```kotlin
// Frame format: [4-byte length][message data]
class FramedTransitCommunicator {
    fun sendMessage(msg: Any) {
        val bytes = transit.write(msg)
        output.writeInt(bytes.size)  // Big-endian
        output.write(bytes)
        output.flush()
    }
    
    fun readMessage(): Any? {
        val length = input.readInt()
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return transit.read(bytes)
    }
}
```

### Message Flow

#### Command Flow
```
UI Action → Clojure → Transit Command → Command Subprocess
                                          ↓
                                     Protobuf Command
                                          ↓
                                     Backend System
```

#### State Flow
```
Backend System → Protobuf State → State Subprocess
                                     ↓
                                 Transit State
                                     ↓
                                 Clojure → UI Update
```

## Process Lifecycle

### Startup Sequence

```kotlin
class SubprocessLauncher {
    fun launchSubprocess(type: SubprocessType): Process {
        val command = listOf(
            "java",
            "-cp", getClasspath(),
            "-Xmx${getMemoryLimit(type)}",
            getMainClass(type),
            "--type", type.name
        )
        
        return ProcessBuilder(command)
            .redirectError(getLogFile(type))
            .start()
    }
}
```

### Initialization

1. **Process Start**: JVM process launched
2. **Transit Setup**: Initialize readers/writers
3. **Connection**: Establish stdio communication
4. **Ready Signal**: Send "subprocess-ready" message
5. **Main Loop**: Enter message processing

### Shutdown

```kotlin
override fun shutdown() {
    running = false
    
    // Graceful shutdown
    pipeline?.stop()
    transitComm.close()
    protoClient?.disconnect()
    
    // Cleanup resources
    threadPool.shutdown()
    threadPool.awaitTermination(5, TimeUnit.SECONDS)
}
```

## Key Components

### Generated Handlers

Auto-generated code for Transit ↔ Protobuf conversion:

```kotlin
// Generated from protobuf definitions
object GeneratedCommandHandlers {
    fun handleCommand(transitData: Map<String, Any>): ProtoMessage {
        return when (val type = transitData.keys.first()) {
            "rotary" -> handleRotaryCommand(transitData["rotary"])
            "cv" -> handleCVCommand(transitData["cv"])
            "system" -> handleSystemCommand(transitData["system"])
            // ... more command types
        }
    }
}
```

### Video Pipeline Management

```kotlin
class GStreamerPipeline {
    private val decoders = listOf(
        "nvh264dec",      // NVIDIA hardware
        "msdkh264dec",    // Intel Media SDK
        "vaapih264dec",   // VA-API
        "avdec_h264"      // Software fallback
    )
    
    fun createPipeline(sink: Element): Pipeline {
        return Pipeline().apply {
            add(AppSrc("source"))
            add(Element.factory("h264parse"))
            add(selectDecoder())
            add(Element.factory("videoconvert"))
            add(sink)
            linkAll()
        }
    }
}
```

### Gesture Recognition

```kotlin
class GestureRecognizer {
    private var state = GestureState.IDLE
    private val config = GestureConfig.load()
    
    fun processMouseEvent(event: MouseEvent): Gesture? {
        return when (state) {
            IDLE -> handleIdleState(event)
            PENDING -> handlePendingState(event)
            PANNING -> handlePanningState(event)
        }
    }
}
```

## Memory Management

### Buffer Pooling

```kotlin
object ByteBufferPool {
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()
    
    fun acquire(size: Int): ByteBuffer {
        return pool.poll()?.clear() 
            ?: ByteBuffer.allocateDirect(size)
    }
    
    fun release(buffer: ByteBuffer) {
        if (pool.size < MAX_POOL_SIZE) {
            pool.offer(buffer)
        }
    }
}
```

### Zero-Copy Streaming

Video frames use direct buffers for zero-copy transfer:

```kotlin
class VideoFrameHandler {
    override fun handleFrame(frame: VideoFrame) {
        val buffer = ByteBufferPool.acquire(frame.size)
        try {
            frame.copyTo(buffer)
            processFrame(buffer)
        } finally {
            ByteBufferPool.release(buffer)
        }
    }
}
```

## Error Handling

### Process Monitoring

```kotlin
class SubprocessMonitor {
    fun monitorProcess(process: Process, type: SubprocessType) {
        thread {
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                handleCrash(type, exitCode)
            }
        }
    }
    
    private fun handleCrash(type: SubprocessType, exitCode: Int) {
        logger.error("Subprocess $type crashed with code $exitCode")
        notifyClojure(ProcessCrashEvent(type, exitCode))
        attemptRestart(type)
    }
}
```

### Communication Errors

```kotlin
override fun handleError(e: Exception) {
    when (e) {
        is EOFException -> handleDisconnect()
        is SocketTimeoutException -> handleTimeout()
        is TransitException -> handleBadMessage(e)
        else -> handleUnexpectedError(e)
    }
}
```

## Performance Considerations

### Threading Model

- **Main thread**: Message processing
- **I/O thread**: Transit communication
- **Video threads**: One per stream
- **Worker pool**: Async operations

### Optimization Strategies

1. **Message Batching**: Group state updates
2. **Lazy Conversion**: Convert only changed fields
3. **Direct Buffers**: For video data
4. **Object Pooling**: Reuse expensive objects

## Configuration

### JVM Options

```bash
# Command subprocess (lightweight)
-Xmx256m -XX:+UseG1GC

# State subprocess (moderate)
-Xmx512m -XX:+UseG1GC

# Video subprocess (heavy)
-Xmx1g -XX:MaxDirectMemorySize=512m
```

### Subprocess Configuration

```clojure
{:subprocesses
 {:command {:memory "256m"
            :restart-on-crash true
            :max-restarts 3}
  :state {:memory "512m"
          :update-interval 100}
  :video {:memory "1g"
          :direct-memory "512m"
          :decoder-preference ["nvh264dec" "msdkh264dec"]}}}
```

## Debugging

### Logging

Each subprocess has its own log file:

```
logs/
├── command-subprocess-20240104-120000.log
├── state-subprocess-20240104-120000.log
├── video-stream-heat-20240104-120000.log
└── video-stream-day-20240104-120000.log
```

### Debug Mode

```kotlin
// Enable verbose Transit logging
System.setProperty("transit.debug", "true")

// Enable GStreamer debugging
System.setenv("GST_DEBUG", "3")

// Enable protobuf logging
LogManager.getLogger("protobuf").level = Level.ALL
```

### Common Issues

**Subprocess won't start**
- Check classpath includes all dependencies
- Verify Java version compatibility
- Look for startup errors in stderr

**Transit decode errors**
- Enable Transit debug logging
- Check message format matches schema
- Verify keyword conversion

**Video not displaying**
- Check GStreamer installation
- Verify decoder availability
- Test with software decoder

## Best Practices

### Do's

1. ✓ Keep subprocesses focused on single responsibility
2. ✓ Use generated code for Transit ↔ Protobuf
3. ✓ Pool expensive resources
4. ✓ Handle all error cases gracefully
5. ✓ Log important state transitions

### Don'ts

1. ✗ Don't share mutable state between threads
2. ✗ Don't block the message processing thread
3. ✗ Don't ignore subprocess crashes
4. ✗ Don't manually write Transit conversion
5. ✗ Don't bypass the communication protocol

## See Also

- [System Overview](./system-overview.md)
- [Transit Protocol](./transit-protocol.md)
- [Video Streaming](./video-streaming.md)
- [Debugging Subprocesses](../guides/debugging-subprocesses.md)